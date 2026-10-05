package com.skyfare.events;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class FareAlertServiceTest {
    private static final BigDecimal TARGET = new BigDecimal("200.00");

    @Test
    void alertsOnFirstObservationBelowTarget() {
        assertThat(FareAlertService.shouldAlert(TARGET, null, new BigDecimal("150.00"))).isTrue();
    }

    @Test
    void alertsWhenPriceCrossesDownThroughTarget() {
        assertThat(FareAlertService.shouldAlert(TARGET, new BigDecimal("250.00"), new BigDecimal("199.99"))).isTrue();
    }

    @Test
    void doesNotAlertWhilePriceStaysBelowTarget() {
        assertThat(FareAlertService.shouldAlert(TARGET, new BigDecimal("180.00"), new BigDecimal("170.00"))).isFalse();
    }

    @Test
    void doesNotAlertAboveTarget() {
        assertThat(FareAlertService.shouldAlert(TARGET, new BigDecimal("300.00"), new BigDecimal("250.00"))).isFalse();
    }
}
