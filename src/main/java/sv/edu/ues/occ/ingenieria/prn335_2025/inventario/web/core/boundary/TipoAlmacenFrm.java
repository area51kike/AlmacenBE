package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoAlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class TipoAlmacenFrm extends DefaultFrm<TipoAlmacen> implements Serializable {

    @Inject
    FacesContext facesContext;

    @Inject
    TipoAlmacenDAO tipoAlmacenDAO;


    public TipoAlmacenFrm() {
        this.nombreBean = " Tipo Almacen";
    }


    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected InventarioDefaultDataAccess<TipoAlmacen> getDao() {
        return tipoAlmacenDAO;
    }

    @Override
    protected TipoAlmacen nuevoRegistro() {
        TipoAlmacen tipoAlmacen = new TipoAlmacen();
        return tipoAlmacen;
    }

    @Override
    protected TipoAlmacen buscarRegistroPorId(Object id) {
        if (id != null && id instanceof Integer buscado && this.modelo.getWrappedData().isEmpty()) {
            for (TipoAlmacen ta : (Iterable<TipoAlmacen>) tipoAlmacenDAO.findAll()) {
                if (ta.getId().equals(buscado)) {
                    return ta;
                }
            }
        }
        return null;
    }

    @Override
    protected String getIdAsText(TipoAlmacen r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected TipoAlmacen getIdByText(String id) {
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