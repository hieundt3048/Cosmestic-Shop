package com.cosmeticshop.cosmetic.Service;

import com.cosmeticshop.cosmetic.Entity.User;

/**
 * Interface cho Authentication Service
 */
public interface IAuthenticationService {
    
    /**
     * Xác thực user bằng username và password
     */
    User authenticate(String username, String password);
}
