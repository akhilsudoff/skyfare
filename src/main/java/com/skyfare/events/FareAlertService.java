package com.skyfare.events;

import com.skyfare.notification.Notification;
import com.skyfare.notification.NotificationRepository;
import com.skyfare.watch.Watch;
import com.skyfare.watch.WatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class FareAlertService {
    private static final Logger log = LoggerFactory.getLogger(FareAlertService.class);
    private final WatchRepository watches;
    private final NotificationRepository notifications;

    public FareAlertService(WatchRepository watches, NotificationRepository notifications) {
        this.watches = watches; this.notifications = notifications;
    }

    @Transactional
    public void handle(FareChangedEvent event) {
        Watch watch = watches.findById(UUID.fromString(event.watchId())).orElse(null);
        if (watch == null) { log.debug("Watch {} no longer exists; dropping event", event.watchId()); return; }
        if (!shouldAlert(watch.getTargetPrice(), event.oldPrice(), event.newPrice())) return;
        String message = "%s\u2192%s on %s is now $%s (target $%s)".formatted(
                event.origin(), event.destination(), event.departDate(),
                event.newPrice().toPlainString(), watch.getTargetPrice().toPlainString());
        notifications.save(new Notification(watch.getUserId(), watch.getId(), message,
                event.oldPrice(), event.newPrice()));
        log.info("Alert created for watch {}: {}", watch.getId(), message);
    }

    static boolean shouldAlert(BigDecimal target, BigDecimal oldPrice, BigDecimal newPrice) {
        boolean nowAtOrBelow = newPrice.compareTo(target) <= 0;
        boolean wasAbove = oldPrice == null || oldPrice.compareTo(target) > 0;
        return nowAtOrBelow && wasAbove;
    }
}
