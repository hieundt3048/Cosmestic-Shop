package com.cosmeticshop.cosmetic.Exception;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Security Exception Handler - Xử lý các exception liên quan đến authentication/authorization
 */
@RestControllerAdvice
public class SecurityExceptionHandler {

    /**
     * Xử lý exception khi authentication thất bại (sai username/password)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.UNAUTHORIZED.value());
        error.put("error", "Unauthorized");
        error.put("message", "Tên đăng nhập hoặc mật khẩu không đúng");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Xử lý exception khi vượt quá số lần login cho phép (429 Too Many Requests)
     */
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Map<String, Object>> handleTooManyRequests(TooManyRequestsException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        error.put("error", "Too Many Requests");
        error.put("message", ex.getMessage());
        error.put("blockedMinutes", ex.getBlockedMinutes());
        error.put("retryAfter", ex.getBlockedMinutes() * 60); // seconds
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    /**
     * Xử lý exception khi user không có quyền truy cập (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.FORBIDDEN.value());
        error.put("error", "Forbidden");
        error.put("message", "Bạn không có quyền truy cập tài nguyên này");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Xử lý các authentication exception khác
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.UNAUTHORIZED.value());
        error.put("error", "Unauthorized");
        error.put("message", "Xác thực thất bại: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Utility method để ghi JSON error response
     * Sử dụng trong filter (không thể dùng @ExceptionHandler)
     */
    public static void writeJsonError(
            HttpServletResponse response, 
            int status, 
            String message) throws IOException {
        
        String errorType = status == 401 ? "Unauthorized" : "Error";
        long timestamp = System.currentTimeMillis();
        
        // Tạo JSON response manually (không dùng ObjectMapper để tránh dependency issue)
        String jsonResponse = String.format(
            "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":%d}",
            status, errorType, message, timestamp
        );

        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(jsonResponse);
    }
}
