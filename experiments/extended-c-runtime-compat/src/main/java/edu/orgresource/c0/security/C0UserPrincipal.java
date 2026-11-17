package edu.orgresource.c0.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public final class C0UserPrincipal implements UserDetails {
    private final String username;
    private final String password;
    private final Long organizationId;
    private final Collection<? extends GrantedAuthority> authorities;

    public C0UserPrincipal(String username, String password, Long organizationId,
                           Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.organizationId = organizationId;
        this.authorities = authorities;
    }

    public Long getOrganizationId() { return organizationId; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
