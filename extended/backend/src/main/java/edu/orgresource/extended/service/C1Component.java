package edu.orgresource.extended.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.orgresource.extended.api.ApiDtos;
import edu.orgresource.extended.domain.AllocationStatus;
import edu.orgresource.extended.domain.ResourceType;

@Component
public class C1Component {
    @Autowired private C1Service service;

    public ApiDtos.WorkspaceOverview workspaceOverview() { return service.workspaceOverview(); }

    public ApiDtos.WorkspacePage<ApiDtos.DepartmentView> departmentPage(String search, int limit, String cursor) {
        return service.departmentPage(search, limit, cursor);
    }
    public ApiDtos.WorkspacePage<ApiDtos.ProjectView> projectPage(Long departmentId, String search, String period, int limit, String cursor) {
        return service.projectPage(departmentId, search, period, limit, cursor);
    }
    public ApiDtos.WorkspacePage<ApiDtos.ResourceView> resourcePage(ResourceType type, String search, int limit, String cursor) {
        return service.resourcePage(type, search, limit, cursor);
    }
    public ApiDtos.WorkspacePage<ApiDtos.RequirementView> requirementPage(Long projectId, String search, int limit, String cursor) {
        return service.requirementPage(projectId, search, limit, cursor);
    }
    public ApiDtos.WorkspacePage<ApiDtos.AllocationView> allocationPage(Long requirementId, Long projectId, AllocationStatus status,
            int limit, String cursor) {
        return service.allocationPage(requirementId, projectId, status, limit, cursor);
    }

    public ApiDtos.DepartmentView createDepartment(ApiDtos.CreateDepartment request) { return service.createDepartment(request); }
    public List<ApiDtos.DepartmentView> listDepartments() { return service.listDepartments(); }
    public ApiDtos.DepartmentView updateDepartment(Long id, ApiDtos.DepartmentUpdate request) { return service.updateDepartment(id, request); }
    public ApiDtos.MessageView deleteDepartment(Long id) { return service.deleteDepartment(id); }
    public ApiDtos.ProjectView createProject(ApiDtos.CreateProject request) { return service.createProject(request); }
    public List<ApiDtos.ProjectView> listProjects(Long departmentId) { return service.listProjects(departmentId); }
    public ApiDtos.ProjectView updateProject(Long id, ApiDtos.ProjectUpdate request) { return service.updateProject(id, request); }
    public ApiDtos.MessageView deleteProject(Long id) { return service.deleteProject(id); }
    public ApiDtos.ResourceView createResource(ApiDtos.CreateResource request) { return service.createResource(request); }
    public List<ApiDtos.ResourceView> listResources(edu.orgresource.extended.domain.ResourceType type) { return service.listResources(type); }
    public ApiDtos.ResourceView updateResource(Long id, ApiDtos.ResourceUpdate request) { return service.updateResource(id, request); }
    public ApiDtos.MessageView deleteResource(Long id) { return service.deleteResource(id); }
    public ApiDtos.ResourceAvailabilityView resourceAvailability(Long id) { return service.resourceAvailability(id); }
    public ApiDtos.ResourceImageUploadView storeResourceImage(Long id, byte[] bytes, String contentType) { return service.storeResourceImage(id, bytes, contentType); }
    public C1Service.ResourceImageContent resourceImage(Long id) { return service.resourceImage(id); }
    public ApiDtos.RequirementView createRequirement(ApiDtos.CreateRequirement request) { return service.createRequirement(request); }
    public List<ApiDtos.RequirementView> listRequirements(Long projectId) { return service.listRequirements(projectId); }
    public ApiDtos.RequirementView updateRequirement(Long id, ApiDtos.RequirementUpdate request) { return service.updateRequirement(id, request); }
    public ApiDtos.MessageView deleteRequirement(Long id) { return service.deleteRequirement(id); }
    public ApiDtos.AllocationView createAllocation(ApiDtos.CreateAllocation request) { return service.createAllocation(request); }
    public List<ApiDtos.AllocationView> listAllocations(Long requirementId, edu.orgresource.extended.domain.AllocationStatus status) { return service.listAllocations(requirementId, status); }
    public ApiDtos.MessageView deleteAllocation(Long id, Long orgId) { return service.deleteAllocation(id, orgId); }
    public ApiDtos.AllocationView revokeSmsConsent(Long id, Long orgId) { return service.revokeSmsConsent(id, orgId); }
    public ApiDtos.DepartmentView readDepartmentV2(Long id) { return service.readDepartmentV2(id); }
    public ApiDtos.DepartmentView createDepartmentV2(ApiDtos.CreateDepartment request) { return service.createDepartmentV2(request); }
    public ApiDtos.AllocationView approve(Long id, Long orgId) { return service.approveAllocation(id, orgId); }
    public ApiDtos.AllocationView issue(Long id, Long orgId) { return service.issueAllocation(id, orgId); }
    public ApiDtos.AllocationView returnAllocation(Long id, Long orgId) { return service.returnAllocation(id, orgId); }
    public ApiDtos.AllocationView consume(Long id, Long orgId) { return service.consumeAllocation(id, orgId); }
    public ApiDtos.AllocationView cancel(Long id, Long orgId) { return service.cancelAllocation(id, orgId); }
    public int cleanupExpiredPending(Long orgId, LocalDateTime now) { return service.cleanupExpiredPending(orgId, now); }
}
