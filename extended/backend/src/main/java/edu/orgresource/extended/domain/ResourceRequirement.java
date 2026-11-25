package edu.orgresource.extended.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "resource_requirement", uniqueConstraints = {
    @UniqueConstraint(name = "uk_requirement_project_resource", columnNames = {"organization_id", "project_id", "resource_id"})
})
public class ResourceRequirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true, columnDefinition = "BINARY(16)", updatable = false)
    public UUID publicId = UUID.randomUUID();

    @Column(name = "organization_id", nullable = false)
    public Long organizationId;

    @Column(name = "project_id", nullable = false)
    public Long projectId;

    @Column(name = "resource_id", nullable = false)
    public Long resourceId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "project_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false),
            @JoinColumn(name = "organization_id", referencedColumnName = "organization_id",
                    insertable = false, updatable = false, nullable = false)
    })
    public Project project;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "resource_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false),
            @JoinColumn(name = "organization_id", referencedColumnName = "organization_id",
                    insertable = false, updatable = false, nullable = false)
    })
    public Resource resource;

    @NotNull
    @Min(1)
    @Column(name = "quantity", nullable = false)
    public Integer quantity;

    @Version @Column(nullable = false)
    public long version;

    protected ResourceRequirement() { }

    public ResourceRequirement(Long organizationId, Project project, Resource resource, Integer quantity) {
        this.organizationId = organizationId;
        this.project = project;
        this.resource = resource;
        this.projectId = project.id;
        this.resourceId = resource.id;
        this.quantity = quantity;
    }
}
