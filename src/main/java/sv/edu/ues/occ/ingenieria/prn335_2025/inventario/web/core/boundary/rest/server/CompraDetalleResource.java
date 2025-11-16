package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDetalleDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.CompraDetalle;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;

import java.util.UUID;

@Path("compra_detalle")
public class CompraDetalleResource {

    @Inject
    CompraDetalleDao compraDetalleDao;

    @Inject
    CompraDAO compraDao;

    @Inject
    ProductoDAO productoDAO;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findRange(@Min(0) @DefaultValue("0") @QueryParam("first") int first,
                              @Max(100) @DefaultValue("50") @QueryParam("max") int max) {
        if (first >= 0 && max <= 100) {
            try {
                Long total = compraDetalleDao.count();
                return Response.ok(compraDetalleDao.findRange(first, max))
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
                CompraDetalle resp = compraDetalleDao.findById(uuid);
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
                CompraDetalle resp = compraDetalleDao.findById(uuid);
                if (resp != null) {
                    compraDetalleDao.eliminar(resp);
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
    public Response create(CompraDetalle entity, @Context UriInfo uriInfo) {
        if (entity != null && entity.getId() == null) {
            try {
                // Validar que Compra sea obligatoria
                if (entity.getIdCompra() == null || entity.getIdCompra().getId() == null) {
                    return Response.status(422)
                            .header("Missing-parameter", "Compra is required")
                            .build();
                }

                // Validar que Producto sea obligatorio
                if (entity.getIdProducto() == null || entity.getIdProducto().getId() == null) {
                    return Response.status(422)
                            .header("Missing-parameter", "Producto is required")
                            .build();
                }

                // Validar que Compra exista
                Compra compra = compraDao.findById(entity.getIdCompra().getId());
                if (compra == null) {
                    return Response.status(422)
                            .header("Missing-parameter",
                                    "Compra with id " + entity.getIdCompra().getId() + " does not exist in database")
                            .build();
                }

                // Validar que Producto exista
                Producto producto = productoDAO.findById(entity.getIdProducto().getId());
                if (producto == null) {
                    return Response.status(422)
                            .header("Missing-parameter",
                                    "Producto with id " + entity.getIdProducto().getId() + " does not exist in database")
                            .build();
                }

                // Asignar entidades completas
                entity.setIdCompra(compra);
                entity.setIdProducto(producto);

                // Generar UUID si no existe
                if (entity.getId() == null) {
                    entity.setId(UUID.randomUUID());
                }

                compraDetalleDao.crear(entity);

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
    public Response update(@PathParam("id") String id, CompraDetalle entity) {
        if (id != null && !id.trim().isEmpty() && entity != null) {
            try {
                UUID uuid = UUID.fromString(id);
                CompraDetalle existing = compraDetalleDao.findById(uuid);
                if (existing != null) {
                    // Validar Compra si se proporciona
                    if (entity.getIdCompra() != null && entity.getIdCompra().getId() != null) {
                        Compra compra = compraDao.findById(entity.getIdCompra().getId());
                        if (compra == null) {
                            return Response.status(422)
                                    .header("Missing-parameter",
                                            "Compra with id " + entity.getIdCompra().getId() + " does not exist in database")
                                    .build();
                        }
                        entity.setIdCompra(compra);
                    }

                    // Validar Producto si se proporciona
                    if (entity.getIdProducto() != null && entity.getIdProducto().getId() != null) {
                        Producto producto = productoDAO.findById(entity.getIdProducto().getId());
                        if (producto == null) {
                            return Response.status(422)
                                    .header("Missing-parameter",
                                            "Producto with id " + entity.getIdProducto().getId() + " does not exist in database")
                                    .build();
                        }
                        entity.setIdProducto(producto);
                    }

                    entity.setId(uuid);
                    compraDetalleDao.modificar(entity);
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