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
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoAlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;

import java.io.OutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoAlmacenConverterTest {

    @InjectMocks
    TipoAlmacenConverter cut;

    @Mock
    TipoAlmacenDAO tipoAlmacenDAO;

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
    void testGetAsObject_StringNull() {
        assertNull(cut.getAsObject(context, component, "null"));
    }

    @Test
    void testGetAsObject_Success() {
        Integer id = 1;
        TipoAlmacen expected = new TipoAlmacen();
        expected.setId(id);

        when(tipoAlmacenDAO.findById(id)).thenReturn(expected);

        TipoAlmacen result = cut.getAsObject(context, component, "1");
        assertNotNull(result);
        assertEquals(expected, result);
        verify(tipoAlmacenDAO).findById(id);
    }

    @Test
    void testGetAsObject_Exception() {
        assertNull(cut.getAsObject(context, component, "abc"));
    }

    @Test
    void testGetAsString_Null() {
        assertEquals("", cut.getAsString(context, component, null));
    }

    @Test
    void testGetAsString_NullId() {
        TipoAlmacen entity = new TipoAlmacen();
        entity.setId(null);
        assertEquals("", cut.getAsString(context, component, entity));
    }

    @Test
    void testGetAsString_Success() {
        Integer id = 1;
        TipoAlmacen entity = new TipoAlmacen();
        entity.setId(id);
        assertEquals("1", cut.getAsString(context, component, entity));
    }
}