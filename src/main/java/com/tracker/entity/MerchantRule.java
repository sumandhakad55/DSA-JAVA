package com.tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** "If the text contains KEYWORD, call it MERCHANT in CATEGORY." */
@Entity
@Table(name = "merchant_rules")
@Getter
@Setter
@NoArgsConstructor
public class MerchantRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String keyword;

    @Column(nullable = false, length = 80)
    private String merchant;

    @Column(nullable = false, length = 40)
    private String category;
}
