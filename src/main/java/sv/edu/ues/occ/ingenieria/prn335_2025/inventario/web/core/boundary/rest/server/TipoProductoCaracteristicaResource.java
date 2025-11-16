package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProductoCaracteristica;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProducto;

@Path("tipo_producto_caracteristica")
public class TipoProductoCaracteristicaResource {

    @Inject
    TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDAO;

    @Inject
    CaracteristicaDAO caracteristicaDAO;

    @Inject
    TipoProductoDao tipoProductoDao;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findRange(@Min(0) @DefaultValue("0") @QueryParam("first") int first,
                              @Max(100) @DefaultValue("50") @QueryParam("max") int max) {
        if (first >= 0 && max <= 100) {
            try {
                Long total = tipoProductoCaracteristicaDAO.count();
                return Response.ok(tipoProductoCaracteristicaDAO.findRange(first, max))
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
    public Response findById(@PathParam("id") Long id) {
        if (id != null && id > 0) {
            try {
                TipoProductoCaracteristica resp = tipoProductoCaracteristicaDAO.findById(id);
                if (resp != null) {
                    return Response.ok(resp).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Record-not-found", "Record with id " + id + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Server-exception", "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header("Missing-parameter", "id must be greater than 0")
                .build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long id) {
        if (id != null && id > 0) {
            try {
                TipoProductoCaracteristica resp = tipoProductoCaracteristicaDAO.findById(id);
                if (resp != null) {
                    tipoProductoCaracteristicaDAO.eliminar(resp);
                    return Response.noContent().build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Record-not-found", "Record with id " + id + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Server-exception", "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header("Missing-parameter", "id must be greater than 0")
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(TipoProductoCaracteristica entity, @Context UriInfo uriInfo) {
        if (entity != null && entity.getId() == null) {
            try {
                // Validar que Caracteristica sea obligatoria
                if (entity.getCaracteristica() == null || entity.getCaracteristica().getId() == null) {
                    return Response.status(422)
                            .header("Missing-parameter", "Caracteristica is required")
                            .build();
                }

                // Validar que TipoProducto sea obligatorio
                if (entity.getTipoProducto() == null || entity.getTipoProducto().getId() == null) {
                    return Response.status(422)
                            .header("Missing-parameter", "TipoProducto is required")
                            .build();
                }

                // Validar que Caracteristica exista
                Caracteristica caracteristica = caracteristicaDAO.findById(entity.getCaracteristica().getId());
                if (caracteristica == null) {
                    return Response.status(422)
                            .header("Missing-parameter",
                                    "Caracteristica with id " + entity.getCaracteristica().getId() +
                                            " does not exist in database")
                            .build();
                }

                // Validar que TipoProducto exista
                TipoProducto tipoProducto = tipoProductoDao.findById(entity.getTipoProducto().getId());
                if (tipoProducto == null) {
                    return Response.status(422)
                            .header("Missing-parameter",
                                    "TipoProducto with id " + entity.getTipoProducto().getId() +
                                            " does not exist in database")
                            .build();
                }

                // Asignar entidades completas
                entity.setCaracteristica(caracteristica);
                entity.setTipoProducto(tipoProducto);

                // Generar ID usando el método del DAO
                Long nuevoId = tipoProductoCaracteristicaDAO.obtenerMaximoId() + 1;
                entity.setId(nuevoId);

                tipoProductoCaracteristicaDAO.crear(entity);

                return Response.created(
                        uriInfo.getAbsolutePathBuilder()
                                .path(String.valueOf(entity.getId()))
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
    public Response update(@PathParam("id") Long id, TipoProductoCaracteristica entity) {
        if (id != null && id > 0 && entity != null) {
            try {
                TipoProductoCaracteristica existing = tipoProductoCaracteristicaDAO.findById(id);
                if (existing != null) {
                    // Validar Caracteristica si se proporciona
                    if (entity.getCaracteristica() != null && entity.getCaracteristica().getId() != null) {
                        Caracteristica caracteristica = caracteristicaDAO.findById(entity.getCaracteristica().getId());
                        if (caracteristica == null) {
                            return Response.status(422)
                                    .header("Missing-parameter",
                                            "Caracteristica with id " + entity.getCaracteristica().getId() +
                                                    " does not exist in database")
                                    .build();
                        }
                        entity.setCaracteristica(caracteristica);
                    }

                    // Validar TipoProducto si se proporciona
                    if (entity.getTipoProducto() != null && entity.getTipoProducto().getId() != null) {
                        TipoProducto tipoProducto = tipoProductoDao.findById(entity.getTipoProducto().getId());
                        if (tipoProducto == null) {
                            return Response.status(422)
                                    .header("Missing-parameter",
                                            "TipoProducto with id " + entity.getTipoProducto().getId() +
                                                    " does not exist in database")
                                    .build();
                        }
                        entity.setTipoProducto(tipoProducto);
                    }

                    entity.setId(id);
                    tipoProductoCaracteristicaDAO.modificar(entity);
                    return Response.ok(entity).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Record-not-found", "Record with id " + id + " not found")
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