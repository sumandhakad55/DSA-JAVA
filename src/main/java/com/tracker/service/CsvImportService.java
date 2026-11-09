package com.tracker.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tracker.entity.Expense;
import com.tracker.entity.MerchantRule;
import com.tracker.exception.BadRequestException;
import com.tracker.repository.ExpenseRepository;
import com.tracker.util.TextUtil;

import lombok.RequiredArgsConstructor;

/**
 * Imports a bank / PhonePe / Google Pay statement saved as CSV.
 * The first row must be the column names. We look for a date column, an amount (or debit)
 * column and a description column.
 */
@Service
@RequiredArgsConstructor
public class CsvImportService {

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            "d/M/yyyy", "d-M-yyyy", "yyyy-M-d", "d MMM yyyy", "d-MMM-yyyy", "d/M/yy", "d MMM yy", "MMM d, yyyy")
            .stream()
            .map(p -> new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(p)
                    .toFormatter(Locale.ENGLISH))
            .toList();

    private final ExpenseRepository repo;
    private final RuleService rules;

    public Map<String, Object> importCsv(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please choose a CSV file.");
        }
        int imported = 0, duplicates = 0, skipped = 0;

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader().setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setTrim(true)
                     .build().parse(reader)) {

            List<String> headers = parser.getHeaderNames();
            String dateCol = find(headers, "date");
            String amountCol = find(headers, "debit", "withdraw", "amount");
            String descCol = find(headers, "narration", "description", "details", "particular", "merchant", "remark");
            String typeCol = find(headers, "dr/cr", "cr/dr", "txn type", "transaction type");

            if (dateCol == null || amountCol == null || descCol == null) {
                throw new BadRequestException(
                        "CSV needs columns for date, amount (or debit) and description. Found: " + headers);
            }

            for (CSVRecord r : parser) {
                try {
                    LocalDateTime when = parseDate(r.get(dateCol));
                    BigDecimal amount = TextUtil.parseAmount(r.get(amountCol));
                    if (when == null || amount == null || amount.signum() <= 0) {
                        skipped++;
                        continue;
                    }
                    if (typeCol != null && r.get(typeCol).toLowerCase().startsWith("cr")) {
                        skipped++; // money received
                        continue;
                    }

                    String desc = r.get(descCol);
                    Optional<MerchantRule> rule = rules.match(desc);
                    String merchant;
                    String category;
                    if (rule.isPresent()) {
                        merchant = rule.get().getMerchant();
                        category = rule.get().getCategory();
                    } else {
                        String guess = desc.replaceAll("(?i)upi[-/]", "").trim();
                        merchant = guess.isEmpty() ? "Unknown"
                                : TextUtil.tidy(guess.substring(0, Math.min(30, guess.length())));
                        category = "Other";
                    }

                    if (repo.existsBySpentAtAndAmountAndMerchant(when, amount, merchant)) {
                        duplicates++;
                        continue;
                    }
                    Expense e = new Expense();
                    e.setAmount(amount);
                    e.setSpentAt(when);
                    e.setMerchant(merchant);
                    e.setCategory(category);
                    e.setPaymentMethod(TextUtil.detectPayment(desc));
                    e.setNote(desc.length() > 255 ? desc.substring(0, 255) : desc);
                    repo.save(e);
                    imported++;
                } catch (RuntimeException ex) {
                    skipped++;
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("imported", imported);
        result.put("duplicatesSkipped", duplicates);
        result.put("rowsSkipped", skipped);
        return result;
    }

    private static String find(List<String> headers, String... keys) {
        for (String key : keys) {
            for (String h : headers) {
                if (h != null && h.toLowerCase().contains(key)) {
                    return h;
                }
            }
        }
        return null;
    }

    private static LocalDateTime parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String s = raw.trim();
        LocalDate d = tryParse(s);
        if (d == null && s.matches(".*[ T].*")) {
            d = tryParse(s.split("[ T]")[0]);
        }
        return d == null ? null : d.atStartOfDay();
    }

    private static LocalDate tryParse(String s) {
        for (DateTimeFormatter f : DATE_FORMATS) {
            try {
                return LocalDate.parse(s, f);
            } catch (RuntimeException ignored) {
                // try next format
            }
        }
        return null;
    }
}
