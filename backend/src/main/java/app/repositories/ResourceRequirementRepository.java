package app.repositories;

import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import app.entities.ResourceRequirement;

@Repository
public interface ResourceRequirementRepository extends JpaRepository<ResourceRequirement, Long> {
    List<ResourceRequirement> findByProject_Id(Long projectId);
    boolean existsByProject_Id(Long projectId);
    boolean existsByResource_Id(Long resourceId);
    boolean existsByProject_IdAndResource_Id(Long projectId, Long resourceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM ResourceRequirement q WHERE q.id = :id")
    Optional<ResourceRequirement> lockById(@Param("id") Long id);
}
