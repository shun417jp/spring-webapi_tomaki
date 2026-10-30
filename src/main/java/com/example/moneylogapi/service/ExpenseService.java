package com.example.moneylogapi.service;

import com.example.moneylogapi.exception.ExpenseArgumentNotValidException;
import com.example.moneylogapi.exception.ExpenseNotFoundException;
import com.example.moneylogapi.repository.ExpenseRepository;
import com.example.moneylogapi.model.Expense;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<Expense> findAll() {
        return expenseRepository.findAll();
    }

    public Expense findById(Long id) {
        Expense expense = expenseRepository.findById(id);

        if (expense == null){
            throw new ExpenseNotFoundException(id);
        }

        return expense;
    }

    public Expense create(Expense expense) {
        validateExpense(expense);

        expenseRepository.insertExpense(expense);
        return expense;
    }

    public Expense update(Long id, Expense expense) {
        validateExpense(expense);
        expense.setId(id);
        return expenseRepository.update(expense);
    }

    public void deleteById(Long id) {
        if (!expenseRepository.delete(id)) {
            throw new ExpenseNotFoundException(id);
        }
    }

    private void validateExpense(Expense expense) {
        if (expense.getTitle() == null || expense.getTitle().isBlank()){
            throw new ExpenseArgumentNotValidException(
                    "タイトルを入力してください"
            );
        }

        if (expense.getAmount() == null){
            throw new ExpenseArgumentNotValidException(
                    "金額を入力してください"
            );
        }

        if (expense.getAmount() <= 0){
            throw new ExpenseArgumentNotValidException(
                    "金額は1以上にしてください"
            );
        }

        if (expense.getExpenseDate() == null){
            throw new ExpenseArgumentNotValidException(
                    "支出日を入力してください"
            );
        }

        if (expense.getCategory() == null || expense.getCategory().isBlank()){
            expense.setCategory("OTHER");
        }

        validateLength(expense.getTitle(), 100, "タイトル");
        validateLength(expense.getCategory(), 50, "カテゴリ");
        validateLength(expense.getPaymentMethod(), 50, "支払方法");
        validateLength(expense.getMemo(), 255, "メモ");
    }

    private void validateLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new ExpenseArgumentNotValidException(
                    fieldName + "は" + maxLength + "文字以内で入力してください"
            );
        }
    }
}
