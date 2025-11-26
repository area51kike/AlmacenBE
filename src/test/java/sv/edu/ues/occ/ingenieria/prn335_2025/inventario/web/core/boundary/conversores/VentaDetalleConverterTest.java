package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.VentaDetalle;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaDetalleConverterTest {

    @InjectMocks
    VentaDetalleConverter cut;

    @Mock
    VentaDetalleDAO ventaDetalleDao;

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
        verify(ventaDetalleDao, never()).findById(any());
    }

    @Test
    void testGetAsObject_Success() {
        UUID uuid = UUID.randomUUID();
        VentaDetalle expected = new VentaDetalle();
        expected.setId(uuid);

        when(ventaDetalleDao.findById(uuid)).thenReturn(expected);

        VentaDetalle result = cut.getAsObject(context, component, uuid.toString());

        assertNotNull(result);
        assertEquals(expected, result);
        verify(ventaDetalleDao).findById(uuid);
    }

    @Test
    void testGetAsString_NullValue() {
        assertEquals("", cut.getAsString(context, component, null));
    }

    @Test
    void testGetAsString_NullId() {
        VentaDetalle entity = new VentaDetalle();
        entity.setId(null);
        assertEquals("", cut.getAsString(context, component, entity));
    }

    @Test
    void testGetAsString_Success() {
        UUID uuid = UUID.randomUUID();
        VentaDetalle entity = new VentaDetalle();
        entity.setId(uuid);

        assertEquals(uuid.toString(), cut.getAsString(context, component, entity));
    }
}