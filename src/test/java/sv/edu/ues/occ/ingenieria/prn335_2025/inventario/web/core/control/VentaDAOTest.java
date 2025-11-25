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
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Cliente;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Venta;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Venta> typedQuery;

    @Mock
    private TypedQuery<Long> longTypedQuery;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<Venta> criteriaQuery;

    @Mock
    private CriteriaQuery<Long> criteriaQueryLong;

    @Mock
    private Root<Venta> root;

    @InjectMocks
    private VentaDAO dao;

    private Venta testVenta;
    private UUID testVentaId;
    private UUID testClienteId;
    private Cliente testCliente;

    @BeforeEach
    void setUp() throws Exception {
        // Inyectar el EntityManager usando reflexión
        Field emField = VentaDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(dao, entityManager);

        // Inicializar IDs de prueba
        testVentaId = UUID.randomUUID();
        testClienteId = UUID.randomUUID();

        // Crear cliente de prueba
        testCliente = new Cliente();
        testCliente.setId(testClienteId);
        testCliente.setNombre("Cliente Test");
        testCliente.setDui("12345678-9");

        // Crear venta de prueba
        testVenta = new Venta();
        testVenta.setId(testVentaId);
        testVenta.setIdCliente(testCliente);
        testVenta.setFecha(OffsetDateTime.now());
        testVenta.setEstado("PENDIENTE");
        testVenta.setObservaciones("Venta de prueba");
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
        Class<Venta> entityClass = dao.getEntityClass();

        // Assert
        assertNotNull(entityClass);
        assertEquals(Venta.class, entityClass);
    }

    @Test
    void testFindById_Success() {
        // Arrange
        when(entityManager.find(Venta.class, testVentaId))
                .thenReturn(testVenta);

        // Act
        Venta result = dao.findById(testVentaId);

        // Assert
        assertNotNull(result);
        assertEquals(testVentaId, result.getId());
        assertEquals("PENDIENTE", result.getEstado());
        assertEquals(testClienteId, result.getIdCliente().getId());
        verify(entityManager, times(1)).find(Venta.class, testVentaId);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(entityManager.find(Venta.class, testVentaId))
                .thenReturn(null);

        // Act
        Venta result = dao.findById(testVentaId);

        // Assert
        assertNull(result);
        verify(entityManager, times(1)).find(Venta.class, testVentaId);
    }

    @Test
    void testFindById_NullId() {
        // Act
        Venta result = dao.findById(null);

        // Assert
        assertNull(result);
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void testFindById_Exception() {
        // Arrange
        when(entityManager.find(Venta.class, testVentaId))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        Venta result = dao.findById(testVentaId);

        // Assert
        assertNull(result);
    }

    @Test
    void testFindRange_Success() {
        // Arrange
        List<Venta> expectedList = new ArrayList<>();
        expectedList.add(testVenta);

        Venta venta2 = new Venta();
        venta2.setId(UUID.randomUUID());
        venta2.setIdCliente(testCliente);
        venta2.setFecha(OffsetDateTime.now().minusDays(1));
        venta2.setEstado("COMPLETADA");
        expectedList.add(venta2);

        // Mock la query específica con JOIN FETCH que usa VentaDAO.findRange()
        when(entityManager.createQuery(
                "SELECT v FROM Venta v JOIN FETCH v.idCliente ORDER BY v.fecha DESC",
                Venta.class))
                .thenReturn(typedQuery);
        when(typedQuery.setFirstResult(anyInt()))
                .thenReturn(typedQuery);
        when(typedQuery.setMaxResults(anyInt()))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Venta> result = dao.findRange(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testVentaId, result.get(0).getId());
        verify(typedQuery, times(1)).setFirstResult(0);
        verify(typedQuery, times(1)).setMaxResults(10);
        verify(typedQuery, times(1)).getResultList();
    }

    @Test
    void testFindRange_EmptyList() {
        // Arrange
        when(entityManager.createQuery(
                "SELECT v FROM Venta v JOIN FETCH v.idCliente ORDER BY v.fecha DESC",
                Venta.class))
                .thenReturn(typedQuery);
        when(typedQuery.setFirstResult(anyInt()))
                .thenReturn(typedQuery);
        when(typedQuery.setMaxResults(anyInt()))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(new ArrayList<>());

        // Act
        List<Venta> result = dao.findRange(0, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindRange_WithPagination() {
        // Arrange
        List<Venta> expectedList = new ArrayList<>();
        expectedList.add(testVenta);

        when(entityManager.createQuery(
                "SELECT v FROM Venta v JOIN FETCH v.idCliente ORDER BY v.fecha DESC",
                Venta.class))
                .thenReturn(typedQuery);
        when(typedQuery.setFirstResult(anyInt()))
                .thenReturn(typedQuery);
        when(typedQuery.setMaxResults(anyInt()))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Venta> result = dao.findRange(10, 5);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(typedQuery, times(1)).setFirstResult(10);
        verify(typedQuery, times(1)).setMaxResults(5);
    }

    @Test
    void testFindRange_Exception() {
        // Arrange
        when(entityManager.createQuery(
                "SELECT v FROM Venta v JOIN FETCH v.idCliente ORDER BY v.fecha DESC",
                Venta.class))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        List<Venta> result = dao.findRange(0, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testBuscarPorCliente_Success() {
        // Arrange
        List<Venta> expectedList = new ArrayList<>();
        expectedList.add(testVenta);

        when(entityManager.createQuery(
                "SELECT v FROM Venta v LEFT JOIN FETCH v.idCliente WHERE v.idCliente.id = :idCliente",
                Venta.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idCliente"), any(UUID.class)))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Venta> result = dao.buscarPorCliente(testClienteId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testVentaId, result.get(0).getId());
        assertEquals(testClienteId, result.get(0).getIdCliente().getId());
        verify(typedQuery, times(1)).setParameter("idCliente", testClienteId);
    }

    @Test
    void testBuscarPorCliente_EmptyList() {
        // Arrange
        when(entityManager.createQuery(
                "SELECT v FROM Venta v LEFT JOIN FETCH v.idCliente WHERE v.idCliente.id = :idCliente",
                Venta.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idCliente"), any(UUID.class)))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(new ArrayList<>());

        // Act
        List<Venta> result = dao.buscarPorCliente(testClienteId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testBuscarPorCliente_MultipleVentas() {
        // Arrange
        List<Venta> expectedList = new ArrayList<>();
        expectedList.add(testVenta);

        Venta venta2 = new Venta();
        venta2.setId(UUID.randomUUID());
        venta2.setIdCliente(testCliente);
        venta2.setEstado("COMPLETADA");
        expectedList.add(venta2);

        when(entityManager.createQuery(
                "SELECT v FROM Venta v LEFT JOIN FETCH v.idCliente WHERE v.idCliente.id = :idCliente",
                Venta.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idCliente"), any(UUID.class)))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Venta> result = dao.buscarPorCliente(testClienteId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testClienteId, result.get(0).getIdCliente().getId());
        assertEquals(testClienteId, result.get(1).getIdCliente().getId());
    }

    @Test
    void testBuscarPorCliente_Exception() {
        // Arrange
        when(entityManager.createQuery(
                "SELECT v FROM Venta v LEFT JOIN FETCH v.idCliente WHERE v.idCliente.id = :idCliente",
                Venta.class))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        List<Venta> result = dao.buscarPorCliente(testClienteId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCount_Success() {
        // Arrange
        when(entityManager.createQuery("SELECT COUNT(v) FROM Venta v", Long.class))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(5L);

        // Act
        Long result = dao.count();

        // Assert
        assertNotNull(result);
        assertEquals(5L, result);
        verify(entityManager, times(1)).createQuery("SELECT COUNT(v) FROM Venta v", Long.class);
        verify(longTypedQuery, times(1)).getSingleResult();
    }

    @Test
    void testCount_Zero() {
        // Arrange
        when(entityManager.createQuery("SELECT COUNT(v) FROM Venta v", Long.class))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(0L);

        // Act
        Long result = dao.count();

        // Assert
        assertNotNull(result);
        assertEquals(0L, result);
    }

    @Test
    void testCount_Exception() {
        // Arrange
        when(entityManager.createQuery("SELECT COUNT(v) FROM Venta v", Long.class))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        Long result = dao.count();

        // Assert
        assertNotNull(result);
        assertEquals(0L, result);
    }

    @Test
    void testCrear_Success() {
        // Arrange
        doNothing().when(entityManager).persist(any(Venta.class));

        // Act
        dao.crear(testVenta);

        // Assert
        verify(entityManager, times(1)).persist(testVenta);
    }

    @Test
    void testModificar_Success() {
        // Arrange
        when(entityManager.merge(any(Venta.class)))
                .thenReturn(testVenta);

        // Act
        Venta result = dao.modificar(testVenta);

        // Assert
        assertNotNull(result);
        assertEquals(testVenta.getId(), result.getId());
        verify(entityManager, times(1)).merge(testVenta);
    }

    @Test
    void testEliminar_Success() {
        // Arrange
        when(entityManager.contains(testVenta)).thenReturn(false);
        when(entityManager.merge(testVenta)).thenReturn(testVenta);
        doNothing().when(entityManager).remove(any(Venta.class));

        // Act
        dao.eliminar(testVenta);

        // Assert
        verify(entityManager, times(1)).merge(testVenta);
        verify(entityManager, times(1)).remove(any(Venta.class));
    }

    @Test
    void testEliminar_EntityAttached() {
        // Arrange
        when(entityManager.contains(testVenta)).thenReturn(true);
        doNothing().when(entityManager).remove(any(Venta.class));

        // Act
        dao.eliminar(testVenta);

        // Assert
        verify(entityManager, never()).merge(any());
        verify(entityManager, times(1)).remove(testVenta);
    }

    @Test
    void testFindAll() {
        // Arrange
        List<Venta> expectedList = new ArrayList<>();
        expectedList.add(testVenta);

        // Mock completo de Criteria API que usa el método findAll() de la clase padre
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Venta.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Venta.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Venta> result = dao.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(entityManager, times(1)).getCriteriaBuilder();
        verify(criteriaBuilder, times(1)).createQuery(Venta.class);
        verify(criteriaQuery, times(1)).from(Venta.class);
        verify(criteriaQuery, times(1)).select(root);
        verify(entityManager, times(1)).createQuery(criteriaQuery);
        verify(typedQuery, times(1)).getResultList();
    }
}