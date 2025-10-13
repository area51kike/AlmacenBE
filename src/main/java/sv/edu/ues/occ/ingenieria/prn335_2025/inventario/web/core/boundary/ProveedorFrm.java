package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProveedorDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Proveedor;

import java.io.Serializable;
@Named
@ViewScoped
public class ProveedorFrm extends DefaultFrm<Proveedor> implements Serializable {

    @Inject
    FacesContext facesContext;

    @Inject
    ProveedorDAO proveedorDao;

    public ProveedorFrm() {
        this.nombreBean = "Proveedores";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected InventarioDefaultDataAccess<Proveedor> getDao() {
        return proveedorDao;
    }

    @Override
    protected Proveedor nuevoRegistro() {
        Proveedor p = new Proveedor();
        return p;
    }

    @Override
    protected Proveedor buscarRegistroPorId(Object id) {
        return null;
    }

    @Override
    protected String getIdAsText(Proveedor r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected Proveedor getIdByText(String id) {
        if (id != null && !id.isBlank()) {
            try {
                Integer buscado = Integer.valueOf(id);
                for (Proveedor p : (Iterable<Proveedor>) proveedorDao.findAll()) {
                    if (p.getId().equals(buscado)) {
                        return p;
                    }
                }
            } catch (NumberFormatException nfe) {
                // No hacer nada
            }
        }
        return null;
    }
}