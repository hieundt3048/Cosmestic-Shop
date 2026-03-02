package com.cosmeticshop.cosmetic.Service;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;

/**
 * Cho phép thêm các quy tắc xác thực mới mà không cần sửa đổi code hiện có (OCP)
 */
public interface ValidationHandler {
    
    void validate(CreateUserRequest request);
    
    void setNext(ValidationHandler next);
}
