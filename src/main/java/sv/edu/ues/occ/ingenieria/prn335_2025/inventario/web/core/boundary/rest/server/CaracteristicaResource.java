package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoUnidadMedidaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoUnidadMedida;

import java.util.List;

/**
 * Recurso REST para manejar características bajo un tipo de unidad de medida específico.
 * Patrón: /tipo_unidad_medida/{idTipoUnidadMedida}/caracteristica
 */
@Path("tipo_unidad_medida/{idTipoUnidadMedida}/caracteristica")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CaracteristicaResource {

    @Inject
    CaracteristicaDAO caracteristicaDAO;

    @Inject
    TipoUnidadMedidaDAO tipoUnidadMedidaDAO;

    /**
     * GET /tipo_unidad_medida/{idTipoUnidadMedida}/caracteristica
     * Obtiene todas las características de un tipo de unidad de medida específico
     */
    @GET
    public Response getCaracteristicasPorTipo(@PathParam("idTipoUnidadMedida") Integer idTipoUnidadMedida) {
        if (idTipoUnidadMedida == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El ID del tipo de unidad de medida no puede ser nulo")
                    .build();
        }

        try {
            // Verificar que existe el tipo de unidad de medida
            TipoUnidadMedida tipoUnidadMedida = tipoUnidadMedidaDAO.findById(idTipoUnidadMedida);
            if (tipoUnidadMedida == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No existe el tipo de unidad de medida con id: " + idTipoUnidadMedida)
                        .build();
            }

            List<Caracteristica> caracteristicas = caracteristicaDAO.findByTipoUnidadMedida(idTipoUnidadMedida);

            return Response.ok(caracteristicas).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener características: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * GET /tipo_unidad_medida/{idTipoUnidadMedida}/caracteristica/{idCaracteristica}
     * Obtiene una característica específica de un tipo de unidad de medida
     */
    @GET
    @Path("/{idCaracteristica}")
    public Response findById(@PathParam("idTipoUnidadMedida") Integer idTipoUnidadMedida,
                             @PathParam("idCaracteristica") Integer idCaracteristica) {
        if (idCaracteristica == null || idTipoUnidadMedida == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Los IDs no pueden ser nulos")
                    .build();
        }

        try {
            Caracteristica caracteristica = caracteristicaDAO.findById(idCaracteristica);

            if (caracteristica == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Característica con id " + idCaracteristica + " no encontrada")
                        .build();
            }

            // Validar que la característica pertenezca al tipo especificado
            if (caracteristica.getIdTipoUnidadMedida() == null ||
                    !caracteristica.getIdTipoUnidadMedida().getId().equals(idTipoUnidadMedida)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("La característica no pertenece al tipo de unidad de medida especificado")
                        .build();
            }

            return Response.ok(caracteristica).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al buscar la característica: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * POST /tipo_unidad_medida/{idTipoUnidadMedida}/caracteristica
     * Crea una nueva característica para un tipo de unidad de medida específico
     */
    @POST
    public Response create(@PathParam("idTipoUnidadMedida") Integer idTipoUnidadMedida,
                           Caracteristica entity,
                           @Context UriInfo uriInfo) {
        if (idTipoUnidadMedida == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El ID del tipo de unidad de medida no puede ser nulo")
                    .build();
        }

        if (entity == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("La entidad no puede ser nula")
                    .build();
        }

        try {
            TipoUnidadMedida tipoUnidadMedida = tipoUnidadMedidaDAO.findById(idTipoUnidadMedida);
            if (tipoUnidadMedida == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No existe el tipo de unidad de medida con id: " + idTipoUnidadMedida)
                        .build();
            }

            // Asignar el tipo de unidad de medida desde la URL
            entity.setIdTipoUnidadMedida(tipoUnidadMedida);

            caracteristicaDAO.crear(entity);

            return Response.created(
                    uriInfo.getAbsolutePathBuilder()
                            .path(entity.getId().toString())
                            .build()
            ).entity(entity).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Datos inválidos: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al crear la característica: " + e.getMessage())
                    .build();
        }
    }

    /**
     * PUT /tipo_unidad_medida/{idTipoUnidadMedida}/caracteristica/{idCaracteristica}
     * Actualiza una característica existente
     */
    @PUT
    @Path("/{idCaracteristica}")
    public Response update(@PathParam("idTipoUnidadMedida") Integer idTipoUnidadMedida,
                           @PathParam("idCaracteristica") Integer idCaracteristica,
                           Caracteristica entity) {
        if (idCaracteristica == null || idTipoUnidadMedida == null) {
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
            Caracteristica existente = caracteristicaDAO.findById(idCaracteristica);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Característica con id " + idCaracteristica + " no encontrada")
                        .build();
            }

            // Validar que la característica pertenezca al tipo especificado en la URL
            if (existente.getIdTipoUnidadMedida() == null ||
                    !existente.getIdTipoUnidadMedida().getId().equals(idTipoUnidadMedida)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("La característica no pertenece al tipo de unidad de medida especificado")
                        .build();
            }

            // Actualizar campos permitidos
            if (entity.getNombre() != null) {
                existente.setNombre(entity.getNombre());
            }
            if (entity.getActivo() != null) {
                existente.setActivo(entity.getActivo());
            }
            if (entity.getDescripcion() != null) {
                existente.setDescripcion(entity.getDescripcion());
            }

            // Permitir cambiar el tipo de unidad de medida si viene en el body
            if (entity.getIdTipoUnidadMedida() != null && entity.getIdTipoUnidadMedida().getId() != null) {
                TipoUnidadMedida nuevoTipo = tipoUnidadMedidaDAO.findById(entity.getIdTipoUnidadMedida().getId());
                if (nuevoTipo == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("No existe el tipo de unidad de medida con id: " + entity.getIdTipoUnidadMedida().getId())
                            .build();
                }
                existente.setIdTipoUnidadMedida(nuevoTipo);
            }

            caracteristicaDAO.modificar(existente);

            return Response.ok(existente).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al actualizar la característica: " + e.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /tipo_unidad_medida/{idTipoUnidadMedida}/caracteristica/{idCaracteristica}
     * Elimina una característica
     */
    @DELETE
    @Path("/{idCaracteristica}")
    public Response delete(@PathParam("idTipoUnidadMedida") Integer idTipoUnidadMedida,
                           @PathParam("idCaracteristica") Integer idCaracteristica) {
        if (idCaracteristica == null || idTipoUnidadMedida == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Los IDs no pueden ser nulos")
                    .build();
        }

        try {
            Caracteristica existente = caracteristicaDAO.findById(idCaracteristica);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Característica con id " + idCaracteristica + " no encontrada")
                        .build();
            }

            // Validar que la característica pertenezca al tipo especificado
            if (existente.getIdTipoUnidadMedida() == null ||
                    !existente.getIdTipoUnidadMedida().getId().equals(idTipoUnidadMedida)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("La característica no pertenece al tipo de unidad de medida especificado")
                        .build();
            }

            caracteristicaDAO.eliminar(existente);
            return Response.noContent().build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al eliminar la característica: " + ex.getMessage())
                    .build();
        }
    }
}
