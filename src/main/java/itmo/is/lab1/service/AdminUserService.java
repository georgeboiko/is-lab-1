package itmo.is.lab1.service;

import itmo.is.lab1.dto.request.UpdateUserRoleRequest;
import itmo.is.lab1.model.UserAccount;
import itmo.is.lab1.model.UserRole;
import itmo.is.lab1.repository.AuthRepository;
import itmo.is.lab1.service.error.AccessDeniedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@ApplicationScoped
public class AdminUserService {

    @Inject
    private AuthRepository authRepository;

    @Inject
    private TransactionIsolation transactionIsolation;

    @Transactional
    public List<UserAccount> findAll(long requesterId) {
        requireAdministrator(requesterId);
        return authRepository.findAllUsers();
    }

    @Transactional
    public UserAccount updateRole(long requesterId, long userId,
                                  @Valid @NotNull UpdateUserRoleRequest input) {
        transactionIsolation.serializable();
        requireAdministrator(requesterId);
        UserRole role = UserRole.valueOf(input.role());

        UserAccount user = authRepository.findUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with id " + userId + " was not found"));

        if (user.getRole() == role) {
            return user;
        }
        if (user.getRole() == UserRole.ADMIN && role == UserRole.USER
                && authRepository.countAdministrators() <= 1) {
            throw new IllegalStateException("The last administrator cannot be demoted");
        }

        user.setRole(role);
        return user;
    }

    private void requireAdministrator(long requesterId) {
        UserAccount user = authRepository.findUserById(requesterId)
                .orElseThrow(() -> new SecurityException("Authenticated user was not found"));
        requireAdminRole(user);
    }

    private void requireAdminRole(UserAccount user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Administrator role is required");
        }
    }
}
