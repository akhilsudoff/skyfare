package com.skyfare.watch;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "watch")
public class Watch {
    @Id @GeneratedValue
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(nullable = false, length = 3)
    private String origin;
    @Column(nullable = false, length = 3)
    private String destination;
    @Column(name = "depart_date", nullable = false)
    private LocalDate departDate;
    @Column(name = "target_price", nullable = false)
    private BigDecimal targetPrice;
    @Column(name = "last_price")
    private BigDecimal lastPrice;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Watch() { }
    public Watch(UUID userId, String origin, String destination, LocalDate departDate, BigDecimal targetPrice) {
        this.userId = userId; this.origin = origin; this.destination = destination;
        this.departDate = departDate; this.targetPrice = targetPrice;
    }
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDate getDepartDate() { return departDate; }
    public BigDecimal getTargetPrice() { return targetPrice; }
    public BigDecimal getLastPrice() { return lastPrice; }
    public Instant getCreatedAt() { return createdAt; }
    public void setLastPrice(BigDecimal lastPrice) { this.lastPrice = lastPrice; }
}
