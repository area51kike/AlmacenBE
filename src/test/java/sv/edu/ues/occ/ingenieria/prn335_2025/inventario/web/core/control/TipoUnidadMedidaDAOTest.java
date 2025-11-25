package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoUnidadMedida;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoUnidadMedidaDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<Long> criteriaQueryLong;

    @Mock
    private Root<TipoUnidadMedida> root;

    private TipoUnidadMedidaDAO tipoUnidadMedidaDAO;

    private TipoUnidadMedida tipoUnidadMedida;
    private Integer testId;

    @BeforeEach
    void setUp() throws Exception {
        tipoUnidadMedidaDAO = new TipoUnidadMedidaDAO();

        // Usar reflexión para inyectar el EntityManager mock
        Field emField = TipoUnidadMedidaDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(tipoUnidadMedidaDAO, entityManager);

        testId = 1;
        tipoUnidadMedida = new TipoUnidadMedida();
        tipoUnidadMedida.setId(testId);
        tipoUnidadMedida.setNombre("Kilogramo");
        tipoUnidadMedida.setUnidadBase("kg");
        tipoUnidadMedida.setActivo(true);
        tipoUnidadMedida.setComentarios("Unidad de masa del Sistema Internacional");
    }

    @Test
    void testGetEntityManager() {
        EntityManager result = tipoUnidadMedidaDAO.getEntityManager();
        assertNotNull(result);
        assertEquals(entityManager, result);
    }

    @Test
    void testGetEntityClass() {
        Class<TipoUnidadMedida> entityClass = tipoUnidadMedidaDAO.getEntityClass();
        assertNotNull(entityClass);
        assertEquals(TipoUnidadMedida.class, entityClass);
    }

    @Test
    void testConstructor() {
        TipoUnidadMedidaDAO dao = new TipoUnidadMedidaDAO();
        assertNotNull(dao);
    }

    @Test
    void testFindById_Success() {
        when(entityManager.find(TipoUnidadMedida.class, testId)).thenReturn(tipoUnidadMedida);

        TipoUnidadMedida result = tipoUnidadMedidaDAO.findById(testId);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("Kilogramo", result.getNombre());
        assertEquals("kg", result.getUnidadBase());
        assertTrue(result.getActivo());
        assertEquals("Unidad de masa del Sistema Internacional", result.getComentarios());
        verify(entityManager).find(TipoUnidadMedida.class, testId);
    }

    @Test
    void testFindById_WithNullId() {
        TipoUnidadMedida result = tipoUnidadMedidaDAO.findById(null);

        assertNull(result);
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void testFindById_NotFound() {
        Integer id = 999;
        when(entityManager.find(TipoUnidadMedida.class, id)).thenReturn(null);

        TipoUnidadMedida result = tipoUnidadMedidaDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(TipoUnidadMedida.class, id);
    }

    @Test
    void testFindById_ThrowsException() {
        when(entityManager.find(TipoUnidadMedida.class, testId))
                .thenThrow(new RuntimeException("Database error"));

        TipoUnidadMedida result = tipoUnidadMedidaDAO.findById(testId);

        assertNull(result);
        verify(entityManager).find(TipoUnidadMedida.class, testId);
    }

    @Test
    void testFindById_InactiveTipoUnidadMedida() {
        tipoUnidadMedida.setActivo(false);
        when(entityManager.find(TipoUnidadMedida.class, testId)).thenReturn(tipoUnidadMedida);

        TipoUnidadMedida result = tipoUnidadMedidaDAO.findById(testId);

        assertNotNull(result);
        assertFalse(result.getActivo());
        verify(entityManager).find(TipoUnidadMedida.class, testId);
    }

    @Test
    void testFindById_WithNullNombre() {
        tipoUnidadMedida.setNombre(null);
        when(entityManager.find(TipoUnidadMedida.class, testId)).thenReturn(tipoUnidadMedida);

        TipoUnidadMedida result = tipoUnidadMedidaDAO.findById(testId);

        assertNotNull(result);
        assertNull(result.getNombre());
        verify(entityManager).find(TipoUnidadMedida.class, testId);
    }

    @Test
    void testFindById_WithNullUnidadBase() {
        tipoUnidadMedida.setUnidadBase(null);
        when(entityManager.find(TipoUnidadMedida.class, testId)).thenReturn(tipoUnidadMedida);

        TipoUnidadMedida result = tipoUnidadMedidaDAO.findById(testId);

        assertNotNull(result);
        assertNull(result.getUnidadBase());
        verify(entityManager).find(TipoUnidadMedida.class, testId);
    }

    @Test
    void testFindById_WithNullComentarios() {
        tipoUnidadMedida.setComentarios(null);
        when(entityManager.find(TipoUnidadMedida.class, testId)).thenReturn(tipoUnidadMedida);

        TipoUnidadMedida result = tipoUnidadMedidaDAO.findById(testId);

        assertNotNull(result);
        assertNull(result.getComentarios());
        verify(entityManager).find(TipoUnidadMedida.class, testId);
    }

    @Test
    void testFindById_WithEmptyNombre() {
        tipoUnidadMedida.setNombre("");
        when(entityManager.find(TipoUnidadMedida.class, testId)).thenReturn(tipoUnidadMedida);

        TipoUnidadMedida result = tipoUnidadMedidaDAO.findById(testId);

        assertNotNull(result);
        assertEquals("", result.getNombre());
        verify(entityManager).find(TipoUnidadMedida.class, testId);
    }

    @Test
    void testFindById_WithEmptyUnidadBase() {
        tipoUnidadMedida.setUnidadBase("");
        when(entityManager.find(TipoUnidadMedida.class, testId)).thenReturn(tipoUnidadMedida);

        TipoUnidadMedida result = tipoUnidadMedidaDAO.findById(testId);

        assertNotNull(result);
        assertEquals("", result.getUnidadBase());
        verify(entityManager).find(TipoUnidadMedida.class, testId);
    }

    @Test
    void testFindById_WithMaxLengthNombre() {
        String maxNombre = "A".repeat(155);
        tipoUnidadMedida.setNombre(maxNombre);
        when(entityManager.find(TipoUnidadMedida.class, testId)).thenReturn(tipoUnidadMedida);

        TipoUnidadMedida result = tipoUnidadMedidaDAO.findById(testId);

        assertNotNull(result);
        assertEquals(155, result.getNombre().length());
        verify(entityManager).find(TipoUnidadMedida.class, testId);
    }

    @Test
    void testFindById_WithMaxLengthUnidadBase() {
        String maxUnidadBase = "u".repeat(155);
        tipoUnidadMedida.setUnidadBase(maxUnidadBase);
        when(entityManager.find(TipoUnidadMedida.class, testId)).thenReturn(tipoUnidadMedida);

        TipoUnidadMedida result = tipoUnidadMedidaDAO.findById(testId);

        assertNotNull(result);
        assertEquals(155, result.getUnidadBase().length());
        verify(entityManager).find(TipoUnidadMedida.class, testId);
    }

    @Test
    void testFindById_WithLongComentarios() {
        String longComentarios = "Comentario: " + "X".repeat(1000);
        tipoUnidadMedida.setComentarios(longComentarios);
        when(entityManager.find(TipoUnidadMedida.class, testId)).thenReturn(tipoUnidadMedida);

        TipoUnidadMedida result = tipoUnidadMedidaDAO.findById(testId);

        assertNotNull(result);
        assertTrue(result.getComentarios().length() > 1000);
        verify(entityManager).find(TipoUnidadMedida.class, testId);
    }

    @Test
    void testFindById_MultipleCalls() {
        when(entityManager.find(TipoUnidadMedida.class, testId)).thenReturn(tipoUnidadMedida);

        TipoUnidadMedida result1 = tipoUnidadMedidaDAO.findById(testId);
        TipoUnidadMedida result2 = tipoUnidadMedidaDAO.findById(testId);

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getId(), result2.getId());
        verify(entityManager, times(2)).find(TipoUnidadMedida.class, testId);
    }

    @Test
    void testFindById_DifferentIds() {
        Integer id1 = 1;
        Integer id2 = 2;

        TipoUnidadMedida tipo1 = new TipoUnidadMedida();
        tipo1.setId(id1);
        tipo1.setNombre("Metro");

        TipoUnidadMedida tipo2 = new TipoUnidadMedida();
        tipo2.setId(id2);
        tipo2.setNombre("Litro");

        when(entityManager.find(TipoUnidadMedida.class, id1)).thenReturn(tipo1);
        when(entityManager.find(TipoUnidadMedida.class, id2)).thenReturn(tipo2);

        TipoUnidadMedida result1 = tipoUnidadMedidaDAO.findById(id1);
        TipoUnidadMedida result2 = tipoUnidadMedidaDAO.findById(id2);

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1.getId(), result2.getId());
        assertEquals("Metro", result1.getNombre());
        assertEquals("Litro", result2.getNombre());
    }

    @Test
    void testFindById_WithNullActivo() {
        tipoUnidadMedida.setActivo(null);
        when(entityManager.find(TipoUnidadMedida.class, testId)).thenReturn(tipoUnidadMedida);

        TipoUnidadMedida result = tipoUnidadMedidaDAO.findById(testId);

        assertNotNull(result);
        assertNull(result.getActivo());
        verify(entityManager).find(TipoUnidadMedida.class, testId);
    }

    @Test
    void testFindById_WithSpecialCharactersInNombre() {
        tipoUnidadMedida.setNombre("Metro³ (cúbico)");
        when(entityManager.find(TipoUnidadMedida.class, testId)).thenReturn(tipoUnidadMedida);

        TipoUnidadMedida result = tipoUnidadMedidaDAO.findById(testId);

        assertNotNull(result);
        assertEquals("Metro³ (cúbico)", result.getNombre());
        verify(entityManager).find(TipoUnidadMedida.class, testId);
    }

    @Test
    void testFindById_WithSpecialCharactersInUnidadBase() {
        tipoUnidadMedida.setUnidadBase("m³");
        when(entityManager.find(TipoUnidadMedida.class, testId)).thenReturn(tipoUnidadMedida);

        TipoUnidadMedida result = tipoUnidadMedidaDAO.findById(testId);

        assertNotNull(result);
        assertEquals("m³", result.getUnidadBase());
        verify(entityManager).find(TipoUnidadMedida.class, testId);
    }

    // ===== TESTS PARA CREAR (usando implementación de clase padre) =====

    @Test
    void testCrear_Success_WithExistingId() {
        // Cuando la entidad ya tiene un ID, se persiste directamente
        TipoUnidadMedida newTipo = new TipoUnidadMedida();
        newTipo.setId(15);
        newTipo.setNombre("Centímetro");
        newTipo.setUnidadBase("cm");
        newTipo.setActivo(true);

        doNothing().when(entityManager).persist(newTipo);

        tipoUnidadMedidaDAO.crear(newTipo);

        assertEquals(15, newTipo.getId());
        verify(entityManager, times(1)).persist(newTipo);
    }

    @Test
    void testCrear_Success_WithNullId() {
        // Con ID null, JPA/Hibernate manejará la generación automática
        TipoUnidadMedida newTipo = new TipoUnidadMedida();
        newTipo.setNombre("Gramo");
        newTipo.setUnidadBase("g");
        newTipo.setActivo(true);

        doNothing().when(entityManager).persist(newTipo);

        tipoUnidadMedidaDAO.crear(newTipo);

        // El ID seguirá siendo null porque no estamos simulando la base de datos
        // En producción, JPA lo asignaría automáticamente
        assertNull(newTipo.getId());
        verify(entityManager, times(1)).persist(newTipo);
    }

    @Test
    void testCrear_Success_WithZeroId() {
        // Con ID = 0, se tratará como nuevo registro
        TipoUnidadMedida newTipo = new TipoUnidadMedida();
        newTipo.setId(0);
        newTipo.setNombre("Tonelada");
        newTipo.setUnidadBase("t");
        newTipo.setActivo(true);

        doNothing().when(entityManager).persist(newTipo);

        tipoUnidadMedidaDAO.crear(newTipo);

        // El ID permanece en 0 porque no hay lógica personalizada
        assertEquals(0, newTipo.getId());
        verify(entityManager, times(1)).persist(newTipo);
    }

    @Test
    void testCrear_NullRegistro() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tipoUnidadMedidaDAO.crear(null);
        });

        assertEquals("El registro no puede ser nulo", exception.getMessage());
        verify(entityManager, never()).persist(any());
    }

    @Test
    void testCrear_NullEntityManager() throws Exception {
        // Crear una nueva instancia del DAO sin EntityManager inyectado
        TipoUnidadMedidaDAO daoConEmNulo = new TipoUnidadMedidaDAO();
        // No inyectar el EntityManager, dejarlo en null

        TipoUnidadMedida newTipo = new TipoUnidadMedida();
        newTipo.setId(20);
        newTipo.setNombre("Mililitro");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            daoConEmNulo.crear(newTipo);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Error al crear el registro"));
    }

    @Test
    void testCrear_Exception_OnPersist() {
        TipoUnidadMedida newTipo = new TipoUnidadMedida();
        newTipo.setId(20);
        newTipo.setNombre("Mililitro");

        doThrow(new RuntimeException("Database error"))
                .when(entityManager).persist(newTipo);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tipoUnidadMedidaDAO.crear(newTipo);
        });

        assertTrue(exception.getMessage().contains("Error al crear el registro"));
        verify(entityManager, times(1)).persist(newTipo);
    }

    @Test
    void testCrear_WithMinimalData() {
        TipoUnidadMedida newTipo = new TipoUnidadMedida();
        newTipo.setId(100);

        doNothing().when(entityManager).persist(newTipo);

        tipoUnidadMedidaDAO.crear(newTipo);

        assertEquals(100, newTipo.getId());
        verify(entityManager, times(1)).persist(newTipo);
    }

    @Test
    void testCrear_WithCompleteData() {
        TipoUnidadMedida newTipo = new TipoUnidadMedida();
        newTipo.setId(50);
        newTipo.setNombre("Miligramo");
        newTipo.setUnidadBase("mg");
        newTipo.setActivo(true);
        newTipo.setComentarios("Unidad de masa muy pequeña");

        doNothing().when(entityManager).persist(newTipo);

        tipoUnidadMedidaDAO.crear(newTipo);

        assertEquals(50, newTipo.getId());
        assertEquals("Miligramo", newTipo.getNombre());
        verify(entityManager, times(1)).persist(newTipo);
    }
}