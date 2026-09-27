package itmo.is.lab1.dto.response;

import java.time.Instant;

public record AuthResponse(
        String token,
        Instant expiresAt,
        Long userId,
        String username
) {}
