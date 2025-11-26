package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.KardexDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.KardexDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Kardex;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.KardexDetalle;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Recurso REST para manejar detalles de lote de un movimiento de kardex específico.
 * Patrón: /kardex/{idKardex}/detalles
 */
@Path("/kardex/{idKardex}/detalles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KardexDetalleResource {

    private static final Logger LOGGER = Logger.getLogger(KardexDetalleResource.class.getName());

    @Inject
    KardexDetalleDAO kardexDetalleDAO;

    @Inject
    KardexDAO kardexDAO;

    /**
     * GET /kardex/{idKardex}/detalles
     * Obtiene todos los detalles de lote de un movimiento de kardex
     */
    @GET
    public Response getDetallesByKardex(@PathParam("idKardex") String idKardexStr) {
        LOGGER.log(Level.INFO, "GET - Obteniendo detalles del kardex: {0}", idKardexStr);

        try {
            UUID idKardex = UUID.fromString(idKardexStr);

            // Validar que el kardex existe
            Kardex kardex = kardexDAO.findById(idKardex);
            if (kardex == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Kardex no encontrado\"}")
                        .build();
            }

            // Obtener todos los detalles y filtrar por kardex
            List<KardexDetalle> todosLosDetalles = kardexDetalleDAO.findAll();
            List<KardexDetalle> detallesDelKardex = todosLosDetalles.stream()
                    .filter(d -> d.getIdKardex() != null &&
                            d.getIdKardex().getId().equals(idKardex))
                    .collect(Collectors.toList());

            LOGGER.log(Level.INFO, "Se encontraron {0} detalles para el kardex {1}",
                    new Object[]{detallesDelKardex.size(), idKardex});

            return Response.ok(detallesDelKardex).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"ID de kardex inválido\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener detalles del kardex " + idKardexStr, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener detalles: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET /kardex/{idKardex}/detalles/{idDetalle}
     * Obtiene un detalle específico de un kardex
     */
    @GET
    @Path("/{idDetalle}")
    public Response getDetalleById(
            @PathParam("idKardex") String idKardexStr,
            @PathParam("idDetalle") String idDetalleStr) {

        LOGGER.log(Level.INFO, "GET - Obteniendo detalle {0} del kardex {1}",
                new Object[]{idDetalleStr, idKardexStr});

        try {
            UUID idKardex = UUID.fromString(idKardexStr);
            UUID idDetalle = UUID.fromString(idDetalleStr);

            KardexDetalle detalle = kardexDetalleDAO.findById(idDetalle);

            if (detalle == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Detalle no encontrado\"}")
                        .build();
            }

            // Validar que el detalle pertenece al kardex solicitado
            if (detalle.getIdKardex() == null ||
                    !detalle.getIdKardex().getId().equals(idKardex)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"El detalle no pertenece al kardex especificado\"}")
                        .build();
            }

            return Response.ok(detalle).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"ID inválido\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener detalle " + idDetalleStr, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener detalle: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET /kardex/{idKardex}/detalles/activos
     * Obtiene solo los detalles activos de un kardex
     */
    @GET
    @Path("/activos")
    public Response getDetallesActivos(@PathParam("idKardex") String idKardexStr) {
        LOGGER.log(Level.INFO, "GET - Obteniendo detalles activos del kardex: {0}", idKardexStr);

        try {
            UUID idKardex = UUID.fromString(idKardexStr);

            // Validar que el kardex existe
            Kardex kardex = kardexDAO.findById(idKardex);
            if (kardex == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Kardex no encontrado\"}")
                        .build();
            }

            // Obtener detalles activos
            List<KardexDetalle> todosLosDetalles = kardexDetalleDAO.findAll();
            List<KardexDetalle> detallesActivos = todosLosDetalles.stream()
                    .filter(d -> d.getIdKardex() != null &&
                            d.getIdKardex().getId().equals(idKardex) &&
                            d.getActivo() != null && d.getActivo())
                    .collect(Collectors.toList());

            LOGGER.log(Level.INFO, "Se encontraron {0} detalles activos para el kardex {1}",
                    new Object[]{detallesActivos.size(), idKardex});

            return Response.ok(detallesActivos).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"ID de kardex inválido\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener detalles activos", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener detalles activos: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * POST /kardex/{idKardex}/detalles
     * Crea un nuevo detalle de lote para un movimiento de kardex
     */
    @POST
    public Response createDetalle(
            @PathParam("idKardex") String idKardexStr,
            KardexDetalle detalle) {

        LOGGER.log(Level.INFO, "POST - Creando detalle para kardex: {0}", idKardexStr);

        try {
            UUID idKardex = UUID.fromString(idKardexStr);

            // Validar que el kardex existe
            Kardex kardex = kardexDAO.findById(idKardex);
            if (kardex == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Kardex no encontrado\"}")
                        .build();
            }

            // Validaciones básicas
            if (detalle.getLote() == null || detalle.getLote().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"El número de lote es obligatorio\"}")
                        .build();
            }

            // Generar UUID y asignar kardex
            detalle.setId(UUID.randomUUID());
            detalle.setIdKardex(kardex);

            // Si no se especifica, el detalle está activo por defecto
            if (detalle.getActivo() == null) {
                detalle.setActivo(true);
            }

            // Crear el detalle
            kardexDetalleDAO.crear(detalle);

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
     * PUT /kardex/{idKardex}/detalles/{idDetalle}
     * Actualiza un detalle existente
     */
    @PUT
    @Path("/{idDetalle}")
    public Response updateDetalle(
            @PathParam("idKardex") String idKardexStr,
            @PathParam("idDetalle") String idDetalleStr,
            KardexDetalle detalle) {

        LOGGER.log(Level.INFO, "PUT - Actualizando detalle {0} del kardex {1}",
                new Object[]{idDetalleStr, idKardexStr});

        try {
            UUID idKardex = UUID.fromString(idKardexStr);
            UUID idDetalle = UUID.fromString(idDetalleStr);

            // Buscar el detalle existente
            KardexDetalle detalleExistente = kardexDetalleDAO.findById(idDetalle);

            if (detalleExistente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Detalle no encontrado\"}")
                        .build();
            }

            // Validar que el detalle pertenece al kardex
            if (detalleExistente.getIdKardex() == null ||
                    !detalleExistente.getIdKardex().getId().equals(idKardex)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"El detalle no pertenece al kardex especificado\"}")
                        .build();
            }

            // Actualizar campos (NO se permite cambiar el kardex)
            if (detalle.getLote() != null) {
                detalleExistente.setLote(detalle.getLote());
            }
            if (detalle.getActivo() != null) {
                detalleExistente.setActivo(detalle.getActivo());
            }

            // Modificar
            KardexDetalle detalleActualizado = kardexDetalleDAO.modificar(detalleExistente);

            LOGGER.log(Level.INFO, "Detalle actualizado exitosamente: {0}", idDetalle);

            return Response.ok(detalleActualizado).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"ID inválido\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar detalle " + idDetalleStr, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al actualizar detalle: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * DELETE /kardex/{idKardex}/detalles/{idDetalle}
     * Elimina (o desactiva) un detalle de kardex
     */
    @DELETE
    @Path("/{idDetalle}")
    public Response deleteDetalle(
            @PathParam("idKardex") String idKardexStr,
            @PathParam("idDetalle") String idDetalleStr,
            @QueryParam("softDelete") @DefaultValue("false") boolean softDelete) {

        LOGGER.log(Level.INFO, "DELETE - Eliminando detalle {0} del kardex {1}",
                new Object[]{idDetalleStr, idKardexStr});

        try {
            UUID idKardex = UUID.fromString(idKardexStr);
            UUID idDetalle = UUID.fromString(idDetalleStr);

            KardexDetalle detalle = kardexDetalleDAO.findById(idDetalle);

            if (detalle == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Detalle no encontrado\"}")
                        .build();
            }

            // Validar que el detalle pertenece al kardex
            if (detalle.getIdKardex() == null ||
                    !detalle.getIdKardex().getId().equals(idKardex)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"El detalle no pertenece al kardex especificado\"}")
                        .build();
            }

            // Soft delete (desactivar) o hard delete (eliminar físicamente)
            if (softDelete) {
                detalle.setActivo(false);
                kardexDetalleDAO.modificar(detalle);
                LOGGER.log(Level.INFO, "Detalle desactivado exitosamente: {0}", idDetalle);
                return Response.ok(detalle).build();
            } else {
                kardexDetalleDAO.eliminar(detalle);
                LOGGER.log(Level.INFO, "Detalle eliminado exitosamente: {0}", idDetalle);
                return Response.noContent().build();
            }

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"ID inválido\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar detalle " + idDetalleStr, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al eliminar detalle: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET /kardex/{idKardex}/detalles/count
     * Cuenta los detalles de un kardex
     */
    @GET
    @Path("/count")
    public Response countDetallesByKardex(
            @PathParam("idKardex") String idKardexStr,
            @QueryParam("soloActivos") @DefaultValue("false") boolean soloActivos) {

        LOGGER.log(Level.INFO, "GET - Contando detalles del kardex: {0}", idKardexStr);

        try {
            UUID idKardex = UUID.fromString(idKardexStr);

            // Validar que el kardex existe
            Kardex kardex = kardexDAO.findById(idKardex);
            if (kardex == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Kardex no encontrado\"}")
                        .build();
            }

            List<KardexDetalle> todosLosDetalles = kardexDetalleDAO.findAll();
            long count = todosLosDetalles.stream()
                    .filter(d -> d.getIdKardex() != null &&
                            d.getIdKardex().getId().equals(idKardex) &&
                            (!soloActivos || (d.getActivo() != null && d.getActivo())))
                    .count();

            return Response.ok("{\"count\": " + count + "}").build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"ID de kardex inválido\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al contar detalles del kardex " + idKardexStr, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al contar detalles: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET /kardex/{idKardex}/detalles/lotes
     * Obtiene la lista de números de lote únicos de un kardex
     */
    @GET
    @Path("/lotes")
    public Response getLotesByKardex(@PathParam("idKardex") String idKardexStr) {
        LOGGER.log(Level.INFO, "GET - Obteniendo lotes del kardex: {0}", idKardexStr);

        try {
            UUID idKardex = UUID.fromString(idKardexStr);

            // Validar que el kardex existe
            Kardex kardex = kardexDAO.findById(idKardex);
            if (kardex == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Kardex no encontrado\"}")
                        .build();
            }

            List<KardexDetalle> todosLosDetalles = kardexDetalleDAO.findAll();
            List<String> lotes = todosLosDetalles.stream()
                    .filter(d -> d.getIdKardex() != null &&
                            d.getIdKardex().getId().equals(idKardex) &&
                            d.getLote() != null && !d.getLote().isBlank())
                    .map(KardexDetalle::getLote)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            return Response.ok(lotes).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"ID de kardex inválido\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener lotes del kardex " + idKardexStr, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener lotes: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}