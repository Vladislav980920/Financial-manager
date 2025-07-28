package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reminder {
    @PositiveOrZero(message = "ID must be positive or zero")
    private int id;

    @Positive(message = "Family ID must be positive")
    private int familyId;

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @NotNull(message = "Due date cannot be null")
    @FutureOrPresent(message = "Due date must be in the future or present")
    private LocalDate dueDate;

    private boolean isCompleted;

    @NotBlank(message = "Type cannot be blank")
    @Pattern(regexp = "^(payment|event|other)$", message = "Type must be payment, event or other")
    private String type;
}