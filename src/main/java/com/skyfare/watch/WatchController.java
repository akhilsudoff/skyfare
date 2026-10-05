package com.skyfare.watch;

import com.skyfare.config.CurrentUser;
import com.skyfare.fares.FareHistoryRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/watches")
public class WatchController {
    private final WatchRepository watches;
    private final FareHistoryRepository history;
    private final CurrentUser currentUser;

    public WatchController(WatchRepository watches, FareHistoryRepository history, CurrentUser currentUser) {
        this.watches = watches; this.history = history; this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<WatchDtos.WatchView> create(@Valid @RequestBody WatchDtos.CreateWatch body) {
        Watch saved = watches.save(new Watch(currentUser.id(), body.origin().toUpperCase(),
                body.destination().toUpperCase(), body.departDate(), body.targetPrice()));
        return ResponseEntity.status(HttpStatus.CREATED).body(WatchDtos.WatchView.from(saved));
    }

    @GetMapping
    public List<WatchDtos.WatchView> list() {
        return watches.findByUserIdOrderByCreatedAtDesc(currentUser.id()).stream()
                .map(WatchDtos.WatchView::from).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        watches.delete(owned(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    public List<WatchDtos.FarePoint> history(@PathVariable UUID id, @RequestParam(defaultValue = "50") int limit) {
        owned(id);
        return history.latest(id, Math.min(limit, 200)).stream()
                .map(i -> new WatchDtos.FarePoint(i.getObservedAt(), i.getPrice(), i.getCurrency(), i.getCarrier()))
                .toList();
    }

    private Watch owned(UUID id) {
        return watches.findByIdAndUserId(id, currentUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch not found"));
    }
}
