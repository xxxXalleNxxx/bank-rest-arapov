package com.example.bankcards.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @Size(min = 2, max = 15, message = "Username must be between 2 and 15 characters")

        String firstname,
        @Size(min = 2, max = 15, message = "Username must be between 3 and 50 characters")

        String lastName,
        @Email(message = "Email should be valid")

        String email
) {
}
