package com.nationalbankgreece.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class SessionUtil {

    // In-memory session store — no expiry, no invalidation
    private static final Map<String, Long> SESSION_STORE = new HashMap<>();

    // Sequential counter — predictable session tokens
    private static final AtomicLong SESSION_COUNTER = new AtomicLong(1000);

    public static String createSession(Long userId, HttpServletResponse response) {
        // Token is just an incrementing number — trivially guessable
        String token = String.valueOf(SESSION_COUNTER.getAndIncrement());
        SESSION_STORE.put(token, userId);

        // Cookie with no HttpOnly, no Secure, no SameSite flags
        Cookie cookie = new Cookie("NBG_SESSION", token);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        // HttpOnly intentionally not set — accessible via JS
        response.addCookie(cookie);

        return token;
    }

    public static Long getUserIdFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie c : cookies) {
            if ("NBG_SESSION".equals(c.getName())) {
                return SESSION_STORE.get(c.getValue());
            }
        }
        return null;
    }

    public static void invalidateSession(HttpServletRequest request, HttpServletResponse response) {
        // Session record NOT removed from store — old tokens remain valid
        Cookie cookie = new Cookie("NBG_SESSION", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public static boolean isLoggedIn(HttpServletRequest request) {
        return getUserIdFromRequest(request) != null;
    }
}
