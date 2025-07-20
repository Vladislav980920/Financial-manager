package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private int id;
    private int familyId;
    private int categoryId;
    private BigDecimal amount;
    private String type; // "income" or "expense"
    private String description;
    private LocalDate date;
    private int userId; // who added the transaction
}
