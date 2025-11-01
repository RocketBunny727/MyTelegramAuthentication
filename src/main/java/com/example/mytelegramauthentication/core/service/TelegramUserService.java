package com.example.mytelegramauthentication.core.service;

import com.example.mytelegramauthentication.core.model.TelegramUser;
import com.example.mytelegramauthentication.core.util.TelegramInitDataValidator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramUserService {

    private final TelegramInitDataValidator validator;
    private final ObjectMapper objectMapper;

    public TelegramUser authenticateAndSaveUser(String initData) {
        if (!validator.checkData(initData)) {
            throw new IllegalArgumentException("Invalid Telegram initData");
        }

        String[] params = initData.split("&");
        Map<String, String> paramMap = Arrays.stream(params)
                .map(param -> param.split("=", 2))
                .filter(arr -> arr.length == 2)
                .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));

        String userParam = paramMap.get("user");
        if (userParam == null) {
            throw new RuntimeException("User data not found in initData");
        }

        try {
            String decodedUserJson = URLDecoder.decode(userParam, StandardCharsets.UTF_8);
            log.info("Decoded user JSON: {}", decodedUserJson);

            Map<String, Object> userData = objectMapper.readValue(decodedUserJson, new TypeReference<>() {});
            log.info("Parsed user data: {}", userData);

            TelegramUser user = TelegramUser.builder()
                    .id(((Number) userData.get("id")).longValue())
                    .firstName((String) userData.get("first_name"))
                    .lastName((String) userData.get("last_name"))
                    .username((String) userData.get("username"))
                    .authDate(paramMap.get("auth_date"))
                    .build();

            log.info("User authenticated: {}", user);
            return user;
        } catch (Exception e) {
            log.error("Error parsing user details", e);
            throw new RuntimeException("Failed to parse user data", e);
        }
    }
}
