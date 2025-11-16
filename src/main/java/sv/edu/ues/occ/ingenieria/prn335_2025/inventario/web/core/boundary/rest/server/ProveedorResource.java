package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProveedorDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Proveedor;

@Path("proveedor")
public class ProveedorResource {

    @Inject
    ProveedorDAO proveedorDAO;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findRange(@Min(0) @DefaultValue("0") @QueryParam("first") int first,
                              @Max(100) @DefaultValue("50") @QueryParam("max") int max) {
        if (first >= 0 && max <= 100) {
            try {
                Long total = proveedorDAO.count();
                return Response.ok(proveedorDAO.findRange(first, max))
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
    public Response findById(@PathParam("id") Integer id) {
        if (id != null && id > 0) {
            try {
                Proveedor resp = proveedorDAO.findById(id);
                if (resp != null) {
                    return Response.ok(resp).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Record-not-found", "Record with id " + id + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Server-exception", "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header("Missing-parameter", "id must be greater than 0")
                .build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Integer id) {
        if (id != null && id > 0) {
            try {
                Proveedor resp = proveedorDAO.findById(id);
                if (resp != null) {
                    proveedorDAO.eliminar(resp);
                    return Response.noContent().build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Record-not-found", "Record with id " + id + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Server-exception", "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header("Missing-parameter", "id must be greater than 0")
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Proveedor entity, @Context UriInfo uriInfo) {
        if (entity != null && entity.getId() == null) {
            try {
                proveedorDAO.crear(entity);
                return Response.created(
                        uriInfo.getAbsolutePathBuilder()
                                .path(String.valueOf(entity.getId()))
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
    public Response update(@PathParam("id") Integer id, Proveedor entity) {
        if (id != null && id > 0 && entity != null) {
            try {
                Proveedor existing = proveedorDAO.findById(id);
                if (existing != null) {
                    entity.setId(id);
                    proveedorDAO.modificar(entity);
                    return Response.ok(entity).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Record-not-found", "Record with id " + id + " not found")
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