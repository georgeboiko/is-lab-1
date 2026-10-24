package itmo.is.lab1.dto.response;

import itmo.is.lab1.model.UserRole;

public record UserResponse(Long id, String username, UserRole role) {}
