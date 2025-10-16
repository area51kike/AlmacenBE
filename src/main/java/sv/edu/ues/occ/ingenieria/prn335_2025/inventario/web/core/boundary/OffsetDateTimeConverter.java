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

    // Con offset: 2025-10-14T13:45-06:00
    private static final DateTimeFormatter FMT_WITH_OFFSET =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmXXX");

    // Sin offset: 2025-10-14T13:45 (lo que envía típicamente el datePicker)
    private static final DateTimeFormatter FMT_NO_OFFSET =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    // Zona por defecto (ajústala si usas otra)
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/El_Salvador");

    @Override

    public OffsetDateTime getAsObject(FacesContext ctx, UIComponent comp, String value) {
        if (value == null || value.isBlank()) return null;

        // Normalizar: colapsa espacios después de 'T'
        String v = value.trim().replaceFirst("T\\s+", "T");
        try {
            // Si trae offset al final, úsalo...
            if (v.matches(".*[+-]\\d{2}:\\d{2}$")) {
                return OffsetDateTime.parse(v, FMT_WITH_OFFSET); // yyyy-MM-dd'T'HH:mmXXX
            }
            // ...si no, asume tu zona
            LocalDateTime ldt = LocalDateTime.parse(v, FMT_NO_OFFSET); // yyyy-MM-dd'T'HH:mm
            return ldt.atZone(DEFAULT_ZONE).toOffsetDateTime();
        } catch (DateTimeParseException e) {
            throw new ConverterException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fecha inválida",
                            "Usa 2025-10-14T13:45 (o con offset: 2025-10-14T13:45-06:00)"));
        }
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent comp, OffsetDateTime value) {
        if (value == null) return "";
        // Devuelve SIN offset porque el datePicker normalmente trabaja sin offset
        return value.atZoneSameInstant(DEFAULT_ZONE).toLocalDateTime().format(FMT_NO_OFFSET);
    }
}