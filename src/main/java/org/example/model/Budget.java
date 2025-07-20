package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Budget {
    private int id;
    private int familyId;
    private int categoryId;
    private BigDecimal limitAmount;
    private String period; // "monthly", "weekly", "yearly"
}
