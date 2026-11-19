package edu.orgresource.extended.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import edu.orgresource.extended.api.ApiDtos;
import edu.orgresource.extended.api.C1ApiException;
import edu.orgresource.extended.domain.Project;
import edu.orgresource.extended.domain.Resource;
import edu.orgresource.extended.domain.ResourceRequirement;
import edu.orgresource.extended.identity.Membership;
import edu.orgresource.extended.repository.ProjectRepository;
import edu.orgresource.extended.repository.ResourceRepository;
import edu.orgresource.extended.repository.ResourceRequirementRepository;
import edu.orgresource.extended.security.C1OrganizationContext;

@Component
public class ProjectWorkComponent {
    private final InventoryService inventory;
    private final C1Service domain;
    private final C1OrganizationContext context;
    private final ProjectRepository projects;
    private final ResourceRepository resources;
    private final ResourceRequirementRepository requirements;
    private final Clock clock;

    public ProjectWorkComponent(InventoryService inventory, C1Service domain, C1OrganizationContext context,
            ProjectRepository projects, ResourceRepository resources, ResourceRequirementRepository requirements, Clock clock) {
        this.inventory = inventory;
        this.domain = domain;
        this.context = context;
        this.projects = projects;
        this.resources = resources;
        this.requirements = requirements;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ApiDtos.WorkspacePage<InventoryService.ProjectAccessView> projectPage(UUID organization, int limit, String cursor) {
        Membership actor = readScope(organization);
        Long lead = leadScope(actor);
        var window = WorkspacePageWindow.of(limit, cursor, actor.organizationId, "project-work", String.valueOf(lead));
        var rows = projects.findProjectWorkPage(actor.organizationId, lead, window.after(), window.queryLimit());
        return window.result(rows, ProjectRepository.ProjectWorkRow::getId, row -> new InventoryService.ProjectAccessView(
                row.getPublicId(), row.getName(), row.getStartAt().toString(), row.getEndAt().toString(),
                row.getLeadMembershipId(), row.getVersion()));
    }

    @Transactional(readOnly = true)
    public ApiDtos.WorkspacePage<RequirementView> requirementPage(UUID organization, UUID projectPublicId, int limit, String cursor) {
        Membership actor = readScope(organization);
        Long lead = leadScope(actor);
        if (projectPublicId != null) {
            Project project = projects.findByPublicIdAndOrganizationId(projectPublicId, actor.organizationId)
                    .orElseThrow(ProjectWorkComponent::notFound);
            if (lead != null && !lead.equals(project.leadMembershipId)) throw notFound();
        }
        var window = WorkspacePageWindow.of(limit, cursor, actor.organizationId, "project-requirements",
                lead + "|" + projectPublicId);
        var rows = requirements.findProjectWorkPage(actor.organizationId, lead, projectPublicId, window.after(), window.queryLimit());
        if (rows.isEmpty()) return new ApiDtos.WorkspacePage<>(List.of(), null);
        var covered = domain.requirementCoverage(actor.organizationId, window.visible(rows).stream()
                .map(ResourceRequirementRepository.ProjectWorkRow::getId).toList(), LocalDateTime.now(clock));
        return window.result(rows, ResourceRequirementRepository.ProjectWorkRow::getId,
                row -> view(row, covered.getOrDefault(row.getId(), 0)));
    }

    @Transactional(readOnly = true)
    public RequirementView requirement(UUID organization, UUID requirementPublicId) {
        Membership actor = readScope(organization);
        return readRequirement(actor, requirementPublicId, LocalDateTime.now(clock));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InventoryService.ProjectAccessView updateProject(UUID organization, UUID id, String ifMatch, ApiDtos.ProjectUpdate body) {
        Membership actor = writeScope(organization);
        Project project = lockProject(actor, id);
        InventoryService.requireVersion(ifMatch, project.version);
        return inventory.projectAccessView(domain.saveProjectFields(project, body));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiDtos.MessageView deleteProject(UUID organization, UUID id, String ifMatch) {
        Membership actor = writeScope(organization);
        Project project = lockProject(actor, id);
        InventoryService.requireVersion(ifMatch, project.version);
        return domain.removeProject(project);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public RequirementView createRequirement(UUID organization, UUID projectId, UUID resourceId, int quantity) {
        Membership actor = writeScope(organization);
        Project project = lockProject(actor, projectId);
        LocalDateTime now = LocalDateTime.now(clock);
        if (!project.endAt.isAfter(now))
            throw new C1ApiException(409, "PROJECT_ENDED", "An ended Project cannot receive a Requirement.");
        Resource resource = resources.lockByPublicIdAndOrganizationId(resourceId, actor.organizationId).orElseThrow(ProjectWorkComponent::notFound);
        var saved = domain.saveNewRequirement(project, resource, quantity);
        return readRequirement(actor, saved.publicId, now);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public RequirementView updateRequirement(UUID organization, UUID id, String ifMatch, int quantity) {
        Membership actor = writeScope(organization);
        var locked = lockRequirement(actor, id);
        InventoryService.requireVersion(ifMatch, locked.requirement.version);
        LocalDateTime now = LocalDateTime.now(clock);
        domain.saveRequirementQuantity(locked.project, locked.requirement, quantity, now);
        return readRequirement(actor, id, now);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiDtos.MessageView deleteRequirement(UUID organization, UUID id, String ifMatch) {
        Membership actor = writeScope(organization);
        ResourceRequirement requirement = lockRequirement(actor, id).requirement;
        InventoryService.requireVersion(ifMatch, requirement.version);
        return domain.removeRequirement(requirement);
    }

    private Project lockProject(Membership actor, UUID id) {
        Project project = projects.lockByPublicId(id, actor.organizationId).orElseThrow(ProjectWorkComponent::notFound);
        InventoryService.requireProjectWriter(actor, project);
        return project;
    }

    private LockedRequirement lockRequirement(Membership actor, UUID id) {
        long org = actor.organizationId;
        ResourceRequirement observed = requirements.findByPublicIdAndOrganizationId(id, org)
                .orElseThrow(ProjectWorkComponent::notFound);
        Project project = projects.lockById(observed.projectId, org).orElseThrow(ProjectWorkComponent::notFound);
        InventoryService.requireProjectWriter(actor, project);
        resources.lockById(observed.resourceId, org).orElseThrow(ProjectWorkComponent::notFound);
        ResourceRequirement requirement = requirements.lockById(observed.id, org).orElseThrow(ProjectWorkComponent::notFound);
        return new LockedRequirement(project, requirement);
    }

    private RequirementView readRequirement(Membership actor, UUID id, LocalDateTime now) {
        var row = requirements.findProjectWorkRow(actor.organizationId, leadScope(actor), id)
                .orElseThrow(ProjectWorkComponent::notFound);
        var coverage = domain.requirementCoverage(actor.organizationId, List.of(row.getId()), now);
        return view(row, coverage.getOrDefault(row.getId(), 0));
    }

    private Membership readScope(UUID org) {
        return context.requirePathOrganization(org, false, Membership.Role.OWNER, Membership.Role.ADMIN,
                Membership.Role.CUSTODIAN, Membership.Role.LEAD);
    }

    private Membership writeScope(UUID org) {
        Membership actor = inventory.lockPathOrganization(org, true).membership();
        InventoryService.requireProjectWriterRole(actor);
        return actor;
    }

    private static Long leadScope(Membership actor) {
        return actor.role == Membership.Role.LEAD ? actor.id : null;
    }

    private static RequirementView view(ResourceRequirementRepository.ProjectWorkRow row, int covered) {
        return new RequirementView(row.getPublicId(), row.getProjectPublicId(), row.getResourcePublicId(), row.getProjectName(),
                row.getResourceName(), row.getResourceType().name(), row.getQuantity(), covered,
                Math.max(0, row.getQuantity() - covered), row.getVersion());
    }

    private static C1ApiException notFound() {
        return new C1ApiException(404, "NOT_FOUND", "The requested Project work was not found.");
    }

    private record LockedRequirement(Project project, ResourceRequirement requirement) { }
    public record RequirementView(UUID id, UUID projectId, UUID resourceId, String projectName, String resourceName,
            String resourceType, int quantity, int allocatedQuantity, int shortageQuantity, long version) { }
}
