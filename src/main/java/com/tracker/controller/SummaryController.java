package com.tracker.controller;

import java.time.YearMonth;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.tracker.service.SummaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService service;

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return service.dashboard();
    }

    /** month = YYYY-MM, e.g. 2026-11. Defaults to the current month. */
    @GetMapping("/monthly")
    public Map<String, Object> monthly(@RequestParam(required = false) String month) {
        YearMonth ym = (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month);
        return service.monthly(ym);
    }
}
