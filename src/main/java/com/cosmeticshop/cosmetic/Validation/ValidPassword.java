package com.cosmeticshop.cosmetic.Validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * ValidPassword - Custom annotation để validate password strength
 * 
 * Yêu cầu:
 * - Độ dài tối thiểu: 8 ký tự
 * - Chứa ít nhất 1 chữ thường (a-z)
 * - Chứa ít nhất 1 chữ hoa (A-Z)
 * - Chứa ít nhất 1 số (0-9)
 * - Chứa ít nhất 1 ký tự đặc biệt (@$!%*?&)
 * - Không chứa username hoặc email (check trong validator)
 */
@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    
    String message() default "Mật khẩu không đủ mạnh";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
