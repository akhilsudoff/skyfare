package com.skyfare.search;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;
    public SearchController(SearchService searchService) { this.searchService = searchService; }

    @GetMapping
    public List<FareOffer> search(
            @RequestParam String origin, @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "2") int flexDays) {
        return searchService.search(origin.toUpperCase(), destination.toUpperCase(), date, flexDays);
    }
}
