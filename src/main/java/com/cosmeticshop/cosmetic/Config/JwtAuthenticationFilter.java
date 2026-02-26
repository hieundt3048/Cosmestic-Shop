package com.cosmeticshop.cosmetic.Config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cosmeticshop.cosmetic.Exception.SecurityExceptionHandler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JwtAuthenticationFilter - Filter xác thực JWT cho mọi request
 * 
 * Flow hoạt động:
 * 1. Mọi HTTP request đều đi qua filter này TRƯỚC KHI vào controller
 * 2. Filter lấy JWT token từ header "Authorization"
 * 3. Validate token và extract username
 * 4. Load user từ database
 * 5. Set authentication vào SecurityContext
 * 6. Request tiếp tục đến controller
 * 
 * OncePerRequestFilter: Đảm bảo filter chỉ chạy 1 lần cho mỗi request
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Bước 1: Lấy header "Authorization" từ request
        final String authHeader = request.getHeader("Authorization");

        // Bước 2: Kiểm tra header có tồn tại và bắt đầu bằng "Bearer " không
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Không có token → bỏ qua filter này, chuyển sang filter tiếp theo
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Bước 3: Trích xuất token (bỏ phần "Bearer " ở đầu)
            final String jwt = authHeader.substring(7);

            // Bước 4: Lấy username từ token
            final String username = jwtUtil.extractUsername(jwt);

            // Bước 5: Kiểm tra username có tồn tại và user chưa được authenticate
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Bước 6: Load user details từ database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Bước 7: Validate token với username
                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                    
                    // Bước 8: Tạo Authentication object
                    // UsernamePasswordAuthenticationToken: Object chứa thông tin xác thực
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails,              // Principal: thông tin user
                            null,                     // Credentials: null vì đã xác thực bằng JWT
                            userDetails.getAuthorities() // Authorities: quyền hạn (roles)
                        );

                    // Bước 9: Set thêm details từ request (IP, session, etc.)
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Bước 10: Set authentication vào SecurityContext
                    // Từ giờ, Spring Security biết user đã được xác thực
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            // Bước 11: Chuyển request sang filter tiếp theo trong chain
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            // Token đã hết hạn
            logger.warn("JWT token đã hết hạn: {}", e.getMessage());
            SecurityExceptionHandler.writeJsonError(response, 401, "Token đã hết hạn, vui lòng đăng nhập lại");
        } catch (MalformedJwtException e) {
            // Token không đúng format
            logger.warn("JWT token không hợp lệ: {}", e.getMessage());
            SecurityExceptionHandler.writeJsonError(response, 401, "Token không hợp lệ");
        } catch (SignatureException e) {
            // Signature không khớp (token bị giả mạo)
            logger.error("JWT signature không hợp lệ: {}", e.getMessage());
            SecurityExceptionHandler.writeJsonError(response, 401, "Token không hợp lệ hoặc đã bị giả mạo");
        } catch (Exception e) {
            // Lỗi khác
            logger.error("Lỗi xác thực JWT: {}", e.getMessage(), e);
            SecurityExceptionHandler.writeJsonError(response, 401, "Xác thực thất bại");
        }
    }
}
