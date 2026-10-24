package itmo.is.lab1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "auth_tokens")
@Cacheable(false)
@Getter
public class AuthToken {

    @Id
    @Column(name = "token_hash", length = 64)
    @Setter(AccessLevel.NONE)
    private String tokenHash;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter(AccessLevel.NONE)
    private UserAccount user;

    @Column(name = "expires_at", nullable = false)
    @Setter(AccessLevel.NONE)
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private OffsetDateTime createdAt;

    protected AuthToken() {
    }

    public AuthToken(String tokenHash, UserAccount user, OffsetDateTime expiresAt) {
        this.tokenHash = tokenHash;
        this.user = user;
        this.expiresAt = expiresAt;
    }
}
