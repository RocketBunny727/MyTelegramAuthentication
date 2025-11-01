package com.example.mytelegramauthentication.core.repository;

import com.example.mytelegramauthentication.core.model.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITelegramUserRepository extends JpaRepository<TelegramUser, Long> {
}
