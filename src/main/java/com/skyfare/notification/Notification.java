package com.skyfare.notification;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification")
public class Notification {
    @Id @GeneratedValue
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "watch_id", nullable = false)
    private UUID watchId;
    @Column(nullable = false)
    private String message;
    @Column(name = "old_price")
    private BigDecimal oldPrice;
    @Column(name = "new_price")
    private BigDecimal newPrice;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Notification() { }
    public Notification(UUID userId, UUID watchId, String message, BigDecimal oldPrice, BigDecimal newPrice) {
        this.userId = userId; this.watchId = watchId; this.message = message;
        this.oldPrice = oldPrice; this.newPrice = newPrice;
    }
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getWatchId() { return watchId; }
    public String getMessage() { return message; }
    public BigDecimal getOldPrice() { return oldPrice; }
    public BigDecimal getNewPrice() { return newPrice; }
    public Instant getCreatedAt() { return createdAt; }
}
