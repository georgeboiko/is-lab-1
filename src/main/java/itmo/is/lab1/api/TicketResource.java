package itmo.is.lab1.api;

import itmo.is.lab1.dto.request.CopyTicketRequest;
import itmo.is.lab1.dto.request.SellTicketRequest;
import itmo.is.lab1.dto.request.TicketRequest;
import itmo.is.lab1.dto.response.PageResponse;
import itmo.is.lab1.dto.response.TicketResponse;
import itmo.is.lab1.model.TicketReadModel;
import itmo.is.lab1.service.TicketService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/tickets")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TicketResource {

    @Inject
    private TicketService ticketService;

    @Inject
    private UpdateNotifier updateNotifier;

    @POST
    public Response create(@Valid @NotNull TicketRequest input) {
        TicketReadModel ticket = ticketService.create(input);
        updateNotifier.notify("ticket", "created", ticket.id());
        return Response.created(URI.create("tickets/" + ticket.id()))
                .entity(TicketResponse.from(ticket))
                .build();
    }

    @GET
    @Path("/{id}")
    public TicketResponse findById(@PathParam("id") int id) {
        return TicketResponse.from(ticketService.findById(id));
    }

    @GET
    public PageResponse<TicketResponse> findPage(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("name") String name,
            @QueryParam("ascending") @DefaultValue("true") boolean ascending) {
        return PageResponse.from(
                ticketService.findPage(page, size, name, ascending),
                TicketResponse::from
        );
    }

    @PUT
    @Path("/{id}")
    public TicketResponse update(@PathParam("id") int id,
                                 @Valid @NotNull TicketRequest input) {
        TicketReadModel ticket = ticketService.update(id, input);
        updateNotifier.notify("ticket", "updated", id);
        return TicketResponse.from(ticket);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        ticketService.delete(id);
        updateNotifier.notify("ticket", "deleted", id);
        return Response.noContent().build();
    }

    @GET
    @Path("/max-id")
    public TicketResponse findWithMaxId() {
        return ticketService.findWithMaxId()
                .map(TicketResponse::from)
                .orElse(null);
    }

    @GET
    @Path("/by-name-prefix")
    public List<TicketResponse> findByNamePrefix(@QueryParam("prefix") String prefix) {
        return ticketService.findByNamePrefix(prefix).stream()
                .map(TicketResponse::from)
                .toList();
    }

    @GET
    @Path("/by-venue-capacity")
    public List<TicketResponse> findByVenueCapacityGreaterThan(
            @QueryParam("capacity") int capacity) {
        return ticketService.findByVenueCapacityGreaterThan(capacity).stream()
                .map(TicketResponse::from)
                .toList();
    }

    @POST
    @Path("/{id}/sell")
    public TicketResponse sell(@PathParam("id") int id,
                               @Valid @NotNull SellTicketRequest input) {
        TicketReadModel ticket = ticketService.sell(
                id, input.amount(), input.personId());
        updateNotifier.notify("ticket", "updated", id);
        return TicketResponse.from(ticket);
    }

    @POST
    @Path("/{id}/copy-with-discount")
    public Response copyWithDiscount(
            @PathParam("id") int id,
            @Valid @NotNull CopyTicketRequest input) {
        TicketReadModel ticket = ticketService.copyWithDiscount(id, input.discount());
        updateNotifier.notify("ticket", "created", ticket.id());
        return Response.created(URI.create("tickets/" + ticket.id()))
                .entity(TicketResponse.from(ticket))
                .build();
    }
}
