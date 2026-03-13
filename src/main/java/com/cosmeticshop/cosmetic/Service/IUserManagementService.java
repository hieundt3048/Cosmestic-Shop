package com.cosmeticshop.cosmetic.Service;

import java.util.List;

import com.cosmeticshop.cosmetic.Dto.ChangeMyPasswordRequest;
import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;
import com.cosmeticshop.cosmetic.Dto.CustomerPurchaseHistoryResponse;
import com.cosmeticshop.cosmetic.Dto.UpdateMyProfileRequest;
import com.cosmeticshop.cosmetic.Dto.UpdateUserRequest;
import com.cosmeticshop.cosmetic.Dto.UserListItemResponse;
import com.cosmeticshop.cosmetic.Entity.User;

/**
 * Responsibility: CHỈ quản lý CRUD operations cho User
 */
public interface IUserManagementService {
    
    /**
     * Tạo user mới
     * Validation được xử lý bởi ValidationService
     */
    User createUser(CreateUserRequest request);

    /**
     * Tạo nhân viên mới
     */
    UserListItemResponse createEmployee(CreateUserRequest request);
    
    /**
     * Xóa user theo ID
     */
    void deleteUser(Long id);

    /**
     * Cập nhật user theo ID
     */
    User updateUser(Long id, UpdateUserRequest request);
    /**
     * Lấy danh sách tất cả users
     */
    List<UserListItemResponse> getAllUsers();

    /**
     * Lấy thông tin hiển thị của user theo ID
     */
    UserListItemResponse getUserSummaryById(Long id);

    /**
     * Cập nhật vai trò user
     */
    UserListItemResponse updateUserRole(Long id, User.Role role);

    /**
     * Cập nhật trạng thái khóa/mở khóa user
     */
    UserListItemResponse updateUserLockStatus(Long id, boolean accountLocked);

    /**
     * Cập nhật status tài khoản (ACTIVE/POLICY_VIOLATION/FRAUD_SUSPECTED/...)
     */
    UserListItemResponse updateUserStatus(Long id, User.Status status, String reason);

    /**
     * Nhân viên tự cập nhật hồ sơ cá nhân.
     */
    UserListItemResponse updateMyProfile(String username, UpdateMyProfileRequest request);

    /**
     * Nhân viên tự đổi mật khẩu.
     */
    void changeMyPassword(String username, ChangeMyPasswordRequest request);
    
    /**
     * Lấy user theo ID
     */
    User getUserById(Long id);
    
    /**
     * Lấy user theo username
     */
    User getUserByUsername(String username);

    /**
     * Lấy danh sách khách hàng (role CUSTOMER)
     */
    List<UserListItemResponse> getCustomers();

    /**
     * Lấy thông tin liên hệ + lịch sử mua hàng của 1 khách.
     */
    CustomerPurchaseHistoryResponse getCustomerPurchaseHistory(Long customerId);

    /**
     * Lấy danh sách nhân viên (role EMPLOYEE)
     */
    List<UserListItemResponse> getEmployees();
}
