package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.*;

import java.util.List;
import java.util.stream.Collectors;

@Path("tipo_producto/{idTipoProducto}/caracteristica")
@Produces(MediaType.APPLICATION_JSON)
public class TipoProductoCaracteristicaResource {

    @Inject
    TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDAO;

    @Inject
    CaracteristicaDAO caracteristicaDAO;

    @Inject
    TipoProductoDAO tipoProductoDAO;

    @GET
    public Response getCaracteristicas(@PathParam("idTipoProducto") Long idTipoProducto) {

        if (idTipoProducto == null || idTipoProducto <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "idTipoProducto must be a positive number")
                    .build();
        }

        try {
            TipoProducto tipoProducto = tipoProductoDAO.findById(idTipoProducto);
            if (tipoProducto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "TipoProducto with id " + idTipoProducto + " not found")
                        .build();
            }

            List<TipoProductoCaracteristica> relaciones =
                    tipoProductoCaracteristicaDAO.findByIdTipoProducto(idTipoProducto);

            if (relaciones == null || relaciones.isEmpty()) {
                return Response.ok(List.of()).build();
            }

            List<Caracteristica> caracteristicas = relaciones.stream()
                    .map(TipoProductoCaracteristica::getCaracteristica)
                    .collect(Collectors.toList());

            return Response.ok(caracteristicas).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Server-Exception", "Error: " + ex.getMessage())
                    .build();
        }
    }

    @GET
    @Path("{idTPC}")
    public Response findById(
            @PathParam("idTipoProducto") Long idTipoProducto,
            @PathParam("idTPC") Long idTPC) {

        if (idTPC == null || idTPC <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "idTPC must be a positive number")
                    .build();
        }

        try {
            TipoProductoCaracteristica tpc = tipoProductoCaracteristicaDAO.findById(idTPC);

            if (tpc == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Record with id " + idTPC + " not found")
                        .build();
            }

            if (!tpc.getTipoProducto().getId().equals(idTipoProducto)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Caracteristica does not belong to this tipo_producto")
                        .build();
            }

            return Response.ok(tpc).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Server-Exception", "Cannot access db: " + ex.getMessage())
                    .build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(
            @PathParam("idTipoProducto") Long idTipoProducto,
            TipoProductoCaracteristica entity,
            @Context UriInfo uriInfo) {

        if (idTipoProducto == null || idTipoProducto <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "idTipoProducto must be a positive number")
                    .build();
        }

        if (entity == null || entity.getCaracteristica() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "caracteristica is required")
                    .build();
        }

        if (entity.getId() != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "id must be null for new records")
                    .build();
        }

        try {
            TipoProducto tipoProducto = tipoProductoDAO.findById(idTipoProducto);
            if (tipoProducto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "TipoProducto with id " + idTipoProducto + " not found")
                        .build();
            }

            Integer idCaracteristica = entity.getCaracteristica().getId();
            if (idCaracteristica == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .header("Error", "caracteristica.id cannot be null")
                        .build();
            }

            Caracteristica caracteristica = caracteristicaDAO.findById(idCaracteristica);
            if (caracteristica == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Caracteristica with id " + idCaracteristica + " not found")
                        .build();
            }

            entity.setTipoProducto(tipoProducto);
            entity.setCaracteristica(caracteristica);

            tipoProductoCaracteristicaDAO.crear(entity);

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

    @PUT
    @Path("{idTPC}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("idTipoProducto") Long idTipoProducto,
            @PathParam("idTPC") Long idTPC,
            TipoProductoCaracteristica entity) {

        if (idTipoProducto == null || idTPC == null) {
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
            TipoProductoCaracteristica existente = tipoProductoCaracteristicaDAO.findById(idTPC);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Record with id " + idTPC + " not found")
                        .build();
            }

            // Verificar que pertenece al tipo_producto del path
            if (!existente.getTipoProducto().getId().equals(idTipoProducto)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Caracteristica does not belong to this tipo_producto")
                        .build();
            }

            // Manejar actualización de tipoProducto
            if (entity.getTipoProducto() != null) {
                Long idTipoProductoBody = entity.getTipoProducto().getId();
                if (idTipoProductoBody == null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .header("Error", "tipoProducto.id cannot be null")
                            .build();
                }

                TipoProducto nuevoTipoProducto = tipoProductoDAO.findById(idTipoProductoBody);
                if (nuevoTipoProducto == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .header("Not-Found", "TipoProducto with id " + idTipoProductoBody + " not found")
                            .build();
                }
                existente.setTipoProducto(nuevoTipoProducto);
            }

            // Manejar actualización de caracteristica
            if (entity.getCaracteristica() != null) {
                Integer idCaracteristicaBody = entity.getCaracteristica().getId();
                if (idCaracteristicaBody == null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .header("Error", "caracteristica.id cannot be null")
                            .build();
                }

                Caracteristica nuevaCaracteristica = caracteristicaDAO.findById(idCaracteristicaBody);
                if (nuevaCaracteristica == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .header("Not-Found", "Caracteristica with id " + idCaracteristicaBody + " not found")
                            .build();
                }
                existente.setCaracteristica(nuevaCaracteristica);
            }

            // Actualizar otros campos
            if (entity.getObligatorio() != null) {
                existente.setObligatorio(entity.getObligatorio());
            }
            if (entity.getFechaCreacion() != null) {
                existente.setFechaCreacion(entity.getFechaCreacion());
            }

            tipoProductoCaracteristicaDAO.modificar(existente);

            return Response.ok(existente).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Server-Exception", "Error updating: " + ex.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("{idTPC}")
    public Response delete(
            @PathParam("idTipoProducto") Long idTipoProducto,
            @PathParam("idTPC") Long idTPC) {

        if (idTPC == null || idTPC <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Error", "idTPC must be a positive number")
                    .build();
        }

        try {
            TipoProductoCaracteristica existente = tipoProductoCaracteristicaDAO.findById(idTPC);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Record with id " + idTPC + " not found")
                        .build();
            }

            if (!existente.getTipoProducto().getId().equals(idTipoProducto)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Not-Found", "Caracteristica does not belong to this tipo_producto")
                        .build();
            }

            tipoProductoCaracteristicaDAO.eliminar(existente);

            return Response.noContent().build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Server-Exception", "Cannot delete record: " + ex.getMessage())
                    .build();
        }
    }
}