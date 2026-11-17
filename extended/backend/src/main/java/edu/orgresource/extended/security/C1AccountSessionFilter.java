package edu.orgresource.extended.security;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import edu.orgresource.extended.identity.UserAccount;
import edu.orgresource.extended.identity.UserAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public final class C1AccountSessionFilter extends OncePerRequestFilter {
    public static final String SESSION_CREATED_AT = "org.orgresource.sessionCreatedAt";
    public static final String LAST_REAUTH_AT = "org.orgresource.lastReauthAt";
    private final UserAccountRepository accounts;
    private final Clock clock;
    private final Duration absoluteLifetime;

    public C1AccountSessionFilter(UserAccountRepository accounts, Clock clock,
            @Value("${c1.session.absolute-lifetime:12h}") Duration absoluteLifetime) {
        this.accounts = accounts; this.clock = clock; this.absoluteLifetime = absoluteLifetime;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = C1RequestPaths.path(request);
        return !path.startsWith("/api/")
                || "/api/v2/auth/csrf".equals(path)
                || "/api/v2/auth/login".equals(path)
                || "/api/v2/auth/recover".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            chain.doFilter(request, response);
            return;
        }
        if (!(authentication.getPrincipal() instanceof ExtendedUserPrincipal principal)) {
            deny(request, response);
            return;
        }
        var session = request.getSession(false);
        Object createdAt = session == null ? null : session.getAttribute(SESSION_CREATED_AT);
        if (!(createdAt instanceof Long created) || clock.millis() - created > absoluteLifetime.toMillis()) {
            deny(request, response);
            return;
        }
        UserAccount account = accounts.findById(principal.accountId()).orElse(null);
        if (account == null || account.status != UserAccount.Status.ACTIVE
                || account.credentialVersion != principal.credentialVersion()) {
            deny(request, response);
            return;
        }
        chain.doFilter(request, response);
    }

    private static void deny(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        C1SecurityResponses.write(request, response, 401, "AUTH_REQUIRED", "Authentication is required.");
    }
}
