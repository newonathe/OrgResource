package app.repositories;

import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import app.entities.Resource;
import app.entities.ResourceType;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    boolean existsByNameKey(String nameKey);
    boolean existsByNameKeyAndIdNot(String nameKey, Long id);
    List<Resource> findByType(ResourceType type);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Resource r WHERE r.id = :id")
    Optional<Resource> lockById(@Param("id") Long id);
}
