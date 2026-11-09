package com.tracker.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.tracker.entity.Expense;
import com.tracker.entity.MerchantRule;
import com.tracker.exception.BadRequestException;
import com.tracker.repository.ExpenseRepository;
import com.tracker.util.TextUtil;

import lombok.RequiredArgsConstructor;

/**
 * Turns a bank debit SMS such as
 * "Rs 234.00 debited from A/c XX1234 on 03-Nov to BLINKIT via UPI Ref 123"
 * into a saved expense.
 */
@Service
@RequiredArgsConstructor
public class SmsParserService {

    private static final Pattern AMOUNT =
            Pattern.compile("(?i)(?:rs\\.?|inr|\u20B9)\\s*([0-9][0-9,]*(?:\\.[0-9]{1,2})?)");
    private static final Pattern PAYEE = Pattern.compile(
            "(?i)\\b(?:to|at|towards)\\s+([A-Za-z0-9][A-Za-z0-9 .&@_-]{1,38}?)"
                    + "(?=\\s+(?:on|via|ref|upi|using|from|for)\\b|[.,;]|$)");
    private static final Pattern DEBIT = Pattern.compile("(?i)debited|spent|paid|sent|purchase|withdrawn");
    private static final Pattern CREDIT = Pattern.compile("(?i)credited|received|deposited");

    private final ExpenseRepository repo;
    private final RuleService rules;

    public Expense parseAndSave(String text) {
        if (CREDIT.matcher(text).find() && !DEBIT.matcher(text).find()) {
            throw new BadRequestException("This looks like money received, not an expense.");
        }
        Matcher am = AMOUNT.matcher(text);
        if (!am.find()) {
            throw new BadRequestException("Could not find an amount like \"Rs 234\" in this message.");
        }
        BigDecimal amount = TextUtil.parseAmount(am.group(1));
        if (amount == null || amount.signum() <= 0) {
            throw new BadRequestException("The amount in this message is not valid.");
        }

        String merchant;
        String category = "Other";
        Optional<MerchantRule> rule = rules.match(text);
        if (rule.isPresent()) {
            merchant = rule.get().getMerchant();
            category = rule.get().getCategory();
        } else {
            Matcher pm = PAYEE.matcher(text);
            merchant = pm.find() ? TextUtil.tidy(pm.group(1)) : "Unknown";
        }

        Expense e = new Expense();
        e.setAmount(amount);
        e.setSpentAt(LocalDateTime.now().withSecond(0).withNano(0));
        e.setMerchant(merchant);
        e.setCategory(category);
        e.setPaymentMethod(TextUtil.detectPayment(text));
        e.setNote("Added from SMS");
        return repo.save(e);
    }
}
