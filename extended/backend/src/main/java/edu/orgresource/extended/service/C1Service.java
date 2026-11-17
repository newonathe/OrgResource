package edu.orgresource.extended.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.orgresource.extended.api.ApiDtos;
import edu.orgresource.extended.api.C1ApiException;
import edu.orgresource.extended.domain.Allocation;
import edu.orgresource.extended.domain.AllocationStatus;
import edu.orgresource.extended.domain.Department;
import edu.orgresource.extended.domain.Project;
import edu.orgresource.extended.domain.Resource;
import edu.orgresource.extended.domain.ResourceRequirement;
import edu.orgresource.extended.repository.AllocationRepository;
import edu.orgresource.extended.repository.DepartmentRepository;
import edu.orgresource.extended.repository.ProjectRepository;
import edu.orgresource.extended.repository.ResourceRepository;
import edu.orgresource.extended.repository.ResourceRequirementRepository;
import edu.orgresource.extended.security.C1OrganizationContext;

@Service
public class C1Service {
    private final DepartmentRepository departments;
    private final ProjectRepository projects;
    private final ResourceRepository resources;
    private final ResourceRequirementRepository requirements;
    private final AllocationRepository allocations;
    private final C1OrganizationContext organizationContext;
    private final Clock philippineClock;
    private final InventoryService inventory;

    public C1Service(DepartmentRepository departments,
                     ProjectRepository projects,
                     ResourceRepository resources,
                     ResourceRequirementRepository requirements,
                     AllocationRepository allocations,
                     C1OrganizationContext organizationContext,
                     Clock philippineClock,
                     InventoryService inventory) {
        this.departments = departments;
        this.projects = projects;
        this.resources = resources;
        this.requirements = requirements;
        this.allocations = allocations;
        this.organizationContext = organizationContext;
        this.philippineClock = philippineClock;
        this.inventory = inventory;
    }

    @Transactional
    public ApiDtos.DepartmentView createDepartment(ApiDtos.CreateDepartment request) {
        Long orgId = organizationContext.requireMutationOrganizationId();
        organizationContext.requireCurrentRole(orgId, edu.orgresource.extended.identity.Membership.Role.OWNER,
                edu.orgresource.extended.identity.Membership.Role.ADMIN);
        String name = displayName(request.name);
        Department saved = departments.saveAndFlush(new Department(orgId, name, nameKey(name)));
        return new ApiDtos.DepartmentView(saved);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.DepartmentView> listDepartments() {
        return departments.findByOrganizationIdOrderById(organizationContext.currentOrganizationId())
                .stream().map(ApiDtos.DepartmentView::new).collect(Collectors.toList());
    }

    @Transactional
    public ApiDtos.ProjectView createProject(ApiDtos.CreateProject request) {
        Long orgId = organizationContext.requireMutationOrganizationId();
        organizationContext.requireCurrentRole(orgId, edu.orgresource.extended.identity.Membership.Role.OWNER,
                edu.orgresource.extended.identity.Membership.Role.ADMIN);
        Department department = departments.findByIdAndOrganizationId(request.departmentId, orgId)
                .orElseThrow(() -> notFound("Department"));
        LocalDateTime startAt = parseDateTime(request.startAt);
        LocalDateTime endAt = parseDateTime(request.endAt);
        if (!endAt.isAfter(startAt)) {
            throw new C1ApiException(400, "VALIDATION_FAILED", "endAt must be later than startAt.");
        }
        if (!startAt.isAfter(LocalDateTime.now(philippineClock))) {
            throw new C1ApiException(400, "VALIDATION_FAILED", "startAt must be in the future.");
        }
        Project saved = projects.saveAndFlush(new Project(orgId, department, displayName(request.name),
                startAt, endAt));
        return new ApiDtos.ProjectView(saved);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.ProjectView> listProjects(Long departmentId) {
        Long orgId = organizationContext.currentOrganizationId();
        if (departmentId == null) return List.of();
        if (departments.findByIdAndOrganizationId(departmentId, orgId).isEmpty()) throw notFound("Department");
        return projects.findByOrganizationIdAndDepartmentIdOrderById(orgId, departmentId)
                .stream().map(ApiDtos.ProjectView::new).collect(Collectors.toList());
    }

    public ApiDtos.ResourceView createResource(ApiDtos.CreateResource request) {
        return inventory.createLegacyResource(request);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.ResourceView> listResources() {
        Long orgId = organizationContext.currentOrganizationId();
        return resources.findByOrganizationIdOrderById(orgId).stream()
                .map(resource -> new ApiDtos.ResourceView(resource,
                        inventory.availableQuantity(resource, LocalDateTime.now(philippineClock))))
                .collect(Collectors.toList());
    }

    @Transactional
    public ApiDtos.RequirementView createRequirement(ApiDtos.CreateRequirement request) {
        Long orgId = organizationContext.requireMutationOrganizationId();
        organizationContext.requireCurrentRole(orgId, edu.orgresource.extended.identity.Membership.Role.OWNER,
                edu.orgresource.extended.identity.Membership.Role.ADMIN);
        Project project = projects.findByIdAndOrganizationId(request.projectId, orgId)
                .orElseThrow(() -> notFound("Project"));
        Resource resource = resources.findByIdAndOrganizationId(request.resourceId, orgId)
                .orElseThrow(() -> notFound("Resource"));
        ResourceRequirement saved = requirements.saveAndFlush(
                new ResourceRequirement(orgId, project, resource, request.quantity));
        return requirementView(saved, LocalDateTime.now(philippineClock));
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.RequirementView> listRequirements(Long projectId) {
        Long orgId = organizationContext.currentOrganizationId();
        if (projects.findByIdAndOrganizationId(projectId, orgId).isEmpty()) throw notFound("Project");
        LocalDateTime now = LocalDateTime.now(philippineClock);
        return requirements.findByOrganizationIdAndProjectIdOrderById(orgId, projectId).stream()
                .map(requirement -> requirementView(requirement, now)).collect(Collectors.toList());
    }

    @Transactional
    public ApiDtos.AllocationView createAllocation(ApiDtos.CreateAllocation request) {
        Long orgId = organizationContext.requireMutationOrganizationId();
        organizationContext.requireCurrentRole(orgId, edu.orgresource.extended.identity.Membership.Role.OWNER,
                edu.orgresource.extended.identity.Membership.Role.ADMIN);
        ResourceRequirement requirement = requirements.findDetailedByIdAndOrganizationId(request.requirementId, orgId)
                .orElseThrow(() -> notFound("Requirement"));
        LocalDateTime now = LocalDateTime.now(philippineClock);
        if (!requirement.project.endAt.isAfter(now)) {
            throw new C1ApiException(409, "PROJECT_ENDED", "An ended Project cannot receive an Allocation.");
        }
        if (request.quantity > requirement.quantity) {
            throw new C1ApiException(400, "VALIDATION_FAILED", "quantity cannot exceed the Requirement quantity.");
        }
        boolean consent = Boolean.TRUE.equals(request.smsConsent) && request.phoneNumber != null;
        Allocation saved = allocations.saveAndFlush(new Allocation(orgId, requirement, request.quantity,
                displayName(request.borrowerName), consent ? request.phoneNumber : null, consent, now));
        return new ApiDtos.AllocationView(saved);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.AllocationView> listAllocations(Long requirementId) {
        Long orgId = organizationContext.currentOrganizationId();
        if (requirements.findByIdAndOrganizationId(requirementId, orgId).isEmpty()) throw notFound("Requirement");
        return allocations.findByOrganizationIdAndRequirementIdOrderById(orgId, requirementId).stream()
                .map(ApiDtos.AllocationView::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApiDtos.DepartmentView readDepartmentV2(Long departmentId) {
        Department department = departments.findByIdAndOrganizationId(
                departmentId, organizationContext.currentOrganizationId()).orElseThrow(() -> notFound("Department"));
        return new ApiDtos.DepartmentView(department);
    }

    @Transactional
    public ApiDtos.DepartmentView createDepartmentV2(ApiDtos.CreateDepartment request) {
        return createDepartment(request);
    }

    @Transactional(readOnly = true)
    public int availableQuantity(Long resourceId, Long organizationId, LocalDateTime sampledNow) {
        Resource resource = resources.findByIdAndOrganizationId(resourceId, organizationId)
                .orElseThrow(() -> notFound("Resource"));
        return inventory.availableQuantity(resource, sampledNow);
    }

    public ApiDtos.AllocationView approveAllocation(Long allocationId, Long organizationId) {
        return inventory.legacyTransition(allocationId, organizationId, InventoryService.InventoryAction.APPROVE);
    }

    public ApiDtos.AllocationView issueAllocation(Long allocationId, Long organizationId) {
        return inventory.legacyTransition(allocationId, organizationId, InventoryService.InventoryAction.ISSUE);
    }

    public ApiDtos.AllocationView returnAllocation(Long allocationId, Long organizationId) {
        return inventory.legacyTransition(allocationId, organizationId, InventoryService.InventoryAction.RETURN);
    }

    public ApiDtos.AllocationView consumeAllocation(Long allocationId, Long organizationId) {
        return inventory.legacyTransition(allocationId, organizationId, InventoryService.InventoryAction.CONSUME);
    }

    public ApiDtos.AllocationView cancelAllocation(Long allocationId, Long organizationId) {
        return inventory.legacyTransition(allocationId, organizationId, InventoryService.InventoryAction.CANCEL);
    }

    @Transactional
    public int cleanupExpiredPending(Long organizationId, LocalDateTime sampledNow) {
        List<Allocation> expired = allocations.findExpiredPending(
                organizationId, AllocationStatus.PENDING, sampledNow);
        expired.forEach(allocation -> {
            allocation.status = AllocationStatus.CANCELLED;
            allocation.cancelledAt = sampledNow;
        });
        allocations.saveAllAndFlush(expired);
        return expired.size();
    }

    private ApiDtos.RequirementView requirementView(ResourceRequirement requirement, LocalDateTime now) {
        Long allocated = requirements.sumCoveredQuantity(requirement.id, requirement.organizationId, now);
        return new ApiDtos.RequirementView(requirement, allocated == null ? 0 : Math.toIntExact(allocated));
    }

    private static String displayName(String value) {
        if (value == null) throw new C1ApiException(400, "VALIDATION_FAILED", "name is required.");
        String display = value.trim().replaceAll("\\s+", " ");
        if (display.isEmpty() || display.length() > 100) {
            throw new C1ApiException(400, "VALIDATION_FAILED", "name must contain 1 to 100 characters.");
        }
        return display;
    }

    private static String nameKey(String display) {
        return display.toLowerCase(Locale.ROOT);
    }

    private static LocalDateTime parseDateTime(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new C1ApiException(400, "VALIDATION_FAILED", "Date values must use PHT local YYYY-MM-DDTHH:mm:ss.");
        }
    }

    private static C1ApiException notFound(String entity) {
        return new C1ApiException(404, "NOT_FOUND", entity + " not found.");
    }

}
