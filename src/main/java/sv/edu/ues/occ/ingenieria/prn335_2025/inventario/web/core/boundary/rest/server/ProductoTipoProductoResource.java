package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProducto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProducto;

import java.util.UUID;

@Path("producto_tipo_producto")
public class ProductoTipoProductoResource {

    @Inject
    ProductoTipoProductoDAO productoTipoProductoDAO;

    @Inject
    ProductoDAO productoDAO;

    @Inject
    TipoProductoDao tipoProductoDao;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findRange(@Min(0) @DefaultValue("0") @QueryParam("first") int first,
                              @Max(100) @DefaultValue("50") @QueryParam("max") int max) {
        if (first >= 0 && max <= 100) {
            try {
                Long total = productoTipoProductoDAO.count();
                return Response.ok(productoTipoProductoDAO.findRange(first, max))
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
                ProductoTipoProducto resp = productoTipoProductoDAO.findById(uuid);
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
                ProductoTipoProducto resp = productoTipoProductoDAO.findById(uuid);
                if (resp != null) {
                    productoTipoProductoDAO.eliminar(resp);
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
    public Response create(ProductoTipoProducto entity, @Context UriInfo uriInfo) {
        if (entity != null && entity.getId() == null) {
            try {
                // Validar que Producto sea obligatorio
                if (entity.getIdProducto() == null || entity.getIdProducto().getId() == null) {
                    return Response.status(422)
                            .header("Missing-parameter", "Producto is required")
                            .build();
                }

                // Validar que TipoProducto sea obligatorio
                if (entity.getIdTipoProducto() == null || entity.getIdTipoProducto().getId() == null) {
                    return Response.status(422)
                            .header("Missing-parameter", "TipoProducto is required")
                            .build();
                }

                // Validar que Producto exista
                Producto producto = productoDAO.findById(entity.getIdProducto().getId());
                if (producto == null) {
                    return Response.status(422)
                            .header("Missing-parameter",
                                    "Producto with id " + entity.getIdProducto().getId() +
                                            " does not exist in database")
                            .build();
                }

                // Validar que TipoProducto exista
                TipoProducto tipoProducto = tipoProductoDao.findById(entity.getIdTipoProducto().getId());
                if (tipoProducto == null) {
                    return Response.status(422)
                            .header("Missing-parameter",
                                    "TipoProducto with id " + entity.getIdTipoProducto().getId() +
                                            " does not exist in database")
                            .build();
                }

                // Asignar entidades completas
                entity.setIdProducto(producto);
                entity.setIdTipoProducto(tipoProducto);

                // Generar UUID si no existe
                if (entity.getId() == null) {
                    entity.setId(UUID.randomUUID());
                }

                productoTipoProductoDAO.crear(entity);

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
    public Response update(@PathParam("id") String id, ProductoTipoProducto entity) {
        if (id != null && !id.trim().isEmpty() && entity != null) {
            try {
                UUID uuid = UUID.fromString(id);
                ProductoTipoProducto existing = productoTipoProductoDAO.findById(uuid);
                if (existing != null) {
                    // Validar Producto si se proporciona
                    if (entity.getIdProducto() != null && entity.getIdProducto().getId() != null) {
                        Producto producto = productoDAO.findById(entity.getIdProducto().getId());
                        if (producto == null) {
                            return Response.status(422)
                                    .header("Missing-parameter",
                                            "Producto with id " + entity.getIdProducto().getId() +
                                                    " does not exist in database")
                                    .build();
                        }
                        entity.setIdProducto(producto);
                    }

                    // Validar TipoProducto si se proporciona
                    if (entity.getIdTipoProducto() != null && entity.getIdTipoProducto().getId() != null) {
                        TipoProducto tipoProducto = tipoProductoDao.findById(entity.getIdTipoProducto().getId());
                        if (tipoProducto == null) {
                            return Response.status(422)
                                    .header("Missing-parameter",
                                            "TipoProducto with id " + entity.getIdTipoProducto().getId() +
                                                    " does not exist in database")
                                    .build();
                        }
                        entity.setIdTipoProducto(tipoProducto);
                    }

                    entity.setId(uuid);
                    productoTipoProductoDAO.modificar(entity);
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