package com.cosmeticshop.cosmetic.Config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cosmeticshop.cosmetic.Entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * JwtUtil - Công cụ xử lý JWT Token
 * 
 * Chức năng:
 * 1. Tạo JWT token từ thông tin user
 * 2. Xác thực token có hợp lệ không
 * 3. Trích xuất thông tin từ token (username, role, etc.)
 */
@Component
public class JwtUtil {

    // Secret key để mã hóa/giải mã token - lấy từ application.properties
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    // Thời gian hết hạn token (milliseconds) - 1 giờ
    @Value("${jwt.expiration:3600000}")
    private long EXPIRATION_TIME;

    /**
     * Tạo Secret Key từ chuỗi SECRET_KEY
     * Sử dụng HMAC-SHA algorithm để mã hóa
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Tạo JWT Token từ thông tin User
     * 
     * Token chứa:
     * - subject: username
     * - claims: role, email (custom data)
     * - issuedAt: thời gian tạo
     * - expiration: thời gian hết hạn
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().toString());
        claims.put("email", user.getEmail());
        claims.put("userId", user.getId());

        return Jwts.builder()
                .claims(claims)                              // Thêm custom data
                .subject(user.getUsername())                 // Username làm subject
                .issuedAt(new Date())                        // Thời gian tạo
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Thời gian hết hạn
                .signWith(getSigningKey())                   // Ký token bằng secret key
                .compact();                                  // Build thành chuỗi JWT
    }

    /**
     * Trích xuất tất cả Claims (dữ liệu) từ token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // Xác thực signature
                .build()
                .parseSignedClaims(token)     // Parse token
                .getPayload();                // Lấy payload (claims)
    }

    /**
     * Lấy username từ token
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Lấy role từ token
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Lấy user ID từ token
     */
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    /**
     * Kiểm tra token có hết hạn chưa
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * Xác thực token
     * 
     * Token hợp lệ khi:
     * 1. Username trong token khớp với username được cung cấp
     * 2. Token chưa hết hạn
     */
    public boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = extractUsername(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            // Token không hợp lệ (sai format, signature, etc.)
            return false;
        }
    }

    /**
     * Kiểm tra token có hợp lệ không (không cần username)
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
