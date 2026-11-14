package app.components;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
    private static final List<AllocationStatus> COVERED = Arrays.asList(
            AllocationStatus.APPROVED, AllocationStatus.ISSUED,
            AllocationStatus.RETURNED, AllocationStatus.CONSUMED);

    @Autowired private ResourceRequirementRepository requirements;
    @Autowired private ProjectRepository projects;
    @Autowired private ResourceRepository resources;
    @Autowired private AllocationRepository allocations;
    @Autowired private Clock philippineClock;

    @Transactional
    public RequirementView create(Long projectId, Long resourceId, Integer quantity) {
        if (quantity == null || quantity < 1 || quantity > 1000000) {
            throw new ApiException(400, "VALIDATION_FAILED", "quantity must be from 1 to 1000000.");
        }
        Project project = projects.findById(projectId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Project not found."));
        Resource resource = resources.findById(resourceId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Resource not found."));
        if (!project.endAt.isAfter(LocalDateTime.now(philippineClock))) {
            throw new ApiException(409, "PROJECT_ENDED", "Requirements cannot be added to an ended Project.");
        }
        if (requirements.existsByProject_IdAndResource_Id(projectId, resourceId)) {
            throw new ApiException(409, "DUPLICATE_REQUIREMENT", "This Project already has a Requirement for that Resource.");
        }
        ResourceRequirement saved = requirements.save(new ResourceRequirement(project, resource, quantity));
        return view(saved);
    }

    @Transactional(readOnly = true)
    public List<RequirementView> list(Long projectId) {
        if (projectId != null && projectId <= 0) {
            throw new ApiException(400, "VALIDATION_FAILED", "projectId must be positive.");
        }
        List<ResourceRequirement> rows = projectId == null
                ? requirements.findAll() : requirements.findByProject_Id(projectId);
        return rows.stream().map(this::view).collect(Collectors.toList());
    }

    private RequirementView view(ResourceRequirement requirement) {
        Long sum = allocations.sumCoveredQuantity(requirement.id, COVERED);
        return new RequirementView(requirement, sum == null ? 0 : sum.intValue());
    }
}
