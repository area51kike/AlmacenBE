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
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.CompraDetalle;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompraDetalleDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<CompraDetalle> typedQuery;

    @Mock
    private TypedQuery<Long> longTypedQuery;

    @Mock
    private TypedQuery<BigDecimal> bigDecimalTypedQuery;

    @InjectMocks
    private CompraDetalleDAO compraDetalleDAO;

    private CompraDetalle compraDetalle;
    private UUID testId;
    private Long testCompraId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testCompraId = 1L;

        compraDetalle = new CompraDetalle();
        compraDetalle.setId(testId);
        compraDetalle.setCantidad(new BigDecimal("5"));
        compraDetalle.setPrecio(new BigDecimal("10.50"));
        compraDetalle.setEstado("RECIBIDO");
        compraDetalle.setObservaciones("Detalle de prueba");
    }

    @Test
    void testGetEntityManager() {
        assertNotNull(compraDetalleDAO.getEntityManager());
        assertEquals(em, compraDetalleDAO.getEntityManager());
    }

    @Test
    void testGetEntityClass() {
        assertEquals(CompraDetalle.class, compraDetalleDAO.getEntityClass());
    }

    // Tests para los métodos específicos de CompraDetalleDAO

    @Test
    void testContarDetallesRecibidosPorCompra_Success() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(eq("idCompra"), eq(testCompraId))).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(3L);

        Long result = compraDetalleDAO.contarDetallesRecibidosPorCompra(testCompraId);

        assertEquals(3L, result);
        verify(em).createQuery(contains("RECIBIDO"), eq(Long.class));
        verify(longTypedQuery).setParameter("idCompra", testCompraId);
        verify(longTypedQuery).getSingleResult();
    }

    @Test
    void testContarDetallesRecibidosPorCompra_Exception() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(eq("idCompra"), eq(testCompraId))).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenThrow(new RuntimeException("Database error"));

        Long result = compraDetalleDAO.contarDetallesRecibidosPorCompra(testCompraId);

        assertEquals(0L, result);
        verify(em).createQuery(anyString(), eq(Long.class));
    }

    @Test
    void testFindByIdCompra_Success() {
        List<CompraDetalle> expectedList = Arrays.asList(compraDetalle);

        when(em.createQuery(anyString(), eq(CompraDetalle.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idCompra"), eq(testCompraId))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<CompraDetalle> result = compraDetalleDAO.findByIdCompra(testCompraId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testId, result.get(0).getId());
        verify(em).createQuery(contains("cd.idCompra.id = :idCompra"), eq(CompraDetalle.class));
        verify(typedQuery).setParameter("idCompra", testCompraId);
        verify(typedQuery).getResultList();
    }

    @Test
    void testFindByIdCompra_EmptyResult() {
        when(em.createQuery(anyString(), eq(CompraDetalle.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idCompra"), eq(testCompraId))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of());

        List<CompraDetalle> result = compraDetalleDAO.findByIdCompra(testCompraId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(typedQuery).getResultList();
    }

    @Test
    void testObtenerTotalCompra_Success() {
        BigDecimal expectedTotal = new BigDecimal("52.50");

        when(em.createQuery(anyString(), eq(BigDecimal.class))).thenReturn(bigDecimalTypedQuery);
        when(bigDecimalTypedQuery.setParameter(eq("idCompra"), eq(testCompraId))).thenReturn(bigDecimalTypedQuery);
        when(bigDecimalTypedQuery.getSingleResult()).thenReturn(expectedTotal);

        BigDecimal result = compraDetalleDAO.obtenerTotalCompra(testCompraId);

        assertNotNull(result);
        assertEquals(expectedTotal, result);
        verify(em).createQuery(contains("SUM(cd.cantidad * cd.precio)"), eq(BigDecimal.class));
        verify(bigDecimalTypedQuery).setParameter("idCompra", testCompraId);
        verify(bigDecimalTypedQuery).getSingleResult();
    }

    @Test
    void testObtenerTotalCompra_NullResult() {
        when(em.createQuery(anyString(), eq(BigDecimal.class))).thenReturn(bigDecimalTypedQuery);
        when(bigDecimalTypedQuery.setParameter(eq("idCompra"), eq(testCompraId))).thenReturn(bigDecimalTypedQuery);
        when(bigDecimalTypedQuery.getSingleResult()).thenReturn(null);

        BigDecimal result = compraDetalleDAO.obtenerTotalCompra(testCompraId);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
        verify(bigDecimalTypedQuery).getSingleResult();
    }

    @Test
    void testObtenerTotalCompra_ZeroResult() {
        when(em.createQuery(anyString(), eq(BigDecimal.class))).thenReturn(bigDecimalTypedQuery);
        when(bigDecimalTypedQuery.setParameter(eq("idCompra"), eq(testCompraId))).thenReturn(bigDecimalTypedQuery);
        when(bigDecimalTypedQuery.getSingleResult()).thenReturn(BigDecimal.ZERO);

        BigDecimal result = compraDetalleDAO.obtenerTotalCompra(testCompraId);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testTodosDetallesRecibidos_True() {
        // Mock para contarDetallesPorCompra
        when(em.createQuery(contains("COUNT(d) FROM CompraDetalle d WHERE"), eq(Long.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(eq("idCompra"), eq(testCompraId)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult())
                .thenReturn(5L)  // Total detalles
                .thenReturn(5L); // Todos recibidos

        boolean result = compraDetalleDAO.todosDetallesRecibidos(testCompraId);

        assertTrue(result);
        verify(em, times(2)).createQuery(anyString(), eq(Long.class));
        verify(longTypedQuery, times(2)).setParameter("idCompra", testCompraId);
        verify(longTypedQuery, times(2)).getSingleResult();
    }

    @Test
    void testTodosDetallesRecibidos_False() {
        when(em.createQuery(contains("COUNT(d) FROM CompraDetalle d WHERE"), eq(Long.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(eq("idCompra"), eq(testCompraId)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult())
                .thenReturn(5L)  // Total detalles
                .thenReturn(3L); // Solo 3 recibidos

        boolean result = compraDetalleDAO.todosDetallesRecibidos(testCompraId);

        assertFalse(result);
    }

    @Test
    void testTodosDetallesRecibidos_ZeroTotal() {
        when(em.createQuery(contains("COUNT(d) FROM CompraDetalle d WHERE"), eq(Long.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(eq("idCompra"), eq(testCompraId)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult())
                .thenReturn(0L)  // Total detalles = 0
                .thenReturn(0L); // Recibidos = 0

        boolean result = compraDetalleDAO.todosDetallesRecibidos(testCompraId);

        assertFalse(result); // Debería ser false cuando total es 0
    }

    @Test
    void testContarDetallesPorCompra_Success() {
        when(em.createQuery(contains("COUNT(d) FROM CompraDetalle d WHERE d.idCompra.id = :idCompra"), eq(Long.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(eq("idCompra"), eq(testCompraId))).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(10L);

        Long result = compraDetalleDAO.contarDetallesPorCompra(testCompraId);

        assertEquals(10L, result);
        verify(em).createQuery(contains("d.idCompra.id = :idCompra"), eq(Long.class));
        verify(longTypedQuery).setParameter("idCompra", testCompraId);
        verify(longTypedQuery).getSingleResult();
    }

    @Test
    void testContarDetallesPorCompra_Exception() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(eq("idCompra"), eq(testCompraId))).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenThrow(new RuntimeException("Database error"));

        Long result = compraDetalleDAO.contarDetallesPorCompra(testCompraId);

        assertEquals(0L, result);
        verify(em).createQuery(anyString(), eq(Long.class));
    }

    @Test
    void testFindById_Success() {
        when(em.find(CompraDetalle.class, testId)).thenReturn(compraDetalle);

        CompraDetalle result = compraDetalleDAO.findById(testId);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        verify(em).find(CompraDetalle.class, testId);
    }

    @Test
    void testFindById_NotFound() {
        when(em.find(CompraDetalle.class, testId)).thenReturn(null);

        CompraDetalle result = compraDetalleDAO.findById(testId);

        assertNull(result);
        verify(em).find(CompraDetalle.class, testId);
    }

    // Tests para los métodos heredados de InventarioDefaultDataAccess

    @Test
    void testFind_Success() {
        when(em.find(CompraDetalle.class, testId)).thenReturn(compraDetalle);

        CompraDetalle result = compraDetalleDAO.find(testId);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        verify(em).find(CompraDetalle.class, testId);
    }

    @Test
    void testFind_NullId() {
        assertThrows(IllegalArgumentException.class, () -> {
            compraDetalleDAO.find(null);
        });
    }

    @Test
    void testFindAll() {
        List<CompraDetalle> expectedList = Arrays.asList(compraDetalle);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<CompraDetalle> cq = mock(CriteriaQuery.class);
        Root<CompraDetalle> root = mock(Root.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(CompraDetalle.class)).thenReturn(cq);
        when(cq.from(CompraDetalle.class)).thenReturn(root);
        when(cq.select(root)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<CompraDetalle> result = compraDetalleDAO.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(em).getCriteriaBuilder();
        verify(typedQuery).getResultList();
    }

    @Test
    void testFindRange_WithValidRange() {
        List<CompraDetalle> expectedList = Arrays.asList(compraDetalle);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<CompraDetalle> cq = mock(CriteriaQuery.class);
        Root<CompraDetalle> root = mock(Root.class);
        jakarta.persistence.criteria.Order order = mock(jakarta.persistence.criteria.Order.class);
        jakarta.persistence.criteria.Path<Object> path = mock(jakarta.persistence.criteria.Path.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(CompraDetalle.class)).thenReturn(cq);
        when(cq.from(CompraDetalle.class)).thenReturn(root);
        when(root.get("id")).thenReturn(path);
        when(cb.asc(path)).thenReturn(order);
        when(cq.select(root)).thenReturn(cq);
        when(cq.orderBy(order)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(0)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(10)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<CompraDetalle> result = compraDetalleDAO.findRange(0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(typedQuery).setFirstResult(0);
        verify(typedQuery).setMaxResults(10);
        verify(typedQuery).getResultList();
    }

    @Test
    void testCount() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Long> cq = mock(CriteriaQuery.class);
        Root<CompraDetalle> root = mock(Root.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Long.class)).thenReturn(cq);
        when(cq.from(CompraDetalle.class)).thenReturn(root);
        when(cq.select(any())).thenReturn(cq);
        when(cb.count(root)).thenReturn(null);
        when(em.createQuery(cq)).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(5L);

        Long count = compraDetalleDAO.count();

        assertEquals(5L, count);
        verify(em).getCriteriaBuilder();
        verify(longTypedQuery).getSingleResult();
    }

    @Test
    void testContar() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Long> cq = mock(CriteriaQuery.class);
        Root<CompraDetalle> root = mock(Root.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Long.class)).thenReturn(cq);
        when(cq.from(CompraDetalle.class)).thenReturn(root);
        when(cq.select(any())).thenReturn(cq);
        when(cb.count(root)).thenReturn(null);
        when(em.createQuery(cq)).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(5L);

        int result = compraDetalleDAO.contar();

        assertEquals(5, result);
        verify(em).getCriteriaBuilder();
        verify(longTypedQuery).getSingleResult();
    }

    @Test
    void testCrear_Success() {
        doNothing().when(em).persist(any(CompraDetalle.class));

        compraDetalleDAO.crear(compraDetalle);

        verify(em).persist(compraDetalle);
    }

    @Test
    void testModificar_Success() {
        when(em.merge(any(CompraDetalle.class))).thenReturn(compraDetalle);

        CompraDetalle result = compraDetalleDAO.modificar(compraDetalle);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        verify(em).merge(compraDetalle);
    }

    @Test
    void testEliminar_Success() {
        when(em.contains(compraDetalle)).thenReturn(false);
        when(em.merge(any(CompraDetalle.class))).thenReturn(compraDetalle);
        doNothing().when(em).remove(any(CompraDetalle.class));

        compraDetalleDAO.eliminar(compraDetalle);

        verify(em).merge(compraDetalle);
        verify(em).remove(compraDetalle);
    }

    @Test
    void testEliminarPorId_Success() {
        when(em.find(CompraDetalle.class, testId)).thenReturn(compraDetalle);
        doNothing().when(em).remove(any(CompraDetalle.class));

        compraDetalleDAO.eliminarPorId(testId);

        verify(em).find(CompraDetalle.class, testId);
        verify(em).remove(compraDetalle);
    }

    // Tests de integración entre métodos

    @Test
    void testIntegration_TodosDetallesRecibidos() {
        // Configurar mocks para el flujo completo de todosDetallesRecibidos
        when(em.createQuery(contains("COUNT(d) FROM CompraDetalle d WHERE"), eq(Long.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(eq("idCompra"), eq(testCompraId)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult())
                .thenReturn(3L)  // contarDetallesPorCompra
                .thenReturn(3L); // contarDetallesRecibidosPorCompra

        boolean result = compraDetalleDAO.todosDetallesRecibidos(testCompraId);

        assertTrue(result);
        verify(longTypedQuery, times(2)).getSingleResult();
    }

    @Test
    void testEdgeCases() {
        // Test con diferentes estados
        compraDetalle.setEstado("PENDIENTE");
        when(em.find(CompraDetalle.class, testId)).thenReturn(compraDetalle);

        CompraDetalle result = compraDetalleDAO.findById(testId);
        assertEquals("PENDIENTE", result.getEstado());

        // Test con cantidad cero
        compraDetalle.setCantidad(BigDecimal.ZERO);
        assertNotNull(compraDetalle.getCantidad());
    }

    @Test
    void testLoggerInExceptionCases() {
        // Verificar que el logger se usa en casos de excepción
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(eq("idCompra"), eq(testCompraId))).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenThrow(new RuntimeException("Test exception"));

        Long result = compraDetalleDAO.contarDetallesRecibidosPorCompra(testCompraId);

        assertEquals(0L, result);
        // El logger debería capturar la excepción, pero no podemos verificar fácilmente el logging en tests
    }
}