package com.example.moneylogapi.controller;

import com.example.moneylogapi.exception.Handler.GlobalExceptionHandler;
import com.example.moneylogapi.mapper.ExpenseMapper;
import com.example.moneylogapi.service.ExpenseService;
import com.example.moneylogapi.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@Import({ExpenseService.class, ExpenseRepository.class, GlobalExceptionHandler.class})
class ExpenseExceptionTests {
    @MockitoBean
    private ExpenseMapper expenseMapper;

    @Autowired
    private MockMvc mockMvc;

    static Stream<Arguments> invalidExpenses() {
        return Stream.of(
                Arguments.of("{\"amount\":100,\"expenseDate\":\"2026-10-09\"}", "タイトルを入力してください"),
                Arguments.of("{\"title\":\"\",\"amount\":100,\"expenseDate\":\"2026-10-09\"}", "タイトルを入力してください"),
                Arguments.of("{\"title\":\"   \",\"amount\":100,\"expenseDate\":\"2026-10-09\"}", "タイトルを入力してください"),
                Arguments.of("{\"title\":\"Book\",\"expenseDate\":\"2026-10-09\"}", "金額を入力してください"),
                Arguments.of("{\"title\":\"Book\",\"amount\":0,\"expenseDate\":\"2026-10-09\"}", "金額は1以上にしてください"),
                Arguments.of("{\"title\":\"Book\",\"amount\":-1,\"expenseDate\":\"2026-10-09\"}", "金額は1以上にしてください"),
                Arguments.of("{\"title\":\"Book\",\"amount\":100}", "支出日を入力してください")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidExpenses")
    void rejectsInvalidCreateAndUpdate(String body, String message) throws Exception {
        mockMvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(message));
        mockMvc.perform(put("/api/expenses/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(message));
        verifyNoInteractions(expenseMapper);
    }

    @Test
    void returns404ForMissingExpenseOnRead() throws Exception {
        when(expenseMapper.findById(999L)).thenReturn(null);
        mockMvc.perform(get("/api/expenses/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("指定されたIDの支出が見つかりません: 999"));
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
            "{\"title\":\"Book\",\"amount\":1.9,\"expenseDate\":\"2026-10-20\"}",
            "{\"title\":\"Book\",\"amount\":0.9,\"expenseDate\":\"2026-10-20\"}",
            "{\"title\":\"Book\",\"amount\":1.0,\"expenseDate\":\"2026-10-20\"}",
            "{\"title\":\"Book\",\"amount\":100,\"expenseDate\":\"invalid-date\"}",
            "{\"title\":\"Book\",\"amount\":100,\"expenseDate\":\"2026-02-30\"}",
            "{\"title\":",
            "null"
    })
    void rejectsInvalidJsonWithoutWritingToDatabase(String body) throws Exception {
        String message = "入力形式が正しくありません。金額は1以上の整数、支出日はyyyy-MM-dd形式で入力してください。";
        mockMvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(content().string(message));
        mockMvc.perform(put("/api/expenses/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(content().string(message));
        verifyNoInteractions(expenseMapper);
    }

    @Test
    void rejectsNonNumericId() throws Exception {
        mockMvc.perform(get("/api/expenses/abc"))
                .andExpect(status().isBadRequest()).andExpect(content().string("IDは整数で指定してください。"));
        verifyNoInteractions(expenseMapper);
    }

    @Test
    void returns404ForMissingExpenseOnUpdate() throws Exception {
        when(expenseMapper.update(any())).thenReturn(0);
        mockMvc.perform(put("/api/expenses/999").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Book","amount":100,"expenseDate":"2026-10-09"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().string("指定されたIDの支出が見つかりません: 999"));
    }

    @Test
    void returns404ForMissingExpenseOnDelete() throws Exception {
        when(expenseMapper.deleteById(999L)).thenReturn(0);
        mockMvc.perform(delete("/api/expenses/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("指定されたIDの支出が見つかりません: 999"));
    }
}
