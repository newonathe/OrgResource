package app.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import app.entities.Resource;
import app.entities.ResourceType;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    boolean existsByNameKey(String nameKey);
    List<Resource> findByType(ResourceType type);
}
