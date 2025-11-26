package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Venta;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.VentaDetalle;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("despachoFrm")
@ViewScoped
public class DespachoFrm extends DefaultFrm<Venta> implements Serializable {

    @Inject
    FacesContext facesContext;

    @Inject
    VentaDAO ventaDAO;

    @Inject
    VentaDetalleDAO ventaDetalleDAO;

    @Inject
    DespachoKardexFrm despachoKardexFrm;

    @Inject
    VentaDetalleFrm ventaDetalleFrm;

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected InventarioDefaultDataAccess<Venta> getDao() {
        return ventaDAO;
    }

    @Override
    protected Venta nuevoRegistro() {
        Venta venta = new Venta();
        venta.setId(UUID.randomUUID());
        venta.setFecha(OffsetDateTime.now());
        venta.setEstado("PAGADA");
        return venta;
    }

    @Override
    protected Venta buscarRegistroPorId(Object id) {
        if (id == null) return null;

        UUID buscado = null;
        try {
            if (id instanceof UUID) {
                buscado = (UUID) id;
            } else {
                buscado = UUID.fromString(id.toString());
            }
        } catch (IllegalArgumentException e) {
            System.err.println("ID no es un UUID válido: " + id);
            return null;
        }

        // Buscar en el modelo actual primero
        if (this.modelo != null && this.modelo.getWrappedData() != null) {
            for (Venta v : this.modelo.getWrappedData()) {
                if (v.getId() != null && v.getId().equals(buscado)) {
                    return v;
                }
            }
        }

        // Si no está en el modelo, buscar en la base de datos
        try {
            return ventaDAO.find(buscado);
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Error al buscar venta por ID", e);
            return null;
        }
    }


    @Override
    protected String getIdAsText(Venta r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected Venta getIdByText(String id) {
        if (id == null) return null;

        try {
            UUID buscado = UUID.fromString(id);

            // Buscar primero en el modelo actual
            if (this.modelo != null && this.modelo.getWrappedData() != null) {
                return this.modelo.getWrappedData().stream()
                        .filter(r -> r.getId() != null && r.getId().equals(buscado))
                        .findFirst()
                        .orElse(buscarRegistroPorId(buscado));
            }

            return buscarRegistroPorId(buscado);

        } catch (IllegalArgumentException e) {
            System.err.println("ID no es un UUID válido: " + id);
            return null;
        }
    }


    public List<Venta> cargarDatos(int first, int max) {
        try {
            // Solo compras PAGADAS
            return ventaDAO.findByEstado("PENDIENTE");
        } catch (Exception e) {
            Logger.getLogger(RecepcionFrm.class.getName()).log(Level.SEVERE, null, e);
        }
        return List.of();
    }

    /**
     * Obtiene los detalles de la venta seleccionada
     */
    public List<VentaDetalle> getDetallesCompraSeleccionada() {
        if (this.registro != null && this.registro.getId() != null) {
            return ventaDetalleDAO.findByIdVenta(this.registro.getId());
        }
        return List.of();
    }

    public DespachoKardexFrm getDespachoKardexFrm() {
        return despachoKardexFrm;
    }

    public VentaDetalleFrm getVentaDetalleFrm() {
        if (this.registro != null && this.registro.getId() != null) {
            ventaDetalleFrm.setIdVenta(this.registro.getId());
        }
        return ventaDetalleFrm;
    }
    public void selectionHandler(org.primefaces.event.SelectEvent<Venta> event) {
        Venta ventaSeleccionada = event.getObject();

        if (ventaSeleccionada != null) {
            this.registro = ventaSeleccionada;
            this.estado = ESTADO_CRUD.MODIFICAR;

            // 👉 Aquí cargas SOLO los detalles de esa venta
            if (ventaSeleccionada.getId() != null) {
                ventaDetalleFrm.cargarDetallesPorVenta(ventaSeleccionada.getId());
            }

            if (despachoKardexFrm != null) {
                despachoKardexFrm.limpiar();
            }
        }
    }


    @Override
    protected boolean esNombreVacio(Venta registro) {
        return false;
    }
}
