package com.tracker.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tracker.entity.Income;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findAllByOrderByReceivedOnDescIdDesc();

    @Query("select sum(i.amount) from Income i")
    BigDecimal totalAll();
}
