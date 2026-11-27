package com.travelplanner.demo.service;

import com.travelplanner.demo.dto.LoginRequest;
import com.travelplanner.demo.dto.RegisterRequest;
import com.travelplanner.demo.model.AppUser;
import com.travelplanner.demo.model.Role;
import com.travelplanner.demo.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

@Service
public class AuthService {
    public static final String SESSION_USER_ID = "userId";
    private final AppUserRepository userRepository;
    private final boolean authEnabled;

    public AuthService(AppUserRepository userRepository,
                       @Value("${app.auth.enabled:false}") boolean authEnabled) {
        this.userRepository = userRepository;
        this.authEnabled = authEnabled;
    }

    // Có đang bật kiểm tra quyền không (giao diện dùng để biết có cần chặn trang hay không)
    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public AppUser register(RegisterRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email này đã được đăng ký");
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setDisplayName(request.displayName().trim());
        user.setPassword(request.password()); // lưu nguyên văn theo quyết định của nhóm
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    public AppUser authenticate(LoginRequest request) {
        // Sai email hay sai mật khẩu đều báo giống nhau, không tiết lộ email có tồn tại hay không
        return userRepository.findByEmail(normalize(request.email()))
                .filter(user -> passwordMatches(request.password(), user.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Email hoặc mật khẩu không đúng"));
    }

    public Optional<AppUser> findById(Long id) {
        return userRepository.findById(id);
    }

    // Việc so sánh mật khẩu nằm ở đúng MỘT chỗ này.
    // Sau này muốn băm (BCrypt) thì chỉ sửa ở đây và ở dòng setPassword trong register.
    private boolean passwordMatches(String input, String stored) {
        return stored.equals(input);
    }

    // Email không phân biệt hoa thường, bỏ dấu cách thừa
    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}