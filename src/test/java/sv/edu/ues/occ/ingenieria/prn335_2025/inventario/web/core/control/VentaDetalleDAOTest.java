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
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Venta;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.VentaDetalle;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaDetalleDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<VentaDetalle> typedQueryVentaDetalle;

    @Mock
    private TypedQuery<Producto> typedQueryProducto;

    @Mock
    private TypedQuery<Long> longTypedQuery;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<VentaDetalle> criteriaQuery;

    @Mock
    private CriteriaQuery<Long> criteriaQueryLong;

    @Mock
    private Root<VentaDetalle> root;

    @InjectMocks
    private VentaDetalleDAO dao;

    private VentaDetalle testVentaDetalle;
    private UUID testVentaDetalleId;
    private UUID testVentaId;
    private UUID testProductoId;
    private Venta testVenta;
    private Producto testProducto;

    @BeforeEach
    void setUp() throws Exception {
        // Inyectar el EntityManager usando reflexión
        Field emField = VentaDetalleDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(dao, entityManager);

        // Inicializar IDs de prueba
        testVentaDetalleId = UUID.randomUUID();
        testVentaId = UUID.randomUUID();
        testProductoId = UUID.randomUUID();

        // Crear Venta de prueba
        testVenta = new Venta();
        testVenta.setId(testVentaId);

        // Crear Producto de prueba
        testProducto = new Producto();
        testProducto.setId(testProductoId);
        testProducto.setNombreProducto("Producto Test");

        // Crear VentaDetalle de prueba
        testVentaDetalle = new VentaDetalle();
        testVentaDetalle.setId(testVentaDetalleId);
        testVentaDetalle.setIdVenta(testVenta);
        testVentaDetalle.setIdProducto(testProducto);
        testVentaDetalle.setCantidad(new BigDecimal("5.00"));
        testVentaDetalle.setPrecio(new BigDecimal("100.00"));
        testVentaDetalle.setEstado("ACTIVO");
        testVentaDetalle.setObservaciones("Detalle de prueba");
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
        Class<VentaDetalle> entityClass = dao.getEntityClass();

        // Assert
        assertNotNull(entityClass);
        assertEquals(VentaDetalle.class, entityClass);
    }

    @Test
    void testFind_Success() {
        // Arrange
        when(entityManager.find(VentaDetalle.class, testVentaDetalleId))
                .thenReturn(testVentaDetalle);

        // Act
        VentaDetalle result = dao.find(testVentaDetalleId);

        // Assert
        assertNotNull(result);
        assertEquals(testVentaDetalleId, result.getId());
        assertEquals(new BigDecimal("5.00"), result.getCantidad());
        assertEquals(new BigDecimal("100.00"), result.getPrecio());
        verify(entityManager, times(1)).find(VentaDetalle.class, testVentaDetalleId);
    }

    @Test
    void testFind_NotFound() {
        // Arrange
        when(entityManager.find(VentaDetalle.class, testVentaDetalleId))
                .thenReturn(null);

        // Act
        VentaDetalle result = dao.find(testVentaDetalleId);

        // Assert
        assertNull(result);
        verify(entityManager, times(1)).find(VentaDetalle.class, testVentaDetalleId);
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
        when(entityManager.find(VentaDetalle.class, testVentaDetalleId))
                .thenReturn(testVentaDetalle);

        // Act
        VentaDetalle result = dao.findById(testVentaDetalleId);

        // Assert
        assertNotNull(result);
        assertEquals(testVentaDetalleId, result.getId());
        assertEquals("ACTIVO", result.getEstado());
        verify(entityManager, times(1)).find(VentaDetalle.class, testVentaDetalleId);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(entityManager.find(VentaDetalle.class, testVentaDetalleId))
                .thenReturn(null);

        // Act
        VentaDetalle result = dao.findById(testVentaDetalleId);

        // Assert
        assertNull(result);
    }

    @Test
    void testFindAll_Success() {
        // Arrange
        List<VentaDetalle> expectedList = new ArrayList<>();
        expectedList.add(testVentaDetalle);

        VentaDetalle detalle2 = new VentaDetalle();
        detalle2.setId(UUID.randomUUID());
        detalle2.setIdVenta(testVenta);
        detalle2.setIdProducto(testProducto);
        detalle2.setCantidad(new BigDecimal("3.00"));
        detalle2.setPrecio(new BigDecimal("50.00"));
        expectedList.add(detalle2);

        // Mock completo de Criteria API
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(VentaDetalle.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(VentaDetalle.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.getResultList()).thenReturn(expectedList);

        // Act
        List<VentaDetalle> result = dao.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testVentaDetalleId, result.get(0).getId());
        verify(entityManager, times(1)).getCriteriaBuilder();
    }

    @Test
    void testFindAll_EmptyList() {
        // Arrange
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(VentaDetalle.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(VentaDetalle.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.getResultList()).thenReturn(new ArrayList<>());

        // Act
        List<VentaDetalle> result = dao.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindRange_Success() {
        // Arrange
        List<VentaDetalle> expectedList = new ArrayList<>();
        expectedList.add(testVentaDetalle);

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(VentaDetalle.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(VentaDetalle.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(root.get("id")).thenReturn(mock(jakarta.persistence.criteria.Path.class));
        when(criteriaBuilder.asc(any())).thenReturn(mock(jakarta.persistence.criteria.Order.class));
        when(criteriaQuery.orderBy(any(jakarta.persistence.criteria.Order.class))).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.setFirstResult(anyInt())).thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.setMaxResults(anyInt())).thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.getResultList()).thenReturn(expectedList);

        // Act
        List<VentaDetalle> result = dao.findRange(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(typedQueryVentaDetalle, times(1)).setFirstResult(0);
        verify(typedQueryVentaDetalle, times(1)).setMaxResults(10);
    }

    @Test
    void testFindRange_WithPagination() {
        // Arrange
        List<VentaDetalle> expectedList = new ArrayList<>();
        expectedList.add(testVentaDetalle);

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(VentaDetalle.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(VentaDetalle.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(root.get("id")).thenReturn(mock(jakarta.persistence.criteria.Path.class));
        when(criteriaBuilder.asc(any())).thenReturn(mock(jakarta.persistence.criteria.Order.class));
        when(criteriaQuery.orderBy(any(jakarta.persistence.criteria.Order.class))).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.setFirstResult(anyInt())).thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.setMaxResults(anyInt())).thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.getResultList()).thenReturn(expectedList);

        // Act
        List<VentaDetalle> result = dao.findRange(10, 5);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(typedQueryVentaDetalle, times(1)).setFirstResult(10);
        verify(typedQueryVentaDetalle, times(1)).setMaxResults(5);
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
        when(criteriaQueryLong.from(VentaDetalle.class)).thenReturn(root);
        when(criteriaBuilder.count(root)).thenReturn(mock(jakarta.persistence.criteria.Expression.class));
        when(criteriaQueryLong.select(any())).thenReturn(criteriaQueryLong);
        when(entityManager.createQuery(criteriaQueryLong)).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(10L);

        // Act
        Long result = dao.count();

        // Assert
        assertNotNull(result);
        assertEquals(10L, result);
        verify(entityManager, times(1)).getCriteriaBuilder();
    }

    @Test
    void testCount_Zero() {
        // Arrange
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(criteriaQueryLong);
        when(criteriaQueryLong.from(VentaDetalle.class)).thenReturn(root);
        when(criteriaBuilder.count(root)).thenReturn(mock(jakarta.persistence.criteria.Expression.class));
        when(criteriaQueryLong.select(any())).thenReturn(criteriaQueryLong);
        when(entityManager.createQuery(criteriaQueryLong)).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(0L);

        // Act
        Long result = dao.count();

        // Assert
        assertNotNull(result);
        assertEquals(0L, result);
    }

    @Test
    void testCrear_Success() {
        // Arrange
        doNothing().when(entityManager).persist(any(VentaDetalle.class));

        // Act
        dao.crear(testVentaDetalle);

        // Assert
        verify(entityManager, times(1)).persist(testVentaDetalle);
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
        when(entityManager.merge(any(VentaDetalle.class)))
                .thenReturn(testVentaDetalle);

        // Act
        VentaDetalle result = dao.modificar(testVentaDetalle);

        // Assert
        assertNotNull(result);
        assertEquals(testVentaDetalle.getId(), result.getId());
        verify(entityManager, times(1)).merge(testVentaDetalle);
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
        when(entityManager.contains(testVentaDetalle)).thenReturn(false);
        when(entityManager.merge(testVentaDetalle)).thenReturn(testVentaDetalle);
        doNothing().when(entityManager).remove(any(VentaDetalle.class));

        // Act
        dao.eliminar(testVentaDetalle);

        // Assert
        verify(entityManager, times(1)).merge(testVentaDetalle);
        verify(entityManager, times(1)).remove(any(VentaDetalle.class));
    }

    @Test
    void testEliminar_EntityAttached() {
        // Arrange
        when(entityManager.contains(testVentaDetalle)).thenReturn(true);
        doNothing().when(entityManager).remove(any(VentaDetalle.class));

        // Act
        dao.eliminar(testVentaDetalle);

        // Assert
        verify(entityManager, never()).merge(any());
        verify(entityManager, times(1)).remove(testVentaDetalle);
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
        when(entityManager.find(VentaDetalle.class, testVentaDetalleId))
                .thenReturn(testVentaDetalle);
        doNothing().when(entityManager).remove(any(VentaDetalle.class));

        // Act
        dao.eliminarPorId(testVentaDetalleId);

        // Assert
        verify(entityManager, times(1)).find(VentaDetalle.class, testVentaDetalleId);
        verify(entityManager, times(1)).remove(testVentaDetalle);
    }

    @Test
    void testEliminarPorId_NotFound() {
        // Arrange
        when(entityManager.find(VentaDetalle.class, testVentaDetalleId))
                .thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> dao.eliminarPorId(testVentaDetalleId));
        verify(entityManager, never()).remove(any());
    }

    @Test
    void testEliminarPorId_NullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> dao.eliminarPorId(null));
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void testFindProductosByIdVenta_Success() {
        // Arrange
        List<Producto> expectedList = new ArrayList<>();
        expectedList.add(testProducto);

        Producto producto2 = new Producto();
        producto2.setId(UUID.randomUUID());
        producto2.setNombreProducto("Producto 2");
        expectedList.add(producto2);

        when(entityManager.createQuery(
                "SELECT vd.idProducto FROM VentaDetalle vd WHERE vd.idVenta.id = :idVenta",
                Producto.class))
                .thenReturn(typedQueryProducto);
        when(typedQueryProducto.setParameter(eq("idVenta"), any(UUID.class)))
                .thenReturn(typedQueryProducto);
        when(typedQueryProducto.getResultList()).thenReturn(expectedList);

        // Act
        List<Producto> result = dao.findProductosByIdVenta(testVentaId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testProductoId, result.get(0).getId());
        assertEquals("Producto Test", result.get(0).getNombreProducto());
        verify(typedQueryProducto, times(1)).setParameter("idVenta", testVentaId);
        verify(typedQueryProducto, times(1)).getResultList();
    }

    @Test
    void testFindProductosByIdVenta_EmptyList() {
        // Arrange
        when(entityManager.createQuery(
                "SELECT vd.idProducto FROM VentaDetalle vd WHERE vd.idVenta.id = :idVenta",
                Producto.class))
                .thenReturn(typedQueryProducto);
        when(typedQueryProducto.setParameter(eq("idVenta"), any(UUID.class)))
                .thenReturn(typedQueryProducto);
        when(typedQueryProducto.getResultList()).thenReturn(new ArrayList<>());

        // Act
        List<Producto> result = dao.findProductosByIdVenta(testVentaId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindProductosByIdVenta_NullIdVenta() {
        // Arrange
        when(entityManager.createQuery(
                "SELECT vd.idProducto FROM VentaDetalle vd WHERE vd.idVenta.id = :idVenta",
                Producto.class))
                .thenReturn(typedQueryProducto);
        when(typedQueryProducto.setParameter(eq("idVenta"), any()))
                .thenReturn(typedQueryProducto);
        when(typedQueryProducto.getResultList()).thenReturn(new ArrayList<>());

        // Act
        List<Producto> result = dao.findProductosByIdVenta(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}