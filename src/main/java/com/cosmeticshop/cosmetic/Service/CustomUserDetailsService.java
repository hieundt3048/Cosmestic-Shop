package com.cosmeticshop.cosmetic.Service;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Entity.User;
import com.cosmeticshop.cosmetic.Repository.UserRepository;

/**
 * CustomUserDetailsService - Service để Spring Security load user
 * 
 * Spring Security cần interface UserDetailsService để:
 * 1. Load user từ database bằng username
 * 2. Convert User entity thành UserDetails (format mà Spring Security hiểu)
 * 
 * UserDetails chứa:
 * - Username
 * - Password (đã mã hóa)
 * - Authorities (quyền hạn/roles)
 * - Account status (enabled, locked, expired, etc.)
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load user từ database bằng username
     * Method này được gọi bởi Spring Security khi xác thực
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm user trong database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User không tồn tại: " + username));

        // Convert User entity thành UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),           // Username
                user.getPassword(),           // Password (đã mã hóa bằng BCrypt)
                getAuthorities(user)          // Authorities (roles)
        );
    }

    /**
     * Convert Role của User entity thành Authorities cho Spring Security
     * 
     * Spring Security sử dụng GrantedAuthority để kiểm tra quyền hạn
     * Format: "ROLE_" + role name (e.g., "ROLE_ADMIN", "ROLE_CUSTOMER")
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        // Tạo authority từ role
        // Ví dụ: user.getRole() = ADMIN → "ROLE_ADMIN"
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }
}
