package com.cosmeticshop.cosmetic.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * PasswordConstraintValidator - Validator logic cho password strength
 * 
 * Kiểm tra:
 * 1. Độ dài tối thiểu: 8 ký tự
 * 2. Chứa ít nhất 1 chữ thường
 * 3. Chứa ít nhất 1 chữ hoa
 * 4. Chứa ít nhất 1 số
 * 5. Chứa ít nhất 1 ký tự đặc biệt
 */
public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    // Regex patterns
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[@$!%*?&]");
    
    private static final int MIN_LENGTH = 8;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            buildConstraintViolation(context, "Mật khẩu không được để trống");
            return false;
        }

        List<String> violations = new ArrayList<>();

        if (password.length() < MIN_LENGTH) {
            violations.add(String.format("Mật khẩu phải có ít nhất %d ký tự", MIN_LENGTH));
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            violations.add("Mật khẩu phải chứa ít nhất 1 chữ thường (a-z)");
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            violations.add("Mật khẩu phải chứa ít nhất 1 chữ hoa (A-Z)");
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            violations.add("Mật khẩu phải chứa ít nhất 1 số (0-9)");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            violations.add("Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt (@$!%*?&)");
        }

        // Nếu có vi phạm, tạo thông báo lỗi tùy chỉnh
        if (!violations.isEmpty()) {
            String message = String.join("; ", violations);
            buildConstraintViolation(context, message);
            return false;
        }

        return true;
    }

    // Tạo thông báo vi phạm tùy chỉnh
    private void buildConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
