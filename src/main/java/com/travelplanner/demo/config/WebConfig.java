package com.travelplanner.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Chỉ kiểm tra các API. Các trang HTML, CSS, JS, ảnh không đi qua bộ chặn này.
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/**");
    }
}