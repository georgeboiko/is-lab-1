package itmo.is.lab1.repository;

import itmo.is.lab1.model.Person;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PersonRepository {

    @PersistenceContext(unitName = "lab1")
    private EntityManager entityManager;

    public void persist(Person person) {
        entityManager.persist(person);
    }

    public Optional<Person> findById(long id) {
        return entityManager.createQuery("""
                        SELECT p
                        FROM Person p
                        LEFT JOIN FETCH p.location
                        WHERE p.id = :id
                        """, Person.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    public List<Person> findAll() {
        return entityManager.createQuery("""
                        SELECT p
                        FROM Person p
                        LEFT JOIN FETCH p.location
                        ORDER BY p.id
                        """, Person.class)
                .getResultList();
    }

    public void remove(Person person) {
        entityManager.remove(person);
    }
}
