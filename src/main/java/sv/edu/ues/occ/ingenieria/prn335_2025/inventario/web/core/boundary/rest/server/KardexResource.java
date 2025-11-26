package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.*;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Recurso REST para manejar registros de kardex (movimientos de inventario).
 * Endpoints principales:
 * - /kardex - Operaciones CRUD generales
 * - /productos/{idProducto}/kardex - Movimientos por producto
 * - /almacenes/{idAlmacen}/kardex - Movimientos por almacén
 */
@Path("/kardex")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KardexResource {

    private static final Logger LOGGER = Logger.getLogger(KardexResource.class.getName());

    @Inject
    KardexDAO kardexDAO;

    @Inject
    ProductoDAO productoDAO;

    @Inject
    AlmacenDAO almacenDAO;

    @Inject
    CompraDetalleDAO compraDetalleDAO;

    @Inject
    VentaDetalleDAO ventaDetalleDAO;

    // ==================== CRUD GENERAL ====================

    /**
     * GET /kardex
     * Obtiene todos los registros de kardex
     */
    @GET
    public Response getAllKardex(
            @QueryParam("tipoMovimiento") String tipoMovimiento,
            @QueryParam("fechaInicio") String fechaInicioStr,
            @QueryParam("fechaFin") String fechaFinStr) {

        LOGGER.log(Level.INFO, "GET - Obteniendo registros de kardex");

        try {
            List<Kardex> kardexList;

            // Filtrar por tipo de movimiento
            if (tipoMovimiento != null && !tipoMovimiento.isBlank()) {
                kardexList = kardexDAO.findByTipoMovimiento(tipoMovimiento);
            }
            // Filtrar por rango de fechas
            else if (fechaInicioStr != null && fechaFinStr != null) {
                OffsetDateTime fechaInicio = OffsetDateTime.parse(fechaInicioStr);
                OffsetDateTime fechaFin = OffsetDateTime.parse(fechaFinStr);
                kardexList = kardexDAO.findByRangoFechas(fechaInicio, fechaFin);
            }
            // Sin filtros, obtener todos
            else {
                kardexList = kardexDAO.findAll();
            }

            LOGGER.log(Level.INFO, "Se encontraron {0} registros de kardex", kardexList.size());
            return Response.ok(kardexList).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener registros de kardex", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener kardex: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET /kardex/{idKardex}
     * Obtiene un registro específico de kardex
     */
    @GET
    @Path("/{idKardex}")
    public Response getKardexById(@PathParam("idKardex") String idKardexStr) {
        LOGGER.log(Level.INFO, "GET - Obteniendo kardex: {0}", idKardexStr);

        try {
            UUID idKardex = UUID.fromString(idKardexStr);
            Kardex kardex = kardexDAO.findById(idKardex);

            if (kardex == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Registro de kardex no encontrado\"}")
                        .build();
            }

            return Response.ok(kardex).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"ID de kardex inválido\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener kardex " + idKardexStr, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener kardex: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * POST /kardex
     * Crea un nuevo registro de kardex (movimiento de inventario)
     */
    @POST
    public Response createKardex(Kardex kardex) {
        LOGGER.log(Level.INFO, "POST - Creando registro de kardex");

        try {
            // Validaciones básicas
            if (kardex.getIdProductoUUID() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"El ID del producto es obligatorio\"}")
                        .build();
            }

            if (kardex.getTipoMovimiento() == null || kardex.getTipoMovimiento().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"El tipo de movimiento es obligatorio\"}")
                        .build();
            }

            if (kardex.getCantidad() == null || kardex.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"La cantidad debe ser mayor a cero\"}")
                        .build();
            }

            // Validar que el producto existe
            Producto producto = productoDAO.findById(kardex.getIdProductoUUID());
            if (producto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Producto no encontrado\"}")
                        .build();
            }

            // Asignar el producto
            kardex.setIdProducto(producto);

            // Validar almacén si está presente
            if (kardex.getIdAlmacenInt() != null) {
                Almacen almacen = almacenDAO.findById(kardex.getIdAlmacenInt());
                if (almacen == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"error\": \"Almacén no encontrado\"}")
                            .build();
                }
                kardex.setIdAlmacen(almacen);
            }

            // Validar CompraDetalle si está presente
            if (kardex.getIdCompraDetalleUUID() != null) {
                CompraDetalle compraDetalle = compraDetalleDAO.findById(kardex.getIdCompraDetalleUUID());
                if (compraDetalle == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"error\": \"Detalle de compra no encontrado\"}")
                            .build();
                }
                kardex.setIdCompraDetalle(compraDetalle);
            }

            // Validar VentaDetalle si está presente
            if (kardex.getIdVentaDetalleUUID() != null) {
                VentaDetalle ventaDetalle = ventaDetalleDAO.findById(kardex.getIdVentaDetalleUUID());
                if (ventaDetalle == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"error\": \"Detalle de venta no encontrado\"}")
                            .build();
                }
                kardex.setIdVentaDetalle(ventaDetalle);
            }

            // Crear el kardex
            kardexDAO.crear(kardex);

            LOGGER.log(Level.INFO, "Kardex creado exitosamente con ID: {0}", kardex.getId());

            return Response.status(Response.Status.CREATED)
                    .entity(kardex)
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Error de validación al crear kardex", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear kardex", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al crear kardex: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * PUT /kardex/{idKardex}
     * Actualiza un registro de kardex
     */
    @PUT
    @Path("/{idKardex}")
    public Response updateKardex(
            @PathParam("idKardex") String idKardexStr,
            Kardex kardex) {

        LOGGER.log(Level.INFO, "PUT - Actualizando kardex: {0}", idKardexStr);

        try {
            UUID idKardex = UUID.fromString(idKardexStr);
            Kardex kardexExistente = kardexDAO.findById(idKardex);

            if (kardexExistente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Kardex no encontrado\"}")
                        .build();
            }

            // Actualizar campos permitidos (NO se permite cambiar producto, almacén, compra o venta)
            if (kardex.getCantidad() != null) {
                kardexExistente.setCantidad(kardex.getCantidad());
            }
            if (kardex.getPrecio() != null) {
                kardexExistente.setPrecio(kardex.getPrecio());
            }
            if (kardex.getCantidadActual() != null) {
                kardexExistente.setCantidadActual(kardex.getCantidadActual());
            }
            if (kardex.getPrecioActual() != null) {
                kardexExistente.setPrecioActual(kardex.getPrecioActual());
            }
            if (kardex.getObservaciones() != null) {
                kardexExistente.setObservaciones(kardex.getObservaciones());
            }
            if (kardex.getReferenciaExterna() != null) {
                kardexExistente.setReferenciaExterna(kardex.getReferenciaExterna());
            }

            Kardex kardexActualizado = kardexDAO.modificar(kardexExistente);

            LOGGER.log(Level.INFO, "Kardex actualizado exitosamente: {0}", idKardex);

            return Response.ok(kardexActualizado).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"ID de kardex inválido\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar kardex " + idKardexStr, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al actualizar kardex: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * DELETE /kardex/{idKardex}
     * Elimina un registro de kardex
     */
    @DELETE
    @Path("/{idKardex}")
    public Response deleteKardex(@PathParam("idKardex") String idKardexStr) {
        LOGGER.log(Level.INFO, "DELETE - Eliminando kardex: {0}", idKardexStr);

        try {
            UUID idKardex = UUID.fromString(idKardexStr);
            Kardex kardex = kardexDAO.findById(idKardex);

            if (kardex == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Kardex no encontrado\"}")
                        .build();
            }

            kardexDAO.eliminar(kardex);

            LOGGER.log(Level.INFO, "Kardex eliminado exitosamente: {0}", idKardex);

            return Response.noContent().build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"ID de kardex inválido\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar kardex " + idKardexStr, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al eliminar kardex: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    // ==================== ENDPOINTS POR PRODUCTO ====================

    /**
     * GET /productos/{idProducto}/kardex
     * Obtiene todos los movimientos de un producto
     */
    @GET
    @Path("/productos/{idProducto}")
    public Response getKardexByProducto(@PathParam("idProducto") String idProductoStr) {
        LOGGER.log(Level.INFO, "GET - Obteniendo kardex del producto: {0}", idProductoStr);

        try {
            UUID idProducto = UUID.fromString(idProductoStr);

            // Validar que el producto existe
            Producto producto = productoDAO.findById(idProducto);
            if (producto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Producto no encontrado\"}")
                        .build();
            }

            List<Kardex> kardexList = kardexDAO.findByProducto(idProducto);

            LOGGER.log(Level.INFO, "Se encontraron {0} movimientos para el producto {1}",
                    new Object[]{kardexList.size(), idProducto});

            return Response.ok(kardexList).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"ID de producto inválido\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener kardex del producto " + idProductoStr, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener movimientos: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET /productos/{idProducto}/kardex/ultimo
     * Obtiene el último movimiento de un producto (opcionalmente en un almacén específico)
     */
    @GET
    @Path("/productos/{idProducto}/ultimo")
    public Response getUltimoMovimientoProducto(
            @PathParam("idProducto") String idProductoStr,
            @QueryParam("idAlmacen") Integer idAlmacen) {

        LOGGER.log(Level.INFO, "GET - Obteniendo último movimiento del producto: {0}", idProductoStr);

        try {
            UUID idProducto = UUID.fromString(idProductoStr);

            Kardex ultimoMovimiento = kardexDAO.findUltimoMovimiento(idProducto, idAlmacen);

            if (ultimoMovimiento == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"No se encontraron movimientos para este producto\"}")
                        .build();
            }

            return Response.ok(ultimoMovimiento).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"ID de producto inválido\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener último movimiento", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener último movimiento: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    // ==================== ENDPOINTS POR ALMACÉN ====================

    /**
     * GET /almacenes/{idAlmacen}/kardex
     * Obtiene todos los movimientos de un almacén
     */
    @GET
    @Path("/almacenes/{idAlmacen}")
    public Response getKardexByAlmacen(@PathParam("idAlmacen") Integer idAlmacen) {
        LOGGER.log(Level.INFO, "GET - Obteniendo kardex del almacén: {0}", idAlmacen);

        try {
            // Validar que el almacén existe
            Almacen almacen = almacenDAO.findById(idAlmacen);
            if (almacen == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Almacén no encontrado\"}")
                        .build();
            }

            List<Kardex> kardexList = kardexDAO.findByAlmacen(idAlmacen);

            LOGGER.log(Level.INFO, "Se encontraron {0} movimientos para el almacén {1}",
                    new Object[]{kardexList.size(), idAlmacen});

            return Response.ok(kardexList).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener kardex del almacén " + idAlmacen, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener movimientos: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    // ==================== ENDPOINTS POR COMPRA/VENTA ====================

    /**
     * GET /compras-detalle/{idCompraDetalle}/kardex
     * Obtiene movimientos relacionados con un detalle de compra
     */
    @GET
    @Path("/compras-detalle/{idCompraDetalle}")
    public Response getKardexByCompraDetalle(@PathParam("idCompraDetalle") String idCompraDetalleStr) {
        LOGGER.log(Level.INFO, "GET - Obteniendo kardex del detalle de compra: {0}", idCompraDetalleStr);

        try {
            UUID idCompraDetalle = UUID.fromString(idCompraDetalleStr);
            List<Kardex> kardexList = kardexDAO.findByCompraDetalle(idCompraDetalle);

            return Response.ok(kardexList).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"ID de compra detalle inválido\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener kardex de compra detalle", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener movimientos: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET /ventas-detalle/{idVentaDetalle}/kardex
     * Obtiene movimientos relacionados con un detalle de venta
     */
    @GET
    @Path("/ventas-detalle/{idVentaDetalle}")
    public Response getKardexByVentaDetalle(@PathParam("idVentaDetalle") String idVentaDetalleStr) {
        LOGGER.log(Level.INFO, "GET - Obteniendo kardex del detalle de venta: {0}", idVentaDetalleStr);

        try {
            UUID idVentaDetalle = UUID.fromString(idVentaDetalleStr);
            List<Kardex> kardexList = kardexDAO.findByVentaDetalle(idVentaDetalle);

            return Response.ok(kardexList).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"ID de venta detalle inválido\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener kardex de venta detalle", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener movimientos: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}