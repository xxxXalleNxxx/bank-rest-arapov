package com.example.bankcards.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest (

    @NotBlank(message = "firstname is required")
    @Size(min = 2, max = 15, message = "Username must be between 2 and 15 characters")
    String firstName,

    @NotBlank(message = "lastName is required")
    @Size(min = 2, max = 15, message = "Username must be between 3 and 50 characters")
    String lastName,

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password

) { }