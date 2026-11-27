package com.travelplanner.demo.dto;

public record MeResponse(boolean authenticated, UserResponse user, boolean authEnabled) {
}