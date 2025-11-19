package com.example.bankcards.entity;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum CardStatus {

    ACTIVE("Активна", "Карта может использоваться", true),
    BLOCKED("Заблокирована", "Карта не может использоваться", false),
    EXPIRES("Просрочена", "Срок действия карты истек", false);

    String statusName;

    String statusDescription;

    Boolean isActive;
}
