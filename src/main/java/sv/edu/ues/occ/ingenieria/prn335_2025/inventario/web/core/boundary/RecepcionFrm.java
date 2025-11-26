package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.CompraDetalle;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("recepcionFrm")
@ViewScoped
public class RecepcionFrm extends DefaultFrm<Compra> implements Serializable {

    @Inject
    FacesContext facesContext;

    @Inject
    CompraDAO compraDAO;

    @Inject
    CompraDetalleDAO compraDetalleDAO;

    @Inject
    RecepcionKardexFrm recepcionKardexFrm;

    @Inject
    CompraDetalleFrm compraDetalleFrm;

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected InventarioDefaultDataAccess<Compra> getDao() {
        return compraDAO;
    }

    @Override
    protected Compra nuevoRegistro() {
        Compra compra = new Compra();
        compra.setFecha(OffsetDateTime.now());
        compra.setEstado("PAGADA");
        return compra;
    }

    @Override
    protected Compra buscarRegistroPorId(Object id) {
        if (id == null) return null;
        try {
            return compraDAO.findById(Long.valueOf(id.toString()));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String getIdAsText(Compra r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected Compra getIdByText(String id) {
        if (id == null) return null;
        try {
            return compraDAO.findById(Long.parseLong(id));
        } catch (Exception e) {
            return null;
        }
    }

    public List<Compra> cargarDatos(int first, int max) {
        try {
            // Solo compras PAGADAS
            return compraDAO.findByEstado("PAGADA");
        } catch (Exception e) {
            Logger.getLogger(RecepcionFrm.class.getName()).log(Level.SEVERE, null, e);
        }
        return List.of();
    }

    /**
     * Obtiene los detalles de la compra seleccionada
     */
    public List<CompraDetalle> getDetallesCompraSeleccionada() {
        if (this.registro != null && this.registro.getId() != null) {
            return compraDetalleDAO.findByIdCompra(this.registro.getId());
        }
        return List.of();
    }

    public RecepcionKardexFrm getRecepcionKardexFrm() {
        return recepcionKardexFrm;
    }

    public CompraDetalleFrm getCompraDetalleFrm() {
        if (this.registro != null && this.registro.getId() != null) {
            compraDetalleFrm.setIdCompra(this.registro.getId());
        }
        return compraDetalleFrm;
    }
}