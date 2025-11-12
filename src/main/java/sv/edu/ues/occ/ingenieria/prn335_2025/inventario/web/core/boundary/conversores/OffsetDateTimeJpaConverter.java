package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Converter(autoApply = true) // Aplica automáticamente a todos los OffsetDateTime
public class OffsetDateTimeJpaConverter implements AttributeConverter<OffsetDateTime, Timestamp> {

    // ✅ Define la zona horaria de El Salvador
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/El_Salvador");

    // Convierte de OffsetDateTime (Entidad Java) a Timestamp (Columna de BD)
    @Override
    public Timestamp convertToDatabaseColumn(OffsetDateTime entityValue) {
        if (entityValue == null) {
            return null;
        }
        // Convierte a Instant y luego a Timestamp para persistir el punto en el tiempo
        return Timestamp.from(entityValue.toInstant());
    }

    // Convierte de Timestamp (Columna de BD) a OffsetDateTime (Entidad Java)
    @Override
    public OffsetDateTime convertToEntityAttribute(Timestamp databaseValue) {
        if (databaseValue == null) {
            return null;
        }
        // ✅ Usa toInstant y atZone para crear el OffsetDateTime con zona de El Salvador
        return databaseValue.toInstant().atZone(DEFAULT_ZONE).toOffsetDateTime();
    }
}