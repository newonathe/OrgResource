package edu.orgresource.extended.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.orgresource.extended.api.C1ApiException;
import edu.orgresource.extended.api.C1RequestValidator;
import edu.orgresource.extended.identity.C1AccountService;
import edu.orgresource.extended.identity.C1AttemptThrottle;
import edu.orgresource.extended.identity.C1HttpResponses;
import edu.orgresource.extended.identity.C1SessionRevocationService;
import edu.orgresource.extended.identity.C1SessionService;
import edu.orgresource.extended.identity.IdentityDtos;
import edu.orgresource.extended.identity.UserAccount;
import edu.orgresource.extended.identity.UserAccountRepository;
import edu.orgresource.extended.security.C1IdentityNormalization;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.security.web.csrf.CsrfToken;

@Component
@Path("/v2/auth")
@Produces(MediaType.APPLICATION_JSON)
public class C1AuthResource {
    @Autowired C1SessionService sessions;
    @Autowired C1AccountService accounts;
    @Autowired C1SessionRevocationService revocations;
    @Autowired UserAccountRepository users;
    @Autowired C1AttemptThrottle throttle;
    @Autowired C1RequestValidator validator;

    @GET @Path("/csrf")
    public Response csrf(@Context HttpServletRequest request) {
        Object value = request.getAttribute(CsrfToken.class.getName());
        if (!(value instanceof CsrfToken)) value = request.getAttribute("_csrf");
        if (!(value instanceof CsrfToken token)) throw new C1ApiException(500, "CSRF_UNAVAILABLE", "CSRF token could not be initialized.");
        return C1HttpResponses.envelope(200, new IdentityDtos.Csrf(token.getToken()), request, true);
    }

    @POST @Path("/login") @Consumes(MediaType.APPLICATION_JSON)
    public Response login(IdentityDtos.LoginRequest body, @Context HttpServletRequest request,
                          @Context HttpServletResponse response) {
        if (body == null) throw new C1ApiException(400, "VALIDATION_FAILED", "Username and password are required.");
        validator.validate(body);
        String key = throttleKey("login", C1IdentityNormalization.username(body.username), request);
        if (!throttle.allow(key)) throw limited();
        try {
            IdentityDtos.LoginResult result = sessions.login(body.username, body.password, request, response);
            throttle.success(key);
            return C1HttpResponses.envelope(200, result, request, true);
        } catch (C1ApiException failure) {
            if ("AUTH_INVALID".equals(failure.getCode())) throw new C1ApiException(401, "AUTH_INVALID", "Username or password is incorrect.");
            throw failure;
        }
    }

    @POST @Path("/recover") @Consumes(MediaType.APPLICATION_JSON)
    public Response recover(IdentityDtos.RecoverRequest body, @Context HttpServletRequest request) {
        if (body == null) throw new C1ApiException(400, "VALIDATION_FAILED", "Recovery details are required.");
        validator.validate(body);
        String username = C1IdentityNormalization.username(body.username);
        String key = throttleKey("recover", username, request);
        if (!throttle.allow(key)) throw limited();
        boolean recovered = accounts.recover(username, body.oneTimeCode, body.newPassword);
        if (recovered) {
            UserAccount account = users.findByUsernameKey(username).orElse(null);
            if (account != null) revocations.revokeAll(account.usernameKey);
            throttle.success(key);
        }
        return C1HttpResponses.envelope(200,
                new IdentityDtos.Message("If the account and recovery code are valid, password recovery has completed."),
                request, true);
    }

    @POST @Path("/logout")
    public Response logout(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        sessions.logout(request, response);
        return Response.noContent().build();
    }

    private static String throttleKey(String action, String accountKey, HttpServletRequest request) {
        String normalized = C1IdentityNormalization.username(accountKey);
        return action + ":" + java.util.HexFormat.of().formatHex(edu.orgresource.extended.identity.C1Secrets.sha256(normalized))
                + ":" + request.getRemoteAddr();
    }
    private static C1ApiException limited() { return new C1ApiException(429, "RATE_LIMITED", "Too many attempts. Try again later."); }
}
