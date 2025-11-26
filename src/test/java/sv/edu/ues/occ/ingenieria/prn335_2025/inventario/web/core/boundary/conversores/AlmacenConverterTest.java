package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores.AlmacenConverter;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.AlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Almacen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlmacenConverterTest {

    @InjectMocks
    AlmacenConverter cut; // Component Under Test

    @Mock
    AlmacenDAO almacenDao;

    @Mock
    FacesContext context;

    @Mock
    UIComponent component;

    @Test
    void testGetAsObject_NullOrEmpty() {
        assertNull(cut.getAsObject(context, component, null));
        assertNull(cut.getAsObject(context, component, ""));
    }

    @Test
    void testGetAsObject_NumberFormatException() {
        assertNull(cut.getAsObject(context, component, "abc"));
        verify(almacenDao, never()).findById(anyInt());
    }

    @Test
    void testGetAsObject_Success() {
        Integer idBuscado = 1;
        Almacen mockAlmacen = new Almacen();
        mockAlmacen.setId(idBuscado);

        when(almacenDao.findById(idBuscado)).thenReturn(mockAlmacen);

        Almacen result = cut.getAsObject(context, component, "1");

        assertNotNull(result);
        assertEquals(mockAlmacen, result);
        verify(almacenDao).findById(idBuscado);
    }


    @Test
    void testGetAsString_NullValue() {
        String result = cut.getAsString(context, component, null);
        assertEquals("", result);
    }

    @Test
    void testGetAsString_NullId() {
        Almacen almacenSinId = new Almacen();
        almacenSinId.setId(null);

        String result = cut.getAsString(context, component, almacenSinId);
        assertEquals("", result);
    }

    @Test
    void testGetAsString_Success() {
        Almacen almacen = new Almacen();
        almacen.setId(123);

        String result = cut.getAsString(context, component, almacen);

        assertNotNull(result);
        assertEquals("123", result);
    }
}