package com.example.moneylogapi.service;

import com.example.moneylogapi.mapper.ExpenseMapper;
import com.example.moneylogapi.repository.ExpenseRepository;
import com.example.moneylogapi.exception.ExpenseNotFoundException;
import com.example.moneylogapi.exception.ExpenseArgumentNotValidException;
import com.example.moneylogapi.model.Expense;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTests {
    @Mock
    private ExpenseMapper expenseMapper;

    @Test
    void updatesUsingPathIdInsteadOfBodyId() {
        Expense expense = createValidExpense();
        expense.setId(999L);
        when(expenseMapper.update(expense)).thenAnswer(invocation -> {
            assertEquals(1L, expense.getId());
            return 1;
        });

        var result = new ExpenseService(new ExpenseRepository(expenseMapper)).update(1L, expense);

        assertSame(expense, result);
        verify(expenseMapper).update(expense);
    }

    @Test
    void throwsWhenNoRowWasUpdated() {
        Expense expense = createValidExpense();
        when(expenseMapper.update(expense)).thenReturn(0);

        assertThrows(ExpenseNotFoundException.class,
                () -> new ExpenseService(new ExpenseRepository(expenseMapper)).update(999L, expense));
        verify(expenseMapper).update(expense);
    }

    @Test
    void rejectsInvalidInputBeforeWritingToDatabase() {
        Expense expense = createValidExpense();
        expense.setAmount(0);
        ExpenseService service = new ExpenseService(new ExpenseRepository(expenseMapper));
        assertThrows(ExpenseArgumentNotValidException.class, () -> service.create(expense));
        assertThrows(ExpenseArgumentNotValidException.class, () -> service.update(1L, expense));
        verifyNoInteractions(expenseMapper);
    }

    private Expense createValidExpense() {
        Expense expense = new Expense();
        expense.setTitle("Java参考書");
        expense.setAmount(2800);
        expense.setExpenseDate(LocalDate.of(2026, 10, 9));
        return expense;
    }

    @ParameterizedTest
    @ValueSource(strings = {"title", "category", "paymentMethod", "memo"})
    void rejectsTextLongerThanDatabaseColumn(String field) {
        Expense expense = createValidExpense();
        switch (field) {
            case "title" -> expense.setTitle("a".repeat(101));
            case "category" -> expense.setCategory("a".repeat(51));
            case "paymentMethod" -> expense.setPaymentMethod("a".repeat(51));
            case "memo" -> expense.setMemo("a".repeat(256));
        }
        ExpenseService service = new ExpenseService(new ExpenseRepository(expenseMapper));
        assertThrows(ExpenseArgumentNotValidException.class, () -> service.create(expense));
        assertThrows(ExpenseArgumentNotValidException.class, () -> service.update(1L, expense));
        verifyNoInteractions(expenseMapper);
    }
}
