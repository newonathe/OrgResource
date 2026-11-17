package edu.orgresource.extended.identity;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface LocalCredentialRepository extends JpaRepository<LocalCredential, Long> {
    Optional<LocalCredential> findByUserId(Long userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from LocalCredential c where c.userId = :userId")
    Optional<LocalCredential> lockByUserId(@Param("userId") Long userId);
}
