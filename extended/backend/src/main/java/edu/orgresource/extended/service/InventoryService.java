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
import edu.orgresource.extended.identity.C1HttpResponses;
import edu.orgresource.extended.identity.IdentityDtos;
import edu.orgresource.extended.identity.Membership;
import edu.orgresource.extended.identity.MembershipRepository;
import edu.orgresource.extended.identity.Organization;
import edu.orgresource.extended.identity.OrganizationRepository;
import edu.orgresource.extended.repository.AllocationRepository;
import edu.orgresource.extended.repository.ProjectRepository;
import edu.orgresource.extended.repository.ResourceRepository;
import edu.orgresource.extended.repository.ResourceRequirementRepository;
import edu.orgresource.extended.security.C1OrganizationContext;

@Service
public class InventoryService {
    private static final int MAX_MOVEMENT_PAGE = 100;
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

    public InventoryService(OrganizationRepository organizations, MembershipRepository memberships,
            C1OrganizationContext organizationContext, ResourceRepository resources,
            ProjectRepository projects, ResourceRequirementRepository requirements,
            AllocationRepository allocations, JdbcTemplate jdbc, Clock clock,
            ObjectMapper objectMapper, Validator javaxValidator) {
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
        requireStockRole(actor.membership);
        AllocationSnapshot snapshot = allocations.findOperationSnapshotByPublicId(allocationPublicId,
                actor.organization.id).map(s -> new AllocationSnapshot(s.getAllocationId(), s.getProjectId(),
                        s.getResourceId(), s.getRequirementId(), s.getRequirementQuantity()))
                .orElseThrow(InventoryService::notFound);
        LockedAllocation locked = lockAllocation(snapshot, actor.organization.id);
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
        locked.allocation.smsConsent = false;
        locked.allocation.phoneCiphertext = null;
        locked.allocation.phoneNonce = null;
        locked.allocation.phoneKeyId = null;
        if (locked.allocation.reminderOutcome == edu.orgresource.extended.domain.ReminderOutcome.CLAIMED)
            locked.allocation.reminderOutcome = edu.orgresource.extended.domain.ReminderOutcome.SUPPRESSED;
        allocations.saveAndFlush(locked.allocation);
        return new ApiDtos.AllocationView(locked.allocation, locked.project.endAt, businessNow());
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
        requireStockRole(actor.membership);
        Allocation allocation = allocations.findByPublicIdAndOrganizationId(allocationPublicId, actor.organization.id)
                .orElseThrow(InventoryService::notFound);
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

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String operationReceipt(UUID organizationPublicId, UUID operationId, String requestId) {
        Actor actor = lockPathOrganization(organizationPublicId, false);
        requireStockRole(actor.membership);
        Optional<MovementFact> movement = findMovementByOperation(actor.organization.id, operationId);
        if (movement.isPresent()) {
            MovementFact fact = movement.get();
            return durableMovementResponse(fact, requestId).json;
        }
        Receipt receipt = findReceiptByOperation(actor.organization.id, operationId).orElseThrow(InventoryService::notFound);
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

    private Actor lockPathOrganization(UUID organizationPublicId, boolean mutation) {
        Organization organization = organizations.findByPublicId(organizationPublicId)
                .orElseThrow(InventoryService::notFound);
        return lockOrganization(organization.id, mutation);
    }

    private Actor lockOrganization(long organizationId, boolean mutation) {
        Organization organization = organizations.lockById(organizationId).orElseThrow(InventoryService::notFound);
        if (mutation && organization.status != Organization.Status.ACTIVE)
            throw new C1ApiException(409, "ORG_FROZEN", "A frozen organization is read-only.");
        long userId = organizationContext.currentUserId();
        Membership locked = memberships.lockByOrganizationAndUser(organizationId, userId)
                .orElseThrow(InventoryService::notFound);
        organizationContext.requireCurrentMembership(organizationId);
        if (locked.status != Membership.Status.ACTIVE)
            throw notFound();
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

    private OperationResponse complete(IdempotencyClaim claim, String requestId, int status,
            InventoryDtos.OperationView data, String etag) {
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

    private static boolean isStockRole(Membership.Role role) {
        return role == Membership.Role.OWNER || role == Membership.Role.CUSTODIAN;
    }

    private static void requireVersion(String ifMatch, long currentVersion) {
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
    private static String etag(long version) { return "\"v" + version + "\""; }
    private static C1ApiException notFound() { return new C1ApiException(404, "NOT_FOUND", "The requested record was not found."); }
    private static C1ApiException integrityFailure() { return new C1ApiException(500, "INVENTORY_INTEGRITY_FAILURE", "Inventory could not be reconciled safely."); }
    private static C1ApiException invalidState(String message) { return new C1ApiException(409, "INVALID_ALLOCATION_STATE", message); }

    public enum InventoryAction { APPROVE, ISSUE, RETURN, CONSUME, CANCEL }
    public record OperationResponse(int status, String json, String etag) { }
    private record Actor(Organization organization, Membership membership, long userId) { }
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
