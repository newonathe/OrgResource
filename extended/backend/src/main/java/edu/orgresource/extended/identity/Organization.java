package edu.orgresource.extended.identity;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "organization")
public class Organization {
    public enum Status { ACTIVE, FROZEN }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "public_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    public UUID publicId;
    @Column(nullable = false, unique = true, length = 64)
    public String slug;
    @Column(name = "display_name", nullable = false, length = 120)
    public String displayName;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    public Status status = Status.ACTIVE;
    @Version @Column(nullable = false)
    public long version;
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @Column(name = "demo_dataset_id", length = 96)
    public String demoDatasetId;
    @Column(name = "demo_reference_key", length = 128)
    public String demoReferenceKey;
    @Column(name = "demo_source_sha", columnDefinition = "CHAR(64)")
    public String demoSourceSha;
    @Column(name = "demo_plan_sha", columnDefinition = "CHAR(64)")
    public String demoPlanSha;
    @Column(name = "demo_as_of")
    public LocalDate demoAsOf;

    protected Organization() { }
    public Organization(String slug, String displayName, LocalDateTime now) {
        this.publicId = UUID.randomUUID(); this.slug = slug; this.displayName = displayName;
        this.status = Status.ACTIVE; this.createdAt = now; this.updatedAt = now;
    }
}
