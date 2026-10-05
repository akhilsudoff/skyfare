package com.skyfare.events;

import com.skyfare.fares.FareHistoryItem;
import com.skyfare.fares.FareHistoryRepository;
import com.skyfare.search.FareOffer;
import com.skyfare.search.FlightProvider;
import com.skyfare.watch.Watch;
import com.skyfare.watch.WatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
@Profile("worker")
public class FarePoller {
    private static final Logger log = LoggerFactory.getLogger(FarePoller.class);
    private static final Duration HISTORY_RETENTION = Duration.ofDays(30);

    private final WatchRepository watches;
    private final FlightProvider provider;
    private final FareHistoryRepository history;
    private final FareEventPublisher publisher;
    private final ExecutorService searchExecutor;

    public FarePoller(WatchRepository watches, FlightProvider provider, FareHistoryRepository history,
                      FareEventPublisher publisher, ExecutorService searchExecutor) {
        this.watches = watches; this.provider = provider; this.history = history;
        this.publisher = publisher; this.searchExecutor = searchExecutor;
    }

    @Scheduled(fixedDelayString = "${skyfare.poll.interval-ms:60000}", initialDelay = 15000)
    public void pollAll() {
        List<Watch> all = watches.findAll();
        if (all.isEmpty()) return;
        log.info("Polling {} watched routes", all.size());

        List<CompletableFuture<Optional<FareOffer>>> lookups = all.stream()
                .map(w -> CompletableFuture.supplyAsync(() -> cheapest(w), searchExecutor)
                        .exceptionally(ex -> {
                            log.warn("Lookup failed for watch {}: {}", w.getId(), ex.toString());
                            return Optional.empty();
                        }))
                .toList();
        CompletableFuture.allOf(lookups.toArray(CompletableFuture[]::new)).join();

        for (int i = 0; i < all.size(); i++) {
            lookups.get(i).join().ifPresent(offer -> record(all.get(i), offer));
        }
    }

    private Optional<FareOffer> cheapest(Watch w) {
        return provider.findFares(w.getOrigin(), w.getDestination(), w.getDepartDate()).stream()
                .min(Comparator.comparing(FareOffer::price));
    }

    private void record(Watch w, FareOffer offer) {
        Instant now = Instant.now();
        FareHistoryItem item = new FareHistoryItem();
        item.setWatchId(w.getId().toString());
        item.setObservedAt(now.toString());
        item.setPrice(offer.price());
        item.setCurrency(offer.currency());
        item.setCarrier(offer.carrier());
        item.setExpiresAt(now.plus(HISTORY_RETENTION).getEpochSecond());
        history.save(item);

        boolean changed = w.getLastPrice() == null || w.getLastPrice().compareTo(offer.price()) != 0;
        if (changed) {
            publisher.publish(new FareChangedEvent(w.getId().toString(), w.getUserId().toString(),
                    w.getOrigin(), w.getDestination(), w.getDepartDate().toString(),
                    w.getLastPrice(), offer.price(), offer.currency(), now.toString()));
            w.setLastPrice(offer.price());
            watches.save(w);
        }
    }
}
