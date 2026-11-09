package com.tracker.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.tracker.dto.ExpenseRequest;
import com.tracker.dto.SmsRequest;
import com.tracker.entity.Expense;
import com.tracker.service.CsvImportService;
import com.tracker.service.ExpenseService;
import com.tracker.service.SmsParserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService service;
    private final CsvImportService csvImport;
    private final SmsParserService smsParser;

    @GetMapping
    public List<Expense> list(@RequestParam(defaultValue = "") String merchant,
                              @RequestParam(defaultValue = "") String category,
                              @RequestParam(defaultValue = "") String paymentMethod,
                              @RequestParam(defaultValue = "") String q,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.search(merchant, category, paymentMethod, from, to, q);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Expense create(@Valid @RequestBody ExpenseRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public Expense update(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/export")
    public ResponseEntity<String> export(@RequestParam(required = false) String month) {
        YearMonth ym = (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"expenses-" + ym + ".csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(service.exportCsv(ym));
    }

    @PostMapping("/import")
    public Map<String, Object> importCsv(@RequestParam("file") MultipartFile file) throws IOException {
        return csvImport.importCsv(file);
    }

    /** Also usable from an Android automation app (MacroDroid / Tasker) that posts every debit SMS. */
    @PostMapping("/parse-sms")
    @ResponseStatus(HttpStatus.CREATED)
    public Expense parseSms(@Valid @RequestBody SmsRequest request) {
        return smsParser.parseAndSave(request.text());
    }
}
