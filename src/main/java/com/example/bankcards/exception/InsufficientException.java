package com.example.bankcards.exception;

public class InsufficientException extends RuntimeException{

    public InsufficientException(String message) {
        super(message);
    }
}
