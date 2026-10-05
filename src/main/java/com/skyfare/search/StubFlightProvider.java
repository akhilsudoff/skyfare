package com.skyfare.search;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Component
@ConditionalOnProperty(name = "skyfare.provider", havingValue = "stub", matchIfMissing = true)
public class StubFlightProvider implements FlightProvider {
    private static final List<String> CARRIERS = List.of("QF", "VA", "JQ", "ZL");
    private static final long BUCKET_MS = 5 * 60 * 1000L;

    @Override
    public List<FareOffer> findFares(String origin, String destination, LocalDate date) {
        try { Thread.sleep(250); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        long bucket = System.currentTimeMillis() / BUCKET_MS;
        Random rnd = new Random((origin + destination + date + bucket).hashCode());
        return CARRIERS.stream()
                .limit(2 + rnd.nextInt(3))
                .map(carrier -> new FareOffer(origin, destination, date, carrier,
                        BigDecimal.valueOf(120 + rnd.nextInt(600)).setScale(2, RoundingMode.HALF_UP),
                        "AUD", rnd.nextInt(2)))
                .toList();
    }
}
