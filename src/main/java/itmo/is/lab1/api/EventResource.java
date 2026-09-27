package itmo.is.lab1.api;

import itmo.is.lab1.dto.request.EventRequest;
import itmo.is.lab1.dto.response.EventResponse;
import itmo.is.lab1.model.Event;
import itmo.is.lab1.service.EventService;
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

@Path("/events")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EventResource {

    @Inject
    private EventService eventService;

    @Inject
    private UpdateNotifier updateNotifier;

    @POST
    public Response create(@Valid @NotNull EventRequest input) {
        Event event = eventService.create(input);
        updateNotifier.notify("event", "created", event.getId());
        return Response.created(URI.create("events/" + event.getId()))
                .entity(EventResponse.from(event))
                .build();
    }

    @GET
    @Path("/{id}")
    public EventResponse findById(@PathParam("id") long id) {
        return EventResponse.from(eventService.findById(id));
    }

    @GET
    public List<EventResponse> findAll() {
        return eventService.findAll().stream()
                .map(EventResponse::from)
                .toList();
    }

    @PUT
    @Path("/{id}")
    public EventResponse update(@PathParam("id") long id,
                                @Valid @NotNull EventRequest input) {
        Event event = eventService.update(id, input);
        updateNotifier.notify("event", "updated", id);
        return EventResponse.from(event);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") long id) {
        eventService.delete(id);
        updateNotifier.notify("event", "deleted", id);
        return Response.noContent().build();
    }
}
