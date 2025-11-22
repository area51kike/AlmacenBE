package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.CompraDetalle;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Recurso REST para manejar detalles de una compra específica.
 * Patrón: /compras/{idCompra}/detalles
 */
@Path("/compras/{idCompra}/detalles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompraDetalleResource {

    private static final Logger LOGGER = Logger.getLogger(CompraDetalleResource.class.getName());

    @Inject
    private CompraDetalleDAO compraDetalleDAO;

    @Inject
    private CompraDAO compraDAO;

    @Inject
    private ProductoDAO productoDAO;

    /**
     * GET /compras/{idCompra}/detalles
     * Obtiene todos los detalles (productos) de una compra
     */
    @GET
    public Response getDetallesByCompra(@PathParam("idCompra") Long idCompra) {
        LOGGER.log(Level.INFO, "GET - Obteniendo detalles de la compra: {0}", idCompra);

        try {
            // Validar que la compra existe
            Compra compra = compraDAO.findById(idCompra);
            if (compra == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Compra no encontrada\"}")
                        .build();
            }

            // Obtener todos los detalles y filtrar por compra
            List<CompraDetalle> todosLosDetalles = compraDetalleDAO.findAll();
            List<CompraDetalle> detallesDeLaCompra = todosLosDetalles.stream()
                    .filter(d -> d.getIdCompra() != null &&
                            d.getIdCompra().getId().equals(idCompra))
                    .collect(Collectors.toList());

            LOGGER.log(Level.INFO, "Se encontraron {0} detalles para la compra {1}",
                    new Object[]{detallesDeLaCompra.size(), idCompra});

            return Response.ok(detallesDeLaCompra).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener detalles de la compra " + idCompra, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener detalles: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET /compras/{idCompra}/detalles/{idDetalle}
     * Obtiene un detalle específico de una compra
     */
    @GET
    @Path("/{idDetalle}")
    public Response getDetalleById(
            @PathParam("idCompra") Long idCompra,
            @PathParam("idDetalle") String idDetalleStr) {

        LOGGER.log(Level.INFO, "GET - Obteniendo detalle {0} de la compra {1}",
                new Object[]{idDetalleStr, idCompra});

        try {
            UUID idDetalle = UUID.fromString(idDetalleStr);
            CompraDetalle detalle = compraDetalleDAO.findById(idDetalle);

            if (detalle == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Detalle no encontrado\"}")
                        .build();
            }

            // Validar que el detalle pertenece a la compra solicitada
            if (detalle.getIdCompra() == null ||
                    !detalle.getIdCompra().getId().equals(idCompra)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"El detalle no pertenece a la compra especificada\"}")
                        .build();
            }

            return Response.ok(detalle).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"ID de detalle inválido\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener detalle " + idDetalleStr, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener detalle: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * POST /compras/{idCompra}/detalles
     * Agrega un producto (detalle) a una compra
     */
    @POST
    public Response createDetalle(
            @PathParam("idCompra") Long idCompra,
            CompraDetalle detalle) {

        LOGGER.log(Level.INFO, "POST - Creando detalle para compra: {0}", idCompra);

        try {
            // Validar que la compra existe
            Compra compra = compraDAO.findById(idCompra);
            if (compra == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Compra no encontrada\"}")
                        .build();
            }

            // Validaciones básicas
            if (detalle.getIdProductoUUID() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"El ID del producto es obligatorio\"}")
                        .build();
            }

            if (detalle.getCantidad() == null || detalle.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"La cantidad debe ser mayor a cero\"}")
                        .build();
            }

            if (detalle.getPrecio() == null || detalle.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"El precio no puede ser negativo\"}")
                        .build();
            }

            // Validar que el producto existe
            Producto producto = productoDAO.findById(detalle.getIdProductoUUID());
            if (producto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Producto no encontrado\"}")
                        .build();
            }

            // Validar que el producto esté activo
            if (producto.getActivo() == null || !producto.getActivo()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"El producto no está activo\"}")
                        .build();
            }

            // Generar UUID y asignar relaciones
            detalle.setId(UUID.randomUUID());
            detalle.setIdCompra(compra);
            detalle.setIdProducto(producto);

            // Crear el detalle
            compraDetalleDAO.crear(detalle);

            LOGGER.log(Level.INFO, "Detalle creado exitosamente con ID: {0}", detalle.getId());

            return Response.status(Response.Status.CREATED)
                    .entity(detalle)
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Error de validación al crear detalle", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear detalle", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al crear detalle: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * PUT /compras/{idCompra}/detalles/{idDetalle}
     * Actualiza un detalle existente (cantidad, precio, observaciones)
     */
    @PUT
    @Path("/{idDetalle}")
    public Response updateDetalle(
            @PathParam("idCompra") Long idCompra,
            @PathParam("idDetalle") String idDetalleStr,
            CompraDetalle detalle) {

        LOGGER.log(Level.INFO, "PUT - Actualizando detalle {0} de la compra {1}",
                new Object[]{idDetalleStr, idCompra});

        try {
            UUID idDetalle = UUID.fromString(idDetalleStr);

            // Buscar el detalle existente
            CompraDetalle detalleExistente = compraDetalleDAO.findById(idDetalle);

            if (detalleExistente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Detalle no encontrado\"}")
                        .build();
            }

            // Validar que el detalle pertenece a la compra
            if (detalleExistente.getIdCompra() == null ||
                    !detalleExistente.getIdCompra().getId().equals(idCompra)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"El detalle no pertenece a la compra especificada\"}")
                        .build();
            }

            // Validaciones de negocio
            if (detalle.getCantidad() != null && detalle.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"La cantidad debe ser mayor a cero\"}")
                        .build();
            }

            if (detalle.getPrecio() != null && detalle.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"El precio no puede ser negativo\"}")
                        .build();
            }

            // Actualizar campos (NO se permite cambiar producto ni compra)
            if (detalle.getCantidad() != null) {
                detalleExistente.setCantidad(detalle.getCantidad());
            }
            if (detalle.getPrecio() != null) {
                detalleExistente.setPrecio(detalle.getPrecio());
            }
            if (detalle.getEstado() != null) {
                detalleExistente.setEstado(detalle.getEstado());
            }
            if (detalle.getObservaciones() != null) {
                detalleExistente.setObservaciones(detalle.getObservaciones());
            }

            // Modificar
            CompraDetalle detalleActualizado = compraDetalleDAO.modificar(detalleExistente);

            LOGGER.log(Level.INFO, "Detalle actualizado exitosamente: {0}", idDetalle);

            return Response.ok(detalleActualizado).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"ID de detalle inválido\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar detalle " + idDetalleStr, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al actualizar detalle: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * DELETE /compras/{idCompra}/detalles/{idDetalle}
     * Elimina un detalle (producto) de una compra
     */
    @DELETE
    @Path("/{idDetalle}")
    public Response deleteDetalle(
            @PathParam("idCompra") Long idCompra,
            @PathParam("idDetalle") String idDetalleStr) {

        LOGGER.log(Level.INFO, "DELETE - Eliminando detalle {0} de la compra {1}",
                new Object[]{idDetalleStr, idCompra});

        try {
            UUID idDetalle = UUID.fromString(idDetalleStr);
            CompraDetalle detalle = compraDetalleDAO.findById(idDetalle);

            if (detalle == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Detalle no encontrado\"}")
                        .build();
            }

            // Validar que el detalle pertenece a la compra
            if (detalle.getIdCompra() == null ||
                    !detalle.getIdCompra().getId().equals(idCompra)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"El detalle no pertenece a la compra especificada\"}")
                        .build();
            }

            // Eliminar
            compraDetalleDAO.eliminar(detalle);

            LOGGER.log(Level.INFO, "Detalle eliminado exitosamente: {0}", idDetalle);

            return Response.noContent().build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"ID de detalle inválido\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar detalle " + idDetalleStr, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al eliminar detalle: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET /compras/{idCompra}/detalles/count
     * Cuenta los detalles de una compra
     */
    @GET
    @Path("/count")
    public Response countDetallesByCompra(@PathParam("idCompra") Long idCompra) {
        LOGGER.log(Level.INFO, "GET - Contando detalles de la compra: {0}", idCompra);

        try {
            // Validar que la compra existe
            Compra compra = compraDAO.findById(idCompra);
            if (compra == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Compra no encontrada\"}")
                        .build();
            }

            List<CompraDetalle> todosLosDetalles = compraDetalleDAO.findAll();
            long count = todosLosDetalles.stream()
                    .filter(d -> d.getIdCompra() != null &&
                            d.getIdCompra().getId().equals(idCompra))
                    .count();

            return Response.ok("{\"count\": " + count + "}").build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al contar detalles de la compra " + idCompra, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al contar detalles: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET /compras/{idCompra}/detalles/total
     * Calcula el total de la compra (suma de cantidad * precio)
     */
    @GET
    @Path("/total")
    public Response getTotalCompra(@PathParam("idCompra") Long idCompra) {
        LOGGER.log(Level.INFO, "GET - Calculando total de la compra: {0}", idCompra);

        try {
            // Validar que la compra existe
            Compra compra = compraDAO.findById(idCompra);
            if (compra == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Compra no encontrada\"}")
                        .build();
            }

            List<CompraDetalle> todosLosDetalles = compraDetalleDAO.findAll();
            BigDecimal total = todosLosDetalles.stream()
                    .filter(d -> d.getIdCompra() != null &&
                            d.getIdCompra().getId().equals(idCompra))
                    .map(d -> {
                        BigDecimal cantidad = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
                        BigDecimal precio = d.getPrecio() != null ? d.getPrecio() : BigDecimal.ZERO;
                        return cantidad.multiply(precio);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return Response.ok("{\"total\": " + total + "}").build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al calcular total de la compra " + idCompra, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al calcular total: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}