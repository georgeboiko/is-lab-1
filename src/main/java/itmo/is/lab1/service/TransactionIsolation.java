package itmo.is.lab1.service;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TransactionIsolation {

    private static final String SERIALIZABLE_KEY = TransactionIsolation.class.getName();

    @PersistenceContext(unitName = "lab1")
    private EntityManager entityManager;

    @Resource
    private TransactionSynchronizationRegistry transactionRegistry;

    @Transactional(Transactional.TxType.MANDATORY)
    public void serializable() {
        if (Boolean.TRUE.equals(transactionRegistry.getResource(SERIALIZABLE_KEY))) {
            return;
        }

        entityManager.createNativeQuery("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE")
                .executeUpdate();
        entityManager.setProperty("jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
        transactionRegistry.putResource(SERIALIZABLE_KEY, Boolean.TRUE);
    }
}
