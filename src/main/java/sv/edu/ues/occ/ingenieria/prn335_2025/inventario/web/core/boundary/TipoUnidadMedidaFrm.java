package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoUnidadMedidaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoUnidadMedida;

import java.io.Serializable;

@Named
@ViewScoped
public class TipoUnidadMedidaFrm extends DefaultFrm<TipoUnidadMedida> implements Serializable {

    @Inject
    FacesContext facesContext;

    @Inject
    TipoUnidadMedidaDAO tipoUnidadMedidaDAO;

    public TipoUnidadMedidaFrm() {
        this.nombreBean = "Unidad de Medida";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected InventarioDefaultDataAccess<TipoUnidadMedida> getDao() {
        return tipoUnidadMedidaDAO;
    }

    @Override
    protected TipoUnidadMedida nuevoRegistro() {
        TipoUnidadMedida tipoUnidadMedida = new TipoUnidadMedida();
        return tipoUnidadMedida;
    }

    @Override
    protected TipoUnidadMedida buscarRegistroPorId(Object id) {
        if (id != null && id instanceof Integer buscado && this.modelo.getWrappedData().isEmpty()) {
            for (TipoUnidadMedida tum : (Iterable<TipoUnidadMedida>) tipoUnidadMedidaDAO.findAll()) {
                if (tum.getId().equals(buscado)) {
                    return tum;
                }
            }
        }
        return null;
    }

    @Override
    protected String getIdAsText(TipoUnidadMedida r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected TipoUnidadMedida getIdByText(String id) {
        if (id != null && this.modelo != null && !this.modelo.getWrappedData().isEmpty()) {
            try {
                Integer buscado = Integer.parseInt(id);
                return this.modelo.getWrappedData().stream()
                        .filter(r -> r.getId() != null && r.getId().equals(buscado))
                        .findFirst()
                        .orElse(null);
            } catch (NumberFormatException e) {
                System.err.println("ID no es un número válido: " + id);
                return null;
            }
        }
        return null;
    }
}