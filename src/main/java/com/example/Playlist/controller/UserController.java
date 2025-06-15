package com.example.Playlist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Playlist.configuration.JwtTokenProvider;
import com.example.Playlist.dto.request.LoginRequest;
import com.example.Playlist.dto.request.RegisterRequest;
import com.example.Playlist.dto.response.LoginResponse;
import com.example.Playlist.entity.WebUser;
import com.example.Playlist.repository.UserRepository;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        WebUser user = userRepository.findByEmail(loginRequest.getEmail());

        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Sai tài khoản hoặc mật khẩu");
        }

        // Tạo token
        String token = jwtTokenProvider.generateToken(user.getEmail());

        return new LoginResponse(token, user.getEmail());
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("Email đã tồn tại");
        }

        WebUser newUser = WebUser.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(newUser);
        return "Đăng ký thành công";
    }
}
