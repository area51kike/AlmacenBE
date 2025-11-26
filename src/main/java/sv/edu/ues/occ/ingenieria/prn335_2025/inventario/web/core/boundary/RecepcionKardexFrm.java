package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.enterprise.context.Dependent;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.*;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("recepcionKardexFrm")
@Dependent
public class RecepcionKardexFrm extends DefaultFrm<Kardex> implements Serializable {

    @Inject
    FacesContext facesContext;

    @Inject
    KardexDAO kardexDAO;

    @Inject
    AlmacenDAO almacenDAO;

    @Inject
    CompraDetalleDAO compraDetalleDAO;

    @Inject
    KardexDetalleDAO kardexDetalleDAO;

    @Inject
    CompraDAO compraDAO;

    private KardexDetalle detalleKardex = new KardexDetalle();

    // CompraDetalle que se está procesando
    private CompraDetalle detalleActual;

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected InventarioDefaultDataAccess<Kardex> getDao() {
        return kardexDAO;
    }


    @Override
    protected Kardex nuevoRegistro() {
        Kardex kardex = new Kardex();
        kardex.setId(UUID.randomUUID());
        kardex.setTipoMovimiento("INGRESO");
        kardex.setFecha(OffsetDateTime.now());
        kardex.setCantidad(BigDecimal.ZERO);
        kardex.setPrecio(BigDecimal.ZERO);
        kardex.setCantidadActual(BigDecimal.ZERO);
        kardex.setPrecioActual(BigDecimal.ZERO);
        return kardex;
    }

    @Override
    protected Kardex buscarRegistroPorId(Object id) {
        if (id == null) return null;

        UUID buscado;
        try {
            if (id instanceof UUID) {
                buscado = (UUID) id;
            } else {
                buscado = UUID.fromString(id.toString());
            }
        } catch (IllegalArgumentException e) {
            return null;
        }

        return kardexDAO.findAll().stream()
                .filter(k -> k.getId() != null && k.getId().equals(buscado))
                .findFirst()
                .orElse(null);
    }

    @Override
    protected String getIdAsText(Kardex r) {
        return (r != null && r.getId() != null) ? r.getId().toString() : null;
    }

    @Override
    protected Kardex getIdByText(String id) {
        if (id == null) return null;
        try {
            return buscarRegistroPorId(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Obtiene lista de almacenes activos
     */
    public List<Almacen> getListaAlmacenes() {
        return almacenDAO.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo()))
                .toList();
    }

    /**
     * Prepara el diálogo para crear un Kardex desde un CompraDetalle
     */
    public void prepararKardex(CompraDetalle detalle) {
        this.detalleActual = detalle;
        this.registro = nuevoRegistro();

        // Pre-llenar datos desde el detalle de compra
        this.registro.setIdProducto(detalle.getIdProducto());
        this.registro.setFecha(OffsetDateTime.now());
        this.registro.setIdCompraDetalle(detalle);
        this.registro.setCantidad(detalle.getCantidad());
        this.registro.setPrecio(detalle.getPrecio());
        this.registro.setIdVentaDetalle(null); // Es ingreso, no despacho
        // Estos los llenará el usuario
        this.registro.setReferenciaExterna(null);
        this.registro.setIdAlmacen(null);
        this.registro.setObservaciones(null);

        // Inicializamos el KardexDetalle
        detalleKardex = new KardexDetalle();
        detalleKardex.setId(UUID.randomUUID());
        detalleKardex.setActivo(true);
        detalleKardex.setIdKardex(registro);
    }

    public void btnRecibirHandler(ActionEvent actionEvent) {
        try {
            // Validar almacén
            if (this.registro.getIdAlmacen() == null) {
                facesContext.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "Debe seleccionar un almacén"));
                return;
            }

            // 1. Guardar el movimiento en Kardex
            kardexDAO.crear(this.registro);

            // Guardar detalle de kardex
            detalleKardex.setIdKardex(this.registro);
            kardexDetalleDAO.crear(detalleKardex);

            // 2. Obtener el detalle de compra y actualizar su estado
            CompraDetalle detalle = this.registro.getIdCompraDetalle();
            detalle.setEstado("RECIBIDO");
            compraDetalleDAO.modificar(detalle);

            // 3. Verificar si todos los detalles están vendidos
            Long idCompra = detalle.getIdCompra().getId();
            verificarYActualizarEstadoCompra(idCompra);

            // 4. Limpiar formulario
            this.registro = new Kardex();
            detalleKardex = new KardexDetalle();

            // 5. Mensaje de éxito
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Producto recibido correctamente"));
            btnCancelarHandler(actionEvent);
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Error al recibir producto", e);
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo recibir el producto: " + e.getMessage()));
        }
    }

    private void verificarYActualizarEstadoCompra(Long idCompra) {
        try {
            // Verificar si todos los detalles están despachados
            if (compraDetalleDAO.todosDetallesRecibidos(idCompra)) {
                // Actualizar estado de la compra
                compraDAO.actualizarEstado(idCompra, "RECIBIDA");

                facesContext.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Información",
                                "Todos los productos han sido recibidos. Compra completada."));
            }
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Error al verificar estado de compra", e);
        }
    }

    public CompraDetalle getDetalleActual() {
        return detalleActual;
    }

    public void setDetalleActual(CompraDetalle detalleActual) {
        this.detalleActual = detalleActual;
    }

    public KardexDetalle getDetalleKardex() {
        return detalleKardex;
    }
}