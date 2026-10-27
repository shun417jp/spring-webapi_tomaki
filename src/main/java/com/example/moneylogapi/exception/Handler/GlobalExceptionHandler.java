package com.example.moneylogapi.exception.Handler;

import com.example.moneylogapi.exception.ExpenseArgumentNotValidException;
import com.example.moneylogapi.exception.ExpenseNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExpenseArgumentNotValidException.class)
    public ResponseEntity<String> handleExpenseArgumentNotValidException(
            ExpenseArgumentNotValidException e) {

        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(ExpenseNotFoundException.class)
    public ResponseEntity<String> handleExpenseNotFoundException(ExpenseNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleInvalidRequestBody(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(
                "入力形式が正しくありません。金額は1以上の整数、支出日はyyyy-MM-dd形式で入力してください。"
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleInvalidId(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body("IDは整数で指定してください。");
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<String> handleInvalidForm(BindException e) {
        return ResponseEntity.badRequest().body(
                "入力形式が正しくありません。金額は1以上の整数、支出日はyyyy-MM-dd形式で入力してください。"
        );
    }
}
