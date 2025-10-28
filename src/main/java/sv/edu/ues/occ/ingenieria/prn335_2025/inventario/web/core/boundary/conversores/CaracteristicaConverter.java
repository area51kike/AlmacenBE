package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Caracteristica;

@FacesConverter(value = "caracteristicaConverter", managed = true)
public class CaracteristicaConverter implements Converter<Caracteristica> {

    @Inject
    private CaracteristicaDAO caracteristicaDAO;

    @Override
    public Caracteristica getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty() || value.equals("null")) {
            return null;
        }
        try {
            Long id = Long.parseLong(value);
            return caracteristicaDAO.findById(id);
        } catch (Exception e) {
            System.err.println("Error en CaracteristicaConverter.getAsObject: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Caracteristica value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return value.getId().toString();
    }
}