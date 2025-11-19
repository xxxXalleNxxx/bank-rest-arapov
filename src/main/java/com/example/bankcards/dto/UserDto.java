package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {

    Long id;
    String firstName;
    String lastName;
    String email;
    int cardsCount;
    BigDecimal totalBalance;

    public UserDto(Long id, String firstName, String lastName, String email, int cardsCount, BigDecimal totalBalance) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.cardsCount = cardsCount;
        this.totalBalance = totalBalance;
    }

    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getCards().size(),
                user.getCards().stream()
                        .map(Card::getBalance)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );
    }
}
