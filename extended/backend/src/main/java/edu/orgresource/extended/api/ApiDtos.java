package edu.orgresource.extended.api;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import edu.orgresource.extended.domain.Allocation;
import edu.orgresource.extended.domain.Department;
import edu.orgresource.extended.domain.Project;
import edu.orgresource.extended.domain.Resource;
import edu.orgresource.extended.domain.ResourceRequirement;
import edu.orgresource.extended.domain.ResourceType;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

public final class ApiDtos {
    private static final DateTimeFormatter LOCAL_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private ApiDtos() { }

    public static class CreateDepartment {
        @NotBlank @Size(max = 100) public String name;
        public CreateDepartment() { }
        public CreateDepartment(String name) { this.name = name; }
    }

    public static class DepartmentUpdate {
        @NotBlank @Size(max = 100) public String name;
    }

    public static class DepartmentView {
        public Long id;
        public String name;
        public DepartmentView() { }
        public DepartmentView(Department department) {
            this.id = department.id;
            this.name = department.name;
        }
    }

    public static class CreateProject {
        @NotNull @Positive public Long departmentId;
        @NotBlank @Size(max = 100) public String name;
        @NotBlank public String startAt;
        @NotBlank public String endAt;
    }

    public static class ProjectUpdate {
        @NotBlank @Size(max = 100) public String name;
        @NotBlank public String startAt;
        @NotBlank public String endAt;
    }

    public static class ProjectView {
        public Long id;
        public Long departmentId;
        public String name;
        public String startAt;
        public String endAt;
        public ProjectView() { }
        public ProjectView(Project project) {
            this.id = project.id;
            this.departmentId = project.department.id;
            this.name = project.name;
            this.startAt = project.startAt.format(LOCAL_DATE_TIME);
            this.endAt = project.endAt.format(LOCAL_DATE_TIME);
        }
    }

    public static class CreateResource {
        @NotBlank @Size(max = 100) public String name;
        @NotNull public ResourceType type;
        @NotNull @Min(0) @Max(1000000) public Integer totalQuantity;
    }

    public static class ResourceUpdate {
        @NotBlank @Size(max = 100) public String name;
        @NotNull @Min(0) @Max(1000000) public Integer totalQuantity;
        @NotNull @Min(0) @Max(1000000) public Integer expectedTotalQuantity;
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

    public static class ResourceImageUploadView {
        public Long resourceId;
        public String contentType;
        public Integer sizeBytes;
        public String sha256;
        public ResourceImageUploadView() { }
        public ResourceImageUploadView(Long resourceId, String contentType, int sizeBytes, String sha256) {
            this.resourceId = resourceId;
            this.contentType = contentType;
            this.sizeBytes = sizeBytes;
            this.sha256 = sha256;
        }
    }

    public static class CreateRequirement {
        @NotNull @Positive public Long projectId;
        @NotNull @Positive public Long resourceId;
        @NotNull @Min(1) @Max(1000000) public Integer quantity;
    }

    public static class RequirementUpdate {
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
            this.projectId = requirement.project.id;
            this.resourceId = requirement.resource.id;
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
        public String status;
        public String createdAt;
        public String approvedAt;
        public String issuedAt;
        public String cancelledAt;
        public AllocationView() { }
        public AllocationView(Allocation allocation) {
            this.id = allocation.id;
            this.requirementId = allocation.requirement.id;
            this.quantity = allocation.quantity;
            this.borrowerName = allocation.borrowerName;
            this.contactPresent = allocation.phoneCiphertext != null;
            this.smsConsent = allocation.smsConsent;
            this.status = allocation.status.name();
            this.createdAt = format(allocation.createdAt);
            this.approvedAt = format(allocation.approvedAt);
            this.issuedAt = format(allocation.issuedAt);
            this.cancelledAt = format(allocation.cancelledAt);
        }
    }

    private static String format(LocalDateTime value) {
        return value == null ? null : value.format(LOCAL_DATE_TIME);
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
        public ErrorView(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    public static class CsrfView {
        public String token;
        public CsrfView() { }
        public CsrfView(String token) { this.token = token; }
    }
}
