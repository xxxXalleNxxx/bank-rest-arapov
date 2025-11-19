package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "block_card_request")
public class BlockCardRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    Card card;

    @ManyToOne(fetch = FetchType.LAZY)
            @JoinColumn(name = "user_user_id")
    User user;

    String reason;

    @Enumerated(EnumType.STRING)
    BlockRequestStatus status;
}
