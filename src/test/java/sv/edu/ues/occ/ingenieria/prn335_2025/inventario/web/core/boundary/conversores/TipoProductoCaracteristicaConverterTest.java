package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.ConverterException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProductoCaracteristica;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoProductoCaracteristicaConverterTest {

    @InjectMocks
    TipoProductoCaracteristicaConverter cut;

    @Mock
    TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDAO;

    @Mock
    FacesContext context;

    @Mock
    UIComponent component;

    private Level originalLevel;

    @BeforeEach
    void setUp() {
        Logger logger = Logger.getLogger(TipoProductoCaracteristicaConverter.class.getName());
        originalLevel = logger.getLevel();
        logger.setLevel(Level.OFF);
    }

    @AfterEach
    void tearDown() {
        Logger.getLogger(TipoProductoCaracteristicaConverter.class.getName()).setLevel(originalLevel);
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
        Long id = 1L;
        TipoProductoCaracteristica expected = new TipoProductoCaracteristica();
        expected.setId(id);

        when(tipoProductoCaracteristicaDAO.find(id)).thenReturn(expected);

        TipoProductoCaracteristica result = cut.getAsObject(context, component, "1");
        assertNotNull(result);
        assertEquals(expected, result);
        verify(tipoProductoCaracteristicaDAO).find(id);
    }

    @Test
    void testGetAsObject_NotFound() {
        Long id = 1L;
        when(tipoProductoCaracteristicaDAO.find(id)).thenReturn(null);

        assertNull(cut.getAsObject(context, component, "1"));
        verify(tipoProductoCaracteristicaDAO).find(id);
    }

    @Test
    void testGetAsObject_NumberFormatException() {
        assertThrows(ConverterException.class, () -> cut.getAsObject(context, component, "abc"));
    }

    @Test
    void testGetAsObject_GeneralException() {
        Long id = 1L;
        when(tipoProductoCaracteristicaDAO.find(id)).thenThrow(new RuntimeException("DB Error"));

        assertThrows(ConverterException.class, () -> cut.getAsObject(context, component, "1"));
    }

    @Test
    void testGetAsString_Null() {
        assertEquals("", cut.getAsString(context, component, null));
    }

    @Test
    void testGetAsString_NullId() {
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        entity.setId(null);
        assertEquals("", cut.getAsString(context, component, entity));
    }

    @Test
    void testGetAsString_Success() {
        Long id = 1L;
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        entity.setId(id);
        assertEquals("1", cut.getAsString(context, component, entity));
    }
}