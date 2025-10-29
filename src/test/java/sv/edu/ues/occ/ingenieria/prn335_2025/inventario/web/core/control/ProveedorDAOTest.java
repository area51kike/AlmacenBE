package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Proveedor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProveedorDAOTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ProveedorDAO proveedorDAO;

    private Proveedor proveedor;
    private Integer testId;

    @BeforeEach
    void setUp() {
        proveedorDAO = new ProveedorDAO();
        proveedorDAO.em = entityManager;

        testId = 1;
        proveedor = new Proveedor();
        proveedor.setId(testId);
        proveedor.setNombre("Distribuidora El Salvador S.A.");
        proveedor.setRazonSocial("Distribuidora El Salvador Sociedad Anónima");
        proveedor.setNit("0614-123456-101-2");
        proveedor.setActivo(true);
        proveedor.setObservaciones("Proveedor principal de tecnología");
    }

    @Test
    void testGetEntityManager() {
        EntityManager result = proveedorDAO.getEntityManager();
        assertNotNull(result);
        assertEquals(entityManager, result);
    }

    @Test
    void testConstructor() {
        ProveedorDAO dao = new ProveedorDAO();
        assertNotNull(dao);
    }

    @Test
    void testConstructorWithParameter() {
        ProveedorDAO dao = new ProveedorDAO(Proveedor.class);
        assertNotNull(dao);
    }

    @Test
    void testFindById_Success() {
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result = proveedorDAO.findById(testId);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("Distribuidora El Salvador S.A.", result.getNombre());
        assertEquals("Distribuidora El Salvador Sociedad Anónima", result.getRazonSocial());
        assertEquals("0614-123456-101-2", result.getNit());
        assertTrue(result.getActivo());
        assertEquals("Proveedor principal de tecnología", result.getObservaciones());
        verify(entityManager).find(Proveedor.class, testId);
    }

    @Test
    void testFindById_NotFound() {
        Integer id = 999;
        when(entityManager.find(Proveedor.class, id)).thenReturn(null);

        Proveedor result = proveedorDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Proveedor.class, id);
    }

    @Test
    void testFindById_Exception() {
        Integer id = 1;
        when(entityManager.find(Proveedor.class, id))
                .thenThrow(new RuntimeException("Database connection error"));

        assertThrows(RuntimeException.class, () -> proveedorDAO.findById(id));
        verify(entityManager).find(Proveedor.class, id);
    }

    @Test
    void testFindById_InactiveProveedor() {
        proveedor.setActivo(false);
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result = proveedorDAO.findById(testId);

        assertNotNull(result);
        assertFalse(result.getActivo());
        verify(entityManager).find(Proveedor.class, testId);
    }

    @Test
    void testFindById_WithNullNombre() {
        proveedor.setNombre(null);
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result = proveedorDAO.findById(testId);

        assertNotNull(result);
        assertNull(result.getNombre());
        verify(entityManager).find(Proveedor.class, testId);
    }

    @Test
    void testFindById_WithNullRazonSocial() {
        proveedor.setRazonSocial(null);
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result = proveedorDAO.findById(testId);

        assertNotNull(result);
        assertNull(result.getRazonSocial());
        verify(entityManager).find(Proveedor.class, testId);
    }

    @Test
    void testFindById_WithNullNit() {
        proveedor.setNit(null);
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result = proveedorDAO.findById(testId);

        assertNotNull(result);
        assertNull(result.getNit());
        verify(entityManager).find(Proveedor.class, testId);
    }

    @Test
    void testFindById_WithNullObservaciones() {
        proveedor.setObservaciones(null);
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result = proveedorDAO.findById(testId);

        assertNotNull(result);
        assertNull(result.getObservaciones());
        verify(entityManager).find(Proveedor.class, testId);
    }

    @Test
    void testFindById_WithEmptyNombre() {
        proveedor.setNombre("");
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result = proveedorDAO.findById(testId);

        assertNotNull(result);
        assertEquals("", result.getNombre());
        verify(entityManager).find(Proveedor.class, testId);
    }

    @Test
    void testFindById_WithEmptyRazonSocial() {
        proveedor.setRazonSocial("");
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result = proveedorDAO.findById(testId);

        assertNotNull(result);
        assertEquals("", result.getRazonSocial());
        verify(entityManager).find(Proveedor.class, testId);
    }

    @Test
    void testFindById_WithMaxLengthNombre() {
        String maxNombre = "A".repeat(155);
        proveedor.setNombre(maxNombre);
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result = proveedorDAO.findById(testId);

        assertNotNull(result);
        assertEquals(155, result.getNombre().length());
        verify(entityManager).find(Proveedor.class, testId);
    }

    @Test
    void testFindById_WithMaxLengthRazonSocial() {
        String maxRazonSocial = "R".repeat(155);
        proveedor.setRazonSocial(maxRazonSocial);
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result = proveedorDAO.findById(testId);

        assertNotNull(result);
        assertEquals(155, result.getRazonSocial().length());
        verify(entityManager).find(Proveedor.class, testId);
    }

    @Test
    void testFindById_WithMaxLengthNit() {
        String maxNit = "01234567890123";
        proveedor.setNit(maxNit);
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result = proveedorDAO.findById(testId);

        assertNotNull(result);
        assertEquals(14, result.getNit().length());
        verify(entityManager).find(Proveedor.class, testId);
    }

    @Test
    void testFindById_WithLongObservaciones() {
        String longObservaciones = "Observación: " + "X".repeat(1000);
        proveedor.setObservaciones(longObservaciones);
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result = proveedorDAO.findById(testId);

        assertNotNull(result);
        assertTrue(result.getObservaciones().length() > 1000);
        verify(entityManager).find(Proveedor.class, testId);
    }

    @Test
    void testFindById_MultipleCalls() {
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result1 = proveedorDAO.findById(testId);
        Proveedor result2 = proveedorDAO.findById(testId);

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getId(), result2.getId());
        verify(entityManager, times(2)).find(Proveedor.class, testId);
    }

    @Test
    void testFindById_DifferentIds() {
        Integer id1 = 1;
        Integer id2 = 2;

        Proveedor proveedor1 = new Proveedor();
        proveedor1.setId(id1);
        proveedor1.setNombre("Proveedor 1");

        Proveedor proveedor2 = new Proveedor();
        proveedor2.setId(id2);
        proveedor2.setNombre("Proveedor 2");

        when(entityManager.find(Proveedor.class, id1)).thenReturn(proveedor1);
        when(entityManager.find(Proveedor.class, id2)).thenReturn(proveedor2);

        Proveedor result1 = proveedorDAO.findById(id1);
        Proveedor result2 = proveedorDAO.findById(id2);

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1.getId(), result2.getId());
        assertEquals("Proveedor 1", result1.getNombre());
        assertEquals("Proveedor 2", result2.getNombre());
    }

    @Test
    void testFindById_WithNullActivo() {
        proveedor.setActivo(null);
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result = proveedorDAO.findById(testId);

        assertNotNull(result);
        assertNull(result.getActivo());
        verify(entityManager).find(Proveedor.class, testId);
    }

    @Test
    void testFindById_WithSpecialCharactersInNombre() {
        proveedor.setNombre("Distribuidora José María & Cía. S.A. de C.V.");
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result = proveedorDAO.findById(testId);

        assertNotNull(result);
        assertEquals("Distribuidora José María & Cía. S.A. de C.V.", result.getNombre());
        verify(entityManager).find(Proveedor.class, testId);
    }

    @Test
    void testFindById_WithSpecialCharactersInRazonSocial() {
        proveedor.setRazonSocial("Sociedad Anónima de Capital Variable \"El Progreso\"");
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result = proveedorDAO.findById(testId);

        assertNotNull(result);
        assertEquals("Sociedad Anónima de Capital Variable \"El Progreso\"", result.getRazonSocial());
        verify(entityManager).find(Proveedor.class, testId);
    }

    @Test
    void testFindById_WithZeroId() {
        Integer zeroId = 0;
        Proveedor proveedorZero = new Proveedor();
        proveedorZero.setId(zeroId);
        proveedorZero.setNombre("Proveedor ID Zero");

        when(entityManager.find(Proveedor.class, zeroId)).thenReturn(proveedorZero);

        Proveedor result = proveedorDAO.findById(zeroId);

        assertNotNull(result);
        assertEquals(0, result.getId());
        verify(entityManager).find(Proveedor.class, zeroId);
    }

    @Test
    void testFindById_WithNegativeId() {
        Integer negativeId = -1;
        when(entityManager.find(Proveedor.class, negativeId)).thenReturn(null);

        Proveedor result = proveedorDAO.findById(negativeId);

        assertNull(result);
        verify(entityManager).find(Proveedor.class, negativeId);
    }

    @Test
    void testFindById_WithLargeId() {
        Integer largeId = Integer.MAX_VALUE;
        Proveedor proveedorLarge = new Proveedor();
        proveedorLarge.setId(largeId);
        proveedorLarge.setNombre("Proveedor ID Grande");

        when(entityManager.find(Proveedor.class, largeId)).thenReturn(proveedorLarge);

        Proveedor result = proveedorDAO.findById(largeId);

        assertNotNull(result);
        assertEquals(Integer.MAX_VALUE, result.getId());
        verify(entityManager).find(Proveedor.class, largeId);
    }
}