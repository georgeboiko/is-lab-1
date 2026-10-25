package itmo.is.lab1.repository;

import itmo.is.lab1.model.Location;
import itmo.is.lab1.model.Person;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class PersonRepository extends JpaRepository {

    public void persist(Person person) {
        entityManager().persist(person);
        entityManager().flush();
    }

    public Optional<Person> findById(long id) {
        return entityManager().createQuery("""
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
        return entityManager().createQuery("""
                        SELECT p
                        FROM Person p
                        LEFT JOIN FETCH p.location
                        ORDER BY p.id
                        """, Person.class)
                .getResultList();
    }

    public List<Person> findMatching(Person example) {
        StringBuilder jpql = new StringBuilder("""
                SELECT p FROM Person p
                LEFT JOIN FETCH p.location
                WHERE p.hairColor = :hairColor
                """);
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("hairColor", example.getHairColor());
        nullableCondition(jpql, parameters, "p.eyeColor", "eyeColor", example.getEyeColor());
        nullableCondition(jpql, parameters, "p.height", "height", example.getHeight());
        nullableCondition(jpql, parameters, "p.nationality", "nationality", example.getNationality());

        Location location = example.getLocation();
        if (location == null) {
            jpql.append(" AND p.location IS NULL");
        } else {
            jpql.append("""
                     AND p.location.x = :locationX AND p.location.y = :locationY
                     AND p.location.z = :locationZ AND p.location.name = :locationName
                    """);
            parameters.put("locationX", location.getX());
            parameters.put("locationY", location.getY());
            parameters.put("locationZ", location.getZ());
            parameters.put("locationName", location.getName());
        }
        jpql.append(" ORDER BY p.id");

        var query = entityManager().createQuery(jpql.toString(), Person.class)
                .setMaxResults(2);
        parameters.forEach(query::setParameter);
        return query.getResultList();
    }

    private void nullableCondition(StringBuilder jpql, Map<String, Object> parameters,
                                   String field, String parameter, Object value) {
        jpql.append(" AND ").append(field);
        if (value == null) {
            jpql.append(" IS NULL");
        } else {
            jpql.append(" = :").append(parameter);
            parameters.put(parameter, value);
        }
    }

    public void remove(Person person) {
        entityManager().remove(person);
    }
}
