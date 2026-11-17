package edu.orgresource.c0.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
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

    protected Project() { }

    public Project(Long organizationId, Department department, String name,
                   LocalDateTime startAt, LocalDateTime endAt) {
        this.organizationId = organizationId;
        this.department = department;
        this.name = name;
        this.startAt = startAt;
        this.endAt = endAt;
    }
}
