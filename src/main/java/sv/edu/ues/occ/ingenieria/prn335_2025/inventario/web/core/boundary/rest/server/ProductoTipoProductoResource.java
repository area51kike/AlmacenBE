package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("producto/{idProducto}/tipo_producto")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductoTipoProductoResource {

    @Inject
    ProductoTipoProductoDAO productoTipoProductoDAO;

    @Inject
    TipoProductoDAO tipoProductoDAO;

    @Inject
    ProductoDAO productoDAO;

    @GET
    public Response getTiposDeProducto(@PathParam("idProducto") UUID idProducto) {
        if (idProducto == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El ID del producto no puede ser nulo")
                    .build();
        }

        try {
            List<ProductoTipoProducto> relaciones =
                    productoTipoProductoDAO.findByid(idProducto);

            if (relaciones == null || relaciones.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No se encontraron tipos de producto para el producto con id: " + idProducto)
                        .build();
            }

            List<TipoProducto> tipos = relaciones.stream()
                    .map(ProductoTipoProducto::getIdTipoProducto)
                    .collect(Collectors.toList());

            return Response.ok(tipos).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener tipos de producto: " + ex.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{idPTP}")
    public Response findById(@PathParam("idPTP") UUID idPTP,
                             @PathParam("idProducto") UUID idProducto) {
        if (idPTP == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El ID no puede ser nulo")
                    .build();
        }

        try {
            ProductoTipoProducto resp = productoTipoProductoDAO.findById(idPTP);

            if (resp == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Registro con id " + idPTP + " no encontrado")
                        .build();
            }

            // Validar que la relación pertenezca al producto especificado
            if (!resp.getIdProducto().getId().equals(idProducto)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("La relación no pertenece al producto especificado")
                        .build();
            }

            return Response.ok(resp).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al buscar el registro: " + ex.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{idPTP}")
    public Response delete(@PathParam("idPTP") UUID idPTP,
                           @PathParam("idProducto") UUID idProducto) {
        if (idPTP == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El ID no puede ser nulo")
                    .build();
        }

        try {
            ProductoTipoProducto existente = productoTipoProductoDAO.findById(idPTP);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Registro con id " + idPTP + " no encontrado")
                        .build();
            }

            // Validar que la relación pertenezca al producto especificado
            if (!existente.getIdProducto().getId().equals(idProducto)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("La relación no pertenece al producto especificado")
                        .build();
            }

            productoTipoProductoDAO.eliminar(existente);
            return Response.noContent().build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al eliminar el registro: " + ex.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/{idTipoProducto}")
    public Response create(
            ProductoTipoProducto entity,
            @PathParam("idProducto") UUID idProducto,
            @PathParam("idTipoProducto") Long idTipoProducto,
            @Context UriInfo uriInfo) {

        if (idProducto == null || idTipoProducto == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Los IDs de producto y tipo de producto no pueden ser nulos")
                    .build();
        }

        try {
            Producto existeProducto = productoDAO.findById(idProducto);
            if (existeProducto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No existe el producto con el id: " + idProducto)
                        .build();
            }

            TipoProducto existeTP = tipoProductoDAO.findById(idTipoProducto);
            if (existeTP == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No existe el tipo de producto con el id: " + idTipoProducto)
                        .build();
            }

            // Crear nueva entidad en lugar de usar la del body
            ProductoTipoProducto nuevaRelacion = new ProductoTipoProducto();
            nuevaRelacion.setId(UUID.randomUUID());
            nuevaRelacion.setIdProducto(existeProducto);
            nuevaRelacion.setIdTipoProducto(existeTP);

            productoTipoProductoDAO.crear(nuevaRelacion);

            return Response.created(
                    uriInfo.getAbsolutePathBuilder()
                            .path(nuevaRelacion.getId().toString())
                            .build()
            ).entity(nuevaRelacion).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al crear: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{idPTP}")
    public Response update(@PathParam("idPTP") UUID idPTP,
                           @PathParam("idProducto") UUID idProducto,
                           ProductoTipoProducto entity) {
        if (idPTP == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El ID no puede ser nulo")
                    .build();
        }

        if (entity == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("La entidad no puede ser nula")
                    .build();
        }

        try {
            ProductoTipoProducto existente = productoTipoProductoDAO.findById(idPTP);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Registro con id " + idPTP + " no encontrado")
                        .build();
            }

            // Validar que la relación pertenezca al producto especificado en la URL
            if (!existente.getIdProducto().getId().equals(idProducto)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("La relación no pertenece al producto especificado")
                        .build();
            }

            // Actualizar el producto si viene en el body
            if (entity.getIdProducto() != null && entity.getIdProducto().getId() != null) {
                UUID idProductoBody = entity.getIdProducto().getId();

                Producto existeProducto = productoDAO.findById(idProductoBody);
                if (existeProducto == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("No existe el producto con el id: " + idProductoBody)
                            .build();
                }
                existente.setIdProducto(existeProducto);
            }

            // Actualizar el tipo de producto si viene en el body
            if (entity.getIdTipoProducto() != null && entity.getIdTipoProducto().getId() != null) {
                Long idTipoProductoBody = entity.getIdTipoProducto().getId();

                TipoProducto existeTP = tipoProductoDAO.findById(idTipoProductoBody);
                if (existeTP == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("No existe el tipo de producto con el id: " + idTipoProductoBody)
                            .build();
                }
                existente.setIdTipoProducto(existeTP);
            }

            productoTipoProductoDAO.modificar(existente);

            return Response.ok(existente).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al actualizar: " + e.getMessage())
                    .build();
        }
    }
}