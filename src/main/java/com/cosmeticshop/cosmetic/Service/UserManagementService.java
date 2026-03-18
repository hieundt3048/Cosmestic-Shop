package com.cosmeticshop.cosmetic.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.ChangeMyPasswordRequest;
import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;
import com.cosmeticshop.cosmetic.Dto.CustomerPurchaseHistoryResponse;
import com.cosmeticshop.cosmetic.Dto.UpdateMyProfileRequest;
import com.cosmeticshop.cosmetic.Dto.UpdateUserRequest;
import com.cosmeticshop.cosmetic.Dto.UserListItemResponse;
import com.cosmeticshop.cosmetic.Entity.Order;
import com.cosmeticshop.cosmetic.Entity.User;
import com.cosmeticshop.cosmetic.Repository.OrderRepository;
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
    private final OrderRepository orderRepository;
    
    public UserManagementService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        IUserValidationService validationService,
        LoginAttemptService loginAttemptService,
        IAuditLogService auditLogService,
        OrderRepository orderRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
        this.loginAttemptService = loginAttemptService;
        this.auditLogService = auditLogService;
        this.orderRepository = orderRepository;
    }
    
    /**
     * Tạo tài khoản mới cho luồng đăng ký công khai.
     * - Validate dữ liệu đầu vào qua validationService
     * - Mã hóa mật khẩu trước khi lưu DB
     * - Luôn gán role CUSTOMER (không nhận role từ client)
     */
    @Override
    @Transactional
    public User createUser(CreateUserRequest request) {
        return createUserWithRole(request, User.Role.CUSTOMER);
    }

    /**
     * Tạo tài khoản nhân viên.
     * Service ép cứng role = EMPLOYEE để đảm bảo đúng nghiệp vụ.
     */
    @Override
    @Transactional
    public UserListItemResponse createEmployee(CreateUserRequest request) {
        User createdUser = createUserWithRole(request, User.Role.EMPLOYEE);
        auditLogService.logAction(
                "CREATE_EMPLOYEE",
                "user#" + createdUser.getId(),
                "Tạo tài khoản nhân viên: " + createdUser.getUsername());
        return toUserListItemResponse(createdUser);
    }

    private User createUserWithRole(CreateUserRequest request, User.Role targetRole) {
        logger.info("Creating new user: {} with role {}", request.getUsername(), targetRole);

        validationService.validateUserRegistration(request);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(targetRole == null ? User.Role.CUSTOMER : targetRole);
        user.setStatus(User.Status.ACTIVE);
        user.setStatusReason(null);
        user.setStatusUpdatedAt(LocalDateTime.now());
        user.setStatusUpdatedBy("system");

        User savedUser = userRepository.save(user);
        logger.info("User '{}' created successfully with ID: {}", savedUser.getUsername(), savedUser.getId());

        return savedUser;
    }

    /**
     * Cập nhật thông tin cơ bản của user theo ID.
        * Nếu request có password mới thì sẽ mã hóa trước khi lưu.
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
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
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

    @Override
    @Transactional
    public UserListItemResponse updateMyProfile(String username, UpdateMyProfileRequest request) {
        User user = getUserByUsername(username);

        String normalizedEmail = request.getEmail() == null ? "" : request.getEmail().trim();
        String normalizedPhone = request.getPhone() == null ? "" : request.getPhone().trim();

        // Email chỉ kiểm tra trùng khi người dùng có gửi giá trị mới.
        if (!normalizedEmail.isEmpty()) {
            Optional<User> duplicateEmail = userRepository.findByEmail(normalizedEmail);
            if (duplicateEmail.isPresent() && !duplicateEmail.get().getId().equals(user.getId())) {
                throw new RuntimeException("Email đã được sử dụng bởi tài khoản khác");
            }
        }

        if (normalizedPhone.isEmpty()) {
            throw new RuntimeException("Số điện thoại không được để trống");
        }

        Optional<User> duplicatePhone = userRepository.findByPhone(normalizedPhone);
        if (duplicatePhone.isPresent() && !duplicatePhone.get().getId().equals(user.getId())) {
            throw new RuntimeException("Số điện thoại đã được sử dụng bởi tài khoản khác");
        }

        String oldFullName = user.getFullName();
        String oldEmail = user.getEmail();
        String oldPhone = user.getPhone();
        String nextFullName = request.getFullName() == null ? "" : request.getFullName().trim();

        // Cho phép cập nhật từng phần: field để trống thì giữ nguyên giá trị cũ.
        user.setFullName(nextFullName.isEmpty() ? user.getFullName() : nextFullName);
        user.setEmail(normalizedEmail.isEmpty() ? user.getEmail() : normalizedEmail);
        user.setPhone(normalizedPhone);

        User savedUser = userRepository.save(user);
        auditLogService.logAction(
                "USER_UPDATE_MY_PROFILE",
                "user#" + savedUser.getId(),
                String.format("Cap nhat ho so ca nhan (fullName: %s -> %s, email: %s -> %s, phone: %s -> %s)",
                        oldFullName,
                        savedUser.getFullName(),
                        oldEmail,
                        savedUser.getEmail(),
                        oldPhone,
                        savedUser.getPhone()));

        return toUserListItemResponse(savedUser);
    }

    @Override
    public UserListItemResponse getMyProfile(String username) {
        return toUserListItemResponse(getUserByUsername(username));
    }

    @Override
    @Transactional
    public void changeMyPassword(String username, ChangeMyPasswordRequest request) {
        User user = getUserByUsername(username);

        // Bắt buộc xác thực mật khẩu hiện tại để tránh đổi mật khẩu trái phép.
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác");
        }

        // Bảo vệ UX: yêu cầu nhập lại mật khẩu mới để giảm lỗi gõ nhầm.
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Xác nhận mật khẩu mới không khớp");
        }

        // Không cho phép dùng lại mật khẩu cũ ngay lập tức.
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditLogService.logAction(
                "USER_CHANGE_MY_PASSWORD",
                "user#" + user.getId(),
                "Nguoi dung tu doi mat khau tai khoan");
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

    @Override
    public CustomerPurchaseHistoryResponse getCustomerPurchaseHistory(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User khong ton tai voi ID: " + customerId));

        if (customer.getRole() != User.Role.CUSTOMER) {
            throw new RuntimeException("User voi ID " + customerId + " khong phai khach hang");
        }

        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDescIdDesc(customerId);
        // Chuyển đổi danh sách order sang DTO gọn để FE hiển thị lịch sử mua hàng.
        List<CustomerPurchaseHistoryResponse.OrderHistoryItem> orderItems = orders.stream()
                .map(this::toCustomerOrderHistoryItem)
                .collect(Collectors.toList());

        auditLogService.logAction(
                "LOOKUP_CUSTOMER_PURCHASE_HISTORY",
                "user#" + customerId,
                "Tra cuu lich su mua hang khach: " + customer.getUsername());

        return new CustomerPurchaseHistoryResponse(
                customer.getId(),
                customer.getUsername(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                orderItems.size(),
                orderItems);
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

        // accountLocked dùng cho UI tổng quan: khóa khi bị khóa cứng hoặc đang bị khóa tạm.
        boolean accountLocked = storedStatus != User.Status.ACTIVE || tempLocked;

        // Phân biệt TEMP_LOCKED để FE hiển thị rõ trường hợp bị khóa tạm do đăng nhập sai.
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

    private CustomerPurchaseHistoryResponse.OrderHistoryItem toCustomerOrderHistoryItem(Order order) {
        int totalItems = order.getOrderItems() == null
                ? 0
                : order.getOrderItems().stream()
                        .filter(item -> item != null)
                        .mapToInt(item -> {
                            Integer quantity = item.getQuantity();
                            return quantity == null ? 0 : quantity;
                        })
                        .sum();

        return new CustomerPurchaseHistoryResponse.OrderHistoryItem(
                order.getId(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus() == null ? "UNKNOWN" : order.getStatus().name(),
                order.getShippingAddress(),
                totalItems);
    }

    private User.Status resolveStoredStatus(User user) {
        // Dữ liệu cũ có thể null status, fallback ACTIVE để không vỡ luồng hiển thị/phân quyền.
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
