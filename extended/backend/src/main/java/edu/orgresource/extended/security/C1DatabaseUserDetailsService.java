package edu.orgresource.extended.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import edu.orgresource.extended.identity.LocalCredentialRepository;
import edu.orgresource.extended.identity.UserAccount;
import edu.orgresource.extended.identity.UserAccountRepository;

@Service
public class C1DatabaseUserDetailsService implements UserDetailsService {
    private final UserAccountRepository accounts;
    private final LocalCredentialRepository credentials;

    public C1DatabaseUserDetailsService(UserAccountRepository accounts, LocalCredentialRepository credentials) {
        this.accounts = accounts; this.credentials = credentials;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        String key = C1IdentityNormalization.username(username);
        UserAccount account = accounts.findByUsernameKey(key)
                .filter(value -> value.status == UserAccount.Status.ACTIVE)
                .orElseThrow(() -> new UsernameNotFoundException("Authentication failed."));
        String hash = credentials.findByUserId(account.id)
                .map(credential -> credential.passwordHash)
                .orElseThrow(() -> new UsernameNotFoundException("Authentication failed."));
        return new ExtendedUserPrincipal(account.id, account.credentialVersion, account.usernameKey, hash);
    }
}
