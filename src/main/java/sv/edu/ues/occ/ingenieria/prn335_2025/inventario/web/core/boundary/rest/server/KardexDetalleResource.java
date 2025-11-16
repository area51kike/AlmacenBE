package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.KardexDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.KardexDetalle;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Kardex;

import java.util.UUID;

@Path("kardex_detalle")
public class KardexDetalleResource {

    @Inject
    KardexDetalleDAO kardexDetalleDAO;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findRange(@Min(0) @DefaultValue("0") @QueryParam("first") int first,
                              @Max(100) @DefaultValue("50") @QueryParam("max") int max) {

        if (first >= 0 && max <= 100) {
            try {
                Long total = kardexDetalleDAO.count();
                return Response.ok(kardexDetalleDAO.findRange(first, max))
                        .header("Total-records", total)
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Server-exception", "Cannot access db")
                        .build();
            }
        }

        return Response.status(422)
                .header("Missing.parameter", "first o max out of range")
                .build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@PathParam("id") String id) {
        if (id != null && !id.isEmpty()) {
            try {
                UUID uuid = UUID.fromString(id);
                KardexDetalle resp = kardexDetalleDAO.findById(uuid);
                if (resp != null) {
                    return Response.ok(resp).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Record-not-found", "Record with id " + id + " not found")
                        .build();
            } catch (IllegalArgumentException ex) {
                return Response.status(422)
                        .header("Invalid.parameter", "Invalid UUID format")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Server-exception", "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header("Missing.parameter", "id must not be null or empty")
                .build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") String id) {
        if (id != null && !id.isEmpty()) {
            try {
                UUID uuid = UUID.fromString(id);
                KardexDetalle resp = kardexDetalleDAO.findById(uuid);
                if (resp != null) {
                    kardexDetalleDAO.eliminar(resp);
                    return Response.noContent().build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Record-not-found", "Record with id " + id + " not found")
                        .build();
            } catch (IllegalArgumentException ex) {
                return Response.status(422)
                        .header("Invalid.parameter", "Invalid UUID format")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Server-exception", "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header("Missing.parameter", "id must not be null or empty")
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(KardexDetalle entity, @Context UriInfo uriInfo) {
        if (entity != null && entity.getId() == null) {
            try {
                // Validar que el Kardex padre exista si está asignado
                if (entity.getIdKardex() != null && entity.getIdKardex().getId() != null) {
                    Kardex kardex = kardexDetalleDAO.getEntityManager()
                            .find(Kardex.class, entity.getIdKardex().getId());
                    if (kardex == null) {
                        return Response.status(422)
                                .header("Missing.parameter", "If kardex is assigned, it must exist in the db")
                                .build();
                    }
                    entity.setIdKardex(kardex);
                }

                // Generar UUID si no existe
                entity.setId(UUID.randomUUID());

                kardexDetalleDAO.crear(entity);
                return Response.created(uriInfo.getAbsolutePathBuilder()
                                .path(String.valueOf(entity.getId()))
                                .build())
                        .build();

            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Server-exception", "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header("Missing.parameter", "entity must not be null and id must be null")
                .build();
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") String id, KardexDetalle entity) {
        if (id != null && !id.isEmpty() && entity != null) {
            try {
                UUID uuid = UUID.fromString(id);
                KardexDetalle existing = kardexDetalleDAO.findById(uuid);

                if (existing == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .header("Record-not-found", "Record with id " + id + " not found")
                            .build();
                }

                // Validar que el Kardex padre exista si está asignado
                if (entity.getIdKardex() != null && entity.getIdKardex().getId() != null) {
                    Kardex kardex = kardexDetalleDAO.getEntityManager()
                            .find(Kardex.class, entity.getIdKardex().getId());
                    if (kardex == null) {
                        return Response.status(422)
                                .header("Missing.parameter", "If kardex is assigned, it must exist in the db")
                                .build();
                    }
                    entity.setIdKardex(kardex);
                }

                entity.setId(uuid);
                kardexDetalleDAO.modificar(entity);
                return Response.ok(entity).build();

            } catch (IllegalArgumentException ex) {
                return Response.status(422)
                        .header("Invalid.parameter", "Invalid UUID format")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Server-exception", "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header("Missing.parameter", "id and entity must not be null")
                .build();
    }
}