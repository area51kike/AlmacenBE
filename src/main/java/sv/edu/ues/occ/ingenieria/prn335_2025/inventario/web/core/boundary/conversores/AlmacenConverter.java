package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.AlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Almacen;

@FacesConverter(value = "almacenConverter", managed = true)
public class AlmacenConverter implements Converter<Almacen> {

    @Inject
    private AlmacenDAO almacenDAO;

    @Override
    public Almacen getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty() || value.equals("null")) {
            return null;
        }
        try {
            Integer id = Integer.parseInt(value);
            return almacenDAO.findById(id);
        } catch (Exception e) {
            System.err.println("Error en AlmacenConverter.getAsObject: " + e.getMessage());
            e.printStackTrace();
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