package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoConverterTest {

    @InjectMocks
    ProductoConverter cut;

    @Mock
    ProductoDAO productoDao;

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
    void testGetAsObject_InvalidUUID() {
        assertNull(cut.getAsObject(context, component, "invalid-uuid"));
        verify(productoDao, never()).findById(any());
    }

    @Test
    void testGetAsObject_NotFound() {
        UUID uuid = UUID.randomUUID();
        when(productoDao.findById(uuid)).thenReturn(null);

        assertNull(cut.getAsObject(context, component, uuid.toString()));
        verify(productoDao).findById(uuid);
    }

    @Test
    void testGetAsObject_Success() {
        UUID uuid = UUID.randomUUID();
        Producto expected = new Producto();
        expected.setId(uuid);

        when(productoDao.findById(uuid)).thenReturn(expected);

        Producto result = cut.getAsObject(context, component, uuid.toString());

        assertNotNull(result);
        assertEquals(expected, result);
        verify(productoDao).findById(uuid);
    }

    @Test
    void testGetAsString_Null() {
        assertEquals("", cut.getAsString(context, component, null));
    }

    @Test
    void testGetAsString_NullId() {
        Producto entity = new Producto();
        entity.setId(null);
        assertEquals("", cut.getAsString(context, component, entity));
    }

    @Test
    void testGetAsString_Success() {
        UUID uuid = UUID.randomUUID();
        Producto entity = new Producto();
        entity.setId(uuid);

        assertEquals(uuid.toString(), cut.getAsString(context, component, entity));
    }
}