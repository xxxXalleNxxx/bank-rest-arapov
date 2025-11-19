package com.example.bankcards.dto;

public record ValidateTokenResponse(
        boolean valid,
        String message,
        UserDto user
) {
}
