package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reminder {
    private int id;
    private int familyId;
    private String title;
    private String description;
    private LocalDate dueDate;
    private boolean isCompleted;
    private String type; // "payment", "budget", "goal", etc.
}
