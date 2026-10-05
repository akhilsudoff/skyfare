package com.skyfare.watch;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class WatchDtos {
    public record CreateWatch(
            @NotNull @Pattern(regexp = "[A-Za-z]{3}", message = "must be a 3-letter IATA code") String origin,
            @NotNull @Pattern(regexp = "[A-Za-z]{3}", message = "must be a 3-letter IATA code") String destination,
            @NotNull @FutureOrPresent LocalDate departDate,
            @NotNull @Positive BigDecimal targetPrice) { }

    public record WatchView(UUID id, String origin, String destination, LocalDate departDate,
                            BigDecimal targetPrice, BigDecimal lastPrice, Instant createdAt) {
        static WatchView from(Watch w) {
            return new WatchView(w.getId(), w.getOrigin(), w.getDestination(), w.getDepartDate(),
                    w.getTargetPrice(), w.getLastPrice(), w.getCreatedAt());
        }
    }

    public record FarePoint(String observedAt, BigDecimal price, String currency, String carrier) { }
}
