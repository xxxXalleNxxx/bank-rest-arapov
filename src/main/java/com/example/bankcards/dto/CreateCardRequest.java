package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCardRequest {

    @NotBlank
    String cardNumber;

    @NotNull
    Long ownerId;

    @NotNull
    LocalDate expiryDate;

    BigDecimal initialBalance;
}
