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

@Named("despachoKardexFrm")
@Dependent
public class DespachoKardexFrm extends DefaultFrm<Kardex> implements Serializable {

    @Inject
    FacesContext facesContext;

    @Inject
    KardexDAO kardexDAO;

    @Inject
    AlmacenDAO almacenDAO;

    @Inject
    VentaDetalleDAO ventaDetalleDAO;

    @Inject
    KardexDetalleDAO kardexDetalleDAO;

    @Inject
    VentaDAO ventaDAO;

    private KardexDetalle detalleKardex = new KardexDetalle();

    // VentaDetalle que se está procesando
    private VentaDetalle detalleActual;

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
        kardex.setTipoMovimiento("SALIDA");
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
    public void prepararKardex(VentaDetalle detalle) {
        this.detalleActual = detalle;
        this.registro = nuevoRegistro();

        // Pre-llenar datos desde el detalle de compra
        this.registro.setIdProducto(detalle.getIdProducto());
        this.registro.setFecha(OffsetDateTime.now());
        this.registro.setIdVentaDetalle(detalle);
        this.registro.setCantidad(detalle.getCantidad());
        this.registro.setPrecio(detalle.getPrecio());
        this.registro.setIdCompraDetalle(null); // Es venta, no ingreso
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

            // 2. Obtener el detalle de venta y actualizar su estado
            VentaDetalle detalle = this.registro.getIdVentaDetalle();
            detalle.setEstado("DESPACHADO");
            ventaDetalleDAO.modificar(detalle);

            // 3. Verificar si todos los detalles están vendidos
            UUID idVenta = detalle.getIdVenta().getId();
            verificarYActualizarEstadoVenta(idVenta);

            // 4. Limpiar formulario
            this.registro = new Kardex();
            detalleKardex = new KardexDetalle();

            // 5. Mensaje de éxito
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Producto despachado correctamente"));
            btnCancelarHandler(actionEvent);
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Error al despachar producto", e);
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo despachar el producto: " + e.getMessage()));
        }
    }

    private void verificarYActualizarEstadoVenta(UUID idVenta) {
        try {
            // Verificar si todos los detalles están despachados
            if (ventaDetalleDAO.todosDetallesDespachados(idVenta)) {
                // Actualizar estado de la compra
                ventaDAO.actualizarEstado(idVenta, "DESPACHADA");

                facesContext.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Información",
                                "Todos los productos han sido despachados. Venta completada."));
            }
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Error al verificar estado de venta", e);
        }
    }

    public VentaDetalle getDetalleActual() {
        return detalleActual;
    }

    public void setDetalleActual(VentaDetalle detalleActual) {
        this.detalleActual = detalleActual;
    }

    public KardexDetalle getDetalleKardex() {
        return detalleKardex;
    }
}