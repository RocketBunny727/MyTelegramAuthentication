package com.example.mytelegramauthentication.core.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
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
