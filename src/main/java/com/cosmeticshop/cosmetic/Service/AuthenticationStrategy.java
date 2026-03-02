package com.cosmeticshop.cosmetic.Service;

import com.cosmeticshop.cosmetic.Entity.User;

/**
 * Cho phép thêm các phương thức xác thực mới (OAuth2, LDAP, v.v.) mà không cần sửa đổi mã hiện có (OCP)
 */
public interface AuthenticationStrategy {

    User authenticate(Object credentials);
    
    boolean supports(String type);
}
