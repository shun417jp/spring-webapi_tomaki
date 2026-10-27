package com.example.moneylogapi.exception;

public class ExpenseNotFoundException extends RuntimeException {

    public ExpenseNotFoundException(Long id) {
        super("指定されたIDの支出が見つかりません: " + id);
    }

}
