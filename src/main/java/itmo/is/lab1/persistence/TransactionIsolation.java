package itmo.is.lab1.persistence;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TransactionIsolation {

    private static final String PERSISTENCE_UNIT_KEY = TransactionIsolation.class.getName();

    private enum Unit {
        DEFAULT,
        SERIALIZABLE
    }

    @PersistenceContext(unitName = "lab1")
    private EntityManager defaultEntityManager;

    @PersistenceContext(unitName = "lab1Serializable")
    private EntityManager serializableEntityManager;

    @Resource
    private TransactionSynchronizationRegistry transactionRegistry;

    @Transactional(Transactional.TxType.MANDATORY)
    public void serializable() {
        if (transactionRegistry.getResource(PERSISTENCE_UNIT_KEY) == Unit.DEFAULT) {
            throw new PersistenceException(
                    "Cannot switch persistence units after database access in the current transaction");
        }
        transactionRegistry.putResource(PERSISTENCE_UNIT_KEY, Unit.SERIALIZABLE);
    }

    public EntityManager entityManager() {
        if (transactionRegistry.getTransactionKey() == null) {
            return defaultEntityManager;
        }

        Unit unit = (Unit) transactionRegistry.getResource(PERSISTENCE_UNIT_KEY);
        if (unit == null) {
            unit = Unit.DEFAULT;
            transactionRegistry.putResource(PERSISTENCE_UNIT_KEY, unit);
        }
        return unit == Unit.SERIALIZABLE ? serializableEntityManager : defaultEntityManager;
    }
}
