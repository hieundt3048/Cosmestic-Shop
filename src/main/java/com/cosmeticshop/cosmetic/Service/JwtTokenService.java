package com.cosmeticshop.cosmetic.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Single Responsibility: CHỈ quản lý JWT token operations
 */
@Service
public class JwtTokenService implements ITokenService {

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

    @Override
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

    @Override
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    @Override
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    @Override
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    @Override
    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    /**
     * Kiểm tra token có hết hạn chưa
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    @Override
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
