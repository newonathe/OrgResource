package app.entities;

import javax.persistence.Column;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "resource",
        uniqueConstraints = @UniqueConstraint(name = "uk_resource_name_key", columnNames = "name_key"),
        indexes = @Index(name = "ix_resource_name_key", columnList = "name_key"))
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    public String name;

    @NotNull
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

    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "image_bytes", columnDefinition = "MEDIUMBLOB")
    private byte[] imageBytes;

    @Column(name = "image_content_type", length = 16)
    public String imageContentType;

    protected Resource() { }

    public Long getId() { return id; }
    public ResourceType getType() { return type; }
    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }
    public byte[] getImageBytes() { return imageBytes; }
    public void setImageBytes(byte[] imageBytes) { this.imageBytes = imageBytes; }

    public Resource(String name, String nameKey, ResourceType type, Integer totalQuantity) {
        this.name = name;
        this.nameKey = nameKey;
        this.type = type;
        this.totalQuantity = totalQuantity;
    }
}
