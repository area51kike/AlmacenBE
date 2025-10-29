package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoAlmacenDAOTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private TipoAlmacenDAO tipoAlmacenDAO;

    private TipoAlmacen tipoAlmacenTest;

    @BeforeEach
    void setUp() throws Exception {
        tipoAlmacenTest = new TipoAlmacen();
        tipoAlmacenTest.setId(1);
        tipoAlmacenTest.setNombre("Almacen Principal");

        // Inyectar el EntityManager mock usando reflexión
        Field emField = TipoAlmacenDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(tipoAlmacenDAO, em);
    }

    @Test
    void testConstructorVacio() {
        TipoAlmacenDAO dao = new TipoAlmacenDAO();
        assertNotNull(dao);
    }

    @Test
    void testConstructorConParametro() {
        TipoAlmacenDAO dao = new TipoAlmacenDAO(TipoAlmacen.class);
        assertNotNull(dao);
    }

    @Test
    void testGetEntityManager() {
        EntityManager result = tipoAlmacenDAO.getEntityManager();
        assertNotNull(result);
        assertEquals(em, result);
    }

    @Test
    void testFindByIdExitoso() {
        // Arrange
        Integer id = 1;
        when(em.find(TipoAlmacen.class, id)).thenReturn(tipoAlmacenTest);

        // Act
        TipoAlmacen resultado = tipoAlmacenDAO.findById(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals("Almacen Principal", resultado.getNombre());
        verify(em, times(1)).find(TipoAlmacen.class, id);
    }

    @Test
    void testFindByIdConIdNulo() {
        // Act
        TipoAlmacen resultado = tipoAlmacenDAO.findById(null);

        // Assert
        assertNull(resultado);
        verify(em, never()).find(any(), any());
    }

    @Test
    void testFindByIdNoEncontrado() {
        // Arrange
        Integer id = 999;
        when(em.find(TipoAlmacen.class, id)).thenReturn(null);

        // Act
        TipoAlmacen resultado = tipoAlmacenDAO.findById(id);

        // Assert
        assertNull(resultado);
        verify(em, times(1)).find(TipoAlmacen.class, id);
    }

    @Test
    void testFindByIdConExcepcion() {
        // Arrange
        Integer id = 1;
        when(em.find(TipoAlmacen.class, id))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        TipoAlmacen resultado = tipoAlmacenDAO.findById(id);

        // Assert
        assertNull(resultado);
        verify(em, times(1)).find(TipoAlmacen.class, id);
    }
}