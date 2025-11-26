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
import java.util.logging.Logger;

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

    // Tests para métodos específicos de VentaDAO

    @Test
    void testActualizarEstado_Success() {
        // Arrange
        String nuevoEstado = "COMPLETADA";
        when(entityManager.find(Venta.class, testVentaId)).thenReturn(testVenta);
        when(entityManager.merge(testVenta)).thenReturn(testVenta);

        // Act
        dao.actualizarEstado(testVentaId, nuevoEstado);

        // Assert
        assertEquals(nuevoEstado, testVenta.getEstado());
        verify(entityManager, times(1)).find(Venta.class, testVentaId);
        verify(entityManager, times(1)).merge(testVenta);
    }

    @Test
    void testActualizarEstado_VentaNoEncontrada() {
        // Arrange
        String nuevoEstado = "COMPLETADA";
        when(entityManager.find(Venta.class, testVentaId)).thenReturn(null);

        // Act
        dao.actualizarEstado(testVentaId, nuevoEstado);

        // Assert
        verify(entityManager, times(1)).find(Venta.class, testVentaId);
        verify(entityManager, never()).merge(any(Venta.class));
    }

    @Test
    void testActualizarEstado_Exception() {
        // Arrange
        String nuevoEstado = "COMPLETADA";
        when(entityManager.find(Venta.class, testVentaId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dao.actualizarEstado(testVentaId, nuevoEstado);
        });

        assertEquals("No se pudo actualizar el estado de la venta", exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
        verify(entityManager, times(1)).find(Venta.class, testVentaId);
    }

    @Test
    void testFindByEstado_Success() {
        // Arrange
        List<Venta> expectedList = new ArrayList<>();
        expectedList.add(testVenta);

        Venta venta2 = new Venta();
        venta2.setId(UUID.randomUUID());
        venta2.setEstado("PENDIENTE");
        expectedList.add(venta2);

        when(entityManager.createQuery("SELECT c FROM Venta c WHERE c.estado = :estado", Venta.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("estado", "PENDIENTE")).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Venta> result = dao.findByEstado("PENDIENTE");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("PENDIENTE", result.get(0).getEstado());
        assertEquals("PENDIENTE", result.get(1).getEstado());
        verify(entityManager).createQuery("SELECT c FROM Venta c WHERE c.estado = :estado", Venta.class);
        verify(typedQuery).setParameter("estado", "PENDIENTE");
        verify(typedQuery).getResultList();
    }

    @Test
    void testFindByEstado_EmptyList() {
        // Arrange
        when(entityManager.createQuery("SELECT c FROM Venta c WHERE c.estado = :estado", Venta.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("estado", "COMPLETADA")).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(new ArrayList<>());

        // Act
        List<Venta> result = dao.findByEstado("COMPLETADA");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(typedQuery).setParameter("estado", "COMPLETADA");
    }

    @Test
    void testFindByEstado_DifferentStates() {
        // Arrange
        List<Venta> expectedList = new ArrayList<>();
        expectedList.add(testVenta);

        when(entityManager.createQuery("SELECT c FROM Venta c WHERE c.estado = :estado", Venta.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("estado", "CANCELADA")).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Venta> result = dao.findByEstado("CANCELADA");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(typedQuery).setParameter("estado", "CANCELADA");
    }

    @Test
    void testFindByEstado_Exception() {
        // Arrange
        when(entityManager.createQuery("SELECT c FROM Venta c WHERE c.estado = :estado", Venta.class))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert - El método propaga la excepción
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dao.findByEstado("PENDIENTE");
        });
        assertEquals("Database error", exception.getMessage());
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
    void testFindRange_WithNegativeFirst_NoValidationInVentaDAO() {
        // VentaDAO.findRange() NO valida parámetros, delega en el padre después del try-catch
        // Arrange
        List<Venta> expectedList = new ArrayList<>();
        when(entityManager.createQuery(
                "SELECT v FROM Venta v JOIN FETCH v.idCliente ORDER BY v.fecha DESC",
                Venta.class))
                .thenReturn(typedQuery);
        when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        // Act - No debería lanzar excepción
        assertDoesNotThrow(() -> {
            List<Venta> result = dao.findRange(-1, 10);
            assertNotNull(result);
        });
    }

    @Test
    void testFindRange_WithZeroPageSize_NoValidationInVentaDAO() {
        // VentaDAO.findRange() NO valida parámetros
        // Arrange
        List<Venta> expectedList = new ArrayList<>();
        when(entityManager.createQuery(
                "SELECT v FROM Venta v JOIN FETCH v.idCliente ORDER BY v.fecha DESC",
                Venta.class))
                .thenReturn(typedQuery);
        when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        // Act - No debería lanzar excepción
        assertDoesNotThrow(() -> {
            List<Venta> result = dao.findRange(0, 0);
            assertNotNull(result);
        });
    }

    @Test
    void testFindRange_WithNegativePageSize_NoValidationInVentaDAO() {
        // VentaDAO.findRange() NO valida parámetros
        // Arrange
        List<Venta> expectedList = new ArrayList<>();
        when(entityManager.createQuery(
                "SELECT v FROM Venta v JOIN FETCH v.idCliente ORDER BY v.fecha DESC",
                Venta.class))
                .thenReturn(typedQuery);
        when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        // Act - No debería lanzar excepción
        assertDoesNotThrow(() -> {
            List<Venta> result = dao.findRange(0, -5);
            assertNotNull(result);
        });
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
    void testBuscarPorCliente_WithNullClienteId_ReturnsEmptyList() {
        // Arrange
        when(entityManager.createQuery(
                "SELECT v FROM Venta v LEFT JOIN FETCH v.idCliente WHERE v.idCliente.id = :idCliente",
                Venta.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idCliente"), isNull()))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(new ArrayList<>());

        // Act
        List<Venta> result = dao.buscarPorCliente(null);

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
    void testCount_Exception_ReturnsZero() {
        // Arrange
        when(entityManager.createQuery("SELECT COUNT(v) FROM Venta v", Long.class))
                .thenThrow(new RuntimeException("Database error"));

        // Act - VentaDAO.count() devuelve 0L en caso de excepción
        Long result = dao.count();

        // Assert
        assertEquals(0L, result);
    }

    // Tests para métodos heredados

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
    void testCrear_Success() {
        // Arrange
        doNothing().when(entityManager).persist(any(Venta.class));

        // Act
        dao.crear(testVenta);

        // Assert
        verify(entityManager, times(1)).persist(testVenta);
    }

    @Test
    void testCrear_NullEntity() {
        // Act & Assert - Debería lanzar IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            dao.crear(null);
        });
    }

    @Test
    void testCrear_EntityManagerNulo_LanzaRuntimeException() throws Exception {
        // Arrange
        VentaDAO daoWithNullEm = new VentaDAO();
        Field emField = VentaDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(daoWithNullEm, null);

        // Act & Assert - CORREGIDO: Cambiar a RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            daoWithNullEm.crear(testVenta);
        });
        assertEquals("Error al crear el registro", exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("EntityManager no disponible", exception.getCause().getMessage());
    }

    @Test
    void testCrear_Exception() {
        // Arrange
        doThrow(new RuntimeException("Persist error")).when(entityManager).persist(any(Venta.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dao.crear(testVenta);
        });
        assertEquals("Error al crear el registro", exception.getMessage());
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
    void testModificar_NullEntity() {
        // Act & Assert - Debería lanzar IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            dao.modificar(null);
        });
    }

    @Test
    void testModificar_EntityManagerNulo_LanzaRuntimeException() throws Exception {
        // Arrange
        VentaDAO daoWithNullEm = new VentaDAO();
        Field emField = VentaDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(daoWithNullEm, null);

        // Act & Assert - CORREGIDO: Cambiar a RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            daoWithNullEm.modificar(testVenta);
        });
        assertEquals("Error al modificar el registro", exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("EntityManager no disponible", exception.getCause().getMessage());
    }

    @Test
    void testModificar_Exception() {
        // Arrange
        when(entityManager.merge(any(Venta.class))).thenThrow(new RuntimeException("Merge error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dao.modificar(testVenta);
        });
        assertEquals("Error al modificar el registro", exception.getMessage());
    }

    @Test
    void testEliminar_Success() {
        // Arrange
        when(entityManager.contains(testVenta)).thenReturn(false);
        when(entityManager.merge(any(Venta.class))).thenReturn(testVenta);
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
    void testEliminar_NullEntity() {
        // Act & Assert - Debería lanzar IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            dao.eliminar(null);
        });
    }

    @Test
    void testEliminar_EntityManagerNulo_LanzaRuntimeException() throws Exception {
        // Arrange
        VentaDAO daoWithNullEm = new VentaDAO();
        Field emField = VentaDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(daoWithNullEm, null);

        // Act & Assert - CORREGIDO: Cambiar a RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            daoWithNullEm.eliminar(testVenta);
        });
        assertEquals("Error al eliminar el registro", exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("EntityManager no disponible", exception.getCause().getMessage());
    }

    @Test
    void testEliminar_Exception() {
        // Arrange
        when(entityManager.contains(testVenta)).thenReturn(true);
        doThrow(new RuntimeException("Remove error")).when(entityManager).remove(testVenta);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dao.eliminar(testVenta);
        });
        assertEquals("Error al eliminar el registro", exception.getMessage());
    }

    @Test
    void testEliminarPorId_Success() {
        // Arrange
        when(entityManager.find(Venta.class, testVentaId)).thenReturn(testVenta);
        doNothing().when(entityManager).remove(testVenta);

        // Act
        dao.eliminarPorId(testVentaId);

        // Assert
        verify(entityManager).find(Venta.class, testVentaId);
        verify(entityManager).remove(testVenta);
    }

    @Test
    void testEliminarPorId_NotFound() {
        // Arrange
        when(entityManager.find(Venta.class, testVentaId)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dao.eliminarPorId(testVentaId);
        });

        assertEquals("Error al eliminar el registro por ID", exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Registro no encontrado", exception.getCause().getMessage());
        verify(entityManager).find(Venta.class, testVentaId);
        verify(entityManager, never()).remove(any());
    }

    @Test
    void testEliminarPorId_NullId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dao.eliminarPorId(null);
        });
        assertEquals("El ID no puede ser nulo", exception.getMessage());
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void testEliminarPorId_EntityManagerNulo_LanzaRuntimeException() throws Exception {
        // Arrange
        VentaDAO daoWithNullEm = new VentaDAO();
        Field emField = VentaDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(daoWithNullEm, null);

        // Act & Assert - CORREGIDO: Cambiar a RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            daoWithNullEm.eliminarPorId(testVentaId);
        });
        assertEquals("Error al eliminar el registro por ID", exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("EntityManager no disponible", exception.getCause().getMessage());
    }

    @Test
    void testFind_Success() {
        // Arrange
        when(entityManager.find(Venta.class, testVentaId)).thenReturn(testVenta);

        // Act
        Venta result = dao.find(testVentaId);

        // Assert
        assertNotNull(result);
        assertEquals(testVentaId, result.getId());
        verify(entityManager).find(Venta.class, testVentaId);
    }

    @Test
    void testFind_NullId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dao.find(null);
        });
        assertEquals("El ID no puede ser nulo", exception.getMessage());
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void testFind_EntityManagerNulo() throws Exception {
        // Arrange - Crear DAO con EntityManager nulo
        VentaDAO daoWithNullEm = new VentaDAO();
        Field emField = VentaDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(daoWithNullEm, null);

        // Act & Assert - CORREGIDO: Cambiar el mensaje esperado
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            daoWithNullEm.find(testVentaId);
        });
        assertEquals("Error al buscar el registro por ID", exception.getMessage());
    }

    @Test
    void testFind_Exception() {
        // Arrange
        when(entityManager.find(Venta.class, testVentaId))
                .thenThrow(new RuntimeException("Find error"));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            dao.find(testVentaId);
        });
        assertEquals("Error al buscar el registro por ID", exception.getMessage());
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

    @Test
    void testFindAll_EntityManagerNulo_LanzaIllegalStateException() throws Exception {
        // Arrange
        VentaDAO daoWithNullEm = new VentaDAO();
        Field emField = VentaDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(daoWithNullEm, null);

        // Act & Assert - CORREGIDO: Cambiar mensaje esperado
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            daoWithNullEm.findAll();
        });
        assertEquals("Error al acceder a todos los registros", exception.getMessage());
    }

    @Test
    void testFindAll_Exception() {
        // Arrange
        when(entityManager.getCriteriaBuilder()).thenThrow(new RuntimeException("Criteria error"));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            dao.findAll();
        });
        assertEquals("Error al acceder a todos los registros", exception.getMessage());
    }

    @Test
    void testContar_Success() {
        // Arrange
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(criteriaQueryLong);
        when(criteriaQueryLong.from(Venta.class)).thenReturn(root);
        when(criteriaQueryLong.select(any())).thenReturn(criteriaQueryLong);
        when(entityManager.createQuery(criteriaQueryLong)).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(10L);

        // Act
        int result = dao.contar();

        // Assert
        assertEquals(10, result);
        verify(entityManager).getCriteriaBuilder();
        verify(longTypedQuery).getSingleResult();
    }

    @Test
    void testContar_EntityManagerNulo() throws Exception {
        // Arrange - Crear DAO con EntityManager nulo
        VentaDAO daoWithNullEm = new VentaDAO();
        Field emField = VentaDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(daoWithNullEm, null);

        // Act
        int result = daoWithNullEm.contar();

        // Assert
        assertEquals(-1, result);
    }

    @Test
    void testContar_WithException() {
        // Arrange
        when(entityManager.getCriteriaBuilder()).thenThrow(new RuntimeException("Contar error"));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            dao.contar();
        });
        assertEquals("dao.AccesoDB", exception.getMessage());
    }

    // Tests para casos edge adicionales

    @Test
    void testActualizarEstado_WithNullEstado() {
        // Arrange
        when(entityManager.find(Venta.class, testVentaId)).thenReturn(testVenta);
        when(entityManager.merge(testVenta)).thenReturn(testVenta);

        // Act
        dao.actualizarEstado(testVentaId, null);

        // Assert
        assertNull(testVenta.getEstado());
        verify(entityManager).find(Venta.class, testVentaId);
        verify(entityManager).merge(testVenta);
    }

    @Test
    void testFindByEstado_WithNullEstado() {
        // Arrange
        List<Venta> expectedList = new ArrayList<>();
        expectedList.add(testVenta);

        when(entityManager.createQuery("SELECT c FROM Venta c WHERE c.estado = :estado", Venta.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("estado", null)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Venta> result = dao.findByEstado(null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(typedQuery).setParameter("estado", null);
    }

    @Test
    void testFindRange_EntityManagerNulo_ReturnsEmptyList() throws Exception {
        // Arrange
        VentaDAO daoWithNullEm = new VentaDAO();
        Field emField = VentaDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(daoWithNullEm, null);

        // Act - VentaDAO.findRange() sobreescribe el comportamiento y devuelve lista vacía
        List<Venta> result = daoWithNullEm.findRange(0, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCount_EntityManagerNulo_ReturnsZero() throws Exception {
        // Arrange
        VentaDAO daoWithNullEm = new VentaDAO();
        Field emField = VentaDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(daoWithNullEm, null);

        // Act - VentaDAO.count() sobreescribe el comportamiento y devuelve 0L en lugar de lanzar excepción
        Long result = daoWithNullEm.count();

        // Assert
        assertEquals(0L, result);
    }

    @Test
    void testFindRange_ExceptionInCriteria_ReturnsEmptyList() {
        // Arrange
        when(entityManager.createQuery(
                "SELECT v FROM Venta v JOIN FETCH v.idCliente ORDER BY v.fecha DESC",
                Venta.class))
                .thenThrow(new RuntimeException("Database error"));

        // Act - VentaDAO.findRange() devuelve lista vacía en caso de excepción
        List<Venta> result = dao.findRange(0, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Tests para el constructor y métodos básicos
    @Test
    void testConstructor() {
        // Act
        VentaDAO nuevoDao = new VentaDAO();

        // Assert
        assertNotNull(nuevoDao);
        assertEquals(Venta.class, nuevoDao.getEntityClass());
    }

    @Test
    void testGetEntityManager_AfterConstruction() throws Exception {
        // Arrange
        VentaDAO nuevoDao = new VentaDAO();
        Field emField = VentaDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(nuevoDao, entityManager);

        // Act
        EntityManager result = nuevoDao.getEntityManager();

        // Assert
        assertEquals(entityManager, result);
    }
}