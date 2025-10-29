package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.application.FacesMessage;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@FacesConverter(value = "offsetDateTimeConverter", managed = true)
@ApplicationScoped
public class OffsetDateTimeConverter implements Converter<OffsetDateTime> {

    // ✅ CORRECCIÓN 1: Eliminar la 'T' para que coincida con el patrón del p:calendar (yyyy-MM-dd HH:mm)
    private static final DateTimeFormatter FMT_WITH_OFFSET =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mmXXX"); // Usar espacio, no 'T'

    // ✅ CORRECCIÓN 2: Eliminar la 'T' para que coincida con el patrón del p:calendar (yyyy-MM-dd HH:mm)
    private static final DateTimeFormatter FMT_NO_OFFSET =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); // Usar espacio, no 'T'

    // Zona por defecto (está bien, asumiendo que es la correcta)
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/El_Salvador");

    @Override
    public OffsetDateTime getAsObject(FacesContext ctx, UIComponent comp, String value) {
        if (value == null || value.isBlank()) return null;

        // Quitar el manejo de la 'T' si ya no está en el patrón
        String v = value.trim();

        try {
            // Si trae offset al final, úsalo...
            if (v.matches(".*[+-]\\d{2}:\\d{2}$")) {
                return OffsetDateTime.parse(v, FMT_WITH_OFFSET);
            }
            // ...si no, asume tu zona
            LocalDateTime ldt = LocalDateTime.parse(v, FMT_NO_OFFSET);
            return ldt.atZone(DEFAULT_ZONE).toOffsetDateTime();
        } catch (DateTimeParseException e) {
            // Ajustar el mensaje de error para reflejar el patrón correcto
            throw new ConverterException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fecha inválida",
                            "Usa yyyy-MM-dd HH:mm o con offset: yyyy-MM-dd HH:mm-06:00"));
        }
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent comp, OffsetDateTime value) {
        if (value == null) return "";
        // ✅ CRÍTICO: Usar el patrón corregido (FMT_NO_OFFSET)
        return value.atZoneSameInstant(DEFAULT_ZONE).toLocalDateTime().format(FMT_NO_OFFSET);
    }
}