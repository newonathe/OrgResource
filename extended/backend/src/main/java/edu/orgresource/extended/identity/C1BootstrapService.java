package edu.orgresource.extended.identity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class C1BootstrapService {
    private static final String LOCK_NAME = "orgresource.extended.c1.bootstrap";
    private static final Pattern SLUG = Pattern.compile("^[a-z0-9](?:[a-z0-9-]{1,62}[a-z0-9])$");
    private static final Pattern USERNAME = Pattern.compile("^[a-z0-9][a-z0-9._-]{2,63}$");

    public record BootstrapResult(long organizationId, long accountId, List<String> recoveryCodes) { }

    private final DataSource dataSource;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final OrganizationRepository organizations;
    private final UserAccountRepository accounts;
    private final LocalCredentialRepository credentials;
    private final MembershipRepository memberships;
    private final RecoveryCodeRepository recoveryCodes;
    private final PasswordEncoder passwordEncoder;
    private final C1AuditService audit;
    private final Clock clock;

    public C1BootstrapService(DataSource dataSource, JdbcTemplate jdbc, PlatformTransactionManager txManager,
            OrganizationRepository organizations, UserAccountRepository accounts,
            LocalCredentialRepository credentials, MembershipRepository memberships,
            RecoveryCodeRepository recoveryCodes, PasswordEncoder passwordEncoder,
            C1AuditService audit, Clock clock) {
        this.dataSource = dataSource; this.jdbc = jdbc; this.transactions = new TransactionTemplate(txManager);
        this.organizations = organizations; this.accounts = accounts; this.credentials = credentials;
        this.memberships = memberships; this.recoveryCodes = recoveryCodes;
        this.passwordEncoder = passwordEncoder; this.audit = audit; this.clock = clock;
    }

    public BootstrapResult bootstrap(String slugInput, String organizationNameInput, String usernameInput,
                                     String displayNameInput, String password) {
        String slug = slugInput == null ? "" : slugInput.trim().toLowerCase(Locale.ROOT);
        String username = usernameInput == null ? "" : usernameInput.trim().toLowerCase(Locale.ROOT);
        String organizationName = cleanName(organizationNameInput, 120, "Organization name");
        String displayName = cleanName(displayNameInput, 120, "Display name");
        if (!SLUG.matcher(slug).matches()) throw new IllegalArgumentException("Slug must use 3-64 lowercase ASCII letters, digits, or hyphens.");
        if (!USERNAME.matcher(username).matches()) throw new IllegalArgumentException("Username must use 3-64 lowercase ASCII letters, digits, dot, underscore, or hyphen.");
        validatePassword(password);

        try (Connection lockConnection = dataSource.getConnection()) {
            if (!acquire(lockConnection)) throw new IllegalStateException("Another bootstrap is in progress; no changes were made.");
            try {
                return transactions.execute(status -> {
                    Integer orgCount = jdbc.queryForObject("SELECT COUNT(*) FROM organization", Integer.class);
                    Integer accountCount = jdbc.queryForObject("SELECT COUNT(*) FROM user_account", Integer.class);
                    if (orgCount == null || accountCount == null || orgCount != 0 || accountCount != 0)
                        throw new IllegalStateException("Extended C is already initialized; bootstrap refused.");
                    LocalDateTime now = now();
                    Organization org = organizations.saveAndFlush(new Organization(slug, organizationName, now));
                    UserAccount account = accounts.saveAndFlush(new UserAccount(username, displayName, now));
                    credentials.saveAndFlush(new LocalCredential(account.id, passwordEncoder.encode(password), now));
                    Membership owner = memberships.saveAndFlush(new Membership(org.id, account.id, Membership.Role.OWNER, now));
                    List<String> rawCodes = C1Secrets.recoveryCodes(5);
                    saveRecoveryBatch(account.id, rawCodes, now);
                    audit.append(org.id, account.id, "BOOTSTRAP_OWNER_CREATED", "membership", owner.publicId.toString());
                    return new BootstrapResult(org.id, account.id, rawCodes);
                });
            } finally {
                release(lockConnection);
            }
        } catch (SQLException failure) {
            throw new IllegalStateException("Database bootstrap failed; no credentials were printed.", failure);
        }
    }

    void saveRecoveryBatch(long accountId, List<String> rawCodes, LocalDateTime now) {
        var batch = C1Secrets.uuid();
        LocalDateTime expires = now.plusDays(365);
        for (String code : rawCodes)
            recoveryCodes.save(new AccountRecoveryCode(accountId, C1Secrets.sha256(code), batch, now, expires));
        recoveryCodes.flush();
    }

    public static void validatePassword(String password) {
        if (password == null || password.codePointCount(0, password.length()) < 15 || password.length() > 1024)
            throw new IllegalArgumentException("Password must contain at least 15 characters.");
    }

    static String cleanName(String input, int max, String label) {
        if (input == null) throw new IllegalArgumentException(label + " is required.");
        String value = input.strip();
        if (value.isEmpty() || value.codePointCount(0, value.length()) > max)
            throw new IllegalArgumentException(label + " must contain 1-" + max + " characters.");
        return value;
    }

    LocalDateTime now() { return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC); }

    private static boolean acquire(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT GET_LOCK(?, 10)")) {
            statement.setString(1, LOCK_NAME);
            try (ResultSet result = statement.executeQuery()) { return result.next() && result.getInt(1) == 1; }
        }
    }

    private static void release(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT RELEASE_LOCK(?)")) {
            statement.setString(1, LOCK_NAME); statement.executeQuery().close();
        } catch (SQLException ignored) { }
    }
}
