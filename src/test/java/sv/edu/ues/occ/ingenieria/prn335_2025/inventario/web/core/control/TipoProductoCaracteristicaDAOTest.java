package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProductoCaracteristica;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoProductoCaracteristicaDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<TipoProductoCaracteristica> typedQueryTipoProductoCaracteristica;

    @Mock
    private TypedQuery<Long> typedQueryLong;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<TipoProductoCaracteristica> criteriaQuery;

    @Mock
    private CriteriaQuery<Long> criteriaQueryLong;

    @Mock
    private Root<TipoProductoCaracteristica> root;

    @InjectMocks
    private TipoProductoCaracteristicaDAO dao;

    private TipoProductoCaracteristica testTipoProductoCaracteristica;
    private Long testId;
    private Long testTipoProductoId;

    @BeforeEach
    void setUp() throws Exception {
        // Inyectar el EntityManager usando reflexión
        Field emField = TipoProductoCaracteristicaDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(dao, entityManager);

        // Inicializar IDs de prueba
        testId = 1L;
        testTipoProductoId = 10L;

        // Crear TipoProductoCaracteristica de prueba SIN relaciones
        // Las relaciones se establecerán en los mocks cuando sea necesario
        testTipoProductoCaracteristica = new TipoProductoCaracteristica();
        testTipoProductoCaracteristica.setId(testId);
        testTipoProductoCaracteristica.setObligatorio(true);
        testTipoProductoCaracteristica.setFechaCreacion(OffsetDateTime.now());
    }

    @Test
    void testGetEntityManager() {
        // Act
        EntityManager em = dao.getEntityManager();

        // Assert
        assertNotNull(em);
        assertEquals(entityManager, em);
    }

    @Test
    void testGetEntityClass() {
        // Act
        Class<TipoProductoCaracteristica> entityClass = dao.getEntityClass();

        // Assert
        assertNotNull(entityClass);
        assertEquals(TipoProductoCaracteristica.class, entityClass);
    }

    @Test
    void testFind_Success() {
        // Arrange
        when(entityManager.find(TipoProductoCaracteristica.class, testId))
                .thenReturn(testTipoProductoCaracteristica);

        // Act
        TipoProductoCaracteristica result = dao.find(testId);

        // Assert
        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertTrue(result.getObligatorio());
        verify(entityManager, times(1)).find(TipoProductoCaracteristica.class, testId);
    }

    @Test
    void testFind_NotFound() {
        // Arrange
        when(entityManager.find(TipoProductoCaracteristica.class, testId))
                .thenReturn(null);

        // Act
        TipoProductoCaracteristica result = dao.find(testId);

        // Assert
        assertNull(result);
        verify(entityManager, times(1)).find(TipoProductoCaracteristica.class, testId);
    }

    @Test
    void testFind_NullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> dao.find(null));
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void testFindById_Success() {
        // Arrange
        when(entityManager.find(TipoProductoCaracteristica.class, testId))
                .thenReturn(testTipoProductoCaracteristica);

        // Act
        TipoProductoCaracteristica result = dao.findById(testId);

        // Assert
        assertNotNull(result);
        assertEquals(testId, result.getId());
        // No verificamos las relaciones ya que no las establecimos
        verify(entityManager, times(1)).find(TipoProductoCaracteristica.class, testId);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(entityManager.find(TipoProductoCaracteristica.class, testId))
                .thenReturn(null);

        // Act
        TipoProductoCaracteristica result = dao.findById(testId);

        // Assert
        assertNull(result);
    }

    @Test
    void testFindById_NullId() {
        // Act
        TipoProductoCaracteristica result = dao.findById(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testFindAll_Success() {
        // Arrange
        List<TipoProductoCaracteristica> expectedList = new ArrayList<>();
        expectedList.add(testTipoProductoCaracteristica);

        TipoProductoCaracteristica tpc2 = new TipoProductoCaracteristica();
        tpc2.setId(2L);
        tpc2.setObligatorio(false);
        expectedList.add(tpc2);

        // Mock completo de Criteria API
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(TipoProductoCaracteristica.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(TipoProductoCaracteristica.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQueryTipoProductoCaracteristica);
        when(typedQueryTipoProductoCaracteristica.getResultList()).thenReturn(expectedList);

        // Act
        List<TipoProductoCaracteristica> result = dao.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testId, result.get(0).getId());
        verify(entityManager, times(1)).getCriteriaBuilder();
    }

    @Test
    void testFindAll_EmptyList() {
        // Arrange
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(TipoProductoCaracteristica.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(TipoProductoCaracteristica.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQueryTipoProductoCaracteristica);
        when(typedQueryTipoProductoCaracteristica.getResultList()).thenReturn(new ArrayList<>());

        // Act
        List<TipoProductoCaracteristica> result = dao.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindRange_Success() {
        // Arrange
        List<TipoProductoCaracteristica> expectedList = new ArrayList<>();
        expectedList.add(testTipoProductoCaracteristica);

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(TipoProductoCaracteristica.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(TipoProductoCaracteristica.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(root.get("id")).thenReturn(mock(jakarta.persistence.criteria.Path.class));
        when(criteriaBuilder.asc(any())).thenReturn(mock(jakarta.persistence.criteria.Order.class));
        when(criteriaQuery.orderBy(any(jakarta.persistence.criteria.Order.class))).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQueryTipoProductoCaracteristica);
        when(typedQueryTipoProductoCaracteristica.setFirstResult(anyInt())).thenReturn(typedQueryTipoProductoCaracteristica);
        when(typedQueryTipoProductoCaracteristica.setMaxResults(anyInt())).thenReturn(typedQueryTipoProductoCaracteristica);
        when(typedQueryTipoProductoCaracteristica.getResultList()).thenReturn(expectedList);

        // Act
        List<TipoProductoCaracteristica> result = dao.findRange(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(typedQueryTipoProductoCaracteristica, times(1)).setFirstResult(0);
        verify(typedQueryTipoProductoCaracteristica, times(1)).setMaxResults(10);
    }

    @Test
    void testFindRange_WithPagination() {
        // Arrange
        List<TipoProductoCaracteristica> expectedList = new ArrayList<>();
        expectedList.add(testTipoProductoCaracteristica);

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(TipoProductoCaracteristica.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(TipoProductoCaracteristica.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(root.get("id")).thenReturn(mock(jakarta.persistence.criteria.Path.class));
        when(criteriaBuilder.asc(any())).thenReturn(mock(jakarta.persistence.criteria.Order.class));
        when(criteriaQuery.orderBy(any(jakarta.persistence.criteria.Order.class))).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQueryTipoProductoCaracteristica);
        when(typedQueryTipoProductoCaracteristica.setFirstResult(anyInt())).thenReturn(typedQueryTipoProductoCaracteristica);
        when(typedQueryTipoProductoCaracteristica.setMaxResults(anyInt())).thenReturn(typedQueryTipoProductoCaracteristica);
        when(typedQueryTipoProductoCaracteristica.getResultList()).thenReturn(expectedList);

        // Act
        List<TipoProductoCaracteristica> result = dao.findRange(5, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(typedQueryTipoProductoCaracteristica, times(1)).setFirstResult(5);
        verify(typedQueryTipoProductoCaracteristica, times(1)).setMaxResults(10);
    }

    @Test
    void testFindRange_InvalidParameters() {
        // Act & Assert - first negativo
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(-1, 10));

        // Act & Assert - pageSize cero
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(0, 0));

        // Act & Assert - pageSize negativo
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(0, -5));
    }

    @Test
    void testCount_Success() {
        // Arrange
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(criteriaQueryLong);
        when(criteriaQueryLong.from(TipoProductoCaracteristica.class)).thenReturn(root);
        when(criteriaBuilder.count(root)).thenReturn(mock(jakarta.persistence.criteria.Expression.class));
        when(criteriaQueryLong.select(any())).thenReturn(criteriaQueryLong);
        when(entityManager.createQuery(criteriaQueryLong)).thenReturn(typedQueryLong);
        when(typedQueryLong.getSingleResult()).thenReturn(15L);

        // Act
        Long result = dao.count();

        // Assert
        assertNotNull(result);
        assertEquals(15L, result);
        verify(entityManager, times(1)).getCriteriaBuilder();
    }

    @Test
    void testCount_Zero() {
        // Arrange
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(criteriaQueryLong);
        when(criteriaQueryLong.from(TipoProductoCaracteristica.class)).thenReturn(root);
        when(criteriaBuilder.count(root)).thenReturn(mock(jakarta.persistence.criteria.Expression.class));
        when(criteriaQueryLong.select(any())).thenReturn(criteriaQueryLong);
        when(entityManager.createQuery(criteriaQueryLong)).thenReturn(typedQueryLong);
        when(typedQueryLong.getSingleResult()).thenReturn(0L);

        // Act
        Long result = dao.count();

        // Assert
        assertNotNull(result);
        assertEquals(0L, result);
    }

    @Test
    void testCrear_Success() {
        // Arrange
        doNothing().when(entityManager).persist(any(TipoProductoCaracteristica.class));

        // Act
        dao.crear(testTipoProductoCaracteristica);

        // Assert
        verify(entityManager, times(1)).persist(testTipoProductoCaracteristica);
    }

    @Test
    void testCrear_NullEntity() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> dao.crear(null));
        verify(entityManager, never()).persist(any());
    }

    @Test
    void testModificar_Success() {
        // Arrange
        when(entityManager.merge(any(TipoProductoCaracteristica.class)))
                .thenReturn(testTipoProductoCaracteristica);

        // Act
        TipoProductoCaracteristica result = dao.modificar(testTipoProductoCaracteristica);

        // Assert
        assertNotNull(result);
        assertEquals(testTipoProductoCaracteristica.getId(), result.getId());
        verify(entityManager, times(1)).merge(testTipoProductoCaracteristica);
    }

    @Test
    void testModificar_NullEntity() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> dao.modificar(null));
        verify(entityManager, never()).merge(any());
    }

    @Test
    void testEliminar_Success() {
        // Arrange
        when(entityManager.contains(testTipoProductoCaracteristica)).thenReturn(false);
        when(entityManager.merge(testTipoProductoCaracteristica)).thenReturn(testTipoProductoCaracteristica);
        doNothing().when(entityManager).remove(any(TipoProductoCaracteristica.class));

        // Act
        dao.eliminar(testTipoProductoCaracteristica);

        // Assert
        verify(entityManager, times(1)).merge(testTipoProductoCaracteristica);
        verify(entityManager, times(1)).remove(any(TipoProductoCaracteristica.class));
    }

    @Test
    void testEliminar_EntityAttached() {
        // Arrange
        when(entityManager.contains(testTipoProductoCaracteristica)).thenReturn(true);
        doNothing().when(entityManager).remove(any(TipoProductoCaracteristica.class));

        // Act
        dao.eliminar(testTipoProductoCaracteristica);

        // Assert
        verify(entityManager, never()).merge(any());
        verify(entityManager, times(1)).remove(testTipoProductoCaracteristica);
    }

    @Test
    void testEliminar_NullEntity() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> dao.eliminar(null));
        verify(entityManager, never()).remove(any());
    }

    @Test
    void testEliminarPorId_Success() {
        // Arrange
        when(entityManager.find(TipoProductoCaracteristica.class, testId))
                .thenReturn(testTipoProductoCaracteristica);
        doNothing().when(entityManager).remove(any(TipoProductoCaracteristica.class));

        // Act
        dao.eliminarPorId(testId);

        // Assert
        verify(entityManager, times(1)).find(TipoProductoCaracteristica.class, testId);
        verify(entityManager, times(1)).remove(testTipoProductoCaracteristica);
    }

    @Test
    void testEliminarPorId_NotFound() {
        // Arrange
        when(entityManager.find(TipoProductoCaracteristica.class, testId))
                .thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> dao.eliminarPorId(testId));
        verify(entityManager, never()).remove(any());
    }

    @Test
    void testEliminarPorId_NullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> dao.eliminarPorId(null));
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void testFindByIdTipoProducto_Success() {
        // Arrange
        List<TipoProductoCaracteristica> expectedList = new ArrayList<>();
        expectedList.add(testTipoProductoCaracteristica);

        TipoProductoCaracteristica tpc2 = new TipoProductoCaracteristica();
        tpc2.setId(2L);
        tpc2.setObligatorio(false);
        expectedList.add(tpc2);

        when(entityManager.createQuery(
                "SELECT tpc FROM TipoProductoCaracteristica tpc WHERE tpc.tipoProducto.id = :idTipoProducto",
                TipoProductoCaracteristica.class))
                .thenReturn(typedQueryTipoProductoCaracteristica);
        when(typedQueryTipoProductoCaracteristica.setParameter(eq("idTipoProducto"), any(Long.class)))
                .thenReturn(typedQueryTipoProductoCaracteristica);
        when(typedQueryTipoProductoCaracteristica.getResultList()).thenReturn(expectedList);

        // Act
        List<TipoProductoCaracteristica> result = dao.findByIdTipoProducto(testTipoProductoId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testId, result.get(0).getId());
        // No verificamos las relaciones ya que no las establecimos
        verify(typedQueryTipoProductoCaracteristica, times(1)).setParameter("idTipoProducto", testTipoProductoId);
    }

    @Test
    void testFindByIdTipoProducto_EmptyList() {
        // Arrange
        when(entityManager.createQuery(
                "SELECT tpc FROM TipoProductoCaracteristica tpc WHERE tpc.tipoProducto.id = :idTipoProducto",
                TipoProductoCaracteristica.class))
                .thenReturn(typedQueryTipoProductoCaracteristica);
        when(typedQueryTipoProductoCaracteristica.setParameter(eq("idTipoProducto"), any(Long.class)))
                .thenReturn(typedQueryTipoProductoCaracteristica);
        when(typedQueryTipoProductoCaracteristica.getResultList()).thenReturn(new ArrayList<>());

        // Act
        List<TipoProductoCaracteristica> result = dao.findByIdTipoProducto(testTipoProductoId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByIdTipoProducto_NullId() {
        // Arrange
        when(entityManager.createQuery(
                "SELECT tpc FROM TipoProductoCaracteristica tpc WHERE tpc.tipoProducto.id = :idTipoProducto",
                TipoProductoCaracteristica.class))
                .thenReturn(typedQueryTipoProductoCaracteristica);
        when(typedQueryTipoProductoCaracteristica.setParameter(eq("idTipoProducto"), any()))
                .thenReturn(typedQueryTipoProductoCaracteristica);
        when(typedQueryTipoProductoCaracteristica.getResultList()).thenReturn(new ArrayList<>());

        // Act
        List<TipoProductoCaracteristica> result = dao.findByIdTipoProducto(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testObtenerMaximoId_Success() {
        // Arrange
        when(entityManager.createQuery(
                "SELECT COALESCE(MAX(t.id), 0) FROM TipoProductoCaracteristica t",
                Long.class))
                .thenReturn(typedQueryLong);
        when(typedQueryLong.getSingleResult()).thenReturn(100L);

        // Act
        Long result = dao.obtenerMaximoId();

        // Assert
        assertNotNull(result);
        assertEquals(100L, result);
        verify(entityManager, times(1)).createQuery(
                "SELECT COALESCE(MAX(t.id), 0) FROM TipoProductoCaracteristica t",
                Long.class);
    }

    @Test
    void testObtenerMaximoId_NoRecords() {
        // Arrange
        when(entityManager.createQuery(
                "SELECT COALESCE(MAX(t.id), 0) FROM TipoProductoCaracteristica t",
                Long.class))
                .thenReturn(typedQueryLong);
        when(typedQueryLong.getSingleResult()).thenReturn(0L);

        // Act
        Long result = dao.obtenerMaximoId();

        // Assert
        assertNotNull(result);
        assertEquals(0L, result);
    }

    @Test
    void testObtenerMaximoId_Exception() {
        // Arrange
        when(entityManager.createQuery(
                "SELECT COALESCE(MAX(t.id), 0) FROM TipoProductoCaracteristica t",
                Long.class))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        Long result = dao.obtenerMaximoId();

        // Assert
        assertNotNull(result);
        assertEquals(0L, result);
    }

    @Test
    void testObtenerMaximoId_LargeNumber() {
        // Arrange
        when(entityManager.createQuery(
                "SELECT COALESCE(MAX(t.id), 0) FROM TipoProductoCaracteristica t",
                Long.class))
                .thenReturn(typedQueryLong);
        when(typedQueryLong.getSingleResult()).thenReturn(999999L);

        // Act
        Long result = dao.obtenerMaximoId();

        // Assert
        assertNotNull(result);
        assertEquals(999999L, result);
    }
}