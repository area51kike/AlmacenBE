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
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoUnidadMedidaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoUnidadMedida;

import java.io.OutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoUnidadMedidaConverterTest {

    @InjectMocks
    TipoUnidadMedidaConverter cut;

    @Mock
    TipoUnidadMedidaDAO tipoUnidadMedidaDAO;

    @Mock
    FacesContext context;

    @Mock
    UIComponent component;

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        PrintStream silentStream = new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        });
        System.setOut(silentStream);
        System.setErr(silentStream);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
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
        TipoUnidadMedida expected = new TipoUnidadMedida();
        expected.setId(id);
        expected.setNombre("Metro");

        when(tipoUnidadMedidaDAO.findById(id)).thenReturn(expected);

        TipoUnidadMedida result = cut.getAsObject(context, component, "1");
        assertNotNull(result);
        assertEquals(expected, result);
        verify(tipoUnidadMedidaDAO).findById(id);
    }

    @Test
    void testGetAsObject_NotFound() {
        Integer id = 99;
        when(tipoUnidadMedidaDAO.findById(id)).thenReturn(null);

        assertNull(cut.getAsObject(context, component, "99"));
        verify(tipoUnidadMedidaDAO).findById(id);
    }

    @Test
    void testGetAsObject_Exception() {
        assertNull(cut.getAsObject(context, component, "abc"));
        verify(tipoUnidadMedidaDAO, never()).findById(anyInt());
    }

    @Test
    void testGetAsString_Null() {
        assertEquals("", cut.getAsString(context, component, null));
    }

    @Test
    void testGetAsString_NullId() {
        TipoUnidadMedida entity = new TipoUnidadMedida();
        entity.setId(null);
        assertEquals("", cut.getAsString(context, component, entity));
    }

    @Test
    void testGetAsString_Success() {
        Integer id = 10;
        TipoUnidadMedida entity = new TipoUnidadMedida();
        entity.setId(id);
        assertEquals("10", cut.getAsString(context, component, entity));
    }
}