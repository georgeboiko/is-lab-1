package itmo.is.lab1.api;

import itmo.is.lab1.dto.request.PersonRequest;
import itmo.is.lab1.dto.response.PersonResponse;
import itmo.is.lab1.model.Person;
import itmo.is.lab1.service.PersonService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/persons")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {

    @Inject
    private PersonService personService;

    @Inject
    private UpdateNotifier updateNotifier;

    @POST
    public Response create(@Valid @NotNull PersonRequest input) {
        Person person = personService.create(input);
        updateNotifier.notify("person", "created", person.getId());
        return Response.created(URI.create("persons/" + person.getId()))
                .entity(PersonResponse.from(person))
                .build();
    }

    @GET
    @Path("/{id}")
    public PersonResponse findById(@PathParam("id") long id) {
        return PersonResponse.from(personService.findById(id));
    }

    @GET
    public List<PersonResponse> findAll() {
        return personService.findAll().stream()
                .map(PersonResponse::from)
                .toList();
    }

    @PUT
    @Path("/{id}")
    public PersonResponse update(@PathParam("id") long id,
                                 @Valid @NotNull PersonRequest input) {
        Person person = personService.update(id, input);
        updateNotifier.notify("person", "updated", id);
        return PersonResponse.from(person);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") long id) {
        personService.delete(id);
        updateNotifier.notify("person", "deleted", id);
        return Response.noContent().build();
    }
}
