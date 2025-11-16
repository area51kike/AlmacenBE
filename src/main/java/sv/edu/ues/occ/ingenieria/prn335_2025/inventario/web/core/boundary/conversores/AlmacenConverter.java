package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.AlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Almacen;

@RequestScoped
@FacesConverter(value = "almacenConverter", managed = true)
public class AlmacenConverter implements Converter<Almacen> {

    @Inject
    AlmacenDAO almacenDao;

    @Override
    public Almacen getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            Integer id = Integer.valueOf(value);
            return almacenDao.findById(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Almacen value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return value.getId().toString();
    }
}