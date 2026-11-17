package edu.orgresource.extended.rest;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.orgresource.extended.identity.C1GovernanceService;
import edu.orgresource.extended.identity.C1HttpResponses;
import edu.orgresource.extended.identity.IdentityDtos;
import edu.orgresource.extended.identity.Invitation;
import edu.orgresource.extended.identity.Membership;
import edu.orgresource.extended.identity.OwnershipTransfer;
import edu.orgresource.extended.identity.UserAccountRepository;
import edu.orgresource.extended.security.C1OrganizationContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Component
@Path("/v2/orgs/{orgId}")
@Produces(MediaType.APPLICATION_JSON)
public class C1GovernanceResource {
    @Autowired C1GovernanceService governance;
    @Autowired C1OrganizationContext organizationContext;
    @Autowired UserAccountRepository users;

    @GET @Path("/members")
    public Response members(@PathParam("orgId") UUID orgId, @QueryParam("limit") Integer limit,
            @QueryParam("cursor") UUID cursor, @Context HttpServletRequest request) {
        int bounded = limit == null ? 50 : Math.max(1, Math.min(100, limit));
        var page = governance.listMembers(organizationContext.currentUserId(), orgId, bounded, cursor);
        return C1HttpResponses.envelope(200, page, request, true);
    }

    @POST @Path("/invitations") @Consumes(MediaType.APPLICATION_JSON)
    public Response invite(@PathParam("orgId") UUID orgId, IdentityDtos.CreateInvitationRequest body,
            @Context HttpServletRequest request) {
        if (body == null || body.role == null)
            throw new edu.orgresource.extended.api.C1ApiException(400, "VALIDATION_FAILED", "A non-owner invitation role is required.");
        C1GovernanceService.IssuedInvitation issued = governance.invite(
                organizationContext.currentUserId(), orgId, body.role);
        Invitation row = issued.invitation();
        var view = new IdentityDtos.InvitationView(row.publicId, orgId, row.role.name(), row.status.name(),
                row.expiresAt.toString(), row.version, issued.token());
        return etag(C1HttpResponses.envelope(201, view, request, true), row.version);
    }

    @POST @Path("/invitations/{invitationId}/revoke")
    public Response revokeInvitation(@PathParam("orgId") UUID orgId, @PathParam("invitationId") UUID invitationId,
            @HeaderParam("If-Match") String ifMatch, @Context HttpServletRequest request) {
        Invitation row = governance.revokeInvitation(organizationContext.currentUserId(), orgId, invitationId, ifMatch);
        var view = new IdentityDtos.InvitationView(row.publicId, orgId, row.role.name(), row.status.name(),
                row.expiresAt.toString(), row.version, null);
        return etag(C1HttpResponses.envelope(200, view, request, true), row.version);
    }

    @POST @Path("/members/{memberId}/role") @Consumes(MediaType.APPLICATION_JSON)
    public Response changeRole(@PathParam("orgId") UUID orgId, @PathParam("memberId") UUID memberId,
            IdentityDtos.ChangeRoleRequest body, @HeaderParam("If-Match") String ifMatch,
            @Context HttpServletRequest request) {
        if (body == null || body.role == null)
            throw new edu.orgresource.extended.api.C1ApiException(400, "VALIDATION_FAILED", "A non-owner membership role is required.");
        Membership member = governance.changeRole(organizationContext.currentUserId(), orgId, memberId, body.role, ifMatch);
        var view = new IdentityDtos.GovernanceMemberView(member.publicId,
                users.findById(member.userId).map(u -> u.displayName).orElse("Unavailable account"),
                member.role.name(), member.status.name(), member.version);
        return etag(C1HttpResponses.envelope(200, view, request, true), member.version);
    }

    @POST @Path("/members/{memberId}/suspend")
    public Response suspend(@PathParam("orgId") UUID orgId, @PathParam("memberId") UUID memberId,
            @HeaderParam("If-Match") String ifMatch, @Context HttpServletRequest request) {
        Membership member = governance.suspend(organizationContext.currentUserId(), orgId, memberId, ifMatch);
        var view = new IdentityDtos.GovernanceMemberView(member.publicId,
                users.findById(member.userId).map(u -> u.displayName).orElse("Unavailable account"),
                member.role.name(), member.status.name(), member.version);
        return etag(C1HttpResponses.envelope(200, view, request, true), member.version);
    }

    @POST @Path("/transfer-ownership") @Consumes(MediaType.APPLICATION_JSON)
    public Response startTransfer(@PathParam("orgId") UUID orgId, IdentityDtos.TransferRequest body,
            @Context HttpServletRequest request) {
        if (body == null || body.toMembershipId == null || body.currentPassword == null)
            throw new edu.orgresource.extended.api.C1ApiException(400, "VALIDATION_FAILED", "Recipient and current password are required.");
        OwnershipTransfer transfer = governance.startTransfer(organizationContext.currentUserId(), orgId,
                body.toMembershipId, body.currentPassword);
        return transferResponse(201, orgId, transfer, request);
    }

    @POST @Path("/accept-transfer") @Consumes(MediaType.APPLICATION_JSON)
    public Response acceptTransfer(@PathParam("orgId") UUID orgId, IdentityDtos.AcceptTransferRequest body,
            @HeaderParam("If-Match") String ifMatch, @Context HttpServletRequest request) {
        if (body == null || body.transferId == null)
            throw new edu.orgresource.extended.api.C1ApiException(400, "VALIDATION_FAILED", "Transfer ID is required.");
        OwnershipTransfer transfer = governance.acceptTransfer(organizationContext.currentUserId(), orgId,
                body.transferId, ifMatch);
        return transferResponse(200, orgId, transfer, request);
    }

    @POST @Path("/transfers/{transferId}/revoke")
    public Response revokeTransfer(@PathParam("orgId") UUID orgId, @PathParam("transferId") UUID transferId,
            @HeaderParam("If-Match") String ifMatch, @Context HttpServletRequest request) {
        OwnershipTransfer transfer = governance.revokeTransfer(organizationContext.currentUserId(), orgId,
                transferId, ifMatch);
        return transferResponse(200, orgId, transfer, request);
    }

    private Response transferResponse(int status, UUID orgId, OwnershipTransfer row, HttpServletRequest request) {
        var view = new IdentityDtos.TransferView(row.publicId, orgId,
                membershipPublicId(row.fromMembershipId), membershipPublicId(row.toMembershipId),
                row.status.name(), row.expiresAt.toString(), row.acceptedAt == null ? null : row.acceptedAt.toString(), row.version);
        return etag(C1HttpResponses.envelope(status, view, request, true), row.version);
    }

    private UUID membershipPublicId(long id) { return governance.membershipPublicId(id); }
    private static Response etag(Response response, long version) {
        return Response.fromResponse(response).tag(new EntityTag("v" + version)).build();
    }
}
