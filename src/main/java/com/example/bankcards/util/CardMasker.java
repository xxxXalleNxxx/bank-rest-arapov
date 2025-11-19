package com.example.bankcards.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CardMasker {

    private static final String MASK_SYMBOL = "*";
    private static final int VISIBLE_NUMS = 4;

    public static String maskCardNumber(String cardNumber) {

        if (cardNumber == null || cardNumber.length() < VISIBLE_NUMS) {
            return "**** **** **** ****";
        }

        String lastNums = cardNumber.substring(cardNumber.length() - VISIBLE_NUMS);

        int maskedLength = cardNumber.length() - VISIBLE_NUMS;

        String maskedPart = MASK_SYMBOL.repeat(maskedLength);

        return formatCardNumber(maskedPart + lastNums);
    }

    public static String formatCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            return cardNumber;
        }

        return String.format("%s %s %s %s",
                cardNumber.substring(0, 4),
                cardNumber.substring(4, 8),
                cardNumber.substring(8, 12),
                cardNumber.substring(12,16)
        );
    }

    public static String getLastNums(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < VISIBLE_NUMS) {
            return "****";
        }
        return cardNumber.substring(cardNumber.length() - VISIBLE_NUMS);
    }
}
