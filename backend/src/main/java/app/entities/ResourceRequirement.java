package app.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "resource_requirement",
        uniqueConstraints = @UniqueConstraint(name = "uk_requirement_project_resource",
                columnNames = {"project_id", "resource_id"}),
        indexes = {
                @Index(name = "ix_requirement_project_id", columnList = "project_id"),
                @Index(name = "ix_requirement_resource_id", columnList = "resource_id")
        })
public class ResourceRequirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_requirement_project"))
    public Project project;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_requirement_resource"))
    public Resource resource;

    @NotNull
    @Min(1)
    @Column(name = "quantity", nullable = false)
    public Integer quantity;

    protected ResourceRequirement() { }

    public Long getId() { return id; }
    public Project getProject() { return project; }
    public Resource getResource() { return resource; }
    public Integer getQuantity() { return quantity; }

    public ResourceRequirement(Project project, Resource resource, Integer quantity) {
        this.project = project;
        this.resource = resource;
        this.quantity = quantity;
    }
}
