package com.skyfare.search;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class SearchService {
    private static final Logger log = LoggerFactory.getLogger(SearchService.class);
    private static final int MAX_FLEX_DAYS = 3;

    private final FlightProvider provider;
    private final ExecutorService searchExecutor;
    private final Timer searchTimer;
    private final Counter lookupCounter;
    private final Tracer tracer = GlobalOpenTelemetry.getTracer("skyfare.search");

    public SearchService(FlightProvider provider, ExecutorService searchExecutor, MeterRegistry registry) {
        this.provider = provider;
        this.searchExecutor = searchExecutor;
        this.searchTimer = Timer.builder("skyfare.search.duration")
                .publishPercentiles(0.5, 0.95, 0.99).register(registry);
        this.lookupCounter = Counter.builder("skyfare.search.lookups").register(registry);
    }

    @CircuitBreaker(name = "flightProvider", fallbackMethod = "searchFallback")
    public List<FareOffer> search(String origin, String destination, LocalDate date, int flexDays) {
        return searchTimer.record(() -> doSearch(origin, destination, date, flexDays));
    }

    private List<FareOffer> doSearch(String origin, String destination, LocalDate date, int flexDays) {
        List<LocalDate> window = datesInWindow(date, flexDays);
        List<CompletableFuture<List<FareOffer>>> futures = window.stream()
                .map(d -> CompletableFuture
                        .supplyAsync(() -> lookup(origin, destination, d), searchExecutor)
                        .exceptionally(ex -> {
                            log.warn("Fare lookup failed for {} {}->{}: {}", d, origin, destination, ex.toString());
                            return List.<FareOffer>of();
                        }))
                .toList();
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        List<FareOffer> all = futures.stream().flatMap(f -> f.join().stream()).toList();
        return cheapestPerDate(all);
    }

    private List<FareOffer> lookup(String origin, String destination, LocalDate d) {
        lookupCounter.increment();
        Span span = tracer.spanBuilder("fare.lookup")
                .setAttribute("skyfare.route", origin + "-" + destination)
                .setAttribute("skyfare.date", d.toString())
                .startSpan();
        try (Scope ignored = span.makeCurrent()) {
            List<FareOffer> offers = provider.findFares(origin, destination, d);
            span.setAttribute("skyfare.offers", offers.size());
            return offers;
        } catch (RuntimeException e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @SuppressWarnings("unused")
    private List<FareOffer> searchFallback(String origin, String destination, LocalDate date, int flexDays, Throwable t) {
        log.error("Flight provider circuit open for {}->{}: {}", origin, destination, t.toString());
        return List.of();
    }

    static List<LocalDate> datesInWindow(LocalDate date, int flexDays) {
        int flex = Math.max(0, Math.min(flexDays, MAX_FLEX_DAYS));
        LocalDate today = LocalDate.now();
        return IntStream.rangeClosed(-flex, flex)
                .mapToObj(date::plusDays)
                .filter(d -> !d.isBefore(today))
                .toList();
    }

    static List<FareOffer> cheapestPerDate(List<FareOffer> offers) {
        Map<LocalDate, Optional<FareOffer>> byDate = offers.stream()
                .collect(Collectors.groupingBy(FareOffer::departureDate,
                        Collectors.minBy(Comparator.comparing(FareOffer::price))));
        return byDate.values().stream().flatMap(Optional::stream)
                .sorted(Comparator.comparing(FareOffer::departureDate)).toList();
    }
}
