package com.example.mytelegramauthentication.core.service;

import com.example.mytelegramauthentication.core.model.TelegramUser;
import com.example.mytelegramauthentication.core.repository.ITelegramUserRepository;
import com.example.mytelegramauthentication.core.util.TelegramInitDataValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramUserService {
    private final ITelegramUserRepository repository;
    private final TelegramInitDataValidator validator;
    private final ObjectMapper objectMapper;

    public TelegramUser authenticateAndSaveUser(String initData) {
        if (!validator.checkData(initData)) {
            throw new IllegalArgumentException("Invalid Telegram initData");
        }

        Map<String, String> params = parseInitData(initData);
        String userJson = params.get("user");
        if (userJson == null) {
            throw new IllegalArgumentException("User data not found in initData");
        }

        try {
            Map<String, Object> userData = objectMapper.readValue(userJson, Map.class);
            TelegramUser user = TelegramUser.builder()
                    .id(Long.parseLong(userData.get("id").toString()))
                    .firstName(userData.get("firstName").toString())
                    .lastName(userData.get("lastName").toString())
                    .username(userData.get("username").toString())
                    .authDate(userData.get("authDate").toString())
                    .build();
            return repository.save(user);
        } catch (Exception e) {
            log.error("Ошибка при парсинге user данных", e);
            throw new RuntimeException("Failed to parse user data", e);
        }
    }

    private Map<String, String> parseInitData(String initData) {
        Map<String, String> params = new HashMap<>();
        for (String param : initData.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
        return params;
    }
}
