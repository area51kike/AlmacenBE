package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.ConverterException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OffsetDateTimeConverterTest {

    @InjectMocks
    OffsetDateTimeConverter cut;

    @Mock
    FacesContext context;

    @Mock
    UIComponent component;

    @Test
    void testGetAsObject_NullOrBlank() {
        assertNull(cut.getAsObject(context, component, null));
        assertNull(cut.getAsObject(context, component, ""));
        assertNull(cut.getAsObject(context, component, "   "));
    }

    @Test
    void testGetAsObject_UIFormat() {
        String input = "25/11/2023 10:30";
        OffsetDateTime result = cut.getAsObject(context, component, input);

        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(11, result.getMonthValue());
        assertEquals(25, result.getDayOfMonth());
        assertEquals(10, result.getHour());
        assertEquals(30, result.getMinute());

        ZoneOffset expectedOffset = ZoneId.of("America/El_Salvador").getRules().getOffset(result.toInstant());
        assertEquals(expectedOffset, result.getOffset());
    }

    @Test
    void testGetAsObject_WithOffset() {
        String input = "2023-11-25 10:30-06:00";
        OffsetDateTime result = cut.getAsObject(context, component, input);

        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(11, result.getMonthValue());
        assertEquals(25, result.getDayOfMonth());
        assertEquals(10, result.getHour());
        assertEquals(ZoneOffset.of("-06:00"), result.getOffset());
    }

    @Test
    void testGetAsObject_NoOffset_Fallback() {
        String input = "2023-11-25 10:30";
        OffsetDateTime result = cut.getAsObject(context, component, input);

        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(11, result.getMonthValue());
        assertEquals(25, result.getDayOfMonth());
        assertEquals(10, result.getHour());

        ZoneOffset expectedOffset = ZoneId.of("America/El_Salvador").getRules().getOffset(result.toInstant());
        assertEquals(expectedOffset, result.getOffset());
    }

    @Test
    void testGetAsObject_InvalidFormat() {
        assertThrows(ConverterException.class, () ->
                cut.getAsObject(context, component, "invalid-date-string")
        );
    }

    @Test
    void testGetAsString_Null() {
        assertEquals("", cut.getAsString(context, component, null));
    }

    @Test
    void testGetAsString_Success() {
        OffsetDateTime value = OffsetDateTime.of(2023, 11, 25, 16, 30, 0, 0, ZoneOffset.UTC);

        // UTC 16:30 is 10:30 in El Salvador (UTC-6 standard time) or similar depending on DST history,
        // checking conversion relative to formatter pattern "dd/MM/yyyy HH:mm"

        String result = cut.getAsString(context, component, value);

        // Assuming standard time UTC-6 for El Salvador in Nov 2023
        assertEquals("25/11/2023 10:30", result);
    }
}