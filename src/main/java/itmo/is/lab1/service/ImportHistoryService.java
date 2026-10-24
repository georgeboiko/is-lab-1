package itmo.is.lab1.service;

import itmo.is.lab1.model.ImportOperation;
import itmo.is.lab1.model.ImportStatus;
import itmo.is.lab1.model.Page;
import itmo.is.lab1.model.UserAccount;
import itmo.is.lab1.model.UserRole;
import itmo.is.lab1.repository.AuthRepository;
import itmo.is.lab1.repository.ImportOperationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ImportHistoryService {

    @Inject
    private ImportOperationRepository repository;

    @Inject
    private AuthRepository authRepository;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public long start(long userId) {
        ImportOperation operation = new ImportOperation(requireUser(userId));
        repository.persist(operation);
        return operation.getId();
    }

    @Transactional(Transactional.TxType.MANDATORY)
    public void complete(long operationId, int addedCount) {
        requireOperation(operationId).complete(addedCount);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void fail(long operationId, String message) {
        ImportOperation operation = requireOperation(operationId);
        if (operation.getStatus() == ImportStatus.RUNNING) {
            operation.fail(message);
        }
    }

    @Transactional
    public Page<ImportOperation> findPage(long requesterId, int page, int size) {
        if (page < 0 || size <= 0 || size > 100 || page > Integer.MAX_VALUE / size) {
            throw new IllegalArgumentException(
                    "Page must be non-negative, size must be between 1 and 100, offset must fit an integer");
        }
        Long ownerId = ownerFilter(requesterId);
        return new Page<>(repository.findPage(ownerId, page * size, size),
                repository.count(ownerId), page, size);
    }

    @Transactional
    public ImportOperation findById(long requesterId, long operationId) {
        return repository.findVisibleById(operationId, ownerFilter(requesterId))
                .orElseThrow(() -> operationNotFound(operationId));
    }

    private Long ownerFilter(long requesterId) {
        UserAccount user = requireUser(requesterId);
        return user.getRole() == UserRole.ADMIN ? null : user.getId();
    }

    private UserAccount requireUser(long userId) {
        return authRepository.findUserById(userId)
                .orElseThrow(() -> new SecurityException("Authenticated user was not found"));
    }

    private ImportOperation requireOperation(long operationId) {
        return repository.findById(operationId)
                .orElseThrow(() -> operationNotFound(operationId));
    }

    private EntityNotFoundException operationNotFound(long id) {
        return new EntityNotFoundException("Import operation with id " + id + " was not found");
    }
}
