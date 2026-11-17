package edu.orgresource.extended.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public final class ExtendedUserPrincipal implements UserDetails, CredentialsContainer, Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private final long accountId;
    private final long credentialVersion;
    private final String username;
    private String password;

    public ExtendedUserPrincipal(long accountId, long credentialVersion, String username, String password) {
        this.accountId = accountId; this.credentialVersion = credentialVersion;
        this.username = username; this.password = password;
    }
    public long accountId() { return accountId; }
    public long credentialVersion() { return credentialVersion; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_AUTHENTICATED"));
    }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
    @Override public void eraseCredentials() { password = null; }
}
