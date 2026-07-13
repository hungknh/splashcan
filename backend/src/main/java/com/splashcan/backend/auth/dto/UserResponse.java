package com.splashcan.backend.auth.dto;

import com.splashcan.backend.user.User;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        User.Role role
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }
}
