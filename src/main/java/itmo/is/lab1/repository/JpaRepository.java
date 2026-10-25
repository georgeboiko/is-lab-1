package itmo.is.lab1.repository;

import itmo.is.lab1.persistence.TransactionIsolation;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

public abstract class JpaRepository {

    @Inject
    private TransactionIsolation transactionIsolation;

    protected EntityManager entityManager() {
        return transactionIsolation.entityManager();
    }
}
