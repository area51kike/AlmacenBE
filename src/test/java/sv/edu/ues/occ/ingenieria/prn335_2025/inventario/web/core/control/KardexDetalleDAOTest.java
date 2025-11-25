package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.KardexDetalle;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KardexDetalleDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<KardexDetalle> criteriaQuery;

    @Mock
    private CriteriaQuery<Long> criteriaQueryLong;

    @Mock
    private Root<KardexDetalle> root;

    @Mock
    private TypedQuery<KardexDetalle> typedQuery;

    @Mock
    private TypedQuery<Long> typedQueryLong;

    @Mock
    private Path<Object> path;

    @Mock
    private Order order;

    private KardexDetalleDAO kardexDetalleDAO;
    private KardexDetalle kardexDetalle;
    private UUID testId;

    @BeforeEach
    void setUp() throws Exception {
        // Crear instancia real del DAO
        kardexDetalleDAO = new KardexDetalleDAO();

        // Inyectar el EntityManager mock usando reflexión
        Field emField = KardexDetalleDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(kardexDetalleDAO, entityManager);

        // Preparar datos de prueba
        testId = UUID.randomUUID();
        kardexDetalle = new KardexDetalle();
        kardexDetalle.setId(testId);
        kardexDetalle.setLote("LOTE-001");
        kardexDetalle.setActivo(true);
    }

    @Test
    void testGetEntityClass() {
        // Act
        Class<KardexDetalle> result = kardexDetalleDAO.getEntityClass();

        // Assert
        assertNotNull(result);
        assertEquals(KardexDetalle.class, result);
    }

    @Test
    void testGetEntityManager() {
        // Act
        EntityManager result = kardexDetalleDAO.getEntityManager();

        // Assert
        assertNotNull(result);
        assertEquals(entityManager, result);
    }

    @Test
    void testFind_ConIdValido_DeberiaRetornarEntidad() {
        // Arrange
        when(entityManager.find(KardexDetalle.class, testId)).thenReturn(kardexDetalle);

        // Act
        KardexDetalle result = kardexDetalleDAO.find(testId);

        // Assert
        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("LOTE-001", result.getLote());
        verify(entityManager, times(1)).find(KardexDetalle.class, testId);
    }

    @Test
    void testFind_ConIdNulo_DeberiaLanzarExcepcion() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> kardexDetalleDAO.find(null)
        );

        assertEquals("El ID no puede ser nulo", exception.getMessage());
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void testFindById_DeberiaRetornarEntidad() {
        // Arrange
        when(entityManager.find(KardexDetalle.class, testId)).thenReturn(kardexDetalle);

        // Act
        KardexDetalle result = kardexDetalleDAO.findById(testId);

        // Assert
        assertNotNull(result);
        assertEquals(testId, result.getId());
        verify(entityManager, times(1)).find(KardexDetalle.class, testId);
    }

    @Test
    void testFindAll_DeberiaRetornarListaDeEntidades() {
        // Arrange
        KardexDetalle detalle2 = new KardexDetalle();
        detalle2.setId(UUID.randomUUID());
        detalle2.setLote("LOTE-002");

        List<KardexDetalle> expectedList = Arrays.asList(kardexDetalle, detalle2);

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(KardexDetalle.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(KardexDetalle.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<KardexDetalle> result = kardexDetalleDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("LOTE-001", result.get(0).getLote());
        assertEquals("LOTE-002", result.get(1).getLote());
        verify(entityManager, times(1)).getCriteriaBuilder();
    }

    @Test
    void testFindRange_ConParametrosValidos_DeberiaRetornarRango() {
        // Arrange
        List<KardexDetalle> expectedList = Arrays.asList(kardexDetalle);

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(KardexDetalle.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(KardexDetalle.class)).thenReturn(root);
        when(root.get("id")).thenReturn(path);
        when(criteriaBuilder.asc(path)).thenReturn(order);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(criteriaQuery.orderBy(order)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(0)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(10)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<KardexDetalle> result = kardexDetalleDAO.findRange(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(typedQuery, times(1)).setFirstResult(0);
        verify(typedQuery, times(1)).setMaxResults(10);
    }

    @Test
    void testFindRange_ConParametrosInvalidos_DeberiaLanzarExcepcion() {
        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> kardexDetalleDAO.findRange(-1, 10)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> kardexDetalleDAO.findRange(0, 0)
        );
    }

    @Test
    void testCount_DeberiaRetornarCantidad() {
        // Arrange
        Long expectedCount = 5L;
        Expression<Long> countExpression = mock(Expression.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(criteriaQueryLong);
        when(criteriaQueryLong.from(KardexDetalle.class)).thenReturn(root);
        when(criteriaBuilder.count(root)).thenReturn(countExpression);
        when(criteriaQueryLong.select(countExpression)).thenReturn(criteriaQueryLong);
        when(entityManager.createQuery(criteriaQueryLong)).thenReturn(typedQueryLong);
        when(typedQueryLong.getSingleResult()).thenReturn(expectedCount);

        // Act
        Long result = kardexDetalleDAO.count();

        // Assert
        assertNotNull(result);
        assertEquals(5L, result);
        verify(entityManager, times(1)).getCriteriaBuilder();
    }

    @Test
    void testCrear_ConEntidadValida_DeberiaCrearRegistro() {
        // Arrange
        doNothing().when(entityManager).persist(kardexDetalle);

        // Act
        assertDoesNotThrow(() -> kardexDetalleDAO.crear(kardexDetalle));

        // Assert
        verify(entityManager, times(1)).persist(kardexDetalle);
    }

    @Test
    void testCrear_ConEntidadNula_DeberiaLanzarExcepcion() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> kardexDetalleDAO.crear(null)
        );

        assertEquals("El registro no puede ser nulo", exception.getMessage());
        verify(entityManager, never()).persist(any());
    }

    @Test
    void testModificar_ConEntidadValida_DeberiaRetornarEntidadModificada() {
        // Arrange
        kardexDetalle.setLote("LOTE-MODIFICADO");
        when(entityManager.merge(kardexDetalle)).thenReturn(kardexDetalle);

        // Act
        KardexDetalle result = kardexDetalleDAO.modificar(kardexDetalle);

        // Assert
        assertNotNull(result);
        assertEquals("LOTE-MODIFICADO", result.getLote());
        verify(entityManager, times(1)).merge(kardexDetalle);
    }

    @Test
    void testModificar_ConEntidadNula_DeberiaLanzarExcepcion() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> kardexDetalleDAO.modificar(null)
        );

        assertEquals("El registro no puede ser nulo", exception.getMessage());
        verify(entityManager, never()).merge(any());
    }

    @Test
    void testEliminar_ConEntidadManejada_DeberiaEliminar() {
        // Arrange
        when(entityManager.contains(kardexDetalle)).thenReturn(true);
        doNothing().when(entityManager).remove(kardexDetalle);

        // Act
        assertDoesNotThrow(() -> kardexDetalleDAO.eliminar(kardexDetalle));

        // Assert
        verify(entityManager, times(1)).contains(kardexDetalle);
        verify(entityManager, times(1)).remove(kardexDetalle);
        verify(entityManager, never()).merge(any());
    }

    @Test
    void testEliminar_ConEntidadNoManejada_DeberiaMergearYEliminar() {
        // Arrange
        when(entityManager.contains(kardexDetalle)).thenReturn(false);
        when(entityManager.merge(kardexDetalle)).thenReturn(kardexDetalle);
        doNothing().when(entityManager).remove(kardexDetalle);

        // Act
        assertDoesNotThrow(() -> kardexDetalleDAO.eliminar(kardexDetalle));

        // Assert
        verify(entityManager, times(1)).contains(kardexDetalle);
        verify(entityManager, times(1)).merge(kardexDetalle);
        verify(entityManager, times(1)).remove(kardexDetalle);
    }

    @Test
    void testEliminar_ConEntidadNula_DeberiaLanzarExcepcion() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> kardexDetalleDAO.eliminar(null)
        );

        assertEquals("La entidad no puede ser nula", exception.getMessage());
        verify(entityManager, never()).remove(any());
    }

    @Test
    void testEliminarPorId_ConIdValido_DeberiaEliminar() {
        // Arrange
        when(entityManager.find(KardexDetalle.class, testId)).thenReturn(kardexDetalle);
        doNothing().when(entityManager).remove(kardexDetalle);

        // Act
        assertDoesNotThrow(() -> kardexDetalleDAO.eliminarPorId(testId));

        // Assert
        verify(entityManager, times(1)).find(KardexDetalle.class, testId);
        verify(entityManager, times(1)).remove(kardexDetalle);
    }

    @Test
    void testEliminarPorId_ConIdInexistente_DeberiaLanzarExcepcion() {
        // Arrange
        when(entityManager.find(KardexDetalle.class, testId)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> kardexDetalleDAO.eliminarPorId(testId)
        );

        assertTrue(exception.getMessage().contains("Error al eliminar el registro por ID"));
        verify(entityManager, times(1)).find(KardexDetalle.class, testId);
        verify(entityManager, never()).remove(any());
    }

    @Test
    void testEliminarPorId_ConIdNulo_DeberiaLanzarExcepcion() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> kardexDetalleDAO.eliminarPorId(null)
        );

        assertEquals("El ID no puede ser nulo", exception.getMessage());
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void testFindAll_CuandoNoHayRegistros_DeberiaRetornarListaVacia() {
        // Arrange
        List<KardexDetalle> expectedList = Arrays.asList();

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(KardexDetalle.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(KardexDetalle.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<KardexDetalle> result = kardexDetalleDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testCount_CuandoNoHayRegistros_DeberiaRetornarCero() {
        // Arrange
        Long expectedCount = 0L;
        Expression<Long> countExpression = mock(Expression.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(criteriaQueryLong);
        when(criteriaQueryLong.from(KardexDetalle.class)).thenReturn(root);
        when(criteriaBuilder.count(root)).thenReturn(countExpression);
        when(criteriaQueryLong.select(countExpression)).thenReturn(criteriaQueryLong);
        when(entityManager.createQuery(criteriaQueryLong)).thenReturn(typedQueryLong);
        when(typedQueryLong.getSingleResult()).thenReturn(expectedCount);

        // Act
        Long result = kardexDetalleDAO.count();

        // Assert
        assertNotNull(result);
        assertEquals(0L, result);
    }
}