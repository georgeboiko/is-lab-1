package itmo.is.lab1.repository;

import itmo.is.lab1.model.AuthToken;
import itmo.is.lab1.model.UserAccount;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.OffsetDateTime;
import java.util.Optional;

@ApplicationScoped
public class AuthRepository {

    @PersistenceContext(unitName = "lab1")
    private EntityManager entityManager;

    public void persist(UserAccount user) {
        entityManager.persist(user);
        entityManager.flush();
    }

    public void persist(AuthToken token) {
        entityManager.persist(token);
    }

    public Optional<UserAccount> findUserByUsername(String username) {
        return entityManager.createQuery("""
                        SELECT u FROM UserAccount u
                        WHERE u.username = :username
                        """, UserAccount.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    public Optional<UserAccount> findUserById(long id) {
        return Optional.ofNullable(entityManager.find(UserAccount.class, id));
    }

    public Optional<AuthToken> findValidToken(String tokenHash, OffsetDateTime now) {
        return entityManager.createQuery("""
                        SELECT t FROM AuthToken t
                        JOIN FETCH t.user
                        WHERE t.tokenHash = :tokenHash
                          AND t.expiresAt > :now
                        """, AuthToken.class)
                .setParameter("tokenHash", tokenHash)
                .setParameter("now", now)
                .getResultStream()
                .findFirst();
    }

    public void deleteToken(String tokenHash) {
        entityManager.createQuery(
                        "DELETE FROM AuthToken t WHERE t.tokenHash = :tokenHash")
                .setParameter("tokenHash", tokenHash)
                .executeUpdate();
    }

    public void deleteExpiredTokens(OffsetDateTime now) {
        entityManager.createQuery(
                        "DELETE FROM AuthToken t WHERE t.expiresAt <= :now")
                .setParameter("now", now)
                .executeUpdate();
    }
}
