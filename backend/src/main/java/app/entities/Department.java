package app.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "department",
        uniqueConstraints = @UniqueConstraint(name = "uk_department_name_key", columnNames = "name_key"),
        indexes = @Index(name = "ix_department_name_key", columnList = "name_key"))
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "name", nullable = false, length = 100)
    public String name;

    @Column(name = "name_key", nullable = false, length = 100)
    public String nameKey;

    protected Department() { }

    public Long getId() { return id; }

    public Department(String name, String nameKey) {
        this.name = name;
        this.nameKey = nameKey;
    }
}
