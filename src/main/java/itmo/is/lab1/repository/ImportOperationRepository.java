package itmo.is.lab1.repository;

import itmo.is.lab1.model.ImportOperation;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ImportOperationRepository extends JpaRepository {

    public void persist(ImportOperation operation) {
        entityManager().persist(operation);
        entityManager().flush();
    }

    public Optional<ImportOperation> findById(long id) {
        return Optional.ofNullable(entityManager().find(ImportOperation.class, id));
    }

    public Optional<ImportOperation> findVisibleById(long id, Long ownerId) {
        var query = entityManager().createQuery("""
                        SELECT o FROM ImportOperation o
                        JOIN FETCH o.user
                        WHERE o.id = :id
                        """ + (ownerId == null ? "" : " AND o.user.id = :ownerId"),
                        ImportOperation.class)
                .setParameter("id", id);
        if (ownerId != null) {
            query.setParameter("ownerId", ownerId);
        }
        return query.getResultList().stream().findFirst();
    }

    public List<ImportOperation> findPage(Long ownerId, int offset, int limit) {
        var query = entityManager().createQuery("""
                        SELECT o FROM ImportOperation o
                        JOIN FETCH o.user
                        """ + (ownerId == null ? "" : " WHERE o.user.id = :ownerId")
                        + " ORDER BY o.createdAt DESC, o.id DESC", ImportOperation.class)
                .setFirstResult(offset)
                .setMaxResults(limit);
        if (ownerId != null) {
            query.setParameter("ownerId", ownerId);
        }
        return query.getResultList();
    }

    public long count(Long ownerId) {
        var query = entityManager().createQuery(
                "SELECT COUNT(o) FROM ImportOperation o"
                        + (ownerId == null ? "" : " WHERE o.user.id = :ownerId"), Long.class);
        if (ownerId != null) {
            query.setParameter("ownerId", ownerId);
        }
        return query.getSingleResult();
    }
}
