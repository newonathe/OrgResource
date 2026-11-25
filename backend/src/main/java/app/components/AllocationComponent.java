package app.components;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import app.dto.ApiDtos.AllocationView;
import app.dto.ApiDtos.MessageView;
import app.entities.Allocation;
import app.entities.AllocationStatus;
import app.entities.CancellationReason;
import app.entities.Project;
import app.entities.Resource;
import app.entities.ResourceRequirement;
import app.entities.ResourceType;
import app.repositories.AllocationRepository;
import app.repositories.ProjectRepository;
import app.repositories.ResourceRepository;
import app.repositories.ResourceRequirementRepository;

@Component
public class AllocationComponent {
    private static final List<AllocationStatus> COMPLETED = Arrays.asList(
            AllocationStatus.ISSUED, AllocationStatus.RETURNED, AllocationStatus.CONSUMED);

    @Autowired private AllocationRepository allocations;
    @Autowired private ResourceRequirementRepository requirements;
    @Autowired private ProjectRepository projects;
    @Autowired private ResourceRepository resources;
    @Autowired private Clock philippineClock;
    @PersistenceContext private EntityManager entityManager;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AllocationView create(Long requirementId, Integer quantity, String rawBorrowerName,
                                 String phoneNumber, Boolean requestedConsent) {
        if (requirementId == null || requirementId <= 0) {
            throw new ApiException(400, "VALIDATION_FAILED", "requirementId must be positive.");
        }
        if (quantity == null || quantity < 1 || quantity > 1000000) {
            throw new ApiException(400, "VALIDATION_FAILED", "quantity must be from 1 to 1000000.");
        }
        String borrowerName = NameNormalizer.display(rawBorrowerName, "borrowerName");
        boolean consent = Boolean.TRUE.equals(requestedConsent);
        if (consent && (phoneNumber == null || !phoneNumber.matches("^\\+[1-9][0-9]{7,14}$"))) {
            throw new ApiException(400, "VALIDATION_FAILED", "smsConsent requires a valid fictional E.164 destination.");
        }

        ResourceRequirement initial = requirements.findById(requirementId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Requirement not found."));
        Long projectId = initial.getProject().getId();
        Long resourceId = initial.getResource().getId();
        Project project = lockProject(projectId);
        Resource resource = lockResource(resourceId);
        ResourceRequirement requirement = lockRequirement(requirementId);
        refreshForUpdate(requirement);
        if (!projectId.equals(requirement.getProject().getId())
                || !resourceId.equals(requirement.getResource().getId())) {
            throw new ApiException(409, "INVALID_ALLOCATION_STATE", "Requirement parents changed during the operation.");
        }

        LocalDateTime now = LocalDateTime.now(philippineClock);
        if (!project.getEndAt().isAfter(now)) {
            throw new ApiException(409, "PROJECT_ENDED", "Allocations cannot be created for an ended Project.");
        }
        if (quantity > requirement.getQuantity()) {
            throw new ApiException(400, "VALIDATION_FAILED", "quantity cannot exceed the Requirement quantity.");
        }
        Allocation allocation = new Allocation(requirement, quantity, borrowerName,
                consent ? phoneNumber : null, consent, now);
        return view(allocations.saveAndFlush(allocation), now);
    }

    @Transactional(readOnly = true)
    public List<AllocationView> list(Long requirementId, AllocationStatus status) {
        if (requirementId != null && requirementId <= 0) {
            throw new ApiException(400, "VALIDATION_FAILED", "requirementId must be positive.");
        }
        List<Allocation> rows;
        if (requirementId != null && status != null) {
            rows = allocations.findByRequirement_IdAndStatus(requirementId, status);
        } else if (requirementId != null) {
            rows = allocations.findByRequirement_Id(requirementId);
        } else if (status != null) {
            rows = allocations.findByStatus(status);
        } else {
            rows = allocations.findAll();
        }
        LocalDateTime now = LocalDateTime.now(philippineClock);
        return rows.stream().map(row -> view(row, now)).collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AllocationView approve(Long id) {
        LockedAllocation context = lockAllocation(id);
        Allocation allocation = context.allocation;
        requireStatus(allocation, AllocationStatus.PENDING, "Only PENDING allocations can be approved.");
        LocalDateTime now = LocalDateTime.now(philippineClock);
        if (!context.project.getEndAt().isAfter(now)) {
            throw new ApiException(409, "PROJECT_ENDED", "An ended Project cannot approve an Allocation.");
        }

        long covered = coveredQuantity(context.requirement, now);
        if (allocation.approvedAt != null) {
            throw new ApiException(409, "INVALID_ALLOCATION_STATE", "A historically approved Allocation cannot be approved again.");
        }
        long demandRemaining = (long) context.requirement.getQuantity() - covered;
        if (demandRemaining <= 0L || allocation.quantity.longValue() > demandRemaining) {
            throw new ApiException(409, "REQUIREMENT_ALREADY_COVERED",
                    "The Allocation exceeds the Requirement's remaining uncovered quantity.");
        }
        long available = availableQuantity(context.resource, now);
        if (allocation.quantity.longValue() > available) {
            throw new ApiException(409, "INSUFFICIENT_STOCK", "The Resource does not have enough available stock.");
        }

        allocation.status = AllocationStatus.APPROVED;
        allocation.approvedAt = now;
        Allocation saved = allocations.saveAndFlush(allocation);
        return view(saved, now);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AllocationView issue(Long id) {
        LockedAllocation context = lockAllocation(id);
        Allocation allocation = context.allocation;
        requireStatus(allocation, AllocationStatus.APPROVED, "Only APPROVED allocations can be issued.");
        if (context.resource.getType() != ResourceType.REUSABLE) {
            throw new ApiException(409, "RESOURCE_TYPE_MISMATCH", "Only reusable Resources can be issued.");
        }
        LocalDateTime now = LocalDateTime.now(philippineClock);
        requireActiveProject(context.project, now);
        allocation.status = AllocationStatus.ISSUED;
        allocation.issuedAt = now;
        return view(allocations.saveAndFlush(allocation), now);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AllocationView returnItems(Long id) {
        LockedAllocation context = lockAllocation(id);
        Allocation allocation = context.allocation;
        requireStatus(allocation, AllocationStatus.ISSUED, "Only ISSUED allocations can be returned.");
        if (context.resource.getType() != ResourceType.REUSABLE) {
            throw new ApiException(409, "RESOURCE_TYPE_MISMATCH", "Only reusable Resources can be returned.");
        }
        LocalDateTime now = LocalDateTime.now(philippineClock);
        allocation.status = AllocationStatus.RETURNED;
        allocation.returnedAt = now;
        return view(allocations.saveAndFlush(allocation), now);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AllocationView consume(Long id) {
        LockedAllocation context = lockAllocation(id);
        Allocation allocation = context.allocation;
        requireStatus(allocation, AllocationStatus.APPROVED, "Only APPROVED allocations can be consumed.");
        if (context.resource.getType() != ResourceType.CONSUMABLE) {
            throw new ApiException(409, "RESOURCE_TYPE_MISMATCH", "Only consumable Resources can be consumed.");
        }
        LocalDateTime now = LocalDateTime.now(philippineClock);
        requireActiveProject(context.project, now);
        int total = context.resource.getTotalQuantity();
        if (total < allocation.quantity) {
            throw new ApiException(409, "INSUFFICIENT_STOCK", "The Resource does not have enough physical stock to consume.");
        }

        context.resource.setTotalQuantity(Math.subtractExact(total, allocation.quantity));
        resources.saveAndFlush(context.resource);
        allocation.status = AllocationStatus.CONSUMED;
        allocation.consumedAt = now;
        allocations.saveAndFlush(allocation);
        return view(allocation, now);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AllocationView cancel(Long id) {
        LockedAllocation context = lockAllocation(id);
        Allocation allocation = context.allocation;
        if (allocation.status != AllocationStatus.PENDING && allocation.status != AllocationStatus.APPROVED) {
            throw new ApiException(409, "INVALID_ALLOCATION_STATE", "Only PENDING or APPROVED allocations can be cancelled.");
        }
        LocalDateTime now = LocalDateTime.now(philippineClock);
        allocation.status = AllocationStatus.CANCELLED;
        allocation.cancelledAt = now;
        allocation.cancellationReason = context.project.getEndAt().isAfter(now)
                ? CancellationReason.USER_REQUEST : CancellationReason.EXPIRED;
        return view(allocations.saveAndFlush(allocation), now);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MessageView delete(Long id) {
        LockedAllocation context = lockAllocation(id);
        Allocation allocation = context.allocation;
        if (allocation.approvedAt != null) {
            throw new ApiException(409, "HISTORY_PROTECTED", "Historically approved Allocations cannot be deleted.");
        }
        if (allocation.status != AllocationStatus.PENDING && allocation.status != AllocationStatus.CANCELLED) {
            throw new ApiException(409, "INVALID_ALLOCATION_STATE", "Only never-approved PENDING or CANCELLED allocations can be deleted.");
        }
        allocations.delete(allocation);
        allocations.flush();
        return new MessageView("Record deleted.");
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AllocationView revokeSmsConsent(Long id) {
        requirePositiveId(id);
        Allocation allocation = allocations.lockById(id)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Allocation not found."));
        refreshForUpdate(allocation);
        allocation.smsConsent = Boolean.FALSE;
        allocation.phoneNumber = null;
        LocalDateTime now = LocalDateTime.now(philippineClock);
        return view(allocations.saveAndFlush(allocation), now);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean expireIfEligible(Long id) {
        LockedAllocation context = lockAllocation(id);
        Allocation allocation = context.allocation;
        if (allocation.status != AllocationStatus.PENDING && allocation.status != AllocationStatus.APPROVED) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now(philippineClock);
        if (context.project.getEndAt().isAfter(now)) {
            return false;
        }
        allocation.status = AllocationStatus.CANCELLED;
        allocation.cancellationReason = CancellationReason.EXPIRED;
        allocation.cancelledAt = now;
        allocations.saveAndFlush(allocation);
        return true;
    }

    private LockedAllocation lockAllocation(Long id) {
        requirePositiveId(id);
        Allocation initialAllocation = allocations.findById(id)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Allocation not found."));
        Long requirementId = initialAllocation.getRequirement().getId();
        ResourceRequirement initialRequirement = requirements.findById(requirementId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Requirement not found."));
        Long projectId = initialRequirement.getProject().getId();
        Long resourceId = initialRequirement.getResource().getId();

        Project project = lockProject(projectId);
        Resource resource = lockResource(resourceId);
        ResourceRequirement requirement = lockRequirement(requirementId);
        refreshForUpdate(requirement);
        Allocation allocation = allocations.lockById(id)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Allocation not found."));
        refreshForUpdate(allocation);

        if (!projectId.equals(requirement.getProject().getId())
                || !resourceId.equals(requirement.getResource().getId())
                || !requirementId.equals(allocation.getRequirement().getId())) {
            throw new ApiException(409, "INVALID_ALLOCATION_STATE", "Allocation parent relationships changed during the operation.");
        }
        return new LockedAllocation(project, resource, requirement, allocation);
    }

    private Project lockProject(Long id) {
        Project project = projects.lockById(id)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Project not found."));
        refreshForUpdate(project);
        return project;
    }

    private Resource lockResource(Long id) {
        Resource resource = resources.lockById(id)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Resource not found."));
        refreshForUpdate(resource);
        return resource;
    }

    private ResourceRequirement lockRequirement(Long id) {
        return requirements.lockById(id)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Requirement not found."));
    }

    private void refreshForUpdate(Object entity) {
        if (entityManager != null) {
            entityManager.refresh(entity);
        }
    }

    private long availableQuantity(Resource resource, LocalDateTime now) {
        Long holdsValue = allocations.sumEffectiveHolds(resource.getId(), AllocationStatus.APPROVED, now);
        long holds = holdsValue == null ? 0L : holdsValue;
        long issued = 0L;
        if (resource.getType() == ResourceType.REUSABLE) {
            Long issuedValue = allocations.sumIssuedCustody(resource.getId(), AllocationStatus.ISSUED);
            issued = issuedValue == null ? 0L : issuedValue;
        }
        long available;
        try {
            available = Math.subtractExact((long) resource.getTotalQuantity(), holds);
            if (resource.getType() == ResourceType.REUSABLE) {
                available = Math.subtractExact(available, issued);
            }
        } catch (ArithmeticException overflow) {
            throw new ApiException(500, "INTERNAL_ERROR", "Stored Resource aggregates exceed supported whole-unit limits.");
        }
        if (available < 0L || available > resource.getTotalQuantity()) {
            throw new ApiException(500, "INTERNAL_ERROR", "Stored Resource commitments violate inventory invariants.");
        }
        return available;
    }

    private long coveredQuantity(ResourceRequirement requirement, LocalDateTime now) {
        Long sum = allocations.sumCoveredQuantity(requirement.getId(), COMPLETED,
                AllocationStatus.APPROVED, now);
        return sum == null ? 0L : sum;
    }

    private void requireActiveProject(Project project, LocalDateTime now) {
        if (now.isBefore(project.getStartAt()) || !now.isBefore(project.getEndAt())) {
            throw new ApiException(409, "OUTSIDE_PROJECT_PERIOD", "This operation is allowed only during the Project period.");
        }
    }

    private void requireStatus(Allocation allocation, AllocationStatus required, String message) {
        if (allocation.status != required) {
            throw new ApiException(409, "INVALID_ALLOCATION_STATE", message);
        }
    }

    private void requirePositiveId(Long id) {
        if (id == null || id <= 0) {
            throw new ApiException(400, "VALIDATION_FAILED", "Allocation ID must be positive.");
        }
    }

    private AllocationView view(Allocation allocation, LocalDateTime now) {
        boolean open = allocation.status == AllocationStatus.PENDING || allocation.status == AllocationStatus.APPROVED;
        boolean expired = open && !allocation.getRequirement().getProject().getEndAt().isAfter(now);
        return new AllocationView(allocation, expired);
    }

    private static final class LockedAllocation {
        private final Project project;
        private final Resource resource;
        private final ResourceRequirement requirement;
        private final Allocation allocation;

        private LockedAllocation(Project project, Resource resource,
                                 ResourceRequirement requirement, Allocation allocation) {
            this.project = project;
            this.resource = resource;
            this.requirement = requirement;
            this.allocation = allocation;
        }
    }
}
