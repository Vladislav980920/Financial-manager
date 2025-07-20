package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMember {
    private int id;
    private int familyId;
    private int userId;
    private String role; // "head", "member", etc.
}
