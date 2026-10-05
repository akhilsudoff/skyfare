package com.skyfare.watch;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WatchRepository extends JpaRepository<Watch, UUID> {
    List<Watch> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Watch> findByIdAndUserId(UUID id, UUID userId);
}
