package com.cosmeticshop.cosmetic.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * LoginAttemptService - Quản lý và giới hạn số lần đăng nhập thất bại
 * Bảo vệ khỏi Brute Force Attack
 */
@Service
public class LoginAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);

    // Số lần login thất bại tối đa
    private static final int MAX_ATTEMPTS = 5;
    
    // Thời gian block (phút)
    private static final int BLOCK_DURATION_MINUTES = 15;

    // Lưu trữ login attempts: username -> LoginAttempt
    private final ConcurrentHashMap<String, LoginAttempt> attemptsCache = new ConcurrentHashMap<>();

    /**
     * Inner class để lưu thông tin login attempt
     */
    private static class LoginAttempt {
        private int attempts;
        private LocalDateTime firstAttempt;
        private LocalDateTime blockedUntil;

        public LoginAttempt() {
            this.attempts = 1;
            this.firstAttempt = LocalDateTime.now();
            this.blockedUntil = null;
        }

        public void incrementAttempts() {
            this.attempts++;
        }

        public void reset() {
            this.attempts = 0;
            this.firstAttempt = LocalDateTime.now();
            this.blockedUntil = null;
        }

        public void block() {
            this.blockedUntil = LocalDateTime.now().plusMinutes(BLOCK_DURATION_MINUTES);
        }

        public boolean isBlocked() {
            if (blockedUntil == null) {
                return false;
            }
            // Nếu đã hết thời gian block → reset
            if (LocalDateTime.now().isAfter(blockedUntil)) {
                reset();
                return false;
            }
            return true;
        }

        public int getAttempts() {
            return attempts;
        }

        public LocalDateTime getBlockedUntil() {
            return blockedUntil;
        }
    }

    /**
     * Kiểm tra username có bị block không
     */
    public boolean isBlocked(String username) {
        LoginAttempt attempt = attemptsCache.get(username);
        if (attempt == null) {
            return false;
        }
        return attempt.isBlocked();
    }

    /**
     * Ghi nhận login thất bại
     */
    public void loginFailed(String username) {
        LoginAttempt attempt = attemptsCache.computeIfAbsent(
            username, 
            k -> new LoginAttempt()
        );

        attempt.incrementAttempts();

        logger.warn("Login failed for user '{}'. Attempt {}/{}", 
                    username, attempt.getAttempts(), MAX_ATTEMPTS);

        // Nếu vượt quá số lần cho phép → block
        if (attempt.getAttempts() >= MAX_ATTEMPTS) {
            attempt.block();
            logger.error("User '{}' has been blocked for {} minutes due to {} failed login attempts", 
                        username, BLOCK_DURATION_MINUTES, attempt.getAttempts());
        }
    }

    /**
     * Reset login attempts khi login thành công
     */
    public void loginSucceeded(String username) {
        LoginAttempt attempt = attemptsCache.get(username);
        if (attempt != null) {
            if (attempt.getAttempts() > 0) {
                logger.info("User '{}' logged in successfully. Resetting failed attempts counter.", username);
            }
            attemptsCache.remove(username);
        }
    }

    /**
     * Lấy số lần login thất bại còn lại
     */
    public int getRemainingAttempts(String username) {
        LoginAttempt attempt = attemptsCache.get(username);
        if (attempt == null) {
            return MAX_ATTEMPTS;
        }
        return Math.max(0, MAX_ATTEMPTS - attempt.getAttempts());
    }

    /**
     * Lấy thời gian còn lại của block (phút)
     */
    public long getBlockedMinutesRemaining(String username) {
        LoginAttempt attempt = attemptsCache.get(username);
        if (attempt == null || !attempt.isBlocked()) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        return java.time.Duration.between(now, attempt.getBlockedUntil()).toMinutes();
    }

    /**
     * Manual unblock user (cho admin)
     */
    public void unblock(String username) {
        attemptsCache.remove(username);
        logger.info("User '{}' has been manually unblocked", username);
    }
}
