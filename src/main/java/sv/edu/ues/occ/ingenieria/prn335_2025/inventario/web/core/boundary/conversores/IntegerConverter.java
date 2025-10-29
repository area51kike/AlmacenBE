package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.enterprise.context.Dependent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.convert.ConverterException;

// Un converter simple para asegurar que los IDs Integer se manejen correctamente
@FacesConverter(value = "integerIdConverter")
@Dependent
public class IntegerConverter implements Converter<Integer> {

    @Override
    public Integer getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            // Manejar errores de conversión
            throw new ConverterException("Error de conversión: El valor '" + value + "' no es un número entero válido.", e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Integer value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }
}