package com.cosmeticshop.cosmetic.Service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;
import com.cosmeticshop.cosmetic.Entity.User;
import com.cosmeticshop.cosmetic.Repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(CreateUserRequest request){
        // Kiểm tra username đã tồn tại
        if(userRepository.findByUsername(request.getUsername()).isPresent()){
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        
        // Kiểm tra email đã tồn tại
        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Email đã tồn tại");
        }
        
        // Kiểm tra phone đã tồn tại
        if(userRepository.findByPhone(request.getPhone()).isPresent()){
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        // Set role mặc định là CUSTOMER nếu không được cung cấp
        user.setRole(request.getRole() != null ? request.getRole() : User.Role.CUSTOMER);

        return userRepository.save(user);
    }

    //xác thực user
    public User authenticate(String username, String rawPassword){

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Tên đăng nhập không đúng"));

        if(!passwordEncoder.matches(rawPassword, user.getPassword())){
            throw new RuntimeException("Mật khẩu không đúng");
        }
        
        return user;
    }

    public List<User> getAllUser(){
        return userRepository.findAll();
    }

    public User getUserById(Long id){
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy User với " + id));
    }

    public void deleteUser(Long id){
        // Kiểm tra user có tồn tại không
        if(!userRepository.existsById(id)){
            throw new RuntimeException("Không tìm thấy User với ID: " + id);
        }
        userRepository.deleteById(id);
    }
}
