package com.tracker.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime spentAt;

    @Column(nullable = false, length = 80)
    private String merchant;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false, length = 30)
    private String paymentMethod;

    @Column(length = 255)
    private String note;
}
