package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.VentaDetalle;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Venta;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;

import java.util.UUID;

@Path("venta_detalle")
public class VentaDetalleResource {

    @Inject
    VentaDetalleDAO ventaDetalleDao;

    @Inject
    VentaDAO ventaDao;

    @Inject
    ProductoDAO productoDAO;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findRange(@Min(0) @DefaultValue("0") @QueryParam("first") int first,
                              @Max(100) @DefaultValue("50") @QueryParam("max") int max) {
        if (first >= 0 && max <= 100) {
            try {
                Long total = ventaDetalleDao.count();
                return Response.ok(ventaDetalleDao.findRange(first, max))
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
                VentaDetalle resp = ventaDetalleDao.findById(uuid);
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
                VentaDetalle resp = ventaDetalleDao.findById(uuid);
                if (resp != null) {
                    ventaDetalleDao.eliminar(resp);
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
    public Response create(VentaDetalle entity, @Context UriInfo uriInfo) {
        if (entity != null && entity.getId() == null) {
            try {
                // Validar que Venta sea obligatoria
                if (entity.getIdVenta() == null || entity.getIdVenta().getId() == null) {
                    return Response.status(422)
                            .header("Missing-parameter", "Venta is required")
                            .build();
                }

                // Validar que Producto sea obligatorio
                if (entity.getIdProducto() == null || entity.getIdProducto().getId() == null) {
                    return Response.status(422)
                            .header("Missing-parameter", "Producto is required")
                            .build();
                }

                // Validar que Venta exista
                Venta venta = ventaDao.findById(entity.getIdVenta().getId());
                if (venta == null) {
                    return Response.status(422)
                            .header("Missing-parameter",
                                    "Venta with id " + entity.getIdVenta().getId() + " does not exist in database")
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
                entity.setIdVenta(venta);
                entity.setIdProducto(producto);

                // Generar UUID si no existe
                if (entity.getId() == null) {
                    entity.setId(UUID.randomUUID());
                }

                ventaDetalleDao.crear(entity);

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
    public Response update(@PathParam("id") String id, VentaDetalle entity) {
        if (id != null && !id.trim().isEmpty() && entity != null) {
            try {
                UUID uuid = UUID.fromString(id);
                VentaDetalle existing = ventaDetalleDao.findById(uuid);
                if (existing != null) {
                    // Validar Venta si se proporciona
                    if (entity.getIdVenta() != null && entity.getIdVenta().getId() != null) {
                        Venta venta = ventaDao.findById(entity.getIdVenta().getId());
                        if (venta == null) {
                            return Response.status(422)
                                    .header("Missing-parameter",
                                            "Venta with id " + entity.getIdVenta().getId() + " does not exist in database")
                                    .build();
                        }
                        entity.setIdVenta(venta);
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
                    ventaDetalleDao.modificar(entity);
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