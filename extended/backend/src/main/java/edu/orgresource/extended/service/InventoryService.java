package edu.orgresource.extended.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.orgresource.extended.api.ApiDtos;
import edu.orgresource.extended.api.C1ApiException;
import edu.orgresource.extended.api.C1ValidationException;
import edu.orgresource.extended.api.InventoryDtos;
import edu.orgresource.extended.domain.Allocation;
import edu.orgresource.extended.domain.AllocationStatus;
import edu.orgresource.extended.domain.Project;
import edu.orgresource.extended.domain.Resource;
import edu.orgresource.extended.domain.ResourceRequirement;
import edu.orgresource.extended.domain.ResourceType;
import edu.orgresource.extended.identity.C1AuditService;
import edu.orgresource.extended.identity.C1HttpResponses;
import edu.orgresource.extended.identity.IdentityDtos;
import edu.orgresource.extended.identity.Membership;
import edu.orgresource.extended.identity.MembershipRepository;
import edu.orgresource.extended.identity.Organization;
import edu.orgresource.extended.identity.OrganizationRepository;
import edu.orgresource.extended.identity.UserAccount;
import edu.orgresource.extended.identity.UserAccountRepository;
import edu.orgresource.extended.repository.AllocationRepository;
import edu.orgresource.extended.repository.ProjectRepository;
import edu.orgresource.extended.repository.ResourceRepository;
import edu.orgresource.extended.repository.ResourceRequirementRepository;
import edu.orgresource.extended.security.C1OrganizationContext;

@Service
public class InventoryService {
    private static final int MAX_MOVEMENT_PAGE = 100;
    private static final String LEAD_ASSIGNMENT_ROUTE = "project-lead-assignment/";
    private static final String ALLOCATION_LINK_ROUTE = "linked-allocation-create/";
    private static final String BORROWER_CONSENT_ROUTE = "borrower-consent/";
    private static final String LEAD_CANCEL_ROUTE = "project-lead-cancel/";
    private final OrganizationRepository organizations;
    private final MembershipRepository memberships;
    private final C1OrganizationContext organizationContext;
    private final ResourceRepository resources;
    private final ProjectRepository projects;
    private final ResourceRequirementRepository requirements;
    private final AllocationRepository allocations;
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final ObjectMapper objectMapper;
    private final Validator javaxValidator;
    private final UserAccountRepository accounts;
    private final C1AuditService audit;

    public InventoryService(OrganizationRepository organizations, MembershipRepository memberships,
            C1OrganizationContext organizationContext, ResourceRepository resources,
            ProjectRepository projects, ResourceRequirementRepository requirements,
            AllocationRepository allocations, JdbcTemplate jdbc, Clock clock,
            ObjectMapper objectMapper, Validator javaxValidator, UserAccountRepository accounts, C1AuditService audit) {
        this.organizations = organizations;
        this.memberships = memberships;
        this.organizationContext = organizationContext;
        this.resources = resources;
        this.projects = projects;
        this.requirements = requirements;
        this.allocations = allocations;
        this.jdbc = jdbc;
        this.clock = clock;
        this.objectMapper = objectMapper;
        this.javaxValidator = javaxValidator;
        this.accounts = accounts;
        this.audit = audit;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiDtos.ResourceView createLegacyResource(ApiDtos.CreateResource request) {
        long selectedOrgId = organizationContext.currentOrganizationId();
        Actor actor = lockOrganization(selectedOrgId, true);
        requireStockRole(actor.membership);
        String name = normalizeName(request.name);
        Resource resource = new Resource(selectedOrgId, name, name.toLowerCase(Locale.ROOT),
                request.type, request.totalQuantity);
        validateResource(resource);
        try {
            resources.saveAndFlush(resource);
        } catch (DataIntegrityViolationException duplicate) {
            throw new C1ApiException(409, "RESOURCE_NAME_EXISTS", "A Resource with this name already exists.");
        }
        if (resource.totalQuantity > 0) {
            UUID operationId = UUID.randomUUID();
            insertMovement(resource, null, actor.membership.id, operationId,
                    fingerprint("POST:/api/resources|" + selectedOrgId + "|" + resource.id + "|" + resource.totalQuantity),
                    "INITIAL", resource.totalQuantity, resource.totalQuantity, 0, utcNow());
        }
        return new ApiDtos.ResourceView(resource, available(resource, businessNow()));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiDtos.ResourceView updateLegacyResource(long resourceId, ApiDtos.ResourceUpdate request) {
        long organizationId = organizationContext.currentOrganizationId();
        Actor actor = lockOrganization(organizationId, true);
        requireStockRole(actor.membership);
        if (resourceId <= 0) throw new C1ApiException(400, "VALIDATION_FAILED", "Resource ID must be positive.");
        Resource resource = resources.lockById(resourceId, organizationId).orElseThrow(InventoryService::notFound);
        if (request.expectedTotalQuantity == null || !request.expectedTotalQuantity.equals(resource.totalQuantity))
            throw new C1ApiException(409, "STALE_RESOURCE_QUANTITY", "Resource totalQuantity changed; reload it before updating.");
        String name = normalizeName(request.name);
        String nameKey = name.toLowerCase(Locale.ROOT);
        if (resources.existsByOrganizationIdAndNameKeyAndIdNot(organizationId, nameKey, resourceId))
            throw new C1ApiException(409, "DUPLICATE_NAME", "A Resource with this name already exists.");
        LocalDateTime sampledNow = businessNow();
        Balance before = balance(resource, sampledNow);
        long committed = resource.type == ResourceType.REUSABLE ? before.holds + before.issued : before.holds;
        if (request.totalQuantity == null || request.totalQuantity < 0 || request.totalQuantity > 1000000)
            throw new C1ApiException(400, "VALIDATION_FAILED", "totalQuantity must be from 0 to 1000000.");
        if (request.totalQuantity < committed)
            throw new C1ApiException(409, "RESOURCE_BELOW_COMMITMENTS", "totalQuantity cannot be lower than current commitments.");
        int delta = request.totalQuantity - resource.totalQuantity;
        resource.name = name;
        resource.nameKey = nameKey;
        resource.totalQuantity = request.totalQuantity;
        try {
            resources.saveAndFlush(resource);
        } catch (DataIntegrityViolationException duplicate) {
            throw new C1ApiException(409, "DUPLICATE_NAME", "A Resource with this name already exists.");
        }
        if (delta != 0) {
            UUID operationId = UUID.randomUUID();
            insertMovement(resource, null, actor.membership.id, operationId,
                    fingerprint("POST:/api/resources/" + resourceId + "/update|" + organizationId + "|" + nameKey
                            + "|" + request.expectedTotalQuantity + "|" + request.totalQuantity),
                    "ADJUST", Math.abs(delta), delta, 0, utcNow());
        }
        return new ApiDtos.ResourceView(resource, available(resource, sampledNow));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiDtos.MessageView deleteLegacyResource(long resourceId) {
        long organizationId = organizationContext.currentOrganizationId();
        Actor actor = lockOrganization(organizationId, true);
        requireStockRole(actor.membership);
        if (resourceId <= 0) throw new C1ApiException(400, "VALIDATION_FAILED", "Resource ID must be positive.");
        Resource resource = resources.lockById(resourceId, organizationId).orElseThrow(InventoryService::notFound);
        if (requirements.existsByOrganizationIdAndResourceId(organizationId, resourceId))
            throw new C1ApiException(409, "DEPENDENCY_EXISTS", "Resource still has ResourceRequirements.");
        if (resource.totalQuantity != 0)
            throw new C1ApiException(409, "RESOURCE_NOT_EMPTY", "Resource totalQuantity must be zero before deletion.");
        if (resource.getImageBytes() != null)
            throw new C1ApiException(409, "RESOURCE_IMAGE_EXISTS", "Remove the Resource image before deleting the Resource.");
        Integer history = jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Integer.class, organizationId, resourceId);
        if (history != null && history > 0)
            throw new C1ApiException(409, "HISTORY_PROTECTED", "Resource stock history is permanently protected.");
        resources.delete(resource);
        resources.flush();
        return new ApiDtos.MessageView("Record deleted.");
    }

    @Transactional(readOnly = true)
    public int availableQuantity(Resource resource, LocalDateTime sampledNow) {
        return available(resource, sampledNow);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OperationResponse receive(UUID organizationPublicId, UUID resourcePublicId,
            InventoryDtos.ReceiveRequest body, String idempotencyKey, String ifMatch, String requestId) {
        Actor actor = lockPathOrganization(organizationPublicId, true);
        requireStockRole(actor.membership);
        Resource resource = resources.lockByPublicIdAndOrganizationId(resourcePublicId, actor.organization.id)
                .orElseThrow(InventoryService::notFound);
        String route = "POST:/api/v2/orgs/resources/receive";
        String canonical = organizationPublicId + "|" + resourcePublicId + "|" + body.operationId + "|" + body.quantity;
        byte[] bodyHash = fingerprint(route + "|" + canonical);
        validateIdempotencyKey(idempotencyKey);
        OperationResponse operationReplay = priorOperation(actor.organization.id, body.operationId, route, bodyHash, requestId);
        if (operationReplay != null) return operationReplay;
        IdempotencyClaim claim = claim(actor, route, idempotencyKey, bodyHash, body.operationId);
        if (claim.replay != null) return claim.replay;

        requireVersion(ifMatch, resource.version);
        LocalDateTime sampledNow = businessNow();
        available(resource, sampledNow);
        if ((long) resource.totalQuantity + body.quantity > 1000000L)
            throw new C1ApiException(409, "RESOURCE_QUANTITY_LIMIT", "The Resource total cannot exceed 1,000,000.");
        resource.totalQuantity += body.quantity;
        resources.saveAndFlush(resource);
        insertMovement(resource, null, actor.membership.id, body.operationId, bodyHash,
                "RECEIPT", body.quantity, body.quantity, 0, utcNow());
        int available = available(resource, sampledNow);
        return complete(claim, requestId, 201,
                new InventoryDtos.OperationView(body.operationId, "RECEIPT", resource.publicId, null,
                        null, body.quantity, resource.totalQuantity, available, resource.version),
                etag(resource.version));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OperationResponse adjust(UUID organizationPublicId, UUID resourcePublicId,
            InventoryDtos.AdjustRequest body, String idempotencyKey, String ifMatch, String requestId) {
        Actor actor = lockPathOrganization(organizationPublicId, true);
        requireStockRole(actor.membership);
        Resource resource = resources.lockByPublicIdAndOrganizationId(resourcePublicId, actor.organization.id)
                .orElseThrow(InventoryService::notFound);
        String route = "POST:/api/v2/orgs/resources/adjust";
        String canonical = organizationPublicId + "|" + resourcePublicId + "|" + body.operationId + "|" + body.newTotalQuantity;
        byte[] bodyHash = fingerprint(route + "|" + canonical);
        validateIdempotencyKey(idempotencyKey);
        OperationResponse operationReplay = priorOperation(actor.organization.id, body.operationId, route, bodyHash, requestId);
        if (operationReplay != null) return operationReplay;
        IdempotencyClaim claim = claim(actor, route, idempotencyKey, bodyHash, body.operationId);
        if (claim.replay != null) return claim.replay;

        requireVersion(ifMatch, resource.version);
        LocalDateTime sampledNow = businessNow();
        Balance before = balance(resource, sampledNow);
        if (body.newTotalQuantity == resource.totalQuantity)
            throw new C1ApiException(422, "NO_CHANGE", "The requested Resource total is unchanged.");
        long committed = before.holds + before.issued;
        if (resource.type == ResourceType.CONSUMABLE) committed = before.holds;
        if (body.newTotalQuantity < committed)
            throw new C1ApiException(409, "RESOURCE_BELOW_COMMITMENTS", "The Resource total cannot be lower than current commitments.");
        int delta = body.newTotalQuantity - resource.totalQuantity;
        resource.totalQuantity = body.newTotalQuantity;
        resources.saveAndFlush(resource);
        insertMovement(resource, null, actor.membership.id, body.operationId, bodyHash,
                "ADJUST", Math.abs(delta), delta, 0, utcNow());
        int available = available(resource, sampledNow);
        return complete(claim, requestId, 200,
                new InventoryDtos.OperationView(body.operationId, "ADJUST", resource.publicId, null,
                        null, Math.abs(delta), resource.totalQuantity, available, resource.version),
                etag(resource.version));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OperationResponse transition(UUID organizationPublicId, UUID allocationPublicId,
            InventoryAction action, InventoryDtos.AllocationOperationRequest body,
            String idempotencyKey, String ifMatch, String requestId) {
        Actor actor = lockPathOrganization(organizationPublicId, true);
        boolean leadCancel = actor.membership.role == Membership.Role.LEAD && action == InventoryAction.CANCEL;
        if (!leadCancel) requireStockRole(actor.membership);
        AllocationSnapshot snapshot = allocations.findOperationSnapshotByPublicId(allocationPublicId,
                actor.organization.id).map(s -> new AllocationSnapshot(s.getAllocationId(), s.getProjectId(),
                        s.getResourceId(), s.getRequirementId(), s.getRequirementQuantity()))
                .orElseThrow(InventoryService::notFound);
        LockedAllocation locked = lockAllocation(snapshot, actor.organization.id);
        if (leadCancel) {
            requireOwnProjectAllocation(actor.membership, locked.project, locked.allocation);
            String route = LEAD_CANCEL_ROUTE + allocationPublicId;
            byte[] bodyHash = fingerprint(route + "|" + body.operationId + "|" + ifMatch);
            IdempotencyClaim claim = accessClaim(actor, route, idempotencyKey, bodyHash, body.operationId);
            if (claim.replay != null) return claim.replay;
            requireVersion(ifMatch, locked.allocation.version);
            requireState(locked.allocation, AllocationStatus.PENDING);
            applyTransition(actor, locked, action, body.operationId, bodyHash);
            Allocation cancelled = locked.allocation;
            return complete(claim, requestId, 200, new AllocationChangeView(cancelled.publicId,
                    cancelled.quantity, cancelled.status.name(), cancelled.version), etag(cancelled.version));
        }
        return transitionLocked(actor, locked, action, body.operationId, idempotencyKey,
                ifMatch, requestId, true);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiDtos.AllocationView legacyTransition(long allocationId, long expectedOrganizationId,
            InventoryAction action) {
        long selectedOrgId = organizationContext.currentOrganizationId();
        if (selectedOrgId != expectedOrganizationId) throw notFound();
        Actor actor = lockOrganization(selectedOrgId, true);
        requireStockRole(actor.membership);
        AllocationSnapshot snapshot = allocations.findOperationSnapshot(allocationId, selectedOrgId)
                .map(s -> new AllocationSnapshot(allocationId, s.getProjectId(), s.getResourceId(),
                        s.getRequirementId(), s.getRequirementQuantity()))
                .orElseThrow(InventoryService::notFound);
        LockedAllocation locked = lockAllocation(snapshot, selectedOrgId);
        applyTransition(actor, locked, action, UUID.randomUUID(), fingerprint("legacy:" + action + ":" + allocationId));
        return new ApiDtos.AllocationView(locked.allocation, locked.project.endAt, businessNow());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiDtos.MessageView deleteLegacyAllocation(long allocationId, long expectedOrganizationId) {
        long organizationId = organizationContext.currentOrganizationId();
        if (organizationId != expectedOrganizationId) throw notFound();
        Actor actor = lockOrganization(organizationId, true);
        requireStockRole(actor.membership);
        if (allocationId <= 0) throw new C1ApiException(400, "VALIDATION_FAILED", "Allocation ID must be positive.");
        AllocationSnapshot snapshot = allocations.findOperationSnapshot(allocationId, organizationId)
                .map(s -> new AllocationSnapshot(allocationId, s.getProjectId(), s.getResourceId(), s.getRequirementId(), s.getRequirementQuantity()))
                .orElseThrow(InventoryService::notFound);
        LockedAllocation locked = lockAllocation(snapshot, organizationId);
        if (locked.allocation.approvedAt != null)
            throw new C1ApiException(409, "HISTORY_PROTECTED", "Historically approved Allocations cannot be deleted.");
        if (locked.allocation.status != AllocationStatus.PENDING && locked.allocation.status != AllocationStatus.CANCELLED)
            throw invalidState("Only never-approved PENDING or CANCELLED allocations can be deleted.");
        allocations.delete(locked.allocation);
        allocations.flush();
        return new ApiDtos.MessageView("Record deleted.");
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiDtos.AllocationView revokeLegacySmsConsent(long allocationId, long expectedOrganizationId) {
        long organizationId = organizationContext.currentOrganizationId();
        if (organizationId != expectedOrganizationId) throw notFound();
        Actor actor = lockOrganization(organizationId, true);
        requireStockRole(actor.membership);
        if (allocationId <= 0) throw new C1ApiException(400, "VALIDATION_FAILED", "Allocation ID must be positive.");
        AllocationSnapshot snapshot = allocations.findOperationSnapshot(allocationId, organizationId)
                .map(s -> new AllocationSnapshot(allocationId, s.getProjectId(), s.getResourceId(), s.getRequirementId(), s.getRequirementQuantity()))
                .orElseThrow(InventoryService::notFound);
        LockedAllocation locked = lockAllocation(snapshot, organizationId);
        clearSmsConsent(locked.allocation);
        allocations.saveAndFlush(locked.allocation);
        return new ApiDtos.AllocationView(locked.allocation, locked.project.endAt, businessNow());
    }

    private static void clearSmsConsent(Allocation allocation) {
        allocation.smsConsent = false;
        allocation.phoneCiphertext = null;
        allocation.phoneNonce = null;
        allocation.phoneKeyId = null;
        if (allocation.reminderOutcome == edu.orgresource.extended.domain.ReminderOutcome.CLAIMED)
            allocation.reminderOutcome = edu.orgresource.extended.domain.ReminderOutcome.SUPPRESSED;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean expireIfEligible(long organizationId, long allocationId, LocalDateTime sampledNow) {
        AllocationSnapshot snapshot = allocations.findOperationSnapshot(allocationId, organizationId)
                .map(s -> new AllocationSnapshot(allocationId, s.getProjectId(), s.getResourceId(), s.getRequirementId(), s.getRequirementQuantity()))
                .orElse(null);
        if (snapshot == null) return false;
        LockedAllocation locked = lockAllocation(snapshot, organizationId);
        Allocation allocation = locked.allocation;
        if ((allocation.status != AllocationStatus.PENDING && allocation.status != AllocationStatus.APPROVED)
                || locked.project.endAt.isAfter(sampledNow)) return false;
        allocation.status = AllocationStatus.CANCELLED;
        allocation.cancelledAt = sampledNow;
        allocation.cancellationReason = "EXPIRED";
        allocations.saveAndFlush(allocation);
        return true;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InventoryDtos.MovementPage movements(UUID organizationPublicId, UUID resourcePublicId,
            int requestedLimit, UUID afterPublicId) {
        Actor actor = lockPathOrganization(organizationPublicId, false);
        requireStockRole(actor.membership);
        if (requestedLimit < 1 || requestedLimit > MAX_MOVEMENT_PAGE)
            throw new C1ApiException(400, "INVALID_PAGE_SIZE", "Movement page size must be between 1 and 100.");
        Resource resource = resources.findByPublicIdAndOrganizationId(resourcePublicId, actor.organization.id)
                .orElseThrow(InventoryService::notFound);
        long afterId = 0;
        if (afterPublicId != null) {
            Long cursorId = jdbc.query("SELECT id FROM stock_movement WHERE organization_id=? AND resource_id=? "
                    + "AND public_id=UUID_TO_BIN(?,0)", rs -> rs.next() ? rs.getLong(1) : null,
                    actor.organization.id, resource.id, afterPublicId.toString());
            if (cursorId == null) throw notFound();
            afterId = cursorId;
        }
        List<InventoryDtos.MovementView> rows = jdbc.query("SELECT BIN_TO_UUID(sm.public_id,0), "
                + "BIN_TO_UUID(sm.operation_public_id,0), BIN_TO_UUID(a.public_id,0), sm.kind, sm.quantity, "
                + "sm.delta_total, sm.delta_issued, sm.occurred_at, sm.id FROM stock_movement sm "
                + "LEFT JOIN allocation a ON a.id=sm.allocation_id AND a.organization_id=sm.organization_id "
                + "WHERE sm.organization_id=? AND sm.resource_id=? AND sm.id>? ORDER BY sm.id LIMIT ?",
                (rs, index) -> new InventoryDtos.MovementView(UUID.fromString(rs.getString(1)),
                        UUID.fromString(rs.getString(2)), rs.getString(3) == null ? null : UUID.fromString(rs.getString(3)),
                        rs.getString(4), rs.getInt(5), rs.getLong(6), rs.getLong(7), rs.getTimestamp(8).toLocalDateTime().toString()),
                actor.organization.id, resource.id, afterId, requestedLimit + 1);
        UUID next = null;
        if (rows.size() > requestedLimit) {
            rows.remove(rows.size() - 1);
            next = rows.get(rows.size() - 1).id();
        }
        return new InventoryDtos.MovementPage(List.copyOf(rows), next);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InventoryDtos.ResourceState resourceState(UUID organizationPublicId, UUID resourcePublicId) {
        Actor actor = lockPathOrganization(organizationPublicId, false);
        requireStockRole(actor.membership);
        Resource resource = resources.findByPublicIdAndOrganizationId(resourcePublicId, actor.organization.id)
                .orElseThrow(InventoryService::notFound);
        return new InventoryDtos.ResourceState(resource.publicId, resource.name, resource.type.name(),
                resource.totalQuantity, available(resource, businessNow()), resource.version);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InventoryDtos.ResourceState removeResourceImage(UUID organizationPublicId, UUID resourcePublicId, String ifMatch) {
        Actor actor = lockPathOrganization(organizationPublicId, true);
        requireStockRole(actor.membership);
        Resource resource = resources.lockByPublicIdAndOrganizationId(resourcePublicId, actor.organization.id)
                .orElseThrow(InventoryService::notFound);
        requireVersion(ifMatch, resource.version);
        if (resource.imageContentType == null)
            throw new C1ApiException(404, "NOT_FOUND", "Resource image was not found.");
        resource.setImageBytes(null);
        resource.imageContentType = null;
        resources.saveAndFlush(resource);
        return new InventoryDtos.ResourceState(resource.publicId, resource.name, resource.type.name(),
                resource.totalQuantity, available(resource, businessNow()), resource.version);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InventoryDtos.ResourcePage resourcePage(UUID organizationPublicId, int requestedLimit, UUID afterPublicId) {
        Actor actor = lockPathOrganization(organizationPublicId, false);
        requireStockRole(actor.membership);
        if (requestedLimit < 1 || requestedLimit > MAX_MOVEMENT_PAGE)
            throw new C1ApiException(400, "INVALID_PAGE_SIZE", "Resource page size must be between 1 and 100.");
        long afterId = 0;
        if (afterPublicId != null) {
            Long cursorId = jdbc.query("SELECT id FROM resource WHERE organization_id=? AND public_id=UUID_TO_BIN(?,0)",
                    rs -> rs.next() ? rs.getLong(1) : null, actor.organization.id, afterPublicId.toString());
            if (cursorId == null) throw notFound();
            afterId = cursorId;
        }
        LocalDateTime sampledNow = businessNow();
        List<ResourcePageRow> rows = jdbc.query("SELECT BIN_TO_UUID(r.public_id,0),r.name,r.type,r.total_quantity,r.version,"
                + "COALESCE(m.ledger_total,0),COALESCE(m.ledger_issued,0),COALESCE(h.quantity,0),COALESCE(i.quantity,0),r.id "
                + "FROM resource r LEFT JOIN (SELECT resource_id,SUM(delta_total) ledger_total,SUM(delta_issued) ledger_issued "
                + "FROM stock_movement WHERE organization_id=? GROUP BY resource_id) m ON m.resource_id=r.id "
                + "LEFT JOIN (SELECT a.resource_id,SUM(a.quantity) quantity FROM allocation a "
                + "JOIN resource_requirement q ON q.id=a.requirement_id AND q.organization_id=a.organization_id "
                + "JOIN project p ON p.id=q.project_id AND p.organization_id=q.organization_id "
                + "WHERE a.organization_id=? AND a.status='APPROVED' AND p.end_at>? GROUP BY a.resource_id) h ON h.resource_id=r.id "
                + "LEFT JOIN (SELECT resource_id,SUM(quantity) quantity FROM allocation "
                + "WHERE organization_id=? AND status='ISSUED' GROUP BY resource_id) i ON i.resource_id=r.id "
                + "WHERE r.organization_id=? AND r.id>? ORDER BY r.id LIMIT ?",
                (rs, index) -> new ResourcePageRow(UUID.fromString(rs.getString(1)), rs.getString(2), rs.getString(3),
                        rs.getInt(4), rs.getLong(5), rs.getLong(6), rs.getLong(7), rs.getLong(8), rs.getLong(9), rs.getLong(10)),
                actor.organization.id, actor.organization.id, Timestamp.valueOf(sampledNow), actor.organization.id,
                actor.organization.id, afterId, requestedLimit + 1);
        List<InventoryDtos.ResourceState> states = new ArrayList<>();
        for (ResourcePageRow row : rows) states.add(resourceState(row));
        UUID next = null;
        if (states.size() > requestedLimit) {
            states.remove(states.size() - 1);
            next = states.get(states.size() - 1).id();
        }
        return new InventoryDtos.ResourcePage(List.copyOf(states), next);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InventoryDtos.AllocationState allocationState(UUID organizationPublicId, UUID allocationPublicId) {
        Actor actor = lockPathOrganization(organizationPublicId, false);
        requireProjectReader(actor.membership);
        Allocation allocation = allocations.findByPublicIdAndOrganizationId(allocationPublicId, actor.organization.id)
                .orElseThrow(InventoryService::notFound);
        if (actor.membership.role == Membership.Role.LEAD) {
            ResourceRequirement requirement = requirements.findByIdAndOrganizationId(allocation.requirementId, actor.organization.id)
                    .orElseThrow(InventoryService::notFound);
            Project project = projects.findByIdAndOrganizationId(requirement.projectId, actor.organization.id)
                    .orElseThrow(InventoryService::notFound);
            requireProjectWriter(actor.membership, project);
        }
        Resource resource = resources.findByIdAndOrganizationId(allocation.resourceId, actor.organization.id)
                .orElseThrow(InventoryService::notFound);
        return new InventoryDtos.AllocationState(allocation.publicId, resource.publicId,
                allocation.status.name(), allocation.quantity, allocation.version);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InventoryDtos.AllocationPage allocationPage(UUID organizationPublicId, int requestedLimit, UUID afterPublicId) {
        Actor actor = lockPathOrganization(organizationPublicId, false);
        requireStockRole(actor.membership);
        if (requestedLimit < 1 || requestedLimit > MAX_MOVEMENT_PAGE)
            throw new C1ApiException(400, "INVALID_PAGE_SIZE", "Allocation page size must be between 1 and 100.");
        long afterId = 0;
        if (afterPublicId != null) {
            Long cursorId = jdbc.query("SELECT id FROM allocation WHERE organization_id=? AND public_id=UUID_TO_BIN(?,0)",
                    rs -> rs.next() ? rs.getLong(1) : null, actor.organization.id, afterPublicId.toString());
            if (cursorId == null) throw notFound();
            afterId = cursorId;
        }
        List<AllocationPageRow> rows = jdbc.query("SELECT BIN_TO_UUID(a.public_id,0),BIN_TO_UUID(r.public_id,0),"
                + "a.status,a.quantity,a.version,a.id FROM allocation a JOIN resource r "
                + "ON r.id=a.resource_id AND r.organization_id=a.organization_id "
                + "WHERE a.organization_id=? AND a.id>? ORDER BY a.id LIMIT ?",
                (rs, index) -> new AllocationPageRow(UUID.fromString(rs.getString(1)), UUID.fromString(rs.getString(2)),
                        rs.getString(3), rs.getInt(4), rs.getLong(5), rs.getLong(6)),
                actor.organization.id, afterId, requestedLimit + 1);
        List<InventoryDtos.AllocationState> states = rows.stream()
                .map(row -> new InventoryDtos.AllocationState(row.publicId, row.resourcePublicId, row.status, row.quantity, row.version))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        UUID next = null;
        if (states.size() > requestedLimit) {
            states.remove(states.size() - 1);
            next = states.get(states.size() - 1).id();
        }
        return new InventoryDtos.AllocationPage(List.copyOf(states), next);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public ProjectAccessView projectAccess(UUID organizationPublicId, UUID projectPublicId) {
        Membership member = organizationContext.requirePathOrganization(organizationPublicId, false,
                Membership.Role.OWNER, Membership.Role.ADMIN, Membership.Role.CUSTODIAN, Membership.Role.LEAD);
        Project project = projects.findByPublicIdAndOrganizationId(projectPublicId, member.organizationId)
                .orElseThrow(InventoryService::notFound);
        if (member.role == Membership.Role.LEAD && !member.id.equals(project.leadMembershipId)) throw notFound();
        return projectAccessView(project);
    }

    @Transactional(readOnly = true)
    public ApiDtos.WorkspacePage<ProjectAllocationView> projectAllocations(UUID organizationPublicId, UUID projectPublicId,
            int limit, String cursor) {
        Membership member = organizationContext.requirePathOrganization(organizationPublicId, false);
        requireProjectReader(member);
        Project project = projects.findByPublicIdAndOrganizationId(projectPublicId, member.organizationId)
                .orElseThrow(InventoryService::notFound);
        Long lead = member.role == Membership.Role.LEAD ? member.id : null;
        if (lead != null && !lead.equals(project.leadMembershipId)) throw notFound();
        var page = WorkspacePageWindow.of(limit, cursor, member.organizationId, "project-allocations", projectPublicId + "|" + lead);
        var rows = allocations.findProjectAllocations(member.organizationId, project.id, lead, page.after(), page.queryLimit());
        return page.result(rows, AllocationRepository.ProjectAllocationRow::getId, row -> {
            boolean own = lead != null && lead.equals(row.getRequester());
            boolean pending = row.getStatus() == AllocationStatus.PENDING;
            boolean neverApproved = row.getApprovedAt() == null && (pending || row.getStatus() == AllocationStatus.CANCELLED);
            boolean canCancel = own && pending;
            boolean canDelete = neverApproved && (own || member.role == Membership.Role.OWNER || member.role == Membership.Role.ADMIN);
            return new ProjectAllocationView(row.getPublicId(), row.getRequirementPublicId(), row.getResourceName(),
                    row.getBorrowerName(), row.getQuantity(), row.getStatus().name(), row.getVersion(), canCancel, canDelete);
        });
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiDtos.MessageView deleteProjectAllocation(UUID organizationPublicId, UUID allocationPublicId, String ifMatch) {
        Actor actor = lockPathOrganization(organizationPublicId, true);
        requireProjectWriterRole(actor.membership);
        AllocationSnapshot snapshot = allocations.findOperationSnapshotByPublicId(allocationPublicId, actor.organization.id)
                .map(row -> new AllocationSnapshot(row.getAllocationId(), row.getProjectId(), row.getResourceId(),
                        row.getRequirementId(), row.getRequirementQuantity())).orElseThrow(InventoryService::notFound);
        LockedAllocation locked = lockAllocation(snapshot, actor.organization.id);
        if (actor.membership.role == Membership.Role.LEAD)
            requireOwnProjectAllocation(actor.membership, locked.project, locked.allocation);
        requireVersion(ifMatch, locked.allocation.version);
        if (locked.allocation.approvedAt != null)
            throw new C1ApiException(409, "HISTORY_PROTECTED", "Historically approved Allocations cannot be deleted.");
        if (locked.allocation.status != AllocationStatus.PENDING && locked.allocation.status != AllocationStatus.CANCELLED)
            throw invalidState("Only never-approved PENDING or CANCELLED allocations can be deleted.");
        allocations.delete(locked.allocation);
        allocations.flush();
        return new ApiDtos.MessageView("Record deleted.");
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public ApiDtos.WorkspacePage<BorrowerOption> borrowerOptions(UUID organizationPublicId, UUID projectPublicId,
            int limit, String cursor, String search) {
        Membership member = organizationContext.requirePathOrganization(organizationPublicId, false,
                Membership.Role.OWNER, Membership.Role.ADMIN, Membership.Role.CUSTODIAN, Membership.Role.LEAD);
        if (projectPublicId == null) throw new C1ApiException(400, "VALIDATION_FAILED", "projectId is required.");
        Project project = projects.findByPublicIdAndOrganizationId(projectPublicId, member.organizationId)
                .orElseThrow(InventoryService::notFound);
        if (member.role == Membership.Role.LEAD && !member.id.equals(project.leadMembershipId)) throw notFound();
        return directoryOptions(member.organizationId, projectPublicId, limit, cursor, search, null);
    }

    @Transactional(readOnly = true)
    public ApiDtos.WorkspacePage<BorrowerOption> leadOptions(UUID organizationPublicId, UUID projectPublicId,
            int limit, String cursor, String search) {
        Membership actor = organizationContext.requirePathOrganization(organizationPublicId, false,
                Membership.Role.OWNER, Membership.Role.ADMIN);
        if (projects.findByPublicIdAndOrganizationId(projectPublicId, actor.organizationId).isEmpty()) throw notFound();
        return directoryOptions(actor.organizationId, projectPublicId, limit, cursor, search, Membership.Role.LEAD);
    }

    private ApiDtos.WorkspacePage<BorrowerOption> directoryOptions(long organizationId, UUID projectPublicId,
            int limit, String cursor, String search, Membership.Role role) {
        if (search != null && search.length() > 100)
            throw new C1ApiException(400, "VALIDATION_FAILED", "Search must be at most 100 characters.");
        String term = search == null || search.isBlank() ? null : search.strip().toLowerCase(Locale.ROOT);
        var page = WorkspacePageWindow.of(limit, cursor, organizationId, role == null ? "borrowers" : "lead-options",
                projectPublicId + "|" + (term == null ? "0" : "1" + term));
        var rows = memberships.findBorrowerOptions(organizationId, page.after(), term,
                Membership.Status.ACTIVE, UserAccount.Status.ACTIVE, role, page.queryLimit());
        return page.result(rows, MembershipRepository.BorrowerRow::getId,
                row -> new BorrowerOption(row.getMembershipId(), row.getDisplayName()));
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public ApiDtos.WorkspacePage<BorrowedView> borrowedPage(UUID organizationPublicId, int limit, String cursor) {
        Membership member = organizationContext.requirePathOrganization(organizationPublicId, false);
        var page = WorkspacePageWindow.of(limit, cursor, member.organizationId, "borrowed", member.publicId.toString());
        return page.result(allocations.findBorrowedPage(member.organizationId, member.id, page.after(), page.queryLimit()),
                AllocationRepository.BorrowedRow::getId, InventoryService::borrowedView);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public BorrowedView borrowed(UUID organizationPublicId, UUID allocationPublicId) {
        Membership member = organizationContext.requirePathOrganization(organizationPublicId, false);
        return borrowedFor(member, allocationPublicId);
    }

    private BorrowedView borrowedFor(Membership member, UUID allocationPublicId) {
        return allocations.findBorrowed(member.organizationId, member.id, allocationPublicId)
                .map(InventoryService::borrowedView).orElseThrow(InventoryService::notFound);
    }

    private static BorrowedView borrowedView(AllocationRepository.BorrowedRow row) {
        return new BorrowedView(row.getPublicId(), row.getProjectName(), row.getResourceName(), row.getQuantity(),
                row.getStatus().name(), row.getCreatedAt(), row.getApprovedAt(), row.getIssuedAt(),
                row.getReturnedAt(), row.getConsumedAt(), row.getCancelledAt(), row.getVersion());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OperationResponse createLinkedAllocation(UUID organizationPublicId, UUID requirementPublicId, UUID borrowerPublicId,
            int quantity, UUID operationId, String idempotencyKey, String requestId) {
        Actor actor = lockPathOrganization(organizationPublicId, true);
        requireProjectWriterRole(actor.membership);
        long org = actor.organization.id;
        ResourceRequirement observed = requirements.findByPublicIdAndOrganizationId(requirementPublicId, org)
                .orElseThrow(InventoryService::notFound);
        Project project = projects.lockById(observed.projectId, org).orElseThrow(InventoryService::notFound);
        requireProjectWriter(actor.membership, project);
        String route = ALLOCATION_LINK_ROUTE + project.publicId;
        byte[] bodyHash = fingerprint(route + "|" + requirementPublicId + "|" + borrowerPublicId + "|" + quantity + "|" + operationId);
        IdempotencyClaim claim = accessClaim(actor, route, idempotencyKey, bodyHash, operationId);
        if (claim.replay != null) return claim.replay;
        Resource resource = resources.lockById(observed.resourceId, org).orElseThrow(InventoryService::notFound);
        ResourceRequirement requirement = requirements.lockById(observed.id, org).orElseThrow(InventoryService::notFound);
        LocalDateTime now = businessNow();
        if (!project.endAt.isAfter(now))
            throw new C1ApiException(409, "PROJECT_ENDED", "An ended Project cannot receive an Allocation.");
        if (quantity < 1 || quantity > requirement.quantity)
            throw new C1ApiException(400, "VALIDATION_FAILED", "quantity must be positive and cannot exceed the Requirement quantity.");
        if (borrowerPublicId == null && resource.type == ResourceType.REUSABLE)
            throw new C1ApiException(400, "VALIDATION_FAILED", "A reusable Allocation requires a borrower membership.");
        Membership borrower = borrowerPublicId == null ? null : memberships.findByOrganizationIdAndPublicId(org, borrowerPublicId)
                .orElseThrow(InventoryService::notFound);
        Membership displayIdentity = borrower == null ? actor.membership : borrower;
        UserAccount account = accounts.findById(displayIdentity.userId)
                .filter(user -> user.status == UserAccount.Status.ACTIVE).orElse(null);
        if (account == null || displayIdentity.status != Membership.Status.ACTIVE)
            throw new C1ApiException(409, "BORROWER_NOT_ELIGIBLE", "Choose an active borrower in this organization.");
        Allocation allocation = new Allocation(org, requirement, quantity, account.displayName, false, now);
        allocation.borrowerMembershipId = borrower == null ? null : borrower.id;
        allocation.requestedByMembershipId = actor.membership.id;
        allocations.saveAndFlush(allocation);
        audit.append(org, actor.userId, "LINKED_ALLOCATION_CREATED", "allocation", allocation.publicId.toString());
        return complete(claim, requestId, 201, new LinkedAllocationView(allocation.publicId, requirement.publicId,
                borrowerPublicId, allocation.borrowerName, allocation.quantity, allocation.status.name(), allocation.version),
                etag(allocation.version));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OperationResponse revokeBorrowerConsent(UUID organizationPublicId, UUID allocationPublicId, UUID operationId,
            String idempotencyKey, String ifMatch, String requestId) {
        Actor actor = lockPathOrganization(organizationPublicId, true);
        borrowedFor(actor.membership, allocationPublicId);
        AllocationSnapshot snapshot = allocations.findOperationSnapshotByPublicId(allocationPublicId, actor.organization.id)
                .map(row -> new AllocationSnapshot(row.getAllocationId(), row.getProjectId(), row.getResourceId(),
                        row.getRequirementId(), row.getRequirementQuantity())).orElseThrow(InventoryService::notFound);
        LockedAllocation locked = lockAllocation(snapshot, actor.organization.id);
        if (!actor.membership.id.equals(locked.allocation.borrowerMembershipId)) throw notFound();
        String route = BORROWER_CONSENT_ROUTE + allocationPublicId;
        IdempotencyClaim claim = accessClaim(actor, route, idempotencyKey, fingerprint(route + "|" + operationId + "|" + ifMatch), operationId);
        if (claim.replay != null) return claim.replay;
        requireVersion(ifMatch, locked.allocation.version);
        clearSmsConsent(locked.allocation);
        allocations.saveAndFlush(locked.allocation);
        return complete(claim, requestId, 200, borrowedFor(actor.membership, allocationPublicId), etag(locked.allocation.version));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OperationResponse assignProjectLead(UUID organizationPublicId, UUID projectPublicId, UUID leadPublicId,
            UUID operationId, String idempotencyKey, String ifMatch, String requestId) {
        Actor actor = lockPathOrganization(organizationPublicId, true);
        requireProjectManager(actor.membership);
        Project project = projects.lockByPublicId(projectPublicId, actor.organization.id).orElseThrow(InventoryService::notFound);
        String route = LEAD_ASSIGNMENT_ROUTE + projectPublicId;
        byte[] bodyHash = fingerprint(route + "|" + operationId + "|" + leadPublicId + "|" + ifMatch);
        IdempotencyClaim claim = accessClaim(actor, route, idempotencyKey, bodyHash, operationId);
        if (claim.replay != null) return claim.replay;
        requireVersion(ifMatch, project.version);
        Membership target = leadPublicId == null ? null : memberships
                .findByOrganizationIdAndPublicId(actor.organization.id, leadPublicId)
                .orElseThrow(InventoryService::notFound);
        if (target != null && (target.role != Membership.Role.LEAD || !isEffectiveMember(target)))
            throw new C1ApiException(409, "LEAD_NOT_ELIGIBLE", "Choose an active LEAD in this organization.");
        Long targetId = target == null ? null : target.id;
        if (!java.util.Objects.equals(project.leadMembershipId, targetId)) {
            project.leadMembershipId = targetId;
            projects.saveAndFlush(project);
            audit.append(actor.organization.id, actor.userId, "PROJECT_LEAD_CHANGED", "project", project.publicId.toString());
        }
        return complete(claim, requestId, 200, projectAccessView(project), etag(project.version));
    }

    ProjectAccessView projectAccessView(Project project) {
        UUID lead = project.leadMembershipId == null ? null : memberships.findById(project.leadMembershipId)
                .filter(row -> row.organizationId.equals(project.organizationId) && row.role == Membership.Role.LEAD
                        && isEffectiveMember(row)).map(row -> row.publicId).orElse(null);
        return new ProjectAccessView(project.publicId, project.name, project.startAt.toString(),
                project.endAt.toString(), lead, project.version);
    }

    private boolean isEffectiveMember(Membership member) {
        return member.status == Membership.Status.ACTIVE && accounts.findById(member.userId)
                .filter(account -> account.status == UserAccount.Status.ACTIVE).isPresent();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String operationReceipt(UUID organizationPublicId, UUID operationId, String requestId) {
        Actor actor = lockPathOrganization(organizationPublicId, false);
        if (isStockRole(actor.membership.role)) {
            Optional<MovementFact> movement = findMovementByOperation(actor.organization.id, operationId);
            if (movement.isPresent()) return durableMovementResponse(movement.get(), requestId).json;
        }
        Optional<Receipt> existing = findReceiptByOperation(actor.organization.id, operationId);
        boolean leadAssignment = existing.isPresent() && existing.get().route.startsWith(LEAD_ASSIGNMENT_ROUTE);
        if (leadAssignment) {
            requireProjectManager(actor.membership);
            Receipt assignment = existing.get();
            UUID projectPublicId = UUID.fromString(assignment.route.substring(LEAD_ASSIGNMENT_ROUTE.length()));
            if (assignment.actorUserId != actor.userId
                    || projects.findByPublicIdAndOrganizationId(projectPublicId, actor.organization.id).isEmpty()) throw notFound();
        } else if (existing.isPresent() && existing.get().actorUserId == actor.userId
                && existing.get().route.startsWith(ALLOCATION_LINK_ROUTE)) {
            requireProjectWriterRole(actor.membership);
            UUID projectPublicId = UUID.fromString(existing.get().route.substring(ALLOCATION_LINK_ROUTE.length()));
            Project project = projects.findByPublicIdAndOrganizationId(projectPublicId, actor.organization.id)
                    .orElseThrow(InventoryService::notFound);
            requireProjectWriter(actor.membership, project);
        } else if (existing.isPresent() && existing.get().actorUserId == actor.userId
                && existing.get().route.startsWith(BORROWER_CONSENT_ROUTE)) {
            UUID allocationPublicId = UUID.fromString(existing.get().route.substring(BORROWER_CONSENT_ROUTE.length()));
            borrowedFor(actor.membership, allocationPublicId);
        } else if (existing.isPresent() && existing.get().actorUserId == actor.userId
                && existing.get().route.startsWith(LEAD_CANCEL_ROUTE)) {
            UUID id = UUID.fromString(existing.get().route.substring(LEAD_CANCEL_ROUTE.length()));
            Allocation allocation = allocations.findByPublicIdAndOrganizationId(id, actor.organization.id)
                    .orElseThrow(InventoryService::notFound);
            ResourceRequirement requirement = requirements.findByIdAndOrganizationId(allocation.requirementId, actor.organization.id)
                    .orElseThrow(InventoryService::notFound);
            Project project = projects.findByIdAndOrganizationId(requirement.projectId, actor.organization.id)
                    .orElseThrow(InventoryService::notFound);
            requireOwnProjectAllocation(actor.membership, project, allocation);
        } else if (existing.isPresent() && (existing.get().route.startsWith(ALLOCATION_LINK_ROUTE)
                || existing.get().route.startsWith(BORROWER_CONSENT_ROUTE)
                || existing.get().route.startsWith(LEAD_CANCEL_ROUTE))) {
            requireStockRole(actor.membership);
            throw notFound();
        } else requireStockRole(actor.membership);
        Receipt receipt = existing.orElseThrow(InventoryService::notFound);
        if (receipt.expiresAt.isBefore(utcNow()))
            throw new C1ApiException(409, "IDEMPOTENCY_KEY_EXPIRED", "The operation receipt has expired.");
        try {
            JsonNode prior = objectMapper.readTree(receipt.responseJson);
            JsonNode data = prior.get("data");
            return objectMapper.writeValueAsString(new IdentityDtos.Envelope<>(data, requestId));
        } catch (JsonProcessingException malformed) {
            throw integrityFailure();
        }
    }

    private OperationResponse transitionLocked(Actor actor, LockedAllocation locked, InventoryAction action,
            UUID operationId, String idempotencyKey, String ifMatch, String requestId, boolean modern) {
        String route = "POST:/api/v2/orgs/allocations/" + action.name().toLowerCase(Locale.ROOT);
        String canonical = actor.organization.publicId + "|" + locked.allocation.publicId + "|" + operationId + "|" + action;
        byte[] bodyHash = fingerprint(route + "|" + canonical);
        if (modern) validateIdempotencyKey(idempotencyKey);
        OperationResponse operationReplay = priorOperation(actor.organization.id, operationId, route, bodyHash, requestId);
        if (operationReplay != null) return operationReplay;
        IdempotencyClaim claim = modern ? claim(actor, route, idempotencyKey, bodyHash, operationId)
                : IdempotencyClaim.none();
        if (claim.replay != null) return claim.replay;
        if (modern) requireVersion(ifMatch, locked.allocation.version);
        applyTransition(actor, locked, action, operationId, bodyHash);
        LocalDateTime sampledNow = businessNow();
        int available = available(locked.resource, sampledNow);
        String etag = etag(locked.allocation.version);
        InventoryDtos.OperationView view = new InventoryDtos.OperationView(operationId, action.name(),
                locked.resource.publicId, locked.allocation.publicId, locked.allocation.status.name(),
                locked.allocation.quantity, locked.resource.totalQuantity, available, locked.allocation.version);
        if (!modern) return new OperationResponse(200, toJson(new IdentityDtos.Envelope<>(
                new ApiDtos.AllocationView(locked.allocation, locked.project.endAt, sampledNow), requestId)), null);
        return complete(claim, requestId, 200, view, etag);
    }

    private void applyTransition(Actor actor, LockedAllocation locked, InventoryAction action,
            UUID operationId, byte[] bodyHash) {
        Allocation allocation = locked.allocation;
        Resource resource = locked.resource;
        LocalDateTime now = businessNow();
        Balance before = balance(resource, now);
        switch (action) {
            case APPROVE -> {
                requireState(allocation, AllocationStatus.PENDING);
                if (!locked.project.endAt.isAfter(now))
                    throw new C1ApiException(409, "PROJECT_ENDED", "The Project has ended.");
                long covered = requirements.sumCoveredQuantity(allocation.requirementId, actor.organization.id, now);
                if (covered + allocation.quantity > locked.requirement.quantity)
                    throw new C1ApiException(409, "REQUIREMENT_COVERED", "The Allocation exceeds remaining requirement.");
                long effectiveHolds = before.holds;
                long committed = resource.type == ResourceType.REUSABLE ? effectiveHolds + before.issued : effectiveHolds;
                if (resource.totalQuantity - committed < allocation.quantity)
                    throw new C1ApiException(409, "INSUFFICIENT_STOCK", "The Resource has insufficient available stock.");
                allocation.status = AllocationStatus.APPROVED;
                allocation.approvedAt = now;
                allocations.saveAndFlush(allocation);
            }
            case ISSUE -> {
                requireState(allocation, AllocationStatus.APPROVED);
                requireType(resource, ResourceType.REUSABLE);
                requireProjectActive(locked.project, now);
                allocation.status = AllocationStatus.ISSUED;
                allocation.issuedAt = now;
                allocations.saveAndFlush(allocation);
                insertMovement(resource, allocation, actor.membership.id, operationId, bodyHash,
                        "ISSUE", allocation.quantity, 0, allocation.quantity, utcNow());
            }
            case RETURN -> {
                requireState(allocation, AllocationStatus.ISSUED);
                requireType(resource, ResourceType.REUSABLE);
                allocation.status = AllocationStatus.RETURNED;
                allocation.returnedAt = now;
                allocations.saveAndFlush(allocation);
                insertMovement(resource, allocation, actor.membership.id, operationId, bodyHash,
                        "RETURN", allocation.quantity, 0, -allocation.quantity, utcNow());
            }
            case CONSUME -> {
                requireState(allocation, AllocationStatus.APPROVED);
                requireType(resource, ResourceType.CONSUMABLE);
                requireProjectActive(locked.project, now);
                if (resource.totalQuantity < allocation.quantity)
                    throw new C1ApiException(409, "INSUFFICIENT_PHYSICAL_STOCK", "The Resource physical quantity is insufficient.");
                resource.totalQuantity -= allocation.quantity;
                allocation.status = AllocationStatus.CONSUMED;
                allocation.consumedAt = now;
                resources.saveAndFlush(resource);
                allocations.saveAndFlush(allocation);
                insertMovement(resource, allocation, actor.membership.id, operationId, bodyHash,
                        "CONSUME", allocation.quantity, -allocation.quantity, 0, utcNow());
            }
            case CANCEL -> {
                if (allocation.status != AllocationStatus.PENDING && allocation.status != AllocationStatus.APPROVED)
                    throw invalidState("Only PENDING or APPROVED allocations can be cancelled.");
                allocation.status = AllocationStatus.CANCELLED;
                allocation.cancelledAt = now;
                allocation.cancellationReason = locked.project.endAt.isAfter(now) ? "USER_REQUEST" : "EXPIRED";
                allocations.saveAndFlush(allocation);
            }
        }
        if (action == InventoryAction.ISSUE || action == InventoryAction.RETURN || action == InventoryAction.CONSUME)
            available(resource, now);
    }

    private LockedAllocation lockAllocation(AllocationSnapshot snapshot, long organizationId) {
        Project project = projects.lockById(snapshot.projectId, organizationId).orElseThrow(InventoryService::notFound);
        Resource resource = resources.lockById(snapshot.resourceId, organizationId).orElseThrow(InventoryService::notFound);
        ResourceRequirement requirement = requirements.lockById(snapshot.requirementId, organizationId)
                .orElseThrow(InventoryService::notFound);
        Allocation allocation = allocations.lockById(snapshot.allocationId, organizationId)
                .orElseThrow(InventoryService::notFound);
        return new LockedAllocation(project, resource, requirement, allocation);
    }

    Actor lockPathOrganization(UUID organizationPublicId, boolean mutation) {
        Organization organization = organizations.findByPublicId(organizationPublicId)
                .orElseThrow(InventoryService::notFound);
        return lockOrganization(organization.id, mutation);
    }

    private Actor lockOrganization(long organizationId, boolean mutation) {
        Organization organization = organizations.lockById(organizationId).orElseThrow(InventoryService::notFound);
        long userId = organizationContext.currentUserId();
        Membership locked = memberships.lockByOrganizationAndUser(organizationId, userId)
                .orElseThrow(InventoryService::notFound);
        organizationContext.requireCurrentMembership(organizationId);
        if (locked.status != Membership.Status.ACTIVE)
            throw notFound();
        if (mutation && organization.status != Organization.Status.ACTIVE)
            throw new C1ApiException(409, "ORG_FROZEN", "A frozen organization is read-only.");
        return new Actor(organization, locked, userId);
    }

    private IdempotencyClaim claim(Actor actor, String route, String suppliedKey, byte[] bodyHash, UUID operationId) {
        validateIdempotencyKey(suppliedKey);
        byte[] keyHash = fingerprint(suppliedKey);
        LocalDateTime created = utcNow();
        int inserted = jdbc.update("INSERT IGNORE INTO idempotency_key(organization_id,actor_user_id,route_fingerprint,key_hash,body_hash,"
                + "state,operation_public_id,http_status,response_json,created_at,expires_at) "
                + "VALUES(?,?,?,?,?,'PROCESSING',UUID_TO_BIN(?,0),200,JSON_OBJECT(),?,DATE_ADD(?,INTERVAL 24 HOUR))",
                actor.organization.id, actor.userId, route, keyHash, bodyHash, operationId.toString(),
                Timestamp.valueOf(created), Timestamp.valueOf(created));
        Receipt receipt = jdbc.query("SELECT id,body_hash,state,BIN_TO_UUID(operation_public_id,0),http_status,"
                + "CAST(response_json AS CHAR),response_etag,expires_at,actor_user_id,route_fingerprint "
                + "FROM idempotency_key WHERE organization_id=? AND actor_user_id=? AND route_fingerprint=? AND key_hash=? FOR UPDATE",
                rs -> rs.next() ? new Receipt(rs.getLong(1), rs.getBytes(2), rs.getString(3), UUID.fromString(rs.getString(4)),
                        rs.getInt(5), rs.getString(6), rs.getString(7), rs.getTimestamp(8).toLocalDateTime(),
                        rs.getLong(9), rs.getString(10)) : null,
                actor.organization.id, actor.userId, route, keyHash);
        if (receipt == null) throw integrityFailure();
        if (!Arrays.equals(receipt.bodyHash, bodyHash) || !receipt.operationId.equals(operationId))
            throw new C1ApiException(409, "IDEMPOTENCY_KEY_REUSED", "This Idempotency-Key is already bound to a different request.");
        if (receipt.state.equals("PROCESSING")) {
            if (inserted != 1) throw integrityFailure();
            return new IdempotencyClaim(receipt.id, null);
        }
        if (receipt.expiresAt.isBefore(created))
            throw new C1ApiException(409, "IDEMPOTENCY_KEY_EXPIRED", "This Idempotency-Key receipt has expired; use a new key.");
        return new IdempotencyClaim(null, receipt.response());
    }

    private IdempotencyClaim accessClaim(Actor actor, String route, String key, byte[] bodyHash, UUID operationId) {
        validateIdempotencyKey(key);
        Optional<Receipt> earlier = findReceiptByOperation(actor.organization.id, operationId);
        if (earlier.isPresent()) {
            Receipt receipt = earlier.get();
            if (receipt.actorUserId != actor.userId || !receipt.route.equals(route)
                    || !Arrays.equals(receipt.bodyHash, bodyHash))
                throw new C1ApiException(409, "OPERATION_ID_REUSED", "This operationId is already bound to a different request.");
            if (receipt.expiresAt.isBefore(utcNow()))
                throw new C1ApiException(409, "IDEMPOTENCY_KEY_EXPIRED", "The operation receipt has expired.");
            return new IdempotencyClaim(null, receipt.response());
        }
        if (findMovementByOperation(actor.organization.id, operationId).isPresent())
            throw new C1ApiException(409, "OPERATION_ID_REUSED", "This operationId is already bound to a stock movement.");
        return claim(actor, route, key, bodyHash, operationId);
    }

    private OperationResponse priorOperation(long organizationId, UUID operationId, String route, byte[] bodyHash, String requestId) {
        Optional<Receipt> receipt = findReceiptByOperation(organizationId, operationId);
        if (receipt.isPresent()) {
            Receipt existing = receipt.get();
            if (!existing.route.equals(route) || !Arrays.equals(existing.bodyHash, bodyHash))
                throw new C1ApiException(409, "OPERATION_ID_REUSED", "This operationId is already bound to a different operation.");
            if (!existing.expiresAt.isBefore(utcNow())) return existing.response();
        }
        Optional<MovementFact> movement = findMovementByOperation(organizationId, operationId);
        if (movement.isPresent()) {
            MovementFact fact = movement.get();
            if (!Arrays.equals(fact.requestFingerprint, bodyHash))
                throw new C1ApiException(409, "OPERATION_ID_REUSED", "This operationId is already bound to a different operation.");
            return durableMovementResponse(fact, requestId);
        }
        if (receipt.isPresent())
            throw new C1ApiException(409, "IDEMPOTENCY_KEY_EXPIRED", "The operation receipt has expired.");
        return null;
    }

    private Optional<MovementFact> findMovementByOperation(long organizationId, UUID operationId) {
        return jdbc.query("SELECT BIN_TO_UUID(m.operation_public_id,0),BIN_TO_UUID(r.public_id,0),"
                + "CASE WHEN a.public_id IS NULL THEN NULL ELSE BIN_TO_UUID(a.public_id,0) END,"
                + "m.kind,m.quantity,m.delta_total,m.delta_issued,m.occurred_at,m.request_fingerprint,actor.user_id "
                + "FROM stock_movement m JOIN resource r ON r.id=m.resource_id AND r.organization_id=m.organization_id "
                + "LEFT JOIN allocation a ON a.id=m.allocation_id AND a.organization_id=m.organization_id "
                + "JOIN membership actor ON actor.id=m.actor_membership_id AND actor.org_id=m.organization_id "
                + "WHERE m.organization_id=? AND m.operation_public_id=UUID_TO_BIN(?,0)",
                rs -> rs.next() ? Optional.of(new MovementFact(UUID.fromString(rs.getString(1)),
                        UUID.fromString(rs.getString(2)), rs.getString(3) == null ? null : UUID.fromString(rs.getString(3)),
                        rs.getString(4), rs.getInt(5), rs.getLong(6), rs.getLong(7),
                        rs.getTimestamp(8).toLocalDateTime(), rs.getBytes(9), rs.getLong(10))) : Optional.empty(),
                organizationId, operationId.toString());
    }

    private OperationResponse durableMovementResponse(MovementFact fact, String requestId) {
        InventoryDtos.MovementReceipt view = new InventoryDtos.MovementReceipt(fact.operationId,
                fact.resourceId, fact.allocationId, fact.kind, fact.quantity, fact.deltaTotal,
                fact.deltaIssued, fact.occurredAt.toString());
        return new OperationResponse(200, toJson(new IdentityDtos.Envelope<>(view, requestId)), null);
    }

    private Optional<Receipt> findReceiptByOperation(long organizationId, UUID operationId) {
        return jdbc.query("SELECT id,body_hash,state,BIN_TO_UUID(operation_public_id,0),http_status,"
                + "CAST(response_json AS CHAR),response_etag,expires_at,actor_user_id,route_fingerprint "
                + "FROM idempotency_key WHERE organization_id=? AND operation_public_id=UUID_TO_BIN(?,0)",
                rs -> rs.next() ? Optional.of(new Receipt(rs.getLong(1), rs.getBytes(2), rs.getString(3),
                        UUID.fromString(rs.getString(4)), rs.getInt(5), rs.getString(6), rs.getString(7),
                        rs.getTimestamp(8).toLocalDateTime(), rs.getLong(9), rs.getString(10))) : Optional.empty(),
                organizationId, operationId.toString());
    }

    private OperationResponse complete(IdempotencyClaim claim, String requestId, int status, Object data, String etag) {
        String responseJson = toJson(new IdentityDtos.Envelope<>(data, requestId));
        int updated = jdbc.update("UPDATE idempotency_key SET state='COMPLETED',http_status=?,response_json=?,response_etag=? "
                + "WHERE id=? AND state='PROCESSING'", status, responseJson, etag, claim.id);
        if (updated != 1) throw integrityFailure();
        return new OperationResponse(status, responseJson, etag);
    }

    private Balance balance(Resource resource, LocalDateTime sampledNow) {
        Balance balance = jdbc.queryForObject("SELECT "
                + "COALESCE((SELECT SUM(delta_total) FROM stock_movement WHERE organization_id=? AND resource_id=?),0), "
                + "COALESCE((SELECT SUM(delta_issued) FROM stock_movement WHERE organization_id=? AND resource_id=?),0), "
                + "COALESCE((SELECT SUM(a.quantity) FROM allocation a JOIN resource_requirement q "
                + "ON q.id=a.requirement_id AND q.organization_id=a.organization_id JOIN project p "
                + "ON p.id=q.project_id AND p.organization_id=q.organization_id WHERE a.organization_id=? AND a.resource_id=? "
                + "AND a.status='APPROVED' AND p.end_at>? ),0), "
                + "COALESCE((SELECT SUM(a.quantity) FROM allocation a WHERE a.organization_id=? AND a.resource_id=? "
                + "AND a.status='ISSUED'),0)",
                (rs, row) -> new Balance(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getLong(4)),
                resource.organizationId, resource.id, resource.organizationId, resource.id,
                resource.organizationId, resource.id, Timestamp.valueOf(sampledNow),
                resource.organizationId, resource.id);
        if (resource.totalQuantity == null || resource.totalQuantity < 0 || resource.totalQuantity > 1000000
                || balance.ledgerTotal != resource.totalQuantity)
            throw integrityFailure();
        if (resource.type == ResourceType.REUSABLE) {
            if (balance.ledgerIssued != balance.issued || resource.totalQuantity < balance.holds + balance.issued)
                throw integrityFailure();
        } else if (balance.ledgerIssued != 0 || balance.issued != 0 || resource.totalQuantity < balance.holds) {
            throw integrityFailure();
        }
        return balance;
    }

    private int available(Resource resource, LocalDateTime sampledNow) {
        Balance balance = balance(resource, sampledNow);
        long available = resource.totalQuantity - balance.holds
                - (resource.type == ResourceType.REUSABLE ? balance.issued : 0);
        if (available < 0 || available > 1000000) throw integrityFailure();
        return Math.toIntExact(available);
    }

    private static InventoryDtos.ResourceState resourceState(ResourcePageRow row) {
        boolean reusable = "REUSABLE".equals(row.type);
        if (row.totalQuantity < 0 || row.totalQuantity > 1000000 || row.ledgerTotal != row.totalQuantity
                || (reusable && (row.ledgerIssued != row.issued || row.totalQuantity < row.holds + row.issued))
                || (!reusable && (!"CONSUMABLE".equals(row.type) || row.ledgerIssued != 0 || row.issued != 0
                        || row.totalQuantity < row.holds))) throw integrityFailure();
        long available = row.totalQuantity - row.holds - (reusable ? row.issued : 0);
        if (available < 0 || available > 1000000) throw integrityFailure();
        return new InventoryDtos.ResourceState(row.publicId, row.name, row.type, row.totalQuantity,
                Math.toIntExact(available), row.version);
    }

    private void insertMovement(Resource resource, Allocation allocation, long actorMembershipId,
            UUID operationId, byte[] requestFingerprint, String kind, int quantity,
            long deltaTotal, long deltaIssued, LocalDateTime occurredAt) {
        try {
            jdbc.update("INSERT INTO stock_movement(public_id,organization_id,resource_id,allocation_id,"
                    + "actor_membership_id,operation_public_id,request_fingerprint,kind,quantity,delta_total,delta_issued,occurred_at) "
                    + "VALUES(UUID_TO_BIN(?,0),?,?,?,?,UUID_TO_BIN(?,0),?,?,?,?,?,?)",
                    UUID.randomUUID().toString(), resource.organizationId, resource.id,
                    allocation == null ? null : allocation.id, actorMembershipId, operationId.toString(),
                    requestFingerprint, kind, quantity, deltaTotal, deltaIssued, Timestamp.valueOf(occurredAt));
        } catch (DataIntegrityViolationException constraint) {
            throw new C1ApiException(409, "STOCK_OPERATION_CONFLICT", "The stock operation conflicts with committed history.");
        }
    }

    private Actor requireLegacyActor(long organizationId) {
        Actor actor = lockOrganization(organizationId, true);
        requireStockRole(actor.membership);
        return actor;
    }

    private void validateResource(Resource resource) {
        if (!javaxValidator.validate(resource).isEmpty()) throw new C1ValidationException();
    }

    private static void validateIdempotencyKey(String suppliedKey) {
        if (suppliedKey == null || suppliedKey.length() < 22 || suppliedKey.length() > 200
                || !suppliedKey.matches("[A-Za-z0-9_-]+"))
            throw new C1ApiException(400, "IDEMPOTENCY_KEY_REQUIRED", "Supply a random Idempotency-Key with at least 128 bits of entropy.");
    }

    private static String normalizeName(String value) {
        if (value == null) throw new C1ApiException(400, "VALIDATION_FAILED", "name is required.");
        String name = value.trim().replaceAll("\\s+", " ");
        if (name.isEmpty() || name.length() > 100)
            throw new C1ApiException(400, "VALIDATION_FAILED", "name must contain 1 to 100 characters.");
        return name;
    }

    private static void requireStockRole(Membership membership) {
        if (!isStockRole(membership.role))
            throw new C1ApiException(403, "FORBIDDEN", "The current membership cannot perform stock operations.");
    }

    private static void requireProjectManager(Membership membership) {
        if (membership.role != Membership.Role.OWNER && membership.role != Membership.Role.ADMIN)
            throw new C1ApiException(403, "FORBIDDEN", "Only owners and administrators can assign Project leads.");
    }

    static void requireProjectWriterRole(Membership member) {
        if (member.role != Membership.Role.OWNER && member.role != Membership.Role.ADMIN && member.role != Membership.Role.LEAD)
            throw new C1ApiException(403, "FORBIDDEN", "The current membership cannot manage Project work.");
    }

    static void requireProjectWriter(Membership member, Project project) {
        requireProjectWriterRole(member);
        if (member.role == Membership.Role.LEAD && !member.id.equals(project.leadMembershipId)) throw notFound();
    }

    private static void requireProjectReader(Membership member) {
        if (member.role == Membership.Role.MEMBER)
            throw new C1ApiException(403, "FORBIDDEN", "The current membership cannot view Project Allocation records.");
    }

    private static void requireOwnProjectAllocation(Membership member, Project project, Allocation allocation) {
        requireProjectWriter(member, project);
        if (member.role != Membership.Role.LEAD || !member.id.equals(allocation.requestedByMembershipId)) throw notFound();
    }

    private static boolean isStockRole(Membership.Role role) {
        return role == Membership.Role.OWNER || role == Membership.Role.CUSTODIAN;
    }

    static void requireVersion(String ifMatch, long currentVersion) {
        if (ifMatch == null || ifMatch.isBlank())
            throw new C1ApiException(428, "PRECONDITION_REQUIRED", "A strong If-Match version is required.");
        if (!ifMatch.matches("\\\"v[0-9]+\\\""))
            throw new C1ApiException(400, "INVALID_ETAG", "If-Match must be a strong quoted version such as \\\"v0\\\".");
        long supplied;
        try { supplied = Long.parseLong(ifMatch.substring(2, ifMatch.length() - 1)); }
        catch (NumberFormatException invalid) { throw new C1ApiException(400, "INVALID_ETAG", "If-Match version is invalid."); }
        if (supplied != currentVersion)
            throw new C1ApiException(412, "STALE_VERSION", "The record changed; reload it before retrying.");
    }

    private static void requireState(Allocation allocation, AllocationStatus expected) {
        if (allocation.status != expected)
            throw invalidState("Only " + expected + " allocations can perform this operation.");
    }

    private static void requireType(Resource resource, ResourceType expected) {
        if (resource.type != expected)
            throw new C1ApiException(409, "RESOURCE_TYPE_MISMATCH", "This operation does not match the Resource type.");
    }

    private static void requireProjectActive(Project project, LocalDateTime now) {
        if (project.startAt.isAfter(now) || !project.endAt.isAfter(now))
            throw new C1ApiException(409, "PROJECT_NOT_ACTIVE", "The Project must satisfy startAt <= now < endAt.");
    }

    private String toJson(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException failure) { throw new IllegalStateException("Could not encode an inventory response.", failure); }
    }

    private static byte[] fingerprint(String value) {
        try { return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)); }
        catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException("SHA-256 unavailable.", impossible); }
    }

    private LocalDateTime businessNow() { return LocalDateTime.now(clock); }
    private LocalDateTime utcNow() { return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC); }
    public static String etag(long version) { return "\"v" + version + "\""; }
    private static C1ApiException notFound() { return new C1ApiException(404, "NOT_FOUND", "The requested record was not found."); }
    private static C1ApiException integrityFailure() { return new C1ApiException(500, "INVENTORY_INTEGRITY_FAILURE", "Inventory could not be reconciled safely."); }
    private static C1ApiException invalidState(String message) { return new C1ApiException(409, "INVALID_ALLOCATION_STATE", message); }

    public enum InventoryAction { APPROVE, ISSUE, RETURN, CONSUME, CANCEL }
    public record OperationResponse(int status, String json, String etag) { }
    public record ProjectAccessView(UUID id, String name, String startAt, String endAt, UUID leadMembershipId, long version) { }
    public record BorrowerOption(UUID membershipId, String displayName) { }
    public record LinkedAllocationView(UUID id, UUID requirementId, UUID borrowerMembershipId,
            String borrowerNameSnapshot, int quantity, String status, long version) { }
    public record BorrowedView(UUID id, String projectName, String resourceName, int quantity, String status,
            LocalDateTime createdAt, LocalDateTime approvedAt, LocalDateTime issuedAt, LocalDateTime returnedAt,
            LocalDateTime consumedAt, LocalDateTime cancelledAt, long version) { }
    public record AllocationChangeView(UUID id, int quantity, String status, long version) { }
    public record ProjectAllocationView(UUID id, UUID requirementId, String resourceName, String borrowerNameSnapshot,
            int quantity, String status, long version, boolean canCancel, boolean canDelete) { }
    record Actor(Organization organization, Membership membership, long userId) { }
    private record Balance(long ledgerTotal, long ledgerIssued, long holds, long issued) { }
    private record ResourcePageRow(UUID publicId, String name, String type, int totalQuantity, long version,
                                   long ledgerTotal, long ledgerIssued, long holds, long issued, long id) { }
    private record AllocationPageRow(UUID publicId, UUID resourcePublicId, String status,
                                     int quantity, long version, long id) { }
    private record AllocationSnapshot(long allocationId, long projectId, long resourceId,
                                      long requirementId, int requirementQuantity) { }
    private record LockedAllocation(Project project, Resource resource, ResourceRequirement requirement,
                                    Allocation allocation) { }
    private record IdempotencyClaim(Long id, OperationResponse replay) {
        static IdempotencyClaim none() { return new IdempotencyClaim(null, null); }
    }
    private record Receipt(Long id, byte[] bodyHash, String state, UUID operationId, int status,
                           String responseJson, String etag, LocalDateTime expiresAt,
                           long actorUserId, String route) {
        OperationResponse response() { return new OperationResponse(status, responseJson, etag); }
    }
    private record MovementFact(UUID operationId, UUID resourceId, UUID allocationId, String kind,
                                int quantity, long deltaTotal, long deltaIssued,
                                LocalDateTime occurredAt, byte[] requestFingerprint, long actorUserId) { }
}
