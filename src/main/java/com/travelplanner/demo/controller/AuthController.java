package com.travelplanner.demo.controller;

import com.travelplanner.demo.dto.LoginRequest;
import com.travelplanner.demo.dto.MeResponse;
import com.travelplanner.demo.dto.RegisterRequest;
import com.travelplanner.demo.dto.UserResponse;
import com.travelplanner.demo.model.AppUser;
import com.travelplanner.demo.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 HttpServletRequest http) {
        AppUser user = authService.register(request);
        startSession(http, user); // đăng ký xong tự đăng nhập luôn
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        AppUser user = authService.authenticate(request);
        startSession(http, user);
        return UserResponse.from(user);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest http) {
        HttpSession session = http.getSession(false); // không tự tạo phiên mới
        if (session != null) {
            session.invalidate();
        }
    }

    // Giao diện gọi hàm này mỗi khi mở trang để biết đã đăng nhập chưa.
    // Chưa đăng nhập vẫn trả 200 (authenticated = false) để khỏi báo lỗi đỏ trong Console.
    @GetMapping("/me")
    public MeResponse me(HttpServletRequest http) {
        boolean authEnabled = authService.isAuthEnabled();
        HttpSession session = http.getSession(false);
        if (session != null && session.getAttribute(AuthService.SESSION_USER_ID) instanceof Long id) {
            return authService.findById(id)
                    .map(user -> new MeResponse(true, UserResponse.from(user), authEnabled))
                    .orElse(new MeResponse(false, null, authEnabled)); // tài khoản đã bị xóa
        }
        return new MeResponse(false, null, authEnabled);
    }

    private void startSession(HttpServletRequest http, AppUser user) {
        HttpSession session = http.getSession(true);
        http.changeSessionId(); // đổi mã phiên sau khi đăng nhập để không dùng lại mã phiên cũ
        session.setAttribute(AuthService.SESSION_USER_ID, user.getId());
    }
}