package itmo.is.lab1.service;

import itmo.is.lab1.dto.LocationRequest;
import itmo.is.lab1.dto.PersonRequest;
import itmo.is.lab1.model.Location;
import itmo.is.lab1.model.Person;
import itmo.is.lab1.repository.PersonRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@ApplicationScoped
public class PersonService {

    @Inject
    private PersonRepository personRepository;

    @Transactional
    public Person create(@Valid @NotNull PersonRequest input) {
        Person person = new Person(input.hairColor());
        applyInput(person, input);
        personRepository.persist(person);
        return person;
    }

    public Person findById(long id) {
        return requirePerson(id);
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    @Transactional
    public Person update(long id, @Valid @NotNull PersonRequest input) {
        Person person = requirePerson(id);
        applyInput(person, input);
        return person;
    }

    @Transactional
    public void delete(long id) {
        personRepository.remove(requirePerson(id));
    }

    private void applyInput(Person person, PersonRequest input) {
        person.setEyeColor(input.eyeColor());
        person.setHairColor(input.hairColor());
        person.setHeight(input.height());
        person.setNationality(input.nationality());
        updateLocation(person, input.location());
    }

    private void updateLocation(Person person, LocationRequest input) {
        if (input == null) {
            person.setLocation(null);
            return;
        }

        Location location = person.getLocation();
        if (location == null) {
            person.setLocation(new Location(
                    input.x(), input.y(), input.z(), input.name()));
            return;
        }

        location.setX(input.x());
        location.setY(input.y());
        location.setZ(input.z());
        location.setName(input.name());
    }

    private Person requirePerson(long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Person with id " + id + " was not found"));
    }
}
