package com.travelplanner.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Xin chào, Spring Boot đã kết nối MySQL thành công!";
    }
}
