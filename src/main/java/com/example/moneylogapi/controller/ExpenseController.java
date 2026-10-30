package com.example.moneylogapi.controller;

import com.example.moneylogapi.model.Expense;
import com.example.moneylogapi.service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ResponseEntity<List<Expense>> findAll() {
        List<Expense> expenses = expenseService.findAll();

        return new ResponseEntity<>(expenses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> findById(@PathVariable Long id) {
        Expense expense = expenseService.findById(id);

        return ResponseEntity.ok(expense);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Expense> create(@RequestBody Expense expense,
                                          UriComponentsBuilder uriBuilder){
        Expense createExpense = expenseService.create(expense);

        URI location = uriBuilder.path("/api/expenses/{id}")
                .buildAndExpand(createExpense.getId()).toUri();

        return ResponseEntity.created(location).body(createExpense);
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Expense> createFromForm(@ModelAttribute Expense expense,
                                                  UriComponentsBuilder uriBuilder){
        return create(expense, uriBuilder);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> update(
            @PathVariable Long id,
            @RequestBody Expense expense){
        Expense updatedExpense = expenseService.update(id, expense);

        return ResponseEntity.ok(updatedExpense);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        expenseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
