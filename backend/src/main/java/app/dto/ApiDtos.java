package app.dto;

import java.time.LocalDateTime;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import app.entities.Allocation;
import app.entities.AllocationStatus;
import app.entities.Project;
import app.entities.Resource;
import app.entities.ResourceRequirement;
import app.entities.ResourceType;

/** JSON request and response shapes. JPA entities are never serialized by controllers. */
public final class ApiDtos {
    private ApiDtos() { }

    public static class CreateDepartment {
        @NotBlank @Size(max = 100) public String name;
    }

    public static class UpdateDepartment {
        @NotBlank @Size(max = 100) public String name;
    }

    public static class DepartmentView {
        public Long id;
        public String name;
        public DepartmentView() { }
        public DepartmentView(Long id, String name) { this.id = id; this.name = name; }
    }

    public static class CreateProject {
        @NotNull @Positive public Long departmentId;
        @NotBlank @Size(max = 100) public String name;
        @NotNull public LocalDateTime startAt;
        @NotNull public LocalDateTime endAt;
    }

    public static class ProjectView {
        public Long id;
        public Long departmentId;
        public String name;
        public LocalDateTime startAt;
        public LocalDateTime endAt;
        public ProjectView() { }
        public ProjectView(Project project) {
            this.id = project.id;
            this.departmentId = project.department.getId();
            this.name = project.name;
            this.startAt = project.startAt;
            this.endAt = project.endAt;
        }
    }

    public static class CreateResource {
        @NotBlank @Size(max = 100) public String name;
        @NotNull public ResourceType type;
        @NotNull @Min(0) @Max(1000000) public Integer totalQuantity;
    }

    public static class ResourceView {
        public Long id;
        public String name;
        public ResourceType type;
        public Integer totalQuantity;
        public Integer availableQuantity;
        public ResourceView() { }
        public ResourceView(Resource resource, int availableQuantity) {
            this.id = resource.id;
            this.name = resource.name;
            this.type = resource.type;
            this.totalQuantity = resource.totalQuantity;
            this.availableQuantity = availableQuantity;
        }
    }

    public static class ResourceAvailabilityView {
        public Long resourceId;
        public Integer availableQuantity;
        public ResourceAvailabilityView() { }
        public ResourceAvailabilityView(Long resourceId, Integer availableQuantity) {
            this.resourceId = resourceId;
            this.availableQuantity = availableQuantity;
        }
    }

    public static class CreateRequirement {
        @NotNull @Positive public Long projectId;
        @NotNull @Positive public Long resourceId;
        @NotNull @Min(1) @Max(1000000) public Integer quantity;
    }

    public static class RequirementView {
        public Long id;
        public Long projectId;
        public Long resourceId;
        public Integer quantity;
        public Integer allocatedQuantity;
        public Integer shortageQuantity;
        public RequirementView() { }
        public RequirementView(ResourceRequirement requirement, int allocatedQuantity) {
            this.id = requirement.id;
            this.projectId = requirement.project.getId();
            this.resourceId = requirement.resource.getId();
            this.quantity = requirement.quantity;
            this.allocatedQuantity = allocatedQuantity;
            this.shortageQuantity = Math.max(0, requirement.quantity - allocatedQuantity);
        }
    }

    public static class CreateAllocation {
        @NotNull @Positive public Long requirementId;
        @NotNull @Min(1) @Max(1000000) public Integer quantity;
        @NotBlank @Size(max = 100) public String borrowerName;
        @Pattern(regexp = "^\\+[1-9][0-9]{7,14}$") public String phoneNumber;
        public Boolean smsConsent;
    }

    public static class AllocationView {
        public Long id;
        public Long requirementId;
        public Integer quantity;
        public String borrowerName;
        public Boolean contactPresent;
        public Boolean smsConsent;
        public AllocationStatus status;
        public LocalDateTime createdAt;
        public LocalDateTime approvedAt;
        public LocalDateTime issuedAt;
        public LocalDateTime returnedAt;
        public LocalDateTime consumedAt;
        public LocalDateTime cancelledAt;
        public String cancellationReason;
        public Boolean expiredPendingCleanup;
        public Boolean reminderAttempted;
        public String reminderOutcome;
        public AllocationView() { }
        public AllocationView(Allocation allocation, boolean expiredPendingCleanup) {
            this.id = allocation.id;
            this.requirementId = allocation.requirement.getId();
            this.quantity = allocation.quantity;
            this.borrowerName = allocation.borrowerName;
            this.contactPresent = allocation.phoneNumber != null;
            this.smsConsent = allocation.smsConsent;
            this.status = allocation.status;
            this.createdAt = allocation.createdAt;
            this.approvedAt = allocation.approvedAt;
            this.issuedAt = allocation.issuedAt;
            this.returnedAt = allocation.returnedAt;
            this.consumedAt = allocation.consumedAt;
            this.cancelledAt = allocation.cancelledAt;
            this.cancellationReason = allocation.cancellationReason == null ? null : allocation.cancellationReason.name();
            this.expiredPendingCleanup = expiredPendingCleanup;
            this.reminderAttempted = allocation.reminderClaimedAt != null;
            this.reminderOutcome = allocation.reminderOutcome.name();
        }
    }

    public static class MessageView {
        public String message;
        public MessageView() { }
        public MessageView(String message) { this.message = message; }
    }

    public static class ErrorView {
        public String code;
        public String message;
        public ErrorView() { }
        public ErrorView(String code, String message) { this.code = code; this.message = message; }
    }
}
