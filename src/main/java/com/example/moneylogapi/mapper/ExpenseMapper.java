package com.example.moneylogapi.mapper;

import com.example.moneylogapi.model.Expense;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExpenseMapper {

    @Select("SELECT id, title, amount, category, payment_method, expense_date, memo FROM expenses ORDER BY id")
    List<Expense> findAll();

    @Select("SELECT id, title, amount, category, payment_method, expense_date, memo FROM expenses WHERE id = #{id}")
    Expense findById(Long id);

    @Insert("""
            INSERT INTO expenses
            (title, amount, category, payment_method, expense_date, memo)
            VALUES
            (#{title}, #{amount}, #{category}, #{paymentMethod}, #{expenseDate}, #{memo})""")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Expense expense);

    @Update("""
            UPDATE expenses
            SET title = #{title},
                amount = #{amount},
                category = #{category},
                payment_method = #{paymentMethod},
                expense_date = #{expenseDate},
                memo = #{memo}
            WHERE id = #{id}
            """)
    int update(Expense expense);

    @Delete("DELETE FROM expenses WHERE id = #{id}")
    int deleteById(Long id);
}
