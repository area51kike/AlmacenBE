package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ClienteDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Cliente;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Venta;

import java.util.List;
import java.util.UUID;

@Path("cliente/{idCliente}/venta")
@Produces(MediaType.APPLICATION_JSON)
public class VentaResource {

    @Inject
    VentaDAO ventaDAO;

    @Inject
    ClienteDAO clienteDAO;

    /**
     * GET /cliente/{idCliente}/venta
     * Obtiene todas las ventas de un cliente
     */
    @GET
    public Response getVentasproCliente(@PathParam("idCliente") UUID idCliente) {
        if (idCliente == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "idCliente no debe ser nulo")
                    .build();
        }

        try {
            // Verificar que el cliente existe
            Cliente cliente = clienteDAO.findById(idCliente);
            if (cliente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Cliente with id " + idCliente + " not found")
                        .build();
            }

            // Obtener las asociadas
            List<Venta> ventas = ventaDAO.buscarPorCliente(idCliente);

            return Response.ok(ventas != null ? ventas : List.of()).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Server-Exception", "Error: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * GET /cliente/{idCliente}/venta/{idVenta}
     * Obtiene una venta específica
     */
    @GET
    @Path("{idVenta}")
    public Response findById(
            @PathParam("idCliente") UUID idCliente,
            @PathParam("idVenta") UUID idVenta) {

        if (idVenta == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "idVenta no debe ser nulo")
                    .build();
        }

        try {
            Venta venta = ventaDAO.findById(idVenta);

            if (venta == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Record with id " + idVenta + " not found")
                        .build();
            }

            // Verificar que pertenece al cliente correcto
            if (!venta.getIdCliente().getId().equals(idCliente)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Venta no pertenece a este cliente")
                        .build();
            }

            return Response.ok(venta).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Server-Exception", "Cannot access db: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * POST /cliente/{idCliente}/venta
     * Crea una nueva para un cliente
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(
            @PathParam("idCliente") UUID idCliente,
            Venta entity,
            @Context UriInfo uriInfo) {

        if (idCliente == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "idCliente no debe ser nulo")
                    .build();
        }

        if (entity == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "Request body is required")
                    .build();
        }

        // Si viene con ID, rechazar pq debe ser nuevo
        if (entity.getId() != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "idVenta must be null for new records")
                    .build();
        }

        try {
            // Verificar que existe el cliente
            Cliente cliente = clienteDAO.findById(idCliente);
            if (cliente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Cliente with id " + idCliente + " not found")
                        .build();
            }

            entity.setIdCliente(cliente);
            entity.setId(UUID.randomUUID());
            ventaDAO.crear(entity);

            return Response.created(
                    uriInfo.getAbsolutePathBuilder()
                            .path(entity.getId().toString())
                            .build()
            ).entity(entity).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Server-Exception", "Error creating: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * PUT /cliente/{idCliente}/venta/{idVenta}
     * Actualiza una venta
     */
    @PUT
    @Path("{idVenta}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("idCliente") UUID idCliente,
            @PathParam("idVenta") UUID idVenta,
            Venta entity) {

        if (idCliente == null || idVenta == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "IDs cannot be null")
                    .build();
        }

        if (entity == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "Request body is required")
                    .build();
        }

        try {
            // Buscar registro existente
            Venta existente = ventaDAO.findById(idVenta);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Record with id " + idVenta + " not found")
                        .build();
            }

            // Verificar que pertenece al cliente del path
            if (!existente.getIdCliente().getId().equals(idCliente)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Venta no pertenece al cliente")
                        .build();
            }

            // Manejar actualización de idCliente
            if (entity.getIdCliente() != null) {
                UUID idClienteBody = entity.getIdCliente().getId();
                if (idClienteBody == null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .header("Error", "idCliente.id cannot be null")
                            .build();
                }

                Cliente nuevoCliente = clienteDAO.findById(idClienteBody);
                if (nuevoCliente == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .header("Not-Found", "Cliente with id " + idClienteBody + " not found")
                            .build();
                }
                existente.setIdCliente(nuevoCliente);
            }

            // Actualizar otros campos
            if (entity.getObservaciones() != null) {
                existente.setObservaciones(entity.getObservaciones());
            }
            if (entity.getEstado() != null) {
                existente.setEstado(entity.getEstado());
            }
            if (entity.getFecha() != null) {
                existente.setFecha(entity.getFecha());
            }

            ventaDAO.modificar(existente);
            return Response.ok(existente).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Server-Exception", "Error updating: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /cliente/{idCliente}/venta/{idVenta}
     * Elimina una venta
     */
    @DELETE
    @Path("{idVenta}")
    public Response delete(
            @PathParam("idCliente") UUID idCliente,
            @PathParam("idVenta") UUID idVenta) {

        if (idVenta == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "idVenta no debe ser nulo")
                    .build();
        }

        try {
            // Buscar registro
            Venta existente = ventaDAO.findById(idVenta);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Record with id " + idVenta + " not found")
                        .build();
            }

            // Verificar que pertenece al tipo de almacén correcto
            if (!existente.getIdCliente().getId().equals(idCliente)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Venta no pertenece a este cliente")
                        .build();
            }

            ventaDAO.eliminar(existente);
            return Response.noContent().build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Server-Exception", "Cannot delete record: " + ex.getMessage())
                    .build();
        }
    }
}