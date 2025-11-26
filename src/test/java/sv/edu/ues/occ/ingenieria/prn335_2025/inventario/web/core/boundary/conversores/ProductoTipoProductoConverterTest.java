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
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProducto;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoTipoProductoConverterTest {

    @InjectMocks
    ProductoTipoProductoConverter cut;

    @Mock
    ProductoTipoProductoDAO dao;

    @Mock
    FacesContext context;

    @Mock
    UIComponent component;

    private Level originalLogLevel;

    @BeforeEach
    void setUp() {
        Logger logger = Logger.getLogger(ProductoTipoProductoConverter.class.getName());
        originalLogLevel = logger.getLevel();
        logger.setLevel(Level.OFF);
    }

    @AfterEach
    void tearDown() {
        Logger.getLogger(ProductoTipoProductoConverter.class.getName()).setLevel(originalLogLevel);
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
        UUID id = UUID.randomUUID();
        ProductoTipoProducto expected = new ProductoTipoProducto();
        expected.setId(id);

        when(dao.find(id)).thenReturn(expected);

        ProductoTipoProducto result = cut.getAsObject(context, component, id.toString());
        assertNotNull(result);
        assertEquals(expected, result);
        verify(dao).find(id);
    }

    @Test
    void testGetAsObject_NotFound() {
        UUID id = UUID.randomUUID();
        when(dao.find(id)).thenReturn(null);

        assertNull(cut.getAsObject(context, component, id.toString()));
        verify(dao).find(id);
    }

    @Test
    void testGetAsObject_InvalidUUID() {
        assertThrows(ConverterException.class, () -> cut.getAsObject(context, component, "invalid-uuid"));
        verify(dao, never()).find(any());
    }

    @Test
    void testGetAsObject_DAOException() {
        UUID id = UUID.randomUUID();
        when(dao.find(id)).thenThrow(new RuntimeException("Error DB"));

        assertThrows(ConverterException.class, () -> cut.getAsObject(context, component, id.toString()));
    }

    @Test
    void testGetAsString_Null() {
        assertEquals("", cut.getAsString(context, component, null));
    }

    @Test
    void testGetAsString_NullId() {
        ProductoTipoProducto entity = new ProductoTipoProducto();
        entity.setId(null);
        assertEquals("", cut.getAsString(context, component, entity));
    }

    @Test
    void testGetAsString_Success() {
        UUID id = UUID.randomUUID();
        ProductoTipoProducto entity = new ProductoTipoProducto();
        entity.setId(id);
        assertEquals(id.toString(), cut.getAsString(context, component, entity));
    }
}