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

    // Formatos de fecha con y sin offset
    private static final DateTimeFormatter FMT_WITH_OFFSET = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mmXXX");
    private static final DateTimeFormatter FMT_NO_OFFSET = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Zona horaria predeterminada
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/El_Salvador");

    @Override
    public OffsetDateTime getAsObject(FacesContext ctx, UIComponent comp, String value) {
        if (value == null || value.isBlank()) return null;

        String v = value.trim();

        try {
            // Si tiene offset, usa el formato correspondiente
            if (v.matches(".*[+-]\\d{2}:\\d{2}$")) {
                return OffsetDateTime.parse(v, FMT_WITH_OFFSET);
            }
            // Si no tiene offset, asume la zona por defecto
            LocalDateTime ldt = LocalDateTime.parse(v, FMT_NO_OFFSET);
            return ldt.atZone(DEFAULT_ZONE).toOffsetDateTime();
        } catch (DateTimeParseException e) {
            throw new ConverterException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fecha inválida",
                            "Usa yyyy-MM-dd HH:mm o con offset: yyyy-MM-dd HH:mm-06:00"));
        }
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent comp, OffsetDateTime value) {
        if (value == null) return "";
        // Devuelve la fecha en el formato sin offset
        return value.atZoneSameInstant(DEFAULT_ZONE).toLocalDateTime().format(FMT_NO_OFFSET);
    }
}
