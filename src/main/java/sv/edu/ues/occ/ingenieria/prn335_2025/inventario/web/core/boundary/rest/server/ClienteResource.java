package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ClienteDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Cliente;

import java.util.UUID;

@Path("cliente")
public class ClienteResource {

    @Inject
    ClienteDAO clienteDAO;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findRange(@Min(0) @DefaultValue("0") @QueryParam("first") int first,
                              @Max(100) @DefaultValue("50") @QueryParam("max") int max) {
        if (first >= 0 && max <= 100) {
            try {
                Long total = clienteDAO.count();
                return Response.ok(clienteDAO.findRange(first, max))
                        .header("Total-records", total)
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Server-exception", "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header("Missing-parameter", "first or max out of range")
                .build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@PathParam("id") String id) {
        if (id != null && !id.trim().isEmpty()) {
            try {
                UUID uuid = UUID.fromString(id);
                Cliente resp = clienteDAO.findById(uuid);
                if (resp != null) {
                    return Response.ok(resp).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Record-not-found", "Record with id " + id + " not found")
                        .build();
            } catch (IllegalArgumentException ex) {
                return Response.status(422)
                        .header("Missing-parameter", "Invalid UUID format")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Server-exception", "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header("Missing-parameter", "id cannot be null or empty")
                .build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") String id) {
        if (id != null && !id.trim().isEmpty()) {
            try {
                UUID uuid = UUID.fromString(id);
                Cliente resp = clienteDAO.findById(uuid);
                if (resp != null) {
                    clienteDAO.eliminar(resp);
                    return Response.noContent().build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Record-not-found", "Record with id " + id + " not found")
                        .build();
            } catch (IllegalArgumentException ex) {
                return Response.status(422)
                        .header("Missing-parameter", "Invalid UUID format")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Server-exception", "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header("Missing-parameter", "id cannot be null or empty")
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Cliente entity, @Context UriInfo uriInfo) {
        if (entity != null && entity.getId() == null) {
            try {
                clienteDAO.crear(entity);
                return Response.created(
                        uriInfo.getAbsolutePathBuilder()
                                .path(entity.getId().toString())
                                .build()
                ).build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Server-exception", "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header("Missing-parameter", "entity must not be null and id must be null")
                .build();
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") String id, Cliente entity) {
        if (id != null && !id.trim().isEmpty() && entity != null) {
            try {
                UUID uuid = UUID.fromString(id);
                Cliente existing = clienteDAO.findById(uuid);
                if (existing != null) {
                    entity.setId(uuid);
                    clienteDAO.modificar(entity);
                    return Response.ok(entity).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Record-not-found", "Record with id " + id + " not found")
                        .build();
            } catch (IllegalArgumentException ex) {
                return Response.status(422)
                        .header("Missing-parameter", "Invalid UUID format")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Server-exception", "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header("Missing-parameter", "id and entity must be valid")
                .build();
    }
}