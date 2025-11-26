package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.CompraDetalle;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompraDetalleConverterTest {

    @InjectMocks
    CompraDetalleConverter cut;

    @Mock
    CompraDetalleDAO compraDetalleDao;

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
    void testGetAsObject_IllegalArgumentException() {
        assertNull(cut.getAsObject(context, component, "invalid-uuid"));
        verify(compraDetalleDao, never()).findById(any());
    }

    @Test
    void testGetAsObject_Success() {
        UUID uuid = UUID.randomUUID();
        CompraDetalle expected = new CompraDetalle();
        expected.setId(uuid);

        when(compraDetalleDao.findById(uuid)).thenReturn(expected);

        CompraDetalle result = cut.getAsObject(context, component, uuid.toString());

        assertNotNull(result);
        assertEquals(expected, result);
        verify(compraDetalleDao).findById(uuid);
    }

    @Test
    void testGetAsString_NullValue() {
        assertEquals("", cut.getAsString(context, component, null));
    }

    @Test
    void testGetAsString_NullId() {
        CompraDetalle entity = new CompraDetalle();
        entity.setId(null);
        assertEquals("", cut.getAsString(context, component, entity));
    }

    @Test
    void testGetAsString_Success() {
        UUID uuid = UUID.randomUUID();
        CompraDetalle entity = new CompraDetalle();
        entity.setId(uuid);

        assertEquals(uuid.toString(), cut.getAsString(context, component, entity));
    }
}