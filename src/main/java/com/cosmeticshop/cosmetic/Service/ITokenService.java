package com.cosmeticshop.cosmetic.Service;

import com.cosmeticshop.cosmetic.Entity.User;

/**
 * Responsibility: CHỈ quản lý JWT token
 */
public interface ITokenService {
    
    /**
     * Tạo JWT token từ thông tin user
     */
    String generateToken(User user);
    
    /**
     * Validate token có hợp lệ và khớp với username không
     */
    boolean validateToken(String token, String username);
    
    /**
     * Trích xuất username từ token
     */
    String extractUsername(String token);
    
    /**
     * Trích xuất role từ token
     */
    String extractRole(String token);
    
    /**
     * Trích xuất user ID từ token
     */
    Long extractUserId(String token);
    
    /**
     * Trích xuất email từ token
     */
    String extractEmail(String token);
}
