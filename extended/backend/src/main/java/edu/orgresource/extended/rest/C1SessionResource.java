package edu.orgresource.extended.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import edu.orgresource.extended.api.C1ApiException;
import edu.orgresource.extended.api.C1RequestValidator;
import edu.orgresource.extended.identity.C1AccountService;
import edu.orgresource.extended.identity.C1HttpResponses;
import edu.orgresource.extended.identity.C1SessionRevocationService;
import edu.orgresource.extended.identity.C1SessionService;
import edu.orgresource.extended.identity.IdentityDtos;
import edu.orgresource.extended.identity.UserAccount;
import edu.orgresource.extended.identity.UserAccountRepository;
import edu.orgresource.extended.security.C1OrganizationContext;
import edu.orgresource.extended.security.ExtendedUserPrincipal;
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

@Component
@Path("/v2")
@Produces(MediaType.APPLICATION_JSON)
public class C1SessionResource {
    @Autowired C1SessionService sessions;
    @Autowired C1AccountService accounts;
    @Autowired C1SessionRevocationService revocations;
    @Autowired UserAccountRepository users;
    @Autowired C1OrganizationContext organizationContext;
    @Autowired C1RequestValidator validator;

    @GET @Path("/session")
    public Response session(@Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, sessions.currentSession(request), request, true);
    }

    @GET @Path("/orgs")
    public Response organizations(@Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, sessions.organizations(organizationContext.currentUserId()), request, true);
    }

    @POST @Path("/session/active-org") @Consumes(MediaType.APPLICATION_JSON)
    public Response activeOrganization(IdentityDtos.ActiveOrganizationRequest body,
            @Context HttpServletRequest request, @Context HttpServletResponse response) {
        validator.validate(body);
        return C1HttpResponses.envelope(200, sessions.selectOrganization(
                organizationContext.currentUserId(), body.organizationId, request, response), request, true);
    }

    @GET @Path("/account")
    public Response account(@Context HttpServletRequest request) {
        UserAccount account = accounts.account(organizationContext.currentUserId());
        return C1HttpResponses.envelope(200,
                new IdentityDtos.AccountView(account.publicId, account.usernameKey, account.displayName, account.status.name()),
                request, true);
    }

    @POST @Path("/account/password") @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(IdentityDtos.PasswordRequest body, @Context HttpServletRequest request,
            @Context HttpServletResponse response) {
        validator.validate(body);
        long userId = organizationContext.currentUserId();
        accounts.changePassword(userId, body.currentPassword, body.newPassword);
        UserAccount updated = users.findById(userId).orElseThrow(C1SessionResource::unauthenticated);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth.getPrincipal() instanceof ExtendedUserPrincipal principal)) throw unauthenticated();
        revocations.revokeOthers(principal.getUsername(), request.getSession(false).getId());
        sessions.replacePrincipalAfterPasswordChange(updated, request, response);
        return C1HttpResponses.envelope(200, new IdentityDtos.Message("Password changed and prior sessions revoked."), request, true);
    }

    @POST @Path("/account/recovery-codes/rotate") @Consumes(MediaType.APPLICATION_JSON)
    public Response rotateRecoveryCodes(IdentityDtos.CurrentPasswordRequest body, @Context HttpServletRequest request) {
        validator.validate(body);
        return C1HttpResponses.envelope(200,
                new IdentityDtos.RecoveryCodes(accounts.rotateRecoveryCodes(organizationContext.currentUserId(), body.currentPassword)),
                request, true);
    }

    @POST @Path("/account/disable") @Consumes(MediaType.APPLICATION_JSON)
    public Response disable(IdentityDtos.CurrentPasswordRequest body, @Context HttpServletRequest request,
            @Context HttpServletResponse response) {
        validator.validate(body);
        long userId = organizationContext.currentUserId();
        UserAccount current = users.findById(userId).orElseThrow(C1SessionResource::unauthenticated);
        accounts.disable(userId, body.currentPassword);
        revocations.revokeAll(current.usernameKey);
        sessions.logout(request, response);
        return Response.noContent().build();
    }

    private static C1ApiException unauthenticated() { return new C1ApiException(401, "AUTH_REQUIRED", "Authentication is required."); }
}
