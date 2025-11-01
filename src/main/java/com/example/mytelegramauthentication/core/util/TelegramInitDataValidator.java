package com.example.mytelegramauthentication.core.util;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramInitDataValidator {

    @Value("${telegram.bots.my_authentication_bot.token}")
    private String botToken;

    private static final String TG_WEB_APP_DATA = "WebAppData";
    private static final String HMAC_SHA256_ALGO = "HmacSHA256";

    public boolean checkData(String initData) {
        log.info("Validating initData: {}", initData);
        if (StringUtils.isBlank(initData)) {
            log.warn("initData is empty");
            return false;
        }

        String[] hashContainer = new String[1];
        List<String> sortedData = extractAndSortData(initData, hashContainer);

        if (hashContainer[0] == null) {
            log.warn("Hash not found in initData");
            return false;
        }

        byte[] secretKey = hmacH256(botToken.getBytes(StandardCharsets.UTF_8),
                TG_WEB_APP_DATA.getBytes(StandardCharsets.UTF_8));
        if (secretKey == null) {
            log.error("Failed to generate secret key");
            return false;
        }

        return validateHash(sortedData, hashContainer[0], secretKey);
    }

    private List<String> extractAndSortData(String initData, String[] hashContainer) {
        Map<String, String> dataMap = new HashMap<>();

        for (String pair : initData.split("&")) {
            int idx = pair.indexOf('=');
            if (idx == -1) continue;

            String key = pair.substring(0, idx);
            String value = pair.substring(idx + 1);

            if ("hash".equals(key)) {
                hashContainer[0] = value;
                continue;
            }

            key = URLDecoder.decode(key, StandardCharsets.UTF_8);
            value = URLDecoder.decode(value, StandardCharsets.UTF_8);

            if ("user".equals(key)) {
                value = URLDecoder.decode(value, StandardCharsets.UTF_8);
            }

            dataMap.put(key, value);
        }

        return dataMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.toList());
    }

    private boolean validateHash(List<String> sortedData, String originalHash, byte[] secretKey) {
        String dataCheckString = String.join("\n", sortedData);
        log.info("data-check-string: {}", dataCheckString);

        byte[] computedHash = hmacH256(dataCheckString.getBytes(StandardCharsets.UTF_8), secretKey);
        if (computedHash == null) {
            log.error("Failed to compute HMAC-SHA256");
            return false;
        }

        String computedHashString = bytesToHex(computedHash);
        log.info("Expected hash: {}, Computed hash: {}", originalHash, computedHashString);

        return originalHash.equalsIgnoreCase(computedHashString);
    }

    private byte[] hmacH256(byte[] data, byte[] key) {
        try {
            Mac sha256HMAC = Mac.getInstance(HMAC_SHA256_ALGO);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, HMAC_SHA256_ALGO);
            sha256HMAC.init(secretKeySpec);
            return sha256HMAC.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error computing HMAC-SHA256: {}", e.getMessage(), e);
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