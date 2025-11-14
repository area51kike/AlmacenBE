package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

import java.util.UUID;

/**
 * Converter JSF para convertir entre String y UUID
 */
@FacesConverter(value = "uuidConverter")
public class UUIDConverter implements Converter<UUID> {

    /**
     * Convierte de String a UUID
     */
    @Override
    public UUID getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Error al convertir String a UUID: " + value);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convierte de UUID a String
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, UUID value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }
}