package com.skyfare.search;

import com.skyfare.config.AsyncConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import static org.assertj.core.api.Assertions.assertThat;

class SearchServiceTest {
    private static final ExecutorService POOL = new AsyncConfig().searchExecutor();

    @AfterAll
    static void shutdown() { POOL.shutdownNow(); }

    @Test
    void returnsCheapestOfferPerDate() {
        LocalDate d = LocalDate.now().plusDays(10);
        List<FareOffer> offers = List.of(
                new FareOffer("ADL", "SYD", d, "QF", new BigDecimal("300.00"), "AUD", 0),
                new FareOffer("ADL", "SYD", d, "JQ", new BigDecimal("150.00"), "AUD", 1),
                new FareOffer("ADL", "SYD", d.plusDays(1), "VA", new BigDecimal("220.00"), "AUD", 0));
        List<FareOffer> result = SearchService.cheapestPerDate(offers);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).carrier()).isEqualTo("JQ");
        assertThat(result.get(0).price()).isEqualByComparingTo("150.00");
    }

    @Test
    void windowExcludesPastDatesAndCapsFlex() {
        List<LocalDate> window = SearchService.datesInWindow(LocalDate.now(), 10);
        assertThat(window).allMatch(d -> !d.isBefore(LocalDate.now()));
        assertThat(window).hasSize(4);
    }

    @Test
    void fansOutConcurrentlyRatherThanSequentially() {
        SearchService service = new SearchService(new StubFlightProvider(), POOL, new SimpleMeterRegistry());
        Instant start = Instant.now();
        List<FareOffer> result = service.search("ADL", "SYD", LocalDate.now().plusDays(30), 3);
        Duration elapsed = Duration.between(start, Instant.now());
        assertThat(result).hasSize(7);
        assertThat(elapsed).isLessThan(Duration.ofMillis(1000));
    }

    @Test
    void failingDateDegradesThatDateOnly() {
        FlightProvider flaky = (o, d, date) -> {
            if (date.getDayOfMonth() % 2 == 0) throw new IllegalStateException("upstream timeout");
            return new StubFlightProvider().findFares(o, d, date);
        };
        SearchService service = new SearchService(flaky, POOL, new SimpleMeterRegistry());
        List<FareOffer> result = service.search("ADL", "MEL", LocalDate.now().plusDays(30), 3);
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(f -> f.departureDate().getDayOfMonth() % 2 == 1);
    }
}
