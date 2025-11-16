package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDetalleDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.CompraDetalle;

import java.util.UUID;

@RequestScoped
@FacesConverter(value = "compraDetalleConverter", managed = true)
public class CompraDetalleConverter implements Converter<CompraDetalle> {

    @Inject
    CompraDetalleDao compraDetalleDao;

    @Override
    public CompraDetalle getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            UUID id = UUID.fromString(value);
            return compraDetalleDao.findById(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, CompraDetalle value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return value.getId().toString();
    }
}