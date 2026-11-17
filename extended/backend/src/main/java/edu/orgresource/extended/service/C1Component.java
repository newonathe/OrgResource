package edu.orgresource.extended.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.orgresource.extended.api.ApiDtos;
import edu.orgresource.extended.domain.Allocation;

@Component
public class C1Component {
    @Autowired private C1Service service;

    public ApiDtos.DepartmentView createDepartment(ApiDtos.CreateDepartment request) { return service.createDepartment(request); }
    public List<ApiDtos.DepartmentView> listDepartments() { return service.listDepartments(); }
    public ApiDtos.ProjectView createProject(ApiDtos.CreateProject request) { return service.createProject(request); }
    public List<ApiDtos.ProjectView> listProjects(Long departmentId) { return service.listProjects(departmentId); }
    public ApiDtos.ResourceView createResource(ApiDtos.CreateResource request) { return service.createResource(request); }
    public List<ApiDtos.ResourceView> listResources() { return service.listResources(); }
    public ApiDtos.RequirementView createRequirement(ApiDtos.CreateRequirement request) { return service.createRequirement(request); }
    public List<ApiDtos.RequirementView> listRequirements(Long projectId) { return service.listRequirements(projectId); }
    public ApiDtos.AllocationView createAllocation(ApiDtos.CreateAllocation request) { return service.createAllocation(request); }
    public List<ApiDtos.AllocationView> listAllocations(Long requirementId) { return service.listAllocations(requirementId); }
    public ApiDtos.DepartmentView readDepartmentV2(Long id) { return service.readDepartmentV2(id); }
    public ApiDtos.DepartmentView createDepartmentV2(ApiDtos.CreateDepartment request) { return service.createDepartmentV2(request); }
    public Allocation approve(Long id, Long orgId) { return service.approveAllocation(id, orgId); }
    public Allocation issue(Long id, Long orgId) { return service.issueAllocation(id, orgId); }
    public Allocation consume(Long id, Long orgId) { return service.consumeAllocation(id, orgId); }
    public int cleanupExpiredPending(Long orgId, LocalDateTime now) { return service.cleanupExpiredPending(orgId, now); }
}
