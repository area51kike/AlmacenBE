package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Almacen;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlmacenDAOTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private AlmacenDAO almacenDAO;

    private Almacen almacen;

    @BeforeEach
    void setUp() {
        almacenDAO = new AlmacenDAO();
        almacenDAO.em = entityManager;

        almacen = new Almacen();
        almacen.setId(1);
        almacen.setActivo(true);
        almacen.setObservaciones("Almacén principal");

        TipoAlmacen tipoAlmacen = new TipoAlmacen();
        tipoAlmacen.setId(1);
        almacen.setIdTipoAlmacen(tipoAlmacen);
    }

    @Test
    void testGetEntityManager() {
        EntityManager result = almacenDAO.getEntityManager();
        assertNotNull(result);
        assertEquals(entityManager, result);
    }

    @Test
    void testConstructor() {
        AlmacenDAO dao = new AlmacenDAO();
        assertNotNull(dao);
    }

    @Test
    void testFindById_Success() {
        Integer id = 1;
        when(entityManager.find(Almacen.class, id)).thenReturn(almacen);

        Almacen result = almacenDAO.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Almacén principal", result.getObservaciones());
        assertTrue(result.getActivo());
        verify(entityManager).find(Almacen.class, id);
    }

    @Test
    void testFindById_NotFound() {
        Integer id = 999;
        when(entityManager.find(Almacen.class, id)).thenReturn(null);

        Almacen result = almacenDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Almacen.class, id);
    }



    @Test
    void testFindById_WithTipoAlmacen() {
        TipoAlmacen tipoAlmacen = new TipoAlmacen();
        tipoAlmacen.setId(2);
        tipoAlmacen.setNombre("Almacén de Materia Prima");

        almacen.setIdTipoAlmacen(tipoAlmacen);

        when(entityManager.find(Almacen.class, 1)).thenReturn(almacen);

        Almacen result = almacenDAO.findById(1);

        assertNotNull(result);
        assertNotNull(result.getIdTipoAlmacen());
        assertEquals(2, result.getIdTipoAlmacen().getId());
        assertEquals("Almacén de Materia Prima", result.getIdTipoAlmacen().getNombre());
    }

    @Test
    void testFindById_InactiveAlmacen() {
        almacen.setActivo(false);
        Integer id = 1;

        when(entityManager.find(Almacen.class, id)).thenReturn(almacen);

        Almacen result = almacenDAO.findById(id);

        assertNotNull(result);
        assertFalse(result.getActivo());
        verify(entityManager).find(Almacen.class, id);
    }

    @Test
    void testFindById_WithNullObservaciones() {
        almacen.setObservaciones(null);
        Integer id = 1;

        when(entityManager.find(Almacen.class, id)).thenReturn(almacen);

        Almacen result = almacenDAO.findById(id);

        assertNotNull(result);
        assertNull(result.getObservaciones());
        verify(entityManager).find(Almacen.class, id);
    }

    @Test
    void testFindById_WithEmptyObservaciones() {
        almacen.setObservaciones("");
        Integer id = 1;

        when(entityManager.find(Almacen.class, id)).thenReturn(almacen);

        Almacen result = almacenDAO.findById(id);

        assertNotNull(result);
        assertEquals("", result.getObservaciones());
        verify(entityManager).find(Almacen.class, id);
    }

    @Test
    void testFindById_WithLongObservaciones() {
        String longObservaciones = "A".repeat(5000);
        almacen.setObservaciones(longObservaciones);
        Integer id = 1;

        when(entityManager.find(Almacen.class, id)).thenReturn(almacen);

        Almacen result = almacenDAO.findById(id);

        assertNotNull(result);
        assertEquals(5000, result.getObservaciones().length());
        verify(entityManager).find(Almacen.class, id);
    }

    @Test
    void testFindById_WithZeroId() {
        Integer id = 0;
        when(entityManager.find(Almacen.class, id)).thenReturn(null);

        Almacen result = almacenDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Almacen.class, id);
    }

    @Test
    void testFindById_WithNegativeId() {
        Integer id = -1;
        when(entityManager.find(Almacen.class, id)).thenReturn(null);

        Almacen result = almacenDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Almacen.class, id);
    }

    @Test
    void testFindById_MultipleCalls() {
        Integer id = 1;
        when(entityManager.find(Almacen.class, id)).thenReturn(almacen);

        Almacen result1 = almacenDAO.findById(id);
        Almacen result2 = almacenDAO.findById(id);

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getId(), result2.getId());
        verify(entityManager, times(2)).find(Almacen.class, id);
    }

    @Test
    void testFindById_WithNullTipoAlmacen() {
        almacen.setIdTipoAlmacen(null);
        Integer id = 1;

        when(entityManager.find(Almacen.class, id)).thenReturn(almacen);

        Almacen result = almacenDAO.findById(id);

        assertNotNull(result);
        assertNull(result.getIdTipoAlmacen());
        verify(entityManager).find(Almacen.class, id);
    }
}