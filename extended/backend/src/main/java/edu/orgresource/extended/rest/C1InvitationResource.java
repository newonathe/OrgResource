package edu.orgresource.extended.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import edu.orgresource.extended.api.C1ApiException;
import edu.orgresource.extended.api.C1RequestValidator;
import edu.orgresource.extended.identity.C1GovernanceService;
import edu.orgresource.extended.identity.C1HttpResponses;
import edu.orgresource.extended.identity.C1Secrets;
import edu.orgresource.extended.identity.C1SessionService;
import edu.orgresource.extended.identity.IdentityDtos;
import edu.orgresource.extended.identity.Organization;
import edu.orgresource.extended.identity.OrganizationRepository;
import edu.orgresource.extended.identity.C1AttemptThrottle;
import edu.orgresource.extended.security.ExtendedUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Component
@Path("/v2/invitations")
@Produces(MediaType.APPLICATION_JSON)
public class C1InvitationResource {
    @Autowired C1GovernanceService governance;
    @Autowired C1SessionService sessions;
    @Autowired OrganizationRepository organizations;
    @Autowired C1RequestValidator validator;
    @Autowired C1AttemptThrottle throttle;

    @POST @Path("/redeem") @Consumes(MediaType.APPLICATION_JSON)
    public Response redeem(IdentityDtos.RedeemInvitationRequest body,
            @Context HttpServletRequest request, @Context HttpServletResponse response) {
        if (body == null) throw new C1ApiException(400, "VALIDATION_FAILED", "An invitation token is required.");
        validator.validate(body);
        String sourceKey = "redeem:" + hex(C1Secrets.sha256(String.valueOf(body.bearerToken))) + ":" + request.getRemoteAddr();
        if (!throttle.allow(sourceKey))
            throw new C1ApiException(429, "RATE_LIMITED", "Too many attempts. Try again later.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long existingUserId = authentication != null && authentication.getPrincipal() instanceof ExtendedUserPrincipal principal
                ? principal.accountId() : null;
        C1GovernanceService.Redemption result = governance.redeem(body.bearerToken, existingUserId,
                body.username, body.password, body.displayName);
        Organization organization = organizations.findById(result.membership().organizationId)
                .orElseThrow(() -> new C1ApiException(404, "INVITE_INVALID", "The invitation is unavailable or no longer active."));
        IdentityDtos.SessionView session = null;
        String csrf = null;
        if (result.newAccount()) {
            var established = sessions.establishNewAccount(result.accountId(), request, response);
            session = established.session(); csrf = established.csrfToken();
        }
        throttle.success(sourceKey);
        var view = new IdentityDtos.InvitationRedemption(organization.publicId,
                result.membership().publicId, result.membership().role.name(), result.newAccount(),
                result.newAccount() ? result.recoveryCodes() : List.of(), session, csrf);
        return C1HttpResponses.envelope(result.newAccount() ? 201 : 200, view, request, true);
    }

    private static String hex(byte[] value) {
        return java.util.HexFormat.of().formatHex(value);
    }
}
