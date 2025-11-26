package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OffsetDateTimeJpaConverterTest {

    @InjectMocks
    OffsetDateTimeJpaConverter cut;

    @Test
    void testConvertToDatabaseColumn_Null() {
        assertNull(cut.convertToDatabaseColumn(null));
    }

    @Test
    void testConvertToDatabaseColumn_Success() {
        OffsetDateTime input = OffsetDateTime.of(2023, 11, 25, 10, 30, 0, 0, ZoneOffset.UTC);
        Timestamp result = cut.convertToDatabaseColumn(input);

        assertNotNull(result);
        assertEquals(Timestamp.from(input.toInstant()), result);
    }

    @Test
    void testConvertToEntityAttribute_Null() {
        assertNull(cut.convertToEntityAttribute(null));
    }

    @Test
    void testConvertToEntityAttribute_Success() {
        Instant instant = Instant.parse("2023-11-25T10:30:00Z");
        Timestamp dbValue = Timestamp.from(instant);

        OffsetDateTime result = cut.convertToEntityAttribute(dbValue);

        assertNotNull(result);
        assertEquals(instant, result.toInstant());

        ZoneId zone = ZoneId.of("America/El_Salvador");
        ZoneOffset expectedOffset = zone.getRules().getOffset(instant);
        assertEquals(expectedOffset, result.getOffset());
    }
}