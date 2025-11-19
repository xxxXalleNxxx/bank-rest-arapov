package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.util.CardEncryptor;
import com.example.bankcards.util.CardMasker;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardDto(

        @Null
        @Schema(description = "Айди", example = "1")
        Long id,

        @NotBlank(message = "Card number is required")
        @Size(min = 16, max = 16, message = "Card number must be 16 digits")
        String maskedCardNumber,

        @NotNull(message = "Expiry date is required")
        @Future(message = "Expiry date must be in the future")
        LocalDate expiryDate,

        @NotNull(message = "Balance is required")
        @DecimalMin(value = "0.0", message = "balance can't be negative")
        BigDecimal balance,

        @NotNull(message = "Owner is required")
        @Valid
        UserDto owner,

        @Schema(description = "Статус карты", example = "ACTIVATED")
        @NotNull(message = "Status is required")
        CardStatus status
) {
    public static CardDto from(Card card) {
        String decrypted = CardEncryptor.decrypt(card.getCardNumber());
        String cardNumber = CardMasker.maskCardNumber(decrypted);
        return new CardDto(
                card.getId(),
                cardNumber,
                card.getExpiryDate(),
                card.getBalance(),
                UserDto.from(card.getOwner()),
                card.getStatus()
        );
    }
}
