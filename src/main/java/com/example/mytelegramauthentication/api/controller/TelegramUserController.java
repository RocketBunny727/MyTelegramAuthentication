package com.example.mytelegramauthentication.api.controller;

import com.example.mytelegramauthentication.core.model.TelegramUser;
import com.example.mytelegramauthentication.core.service.TelegramUserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TelegramUserController {

    private final TelegramUserService userService;

    @GetMapping("/")
    public String index(HttpServletRequest request, Model model) {
        TelegramUser user = (TelegramUser) request.getAttribute("user");
        String errorMessage = (String) request.getAttribute("errorMessage");

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("telegramInitData".equals(cookie.getName())) {
                    log.info("Found cookie telegramInitData: {}", cookie.getValue());
                }
            }
        }

        if (errorMessage != null) {
            log.warn("Error: {}", errorMessage);
            model.addAttribute("errorMessage", errorMessage);
            return "loading";
        }

        if (user != null) {
            log.info("User details: {}", user);
            model.addAttribute("user", user);
            return "index";
        }

        log.warn("No user found");
        return "loading";
    }

    @PostMapping("/auth")
    public String authenticate(@RequestParam("initData") String initData, Model model) {
        log.info("Catch POST /auth with initData: {}", initData);
        try {
            TelegramUser user = userService.authenticateAndSaveUser(initData);
            log.info("User authenticated: {}", user);
            model.addAttribute("user", user);
            return "index";
        } catch (IllegalArgumentException e) {
            log.error("Error validation initData: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        } catch (Exception e) {
            log.error("Unexpected error", e);
            model.addAttribute("errorMessage", "Ошибка обработки данных");
            return "error";
        }
    }
}
