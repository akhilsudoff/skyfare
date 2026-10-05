package com.skyfare.events;

import java.math.BigDecimal;

public record FareChangedEvent(
        String watchId, String userId, String origin, String destination, String departDate,
        BigDecimal oldPrice, BigDecimal newPrice, String currency, String observedAt) { }
