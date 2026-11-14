package app.components;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import app.dto.ApiDtos.AllocationView;
import app.entities.Allocation;
import app.entities.AllocationStatus;
import app.entities.Project;
import app.entities.Resource;
import app.entities.ResourceRequirement;
import app.repositories.AllocationRepository;
import app.repositories.ProjectRepository;
import app.repositories.ResourceRepository;
import app.repositories.ResourceRequirementRepository;

@Component
public class AllocationComponent {
    @Autowired private AllocationRepository allocations;
    @Autowired private ResourceRequirementRepository requirements;
    @Autowired private ProjectRepository projects;
    @Autowired private ResourceRepository resources;
    @Autowired private Clock philippineClock;

    @Transactional
    public AllocationView create(Long requirementId, Integer quantity, String rawBorrowerName,
                                 String phoneNumber, Boolean requestedConsent) {
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
        Long projectId = initial.project.getId();
        Long resourceId = initial.resource.getId();
        Project project = projects.lockById(projectId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Project not found."));
        Resource resource = resources.lockById(resourceId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Resource not found."));
        ResourceRequirement requirement = requirements.lockById(requirementId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Requirement not found."));
        LocalDateTime now = LocalDateTime.now(philippineClock);
        if (!project.getEndAt().isAfter(now)) {
            throw new ApiException(409, "PROJECT_ENDED", "Allocations cannot be created for an ended Project.");
        }
        if (quantity > requirement.quantity) {
            throw new ApiException(400, "VALIDATION_FAILED", "quantity cannot exceed the Requirement quantity.");
        }
        Allocation allocation = new Allocation(requirement, quantity, borrowerName,
                consent ? phoneNumber : null, consent, now);
        return view(allocations.save(allocation), now);
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

    private AllocationView view(Allocation allocation, LocalDateTime now) {
        boolean open = allocation.status == AllocationStatus.PENDING || allocation.status == AllocationStatus.APPROVED;
        boolean expired = open && !allocation.requirement.getProject().getEndAt().isAfter(now);
        return new AllocationView(allocation, expired);
    }
}
