package com.example.moneylogapi.exception;

public class ExpenseArgumentNotValidException extends RuntimeException {

    public ExpenseArgumentNotValidException(String message) {
        super(message);
    }
}