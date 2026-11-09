package com.tracker.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.tracker.dto.RuleRequest;
import com.tracker.entity.MerchantRule;
import com.tracker.exception.BadRequestException;
import com.tracker.exception.NotFoundException;
import com.tracker.repository.RuleRepository;
import com.tracker.util.TextUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository repo;

    public List<MerchantRule> all() {
        return repo.findAll(Sort.by("merchant"));
    }

    public MerchantRule add(RuleRequest r) {
        String keyword = r.keyword().trim().toLowerCase();
        if (repo.existsByKeywordIgnoreCase(keyword)) {
            throw new BadRequestException("A rule for \"" + keyword + "\" already exists.");
        }
        MerchantRule rule = new MerchantRule();
        rule.setKeyword(keyword);
        rule.setMerchant(TextUtil.tidy(r.merchant()));
        rule.setCategory(r.category().trim());
        return repo.save(rule);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("Rule not found.");
        }
        repo.deleteById(id);
    }

    /** Finds the rule whose keyword appears in the text. Longer keywords win. */
    public Optional<MerchantRule> match(String text) {
        if (text == null) {
            return Optional.empty();
        }
        String t = text.toLowerCase();
        return repo.findAll().stream()
                .filter(rule -> t.contains(rule.getKeyword().toLowerCase()))
                .max(Comparator.comparingInt(rule -> rule.getKeyword().length()));
    }
}
