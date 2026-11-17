package edu.orgresource.c0.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import edu.orgresource.c0.api.C0ApiException;

@Component
public class C0OrganizationContext {
    public Long currentOrganizationId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof C0UserPrincipal principal)) {
            throw new C0ApiException(401, "UNAUTHENTICATED", "Authentication is required.");
        }
        return principal.getOrganizationId();
    }
}
