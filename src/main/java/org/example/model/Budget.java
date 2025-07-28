package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Budget {
    @PositiveOrZero(message = "ID must be positive or zero")
    private int id;

    @Positive(message = "Family ID must be positive")
    private int familyId;

    @Positive(message = "Category ID must be positive")
    private int categoryId;

    @NotNull(message = "Limit amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Limit amount must be greater than 0")
    private BigDecimal limitAmount;

    @NotBlank(message = "Period cannot be blank")
    @Pattern(regexp = "^(monthly|weekly|yearly)$", message = "Period must be monthly, weekly or yearly")
    private String period;
}