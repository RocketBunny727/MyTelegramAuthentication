package com.example.mytelegramauthentication.core.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramUser {
    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String authDate;
}
