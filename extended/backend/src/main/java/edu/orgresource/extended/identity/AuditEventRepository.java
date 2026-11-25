package edu.orgresource.extended.identity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findTop100ByOrganizationIdOrderByIdDesc(Long organizationId);
}
