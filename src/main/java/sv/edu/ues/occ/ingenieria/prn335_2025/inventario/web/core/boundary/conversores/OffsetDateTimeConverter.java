package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

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

    // ✅ CORRECCIÓN: Formato que usa PrimeFaces Calendar
    private static final DateTimeFormatter UI_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Formatos de fecha con y sin offset (para compatibilidad)
    private static final DateTimeFormatter FMT_WITH_OFFSET = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mmXXX");
    private static final DateTimeFormatter FMT_NO_OFFSET = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Zona horaria predeterminada
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/El_Salvador");

    @Override
    public OffsetDateTime getAsObject(FacesContext ctx, UIComponent comp, String value) {
        if (value == null || value.isBlank()) return null;

        String v = value.trim();

        try {
            // ✅ PRIORIDAD 1: Intentar con formato UI (dd/MM/yyyy HH:mm)
            try {
                LocalDateTime ldt = LocalDateTime.parse(v, UI_FORMATTER);
                return ldt.atZone(DEFAULT_ZONE).toOffsetDateTime();
            } catch (DateTimeParseException e1) {
                // Continuar con otros formatos
            }

            // Si tiene offset, usa el formato correspondiente
            if (v.matches(".*[+-]\\d{2}:\\d{2}$")) {
                return OffsetDateTime.parse(v, FMT_WITH_OFFSET);
            }

            // Si no tiene offset, asume formato yyyy-MM-dd HH:mm
            LocalDateTime ldt = LocalDateTime.parse(v, FMT_NO_OFFSET);
            return ldt.atZone(DEFAULT_ZONE).toOffsetDateTime();

        } catch (DateTimeParseException e) {
            throw new ConverterException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fecha inválida",
                            "Usa dd/MM/yyyy HH:mm"));
        }
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent comp, OffsetDateTime value) {
        if (value == null) return "";
        // ✅ CORRECCIÓN: Devuelve en formato UI (dd/MM/yyyy HH:mm)
        return value.atZoneSameInstant(DEFAULT_ZONE).toLocalDateTime().format(UI_FORMATTER);
    }
}