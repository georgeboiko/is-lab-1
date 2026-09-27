package itmo.is.lab1.dto.response;

import itmo.is.lab1.model.Color;
import itmo.is.lab1.model.Country;
import itmo.is.lab1.model.Person;

public record PersonResponse(
        Long id,
        Color eyeColor,
        Color hairColor,
        LocationResponse location,
        Integer height,
        Country nationality
) {

    public static PersonResponse from(Person person) {
        return person == null ? null : new PersonResponse(
                person.getId(),
                person.getEyeColor(),
                person.getHairColor(),
                LocationResponse.from(person.getLocation()),
                person.getHeight(),
                person.getNationality()
        );
    }
}
