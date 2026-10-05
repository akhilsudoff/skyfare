package com.skyfare.notification;

import com.skyfare.config.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    public record NotificationView(UUID id, UUID watchId, String message,
                                   BigDecimal oldPrice, BigDecimal newPrice, Instant createdAt) { }

    private final NotificationRepository notifications;
    private final CurrentUser currentUser;

    public NotificationController(NotificationRepository notifications, CurrentUser currentUser) {
        this.notifications = notifications; this.currentUser = currentUser;
    }

    @GetMapping
    public List<NotificationView> list() {
        return notifications.findTop100ByUserIdOrderByCreatedAtDesc(currentUser.id()).stream()
                .map(n -> new NotificationView(n.getId(), n.getWatchId(), n.getMessage(),
                        n.getOldPrice(), n.getNewPrice(), n.getCreatedAt()))
                .toList();
    }
}
