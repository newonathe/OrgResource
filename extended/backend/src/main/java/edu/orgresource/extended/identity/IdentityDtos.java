package edu.orgresource.extended.identity;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public final class IdentityDtos {
    private IdentityDtos() { }

    public record Envelope<T>(T data, String requestId) { }
    public record Error(String code, String message, String requestId) { }
    public record Csrf(String csrfToken) { }
    public record LoginResult(SessionView session, String csrfToken) { }
    public record SessionView(UUID accountId, String displayName, UUID activeOrgId, List<MemberView> memberships) { }
    public record OrganizationView(UUID id, String slug, String displayName, String role) { }
    public record AccountView(UUID id, String username, String displayName, String status) { }
    public record MemberView(UUID id, UUID organizationId, String username, String displayName,
                             String role, String status, long version) { }
    public record GovernanceMemberView(UUID id, String displayName, String role, String status, long version) { }
    public record MemberPage(List<GovernanceMemberView> items, UUID nextCursor) { }
    public record InvitationView(UUID id, UUID organizationId, String role, String status,
                                 String expiresAt, long version, String bearerToken) { }
    public record TransferView(UUID id, UUID organizationId, UUID fromMembershipId, UUID toMembershipId,
                               String status, String expiresAt, String acceptedAt, long version) { }
    public record InvitationRedemption(UUID organizationId, UUID membershipId, String role,
            boolean newAccount, List<String> recoveryCodes, SessionView session, String csrfToken) { }
    public record RecoveryCodes(List<String> codes) { }
    public record Message(String message) { }

    public static class LoginRequest {
        @NotBlank @Size(max = 64) public String username;
        @NotBlank @Size(max = 1024) public String password;
    }
    public static class RecoverRequest {
        @NotBlank @Size(max = 64) public String username;
        @NotBlank @Size(max = 128) public String oneTimeCode;
        @NotBlank @Size(min = 15, max = 1024) public String newPassword;
    }
    public static class ActiveOrganizationRequest {
        @NotNull public UUID organizationId;
    }
    public static class PasswordRequest {
        @NotBlank @Size(max = 1024) public String currentPassword;
        @NotBlank @Size(min = 15, max = 1024) public String newPassword;
    }
    public static class CurrentPasswordRequest {
        @NotBlank @Size(max = 1024) public String currentPassword;
    }
    public static class RedeemInvitationRequest {
        @NotBlank @Size(max = 128) public String bearerToken;
        @Size(max = 64) public String username;
        @Size(min = 15, max = 1024) public String password;
        @Size(max = 120) public String displayName;
    }
    public static class CreateInvitationRequest {
        @NotNull public Membership.Role role;
    }
    public static class ChangeRoleRequest {
        @NotNull public Membership.Role role;
    }
    public static class TransferRequest {
        @NotNull public UUID toMembershipId;
        @NotBlank @Size(max = 1024) public String currentPassword;
    }
    public static class AcceptTransferRequest {
        @NotNull public UUID transferId;
    }
}
