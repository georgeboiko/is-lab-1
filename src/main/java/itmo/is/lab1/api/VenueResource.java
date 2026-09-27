package itmo.is.lab1.api;

import itmo.is.lab1.dto.request.VenueRequest;
import itmo.is.lab1.dto.response.VenueResponse;
import itmo.is.lab1.model.Venue;
import itmo.is.lab1.service.VenueService;
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

@Path("/venues")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VenueResource {

    @Inject
    private VenueService venueService;

    @Inject
    private UpdateNotifier updateNotifier;

    @POST
    public Response create(@Valid @NotNull VenueRequest input) {
        Venue venue = venueService.create(input);
        updateNotifier.notify("venue", "created", venue.getId());
        return Response.created(URI.create("venues/" + venue.getId()))
                .entity(VenueResponse.from(venue))
                .build();
    }

    @GET
    @Path("/{id}")
    public VenueResponse findById(@PathParam("id") long id) {
        return VenueResponse.from(venueService.findById(id));
    }

    @GET
    public List<VenueResponse> findAll() {
        return venueService.findAll().stream()
                .map(VenueResponse::from)
                .toList();
    }

    @PUT
    @Path("/{id}")
    public VenueResponse update(@PathParam("id") long id,
                                @Valid @NotNull VenueRequest input) {
        Venue venue = venueService.update(id, input);
        updateNotifier.notify("venue", "updated", id);
        return VenueResponse.from(venue);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") long id) {
        venueService.delete(id);
        updateNotifier.notify("venue", "deleted", id);
        return Response.noContent().build();
    }
}
