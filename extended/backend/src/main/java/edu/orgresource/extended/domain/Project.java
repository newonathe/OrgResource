package edu.orgresource.extended.domain;

import java.time.LocalDateTime;

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
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "project")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "organization_id", nullable = false)
    public Long organizationId;

    @Column(name = "department_id", nullable = false)
    public Long departmentId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "department_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false),
            @JoinColumn(name = "organization_id", referencedColumnName = "organization_id",
                    insertable = false, updatable = false, nullable = false)
    })
    public Department department;

    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "name", nullable = false, length = 100)
    public String name;

    @NotNull
    @Column(name = "start_at", nullable = false)
    public LocalDateTime startAt;

    @NotNull
    @Column(name = "end_at", nullable = false)
    public LocalDateTime endAt;

    @Version @Column(nullable = false)
    public long version;

    protected Project() { }

    public Project(Long organizationId, Department department, String name,
                   LocalDateTime startAt, LocalDateTime endAt) {
        this.organizationId = organizationId;
        this.department = department;
        this.departmentId = department.id;
        this.name = name;
        this.startAt = startAt;
        this.endAt = endAt;
    }
}
