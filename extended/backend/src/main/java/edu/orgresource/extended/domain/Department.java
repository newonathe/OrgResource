package edu.orgresource.extended.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "department", uniqueConstraints = {
        @UniqueConstraint(name = "uk_department_org_name", columnNames = {"organization_id", "name_key"})
})
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "organization_id", nullable = false)
    public Long organizationId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    public String name;

    @Column(name = "name_key", nullable = false, length = 100)
    public String nameKey;

    @Version @Column(nullable = false)
    public long version;

    protected Department() { }

    public Department(Long organizationId, String name, String nameKey) {
        this.organizationId = organizationId;
        this.name = name;
        this.nameKey = nameKey;
    }
}
