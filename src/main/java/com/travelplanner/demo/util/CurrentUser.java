package com.travelplanner.demo.util;

import com.travelplanner.demo.config.AuthInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Lấy người đang đăng nhập trong controller:
//     Long userId = CurrentUser.requireId(request);
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long requireId(HttpServletRequest request) {
        if (request.getAttribute(AuthInterceptor.ATTR_USER_ID) instanceof Long id) {
            return id;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Bạn cần đăng nhập để thực hiện thao tác này");
    }

    public static boolean isAdmin(HttpServletRequest request) {
        return "ADMIN".equals(request.getAttribute(AuthInterceptor.ATTR_USER_ROLE));
    }
}