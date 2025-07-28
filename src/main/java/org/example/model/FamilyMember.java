package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMember {
    @PositiveOrZero(message = "ID must be positive or zero")
    private int id;

    @Positive(message = "Family ID must be positive")
    private int familyId;

    @Positive(message = "User ID must be positive")
    private int userId;

    @NotBlank(message = "Role cannot be blank")
    @Pattern(regexp = "^(admin|member|child)$", message = "Role must be admin, member or child")
    private String role;
}