package com.travelplanner.demo.dto;

import com.travelplanner.demo.model.AppUser;

public record UserResponse(Long id, String email, String displayName, String role) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getEmail(),
                user.getDisplayName(), user.getRole().name());
    }
}