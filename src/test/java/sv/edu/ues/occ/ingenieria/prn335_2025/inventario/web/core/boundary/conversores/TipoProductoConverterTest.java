package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProducto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoProductoConverterTest {

    @InjectMocks
    TipoProductoConverter cut;

    @Mock
    TipoProductoDAO tipoProductoDao;

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
    }

    @Test
    void testGetAsObject_NumberFormatException() {
        assertNull(cut.getAsObject(context, component, "abc"));
        verify(tipoProductoDao, never()).findById(any());
    }

    @Test
    void testGetAsObject_Success() {
        Long id = 1L;
        TipoProducto expected = new TipoProducto();
        expected.setId(id);

        when(tipoProductoDao.findById(id)).thenReturn(expected);

        TipoProducto result = cut.getAsObject(context, component, "1");
        assertNotNull(result);
        assertEquals(expected, result);
        verify(tipoProductoDao).findById(id);
    }

    @Test
    void testGetAsString_Null() {
        assertEquals("", cut.getAsString(context, component, null));
    }

    @Test
    void testGetAsString_NullId() {
        TipoProducto entity = new TipoProducto();
        entity.setId(null);
        assertEquals("", cut.getAsString(context, component, entity));
    }

    @Test
    void testGetAsString_Success() {
        Long id = 1L;
        TipoProducto entity = new TipoProducto();
        entity.setId(id);
        assertEquals("1", cut.getAsString(context, component, entity));
    }
}