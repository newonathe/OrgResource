package edu.orgresource.extended.security;

import java.util.Arrays;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import edu.orgresource.extended.api.C1ApiException;
import edu.orgresource.extended.identity.Membership;
import edu.orgresource.extended.identity.MembershipRepository;
import edu.orgresource.extended.identity.Organization;
import edu.orgresource.extended.identity.OrganizationRepository;
import edu.orgresource.extended.identity.UserAccount;
import edu.orgresource.extended.identity.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Component
public class C1OrganizationContext {
    public static final String ACTIVE_ORG_ID = "org.orgresource.activeOrganizationId";
    private final UserAccountRepository accounts;
    private final MembershipRepository memberships;
    private final OrganizationRepository organizations;

    public C1OrganizationContext(UserAccountRepository accounts, MembershipRepository memberships,
                                 OrganizationRepository organizations) {
        this.accounts = accounts; this.memberships = memberships; this.organizations = organizations;
    }

    public long currentUserId() { return principal().accountId(); }

    public Long currentOrganizationId() {
        HttpSession session = currentSession();
        Object stored = session == null ? null : session.getAttribute(ACTIVE_ORG_ID);
        if (!(stored instanceof Long orgId)) throw new C1ApiException(409, "ORG_CONTEXT_REQUIRED", "Select an active organization.");
        requireMembership(orgId, false);
        return orgId;
    }

    public Long requireMutationOrganizationId() {
        Long orgId = currentOrganizationId();
        requireMembership(orgId, true);
        return orgId;
    }

    public Membership requirePathOrganization(java.util.UUID publicOrgId, boolean mutation,
                                               Membership.Role... roles) {
        ExtendedUserPrincipal principal = principal();
        Organization org = organizations.findByPublicId(publicOrgId)
                .orElseThrow(() -> notFound());
        Membership membership = memberships.findByOrganizationIdAndUserId(org.id, principal.accountId())
                .filter(row -> row.status == Membership.Status.ACTIVE)
                .orElseThrow(C1OrganizationContext::notFound);
        UserAccount account = accounts.findById(principal.accountId()).orElseThrow(C1OrganizationContext::unauthenticated);
        if (account.status != UserAccount.Status.ACTIVE || account.credentialVersion != principal.credentialVersion())
            throw unauthenticated();
        if (mutation && org.status != Organization.Status.ACTIVE)
            throw new C1ApiException(409, "ORG_FROZEN", "A frozen organization is read-only.");
        if (roles.length > 0 && Arrays.stream(roles).noneMatch(role -> role == membership.role))
            throw new C1ApiException(403, "FORBIDDEN", "The current membership cannot perform this action.");
        return membership;
    }

    public Membership requireCurrentRole(long orgId, Membership.Role... roles) {
        Membership membership = requireMembership(orgId, true);
        if (Arrays.stream(roles).noneMatch(role -> role == membership.role))
            throw new C1ApiException(403, "FORBIDDEN", "The current membership cannot perform this action.");
        return membership;
    }

    private Membership requireMembership(long orgId, boolean mutation) {
        ExtendedUserPrincipal principal = principal();
        UserAccount account = accounts.findById(principal.accountId()).orElseThrow(C1OrganizationContext::unauthenticated);
        if (account.status != UserAccount.Status.ACTIVE || account.credentialVersion != principal.credentialVersion())
            throw unauthenticated();
        Organization org = organizations.findById(orgId).orElseThrow(C1OrganizationContext::notFound);
        Membership membership = memberships.findByOrganizationIdAndUserId(orgId, account.id)
                .filter(row -> row.status == Membership.Status.ACTIVE)
                .orElseThrow(C1OrganizationContext::notFound);
        if (mutation && org.status != Organization.Status.ACTIVE)
            throw new C1ApiException(409, "ORG_FROZEN", "A frozen organization is read-only.");
        return membership;
    }

    private static ExtendedUserPrincipal principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof ExtendedUserPrincipal p))
            throw unauthenticated();
        return p;
    }

    private static HttpSession currentSession() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            return request.getSession(false);
        }
        return null;
    }

    private static C1ApiException unauthenticated() { return new C1ApiException(401, "AUTH_REQUIRED", "Authentication is required."); }
    private static C1ApiException notFound() { return new C1ApiException(404, "NOT_FOUND", "The requested organization or membership was not found."); }
}
