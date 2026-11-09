package com.tracker.service;

import static com.tracker.service.ExpenseService.nz;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tracker.entity.Expense;
import com.tracker.repository.ExpenseRepository;
import com.tracker.repository.IncomeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final ExpenseRepository expenses;
    private final IncomeRepository incomes;
    private final ExpenseService expenseService;
    private final SettingsService settings;

    /** Opening balance + all income - all expenses. */
    public BigDecimal balance() {
        BigDecimal start = settings.getDecimal(SettingsService.STARTING_BALANCE, BigDecimal.ZERO);
        return start.add(nz(incomes.totalAll())).subtract(nz(expenses.totalAll()));
    }

    public BigDecimal spentToday() {
        LocalDate d = LocalDate.now();
        return nz(expenses.totalBetween(d.atStartOfDay(), d.plusDays(1).atStartOfDay()));
    }

    public Map<String, Object> dashboard() {
        YearMonth ym = YearMonth.now();
        List<Expense> month = expenseService.forMonth(ym);
        BigDecimal monthTotal = sum(month);
        BigDecimal budget = settings.getDecimal(SettingsService.MONTHLY_BUDGET, BigDecimal.ZERO);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("todayTotal", spentToday());
        out.put("monthTotal", monthTotal);
        out.put("balance", balance());
        out.put("budget", budget);
        out.put("budgetUsedPercent", percent(monthTotal, budget));
        out.put("byMerchant", group(month, Expense::getMerchant));
        out.put("byPayment", group(month, Expense::getPaymentMethod));
        return out;
    }

    public Map<String, Object> monthly(YearMonth ym) {
        List<Expense> list = expenseService.forMonth(ym);
        BigDecimal total = sum(list);
        BigDecimal previous = sum(expenseService.forMonth(ym.minusMonths(1)));

        Map<Integer, BigDecimal> byDay = new LinkedHashMap<>();
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            byDay.put(d, BigDecimal.ZERO);
        }
        for (Expense e : list) {
            byDay.merge(e.getSpentAt().getDayOfMonth(), e.getAmount(), BigDecimal::add);
        }

        int daysCounted = ym.equals(YearMonth.now()) ? LocalDate.now().getDayOfMonth() : ym.lengthOfMonth();
        Expense biggest = list.stream().max((a, b) -> a.getAmount().compareTo(b.getAmount())).orElse(null);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("month", ym.toString());
        out.put("total", total);
        out.put("count", list.size());
        out.put("dailyAverage", total.divide(BigDecimal.valueOf(daysCounted), 2, RoundingMode.HALF_UP));
        out.put("byMerchant", group(list, Expense::getMerchant));
        out.put("byCategory", group(list, Expense::getCategory));
        out.put("byPayment", group(list, Expense::getPaymentMethod));
        out.put("byDay", byDay);
        out.put("previousMonthTotal", previous);
        out.put("changePercent", previous.signum() == 0 ? null
                : total.subtract(previous).multiply(BigDecimal.valueOf(100))
                       .divide(previous, 1, RoundingMode.HALF_UP));
        if (biggest != null) {
            Map<String, Object> b = new LinkedHashMap<>();
            b.put("merchant", biggest.getMerchant());
            b.put("amount", biggest.getAmount());
            b.put("spentAt", biggest.getSpentAt());
            out.put("biggest", b);
        }
        return out;
    }

    /** The message sent every night. */
    public String nightlyMessage() {
        BigDecimal today = spentToday();
        BigDecimal balance = balance();
        if (today.signum() == 0) {
            return "Great job! You did not spend anything today. Your current balance is " + rs(balance) + ".";
        }
        StringBuilder sb = new StringBuilder("Good evening! You spent " + rs(today) + " today. "
                + "Your current balance is " + rs(balance) + ".");

        BigDecimal budget = settings.getDecimal(SettingsService.MONTHLY_BUDGET, BigDecimal.ZERO);
        BigDecimal used = percent(sum(expenseService.forMonth(YearMonth.now())), budget);
        if (budget.signum() > 0 && used.compareTo(BigDecimal.valueOf(80)) >= 0) {
            sb.append(" Careful: you have used ").append(used.stripTrailingZeros().toPlainString())
              .append("% of this month's budget.");
        }
        sb.append(" Keep tracking your spending!");
        return sb.toString();
    }

    // ---------- helpers ----------

    public static String rs(BigDecimal amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.of("en", "IN"));
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(0);
        return "\u20B9" + nf.format(amount);
    }

    private static BigDecimal sum(List<Expense> list) {
        return list.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal percent(BigDecimal part, BigDecimal whole) {
        if (whole == null || whole.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return part.multiply(BigDecimal.valueOf(100)).divide(whole, 1, RoundingMode.HALF_UP);
    }

    /** Totals per key, biggest first. */
    private static Map<String, BigDecimal> group(List<Expense> list, Function<Expense, String> key) {
        Map<String, BigDecimal> totals = new HashMap<>();
        for (Expense e : list) {
            totals.merge(key.apply(e), e.getAmount(), BigDecimal::add);
        }
        List<Map.Entry<String, BigDecimal>> entries = new ArrayList<>(totals.entrySet());
        entries.sort(Map.Entry.<String, BigDecimal>comparingByValue().reversed());
        return entries.stream().collect(Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }
}
