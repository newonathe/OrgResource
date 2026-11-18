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
import edu.orgresource.extended.domain.ResourceType;
import edu.orgresource.extended.domain.AllocationStatus;
import edu.orgresource.extended.repository.AllocationRepository;
import edu.orgresource.extended.repository.DepartmentRepository;
import edu.orgresource.extended.repository.ProjectRepository;
import edu.orgresource.extended.repository.ResourceRepository;
import edu.orgresource.extended.repository.ResourceRequirementRepository;
import edu.orgresource.extended.security.C1OrganizationContext;
import edu.orgresource.extended.security.ContactCipher;

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
    private final ContactCipher contactCipher;

    public C1Service(DepartmentRepository departments,
                     ProjectRepository projects,
                     ResourceRepository resources,
                     ResourceRequirementRepository requirements,
                     AllocationRepository allocations,
                     C1OrganizationContext organizationContext,
                     Clock philippineClock,
                     InventoryService inventory,
                     ContactCipher contactCipher) {
        this.departments = departments;
        this.projects = projects;
        this.resources = resources;
        this.requirements = requirements;
        this.allocations = allocations;
        this.organizationContext = organizationContext;
        this.philippineClock = philippineClock;
        this.inventory = inventory;
        this.contactCipher = contactCipher;
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

    @Transactional
    public ApiDtos.DepartmentView updateDepartment(Long id, ApiDtos.DepartmentUpdate request) {
        Long orgId = organizationContext.requireMutationOrganizationId();
        requireManager(orgId);
        requirePositiveId(id, "Department");
        Department department = departments.lockById(id, orgId).orElseThrow(() -> notFound("Department"));
        String name = displayName(request.name);
        String key = nameKey(name);
        if (departments.existsByOrganizationIdAndNameKeyAndIdNot(orgId, key, id))
            throw new C1ApiException(409, "DUPLICATE_NAME", "A Department with this name already exists.");
        department.name = name;
        department.nameKey = key;
        return new ApiDtos.DepartmentView(departments.saveAndFlush(department));
    }

    @Transactional
    public ApiDtos.MessageView deleteDepartment(Long id) {
        Long orgId = organizationContext.requireMutationOrganizationId();
        requireManager(orgId);
        requirePositiveId(id, "Department");
        Department department = departments.lockById(id, orgId).orElseThrow(() -> notFound("Department"));
        if (projects.existsByOrganizationIdAndDepartmentId(orgId, id))
            throw new C1ApiException(409, "DEPENDENCY_EXISTS", "Department still has Projects.");
        departments.delete(department);
        departments.flush();
        return new ApiDtos.MessageView("Record deleted.");
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

    @Transactional
    public ApiDtos.ProjectView updateProject(Long id, ApiDtos.ProjectUpdate request) {
        Long orgId = organizationContext.requireMutationOrganizationId();
        requireManager(orgId);
        requirePositiveId(id, "Project");
        Project project = projects.lockById(id, orgId).orElseThrow(() -> notFound("Project"));
        String name = displayName(request.name);
        LocalDateTime startAt = parseDateTime(request.startAt);
        LocalDateTime endAt = parseDateTime(request.endAt);
        if (!endAt.isAfter(startAt)) throw new C1ApiException(400, "INVALID_PROJECT_PERIOD", "endAt must be later than startAt.");
        boolean datesChanged = !project.startAt.equals(startAt) || !project.endAt.equals(endAt);
        if (datesChanged) {
            if (!startAt.isAfter(LocalDateTime.now(philippineClock)))
                throw new C1ApiException(400, "INVALID_PROJECT_PERIOD", "Project startAt must be in the future when dates change.");
            if (allocations.countHistoricallyApprovedByProject(orgId, id) > 0)
                throw new C1ApiException(409, "PROJECT_DATES_LOCKED", "Project dates cannot change after an Allocation has been approved.");
        }
        project.name = name;
        project.startAt = startAt;
        project.endAt = endAt;
        return new ApiDtos.ProjectView(projects.saveAndFlush(project));
    }

    @Transactional
    public ApiDtos.MessageView deleteProject(Long id) {
        Long orgId = organizationContext.requireMutationOrganizationId();
        requireManager(orgId);
        requirePositiveId(id, "Project");
        Project project = projects.lockById(id, orgId).orElseThrow(() -> notFound("Project"));
        if (requirements.existsByOrganizationIdAndProjectId(orgId, id))
            throw new C1ApiException(409, "DEPENDENCY_EXISTS", "Project still has ResourceRequirements.");
        projects.delete(project);
        projects.flush();
        return new ApiDtos.MessageView("Record deleted.");
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.ProjectView> listProjects(Long departmentId) {
        Long orgId = organizationContext.currentOrganizationId();
        if (departmentId == null) return projects.findByOrganizationIdOrderById(orgId)
                .stream().map(ApiDtos.ProjectView::new).collect(Collectors.toList());
        if (departmentId <= 0) throw new C1ApiException(400, "VALIDATION_FAILED", "departmentId must be positive.");
        if (departments.findByIdAndOrganizationId(departmentId, orgId).isEmpty()) throw notFound("Department");
        return projects.findByOrganizationIdAndDepartmentIdOrderById(orgId, departmentId)
                .stream().map(ApiDtos.ProjectView::new).collect(Collectors.toList());
    }

    public ApiDtos.ResourceView createResource(ApiDtos.CreateResource request) {
        return inventory.createLegacyResource(request);
    }

    public ApiDtos.ResourceView updateResource(Long id, ApiDtos.ResourceUpdate request) {
        return inventory.updateLegacyResource(id, request);
    }

    public ApiDtos.MessageView deleteResource(Long id) {
        return inventory.deleteLegacyResource(id);
    }

    @Transactional(readOnly = true)
    public ApiDtos.ResourceAvailabilityView resourceAvailability(Long id) {
        Long orgId = organizationContext.currentOrganizationId();
        requirePositiveId(id, "Resource");
        Resource resource = resources.findByIdAndOrganizationId(id, orgId).orElseThrow(() -> notFound("Resource"));
        return new ApiDtos.ResourceAvailabilityView(id, inventory.availableQuantity(resource, LocalDateTime.now(philippineClock)));
    }

    @Transactional
    public ApiDtos.ResourceImageUploadView storeResourceImage(Long id, byte[] bytes, String contentType) {
        Long orgId = organizationContext.requireMutationOrganizationId();
        organizationContext.requireCurrentRole(orgId, edu.orgresource.extended.identity.Membership.Role.OWNER,
                edu.orgresource.extended.identity.Membership.Role.CUSTODIAN);
        requirePositiveId(id, "Resource");
        Resource resource = resources.lockById(id, orgId).orElseThrow(() -> notFound("Resource"));
        String verifiedType = ResourceImageSupport.validate(bytes, contentType);
        resource.setImageBytes(bytes.clone());
        resource.imageContentType = verifiedType;
        resources.saveAndFlush(resource);
        return new ApiDtos.ResourceImageUploadView(id, verifiedType, bytes.length, ResourceImageSupport.sha256(bytes));
    }

    @Transactional(readOnly = true)
    public ResourceImageContent resourceImage(Long id) {
        Long orgId = organizationContext.currentOrganizationId();
        requirePositiveId(id, "Resource");
        Resource resource = resources.findByIdAndOrganizationId(id, orgId).orElseThrow(() -> notFound("Resource"));
        byte[] bytes = resource.getImageBytes();
        if (bytes == null || resource.imageContentType == null) throw notFound("Resource image");
        return new ResourceImageContent(bytes.clone(), resource.imageContentType);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.ResourceView> listResources(ResourceType type) {
        Long orgId = organizationContext.currentOrganizationId();
        LocalDateTime now = LocalDateTime.now(philippineClock);
        List<Resource> rows = type == null ? resources.findByOrganizationIdOrderById(orgId)
                : resources.findByOrganizationIdAndTypeOrderById(orgId, type);
        List<Object[]> balances = allocations.findInventoryListBalances(orgId, now);
        java.util.Map<Long, Object[]> byResource = balances.stream()
                .collect(java.util.stream.Collectors.toMap(row -> ((Number) row[0]).longValue(), row -> row));
        return rows.stream().map(resource -> {
            Object[] balance = byResource.get(resource.id);
            if (balance == null) throw new C1ApiException(500, "INVENTORY_INTEGRITY_FAILURE", "Inventory could not be reconciled safely.");
            long ledgerTotal = ((Number) balance[1]).longValue();
            long ledgerIssued = ((Number) balance[2]).longValue();
            long holds = ((Number) balance[3]).longValue();
            long issued = ((Number) balance[4]).longValue();
            if (ledgerTotal != resource.totalQuantity.longValue() || ledgerIssued != issued)
                throw new C1ApiException(500, "INVENTORY_INTEGRITY_FAILURE", "Inventory could not be reconciled safely.");
            long available = resource.totalQuantity.longValue() - holds
                    - (resource.type == ResourceType.REUSABLE ? issued : 0);
            if (available < 0 || available > resource.totalQuantity)
                throw new C1ApiException(500, "INVENTORY_INTEGRITY_FAILURE", "Inventory could not be reconciled safely.");
            return new ApiDtos.ResourceView(resource, Math.toIntExact(available));
        }).collect(Collectors.toList());
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

    @Transactional
    public ApiDtos.RequirementView updateRequirement(Long id, ApiDtos.RequirementUpdate request) {
        Long orgId = organizationContext.requireMutationOrganizationId();
        requireManager(orgId);
        requirePositiveId(id, "Requirement");
        if (request.quantity == null || request.quantity < 1 || request.quantity > 1000000)
            throw new C1ApiException(400, "VALIDATION_FAILED", "quantity must be from 1 to 1000000.");
        ResourceRequirement initial = requirements.findDetailedByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> notFound("Requirement"));
        Project project = projects.lockById(initial.projectId, orgId).orElseThrow(() -> notFound("Project"));
        resources.lockById(initial.resourceId, orgId).orElseThrow(() -> notFound("Resource"));
        ResourceRequirement requirement = requirements.lockById(id, orgId).orElseThrow(() -> notFound("Requirement"));
        LocalDateTime now = LocalDateTime.now(philippineClock);
        if (!project.endAt.isAfter(now)) throw new C1ApiException(409, "PROJECT_ENDED", "Requirements cannot be changed after the Project ends.");
        Long coverageValue = requirements.sumCoveredQuantity(id, orgId, now);
        long coverage = coverageValue == null ? 0 : coverageValue;
        Integer pending = requirements.maxPendingQuantity(id, orgId, AllocationStatus.PENDING, now);
        if (request.quantity.longValue() < Math.max(coverage, pending == null ? 0L : pending.longValue()))
            throw new C1ApiException(409, "QUANTITY_BELOW_COMMITMENTS", "quantity cannot be lower than current coverage or the largest pending Allocation.");
        requirement.quantity = request.quantity;
        return requirementView(requirements.saveAndFlush(requirement), now);
    }

    @Transactional
    public ApiDtos.MessageView deleteRequirement(Long id) {
        Long orgId = organizationContext.requireMutationOrganizationId();
        requireManager(orgId);
        requirePositiveId(id, "Requirement");
        ResourceRequirement initial = requirements.findDetailedByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> notFound("Requirement"));
        projects.lockById(initial.projectId, orgId).orElseThrow(() -> notFound("Project"));
        resources.lockById(initial.resourceId, orgId).orElseThrow(() -> notFound("Resource"));
        ResourceRequirement requirement = requirements.lockById(id, orgId).orElseThrow(() -> notFound("Requirement"));
        if (allocations.existsByOrganizationIdAndRequirementId(orgId, id))
            throw new C1ApiException(409, "DEPENDENCY_EXISTS", "Requirement still has Allocation history.");
        requirements.delete(requirement);
        requirements.flush();
        return new ApiDtos.MessageView("Record deleted.");
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.RequirementView> listRequirements(Long projectId) {
        Long orgId = organizationContext.currentOrganizationId();
        if (projectId != null && projectId <= 0) throw new C1ApiException(400, "VALIDATION_FAILED", "projectId must be positive.");
        if (projectId != null && projects.findByIdAndOrganizationId(projectId, orgId).isEmpty()) throw notFound("Project");
        LocalDateTime now = LocalDateTime.now(philippineClock);
        List<ResourceRequirement> rows = projectId == null ? requirements.findByOrganizationIdOrderById(orgId)
                : requirements.findByOrganizationIdAndProjectIdOrderById(orgId, projectId);
        return rows.stream()
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
        boolean consent = Boolean.TRUE.equals(request.smsConsent);
        if (consent && (request.phoneNumber == null || !request.phoneNumber.matches("^\\+[1-9][0-9]{7,14}$")))
            throw new C1ApiException(400, "VALIDATION_FAILED", "smsConsent requires a valid fictional E.164 destination.");
        Allocation allocation = new Allocation(orgId, requirement, request.quantity,
                displayName(request.borrowerName), consent, now);
        if (consent) {
            ContactCipher.EncryptedContact encrypted = contactCipher.encrypt(orgId, allocation.publicId, request.phoneNumber);
            allocation.phoneCiphertext = encrypted.ciphertext();
            allocation.phoneNonce = encrypted.nonce();
            allocation.phoneKeyId = encrypted.keyId();
        }
        Allocation saved = allocations.saveAndFlush(allocation);
        return new ApiDtos.AllocationView(saved);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.AllocationView> listAllocations(Long requirementId, AllocationStatus status) {
        Long orgId = organizationContext.currentOrganizationId();
        organizationContext.requireCurrentReadRole(orgId,
                edu.orgresource.extended.identity.Membership.Role.OWNER,
                edu.orgresource.extended.identity.Membership.Role.ADMIN,
                edu.orgresource.extended.identity.Membership.Role.CUSTODIAN);
        if (requirementId != null && requirementId <= 0) throw new C1ApiException(400, "VALIDATION_FAILED", "requirementId must be positive.");
        if (requirementId != null && requirements.findByIdAndOrganizationId(requirementId, orgId).isEmpty()) throw notFound("Requirement");
        List<Allocation> rows;
        if (requirementId != null && status != null)
            rows = allocations.findByOrganizationIdAndRequirementIdAndStatusOrderById(orgId, requirementId, status);
        else if (requirementId != null)
            rows = allocations.findByOrganizationIdAndRequirementIdOrderById(orgId, requirementId);
        else if (status != null)
            rows = allocations.findByOrganizationIdAndStatusOrderById(orgId, status);
        else rows = allocations.findByOrganizationIdOrderById(orgId);
        LocalDateTime now = LocalDateTime.now(philippineClock);
        return rows.stream()
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

    public ApiDtos.MessageView deleteAllocation(Long allocationId, Long organizationId) {
        return inventory.deleteLegacyAllocation(allocationId, organizationId);
    }

    public ApiDtos.AllocationView revokeSmsConsent(Long allocationId, Long organizationId) {
        return inventory.revokeLegacySmsConsent(allocationId, organizationId);
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

    public record ResourceImageContent(byte[] bytes, String contentType) { }

    private void requireManager(Long organizationId) {
        organizationContext.requireCurrentRole(organizationId,
                edu.orgresource.extended.identity.Membership.Role.OWNER,
                edu.orgresource.extended.identity.Membership.Role.ADMIN);
    }

    private static void requirePositiveId(Long id, String entity) {
        if (id == null || id <= 0) throw new C1ApiException(400, "VALIDATION_FAILED", entity + " ID must be positive.");
    }

}
