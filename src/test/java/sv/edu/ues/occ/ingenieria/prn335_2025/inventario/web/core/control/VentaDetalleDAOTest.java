package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
    private TypedQuery<BigDecimal> bigDecimalTypedQuery;

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

    // Tests para métodos específicos de VentaDetalleDAO

    @Test
    void testConstructor() {
        // Act
        VentaDetalleDAO newDao = new VentaDetalleDAO();

        // Assert
        assertNotNull(newDao);
    }

    @Test
    void testGetEntityManager() {
        // Act
        EntityManager result = dao.getEntityManager();

        // Assert
        assertNotNull(result);
        assertEquals(entityManager, result);
    }

    @Test
    void testFindProductosByIdVenta_Success() {
        // Arrange
        List<Producto> expectedList = new ArrayList<>();
        expectedList.add(testProducto);

        when(entityManager.createQuery(
                "SELECT vd.idProducto FROM VentaDetalle vd WHERE vd.idVenta.id = :idVenta",
                Producto.class))
                .thenReturn(typedQueryProducto);
        when(typedQueryProducto.setParameter("idVenta", testVentaId))
                .thenReturn(typedQueryProducto);
        when(typedQueryProducto.getResultList()).thenReturn(expectedList);

        // Act
        List<Producto> result = dao.findProductosByIdVenta(testVentaId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProductoId, result.get(0).getId());
        verify(typedQueryProducto).setParameter("idVenta", testVentaId);
        verify(typedQueryProducto).getResultList();
    }

    @Test
    void testFindProductosByIdVenta_EmptyList() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(Producto.class)))
                .thenReturn(typedQueryProducto);
        when(typedQueryProducto.setParameter(anyString(), any(UUID.class)))
                .thenReturn(typedQueryProducto);
        when(typedQueryProducto.getResultList()).thenReturn(new ArrayList<>());

        // Act
        List<Producto> result = dao.findProductosByIdVenta(testVentaId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testContarDetallesPorVenta_Success() {
        // Arrange
        when(entityManager.createQuery(
                "SELECT COUNT(d) FROM VentaDetalle d WHERE d.idVenta.id = :idCompra",
                Long.class))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter("idCompra", testVentaId)).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(5L);

        // Act
        Long result = dao.contarDetallesPorVenta(testVentaId);

        // Assert
        assertEquals(5L, result);
        verify(entityManager).createQuery(contains("COUNT(d)"), eq(Long.class));
        verify(longTypedQuery).setParameter("idCompra", testVentaId);
        verify(longTypedQuery).getSingleResult();
    }

    @Test
    void testContarDetallesPorVenta_Exception() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(Long.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(anyString(), any())).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenThrow(new RuntimeException("Database error"));

        // Act
        Long result = dao.contarDetallesPorVenta(testVentaId);

        // Assert
        assertEquals(0L, result);
        verify(entityManager).createQuery(anyString(), eq(Long.class));
    }

    @Test
    void testContarDetallesDespachadosPorVenta_Success() {
        // Arrange
        when(entityManager.createQuery(
                "SELECT COUNT(d) FROM VentaDetalle d WHERE d.idVenta.id = :idVenta AND d.estado = 'DESPACHADO'",
                Long.class))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter("idVenta", testVentaId)).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(3L);

        // Act
        Long result = dao.contarDetallesDespachadosPorVenta(testVentaId);

        // Assert
        assertEquals(3L, result);
        verify(entityManager).createQuery(contains("DESPACHADO"), eq(Long.class));
        verify(longTypedQuery).setParameter("idVenta", testVentaId);
        verify(longTypedQuery).getSingleResult();
    }

    @Test
    void testContarDetallesDespachadosPorVenta_Exception() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(Long.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(anyString(), any())).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenThrow(new RuntimeException("Database error"));

        // Act
        Long result = dao.contarDetallesDespachadosPorVenta(testVentaId);

        // Assert
        assertEquals(0L, result);
        verify(entityManager).createQuery(anyString(), eq(Long.class));
    }

    @Test
    void testFindByIdVenta_Success() {
        // Arrange
        List<VentaDetalle> expectedList = new ArrayList<>();
        expectedList.add(testVentaDetalle);

        when(entityManager.createQuery(
                "SELECT vd FROM VentaDetalle vd WHERE vd.idVenta.id = :idVenta",
                VentaDetalle.class))
                .thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.setParameter("idVenta", testVentaId)).thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.getResultList()).thenReturn(expectedList);

        // Act
        List<VentaDetalle> result = dao.findByIdVenta(testVentaId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testVentaDetalleId, result.get(0).getId());
        verify(typedQueryVentaDetalle).setParameter("idVenta", testVentaId);
        verify(typedQueryVentaDetalle).getResultList();
    }

    @Test
    void testFindByIdVenta_EmptyList() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(VentaDetalle.class)))
                .thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.setParameter(anyString(), any(UUID.class)))
                .thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.getResultList()).thenReturn(new ArrayList<>());

        // Act
        List<VentaDetalle> result = dao.findByIdVenta(testVentaId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testTodosDetallesDespachados_True() {
        // Arrange
        when(entityManager.createQuery(contains("COUNT(d)"), eq(Long.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(anyString(), any(UUID.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult())
                .thenReturn(5L)
                .thenReturn(5L);

        // Act
        boolean result = dao.todosDetallesDespachados(testVentaId);

        // Assert
        assertTrue(result);
        verify(entityManager, times(2)).createQuery(anyString(), eq(Long.class));
        verify(longTypedQuery, times(2)).getSingleResult();
    }

    @Test
    void testTodosDetallesDespachados_False() {
        // Arrange
        when(entityManager.createQuery(contains("COUNT(d)"), eq(Long.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(anyString(), any(UUID.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult())
                .thenReturn(5L)
                .thenReturn(3L);

        // Act
        boolean result = dao.todosDetallesDespachados(testVentaId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testTodosDetallesDespachados_ZeroTotal() {
        // Arrange
        when(entityManager.createQuery(contains("COUNT(d)"), eq(Long.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(anyString(), any(UUID.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult())
                .thenReturn(0L)
                .thenReturn(0L);

        // Act
        boolean result = dao.todosDetallesDespachados(testVentaId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testObtenerTotalVenta_Success() {
        // Arrange
        BigDecimal expectedTotal = new BigDecimal("500.00");

        when(entityManager.createQuery(
                "SELECT COALESCE(SUM(cd.cantidad * cd.precio), 0) FROM VentaDetalle cd WHERE cd.idVenta.id = :idVenta",
                BigDecimal.class))
                .thenReturn(bigDecimalTypedQuery);
        when(bigDecimalTypedQuery.setParameter("idVenta", testVentaId)).thenReturn(bigDecimalTypedQuery);
        when(bigDecimalTypedQuery.getSingleResult()).thenReturn(expectedTotal);

        // Act
        BigDecimal result = dao.obtenerTotalVenta(testVentaId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedTotal, result);
        verify(entityManager).createQuery(contains("SUM(cd.cantidad * cd.precio)"), eq(BigDecimal.class));
        verify(bigDecimalTypedQuery).setParameter("idVenta", testVentaId);
        verify(bigDecimalTypedQuery).getSingleResult();
    }

    @Test
    void testObtenerTotalVenta_NullResult() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(BigDecimal.class)))
                .thenReturn(bigDecimalTypedQuery);
        when(bigDecimalTypedQuery.setParameter(anyString(), any())).thenReturn(bigDecimalTypedQuery);
        when(bigDecimalTypedQuery.getSingleResult()).thenReturn(null);

        // Act
        BigDecimal result = dao.obtenerTotalVenta(testVentaId);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testFindById_Success() {
        // Arrange
        when(entityManager.find(VentaDetalle.class, testVentaDetalleId)).thenReturn(testVentaDetalle);

        // Act
        VentaDetalle result = dao.findById(testVentaDetalleId);

        // Assert
        assertNotNull(result);
        assertEquals(testVentaDetalleId, result.getId());
        verify(entityManager).find(VentaDetalle.class, testVentaDetalleId);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(entityManager.find(VentaDetalle.class, testVentaDetalleId)).thenReturn(null);

        // Act
        VentaDetalle result = dao.findById(testVentaDetalleId);

        // Assert
        assertNull(result);
        verify(entityManager).find(VentaDetalle.class, testVentaDetalleId);
    }

    @Test
    void testContarPorVenta_Success() {
        // Arrange
        when(entityManager.createQuery(
                "SELECT COUNT(d) FROM VentaDetalle d WHERE d.idVenta.id = :idVenta",
                Long.class))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter("idVenta", testVentaId)).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(7L);

        // Act
        int result = dao.contarPorVenta(testVentaId);

        // Assert
        assertEquals(7, result);
        verify(longTypedQuery).setParameter("idVenta", testVentaId);
        verify(longTypedQuery).getSingleResult();
    }

    @Test
    void testContarPorVenta_NullResult() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(Long.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(anyString(), any())).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(null);

        // Act
        int result = dao.contarPorVenta(testVentaId);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void testContarPorVenta_Exception() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(Long.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(anyString(), any())).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenThrow(new RuntimeException("Database error"));

        // Act
        int result = dao.contarPorVenta(testVentaId);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void testFindPorVenta_Success() {
        // Arrange
        List<VentaDetalle> expectedList = new ArrayList<>();
        expectedList.add(testVentaDetalle);

        when(entityManager.createQuery(
                "SELECT vd FROM VentaDetalle vd WHERE vd.idVenta.id = :idVenta",
                VentaDetalle.class))
                .thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.setParameter("idVenta", testVentaId)).thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.setFirstResult(10)).thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.setMaxResults(5)).thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.getResultList()).thenReturn(expectedList);

        // Act
        List<VentaDetalle> result = dao.findPorVenta(testVentaId, 10, 5);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(typedQueryVentaDetalle).setParameter("idVenta", testVentaId);
        verify(typedQueryVentaDetalle).setFirstResult(10);
        verify(typedQueryVentaDetalle).setMaxResults(5);
        verify(typedQueryVentaDetalle).getResultList();
    }

    @Test
    void testFindPorVenta_EmptyList() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(VentaDetalle.class)))
                .thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.setParameter(anyString(), any(UUID.class)))
                .thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.setFirstResult(anyInt())).thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.setMaxResults(anyInt())).thenReturn(typedQueryVentaDetalle);
        when(typedQueryVentaDetalle.getResultList()).thenReturn(new ArrayList<>());

        // Act
        List<VentaDetalle> result = dao.findPorVenta(testVentaId, 0, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindPorVenta_Exception() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(VentaDetalle.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        List<VentaDetalle> result = dao.findPorVenta(testVentaId, 0, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}