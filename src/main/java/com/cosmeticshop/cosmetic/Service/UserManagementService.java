package com.cosmeticshop.cosmetic.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;
import com.cosmeticshop.cosmetic.Dto.UpdateUserRequest;
import com.cosmeticshop.cosmetic.Dto.UserListItemResponse;
import com.cosmeticshop.cosmetic.Entity.User;
import com.cosmeticshop.cosmetic.Repository.UserRepository;

import jakarta.transaction.Transactional;

/**
 * Single Responsibility: CHỈ quản lý CRUD operations cho User
 */
@Service
public class UserManagementService implements IUserManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserManagementService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IUserValidationService validationService;
    private final LoginAttemptService loginAttemptService;
    private final IAuditLogService auditLogService;
    
    public UserManagementService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        IUserValidationService validationService,
        LoginAttemptService loginAttemptService,
        IAuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
        this.loginAttemptService = loginAttemptService;
        this.auditLogService = auditLogService;
    }
    
    /**
     * Tạo tài khoản mới (dùng cho luồng đăng ký/chung).
     * - Validate dữ liệu đầu vào qua validationService
     * - Mã hóa mật khẩu trước khi lưu DB
     * - Gán role mặc định CUSTOMER nếu request không truyền role
     */
    @Override
    @Transactional
    public User createUser(CreateUserRequest request) {
        logger.info("Creating new user: {}", request.getUsername());
        
        validationService.validateUserRegistration(request);
        
        // Chỉ lo việc tạo user entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : User.Role.CUSTOMER);
        user.setStatus(User.Status.ACTIVE);
        user.setStatusReason(null);
        user.setStatusUpdatedAt(LocalDateTime.now());
        user.setStatusUpdatedBy("system");
        
        User savedUser = userRepository.save(user);
        logger.info("User '{}' created successfully with ID: {}", savedUser.getUsername(), savedUser.getId());
        
        return savedUser;
    }

    /**
     * Tạo tài khoản nhân viên.
     * Service ép cứng role = EMPLOYEE để đảm bảo đúng nghiệp vụ.
     */
    @Override
    @Transactional
    public UserListItemResponse createEmployee(CreateUserRequest request) {
        request.setRole(User.Role.EMPLOYEE);
        User createdUser = createUser(request);
        auditLogService.logAction(
                "CREATE_EMPLOYEE",
                "user#" + createdUser.getId(),
                "Tạo tài khoản nhân viên: " + createdUser.getUsername());
        return toUserListItemResponse(createdUser);
    }

    /**
     * Cập nhật thông tin cơ bản của user theo ID.
     * Lưu ý: method này đang set trực tiếp password từ request.
     */
    @Override
    public User updateUser(Long id, UpdateUserRequest request){
        User user = userRepository.findById(id)
        .orElseThrow(() -> {
                logger.warn("User with ID '{}' not found", id);
                return new RuntimeException("User không tồn tại với ID: " + id);
            });

        String oldFullName = user.getFullName();
        String oldEmail = user.getEmail();
        
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());

        
        User savedUser = userRepository.save(user);
        auditLogService.logAction(
                "UPDATE_USER_PROFILE",
                "user#" + savedUser.getId(),
                String.format("Cập nhật hồ sơ user '%s' (fullName: %s -> %s, email: %s -> %s)",
                        savedUser.getUsername(),
                        oldFullName,
                        savedUser.getFullName(),
                        oldEmail,
                        savedUser.getEmail()));
        return savedUser;
    }

    /**
     * Xóa tài khoản theo ID.
     * - Kiểm tra tồn tại trước khi xóa để trả thông báo rõ ràng.
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User with ID {} not found", id);
                    return new RuntimeException("User không tồn tại với ID: " + id);
                });
        
        userRepository.deleteById(id);
        auditLogService.logAction(
                "DELETE_USER",
                "user#" + id,
                "Xóa tài khoản: " + user.getUsername());
        logger.info("User with ID {} deleted successfully", id);
    }
    
    /**
     * Danh sách toàn bộ tài khoản cho màn hình quản trị.
     * Trả về DTO đã được chuẩn hóa để FE dùng trực tiếp.
     */
    @Override
    public List<UserListItemResponse> getAllUsers() {
        logger.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::toUserListItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin hiển thị 1 tài khoản theo ID.
     */
    @Override
    public UserListItemResponse getUserSummaryById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User with ID {} not found", id);
                    return new RuntimeException("User không tồn tại với ID: " + id);
                });
        return toUserListItemResponse(user);
    }

    /**
     * Cập nhật vai trò (ADMIN/EMPLOYEE/CUSTOMER) cho tài khoản.
     */
    @Override
    @Transactional
    public UserListItemResponse updateUserRole(Long id, User.Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User with ID {} not found", id);
                    return new RuntimeException("User không tồn tại với ID: " + id);
                });

        User.Role oldRole = user.getRole();
        user.setRole(role);
        User savedUser = userRepository.save(user);
        auditLogService.logAction(
                "UPDATE_USER_ROLE",
                "user#" + savedUser.getId(),
                String.format("Đổi vai trò: %s -> %s", oldRole, role));
        logger.info("Updated role for user '{}' to {}", savedUser.getUsername(), role);
        return toUserListItemResponse(savedUser);
    }

    /**
     * Khóa/Mở khóa tài khoản cho nghiệp vụ quản trị.
     * - accountLocked = true  -> lock user
     * - accountLocked = false -> unblock user
     */
    @Override
    public UserListItemResponse updateUserLockStatus(Long id, boolean accountLocked) {
        if (accountLocked) {
            return updateUserStatus(id, User.Status.LOCKED_MANUAL, "Khóa thủ công bởi quản trị viên");
        }
        return updateUserStatus(id, User.Status.ACTIVE, "Mở khóa thủ công bởi quản trị viên");
    }

    @Override
    @Transactional
    public UserListItemResponse updateUserStatus(Long id, User.Status status, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User with ID {} not found", id);
                    return new RuntimeException("User không tồn tại với ID: " + id);
                });

        User.Status oldStatus = resolveStoredStatus(user);
        String normalizedReason = reason == null ? null : reason.trim();
        if (normalizedReason != null && normalizedReason.isEmpty()) {
            normalizedReason = null;
        }

        user.setStatus(status);
        user.setStatusReason(status == User.Status.ACTIVE ? null : normalizedReason);
        user.setStatusUpdatedAt(LocalDateTime.now());
        user.setStatusUpdatedBy(getCurrentActorUsername());

        if (status == User.Status.ACTIVE) {
            loginAttemptService.unblock(user.getUsername());
        } else {
            loginAttemptService.lockUser(user.getUsername());
        }

        User savedUser = userRepository.save(user);
        auditLogService.logAction(
                "UPDATE_USER_STATUS",
                "user#" + savedUser.getId(),
                String.format("Đổi trạng thái: %s -> %s. Lý do: %s",
                        oldStatus,
                        status,
                        normalizedReason == null ? "N/A" : normalizedReason));

        logger.info("Updated status for user '{}' from {} to {}", savedUser.getUsername(), oldStatus, status);
        return toUserListItemResponse(savedUser);
    }
    
    /**
     * Lấy User entity theo ID (dùng cho các nghiệp vụ nội bộ khác).
     */
    @Override
    public User getUserById(Long id) {
        logger.debug("Fetching user by ID: {}", id);
        return userRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("User with ID {} not found", id);
                return new RuntimeException("User không tồn tại với ID: " + id);
            });
    }
    
    /**
     * Lấy User entity theo username.
     */
    @Override
    public User getUserByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);
        return userRepository.findByUsername(username)
            .orElseThrow(() -> {
                logger.warn("User with username '{}' not found", username);
                return new RuntimeException("User không tồn tại với username: " + username);
            });
    }

    /**
     * Danh sách khách hàng để FE hiển thị theo vai trò.
     */
    @Override
    public List<UserListItemResponse> getCustomers() {
        logger.debug("Fetching users with role CUSTOMER");
        return userRepository.findByRole(User.Role.CUSTOMER).stream()
                .map(this::toUserListItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * Danh sách nhân viên để FE hiển thị theo vai trò.
     */
    @Override
    public List<UserListItemResponse> getEmployees() {
        logger.debug("Fetching users with role EMPLOYEE");
        return userRepository.findByRole(User.Role.EMPLOYEE).stream()
                .map(this::toUserListItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mapper từ User entity -> UserListItemResponse.
     * Tại đây đồng thời suy ra trạng thái hoạt động cho UI:
     * - accountLocked = true  -> status LOCKED
     * - accountLocked = false -> status ACTIVE
     */
    private UserListItemResponse toUserListItemResponse(User user) {
        int totalOrders = user.getOrders() == null ? 0 : user.getOrders().size();
        User.Status storedStatus = resolveStoredStatus(user);
        boolean tempLocked = loginAttemptService.isBlocked(user.getUsername());
        boolean accountLocked = storedStatus != User.Status.ACTIVE || tempLocked;
        String status = storedStatus == User.Status.ACTIVE && tempLocked ? "TEMP_LOCKED" : storedStatus.name();
        String statusReason = user.getStatusReason();
        if (storedStatus == User.Status.ACTIVE && tempLocked && (statusReason == null || statusReason.isBlank())) {
            statusReason = "Tạm khóa do đăng nhập sai quá nhiều lần";
        }

        return new UserListItemResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                accountLocked,
                status,
                statusReason,
                user.getStatusUpdatedAt(),
                user.getStatusUpdatedBy(),
                totalOrders);
    }

    private User.Status resolveStoredStatus(User user) {
        return user.getStatus() == null ? User.Status.ACTIVE : user.getStatus();
    }

    private String getCurrentActorUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        String name = authentication.getName();
        return (name == null || name.isBlank()) ? "system" : name;
    }
}
