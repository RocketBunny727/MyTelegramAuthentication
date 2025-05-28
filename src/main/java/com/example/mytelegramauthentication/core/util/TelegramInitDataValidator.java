package com.example.mytelegramauthentication.core.util;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class TelegramInitDataValidator {
    private static final String HMAC_SHA256_ALGO = "HmacSHA256";
    private static final String TG_WEB_APP_DATA = "WebAppData";

    private final String botToken;

    public TelegramInitDataValidator(String botToken) {
        this.botToken = botToken;
    }

    public boolean checkData(String initData) {
        if (StringUtils.isBlank(initData)) {
            return false;
        }

        String parsedInitData = URLDecoder.decode(initData, StandardCharsets.UTF_8);
        String[] hashContainer = new String[1];
        List<String> sortedUrlDecoded = extractAndSortData(parsedInitData, hashContainer);

        if (hashContainer[0] == null) {
            return false;
        }

        byte[] secretKeyForData = hmacH256(botToken.getBytes(StandardCharsets.UTF_8), TG_WEB_APP_DATA.getBytes(StandardCharsets.UTF_8));
        if (secretKeyForData == null) {
            log.error("Невозможно сформировать ключ для подписи начальных данных пользователя Mini App");
            return false;
        }

        return validateHash(sortedUrlDecoded, hashContainer[0], secretKeyForData);
    }

    private boolean validateHash(List<String> sortedData, String originalHash, byte[] secretKey) {
        byte[] dataHash = hmacH256(String.join("\n", sortedData).getBytes(StandardCharsets.UTF_8), secretKey);
        if (dataHash == null) {
            return false;
        }

        String computedHashString = bytesToHex(dataHash);
        return Objects.equals(originalHash, computedHashString);
    }

    private List<String> extractAndSortData(String initData, String[] hashContainer) {
        return Arrays.stream(initData.split("&"))
                .map(s -> extractHash(s, hashContainer))
                .filter(s -> !s.startsWith("hash="))
                .map(s -> URLDecoder.decode(s, StandardCharsets.UTF_8))
                .sorted()
                .collect(Collectors.toList());
    }

    private String extractHash(String param, String[] hashContainer) {
        if (param.startsWith("hash=")) {
            hashContainer[0] = param.substring(5);
        }
        return param;
    }

    private byte[] hmacH256(byte[] data, byte[] key) {
        try {
            Mac sha256HMAC = Mac.getInstance(HMAC_SHA256_ALGO);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, HMAC_SHA256_ALGO);
            sha256HMAC.init(secretKeySpec);
            return sha256HMAC.doFinal(data);
        } catch (Exception e) {
            log.error("Ошибка при вычислении HMAC-SHA256", e);
            return null;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}