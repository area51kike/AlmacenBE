package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoTipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("producto_tipo_producto/{idPTP}/caracteristica")
@Produces(MediaType.APPLICATION_JSON)
public class ProductoTipoProductoCaracteristicaResource {

    @Inject
    ProductoTipoProductoCaracteristicaDAO productoTipoProductoCaracteristicaDAO;

    @Inject
    ProductoTipoProductoDAO productoTipoProductoDAO;

    @Inject
    TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDAO;

    /**
     * GET /producto_tipo_producto/{idPTP}/caracteristica
     * Obtiene un resumen de características con sus valores
     */
    @GET
    public Response getCaracteristicas(@PathParam("idPTP") UUID idPTP) {

        if (idPTP == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "idPTP cannot be null")
                    .build();
        }

        try {
            // Verificar que el ProductoTipoProducto existe
            ProductoTipoProducto productoTipoProducto = productoTipoProductoDAO.findById(idPTP);
            if (productoTipoProducto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "ProductoTipoProducto with id " + idPTP + " not found")
                        .build();
            }

            // Buscar características
            List<ProductoTipoProductoCaracteristica> caracteristicas =
                    productoTipoProductoCaracteristicaDAO.findByIdProductoTipoProducto(idPTP);

            if (caracteristicas == null || caracteristicas.isEmpty()) {
                return Response.ok(List.of()).build();
            }

            List<Map<String, Object>> resumen = caracteristicas.stream()
                    .map(c -> {
                        Map<String, Object> map = new java.util.HashMap<>();
                        map.put("id", c.getId());
                        map.put("nombreCaracteristica", c.getIdTipoProductoCaracteristica().getCaracteristica().getNombre());
                        map.put("valor", c.getValor() != null ? c.getValor() : "");
                        map.put("observaciones", c.getObservaciones() != null ? c.getObservaciones() : "");
                        map.put("obligatorio", c.getIdTipoProductoCaracteristica().getObligatorio());
                        return map;
                    })
                    .collect(Collectors.toList());

            return Response.ok(resumen).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Server-Exception", "Error: " + ex.getMessage())
                    .build();
        }
    }


    /**
     * GET /producto_tipo_producto/{idPTP}/caracteristica/{idPTPC}
     * Obtiene productoTipoProductoCarateristica
     */
    @GET
    @Path("{idPTPC}")
    public Response findById(
            @PathParam("idPTP") UUID idPTP,
            @PathParam("idPTPC") UUID idPTPC) {

        if (idPTPC == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "idPTPC cannot be null")
                    .build();
        }

        try {
            ProductoTipoProductoCaracteristica ptpc =
                    productoTipoProductoCaracteristicaDAO.findById(idPTPC);

            if (ptpc == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Record with id " + idPTPC + " not found")
                        .build();
            }

            // Verificar que pertenece al ProductoTipoProducto correcto
            if (ptpc.getIdProductoTipoProducto() == null ||
                    !ptpc.getIdProductoTipoProducto().getId().equals(idPTP)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Caracteristica no pertenece a este ProductoTipoProducto")
                        .build();
            }

            return Response.ok(ptpc).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Server-Exception", "Cannot access db: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * POST /producto_tipo_producto/{idPTP}/caracteristica
     * Asocia una característica a un producto_tipo_producto
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(
            @PathParam("idPTP") UUID idPTP,
            ProductoTipoProductoCaracteristica entity,
            @Context UriInfo uriInfo) {

        if (idPTP == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "idPTP cannot be null")
                    .build();
        }

        if (entity == null || entity.getIdTipoProductoCaracteristica() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "idTipoProductoCaracteristica is required")
                    .build();
        }

        // Si viene con ID, rechazar (debe ser nuevo)
        if (entity.getId() != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "idProductoTipoProductoCaracteristica no debe agregarse")
                    .build();
        }

        try {
            // Verificar que existe el ProductoTipoProducto
            ProductoTipoProducto productoTipoProducto = productoTipoProductoDAO.findById(idPTP);
            if (productoTipoProducto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "ProductoTipoProducto with id " + idPTP + " not found")
                        .build();
            }

            // Validar consistencia si viene idProductoTipoProducto en el body
            if (entity.getIdProductoTipoProducto() != null) {
                if (!entity.getIdProductoTipoProducto().getId().equals(idPTP)) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .header("Error", "idProductoTipoProducto del json y el del path no coinciden")
                            .build();
                }
            }

            // Extraer ID de la característica
            Long idTipoProductoCaracteristica = entity.getIdTipoProductoCaracteristica().getId();
            if (idTipoProductoCaracteristica == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .header("Error", "idTipoProductoCaracteristica.id no puede ser null")
                        .build();
            }

            // Verificar que existe la TipoProductoCaracteristica
            TipoProductoCaracteristica tipoProductoCaracteristica =
                    tipoProductoCaracteristicaDAO.findById(idTipoProductoCaracteristica);
            if (tipoProductoCaracteristica == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "TipoProductoCaracteristica with id " +
                                idTipoProductoCaracteristica + " not found")
                        .build();
            }

            // Asignar relaciones
            entity.setIdProductoTipoProducto(productoTipoProducto);
            entity.setIdTipoProductoCaracteristica(tipoProductoCaracteristica);
            entity.setId(UUID.randomUUID());
            productoTipoProductoCaracteristicaDAO.crear(entity);

            return Response.created(uriInfo.getAbsolutePathBuilder()
                            .path(entity.getId().toString())
                            .build())
                    .entity(entity)
                    .build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Server-Exception", "Error creating: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * PUT /producto_tipo_producto/{idPTP}/caracteristica/{idPTPC}
     * Actualiza una característica de producto_tipo_producto
     */
    @PUT
    @Path("{idPTPC}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("idPTP") UUID idPTP,
            @PathParam("idPTPC") UUID idPTPC,
            ProductoTipoProductoCaracteristica entity) {

        if (idPTP == null || idPTPC == null) {
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
            ProductoTipoProductoCaracteristica existente =
                    productoTipoProductoCaracteristicaDAO.findById(idPTPC);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Record with id " + idPTPC + " not found")
                        .build();
            }

            // Verificar que pertenece al ProductoTipoProducto del path
            if (existente.getIdProductoTipoProducto() == null ||
                    !existente.getIdProductoTipoProducto().getId().equals(idPTP)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Caracteristica no pertenece a este ProductoTipoProducto")
                        .build();
            }

            // Manejar actualización de idProductoTipoProducto
            if (entity.getIdProductoTipoProducto() != null) {
                UUID idPTPBody = entity.getIdProductoTipoProducto().getId();
                if (idPTPBody == null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .header("Error", "idProductoTipoProducto.id cannot be null")
                            .build();
                }

                ProductoTipoProducto nuevoPTP = productoTipoProductoDAO.findById(idPTPBody);
                if (nuevoPTP == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .header("Not-Found", "ProductoTipoProducto with id " + idPTPBody + " not found")
                            .build();
                }
                existente.setIdProductoTipoProducto(nuevoPTP);
            }

            // Manejar actualización de idTipoProductoCaracteristica
            if (entity.getIdTipoProductoCaracteristica() != null) {
                Long idTPCBody = entity.getIdTipoProductoCaracteristica().getId();
                if (idTPCBody == null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .header("Error", "idTipoProductoCaracteristica.id no puede ser null")
                            .build();
                }

                TipoProductoCaracteristica nuevaTPC = tipoProductoCaracteristicaDAO.findById(idTPCBody);
                if (nuevaTPC == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .header("Not-Found", "TipoProductoCaracteristica with id " + idTPCBody + " not found")
                            .build();
                }
                existente.setIdTipoProductoCaracteristica(nuevaTPC);
            }

            // Actualizar campos específicos
            if (entity.getValor() != null) {
                existente.setValor(entity.getValor());
            }
            if (entity.getObservaciones() != null) {
                existente.setObservaciones(entity.getObservaciones());
            }

            productoTipoProductoCaracteristicaDAO.modificar(existente);

            return Response.ok(existente).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Server-Exception", "Error updating: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /producto_tipo_producto/{idPTP}/caracteristica/{idPTPC}
     * Elimina la asociación de una característica
     */
    @DELETE
    @Path("{idPTPC}")
    public Response delete(
            @PathParam("idPTP") UUID idPTP,
            @PathParam("idPTPC") UUID idPTPC) {

        if (idPTPC == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "idPTPC cannot be null")
                    .build();
        }

        try {
            // Buscar registro
            ProductoTipoProductoCaracteristica existente =
                    productoTipoProductoCaracteristicaDAO.findById(idPTPC);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Record with id " + idPTPC + " not found")
                        .build();
            }

            // Verificar que pertenece al ProductoTipoProducto correcto
            if (existente.getIdProductoTipoProducto() == null ||
                    !existente.getIdProductoTipoProducto().getId().equals(idPTP)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Caracteristica no pertenece a este ProductoTipoProducto")
                        .build();
            }

            productoTipoProductoCaracteristicaDAO.eliminar(existente);

            return Response.noContent().build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Server-Exception", "Cannot delete record: " + ex.getMessage())
                    .build();
        }
    }
}