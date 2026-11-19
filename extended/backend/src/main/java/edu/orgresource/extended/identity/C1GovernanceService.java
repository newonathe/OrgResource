package edu.orgresource.extended.identity;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import edu.orgresource.extended.api.C1ApiException;

@Service
public class C1GovernanceService {
    private final OrganizationRepository organizations;
    private final UserAccountRepository accounts;
    private final LocalCredentialRepository credentials;
    private final MembershipRepository memberships;
    private final InvitationRepository invitations;
    private final OwnershipTransferRepository transfers;
    private final C1AuditService audit;
    private final PasswordEncoder passwords;
    private final Clock clock;
    private final Duration transferLifetime;

    public C1GovernanceService(OrganizationRepository organizations, UserAccountRepository accounts,
            LocalCredentialRepository credentials, MembershipRepository memberships,
            InvitationRepository invitations, OwnershipTransferRepository transfers, RecoveryCodeRepository recoveryCodes,
            C1AuditService audit, PasswordEncoder passwords, Clock clock,
            @Value("${c1.transfer.lifetime:24h}") Duration transferLifetime) {
        this.organizations = organizations; this.accounts = accounts; this.credentials = credentials;
        this.memberships = memberships; this.invitations = invitations; this.transfers = transfers;
        this.recoveryCodes = recoveryCodes;
        this.audit = audit; this.passwords = passwords; this.clock = clock; this.transferLifetime = transferLifetime;
    }

    @Transactional(readOnly = true)
    public IdentityDtos.MemberPage listMembers(long actorId, UUID orgPublicId, int limit, UUID cursor) {
        Organization org = organizations.findByPublicId(orgPublicId).orElseThrow(C1GovernanceService::notFound);
        Membership actor = memberships.findByOrganizationIdAndUserId(org.id, actorId)
                .filter(m -> m.status == Membership.Status.ACTIVE).orElseThrow(C1GovernanceService::notFound);
        requireActiveAccount(actorId);
        if (actor.role != Membership.Role.OWNER && actor.role != Membership.Role.ADMIN) throw forbidden();
        long afterId = 0;
        if (cursor != null) {
            Membership cursorMember = memberships.findByOrganizationIdAndPublicId(org.id, cursor)
                    .orElseThrow(C1GovernanceService::notFound);
            afterId = cursorMember.id;
        }
        List<Membership> rows = memberships.findByOrganizationIdAndIdGreaterThanOrderById(
                org.id, afterId, PageRequest.of(0, Math.min(100, Math.max(1, limit)) + 1));
        boolean more = rows.size() > Math.min(100, Math.max(1, limit));
        if (more) rows = new ArrayList<>(rows.subList(0, Math.min(100, Math.max(1, limit))));
        var userIds = rows.stream().map(m -> m.userId).distinct().toList();
        var userMap = accounts.findAllById(userIds).stream().collect(java.util.stream.Collectors.toMap(a -> a.id, a -> a));
        List<IdentityDtos.GovernanceMemberView> result = rows.stream().map(m -> {
            UserAccount account = userMap.get(m.userId);
            return new IdentityDtos.GovernanceMemberView(m.publicId,
                    account == null ? "Unavailable account" : account.displayName, m.role.name(), m.status.name(), m.version);
        }).toList();
        UUID next = more && !rows.isEmpty() ? rows.get(rows.size() - 1).publicId : null;
        return new IdentityDtos.MemberPage(result, next);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public IssuedInvitation invite(long actorId, UUID orgPublicId, Membership.Role requestedRole) {
        Organization org = lockOrganization(orgPublicId);
        Membership actor = lockActor(org, actorId, true);
        requireGrant(actor.role, requestedRole);
        String token = C1Secrets.bearerToken();
        LocalDateTime now = now();
        Invitation invitation = invitations.saveAndFlush(new Invitation(org.id, actor.id, requestedRole,
                C1Secrets.sha256(token), now, now.plusHours(24)));
        audit.append(org.id, actorId, "INVITATION_CREATED", "invitation", invitation.publicId.toString());
        return new IssuedInvitation(invitation, token);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Invitation revokeInvitation(long actorId, UUID orgPublicId, UUID invitePublicId, String ifMatch) {
        Organization org = lockOrganization(orgPublicId);
        Membership actor = lockActor(org, actorId, true);
        Invitation invite = invitations.lockByOrgAndPublicId(org.id, invitePublicId)
                .orElseThrow(C1GovernanceService::notFound);
        requireGrant(actor.role, invite.role);
        requireVersion(ifMatch, invite.version);
        if (invite.status != Invitation.Status.INVITED || !invite.expiresAt.isAfter(now()))
            throw conflict("INVITATION_NOT_ACTIVE", "Only an active invitation can be revoked.");
        invite.status = Invitation.Status.REVOKED;
        invite.updatedAt = now();
        invitations.saveAndFlush(invite);
        audit.append(org.id, actorId, "INVITATION_REVOKED", "invitation", invite.publicId.toString());
        return invite;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Membership changeRole(long actorId, UUID orgPublicId, UUID memberPublicId,
            Membership.Role newRole, String ifMatch) {
        Organization org = lockOrganization(orgPublicId);
        Membership actor = findActor(org, actorId);
        Membership target = memberships.findByOrganizationIdAndPublicId(org.id, memberPublicId)
                .orElseThrow(C1GovernanceService::notFound);
        List<Membership> locked = lockMemberships(org.id, actor.id, target.id);
        actor = byId(locked, actor.id); target = byId(locked, target.id);
        requireActorCurrent(org, actor, true);
        if (actor.id.equals(target.id)) throw forbidden();
        if (target.role == Membership.Role.OWNER || newRole == Membership.Role.OWNER) throw forbidden();
        if (actor.role == Membership.Role.OWNER) {
            // The owner may administer every non-owner role.
        } else if (actor.role == Membership.Role.ADMIN) {
            if (target.role == Membership.Role.ADMIN || newRole == Membership.Role.ADMIN) throw forbidden();
        } else throw forbidden();
        requireTargetAccountActive(target);
        requireVersion(ifMatch, target.version);
        target.role = newRole;
        target.updatedAt = now();
        memberships.saveAndFlush(target);
        audit.append(org.id, actorId, "MEMBERSHIP_ROLE_CHANGED", "membership", target.publicId.toString());
        return target;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Membership suspend(long actorId, UUID orgPublicId, UUID memberPublicId, String ifMatch) {
        Organization org = lockOrganization(orgPublicId);
        Membership actor = findActor(org, actorId);
        Membership target = memberships.findByOrganizationIdAndPublicId(org.id, memberPublicId)
                .orElseThrow(C1GovernanceService::notFound);
        List<Membership> locked = lockMemberships(org.id, actor.id, target.id);
        actor = byId(locked, actor.id); target = byId(locked, target.id);
        requireActorCurrent(org, actor, true);
        if (actor.id.equals(target.id) || target.role == Membership.Role.OWNER) throw forbidden();
        if (actor.role != Membership.Role.OWNER && actor.role != Membership.Role.ADMIN) throw forbidden();
        if (actor.role == Membership.Role.ADMIN && target.role == Membership.Role.ADMIN) throw forbidden();
        requireTargetAccountActive(target);
        requireVersion(ifMatch, target.version);
        if (target.status != Membership.Status.ACTIVE) throw conflict("MEMBERSHIP_NOT_ACTIVE", "Only an active membership can be suspended.");
        target.status = Membership.Status.SUSPENDED;
        target.updatedAt = now();
        memberships.saveAndFlush(target);
        audit.append(org.id, actorId, "MEMBERSHIP_SUSPENDED", "membership", target.publicId.toString());
        return target;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OwnershipTransfer startTransfer(long actorId, UUID orgPublicId,
            UUID targetPublicId, String currentPassword) {
        Organization org = lockOrganization(orgPublicId);
        Membership actor = findActor(org, actorId);
        Membership target = memberships.findByOrganizationIdAndPublicId(org.id, targetPublicId)
                .orElseThrow(C1GovernanceService::notFound);
        List<Membership> locked = lockMemberships(org.id, actor.id, target.id);
        actor = byId(locked, actor.id); target = byId(locked, target.id);
        requireActorCurrent(org, actor, true); requireTargetAccountActive(target);
        if (actor.role != Membership.Role.OWNER) throw forbidden();
        if (target.status != Membership.Status.ACTIVE || target.role != Membership.Role.ADMIN)
            throw conflict("TRANSFER_TARGET_INVALID", "The transfer recipient must be an active ADMIN in this organization.");
        LocalCredential credential = credentials.findByUserId(actorId).orElseThrow(C1GovernanceService::notFound);
        if (currentPassword == null || !passwords.matches(currentPassword, credential.passwordHash))
            throw new C1ApiException(401, "CURRENT_PASSWORD_INVALID", "Current password is incorrect.");
        LocalDateTime now = now();
        for (OwnershipTransfer old : transfers.lockPendingByOrganization(org.id)) {
            if (old.expiresAt.isAfter(now)) throw conflict("TRANSFER_ALREADY_PENDING", "An ownership transfer is already pending.");
            old.status = OwnershipTransfer.Status.EXPIRED;
            transfers.save(old);
        }
        transfers.flush();
        OwnershipTransfer transfer = transfers.saveAndFlush(new OwnershipTransfer(org.id, actor.id, target.id,
                now, now.plus(transferLifetime)));
        audit.append(org.id, actorId, "OWNERSHIP_TRANSFER_STARTED", "ownership_transfer", transfer.publicId.toString());
        return transfer;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OwnershipTransfer acceptTransfer(long actorId, UUID orgPublicId, UUID transferPublicId, String ifMatch) {
        Organization org = lockOrganization(orgPublicId);
        OwnershipTransfer observed = transfers.findByOrganizationIdAndPublicId(org.id, transferPublicId)
                .orElseThrow(C1GovernanceService::notFound);
        List<Membership> locked = lockMemberships(org.id, observed.fromMembershipId, observed.toMembershipId);
        Membership from = byId(locked, observed.fromMembershipId);
        Membership to = byId(locked, observed.toMembershipId);
        OwnershipTransfer found = transfers.lockByOrgAndPublicId(org.id, transferPublicId)
                .orElseThrow(C1GovernanceService::notFound);
        if (to.userId != actorId) throw notFound();
        requireActorCurrent(org, to, true);
        requireTargetAccountActive(from);
        if (to.role != Membership.Role.ADMIN || from.role != Membership.Role.OWNER
                || from.status != Membership.Status.ACTIVE || to.status != Membership.Status.ACTIVE)
            throw notFound();
        requireVersion(ifMatch, found.version);
        if (found.status != OwnershipTransfer.Status.PENDING || !found.expiresAt.isAfter(now()))
            throw conflict("TRANSFER_NOT_ACTIVE", "The ownership transfer is no longer active.");
        LocalDateTime now = now();
        to.role = Membership.Role.OWNER; to.updatedAt = now;
        from.role = Membership.Role.ADMIN; from.updatedAt = now;
        memberships.saveAllAndFlush(List.of(from, to));
        found.status = OwnershipTransfer.Status.ACCEPTED; found.acceptedAt = now;
        transfers.saveAndFlush(found);
        audit.append(org.id, actorId, "OWNERSHIP_TRANSFER_ACCEPTED", "ownership_transfer", found.publicId.toString());
        return found;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OwnershipTransfer revokeTransfer(long actorId, UUID orgPublicId, UUID transferPublicId, String ifMatch) {
        Organization org = lockOrganization(orgPublicId);
        Membership actor = lockActor(org, actorId, true);
        OwnershipTransfer transfer = transfers.lockByOrgAndPublicId(org.id, transferPublicId)
                .orElseThrow(C1GovernanceService::notFound);
        if (actor.role != Membership.Role.OWNER || actor.id.longValue() != transfer.fromMembershipId)
            throw forbidden();
        requireVersion(ifMatch, transfer.version);
        if (transfer.status != OwnershipTransfer.Status.PENDING || !transfer.expiresAt.isAfter(now()))
            throw conflict("TRANSFER_NOT_ACTIVE", "Only an active pending ownership transfer can be revoked.");
        transfer.status = OwnershipTransfer.Status.REVOKED;
        transfers.saveAndFlush(transfer);
        audit.append(org.id, actorId, "OWNERSHIP_TRANSFER_REVOKED", "ownership_transfer", transfer.publicId.toString());
        return transfer;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Redemption redeem(String rawToken, Long existingAccountId, String usernameInput,
            String password, String displayNameInput) {
        if (rawToken == null || rawToken.length() > 128) throw inviteInvalid();
        InvitationRepository.LockTarget observed = invitations.findLockTargetByTokenHash(C1Secrets.sha256(rawToken))
                .orElseThrow(C1GovernanceService::inviteInvalid);
        Organization org = organizations.lockById(observed.getOrganizationId()).orElseThrow(C1GovernanceService::inviteInvalid);
        Invitation invitation = invitations.lockById(observed.getId()).orElseThrow(C1GovernanceService::inviteInvalid);
        LocalDateTime now = now();
        if (org.status != Organization.Status.ACTIVE || invitation.status != Invitation.Status.INVITED
                || !invitation.expiresAt.isAfter(now)) throw inviteInvalid();

        UserAccount account;
        List<String> rawRecoveryCodes = List.of();
        if (existingAccountId != null) {
            account = accounts.findById(existingAccountId).filter(a -> a.status == UserAccount.Status.ACTIVE)
                    .orElseThrow(C1GovernanceService::inviteInvalid);
            if (usernameInput != null || password != null || displayNameInput != null)
                throw new C1ApiException(400, "VALIDATION_FAILED", "An authenticated account redeems an invitation without new-account fields.");
        } else {
            if (usernameInput == null || password == null || displayNameInput == null)
                throw new C1ApiException(400, "VALIDATION_FAILED", "Username, password and display name are required for a new account.");
            C1BootstrapService.validatePassword(password);
            String username = edu.orgresource.extended.security.C1IdentityNormalization.username(usernameInput);
            if (!username.matches("^[a-z0-9][a-z0-9._-]{2,63}$"))
                throw new C1ApiException(400, "VALIDATION_FAILED", "Username must be 3-64 lowercase ASCII letters, digits, dot, underscore, or hyphen.");
            if (accounts.findByUsernameKey(username).isPresent())
                throw conflict("USERNAME_UNAVAILABLE", "That username cannot be used.");
            String displayName = C1BootstrapService.cleanName(displayNameInput, 120, "Display name");
            account = accounts.saveAndFlush(new UserAccount(username, displayName, now));
            credentials.saveAndFlush(new LocalCredential(account.id, passwords.encode(password), now));
            rawRecoveryCodes = C1Secrets.recoveryCodes(5);
        }
        if (memberships.findByOrganizationIdAndUserId(org.id, account.id).isPresent())
            throw conflict("MEMBERSHIP_EXISTS", "This account already has a membership in the organization.");
        Membership member = memberships.saveAndFlush(new Membership(org.id, account.id, invitation.role, now));
        invitation.status = Invitation.Status.ACCEPTED; invitation.acceptedByUserId = account.id; invitation.updatedAt = now;
        invitations.saveAndFlush(invitation);
        if (!rawRecoveryCodes.isEmpty()) saveRecovery(account.id, rawRecoveryCodes, now);
        audit.append(org.id, account.id, "INVITATION_REDEEMED", "membership", member.publicId.toString());
        return new Redemption(account.id, existingAccountId == null, rawRecoveryCodes, member);
    }

    private void saveRecovery(long accountId, List<String> rawCodes, LocalDateTime now) {
        UUID batch = C1Secrets.uuid();
        for (String code : rawCodes) newRecoveryCode(accountId, code, batch, now);
    }

    private void newRecoveryCode(long accountId, String raw, UUID batch, LocalDateTime now) {
        // Repository insertion belongs to the same transaction as invitation consumption.
        recoveryCodes.save(new AccountRecoveryCode(accountId, C1Secrets.sha256(raw), batch, now, now.plusDays(365)));
    }

    private final RecoveryCodeRepository recoveryCodes;

    private Organization lockOrganization(UUID publicId) {
        Organization observed = organizations.findByPublicId(publicId).orElseThrow(C1GovernanceService::notFound);
        return organizations.lockById(observed.id).orElseThrow(C1GovernanceService::notFound);
    }

    private Membership lockActor(Organization org, long userId, boolean mutation) {
        Membership observed = findActor(org, userId);
        Membership actor = memberships.lockByOrganizationAndIds(org.id, List.of(observed.id)).stream()
                .findFirst().orElseThrow(C1GovernanceService::notFound);
        requireActorCurrent(org, actor, mutation);
        return actor;
    }

    private Membership findActor(Organization org, long userId) {
        return memberships.findByOrganizationIdAndUserId(org.id, userId)
                .filter(m -> m.status == Membership.Status.ACTIVE).orElseThrow(C1GovernanceService::notFound);
    }

    private void requireActorCurrent(Organization org, Membership actor, boolean mutation) {
        requireActiveAccount(actor.userId);
        if (actor.status != Membership.Status.ACTIVE) throw notFound();
        if (mutation && org.status != Organization.Status.ACTIVE) throw conflict("ORG_FROZEN", "A frozen organization is read-only.");
    }

    private void requireTargetAccountActive(Membership member) {
        requireActiveAccount(member.userId);
        if (member.status != Membership.Status.ACTIVE) throw notFound();
    }

    private void requireActiveAccount(long id) {
        if (accounts.findById(id).filter(a -> a.status == UserAccount.Status.ACTIVE).isEmpty()) throw notFound();
    }

    private List<Membership> lockMemberships(long orgId, Long... ids) {
        List<Long> sorted = java.util.Arrays.stream(ids).distinct().sorted().toList();
        List<Membership> rows = memberships.lockByOrganizationAndIds(orgId, sorted);
        if (rows.size() != sorted.size()) throw notFound();
        return rows;
    }

    private static Membership byId(List<Membership> rows, Long id) {
        return rows.stream().filter(m -> m.id.equals(id)).findFirst().orElseThrow(C1GovernanceService::notFound);
    }

    private static void requireGrant(Membership.Role actor, Membership.Role requested) {
        boolean allowed = requested != null && requested != Membership.Role.OWNER &&
                (actor == Membership.Role.OWNER || actor == Membership.Role.ADMIN
                        && requested != Membership.Role.ADMIN);
        if (!allowed) throw forbidden();
    }

    public static void requireVersion(String header, long version) {
        if (header == null || header.isBlank()) throw new C1ApiException(428, "PRECONDITION_REQUIRED", "A current If-Match version is required.");
        if (!header.matches("\"v[0-9]+\"")) throw new C1ApiException(400, "IF_MATCH_INVALID", "If-Match must contain one strong quoted version tag.");
        long supplied;
        try { supplied = Long.parseLong(header.substring(2, header.length() - 1)); }
        catch (NumberFormatException invalid) { throw new C1ApiException(400, "IF_MATCH_INVALID", "If-Match version is invalid."); }
        if (supplied != version) throw new C1ApiException(412, "STALE_VERSION", "The record changed. Reload it before trying again.");
    }

    @Transactional(readOnly = true)
    public UUID membershipPublicId(long id) {
        return memberships.findById(id).map(m -> m.publicId).orElseThrow(C1GovernanceService::notFound);
    }

    private LocalDateTime now() { return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC); }
    private static C1ApiException notFound() { return new C1ApiException(404, "NOT_FOUND", "The requested organization or record was not found."); }
    private static C1ApiException forbidden() { return new C1ApiException(403, "FORBIDDEN", "The current membership cannot perform this action."); }
    private static C1ApiException inviteInvalid() { return new C1ApiException(404, "INVITE_INVALID", "The invitation is unavailable or no longer active."); }
    private static C1ApiException conflict(String code, String message) { return new C1ApiException(409, code, message); }

    public record IssuedInvitation(Invitation invitation, String token) { }
    public record Redemption(long accountId, boolean newAccount, List<String> recoveryCodes, Membership membership) { }
}
