package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores.CaracteristicaConverter;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Caracteristica;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaracteristicaConverterTest {

    @InjectMocks
    CaracteristicaConverter cut; // Component Under Test

    @Mock
    CaracteristicaDAO caracteristicaDAO;

    @Mock
    FacesContext context;

    @Mock
    UIComponent component;


    @Test
    void testGetAsObject_InputsInvalidos() {

        assertNull(cut.getAsObject(context, component, null));
        assertNull(cut.getAsObject(context, component, ""));
        assertNull(cut.getAsObject(context, component, "   "));
        assertNull(cut.getAsObject(context, component, "null"));
    }

    @Test
    void testGetAsObject_Exception() {
        assertNull(cut.getAsObject(context, component, "abc"));
        verify(caracteristicaDAO, never()).findById(anyLong());
    }

    @Test
    void testGetAsObject_Success() {
        Long idBuscado = 10L;
        Caracteristica mockEntity = new Caracteristica();
        mockEntity.setId(Math.toIntExact(idBuscado));

        when(caracteristicaDAO.findById(idBuscado)).thenReturn(mockEntity);

        Caracteristica result = cut.getAsObject(context, component, "10");

        assertNotNull(result);
        assertEquals(mockEntity, result);
        verify(caracteristicaDAO).findById(idBuscado);
    }

    @Test
    void testGetAsString_NullValue() {
        String result = cut.getAsString(context, component, null);
        assertEquals("", result);
    }

    @Test
    void testGetAsString_NullId() {
        Caracteristica entidadSinId = new Caracteristica();
        entidadSinId.setId(null);

        String result = cut.getAsString(context, component, entidadSinId);
        assertEquals("", result);
    }

    @Test
    void testGetAsString_Success() {
        Caracteristica entidad = new Caracteristica();
        entidad.setId(500);

        String result = cut.getAsString(context, component, entidad);

        assertNotNull(result);
        assertEquals("500", result);
    }
}