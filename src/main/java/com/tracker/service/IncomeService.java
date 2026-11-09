package com.tracker.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tracker.dto.IncomeRequest;
import com.tracker.entity.Income;
import com.tracker.exception.NotFoundException;
import com.tracker.repository.IncomeRepository;
import com.tracker.util.TextUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository repo;

    public List<Income> all() {
        return repo.findAllByOrderByReceivedOnDescIdDesc();
    }

    public Income add(IncomeRequest r) {
        Income i = new Income();
        i.setAmount(r.amount());
        i.setReceivedOn(r.receivedOn() == null ? LocalDate.now() : r.receivedOn());
        i.setSource(TextUtil.tidy(r.source()));
        return repo.save(i);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("Income entry not found.");
        }
        repo.deleteById(id);
    }
}
