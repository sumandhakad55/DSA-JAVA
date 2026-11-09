package com.tracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tracker.dto.ExpenseRequest;
import com.tracker.entity.Expense;
import com.tracker.exception.NotFoundException;
import com.tracker.repository.ExpenseRepository;
import com.tracker.util.TextUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository repo;

    public List<Expense> search(String merchant, String category, String payment,
                                LocalDate from, LocalDate to, String q) {
        LocalDateTime f = from == null ? LocalDateTime.of(2000, 1, 1, 0, 0) : from.atStartOfDay();
        LocalDateTime t = to == null ? LocalDateTime.of(2100, 1, 1, 0, 0) : to.plusDays(1).atStartOfDay();
        return repo.search(blank(merchant), blank(category), blank(payment), f, t, blank(q));
    }

    public List<Expense> forMonth(YearMonth ym) {
        return repo.findBySpentAtGreaterThanEqualAndSpentAtLessThanOrderBySpentAtDesc(
                ym.atDay(1).atStartOfDay(), ym.plusMonths(1).atDay(1).atStartOfDay());
    }

    public Expense get(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Expense not found."));
    }

    public Expense create(ExpenseRequest r) {
        return repo.save(apply(new Expense(), r));
    }

    public Expense update(Long id, ExpenseRequest r) {
        return repo.save(apply(get(id), r));
    }

    public void delete(Long id) {
        repo.delete(get(id));
    }

    private Expense apply(Expense e, ExpenseRequest r) {
        e.setAmount(r.amount());
        e.setSpentAt(r.spentAt() == null ? LocalDateTime.now().withSecond(0).withNano(0) : r.spentAt());
        e.setMerchant(TextUtil.tidy(r.merchant()));
        e.setCategory(r.category().trim());
        e.setPaymentMethod(r.paymentMethod().trim());
        e.setNote(r.note() == null || r.note().isBlank() ? null : r.note().trim());
        return e;
    }

    public String exportCsv(YearMonth ym) {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        StringBuilder sb = new StringBuilder("\uFEFFDate,Time,App,Category,Payment method,Amount,Note\n");
        for (Expense e : forMonth(ym)) {
            sb.append(e.getSpentAt().format(dateFmt)).append(',')
              .append(e.getSpentAt().format(timeFmt)).append(',')
              .append(csv(e.getMerchant())).append(',')
              .append(csv(e.getCategory())).append(',')
              .append(csv(e.getPaymentMethod())).append(',')
              .append(e.getAmount().toPlainString()).append(',')
              .append(csv(e.getNote())).append('\n');
        }
        return sb.toString();
    }

    private static String csv(String s) {
        if (s == null) {
            return "";
        }
        // stop spreadsheet formula injection
        if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) {
            s = "'" + s;
        }
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private static String blank(String s) {
        return s == null ? "" : s.trim();
    }

    public static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
