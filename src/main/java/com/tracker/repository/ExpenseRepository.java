package com.tracker.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tracker.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findBySpentAtGreaterThanEqualAndSpentAtLessThanOrderBySpentAtDesc(
            LocalDateTime from, LocalDateTime to);

    /** May return null when there are no rows - callers convert null to zero. */
    @Query("select sum(e.amount) from Expense e")
    BigDecimal totalAll();

    @Query("select sum(e.amount) from Expense e where e.spentAt >= :from and e.spentAt < :to")
    BigDecimal totalBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    boolean existsBySpentAtAndAmountAndMerchant(LocalDateTime spentAt, BigDecimal amount, String merchant);

    @Query("""
            select e from Expense e
            where (:merchant = '' or lower(e.merchant) = lower(:merchant))
              and (:category = '' or e.category = :category)
              and (:payment = '' or e.paymentMethod = :payment)
              and e.spentAt >= :from and e.spentAt < :to
              and (:q = '' or lower(e.merchant) like lower(concat('%', :q, '%'))
                   or lower(coalesce(e.note, '')) like lower(concat('%', :q, '%')))
            order by e.spentAt desc
            """)
    List<Expense> search(@Param("merchant") String merchant,
                         @Param("category") String category,
                         @Param("payment") String payment,
                         @Param("from") LocalDateTime from,
                         @Param("to") LocalDateTime to,
                         @Param("q") String q);
}
