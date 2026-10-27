package com.example.moneylogapi.controller;

import com.example.moneylogapi.model.Expense;
import com.example.moneylogapi.exception.ExpenseArgumentNotValidException;
import com.example.moneylogapi.exception.ExpenseNotFoundException;
import com.example.moneylogapi.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"amount\":1,\"expenseDate\":\"2026-10-02\"}",
            "{\"title\":\"\",\"amount\":1,\"expenseDate\":\"2026-10-02\"}",
            "{\"title\":\"   \",\"amount\":1,\"expenseDate\":\"2026-10-02\"}",
            "{\"title\":\"Book\",\"expenseDate\":\"2026-10-02\"}",
            "{\"title\":\"Book\",\"amount\":0,\"expenseDate\":\"2026-10-02\"}",
            "{\"title\":\"Book\",\"amount\":-1,\"expenseDate\":\"2026-10-02\"}",
            "{\"title\":\"Book\",\"amount\":1}"
    })
    void returnsBadRequestFromService(String body) throws Exception {
        when(expenseService.create(any(Expense.class)))
                .thenThrow(new ExpenseArgumentNotValidException("入力が不正です"));
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("入力が不正です"));
        verify(expenseService).create(any(Expense.class));
    }

    @Test
    void acceptsValidExpense() throws Exception {
        when(expenseService.create(any(Expense.class)))
                .thenAnswer(invocation -> {
                    Expense expense = invocation.getArgument(0);
                    expense.setId(1L);
                    return expense;
                });
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Book","amount":1,"expenseDate":"2026-10-02"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Location", "http://localhost/api/expenses/1"));
        verify(expenseService).create(any(Expense.class));
    }

    @Test
    void updatesExpense() throws Exception {
        when(expenseService.update(eq(1L), any(Expense.class)))
                .thenAnswer(invocation -> {
                    Expense expense = invocation.getArgument(1);
                    expense.setId(invocation.getArgument(0));
                    return expense;
                });
        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated book","amount":3200,"expenseDate":"2026-10-02"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated book"))
                .andExpect(jsonPath("$.amount").value(3200));
        verify(expenseService).update(eq(1L), any(Expense.class));
    }

    @Test
    void returnsNotFoundForMissingExpense() throws Exception {
        when(expenseService.update(eq(999L), any(Expense.class)))
                .thenThrow(new ExpenseNotFoundException(999L));
        mockMvc.perform(put("/api/expenses/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Book","amount":1,"expenseDate":"2026-10-02"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().string("指定されたIDの支出が見つかりません: 999"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"amount\":1,\"expenseDate\":\"2026-10-02\"}",
            "{\"title\":\"\",\"amount\":1,\"expenseDate\":\"2026-10-02\"}",
            "{\"title\":\"   \",\"amount\":1,\"expenseDate\":\"2026-10-02\"}",
            "{\"title\":\"Book\",\"expenseDate\":\"2026-10-02\"}",
            "{\"title\":\"Book\",\"amount\":0,\"expenseDate\":\"2026-10-02\"}",
            "{\"title\":\"Book\",\"amount\":-1,\"expenseDate\":\"2026-10-02\"}",
            "{\"title\":\"Book\",\"amount\":1}"
    })
    void returnsBadRequestFromUpdateService(String body) throws Exception {
        when(expenseService.update(eq(1L), any(Expense.class)))
                .thenThrow(new ExpenseArgumentNotValidException("入力が不正です"));
        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("入力が不正です"));
        verify(expenseService).update(eq(1L), any(Expense.class));
    }

    @Test
    void deletesExpense() throws Exception {
        mockMvc.perform(delete("/api/expenses/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(expenseService).deleteById(1L);
    }

    @Test
    void returnsNotFoundWhenDeletingMissingExpense() throws Exception {
        doThrow(new ExpenseNotFoundException(999L)).when(expenseService).deleteById(999L);
        mockMvc.perform(delete("/api/expenses/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("指定されたIDの支出が見つかりません: 999"));
    }

    @Test
    void returnsNotFoundWhenReadingMissingExpense() throws Exception {
        when(expenseService.findById(999L)).thenThrow(new ExpenseNotFoundException(999L));
        mockMvc.perform(get("/api/expenses/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("指定されたIDの支出が見つかりません: 999"));
    }
}
