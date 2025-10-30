package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoAlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;

@FacesConverter(value = "tipoAlmacenConverter", managed = true)
public class TipoAlmacenConverter implements Converter<TipoAlmacen> {

    @EJB
    private TipoAlmacenDAO tipoAlmacenDAO;

    @Override
    public TipoAlmacen getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty() || value.equals("null")) {
            return null;
        }
        try {
            Integer id = Integer.parseInt(value);
            return tipoAlmacenDAO.findById(id);
        } catch (Exception e) {
            System.err.println("Error en TipoAlmacenConverter.getAsObject: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, TipoAlmacen value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return value.getId().toString();
    }
}