package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoTipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProductoCaracteristica;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProducto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProductoCaracteristica;

import java.util.UUID;

@Path("producto_tipo_producto_caracteristica")
public class ProductoTipoProductoCaracteristicaResource {

    @Inject
    ProductoTipoProductoCaracteristicaDAO productoTipoProductoCaracteristicaDAO;

    @Inject
    ProductoTipoProductoDAO productoTipoProductoDAO;

    @Inject
    TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDAO;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findRange(@Min(0) @DefaultValue("0") @QueryParam("first") int first,
                              @Max(100) @DefaultValue("50") @QueryParam("max") int max) {
        if (first >= 0 && max <= 100) {
            try {
                Long total = productoTipoProductoCaracteristicaDAO.count();
                return Response.ok(productoTipoProductoCaracteristicaDAO.findRange(first, max))
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
                ProductoTipoProductoCaracteristica resp = productoTipoProductoCaracteristicaDAO.findById(uuid);
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
                ProductoTipoProductoCaracteristica resp = productoTipoProductoCaracteristicaDAO.findById(uuid);
                if (resp != null) {
                    productoTipoProductoCaracteristicaDAO.eliminar(resp);
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
    public Response create(ProductoTipoProductoCaracteristica entity, @Context UriInfo uriInfo) {
        if (entity != null && entity.getId() == null) {
            try {
                // Validar que ProductoTipoProducto sea obligatorio
                if (entity.getIdProductoTipoProducto() == null ||
                        entity.getIdProductoTipoProducto().getId() == null) {
                    return Response.status(422)
                            .header("Missing-parameter", "ProductoTipoProducto is required")
                            .build();
                }

                // Validar que TipoProductoCaracteristica sea obligatorio
                if (entity.getIdTipoProductoCaracteristica() == null ||
                        entity.getIdTipoProductoCaracteristica().getId() == null) {
                    return Response.status(422)
                            .header("Missing-parameter", "TipoProductoCaracteristica is required")
                            .build();
                }

                // Validar que ProductoTipoProducto exista
                ProductoTipoProducto productoTipoProducto = productoTipoProductoDAO.findById(
                        entity.getIdProductoTipoProducto().getId()
                );
                if (productoTipoProducto == null) {
                    return Response.status(422)
                            .header("Missing-parameter",
                                    "ProductoTipoProducto with id " + entity.getIdProductoTipoProducto().getId() +
                                            " does not exist in database")
                            .build();
                }

                // Validar que TipoProductoCaracteristica exista
                TipoProductoCaracteristica tipoProductoCaracteristica = tipoProductoCaracteristicaDAO.findById(
                        entity.getIdTipoProductoCaracteristica().getId()
                );
                if (tipoProductoCaracteristica == null) {
                    return Response.status(422)
                            .header("Missing-parameter",
                                    "TipoProductoCaracteristica with id " + entity.getIdTipoProductoCaracteristica().getId() +
                                            " does not exist in database")
                            .build();
                }

                // Asignar entidades completas
                entity.setIdProductoTipoProducto(productoTipoProducto);
                entity.setIdTipoProductoCaracteristica(tipoProductoCaracteristica);

                productoTipoProductoCaracteristicaDAO.crear(entity);

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
    public Response update(@PathParam("id") String id, ProductoTipoProductoCaracteristica entity) {
        if (id != null && !id.trim().isEmpty() && entity != null) {
            try {
                UUID uuid = UUID.fromString(id);
                ProductoTipoProductoCaracteristica existing = productoTipoProductoCaracteristicaDAO.findById(uuid);
                if (existing != null) {
                    // Validar ProductoTipoProducto si se proporciona
                    if (entity.getIdProductoTipoProducto() != null &&
                            entity.getIdProductoTipoProducto().getId() != null) {

                        ProductoTipoProducto productoTipoProducto = productoTipoProductoDAO.findById(
                                entity.getIdProductoTipoProducto().getId()
                        );
                        if (productoTipoProducto == null) {
                            return Response.status(422)
                                    .header("Missing-parameter",
                                            "ProductoTipoProducto with id " + entity.getIdProductoTipoProducto().getId() +
                                                    " does not exist in database")
                                    .build();
                        }
                        entity.setIdProductoTipoProducto(productoTipoProducto);
                    }

                    // Validar TipoProductoCaracteristica si se proporciona
                    if (entity.getIdTipoProductoCaracteristica() != null &&
                            entity.getIdTipoProductoCaracteristica().getId() != null) {

                        TipoProductoCaracteristica tipoProductoCaracteristica = tipoProductoCaracteristicaDAO.findById(
                                entity.getIdTipoProductoCaracteristica().getId()
                        );
                        if (tipoProductoCaracteristica == null) {
                            return Response.status(422)
                                    .header("Missing-parameter",
                                            "TipoProductoCaracteristica with id " + entity.getIdTipoProductoCaracteristica().getId() +
                                                    " does not exist in database")
                                    .build();
                        }
                        entity.setIdTipoProductoCaracteristica(tipoProductoCaracteristica);
                    }

                    entity.setId(uuid);
                    productoTipoProductoCaracteristicaDAO.modificar(entity);
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