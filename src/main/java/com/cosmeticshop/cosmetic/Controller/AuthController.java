package com.cosmeticshop.cosmetic.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;
import com.cosmeticshop.cosmetic.Dto.LoginRequest;
import com.cosmeticshop.cosmetic.Dto.LoginResponse;
import com.cosmeticshop.cosmetic.Entity.User;
import com.cosmeticshop.cosmetic.Exception.TooManyRequestsException;
import com.cosmeticshop.cosmetic.Service.IAuthenticationService;
import com.cosmeticshop.cosmetic.Service.ITokenService;
import com.cosmeticshop.cosmetic.Service.IUserManagementService;
import com.cosmeticshop.cosmetic.Service.LoginAttemptService;

import jakarta.validation.Valid;

/**
 * Authentication Controller - Xử lý authentication requests
 * 
 * Responsibility: CHỈ handle authentication (login, register)
 * - Đăng ký user mới
 * - Đăng nhập và generate token
 * - Rate limiting cho login
 * 
 * KHÔNG handle user management (CRUD) - đó là việc của UserController
 * 
 * Tuân thủ Single Responsibility Principle (SRP)
 * Tuân thủ Dependency Inversion Principle (DIP) - phụ thuộc vào interface
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    // DIP: Phụ thuộc vào interface thay vì concrete class
    private final IAuthenticationService authenticationService;
    private final IUserManagementService userManagementService;
    private final ITokenService tokenService;
    private final LoginAttemptService loginAttemptService;

    public AuthController(
        IAuthenticationService authenticationService,
        IUserManagementService userManagementService,
        ITokenService tokenService,
        LoginAttemptService loginAttemptService
    ) {
        this.authenticationService = authenticationService;
        this.userManagementService = userManagementService;
        this.tokenService = tokenService;
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * Đăng ký user mới
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public User register(@RequestBody @Valid CreateUserRequest request) {
        logger.info("New user registration request: {}", request.getUsername());
        return userManagementService.createUser(request);
    }

    /**
     * Đăng nhập - Login endpoint với Rate Limiting
     * 
     * Flow:
     * 1. Validate request (username, password không empty)
     * 2. Check rate limiting - user có bị block không
     * 3. Authenticate user (check username/password với database)
     * 4. Generate JWT token từ user info
     * 5. Reset login attempts khi thành công
     * 6. Return token + user info trong response
     * 
     * Client sẽ lưu token và gửi kèm trong header cho các request sau
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // Bước 1: Validate request
        validationLoginRequest(request);
        
        String username = request.getUsername();

        // Bước 2: Check rate limiting - user có bị block không
        if (loginAttemptService.isBlocked(username)) {
            long blockedMinutes = loginAttemptService.getBlockedMinutesRemaining(username);
            logger.warn("Login attempt from blocked user '{}'. Blocked for {} more minutes", 
                       username, blockedMinutes);
            throw new TooManyRequestsException(
                String.format("Tài khoản đã bị khóa do đăng nhập sai quá nhiều lần. Vui lòng thử lại sau %d phút.", 
                             blockedMinutes),
                blockedMinutes
            );
        }

        try {
            // Bước 3: Authenticate user - SRP: delegate to AuthenticationService
            User user = authenticationService.authenticate(username, request.getPassword());

            // Bước 4: Generate JWT token - SRP: delegate to TokenService
            String token = tokenService.generateToken(user);

            // Bước 5: Reset login attempts khi login thành công
            loginAttemptService.loginSucceeded(username);

            // Bước 6: Tạo response với token, user info và message
            LoginResponse response = new LoginResponse(token, user, "Đăng nhập thành công");
            
            logger.info("User '{}' logged in successfully", username);
            
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Login thất bại - ghi nhận attempt
            loginAttemptService.loginFailed(username);
            
            int remainingAttempts = loginAttemptService.getRemainingAttempts(username);
            
            if (remainingAttempts > 0) {
                logger.warn("Login failed for user '{}'. {} attempts remaining", 
                           username, remainingAttempts);
                throw new RuntimeException(
                    String.format("%s. Còn %d lần thử.", e.getMessage(), remainingAttempts)
                );
            } else {
                logger.error("User '{}' has been blocked due to too many failed login attempts", username);
                throw new RuntimeException("Đăng nhập thất bại quá nhiều lần. Tài khoản đã bị khóa tạm thời.");
            }
        }
    }

    /**
     * Xác thực yêu cầu đăng nhập
     */
    private void validationLoginRequest(LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Tên đăng nhập không được để trống");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Mật khẩu không được để trống");
        }
    }
}
