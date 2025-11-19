package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferRequest {

    @NotNull
    Long fromCardId;

    @NotNull
    Long toCardId;

    @NotNull
            @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0 рублей")
    BigDecimal amount;

    String description;
}
