package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.AlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoAlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Almacen;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;

import java.util.List;

@Path("tipo_almacen/{idTipoAlmacen}/almacen")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlmacenResource {

    @Inject
    AlmacenDAO almacenDAO;

    @Inject
    TipoAlmacenDAO tipoAlmacenDAO;

    /**
     * GET /tipo_almacen/{idTipoAlmacen}/almacen
     * Obtiene todos los almacenes de un tipo específico
     */
    @GET
    public Response getAlmacenesPorTipo(@PathParam("idTipoAlmacen") Integer idTipoAlmacen) {
        if (idTipoAlmacen == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El ID del tipo de almacén no puede ser nulo")
                    .build();
        }

        try {
            // Verificar que existe el tipo de almacén
            TipoAlmacen tipoAlmacen = tipoAlmacenDAO.findById(idTipoAlmacen);
            if (tipoAlmacen == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No existe el tipo de almacén con id: " + idTipoAlmacen)
                        .build();
            }

            List<Almacen> almacenes = almacenDAO.findByTipoAlmacen(idTipoAlmacen);

            if (almacenes.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No se encontraron almacenes para el tipo de almacén con id: " + idTipoAlmacen)
                        .build();
            }

            return Response.ok(almacenes).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener almacenes: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * GET /tipo_almacen/{idTipoAlmacen}/almacen/{idAlmacen}
     * Obtiene un almacén específico de un tipo de almacén
     */
    @GET
    @Path("/{idAlmacen}")
    public Response findById(@PathParam("idTipoAlmacen") Integer idTipoAlmacen,
                             @PathParam("idAlmacen") Integer idAlmacen) {
        if (idAlmacen == null || idTipoAlmacen == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Los IDs no pueden ser nulos")
                    .build();
        }

        try {
            Almacen almacen = almacenDAO.findById(idAlmacen);

            if (almacen == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Almacén con id " + idAlmacen + " no encontrado")
                        .build();
            }

            // Validar que el almacén pertenezca al tipo especificado
            if (almacen.getIdTipoAlmacen() == null ||
                    !almacen.getIdTipoAlmacen().getId().equals(idTipoAlmacen)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("El almacén no pertenece al tipo de almacén especificado")
                        .build();
            }

            return Response.ok(almacen).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al buscar el almacén: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * POST /tipo_almacen/{idTipoAlmacen}/almacen
     * Crea un nuevo almacén para un tipo específico
     */
    @POST
    public Response create(@PathParam("idTipoAlmacen") Integer idTipoAlmacen,
                           Almacen entity,
                           @Context UriInfo uriInfo) {
        if (idTipoAlmacen == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El ID del tipo de almacén no puede ser nulo")
                    .build();
        }

        if (entity == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("La entidad no puede ser nula")
                    .build();
        }

        try {
            TipoAlmacen tipoAlmacen = tipoAlmacenDAO.findById(idTipoAlmacen);
            if (tipoAlmacen == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No existe el tipo de almacén con id: " + idTipoAlmacen)
                        .build();
            }

            // Asignar el tipo de almacén
            entity.setIdTipoAlmacen(tipoAlmacen);

            almacenDAO.crear(entity);

            return Response.created(
                    uriInfo.getAbsolutePathBuilder()
                            .path(entity.getId().toString())
                            .build()
            ).entity(entity).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al crear el almacén: " + e.getMessage())
                    .build();
        }
    }

    /**
     * PUT /tipo_almacen/{idTipoAlmacen}/almacen/{idAlmacen}
     * Actualiza un almacén existente
     */
    @PUT
    @Path("/{idAlmacen}")
    public Response update(@PathParam("idTipoAlmacen") Integer idTipoAlmacen,
                           @PathParam("idAlmacen") Integer idAlmacen,
                           Almacen entity) {
        if (idAlmacen == null || idTipoAlmacen == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Los IDs no pueden ser nulos")
                    .build();
        }

        if (entity == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("La entidad no puede ser nula")
                    .build();
        }

        try {
            Almacen existente = almacenDAO.findById(idAlmacen);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Almacén con id " + idAlmacen + " no encontrado")
                        .build();
            }

            // Validar que el almacén pertenezca al tipo especificado
            if (existente.getIdTipoAlmacen() == null ||
                    !existente.getIdTipoAlmacen().getId().equals(idTipoAlmacen)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("El almacén no pertenece al tipo de almacén especificado")
                        .build();
            }

            // Actualizar campos
            existente.setActivo(entity.getActivo());
            existente.setObservaciones(entity.getObservaciones());

            // Sí se envía un nuevo tipo de almacén en el body, actualizarlo
            if (entity.getIdTipoAlmacen() != null && entity.getIdTipoAlmacen().getId() != null) {
                TipoAlmacen nuevoTipo = tipoAlmacenDAO.findById(entity.getIdTipoAlmacen().getId());
                if (nuevoTipo == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("No existe el tipo de almacén con id: " + entity.getIdTipoAlmacen().getId())
                            .build();
                }
                existente.setIdTipoAlmacen(nuevoTipo);
            }

            almacenDAO.modificar(existente);

            return Response.ok(existente).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al actualizar el almacén: " + e.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /tipo_almacen/{idTipoAlmacen}/almacen/{idAlmacen}
     * Elimina un almacén
     */
    @DELETE
    @Path("/{idAlmacen}")
    public Response delete(@PathParam("idTipoAlmacen") Integer idTipoAlmacen,
                           @PathParam("idAlmacen") Integer idAlmacen) {
        if (idAlmacen == null || idTipoAlmacen == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Los IDs no pueden ser nulos")
                    .build();
        }

        try {
            Almacen existente = almacenDAO.findById(idAlmacen);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Almacén con id " + idAlmacen + " no encontrado")
                        .build();
            }

            // Validar que el almacén pertenezca al tipo especificado
            if (existente.getIdTipoAlmacen() == null ||
                    !existente.getIdTipoAlmacen().getId().equals(idTipoAlmacen)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("El almacén no pertenece al tipo de almacén especificado")
                        .build();
            }

            almacenDAO.eliminar(existente);
            return Response.noContent().build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al eliminar el almacén: " + ex.getMessage())
                    .build();
        }
    }
}