package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardSearchRequest {

    String search;
    CardStatus status;
    int page = 0;
    int size = 10;
}
