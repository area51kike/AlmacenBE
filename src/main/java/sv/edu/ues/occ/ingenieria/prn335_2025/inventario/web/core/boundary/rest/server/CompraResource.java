package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProveedorDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Proveedor;

@Path("compra")
public class CompraResource {

    @Inject
    CompraDAO compraDao;

    @Inject
    ProveedorDAO proveedorDAO;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findRange(@Min(0) @DefaultValue("0") @QueryParam("first") int first,
                              @Max(100) @DefaultValue("50") @QueryParam("max") int max) {
        if (first >= 0 && max <= 100) {
            try {
                Long total = compraDao.count();
                return Response.ok(compraDao.findRange(first, max))
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
    public Response findById(@PathParam("id") Long id) {
        if (id != null && id > 0) {
            try {
                Compra resp = compraDao.findById(id);
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
    public Response delete(@PathParam("id") Long id) {
        if (id != null && id > 0) {
            try {
                Compra resp = compraDao.findById(id);
                if (resp != null) {
                    compraDao.eliminar(resp);
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
    public Response create(Compra entity, @Context UriInfo uriInfo) {
        if (entity != null && entity.getId() == null) {
            try {
                // Validar que el proveedor sea obligatorio
                if (entity.getIdProveedor() == null) {
                    return Response.status(422)
                            .header("Missing-parameter", "Proveedor is required")
                            .build();
                }

                // Validar que el proveedor exista
                Proveedor proveedor = proveedorDAO.findById(entity.getIdProveedor());
                if (proveedor == null) {
                    return Response.status(422)
                            .header("Missing-parameter",
                                    "Proveedor with id " + entity.getIdProveedor() + " does not exist in database")
                            .build();
                }

                // El DAO ya maneja la asignación del proveedor completo
                compraDao.crear(entity);

                return Response.created(
                        uriInfo.getAbsolutePathBuilder()
                                .path(String.valueOf(entity.getId()))
                                .build()
                ).build();

            } catch (IllegalArgumentException ex) {
                return Response.status(422)
                        .header("Validation-error", ex.getMessage())
                        .build();
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
    public Response update(@PathParam("id") Long id, Compra entity) {
        if (id != null && id > 0 && entity != null) {
            try {
                Compra existing = compraDao.findById(id);
                if (existing != null) {
                    // Validar proveedor si se proporciona
                    if (entity.getIdProveedor() != null) {
                        Proveedor proveedor = proveedorDAO.findById(entity.getIdProveedor());
                        if (proveedor == null) {
                            return Response.status(422)
                                    .header("Missing-parameter",
                                            "Proveedor with id " + entity.getIdProveedor() + " does not exist in database")
                                    .build();
                        }
                        entity.setProveedor(proveedor);
                    }

                    entity.setId(id);
                    compraDao.modificar(entity);
                    return Response.ok(entity).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Record-not-found", "Record with id " + id + " not found")
                        .build();
            } catch (IllegalArgumentException ex) {
                return Response.status(422)
                        .header("Validation-error", ex.getMessage())
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