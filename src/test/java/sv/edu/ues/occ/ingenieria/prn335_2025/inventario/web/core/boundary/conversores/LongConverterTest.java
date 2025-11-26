package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.ConverterException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LongConverterTest {

    @InjectMocks
    LongConverter cut;

    @Mock
    FacesContext context;

    @Mock
    UIComponent component;

    @Test
    void testGetAsObject_Null() {
        assertNull(cut.getAsObject(context, component, null));
    }

    @Test
    void testGetAsObject_Empty() {
        assertNull(cut.getAsObject(context, component, ""));
        assertNull(cut.getAsObject(context, component, "   "));
    }

    @Test
    void testGetAsObject_Success() {
        Long expected = 123456789L;
        assertEquals(expected, cut.getAsObject(context, component, "123456789"));
        assertEquals(expected, cut.getAsObject(context, component, " 123456789 "));
    }

    @Test
    void testGetAsObject_Exception() {
        assertThrows(ConverterException.class, () -> cut.getAsObject(context, component, "abc"));
    }

    @Test
    void testGetAsString_Null() {
        assertEquals("", cut.getAsString(context, component, null));
    }

    @Test
    void testGetAsString_Success() {
        assertEquals("987654321", cut.getAsString(context, component, 987654321L));
    }
}