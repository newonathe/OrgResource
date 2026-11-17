package edu.orgresource.extended.identity;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import edu.orgresource.extended.api.C1ApiException;
import edu.orgresource.extended.security.C1IdentityNormalization;

@Service
public class C1AccountService {
    private final UserAccountRepository accounts;
    private final LocalCredentialRepository credentials;
    private final RecoveryCodeRepository recovery;
    private final MembershipRepository memberships;
    private final OrganizationRepository organizations;
    private final PasswordEncoder encoder;
    private final C1AuditService audit;
    private final Clock clock;
    private final String dummyHash;

    public C1AccountService(UserAccountRepository accounts, LocalCredentialRepository credentials,
            RecoveryCodeRepository recovery, MembershipRepository memberships, OrganizationRepository organizations,
            PasswordEncoder encoder, C1AuditService audit, Clock clock) {
        this.accounts = accounts; this.credentials = credentials; this.recovery = recovery;
        this.memberships = memberships; this.organizations = organizations; this.encoder = encoder;
        this.audit = audit; this.clock = clock;
        this.dummyHash = encoder.encode("C1 recovery timing padding only");
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean recover(String usernameInput, String rawCode, String newPassword) {
        C1BootstrapService.validatePassword(newPassword);
        String username = C1IdentityNormalization.username(usernameInput);
        UserAccount account = accounts.lockByUsernameKey(username).orElse(null);
        if (account == null) { encoder.matches(rawCode == null ? "" : rawCode, dummyHash); return false; }
        if (account == null || account.status != UserAccount.Status.ACTIVE || rawCode == null) {
            encoder.matches(rawCode == null ? "" : rawCode, dummyHash); return false;
        }
        var code = recovery.lockByUserAndHash(account.id, C1Secrets.sha256(rawCode)).orElse(null);
        LocalDateTime now = now();
        if (code == null || code.usedAt != null || code.revokedAt != null || !code.expiresAt.isAfter(now)) {
            encoder.matches(rawCode, dummyHash);
            return false;
        }
        code.usedAt = now;
        recovery.save(code);
        LocalCredential credential = credentials.lockByUserId(account.id).orElse(null);
        if (credential == null) { encoder.matches(rawCode, dummyHash); return false; }
        credential.passwordHash = encoder.encode(newPassword);
        credential.changedAt = now;
        account.credentialVersion++;
        account.updatedAt = now;
        credentials.save(credential);
        accounts.save(account);
        audit.append(null, account.id, "ACCOUNT_RECOVERED", "user_account", account.publicId.toString());
        return true;
    }

    @Transactional
    public void changePassword(long userId, String currentPassword, String newPassword) {
        C1BootstrapService.validatePassword(newPassword);
        UserAccount account = accounts.lockById(userId).orElseThrow(C1AccountService::unauthenticated);
        if (account.status != UserAccount.Status.ACTIVE) throw unauthenticated();
        LocalCredential credential = credentials.lockByUserId(userId).orElseThrow(C1AccountService::unauthenticated);
        if (currentPassword == null || !encoder.matches(currentPassword, credential.passwordHash))
            throw new C1ApiException(401, "CURRENT_PASSWORD_INVALID", "Current password is incorrect.");
        if (encoder.matches(newPassword, credential.passwordHash))
            throw new C1ApiException(422, "PASSWORD_REUSE", "Choose a password different from the current password.");
        LocalDateTime now = now();
        credential.passwordHash = encoder.encode(newPassword);
        credential.changedAt = now;
        account.credentialVersion++;
        account.updatedAt = now;
        credentials.save(credential); accounts.save(account);
        audit.append(null, account.id, "ACCOUNT_PASSWORD_CHANGED", "user_account", account.publicId.toString());
    }

    @Transactional
    public List<String> rotateRecoveryCodes(long userId, String currentPassword) {
        UserAccount account = accounts.lockById(userId).orElseThrow(C1AccountService::unauthenticated);
        LocalCredential credential = credentials.lockByUserId(userId).orElseThrow(C1AccountService::unauthenticated);
        if (account.status != UserAccount.Status.ACTIVE || currentPassword == null
                || !encoder.matches(currentPassword, credential.passwordHash))
            throw new C1ApiException(401, "CURRENT_PASSWORD_INVALID", "Current password is incorrect.");
        LocalDateTime now = now();
        recovery.lockUnusedByUser(userId).forEach(code -> { code.revokedAt = now; recovery.save(code); });
        List<String> codes = C1Secrets.recoveryCodes(5);
        var batch = C1Secrets.uuid();
        for (String code : codes)
            recovery.save(new AccountRecoveryCode(userId, C1Secrets.sha256(code), batch, now, now.plusDays(365)));
        audit.append(null, userId, "RECOVERY_CODES_ROTATED", "user_account", account.publicId.toString());
        return codes;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void disable(long userId, String currentPassword) {
        List<Long> orgIds = memberships.organizationIdsByUserAndStatus(userId, Membership.Status.ACTIVE)
                .stream().distinct().sorted().toList();
        for (Long orgId : orgIds) organizations.lockById(orgId).orElseThrow(C1AccountService::notFound);
        for (Long orgId : orgIds) {
            Membership member = memberships.lockByOrganizationAndUser(orgId, userId).orElse(null);
            if (member != null && member.status == Membership.Status.ACTIVE)
                memberships.lockByOrganizationAndIds(orgId, List.of(member.id));
        }
        UserAccount account = accounts.lockById(userId).orElseThrow(C1AccountService::unauthenticated);
        LocalCredential credential = credentials.lockByUserId(userId).orElseThrow(C1AccountService::unauthenticated);
        if (account.status != UserAccount.Status.ACTIVE || currentPassword == null
                || !encoder.matches(currentPassword, credential.passwordHash))
            throw new C1ApiException(401, "CURRENT_PASSWORD_INVALID", "Current password is incorrect.");
        for (Long orgId : orgIds) {
            long remaining = memberships.countEffectiveActiveOwners(orgId);
            Membership member = memberships.findByOrganizationIdAndUserId(orgId, userId).orElse(null);
            if (member != null && member.status == Membership.Status.ACTIVE
                    && member.role == Membership.Role.OWNER && remaining <= 1)
                throw new C1ApiException(409, "LAST_OWNER", "Account cannot be disabled while it is the only active owner of an organization.");
        }
        account.status = UserAccount.Status.DISABLED;
        account.credentialVersion++;
        account.updatedAt = now();
        accounts.save(account);
        audit.append(null, userId, "ACCOUNT_DISABLED", "user_account", account.publicId.toString());
    }

    @Transactional(readOnly = true)
    public UserAccount account(long userId) {
        return accounts.findById(userId).filter(a -> a.status == UserAccount.Status.ACTIVE)
                .orElseThrow(C1AccountService::unauthenticated);
    }

    public boolean passwordMatches(long userId, String password) {
        return credentials.findByUserId(userId).map(c -> encoder.matches(password, c.passwordHash)).orElse(false);
    }

    private LocalDateTime now() { return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC); }
    private static C1ApiException unauthenticated() { return new C1ApiException(401, "AUTH_REQUIRED", "Authentication is required."); }
    private static C1ApiException notFound() { return new C1ApiException(404, "NOT_FOUND", "The requested organization was not found."); }
}
