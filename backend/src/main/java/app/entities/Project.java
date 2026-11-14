package app.entities;

import java.time.LocalDateTime;
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
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "project", indexes = @Index(name = "ix_project_department_id", columnList = "department_id"))
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_project_department"))
    public Department department;

    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    public String name;

    @NotNull
    @Column(name = "start_at", nullable = false)
    public LocalDateTime startAt;

    @NotNull
    @Column(name = "end_at", nullable = false)
    public LocalDateTime endAt;

    protected Project() { }

    public Long getId() { return id; }
    public Department getDepartment() { return department; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }

    public Project(Department department, String name, LocalDateTime startAt, LocalDateTime endAt) {
        this.department = department;
        this.name = name;
        this.startAt = startAt;
        this.endAt = endAt;
    }
}
