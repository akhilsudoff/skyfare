package com.skyfare.search;

import java.time.LocalDate;
import java.util.List;

public interface FlightProvider {
    List<FareOffer> findFares(String origin, String destination, LocalDate date);
}
