package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProducto;

@FacesConverter(value = "tipoProductoConverter", managed = true)
public class TipoProductoConverter implements Converter<TipoProducto> {

    @EJB
    private TipoProductoDao tipoProductoDAO;

    @Override
    public TipoProducto getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty() || value.equals("null")) {
            return null;
        }
        try {
            Long id = Long.parseLong(value);
            return tipoProductoDAO.findById(id);
        } catch (Exception e) {
            System.err.println("Error en TipoProductoConverter.getAsObject: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, TipoProducto value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return value.getId().toString();
    }
}