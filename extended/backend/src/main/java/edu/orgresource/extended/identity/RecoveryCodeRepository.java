package edu.orgresource.extended.identity;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface RecoveryCodeRepository extends JpaRepository<AccountRecoveryCode, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from AccountRecoveryCode r where r.userId = :userId and r.codeHash = :hash")
    Optional<AccountRecoveryCode> lockByUserAndHash(@Param("userId") Long userId, @Param("hash") byte[] hash);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from AccountRecoveryCode r where r.userId = :userId and r.usedAt is null and r.revokedAt is null")
    List<AccountRecoveryCode> lockUnusedByUser(@Param("userId") Long userId);
    long deleteByUserId(Long userId);
}
