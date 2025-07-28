package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @PositiveOrZero(message = "ID must be positive or zero")
    private int id;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Type cannot be blank")
    @Pattern(regexp = "^(income|expense)$", message = "Type must be income or expense")
    private String type;

    @PositiveOrZero(message = "Family ID must be positive or zero")
    private int familyId;
}