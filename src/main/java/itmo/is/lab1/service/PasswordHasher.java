package itmo.is.lab1.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class PasswordHasher {

    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean matches(String password, String hash) {
        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
