package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialGoal {
    @PositiveOrZero(message = "ID must be positive or zero")
    private int id;

    @Positive(message = "Family ID must be positive")
    private int familyId;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @NotNull(message = "Target amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Target amount must be greater than 0")
    private BigDecimal targetAmount;

    @NotNull(message = "Current amount cannot be null")
    @DecimalMin(value = "0.0", message = "Current amount must be positive or zero")
    private BigDecimal currentAmount;

    @NotNull(message = "Target date cannot be null")
    @FutureOrPresent(message = "Target date must be in the future or present")
    private LocalDate targetDate;

    @NotBlank(message = "Priority cannot be blank")
    @Pattern(regexp = "^(high|medium|low)$", message = "Priority must be high, medium or low")
    private String priority;
}