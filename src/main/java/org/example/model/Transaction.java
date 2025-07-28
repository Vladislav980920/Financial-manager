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
public class Transaction {
    @PositiveOrZero(message = "ID must be positive or zero")
    private int id;

    @Positive(message = "Family ID must be positive")
    private int familyId;

    @Positive(message = "Category ID must be positive")
    private int categoryId;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Type cannot be blank")
    @Pattern(regexp = "^(income|expense)$", message = "Type must be income or expense")
    private String type;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @NotNull(message = "Date cannot be null")
    @PastOrPresent(message = "Date must be in the past or present")
    private LocalDate date;

    @Positive(message = "User ID must be positive")
    private int userId;
}