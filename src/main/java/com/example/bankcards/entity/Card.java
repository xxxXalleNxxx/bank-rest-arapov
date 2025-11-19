package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    Long id;

    @Column(name = "card_number", nullable = false)
    String cardNumber;

    @Column(name = "last_four_digits", nullable = false, length = 4)
    String lastFourNumbers;

    @Column(name = "expiry_date", nullable = false)
    LocalDate expiryDate;

    @Column(name = "balance", nullable = false)
    BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User owner;

    @Enumerated(EnumType.STRING)
    CardStatus status;

    @OneToMany(mappedBy = "fromCard", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<Transactions> outTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "toCard", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<Transactions> inTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<BlockCardRequest> blockRequests = new ArrayList<>();

    public boolean isActive() {
        return status == CardStatus.ACTIVE;
    }
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }
}
