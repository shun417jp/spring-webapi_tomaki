package com.example.moneylogapi.repository;

import com.example.moneylogapi.exception.ExpenseNotFoundException;
import com.example.moneylogapi.mapper.ExpenseMapper;
import com.example.moneylogapi.model.Expense;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ExpenseRepository {
    private final ExpenseMapper expenseMapper;

    public ExpenseRepository(ExpenseMapper expenseMapper) {
        this.expenseMapper = expenseMapper;
    }

    public List<Expense> findAll() {
        return expenseMapper.findAll();
    }

    public int insertExpense(Expense expense) {
        return expenseMapper.insert(expense);
    }

    public Expense findById(Long id) {
        return expenseMapper.findById(id);
    }

    public boolean delete(Long id) {
        return expenseMapper.deleteById(id) > 0;
    }

    public Expense update(Expense expense) {
        if (expenseMapper.update(expense) == 0) {
            throw new ExpenseNotFoundException(expense.getId());
        }
        return expense;
    }
}
