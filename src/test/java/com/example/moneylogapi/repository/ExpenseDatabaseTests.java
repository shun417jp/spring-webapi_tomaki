package com.example.moneylogapi.repository;

import com.example.moneylogapi.exception.ExpenseNotFoundException;
import com.example.moneylogapi.model.Expense;
import com.example.moneylogapi.service.ExpenseService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// 明示的に接続先を指定した場合だけ、専用スキーマで実行します。
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = ".+")
class ExpenseDatabaseTests {
    private static final String SCHEMA = "moneylog_test_" + UUID.randomUUID().toString().replace("-", "");
    private static boolean schemaCreated;

    @Autowired
    private ExpenseService expenseService;

    private static String username() {
        return System.getenv().getOrDefault("TEST_DB_USERNAME", "postgres");
    }

    private static String password() {
        return System.getenv().getOrDefault("TEST_DB_PASSWORD", "");
    }

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) throws Exception {
        try (var connection = DriverManager.getConnection(System.getenv("TEST_DB_URL"), username(), password());
             var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + SCHEMA);
            schemaCreated = true;
            connection.setSchema(SCHEMA);
            new ResourceDatabasePopulator(new FileSystemResource("db/schema.sql")).populate(connection);
        }
        registry.add("spring.datasource.url", () -> System.getenv("TEST_DB_URL"));
        registry.add("spring.datasource.username", ExpenseDatabaseTests::username);
        registry.add("spring.datasource.password", ExpenseDatabaseTests::password);
        registry.add("spring.datasource.hikari.schema", () -> SCHEMA);
    }

    @AfterAll
    static void removeTestSchema() throws Exception {
        if (schemaCreated) {
            try (var connection = DriverManager.getConnection(System.getenv("TEST_DB_URL"), username(), password());
                 var statement = connection.createStatement()) {
                statement.execute("DROP SCHEMA " + SCHEMA + " CASCADE");
            }
        }
    }

    @Test
    @Transactional
    void allFiveOperationsWorkWithPostgres() {
        assertTrue(expenseService.findAll().isEmpty());

        Expense expense = new Expense();
        expense.setTitle("DB動作確認");
        expense.setAmount(100);
        expense.setExpenseDate(LocalDate.of(2026, 10, 20));
        expense.setPaymentMethod("CASH");
        expense.setMemo("登録確認");
        Expense created = expenseService.create(expense);
        assertNotNull(created.getId());
        Expense saved = expenseService.findById(created.getId());
        assertEquals("OTHER", saved.getCategory());
        assertEquals("CASH", saved.getPaymentMethod());
        assertEquals(expense.getExpenseDate(), saved.getExpenseDate());
        assertEquals(1, expenseService.findAll().size());

        expense.setTitle("DB更新確認");
        expense.setAmount(200);
        expense.setCategory("EDUCATION");
        expense.setMemo(null);
        expenseService.update(created.getId(), expense);
        Expense updated = expenseService.findById(created.getId());
        assertEquals("DB更新確認", updated.getTitle());
        assertEquals(200, updated.getAmount());
        assertEquals("EDUCATION", updated.getCategory());
        assertNull(updated.getMemo());

        expenseService.deleteById(created.getId());
        assertTrue(expenseService.findAll().isEmpty());
        assertThrows(ExpenseNotFoundException.class, () -> expenseService.findById(created.getId()));
        assertThrows(ExpenseNotFoundException.class, () -> expenseService.update(created.getId(), expense));
        assertThrows(ExpenseNotFoundException.class, () -> expenseService.deleteById(created.getId()));
    }
}
