package com.cosmeticshop.cosmetic.Service;

import com.cosmeticshop.cosmetic.Entity.User;

/**
 * Interface cho Authentication Service
 * Supports multiple authentication strategies (OCP)
 */
public interface IAuthenticationService {
    
    /**
     * Xác thực user bằng username và password (legacy method)
     */
    User authenticate(String username, String password);
    
    /**
     * Xác thực user với strategy pattern
     * @param type Authentication type ("username_password", "oauth2", etc.)
     * @param credentials Credentials object for the authentication type
     * @return Authenticated user
     */
    User authenticate(String type, Object credentials);
}
