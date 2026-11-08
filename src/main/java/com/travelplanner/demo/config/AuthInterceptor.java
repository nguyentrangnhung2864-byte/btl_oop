package com.travelplanner.demo.config;

import com.travelplanner.demo.model.AppUser;
import com.travelplanner.demo.model.Role;
import com.travelplanner.demo.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    // Controller của B, C đọc hai giá trị này từ request khi người dùng đã đăng nhập
    public static final String ATTR_USER_ID = "currentUserId";
    public static final String ATTR_USER_ROLE = "currentUserRole";

    private enum Access { PUBLIC, LOGIN, ADMIN }

    private final AuthService authService;
    private final boolean enabled;

    public AuthInterceptor(AuthService authService,
                           @Value("${app.auth.enabled:false}") boolean enabled) {
        this.authService = authService;
        this.enabled = enabled;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // Luôn ghi nhận người dùng hiện tại (nếu có), kể cả khi công tắc đang tắt
        AppUser user = currentUser(request);
        if (user != null) {
            request.setAttribute(ATTR_USER_ID, user.getId());
            request.setAttribute(ATTR_USER_ROLE, user.getRole().name());
        }

        if (!enabled) {
            return true; // công tắc tắt: không chặn gì
        }

        Access needed = requiredAccess(request.getMethod(), request.getRequestURI());
        if (needed == Access.PUBLIC) {
            return true;
        }
        if (user == null) {
            reject(response, 401, "Bạn cần đăng nhập để thực hiện thao tác này");
            return false;
        }
        if (needed == Access.ADMIN && user.getRole() != Role.ADMIN) {
            reject(response, 403, "Bạn không có quyền thực hiện thao tác này");
            return false;
        }
        return true;
    }

    // TOÀN BỘ luật truy cập nằm ở đây. Muốn mở công khai một API thì thêm một dòng vào hàm này.
    private Access requiredAccess(String method, String path) {
        if (path.startsWith("/api/auth/")) {
            return Access.PUBLIC;
        }
        if (path.equals("/api/places") || path.startsWith("/api/places/")) {
            if (path.endsWith("/ai-overview")) {
                return Access.LOGIN; // AI tốn hạn mức Gemini nên cần đăng nhập
            }
            if (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("HEAD")) {
                return Access.PUBLIC; // ai cũng xem được địa điểm
            }
            return Access.ADMIN; // thêm, sửa, xóa địa điểm
        }
        return Access.LOGIN; // mọi API còn lại (lịch trình...) cần đăng nhập
    }

    private AppUser currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AuthService.SESSION_USER_ID) instanceof Long id) {
            return authService.findById(id).orElse(null);
        }
        return null;
    }

    // Trả lỗi theo đúng định dạng chung của dự án: {"status": ..., "message": ...}
    private void reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"message\":\"" + message + "\"}");
    }
}