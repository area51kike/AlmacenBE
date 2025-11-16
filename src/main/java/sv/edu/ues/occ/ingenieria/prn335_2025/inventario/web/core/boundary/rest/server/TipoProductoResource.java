package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProducto;

import java.util.List;

@Path("tipo_producto")
public class TipoProductoResource {

    @Inject
    TipoProductoDao tipoProductoDao;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findRange(@Min(0) @DefaultValue("0") @QueryParam("first") int first,
                                     @Max(100) @DefaultValue("50") @QueryParam("max") int max) {

        if (first >= 0 && max <= 100) {
            try {
                Long total = tipoProductoDao.count();
                return Response.ok(tipoProductoDao.findRange(first, max)).header("Total-records",total).build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Server-exception", "Cannot access db").build();

            }
        }

        return Response.status(422).header("Missing.parameter", "first o max out of range").build();
    }
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@PathParam("id") Long id) {
        if(id != null && id > 0) {
            try {
                TipoProducto resp= tipoProductoDao.findById(id);
                if(resp != null) {
                    return Response.ok(resp).build();
                }
                  return Response.status(Response.Status.NOT_FOUND).header("Record-not-found", "Record with id "+id+" not found").build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Server-exception", "Cannot access db").build();
            }
        }
        return Response.status(422).header("Missing.parameter", "first o max out of range").build();    }
    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long id){
        if(id != null && id > 0) {
            try {
                TipoProducto resp= tipoProductoDao.findById(id);
                if(resp != null) {
                    tipoProductoDao.eliminar(resp);
                    return Response.noContent().build();
                }
                return Response.status(Response.Status.NOT_FOUND).header("Record-not-found", "Record with id "+id+" not found").build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Server-exception", "Cannot access db").build();
            }
        }
        return Response.status(422).header("Missing.parameter", "first o max out of range").build();

    }
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(TipoProducto entity, @Context UriInfo uriInfo){
        if(entity!= null && entity.getId() == null){
            try {
                if(entity.getIdTipoProductoPadre()!=null && entity.getIdTipoProductoPadre().getId()!=null){
                    TipoProducto padre= tipoProductoDao.findById(entity.getIdTipoProductoPadre().getId());
                    if(padre!=null){
                        return Response.status(422).header("Missing.parameter", "If parent is assigned, must be null and exist in the db").build();

                    }
                   entity.setIdTipoProductoPadre(padre);
                }
                tipoProductoDao.crear(entity);
                return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(entity.getId())).build()).build();

            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Server-exception", "Cannot access db").build();
            }
        }
        return Response.status(422).header("Missing.parameter", "entity must  not be null and entity").build();
    }
}