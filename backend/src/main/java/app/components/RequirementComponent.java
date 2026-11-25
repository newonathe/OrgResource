package app.components;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import app.dto.ApiDtos.MessageView;
import app.dto.ApiDtos.RequirementView;
import app.entities.AllocationStatus;
import app.entities.Project;
import app.entities.Resource;
import app.entities.ResourceRequirement;
import app.repositories.AllocationRepository;
import app.repositories.ProjectRepository;
import app.repositories.ResourceRepository;
import app.repositories.ResourceRequirementRepository;

@Component
public class RequirementComponent {
    private static final List<AllocationStatus> COMPLETED = Arrays.asList(
            AllocationStatus.ISSUED, AllocationStatus.RETURNED, AllocationStatus.CONSUMED);

    @Autowired private ResourceRequirementRepository requirements;
    @Autowired private ProjectRepository projects;
    @Autowired private ResourceRepository resources;
    @Autowired private AllocationRepository allocations;
    @Autowired private Clock philippineClock;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public RequirementView create(Long projectId, Long resourceId, Integer quantity) {
        if (quantity == null || quantity < 1 || quantity > 1000000) {
            throw new ApiException(400, "VALIDATION_FAILED", "quantity must be from 1 to 1000000.");
        }
        Project project = projects.lockById(projectId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Project not found."));
        Resource resource = resources.lockById(resourceId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Resource not found."));
        LocalDateTime now = LocalDateTime.now(philippineClock);
        if (!project.getEndAt().isAfter(now)) {
            throw new ApiException(409, "PROJECT_ENDED", "Requirements cannot be added to an ended Project.");
        }
        if (requirements.existsByProject_IdAndResource_Id(projectId, resourceId)) {
            throw new ApiException(409, "DUPLICATE_REQUIREMENT", "This Project already has a Requirement for that Resource.");
        }
        ResourceRequirement saved = requirements.save(new ResourceRequirement(project, resource, quantity));
        return view(saved, now);
    }

    @Transactional(readOnly = true)
    public List<RequirementView> list(Long projectId) {
        if (projectId != null && projectId <= 0) {
            throw new ApiException(400, "VALIDATION_FAILED", "projectId must be positive.");
        }
        List<ResourceRequirement> rows = projectId == null
                ? requirements.findAll() : requirements.findByProject_Id(projectId);
        LocalDateTime now = LocalDateTime.now(philippineClock);
        return rows.stream().map(requirement -> view(requirement, now)).collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public RequirementView update(Long id, Integer quantity) {
        requirePositiveId(id);
        validateQuantity(quantity);
        ResourceRequirement initial = requirements.findById(id)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Requirement not found."));
        Long projectId = initial.project.getId();
        Long resourceId = initial.resource.getId();
        Project project = projects.lockById(projectId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Project not found."));
        resources.lockById(resourceId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Resource not found."));
        ResourceRequirement requirement = requirements.lockById(id)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Requirement not found."));

        LocalDateTime now = LocalDateTime.now(philippineClock);
        if (!project.getEndAt().isAfter(now)) {
            throw new ApiException(409, "PROJECT_ENDED", "Requirements cannot be changed after the Project ends.");
        }
        Long coveredValue = allocations.sumCoveredQuantity(requirement.id, COMPLETED,
                AllocationStatus.APPROVED, now);
        long covered = coveredValue == null ? 0L : coveredValue;
        Integer pendingValue = allocations.maxPendingQuantity(requirement.id, AllocationStatus.PENDING);
        int largestPending = pendingValue == null ? 0 : pendingValue;
        if (quantity.longValue() < Math.max(covered, (long) largestPending)) {
            throw new ApiException(409, "QUANTITY_BELOW_COMMITMENTS",
                    "quantity cannot be lower than current coverage or the largest live pending Allocation.");
        }

        requirement.quantity = quantity;
        ResourceRequirement saved = requirements.saveAndFlush(requirement);
        return view(saved, now);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MessageView delete(Long id) {
        requirePositiveId(id);
        ResourceRequirement initial = requirements.findById(id)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Requirement not found."));
        Long projectId = initial.project.getId();
        Long resourceId = initial.resource.getId();
        projects.lockById(projectId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Project not found."));
        resources.lockById(resourceId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Resource not found."));
        ResourceRequirement requirement = requirements.lockById(id)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Requirement not found."));
        if (allocations.existsByRequirement_Id(id)) {
            throw new ApiException(409, "DEPENDENCY_EXISTS", "Requirement still has Allocation history.");
        }
        requirements.delete(requirement);
        requirements.flush();
        return new MessageView("Record deleted.");
    }

    private RequirementView view(ResourceRequirement requirement, LocalDateTime now) {
        Long sum = allocations.sumCoveredQuantity(requirement.id, COMPLETED,
                AllocationStatus.APPROVED, now);
        return new RequirementView(requirement, sum == null ? 0 : Math.toIntExact(sum));
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1 || quantity > 1000000) {
            throw new ApiException(400, "VALIDATION_FAILED", "quantity must be from 1 to 1000000.");
        }
    }

    private void requirePositiveId(Long id) {
        if (id == null || id <= 0) {
            throw new ApiException(400, "VALIDATION_FAILED", "Requirement ID must be positive.");
        }
    }
}
