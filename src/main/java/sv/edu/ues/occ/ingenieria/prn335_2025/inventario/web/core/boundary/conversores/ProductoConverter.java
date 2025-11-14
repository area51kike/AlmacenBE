package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;

import java.util.UUID;

@FacesConverter(value = "productoConverter", managed = true)
public class ProductoConverter implements Converter<Producto> {

    @Inject
    ProductoDAO productoDao;

    @Override
    public Producto getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            UUID id = UUID.fromString(value);
            return productoDao.findById(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Producto value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return value.getId().toString();
    }
}