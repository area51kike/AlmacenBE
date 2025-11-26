package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UUIDConverterTest {

    @InjectMocks
    UUIDConverter cut;

    @Mock
    FacesContext context;

    @Mock
    UIComponent component;

    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        }));
    }

    @AfterEach
    void tearDown() {
        System.setErr(originalErr);
    }

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
        UUID uuid = UUID.randomUUID();
        UUID result = cut.getAsObject(context, component, uuid.toString());
        assertEquals(uuid, result);
    }

    @Test
    void testGetAsObject_Exception() {
        assertNull(cut.getAsObject(context, component, "invalid-uuid"));
    }

    @Test
    void testGetAsString_Null() {
        assertEquals("", cut.getAsString(context, component, null));
    }

    @Test
    void testGetAsString_Success() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid.toString(), cut.getAsString(context, component, uuid));
    }
}