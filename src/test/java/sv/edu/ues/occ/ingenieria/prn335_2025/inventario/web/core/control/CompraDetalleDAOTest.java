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
    private TypedQuery<Long> countQuery;

    @InjectMocks
    private CompraDetalleDAO compraDetalleDAO;

    private CompraDetalle compraDetalle;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        compraDetalle = new CompraDetalle();
        compraDetalle.setId(testId);
        compraDetalle.setCantidad(new BigDecimal("5"));
        compraDetalle.setPrecio(new BigDecimal("10.50"));
        compraDetalle.setEstado("ACTIVO");
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

    @Test
    void testFindById_Success() {
        when(em.find(CompraDetalle.class, testId)).thenReturn(compraDetalle);

        CompraDetalle result = compraDetalleDAO.findById(testId);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        verify(em, times(1)).find(CompraDetalle.class, testId);
    }

    @Test
    void testFindById_NotFound() {
        when(em.find(CompraDetalle.class, testId)).thenReturn(null);

        CompraDetalle result = compraDetalleDAO.findById(testId);

        assertNull(result);
        verify(em, times(1)).find(CompraDetalle.class, testId);
    }

    @Test
    void testFind_Success() {
        when(em.find(CompraDetalle.class, testId)).thenReturn(compraDetalle);

        CompraDetalle result = compraDetalleDAO.find(testId);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("ACTIVO", result.getEstado());
        verify(em, times(1)).find(CompraDetalle.class, testId);
    }

    @Test
    void testFind_NullId() {
        // find lanza IllegalArgumentException cuando id es null
        assertThrows(IllegalArgumentException.class, () -> {
            compraDetalleDAO.find(null);
        });
    }

    @Test
    void testFindAll() {
        List<CompraDetalle> expectedList = Arrays.asList(
                compraDetalle,
                createTestCompraDetalle()
        );

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
        assertEquals(2, result.size());
        verify(em, times(1)).getCriteriaBuilder();
        verify(typedQuery, times(1)).getResultList();
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
        verify(typedQuery, times(1)).setFirstResult(0);
        verify(typedQuery, times(1)).setMaxResults(10);
        verify(typedQuery, times(1)).getResultList();
    }

    @Test
    void testFindRange_WithNegativeFirst() {
        // findRange lanza IllegalArgumentException cuando first < 0
        assertThrows(IllegalArgumentException.class, () -> {
            compraDetalleDAO.findRange(-1, 10);
        });
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
        when(em.createQuery(cq)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(5L);

        Long count = compraDetalleDAO.count();

        assertEquals(5L, count);
        verify(em, times(1)).getCriteriaBuilder();
        verify(countQuery, times(1)).getSingleResult();
    }

    @Test
    void testCount_EmptyTable() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Long> cq = mock(CriteriaQuery.class);
        Root<CompraDetalle> root = mock(Root.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Long.class)).thenReturn(cq);
        when(cq.from(CompraDetalle.class)).thenReturn(root);
        when(cq.select(any())).thenReturn(cq);
        when(cb.count(root)).thenReturn(null);
        when(em.createQuery(cq)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);

        Long count = compraDetalleDAO.count();

        assertEquals(0L, count);
    }

    @Test
    void testCrear_Success() {
        doNothing().when(em).persist(any(CompraDetalle.class));

        compraDetalleDAO.crear(compraDetalle);

        verify(em, times(1)).persist(compraDetalle);
    }

    @Test
    void testCrear_NullEntity() {
        // crear lanza IllegalArgumentException cuando el registro es null
        assertThrows(IllegalArgumentException.class, () -> {
            compraDetalleDAO.crear(null);
        });
    }

    @Test
    void testModificar_Success() {
        when(em.merge(any(CompraDetalle.class))).thenReturn(compraDetalle);

        CompraDetalle result = compraDetalleDAO.modificar(compraDetalle);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        verify(em, times(1)).merge(compraDetalle);
    }

    @Test
    void testModificar_NullEntity() {
        // modificar lanza IllegalArgumentException cuando el registro es null
        assertThrows(IllegalArgumentException.class, () -> {
            compraDetalleDAO.modificar(null);
        });
    }

    @Test
    void testEliminar_Success() {
        when(em.contains(compraDetalle)).thenReturn(false);
        when(em.merge(any(CompraDetalle.class))).thenReturn(compraDetalle);
        doNothing().when(em).remove(any(CompraDetalle.class));

        compraDetalleDAO.eliminar(compraDetalle);

        verify(em, times(1)).merge(compraDetalle);
        verify(em, times(1)).remove(compraDetalle);
    }

    @Test
    void testEliminar_NullEntity() {
        // eliminar lanza IllegalArgumentException cuando la entidad es null
        assertThrows(IllegalArgumentException.class, () -> {
            compraDetalleDAO.eliminar(null);
        });
    }

    @Test
    void testEliminarPorId_Success() {
        when(em.find(CompraDetalle.class, testId)).thenReturn(compraDetalle);
        doNothing().when(em).remove(any(CompraDetalle.class));

        compraDetalleDAO.eliminarPorId(testId);

        verify(em, times(1)).find(CompraDetalle.class, testId);
        verify(em, times(1)).remove(compraDetalle);
    }

    @Test
    void testEliminarPorId_NotFound() {
        when(em.find(CompraDetalle.class, testId)).thenReturn(null);

        // eliminarPorId lanza RuntimeException cuando no encuentra el registro
        // (wrappea el IllegalArgumentException en RuntimeException)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraDetalleDAO.eliminarPorId(testId);
        });

        assertTrue(exception.getMessage().contains("Error al eliminar el registro por ID"));
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertTrue(exception.getCause().getMessage().contains("Registro no encontrado"));

        verify(em, times(1)).find(CompraDetalle.class, testId);
        verify(em, never()).remove(any());
    }

    @Test
    void testEliminarPorId_NullId() {
        // eliminarPorId lanza IllegalArgumentException cuando el ID es null
        assertThrows(IllegalArgumentException.class, () -> {
            compraDetalleDAO.eliminarPorId(null);
        });
    }

    @Test
    void testCRUDCompleteFlow() {
        // CREATE
        doNothing().when(em).persist(any(CompraDetalle.class));
        compraDetalleDAO.crear(compraDetalle);
        verify(em, times(1)).persist(compraDetalle);

        // READ
        when(em.find(CompraDetalle.class, testId)).thenReturn(compraDetalle);
        CompraDetalle found = compraDetalleDAO.findById(testId);
        assertNotNull(found);

        // UPDATE
        found.setEstado("INACTIVO");
        when(em.merge(any(CompraDetalle.class))).thenReturn(found);
        CompraDetalle updated = compraDetalleDAO.modificar(found);
        assertEquals("INACTIVO", updated.getEstado());

        // DELETE
        when(em.merge(any(CompraDetalle.class))).thenReturn(found);
        doNothing().when(em).remove(any(CompraDetalle.class));
        compraDetalleDAO.eliminar(found);
        verify(em, times(1)).remove(any(CompraDetalle.class));
    }

    @Test
    void testFindAll_EmptyList() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<CompraDetalle> cq = mock(CriteriaQuery.class);
        Root<CompraDetalle> root = mock(Root.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(CompraDetalle.class)).thenReturn(cq);
        when(cq.from(CompraDetalle.class)).thenReturn(root);
        when(cq.select(root)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList());

        List<CompraDetalle> result = compraDetalleDAO.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindRange_InvalidRange() {
        // findRange lanza IllegalArgumentException cuando pageSize <= 0
        assertThrows(IllegalArgumentException.class, () -> {
            compraDetalleDAO.findRange(10, 0);
        });
    }

    // Método auxiliar para crear CompraDetalle de prueba
    private CompraDetalle createTestCompraDetalle() {
        CompraDetalle detalle = new CompraDetalle();
        detalle.setId(UUID.randomUUID());
        detalle.setCantidad(new BigDecimal("2"));
        detalle.setPrecio(new BigDecimal("20.00"));
        detalle.setEstado("PENDIENTE");
        return detalle;
    }
}