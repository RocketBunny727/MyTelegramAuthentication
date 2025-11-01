package com.example.mytelegramauthentication.core.security.filter;

import com.example.mytelegramauthentication.core.model.TelegramUser;
import com.example.mytelegramauthentication.core.service.TelegramUserService;
import com.example.mytelegramauthentication.core.util.TelegramInitDataValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthFilter extends OncePerRequestFilter {

    private final TelegramUserService userService;
    private final TelegramInitDataValidator validator;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String userAgent = request.getHeader("user-agent");
        log.info("User-Agent: {}", userAgent);

        if (userAgent == null || !userAgent.toLowerCase().contains("telegram")) {
            log.info("Skip request (not from telegram client)");
            filterChain.doFilter(request, response);
            return;
        }

        log.info("Full URL of request: {}", request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
        log.info("HEADERS:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            log.info("Header: {} = {}", headerName, headerValue);
        }

        log.info("Full params of request:");
        String initData = request.getParameter("initData");
        if (initData != null && !initData.isEmpty()) {
            log.info("Parameter: initData = {}", initData);
            try {
                if (validator.checkData(initData)) {
                    TelegramUser user = userService.authenticateAndSaveUser(initData);
                    log.info("User authenticated: {}", user);
                    request.setAttribute("user", user);
                } else {
                    log.warn("Validation of initData failed");
                    request.setAttribute("errorMessage", "Invalid authentication data");
                }
            } catch (Exception e) {
                log.error("Error processing initData", e);
                request.setAttribute("errorMessage", "Error processing data");
            }
        } else {
            log.warn("initData not append at request");
        }

        filterChain.doFilter(request, response);
    }
}
