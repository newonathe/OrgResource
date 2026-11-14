package app.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import app.entities.ResourceRequirement;

@Repository
public interface ResourceRequirementRepository extends JpaRepository<ResourceRequirement, Long> {
    List<ResourceRequirement> findByProject_Id(Long projectId);
    boolean existsByProject_IdAndResource_Id(Long projectId, Long resourceId);
}
