package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProveedorDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Proveedor;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Recurso REST para manejar compras de un proveedor específico.
 * Patrón: /proveedores/{idProveedor}/compras
 */
@Path("/proveedores/{idProveedor}/compras")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompraResource {

    private static final Logger LOGGER = Logger.getLogger(CompraResource.class.getName());

    @Inject
    CompraDAO compraDAO;

    @Inject
    ProveedorDAO proveedorDAO;

    /**
     * GET /proveedores/{idProveedor}/compras
     * Obtiene todas las compras de un proveedor
     */
    @GET
    public Response getComprasByProveedor(@PathParam("idProveedor") Integer idProveedor) {
        LOGGER.log(Level.INFO, "GET - Obteniendo compras del proveedor: {0}", idProveedor);

        try {
            // Validar que el proveedor existe
            Proveedor proveedor = proveedorDAO.findById(idProveedor);
            if (proveedor == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Proveedor no encontrado\"}")
                        .build();
            }

            // Obtener todas las compras y filtrar por proveedor
            List<Compra> todasLasCompras = compraDAO.findAll();
            List<Compra> comprasDelProveedor = todasLasCompras.stream()
                    .filter(c -> c.getIdProveedor() != null && c.getIdProveedor().equals(idProveedor))
                    .collect(Collectors.toList());

            LOGGER.log(Level.INFO, "Se encontraron {0} compras para el proveedor {1}",
                    new Object[]{comprasDelProveedor.size(), idProveedor});

            return Response.ok(comprasDelProveedor).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener compras del proveedor " + idProveedor, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener compras: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET /proveedores/{idProveedor}/compras/{idCompra}
     * Obtiene una compra específica de un proveedor
     */
    @GET
    @Path("/{idCompra}")
    public Response getCompraById(
            @PathParam("idProveedor") Integer idProveedor,
            @PathParam("idCompra") Long idCompra) {

        LOGGER.log(Level.INFO, "GET - Obteniendo compra {0} del proveedor {1}",
                new Object[]{idCompra, idProveedor});

        try {
            Compra compra = compraDAO.findById(idCompra);

            if (compra == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Compra no encontrada\"}")
                        .build();
            }

            // Validar que la compra pertenece al proveedor solicitado
            if (!compra.getIdProveedor().equals(idProveedor)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"La compra no pertenece al proveedor especificado\"}")
                        .build();
            }

            return Response.ok(compra).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener compra " + idCompra, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener compra: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * POST /proveedores/{idProveedor}/compras
     * Crea una nueva compra para un proveedor
     */
    @POST
    public Response createCompra(
            @PathParam("idProveedor") Integer idProveedor,
            Compra compra) {

        LOGGER.log(Level.INFO, "POST - Creando compra para proveedor: {0}", idProveedor);

        try {
            // Validar que el proveedor existe
            Proveedor proveedor = proveedorDAO.findById(idProveedor);
            if (proveedor == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Proveedor no encontrado\"}")
                        .build();
            }

            // Asignar el proveedor del path a la compra
            compra.setIdProveedor(idProveedor);

            // Validaciones básicas
            if (compra.getFecha() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"La fecha es obligatoria\"}")
                        .build();
            }

            // Crear la compra
            compraDAO.crear(compra);

            LOGGER.log(Level.INFO, "Compra creada exitosamente con ID: {0}", compra.getId());

            return Response.status(Response.Status.CREATED)
                    .entity(compra)
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Error de validación al crear compra", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear compra", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al crear compra: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * PUT /proveedores/{idProveedor}/compras/{idCompra}
     * Actualiza una compra existente
     */
    @PUT
    @Path("/{idCompra}")
    public Response updateCompra(
            @PathParam("idProveedor") Integer idProveedor,
            @PathParam("idCompra") Long idCompra,
            Compra compra) {

        LOGGER.log(Level.INFO, "PUT - Actualizando compra {0} del proveedor {1}",
                new Object[]{idCompra, idProveedor});

        try {
            // Buscar la compra existente
            Compra compraExistente = compraDAO.findById(idCompra);

            if (compraExistente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Compra no encontrada\"}")
                        .build();
            }

            // Validar que la compra pertenece al proveedor
            if (!compraExistente.getIdProveedor().equals(idProveedor)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"La compra no pertenece al proveedor especificado\"}")
                        .build();
            }

            // Actualizar campos (mantener el ID y el proveedor)
            compraExistente.setFecha(compra.getFecha());
            compraExistente.setEstado(compra.getEstado());
            compraExistente.setObservaciones(compra.getObservaciones());

            // Modificar
            Compra compraActualizada = compraDAO.modificar(compraExistente);

            LOGGER.log(Level.INFO, "Compra actualizada exitosamente: {0}", idCompra);

            return Response.ok(compraActualizada).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar compra " + idCompra, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al actualizar compra: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * DELETE /proveedores/{idProveedor}/compras/{idCompra}
     * Elimina una compra
     */
    @DELETE
    @Path("/{idCompra}")
    public Response deleteCompra(
            @PathParam("idProveedor") Integer idProveedor,
            @PathParam("idCompra") Long idCompra) {

        LOGGER.log(Level.INFO, "DELETE - Eliminando compra {0} del proveedor {1}",
                new Object[]{idCompra, idProveedor});

        try {
            Compra compra = compraDAO.findById(idCompra);

            if (compra == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Compra no encontrada\"}")
                        .build();
            }

            // Validar que la compra pertenece al proveedor
            if (!compra.getIdProveedor().equals(idProveedor)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"La compra no pertenece al proveedor especificado\"}")
                        .build();
            }

            // Eliminar
            compraDAO.eliminar(compra);

            LOGGER.log(Level.INFO, "Compra eliminada exitosamente: {0}", idCompra);

            return Response.noContent().build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar compra " + idCompra, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al eliminar compra: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET /proveedores/{idProveedor}/compras/count
     * Cuenta las compras de un proveedor
     */
    @GET
    @Path("/count")
    public Response countComprasByProveedor(@PathParam("idProveedor") Integer idProveedor) {
        LOGGER.log(Level.INFO, "GET - Contando compras del proveedor: {0}", idProveedor);

        try {
            // Validar que el proveedor existe
            Proveedor proveedor = proveedorDAO.findById(idProveedor);
            if (proveedor == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Proveedor no encontrado\"}")
                        .build();
            }

            List<Compra> todasLasCompras = compraDAO.findAll();
            long count = todasLasCompras.stream()
                    .filter(c -> c.getIdProveedor() != null && c.getIdProveedor().equals(idProveedor))
                    .count();

            return Response.ok("{\"count\": " + count + "}").build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al contar compras del proveedor " + idProveedor, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al contar compras: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}