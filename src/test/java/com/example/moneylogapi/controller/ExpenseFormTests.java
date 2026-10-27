package com.example.moneylogapi.controller;

import com.example.moneylogapi.exception.Handler.GlobalExceptionHandler;
import com.example.moneylogapi.mapper.ExpenseMapper;
import com.example.moneylogapi.model.Expense;
import com.example.moneylogapi.repository.ExpenseRepository;
import com.example.moneylogapi.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@Import({ExpenseService.class, ExpenseRepository.class, GlobalExceptionHandler.class})
class ExpenseFormTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseMapper expenseMapper;

    private void mockInsert() {
        when(expenseMapper.insert(any(Expense.class))).thenAnswer(invocation -> {
            Expense expense = invocation.getArgument(0);
            expense.setId(123L);
            return 1;
        });
    }

    @Test
    void registersExpenseFromMultipartForm() throws Exception {
        mockInsert();
        mockMvc.perform(multipart("/api/expenses")
                        .param("title", "Java参考書")
                        .param("amount", "2800")
                        .param("expenseDate", "2026-10-20")
                        .param("category", "EDUCATION")
                        .param("paymentMethod", "CREDIT_CARD")
                        .param("memo", "勉強用"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/expenses/123"))
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.title").value("Java参考書"))
                .andExpect(jsonPath("$.amount").value(2800))
                .andExpect(jsonPath("$.expenseDate").value("2026-10-20"))
                .andExpect(jsonPath("$.category").value("EDUCATION"))
                .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.memo").value("勉強用"));
        verify(expenseMapper).insert(any(Expense.class));
    }

    @Test
    void defaultsCategoryWhenOnlyRequiredFieldsAreSent() throws Exception {
        mockInsert();
        mockMvc.perform(multipart("/api/expenses")
                        .param("title", "昼食").param("amount", "800").param("expenseDate", "2026-10-20"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("OTHER"));
    }

    @Test
    void alsoSupportsUrlEncodedForm() throws Exception {
        mockInsert();
        mockMvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Book").param("amount", "100").param("expenseDate", "2026-10-20"))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.9", "1.0", "abc", "2147483648"})
    void rejectsInvalidFormAmount(String amount) throws Exception {
        mockMvc.perform(multipart("/api/expenses")
                        .param("title", "Book").param("amount", amount).param("expenseDate", "2026-10-20"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("入力形式が正しくありません。金額は1以上の整数、支出日はyyyy-MM-dd形式で入力してください。"));
        verifyNoInteractions(expenseMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-date", "2026-02-30"})
    void rejectsInvalidFormDate(String date) throws Exception {
        mockMvc.perform(multipart("/api/expenses")
                        .param("title", "Book").param("amount", "100").param("expenseDate", date))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(expenseMapper);
    }

    @Test
    void rejectsEmptyRequiredFields() throws Exception {
        mockMvc.perform(multipart("/api/expenses").param("title", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("タイトルを入力してください"));
        verifyNoInteractions(expenseMapper);
    }

    @Test
    void rejectsZeroAmount() throws Exception {
        mockMvc.perform(multipart("/api/expenses")
                        .param("title", "Book").param("amount", "0").param("expenseDate", "2026-10-20"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("金額は1以上にしてください"));
        verifyNoInteractions(expenseMapper);
    }

    @Test
    void frontendIsServed() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("index.html"));
        mockMvc.perform(get("/index.html")).andExpect(status().isOk());
        mockMvc.perform(get("/app.js")).andExpect(status().isOk());
        mockMvc.perform(get("/style.css")).andExpect(status().isOk());
    }
}
