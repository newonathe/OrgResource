package edu.orgresource.extended.identity;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.orgresource.extended.api.C1ApiException;
import edu.orgresource.extended.security.C1AccountSessionFilter;
import edu.orgresource.extended.security.C1OrganizationContext;
import edu.orgresource.extended.security.ExtendedUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class C1SessionService {
    private final AuthenticationManager authenticationManager;
    private final SessionAuthenticationStrategy sessionStrategy;
    private final SecurityContextRepository contextRepository;
    private final CsrfTokenRepository csrfRepository;
    private final UserAccountRepository accounts;
    private final MembershipRepository memberships;
    private final OrganizationRepository organizations;
    private final C1AccountService accountService;
    private final Clock clock;

    public C1SessionService(AuthenticationManager authenticationManager,
            SessionAuthenticationStrategy sessionStrategy, SecurityContextRepository contextRepository,
            CsrfTokenRepository csrfRepository, UserAccountRepository accounts,
            MembershipRepository memberships, OrganizationRepository organizations,
            C1AccountService accountService, Clock clock) {
        this.authenticationManager = authenticationManager; this.sessionStrategy = sessionStrategy;
        this.contextRepository = contextRepository; this.csrfRepository = csrfRepository;
        this.accounts = accounts; this.memberships = memberships; this.organizations = organizations;
        this.accountService = accountService; this.clock = clock;
    }

    public IdentityDtos.LoginResult login(String username, String password,
                                          HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, password));
        } catch (org.springframework.security.core.AuthenticationException failure) {
            throw new C1ApiException(401, "AUTH_INVALID", "Username or password is incorrect.");
        }
        sessionStrategy.onAuthentication(authentication, request, response);
        establish(authentication, request, response);
        ExtendedUserPrincipal principal = (ExtendedUserPrincipal) authentication.getPrincipal();
        UserAccount account = accountService.account(principal.accountId());
        return new IdentityDtos.LoginResult(sessionView(account, request), refreshCsrf(request, response));
    }

    public IdentityDtos.LoginResult establishNewAccount(long accountId, HttpServletRequest request,
            HttpServletResponse response) {
        UserAccount account = accountService.account(accountId);
        ExtendedUserPrincipal principal = new ExtendedUserPrincipal(account.id, account.credentialVersion,
                account.usernameKey, null);
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());
        sessionStrategy.onAuthentication(authentication, request, response);
        establish(authentication, request, response);
        return new IdentityDtos.LoginResult(sessionView(account, request), refreshCsrf(request, response));
    }

    public String refreshCsrf(HttpServletRequest request, HttpServletResponse response) {
        csrfRepository.saveToken(null, request, response);
        CsrfToken token = csrfRepository.generateToken(request);
        csrfRepository.saveToken(token, request, response);
        return token.getToken();
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        var session = request.getSession(false);
        if (session != null) session.invalidate();
    }

    public void replacePrincipalAfterPasswordChange(UserAccount account, HttpServletRequest request,
                                                     HttpServletResponse response) {
        var existing = request.getSession(false);
        String priorId = existing == null ? null : existing.getId();
        if (existing != null) {
            request.changeSessionId();
            var current = request.getSession(false);
            current.setAttribute(C1AccountSessionFilter.SESSION_CREATED_AT, clock.millis());
            current.setAttribute(C1AccountSessionFilter.LAST_REAUTH_AT, clock.millis());
        }
        ExtendedUserPrincipal principal = new ExtendedUserPrincipal(account.id, account.credentialVersion,
                account.usernameKey, null);
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
        refreshCsrf(request, response);
    }

    public IdentityDtos.SessionView currentSession(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof ExtendedUserPrincipal principal))
            throw new C1ApiException(401, "AUTH_REQUIRED", "Authentication is required.");
        return sessionView(accountService.account(principal.accountId()), request);
    }

    @Transactional(readOnly = true)
    public List<IdentityDtos.OrganizationView> organizations(long accountId) {
        List<IdentityDtos.OrganizationView> result = new ArrayList<>();
        for (Membership membership : memberships.findByUserIdAndStatusOrderByOrganizationId(
                accountId, Membership.Status.ACTIVE)) {
            Organization organization = organizations.findById(membership.organizationId).orElse(null);
            if (organization != null && organization.status == Organization.Status.ACTIVE)
                result.add(new IdentityDtos.OrganizationView(organization.publicId, organization.slug,
                        organization.displayName, membership.role.name()));
        }
        return List.copyOf(result);
    }

    @Transactional
    public IdentityDtos.SessionView selectOrganization(long accountId, UUID publicOrgId,
            HttpServletRequest request, HttpServletResponse response) {
        Organization organization = organizations.findByPublicId(publicOrgId).orElseThrow(C1SessionService::notFound);
        if (organization.status != Organization.Status.ACTIVE) throw new C1ApiException(404, "NOT_FOUND", "The requested organization or membership was not found.");
        Membership membership = memberships.findByOrganizationIdAndUserId(organization.id, accountId)
                .filter(row -> row.status == Membership.Status.ACTIVE).orElseThrow(C1SessionService::notFound);
        UserAccount account = accountService.account(accountId);
        request.changeSessionId();
        request.getSession(true).setAttribute(C1OrganizationContext.ACTIVE_ORG_ID, organization.id);
        refreshCsrf(request, response);
        SecurityContext context = SecurityContextHolder.getContext();
        contextRepository.saveContext(context, request, response);
        return sessionView(account, request);
    }

    public IdentityDtos.SessionView sessionView(UserAccount account, HttpServletRequest request) {
        List<IdentityDtos.MemberView> memberViews = new ArrayList<>();
        for (Membership membership : memberships.findByUserIdAndStatusOrderByOrganizationId(
                account.id, Membership.Status.ACTIVE)) {
            Organization organization = organizations.findById(membership.organizationId).orElse(null);
            if (organization == null) continue;
            memberViews.add(new IdentityDtos.MemberView(membership.publicId, organization.publicId,
                    account.usernameKey, account.displayName, membership.role.name(), membership.status.name(), membership.version));
        }
        memberViews.sort(Comparator.comparing(view -> view.organizationId().toString()));
        UUID activeOrg = null;
        var session = request.getSession(false);
        Object stored = session == null ? null : session.getAttribute(C1OrganizationContext.ACTIVE_ORG_ID);
        if (stored instanceof Long orgId) {
            Organization organization = organizations.findById(orgId).orElse(null);
            boolean member = memberships.findByOrganizationIdAndUserId(orgId, account.id)
                    .filter(row -> row.status == Membership.Status.ACTIVE).isPresent();
            if (organization != null && member) activeOrg = organization.publicId;
            else session.removeAttribute(C1OrganizationContext.ACTIVE_ORG_ID);
        }
        return new IdentityDtos.SessionView(account.publicId, account.displayName, activeOrg, List.copyOf(memberViews));
    }

    private void establish(Authentication authentication, HttpServletRequest request,
                           HttpServletResponse response) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute(C1AccountSessionFilter.SESSION_CREATED_AT, clock.millis());
        request.getSession(false).setAttribute(C1AccountSessionFilter.LAST_REAUTH_AT, clock.millis());
        contextRepository.saveContext(context, request, response);
    }

    private static LocalDateTime now(Clock clock) { return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC); }
    private static C1ApiException notFound() { return new C1ApiException(404, "NOT_FOUND", "The requested organization or membership was not found."); }
}
