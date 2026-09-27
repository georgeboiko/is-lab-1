package itmo.is.lab1.dto.response;

import java.time.OffsetDateTime;

public record AuthResponse(
        String token,
        OffsetDateTime expiresAt,
        Long userId,
        String username
) {}
