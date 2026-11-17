package edu.orgresource.extended.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "resource", uniqueConstraints = {
        @UniqueConstraint(name = "uk_resource_org_name", columnNames = {"organization_id", "name_key"})
})
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    public UUID publicId = UUID.randomUUID();

    @Column(name = "organization_id", nullable = false)
    public Long organizationId;

    @Column(name = "name", nullable = false, length = 100)
    public String name;

    @Column(name = "name_key", nullable = false, length = 100)
    public String nameKey;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    public ResourceType type;

    @NotNull
    @Min(0)
    @Column(name = "total_quantity", nullable = false)
    public Integer totalQuantity;

    @Version @Column(nullable = false)
    public long version;

    protected Resource() { }

    public Resource(Long organizationId, String name, String nameKey, ResourceType type, Integer totalQuantity) {
        this.organizationId = organizationId;
        this.name = name;
        this.nameKey = nameKey;
        this.type = type;
        this.totalQuantity = totalQuantity;
    }
}
