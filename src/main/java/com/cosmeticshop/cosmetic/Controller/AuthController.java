package com.cosmeticshop.cosmetic.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;
import com.cosmeticshop.cosmetic.Dto.LoginRequest;
import com.cosmeticshop.cosmetic.Dto.LoginResponse;
import com.cosmeticshop.cosmetic.Entity.User;
import com.cosmeticshop.cosmetic.Service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/register")
    public User register(@RequestBody @Valid CreateUserRequest request){
        return userService.createUser(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.ok("Xóa user thành công với ID: " + id);
    }

    //Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request){
        validationLoginRequest(request);

        User user = userService.authenticate(request.getUsername(), request.getPassword());

        LoginResponse response = new LoginResponse(user, "Đăng nhập thành công");
        return ResponseEntity.ok(response);
    }

    //Xác thực yêu cầu đăng nhập
    public void validationLoginRequest(LoginRequest request){

        if(request.getUsername() == null || request.getUsername().trim().isEmpty()){
            throw new RuntimeException("Tên đăng nhập không được để trống");
        }

        if(request.getPassword() == null || request.getPassword().trim().isEmpty()){
            throw new RuntimeException("Mật khẩu không được để trống");
        }
    }

}
