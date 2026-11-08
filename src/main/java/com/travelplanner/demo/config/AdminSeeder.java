package com.travelplanner.demo.config;

import com.travelplanner.demo.model.AppUser;
import com.travelplanner.demo.model.Role;
import com.travelplanner.demo.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final AppUserRepository userRepository;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(AppUserRepository userRepository,
                       @Value("${app.admin.email:}") String adminEmail,
                       @Value("${app.admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.warn("Chưa cấu hình ADMIN_EMAIL và ADMIN_PASSWORD nên không tạo tài khoản quản trị");
            return;
        }

        String email = adminEmail.trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return; // đã có rồi thì không tạo lại
        }

        AppUser admin = new AppUser();
        admin.setEmail(email);
        admin.setDisplayName("Quản trị viên");
        admin.setPassword(adminPassword);
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        log.info("Đã tạo tài khoản quản trị: {}", email); // không ghi mật khẩu vào log
    }
}