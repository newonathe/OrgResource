package edu.orgresource.extended.identity;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Bounded, restart-safe expiry of invitation and ownership-transfer state. */
@Component
public class C1GovernanceExpiryWorker {
    private final Clock clock;
    private final OrganizationRepository organizations;
    private final InvitationRepository invitations;
    private final OwnershipTransferRepository transfers;
    private final C1AuditService audit;
    private final TransactionTemplate transactions;

    public C1GovernanceExpiryWorker(Clock clock, OrganizationRepository organizations,
            InvitationRepository invitations, OwnershipTransferRepository transfers,
            C1AuditService audit, PlatformTransactionManager transactionManager) {
        this.clock = clock; this.organizations = organizations; this.invitations = invitations;
        this.transfers = transfers; this.audit = audit;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    @Scheduled(fixedDelayString = "${c1.governance-expiry-delay-ms:60000}")
    public void expireBoundedBatch() {
        LocalDateTime sampledNow = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        invitations.findTop100ByStatusAndExpiresAtLessThanEqualOrderByOrganizationIdAscIdAsc(
                Invitation.Status.INVITED, sampledNow).forEach(candidate -> transactions.execute(status -> {
                    organizations.lockById(candidate.organizationId);
                    Invitation row = invitations.lockById(candidate.id).orElse(null);
                    if (row != null && row.status == Invitation.Status.INVITED && !row.expiresAt.isAfter(sampledNow)) {
                        row.status = Invitation.Status.EXPIRED;
                        row.updatedAt = sampledNow;
                        invitations.saveAndFlush(row);
                        audit.append(row.organizationId, null, "INVITATION_EXPIRED", "invitation", row.publicId.toString());
                    }
                    return null;
                }));
        transfers.findTop100ByStatusAndExpiresAtLessThanEqualOrderByOrganizationIdAscIdAsc(
                OwnershipTransfer.Status.PENDING, sampledNow).forEach(candidate -> transactions.execute(status -> {
                    organizations.lockById(candidate.organizationId);
                    OwnershipTransfer row = transfers.lockById(candidate.id).orElse(null);
                    if (row != null && row.status == OwnershipTransfer.Status.PENDING && !row.expiresAt.isAfter(sampledNow)) {
                        row.status = OwnershipTransfer.Status.EXPIRED;
                        transfers.saveAndFlush(row);
                        audit.append(row.organizationId, null, "OWNERSHIP_TRANSFER_EXPIRED", "ownership_transfer", row.publicId.toString());
                    }
                    return null;
                }));
    }
}
