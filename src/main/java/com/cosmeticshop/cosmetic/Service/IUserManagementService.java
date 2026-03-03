package com.cosmeticshop.cosmetic.Service;

import java.util.List;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;
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
    List<User> getAllUsers();
    
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
     * Lấy danh sách nhân viên (role EMPLOYEE)
     */
    List<UserListItemResponse> getEmployees();
}
