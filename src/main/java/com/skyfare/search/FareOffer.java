package com.skyfare.search;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FareOffer(
        String origin, String destination, LocalDate departureDate,
        String carrier, BigDecimal price, String currency, int stops) { }
