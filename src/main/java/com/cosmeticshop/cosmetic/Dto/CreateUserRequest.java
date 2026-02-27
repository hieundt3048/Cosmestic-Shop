package com.cosmeticshop.cosmetic.Dto;

import com.cosmeticshop.cosmetic.Entity.User;
import com.cosmeticshop.cosmetic.Validation.ValidPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * CreateUserRequest - DTO cho registration với password validation
 */
public class CreateUserRequest {

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3-50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username chỉ chứa chữ cái, số và dấu gạch dưới")
    private String username;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    @ValidPassword  // Custom validator cho password strength
    private String password;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại không hợp lệ (phải bắt đầu bằng 0 và có 10 số)")
    private String phone;
    
    @Size(max = 100, message = "Họ tên không được quá 100 ký tự")
    private String fullName;
    
    private User.Role role;

    //Getter && Setter
    public void setRole(User.Role role){
        this.role = role;
    }

    public User.Role getRole(){
        return role;
    }
    public void setUsername(String username){
        this.username = username;
    }

    public String getUsername(){
        return username;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getPassword(){
        return password;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getEmail(){
        return email;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }

    public String getPhone(){
        return phone;
    }

    public void setFullName(String fullName){
        this.fullName = fullName;
    }

    public String getFullName(){
        return fullName;
    }
}
