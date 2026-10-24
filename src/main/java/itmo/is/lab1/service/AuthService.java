package itmo.is.lab1.service;

import itmo.is.lab1.dto.request.CredentialsRequest;
import itmo.is.lab1.model.AuthToken;
import itmo.is.lab1.model.UserAccount;
import itmo.is.lab1.model.UserRole;
import itmo.is.lab1.repository.AuthRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

@ApplicationScoped
public class AuthService {

    private static final Duration TOKEN_LIFETIME = Duration.ofHours(24);
    private static final int TOKEN_BYTES = 32;

    @Inject
    private AuthRepository authRepository;

    @Inject
    private PasswordHasher passwordHasher;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public LoginResult register(@Valid @NotNull CredentialsRequest input) {
        String username = normalizeUsername(input.username());
        if (authRepository.findUserByUsername(username).isPresent()) {
            throw new IllegalStateException("Username is already taken");
        }

        UserAccount user = new UserAccount(
                username, passwordHasher.hash(input.password()));
        try {
            authRepository.persist(user);
        } catch (PersistenceException exception) {
            throw new IllegalStateException("Username is already taken", exception);
        }
        return createToken(user);
    }

    @Transactional
    public LoginResult login(@Valid @NotNull CredentialsRequest input) {
        String username = normalizeUsername(input.username());
        UserAccount user = authRepository.findUserByUsername(username)
                .filter(candidate -> passwordHasher.matches(
                        input.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new SecurityException(
                        "Invalid username or password"));

        authRepository.deleteExpiredTokens(now());
        return createToken(user);
    }

    public Optional<AuthenticatedUser> authenticate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return authRepository.findValidToken(hashToken(rawToken), now())
                .map(token -> new AuthenticatedUser(
                        token.getUser().getId(),
                        token.getUser().getUsername(),
                        token.getUser().getRole()));
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken != null && !rawToken.isBlank()) {
            authRepository.deleteToken(hashToken(rawToken));
        }
    }

    private LoginResult createToken(UserAccount user) {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(bytes);
        OffsetDateTime expiresAt = now().plus(TOKEN_LIFETIME);
        authRepository.persist(new AuthToken(
                hashToken(rawToken), user, expiresAt));
        return new LoginResult(
                rawToken, expiresAt, user.getId(), user.getUsername(), user.getRole());
    }

    private String normalizeUsername(String username) {
        String normalized = username.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() < 3 || normalized.length() > 64) {
            throw new IllegalArgumentException(
                    "Username must contain between 3 and 64 characters");
        }
        return normalized;
    }

    private String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Token hashing is unavailable", exception);
        }
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    public record LoginResult(
            String token,
            OffsetDateTime expiresAt,
            Long userId,
            String username,
            UserRole role
    ) {}

    public record AuthenticatedUser(Long id, String username, UserRole role) {}
}
