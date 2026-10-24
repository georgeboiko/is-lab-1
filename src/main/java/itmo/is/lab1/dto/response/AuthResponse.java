package itmo.is.lab1.dto.response;

import itmo.is.lab1.model.UserRole;

import java.time.OffsetDateTime;

public record AuthResponse(
        String token,
        OffsetDateTime expiresAt,
        Long userId,
        String username,
        UserRole role
) {}
