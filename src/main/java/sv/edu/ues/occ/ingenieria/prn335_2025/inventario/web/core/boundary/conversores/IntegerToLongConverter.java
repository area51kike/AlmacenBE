// Archivo: IntegerToLongConverter.java

package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false) // No queremos aplicarlo automáticamente a todos los Long/Integer
public class IntegerToLongConverter implements AttributeConverter<Long, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Long attribute) {
        // Al escribir en DB: Long -> Integer
        return (attribute == null) ? null : attribute.intValue();
    }

    @Override
    public Long convertToEntityAttribute(Integer dbData) {
        // Al leer de DB: Integer -> Long (Soluciona el ClassCastException)
        return (dbData == null) ? null : dbData.longValue();
    }
}