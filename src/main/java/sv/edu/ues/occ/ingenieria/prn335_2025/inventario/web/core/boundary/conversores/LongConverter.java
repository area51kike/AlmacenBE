package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
@FacesConverter(value = "longConverter")
public class LongConverter implements Converter<Long> {

    @Override
    public Long getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new ConverterException("No se pudo convertir '" + value + "' a Long: " + e.getMessage(), e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Long value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }
}