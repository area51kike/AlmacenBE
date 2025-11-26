package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Proveedor;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompraDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Compra> compraQuery;

    @Mock
    private TypedQuery<Long> countQuery;

    private CompraDAO compraDAO;
    private Compra compra;
    private Proveedor proveedor;

    @BeforeEach
    void setUp() throws Exception {
        compraDAO = new CompraDAO();

        Field emField = CompraDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(compraDAO, entityManager);

        proveedor = new Proveedor();
        proveedor.setId(1);
        proveedor.setNombre("Proveedor Test");

        compra = new Compra();
        compra.setId(1L);
        compra.setIdProveedor(1);
        compra.setFecha(OffsetDateTime.now());
        compra.setEstado("PENDIENTE");
        compra.setObservaciones("Compra de prueba");
    }

    @Test
    void testGetEntityManager() {
        EntityManager result = compraDAO.getEntityManager();
        assertNotNull(result);
        assertEquals(entityManager, result);
    }

    @Test
    void testConstructor() {
        CompraDAO dao = new CompraDAO();
        assertNotNull(dao);
    }

    // ===== TESTS DE findById =====

    @Test
    void testFindById_Success() {
        Long id = 1L;
        when(entityManager.find(Compra.class, id)).thenReturn(compra);

        Compra result = compraDAO.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("PENDIENTE", result.getEstado());
        assertEquals("Compra de prueba", result.getObservaciones());
        verify(entityManager).find(Compra.class, id);
    }

    @Test
    void testFindById_NotFound() {
        Long id = 999L;
        when(entityManager.find(Compra.class, id)).thenReturn(null);

        Compra result = compraDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Compra.class, id);
    }

    @Test
    void testFindById_WithZeroId() {
        Long id = 0L;
        when(entityManager.find(Compra.class, id)).thenReturn(null);

        Compra result = compraDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Compra.class, id);
    }

    @Test
    void testFindById_WithNegativeId() {
        Long id = -1L;
        when(entityManager.find(Compra.class, id)).thenReturn(null);

        Compra result = compraDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Compra.class, id);
    }

    @Test
    void testFindById_WithProveedor() {
        compra.setProveedor(proveedor);
        Long id = 1L;

        when(entityManager.find(Compra.class, id)).thenReturn(compra);

        Compra result = compraDAO.findById(id);

        assertNotNull(result);
        assertNotNull(result.getProveedor());
        assertEquals(1, result.getProveedor().getId());
        verify(entityManager).find(Compra.class, id);
    }

    // ===== TESTS DE crear =====

    @Test
    void testCrear_Success() {
        when(entityManager.find(Proveedor.class, 1)).thenReturn(proveedor);
        doNothing().when(entityManager).persist(any(Compra.class));

        compraDAO.crear(compra);

        verify(entityManager).find(Proveedor.class, 1);
        verify(entityManager).persist(compra);
        assertEquals(proveedor, compra.getProveedor());
    }

    @Test
    void testCrear_WithNullIdProveedor() {
        compra.setIdProveedor(null);

        // La excepción IllegalArgumentException se wrappea en RuntimeException por el catch
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraDAO.crear(compra);
        });

        assertTrue(exception.getMessage().contains("obligatorio"));
        verify(entityManager, never()).persist(any());
    }

    @Test
    void testCrear_WithNonExistentProveedor() {
        when(entityManager.find(Proveedor.class, 999)).thenReturn(null);
        compra.setIdProveedor(999);

        // La excepción IllegalArgumentException se wrappea en RuntimeException por el catch
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraDAO.crear(compra);
        });

        assertTrue(exception.getMessage().contains("no existe"));
        verify(entityManager).find(Proveedor.class, 999);
        verify(entityManager, never()).persist(any());
    }

    @Test
    void testCrear_WithPersistException() {
        when(entityManager.find(Proveedor.class, 1)).thenReturn(proveedor);
        doThrow(new PersistenceException("Database error")).when(entityManager).persist(any(Compra.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraDAO.crear(compra);
        });

        assertTrue(exception.getMessage().contains("Error al crear la compra"));
        verify(entityManager).persist(compra);
    }

    @Test
    void testCrear_WithNestedExceptions() {
        when(entityManager.find(Proveedor.class, 1)).thenReturn(proveedor);

        RuntimeException causeRoot = new RuntimeException("Root cause");
        RuntimeException causeMiddle = new RuntimeException("Middle cause", causeRoot);
        PersistenceException mainException = new PersistenceException("Main exception", causeMiddle);

        doThrow(mainException).when(entityManager).persist(any(Compra.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraDAO.crear(compra);
        });

        assertTrue(exception.getMessage().contains("Root cause"));
        verify(entityManager).persist(compra);
    }

    @Test
    void testCrear_WithNullObservaciones() {
        compra.setObservaciones(null);
        when(entityManager.find(Proveedor.class, 1)).thenReturn(proveedor);
        doNothing().when(entityManager).persist(any(Compra.class));

        compraDAO.crear(compra);

        verify(entityManager).persist(compra);
        assertNull(compra.getObservaciones());
    }

    @Test
    void testCrear_WithNullFecha() {
        compra.setFecha(null);
        when(entityManager.find(Proveedor.class, 1)).thenReturn(proveedor);
        doNothing().when(entityManager).persist(any(Compra.class));

        compraDAO.crear(compra);

        verify(entityManager).persist(compra);
        assertNull(compra.getFecha());
    }

    @Test
    void testCrear_WithEmptyEstado() {
        compra.setEstado("");
        when(entityManager.find(Proveedor.class, 1)).thenReturn(proveedor);
        doNothing().when(entityManager).persist(any(Compra.class));

        compraDAO.crear(compra);

        verify(entityManager).persist(compra);
        assertEquals("", compra.getEstado());
    }

    @Test
    void testCrear_WithMaxLengthEstado() {
        compra.setEstado("0123456789"); // 10 caracteres
        when(entityManager.find(Proveedor.class, 1)).thenReturn(proveedor);
        doNothing().when(entityManager).persist(any(Compra.class));

        compraDAO.crear(compra);

        verify(entityManager).persist(compra);
        assertEquals(10, compra.getEstado().length());
    }

    @Test
    void testCrear_WithLongObservaciones() {
        String longObservaciones = "A".repeat(10000);
        compra.setObservaciones(longObservaciones);
        when(entityManager.find(Proveedor.class, 1)).thenReturn(proveedor);
        doNothing().when(entityManager).persist(any(Compra.class));

        compraDAO.crear(compra);

        verify(entityManager).persist(compra);
        assertEquals(10000, compra.getObservaciones().length());
    }

    @Test
    void testCrear_WithDifferentEstados() {
        String[] estados = {"PENDIENTE", "PAGADA", "COMPLETADA", "CANCELADA"};

        for (String estado : estados) {
            compra.setEstado(estado);
            when(entityManager.find(Proveedor.class, 1)).thenReturn(proveedor);
            doNothing().when(entityManager).persist(any(Compra.class));

            compraDAO.crear(compra);

            assertEquals(estado, compra.getEstado());
            reset(entityManager);
        }
    }

    // ===== TESTS DE eliminar =====

    @Test
    void testEliminar_WithManagedEntity() {
        when(entityManager.contains(compra)).thenReturn(true);
        doNothing().when(entityManager).remove(compra);

        compraDAO.eliminar(compra);

        verify(entityManager).remove(compra);
        verify(entityManager, never()).merge(any());
    }

    @Test
    void testEliminar_WithDetachedEntity() {
        when(entityManager.contains(compra)).thenReturn(false);
        when(entityManager.merge(compra)).thenReturn(compra);
        doNothing().when(entityManager).remove(any(Compra.class));

        compraDAO.eliminar(compra);

        verify(entityManager).merge(compra);
        verify(entityManager).remove(any(Compra.class));
    }

    @Test
    void testEliminar_WithException() {
        when(entityManager.contains(compra)).thenReturn(true);
        doThrow(new PersistenceException("Database error")).when(entityManager).remove(any(Compra.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraDAO.eliminar(compra);
        });

        assertTrue(exception.getMessage().contains("Error al eliminar la compra"));
        verify(entityManager).remove(compra);
    }

    @Test
    void testEliminar_WithNullCompra() {
        assertThrows(RuntimeException.class, () -> {
            compraDAO.eliminar(null);
        });
    }

    // ===== TESTS DE validarProveedor =====

    @Test
    void testValidarProveedor_WithValidId() {
        when(entityManager.find(Proveedor.class, 1)).thenReturn(proveedor);

        assertDoesNotThrow(() -> {
            compraDAO.validarProveedor(1);
        });

        verify(entityManager).find(Proveedor.class, 1);
    }

    @Test
    void testValidarProveedor_WithNullId() {
        assertDoesNotThrow(() -> {
            compraDAO.validarProveedor(null);
        });

        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void testValidarProveedor_WithNonExistentId() {
        when(entityManager.find(Proveedor.class, 999)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            compraDAO.validarProveedor(999);
        });

        assertTrue(exception.getMessage().contains("no existe"));
        verify(entityManager).find(Proveedor.class, 999);
    }

    @Test
    void testValidarProveedor_WithZeroId() {
        when(entityManager.find(Proveedor.class, 0)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            compraDAO.validarProveedor(0);
        });

        assertTrue(exception.getMessage().contains("no existe"));
        verify(entityManager).find(Proveedor.class, 0);
    }

    @Test
    void testValidarProveedor_WithNegativeId() {
        when(entityManager.find(Proveedor.class, -1)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            compraDAO.validarProveedor(-1);
        });

        assertTrue(exception.getMessage().contains("no existe"));
        verify(entityManager).find(Proveedor.class, -1);
    }

    // ===== TESTS DE buscarPagadasParaRecepcion =====

    @Test
    void testBuscarPagadasParaRecepcion_Success() {
        Compra compra1 = new Compra();
        compra1.setId(1L);
        compra1.setEstado("PAGADA");

        Compra compra2 = new Compra();
        compra2.setId(2L);
        compra2.setEstado("PAGADA");

        List<Compra> comprasPagadas = Arrays.asList(compra1, compra2);

        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setFirstResult(anyInt())).thenReturn(compraQuery);
        when(compraQuery.setMaxResults(anyInt())).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(comprasPagadas);

        List<Compra> result = compraDAO.buscarPagadasParaRecepcion(0, 10);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("PAGADA", result.get(0).getEstado());
        assertEquals("PAGADA", result.get(1).getEstado());
        verify(compraQuery).setFirstResult(0);
        verify(compraQuery).setMaxResults(10);
    }

    @Test
    void testBuscarPagadasParaRecepcion_EmptyList() {
        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setFirstResult(anyInt())).thenReturn(compraQuery);
        when(compraQuery.setMaxResults(anyInt())).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(Collections.emptyList());

        List<Compra> result = compraDAO.buscarPagadasParaRecepcion(0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(compraQuery).setFirstResult(0);
        verify(compraQuery).setMaxResults(10);
    }

    @Test
    void testBuscarPagadasParaRecepcion_WithPagination() {
        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setFirstResult(anyInt())).thenReturn(compraQuery);
        when(compraQuery.setMaxResults(anyInt())).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(Collections.emptyList());

        compraDAO.buscarPagadasParaRecepcion(20, 5);

        verify(compraQuery).setFirstResult(20);
        verify(compraQuery).setMaxResults(5);
    }

    @Test
    void testBuscarPagadasParaRecepcion_WithException() {
        when(entityManager.createQuery(anyString(), eq(Compra.class)))
                .thenThrow(new PersistenceException("Query error"));

        assertThrows(PersistenceException.class, () -> {
            compraDAO.buscarPagadasParaRecepcion(0, 10);
        });
    }

    // ===== TESTS DE contarPagadasParaRecepcion =====

    @Test
    void testContarPagadasParaRecepcion_Success() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(15L);

        Long result = compraDAO.contarPagadasParaRecepcion();

        assertNotNull(result);
        assertEquals(15L, result);
        verify(entityManager).createQuery(anyString(), eq(Long.class));
        verify(countQuery).getSingleResult();
    }

    @Test
    void testContarPagadasParaRecepcion_WithZeroResults() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);

        Long result = compraDAO.contarPagadasParaRecepcion();

        assertNotNull(result);
        assertEquals(0L, result);
        verify(countQuery).getSingleResult();
    }

    @Test
    void testContarPagadasParaRecepcion_WithException() {
        when(entityManager.createQuery(anyString(), eq(Long.class)))
                .thenThrow(new PersistenceException("Count query error"));

        assertThrows(PersistenceException.class, () -> {
            compraDAO.contarPagadasParaRecepcion();
        });
    }



    @Test
    void testCompra_GetIdProveedor_WithProveedor() {
        compra.setProveedor(proveedor);

        Integer idProveedor = compra.getIdProveedor();

        assertEquals(1, idProveedor);
    }

    @Test
    void testCompra_GetIdProveedor_WithNullProveedor() {
        compra.setProveedor(null);

        Integer idProveedor = compra.getIdProveedor();

        assertNull(idProveedor);
    }

    @Test
    void testCompra_SetIdProveedor_CreatesProxy() {
        compra.setIdProveedor(5);

        assertNotNull(compra.getProveedor());
        assertEquals(5, compra.getProveedor().getId());
    }

    @Test
    void testCompra_SetIdProveedor_WithNull() {
        compra.setIdProveedor(null);

        assertNull(compra.getProveedor());
    }

    @Test
    void testCompra_ToString() {
        compra.setProveedor(proveedor);

        String result = compra.toString();

        assertNotNull(result);
        assertTrue(result.contains("Compra{"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("idProveedor=1"));
        assertTrue(result.contains("estado='PENDIENTE'"));
    }

    @Test
    void testCompra_WithDifferentEstados() {
        String[] estados = {"PENDIENTE", "PAGADA", "COMPLETADA", "CANCELADA", "EN PROCESO"};

        for (String estado : estados) {
            compra.setEstado(estado);
            assertEquals(estado, compra.getEstado());
        }
    }

    @Test
    void testCompra_WithSpecialCharactersInObservaciones() {
        String specialChars = "Compra urgente: áéíóú ñÑ @#$% - Entrega 15/12/2024";
        compra.setObservaciones(specialChars);

        assertEquals(specialChars, compra.getObservaciones());
    }

    @Test
    void testCompra_WithFutureDate() {
        OffsetDateTime futureDate = OffsetDateTime.now().plusDays(30);
        compra.setFecha(futureDate);

        assertEquals(futureDate, compra.getFecha());
    }

    @Test
    void testCompra_WithPastDate() {
        OffsetDateTime pastDate = OffsetDateTime.now().minusDays(30);
        compra.setFecha(pastDate);

        assertEquals(pastDate, compra.getFecha());
    }
    // ===== TESTS DE findByEstado =====

    @Test
    void testFindByEstado_Success() {
        Compra compra1 = new Compra();
        compra1.setId(1L);
        compra1.setEstado("PENDIENTE");

        Compra compra2 = new Compra();
        compra2.setId(2L);
        compra2.setEstado("PENDIENTE");

        List<Compra> comprasPendientes = Arrays.asList(compra1, compra2);

        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setParameter(eq("estado"), eq("PENDIENTE"))).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(comprasPendientes);

        List<Compra> result = compraDAO.findByEstado("PENDIENTE");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("PENDIENTE", result.get(0).getEstado());
        assertEquals("PENDIENTE", result.get(1).getEstado());
        verify(compraQuery).setParameter("estado", "PENDIENTE");
        verify(compraQuery).getResultList();
    }

    @Test
    void testFindByEstado_EmptyList() {
        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setParameter(eq("estado"), eq("INEXISTENTE"))).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(Collections.emptyList());

        List<Compra> result = compraDAO.findByEstado("INEXISTENTE");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(compraQuery).setParameter("estado", "INEXISTENTE");
    }

    @Test
    void testFindByEstado_WithPagada() {
        Compra compra1 = new Compra();
        compra1.setId(1L);
        compra1.setEstado("PAGADA");

        List<Compra> comprasPagadas = Collections.singletonList(compra1);

        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setParameter(eq("estado"), eq("PAGADA"))).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(comprasPagadas);

        List<Compra> result = compraDAO.findByEstado("PAGADA");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PAGADA", result.get(0).getEstado());
    }

    @Test
    void testFindByEstado_WithCompletada() {
        Compra compra1 = new Compra();
        compra1.setId(1L);
        compra1.setEstado("COMPLETADA");

        List<Compra> comprasCompletadas = Collections.singletonList(compra1);

        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setParameter(eq("estado"), eq("COMPLETADA"))).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(comprasCompletadas);

        List<Compra> result = compraDAO.findByEstado("COMPLETADA");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("COMPLETADA", result.get(0).getEstado());
    }

    @Test
    void testFindByEstado_WithCancelada() {
        Compra compra1 = new Compra();
        compra1.setId(1L);
        compra1.setEstado("CANCELADA");

        List<Compra> comprasCanceladas = Collections.singletonList(compra1);

        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setParameter(eq("estado"), eq("CANCELADA"))).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(comprasCanceladas);

        List<Compra> result = compraDAO.findByEstado("CANCELADA");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CANCELADA", result.get(0).getEstado());
    }

    @Test
    void testFindByEstado_WithNullEstado() {
        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setParameter(eq("estado"), isNull())).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(Collections.emptyList());

        List<Compra> result = compraDAO.findByEstado(null);

        assertNotNull(result);
        verify(compraQuery).setParameter("estado", null);
    }

    @Test
    void testFindByEstado_WithEmptyString() {
        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setParameter(eq("estado"), eq(""))).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(Collections.emptyList());

        List<Compra> result = compraDAO.findByEstado("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(compraQuery).setParameter("estado", "");
    }

    @Test
    void testFindByEstado_WithCaseVariation() {
        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setParameter(eq("estado"), eq("pendiente"))).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(Collections.emptyList());

        List<Compra> result = compraDAO.findByEstado("pendiente");

        assertNotNull(result);
        verify(compraQuery).setParameter("estado", "pendiente");
    }

    @Test
    void testFindByEstado_WithException() {
        when(entityManager.createQuery(anyString(), eq(Compra.class)))
                .thenThrow(new PersistenceException("Query error"));

        assertThrows(PersistenceException.class, () -> {
            compraDAO.findByEstado("PENDIENTE");
        });
    }

    @Test
    void testFindByEstado_WithMultipleResults() {
        List<Compra> compras = new java.util.ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Compra c = new Compra();
            c.setId((long) i);
            c.setEstado("PAGADA");
            compras.add(c);
        }

        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setParameter(eq("estado"), eq("PAGADA"))).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(compras);

        List<Compra> result = compraDAO.findByEstado("PAGADA");

        assertNotNull(result);
        assertEquals(5, result.size());
        result.forEach(c -> assertEquals("PAGADA", c.getEstado()));
    }

// ===== TESTS DE actualizarEstado =====

    @Test
    void testActualizarEstado_Success() {
        Long idCompra = 1L;
        String nuevoEstado = "COMPLETADA";

        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class))).thenReturn(compra);

        compraDAO.actualizarEstado(idCompra, nuevoEstado);

        verify(entityManager).find(Compra.class, idCompra);
        verify(entityManager).merge(compra);
        assertEquals(nuevoEstado, compra.getEstado());
    }

    @Test
    void testActualizarEstado_FromPendienteToPagada() {
        Long idCompra = 1L;
        compra.setEstado("PENDIENTE");

        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class))).thenReturn(compra);

        compraDAO.actualizarEstado(idCompra, "PAGADA");

        assertEquals("PAGADA", compra.getEstado());
        verify(entityManager).merge(compra);
    }

    @Test
    void testActualizarEstado_FromPagadaToCompletada() {
        Long idCompra = 1L;
        compra.setEstado("PAGADA");

        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class))).thenReturn(compra);

        compraDAO.actualizarEstado(idCompra, "COMPLETADA");

        assertEquals("COMPLETADA", compra.getEstado());
        verify(entityManager).merge(compra);
    }

    @Test
    void testActualizarEstado_ToCancelada() {
        Long idCompra = 1L;
        compra.setEstado("PENDIENTE");

        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class))).thenReturn(compra);

        compraDAO.actualizarEstado(idCompra, "CANCELADA");

        assertEquals("CANCELADA", compra.getEstado());
        verify(entityManager).merge(compra);
    }

    @Test
    void testActualizarEstado_WithNonExistentId() {
        Long idCompra = 999L;
        when(entityManager.find(Compra.class, idCompra)).thenReturn(null);

        // No debería lanzar excepción, simplemente no hace nada
        assertDoesNotThrow(() -> {
            compraDAO.actualizarEstado(idCompra, "COMPLETADA");
        });

        verify(entityManager).find(Compra.class, idCompra);
        verify(entityManager, never()).merge(any());
    }

    @Test
    void testActualizarEstado_WithNullId() {
        when(entityManager.find(Compra.class, null)).thenReturn(null);

        assertDoesNotThrow(() -> {
            compraDAO.actualizarEstado(null, "COMPLETADA");
        });

        verify(entityManager, never()).merge(any());
    }

    @Test
    void testActualizarEstado_WithNullEstado() {
        Long idCompra = 1L;

        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class))).thenReturn(compra);

        compraDAO.actualizarEstado(idCompra, null);

        assertNull(compra.getEstado());
        verify(entityManager).merge(compra);
    }

    @Test
    void testActualizarEstado_WithEmptyEstado() {
        Long idCompra = 1L;

        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class))).thenReturn(compra);

        compraDAO.actualizarEstado(idCompra, "");

        assertEquals("", compra.getEstado());
        verify(entityManager).merge(compra);
    }

    @Test
    void testActualizarEstado_WithSameEstado() {
        Long idCompra = 1L;
        String estadoActual = "PENDIENTE";
        compra.setEstado(estadoActual);

        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class))).thenReturn(compra);

        compraDAO.actualizarEstado(idCompra, estadoActual);

        assertEquals(estadoActual, compra.getEstado());
        verify(entityManager).merge(compra);
    }

    @Test
    void testActualizarEstado_WithException() {
        Long idCompra = 1L;
        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class)))
                .thenThrow(new PersistenceException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraDAO.actualizarEstado(idCompra, "COMPLETADA");
        });

        assertTrue(exception.getMessage().contains("No se pudo actualizar el estado"));
        verify(entityManager).find(Compra.class, idCompra);
    }

    @Test
    void testActualizarEstado_WithLongEstado() {
        Long idCompra = 1L;
        String estadoLargo = "ESTADO_MUY_LARGO_QUE_EXCEDE_LIMITE";

        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class))).thenReturn(compra);

        compraDAO.actualizarEstado(idCompra, estadoLargo);

        assertEquals(estadoLargo, compra.getEstado());
        verify(entityManager).merge(compra);
    }

    @Test
    void testActualizarEstado_MultipleUpdates() {
        Long idCompra = 1L;
        String[] estados = {"PENDIENTE", "PAGADA", "COMPLETADA"};

        for (String estado : estados) {
            when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
            when(entityManager.merge(any(Compra.class))).thenReturn(compra);

            compraDAO.actualizarEstado(idCompra, estado);

            assertEquals(estado, compra.getEstado());
            reset(entityManager);
        }
    }

    @Test
    void testActualizarEstado_WithSpecialCharacters() {
        Long idCompra = 1L;
        String estadoEspecial = "ESTADO-CON_CARACTERES@ESPECIALES";

        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class))).thenReturn(compra);

        compraDAO.actualizarEstado(idCompra, estadoEspecial);

        assertEquals(estadoEspecial, compra.getEstado());
        verify(entityManager).merge(compra);
    }

    @Test
    void testActualizarEstado_WithZeroId() {
        Long idCompra = 0L;
        when(entityManager.find(Compra.class, idCompra)).thenReturn(null);

        assertDoesNotThrow(() -> {
            compraDAO.actualizarEstado(idCompra, "COMPLETADA");
        });

        verify(entityManager).find(Compra.class, idCompra);
        verify(entityManager, never()).merge(any());
    }

    @Test
    void testActualizarEstado_WithNegativeId() {
        Long idCompra = -1L;
        when(entityManager.find(Compra.class, idCompra)).thenReturn(null);

        assertDoesNotThrow(() -> {
            compraDAO.actualizarEstado(idCompra, "COMPLETADA");
        });

        verify(entityManager).find(Compra.class, idCompra);
        verify(entityManager, never()).merge(any());
    }

    @Test
    void testActualizarEstado_WithTransactionException() {
        Long idCompra = 1L;
        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class)))
                .thenThrow(new jakarta.persistence.RollbackException("Transaction rolled back"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraDAO.actualizarEstado(idCompra, "COMPLETADA");
        });

        assertTrue(exception.getMessage().contains("No se pudo actualizar el estado"));
        verify(entityManager).merge(compra);
    }
    // ===== TESTS ADICIONALES PARA MEJORAR COBERTURA =====

// Tests adicionales para el método crear() - cubrir más ramas

    @Test
    void testCrear_WithProveedorAlreadySet() {
        compra.setProveedor(proveedor);
        when(entityManager.find(Proveedor.class, 1)).thenReturn(proveedor);
        doNothing().when(entityManager).persist(any(Compra.class));

        compraDAO.crear(compra);

        verify(entityManager).find(Proveedor.class, 1);
        verify(entityManager).persist(compra);
        assertEquals(proveedor, compra.getProveedor());
    }

    @Test
    void testCrear_WithExceptionDuringProveedorFind() {
        when(entityManager.find(Proveedor.class, 1))
                .thenThrow(new PersistenceException("Database connection error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraDAO.crear(compra);
        });

        assertTrue(exception.getMessage().contains("Error al crear la compra"));
        verify(entityManager).find(Proveedor.class, 1);
        verify(entityManager, never()).persist(any());
    }

    @Test
    void testCrear_WithNullProveedorButValidId() {
        compra.setProveedor(null);
        compra.setIdProveedor(1);
        when(entityManager.find(Proveedor.class, 1)).thenReturn(proveedor);
        doNothing().when(entityManager).persist(any(Compra.class));

        compraDAO.crear(compra);

        verify(entityManager).find(Proveedor.class, 1);
        verify(entityManager).persist(compra);
        assertNotNull(compra.getProveedor());
        assertEquals(proveedor, compra.getProveedor());
    }

    @Test
    void testCrear_WithSingleLevelException() {
        when(entityManager.find(Proveedor.class, 1)).thenReturn(proveedor);
        RuntimeException simpleException = new RuntimeException("Simple error");
        doThrow(simpleException).when(entityManager).persist(any(Compra.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraDAO.crear(compra);
        });

        assertTrue(exception.getMessage().contains("Simple error"));
        verify(entityManager).persist(compra);
    }

    @Test
    void testCrear_WithDeepNestedExceptions() {
        when(entityManager.find(Proveedor.class, 1)).thenReturn(proveedor);

        RuntimeException level4 = new RuntimeException("Level 4 cause");
        RuntimeException level3 = new RuntimeException("Level 3", level4);
        RuntimeException level2 = new RuntimeException("Level 2", level3);
        RuntimeException level1 = new RuntimeException("Level 1", level2);
        PersistenceException topException = new PersistenceException("Top exception", level1);

        doThrow(topException).when(entityManager).persist(any(Compra.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraDAO.crear(compra);
        });

        assertTrue(exception.getMessage().contains("Level 4 cause"));
        verify(entityManager).persist(compra);
    }

// Tests adicionales para eliminar() - cubrir más escenarios

    @Test
    void testEliminar_WithNullId() {
        Compra compraSinId = new Compra();
        compraSinId.setEstado("PENDIENTE");

        when(entityManager.contains(compraSinId)).thenReturn(true);
        doNothing().when(entityManager).remove(compraSinId);

        assertDoesNotThrow(() -> {
            compraDAO.eliminar(compraSinId);
        });

        verify(entityManager).remove(compraSinId);
    }

    @Test
    void testEliminar_WithDetachedEntityAndException() {
        when(entityManager.contains(compra)).thenReturn(false);
        when(entityManager.merge(compra)).thenReturn(compra);
        doThrow(new PersistenceException("Cannot remove")).when(entityManager).remove(any(Compra.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraDAO.eliminar(compra);
        });

        assertTrue(exception.getMessage().contains("Error al eliminar la compra"));
        verify(entityManager).merge(compra);
    }

    @Test
    void testEliminar_WithIllegalStateException() {
        when(entityManager.contains(compra)).thenReturn(true);
        doThrow(new IllegalStateException("Entity manager closed")).when(entityManager).remove(any(Compra.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraDAO.eliminar(compra);
        });

        assertTrue(exception.getMessage().contains("Error al eliminar la compra"));
    }

    @Test
    void testEliminar_WithTransactionException() {
        when(entityManager.contains(compra)).thenReturn(true);
        doThrow(new jakarta.persistence.TransactionRequiredException("No transaction"))
                .when(entityManager).remove(any(Compra.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraDAO.eliminar(compra);
        });

        assertTrue(exception.getMessage().contains("Error al eliminar la compra"));
    }

// Tests adicionales para findById() - cubrir más casos

    @Test
    void testFindById_WithStringId() {
        String stringId = "1";
        when(entityManager.find(Compra.class, stringId)).thenReturn(compra);

        Compra result = compraDAO.findById(stringId);

        assertNotNull(result);
        verify(entityManager).find(Compra.class, stringId);
    }

    @Test
    void testFindById_WithLargeId() {
        Long largeId = Long.MAX_VALUE;
        when(entityManager.find(Compra.class, largeId)).thenReturn(null);

        Compra result = compraDAO.findById(largeId);

        assertNull(result);
        verify(entityManager).find(Compra.class, largeId);
    }

// Tests adicionales para validarProveedor()

    @Test
    void testValidarProveedor_WithValidProveedorButNullNombre() {
        Proveedor proveedorSinNombre = new Proveedor();
        proveedorSinNombre.setId(1);
        proveedorSinNombre.setNombre(null);

        when(entityManager.find(Proveedor.class, 1)).thenReturn(proveedorSinNombre);

        assertDoesNotThrow(() -> {
            compraDAO.validarProveedor(1);
        });

        verify(entityManager).find(Proveedor.class, 1);
    }

    @Test
    void testValidarProveedor_WithMaxIntegerId() {
        Integer maxId = Integer.MAX_VALUE;
        when(entityManager.find(Proveedor.class, maxId)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            compraDAO.validarProveedor(maxId);
        });

        assertTrue(exception.getMessage().contains("no existe"));
        verify(entityManager).find(Proveedor.class, maxId);
    }

    @Test
    void testValidarProveedor_WithMinIntegerId() {
        Integer minId = Integer.MIN_VALUE;
        when(entityManager.find(Proveedor.class, minId)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            compraDAO.validarProveedor(minId);
        });

        assertTrue(exception.getMessage().contains("no existe"));
        verify(entityManager).find(Proveedor.class, minId);
    }

// Tests adicionales para buscarPagadasParaRecepcion()

    @Test
    void testBuscarPagadasParaRecepcion_WithNegativeFirst() {
        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setFirstResult(anyInt())).thenReturn(compraQuery);
        when(compraQuery.setMaxResults(anyInt())).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(Collections.emptyList());

        List<Compra> result = compraDAO.buscarPagadasParaRecepcion(-1, 10);

        verify(compraQuery).setFirstResult(-1);
        verify(compraQuery).setMaxResults(10);
    }

    @Test
    void testBuscarPagadasParaRecepcion_WithZeroMax() {
        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setFirstResult(anyInt())).thenReturn(compraQuery);
        when(compraQuery.setMaxResults(anyInt())).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(Collections.emptyList());

        List<Compra> result = compraDAO.buscarPagadasParaRecepcion(0, 0);

        verify(compraQuery).setMaxResults(0);
    }

    @Test
    void testBuscarPagadasParaRecepcion_WithLargeNumbers() {
        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setFirstResult(anyInt())).thenReturn(compraQuery);
        when(compraQuery.setMaxResults(anyInt())).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(Collections.emptyList());

        List<Compra> result = compraDAO.buscarPagadasParaRecepcion(1000000, 50000);

        verify(compraQuery).setFirstResult(1000000);
        verify(compraQuery).setMaxResults(50000);
    }

    @Test
    void testBuscarPagadasParaRecepcion_WithSingleResult() {
        Compra singleCompra = new Compra();
        singleCompra.setId(1L);
        singleCompra.setEstado("PAGADA");

        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setFirstResult(anyInt())).thenReturn(compraQuery);
        when(compraQuery.setMaxResults(anyInt())).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(Collections.singletonList(singleCompra));

        List<Compra> result = compraDAO.buscarPagadasParaRecepcion(0, 1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PAGADA", result.get(0).getEstado());
    }

    @Test
    void testBuscarPagadasParaRecepcion_WithIllegalArgumentException() {
        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setFirstResult(anyInt())).thenReturn(compraQuery);
        when(compraQuery.setMaxResults(anyInt())).thenThrow(new IllegalArgumentException("Invalid max results"));

        assertThrows(IllegalArgumentException.class, () -> {
            compraDAO.buscarPagadasParaRecepcion(0, -1);
        });
    }

// Tests adicionales para contarPagadasParaRecepcion()

    @Test
    void testContarPagadasParaRecepcion_WithLargeCount() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(999999L);

        Long result = compraDAO.contarPagadasParaRecepcion();

        assertEquals(999999L, result);
    }

    @Test
    void testContarPagadasParaRecepcion_WithNullResult() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(null);

        Long result = compraDAO.contarPagadasParaRecepcion();

        assertNull(result);
    }

    @Test
    void testContarPagadasParaRecepcion_WithNoResultException() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenThrow(new jakarta.persistence.NoResultException("No results"));

        assertThrows(jakarta.persistence.NoResultException.class, () -> {
            compraDAO.contarPagadasParaRecepcion();
        });
    }

    @Test
    void testContarPagadasParaRecepcion_WithNonUniqueResultException() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenThrow(new jakarta.persistence.NonUniqueResultException("Multiple results"));

        assertThrows(jakarta.persistence.NonUniqueResultException.class, () -> {
            compraDAO.contarPagadasParaRecepcion();
        });
    }

// Tests adicionales para findByEstado()

    @Test
    void testFindByEstado_WithWhitespaceEstado() {
        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setParameter(eq("estado"), eq("  "))).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(Collections.emptyList());

        List<Compra> result = compraDAO.findByEstado("  ");

        assertNotNull(result);
        verify(compraQuery).setParameter("estado", "  ");
    }

    @Test
    void testFindByEstado_WithVeryLongEstado() {
        String longEstado = "A".repeat(1000);
        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setParameter(eq("estado"), eq(longEstado))).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(Collections.emptyList());

        List<Compra> result = compraDAO.findByEstado(longEstado);

        assertNotNull(result);
        verify(compraQuery).setParameter("estado", longEstado);
    }

    @Test
    void testFindByEstado_WithSpecialCharacters() {
        String specialEstado = "ESTADO@#$%&*()";
        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setParameter(eq("estado"), eq(specialEstado))).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(Collections.emptyList());

        List<Compra> result = compraDAO.findByEstado(specialEstado);

        assertNotNull(result);
        verify(compraQuery).setParameter("estado", specialEstado);
    }

    @Test
    void testFindByEstado_WithQueryTimeoutException() {
        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setParameter(eq("estado"), anyString())).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenThrow(new jakarta.persistence.QueryTimeoutException("Query timeout"));

        assertThrows(jakarta.persistence.QueryTimeoutException.class, () -> {
            compraDAO.findByEstado("PENDIENTE");
        });
    }

    @Test
    void testFindByEstado_WithMaxResults() {
        List<Compra> compras = new java.util.ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            Compra c = new Compra();
            c.setId((long) i);
            c.setEstado("PAGADA");
            compras.add(c);
        }

        when(entityManager.createQuery(anyString(), eq(Compra.class))).thenReturn(compraQuery);
        when(compraQuery.setParameter(eq("estado"), eq("PAGADA"))).thenReturn(compraQuery);
        when(compraQuery.getResultList()).thenReturn(compras);

        List<Compra> result = compraDAO.findByEstado("PAGADA");

        assertNotNull(result);
        assertEquals(100, result.size());
    }

// Tests adicionales para actualizarEstado()

    @Test
    void testActualizarEstado_WithVeryLongEstado() {
        Long idCompra = 1L;
        String veryLongEstado = "E".repeat(10000);

        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class))).thenReturn(compra);

        compraDAO.actualizarEstado(idCompra, veryLongEstado);

        assertEquals(veryLongEstado, compra.getEstado());
        verify(entityManager).merge(compra);
    }

    @Test
    void testActualizarEstado_WithWhitespaceEstado() {
        Long idCompra = 1L;
        String whitespaceEstado = "   ";

        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class))).thenReturn(compra);

        compraDAO.actualizarEstado(idCompra, whitespaceEstado);

        assertEquals(whitespaceEstado, compra.getEstado());
        verify(entityManager).merge(compra);
    }

    @Test
    void testActualizarEstado_WithOptimisticLockException() {
        Long idCompra = 1L;
        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class)))
                .thenThrow(new jakarta.persistence.OptimisticLockException("Optimistic lock failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraDAO.actualizarEstado(idCompra, "COMPLETADA");
        });

        assertTrue(exception.getMessage().contains("No se pudo actualizar el estado"));
        verify(entityManager).merge(compra);
    }

    @Test
    void testActualizarEstado_WithPessimisticLockException() {
        Long idCompra = 1L;
        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class)))
                .thenThrow(new jakarta.persistence.PessimisticLockException("Pessimistic lock failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraDAO.actualizarEstado(idCompra, "COMPLETADA");
        });

        assertTrue(exception.getMessage().contains("No se pudo actualizar el estado"));
        verify(entityManager).merge(compra);
    }

    @Test
    void testActualizarEstado_FromCanceladaToAnyState() {
        Long idCompra = 1L;
        compra.setEstado("CANCELADA");

        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class))).thenReturn(compra);

        compraDAO.actualizarEstado(idCompra, "PENDIENTE");

        assertEquals("PENDIENTE", compra.getEstado());
        verify(entityManager).merge(compra);
    }

    @Test
    void testActualizarEstado_FromCompletadaToAnyState() {
        Long idCompra = 1L;
        compra.setEstado("COMPLETADA");

        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class))).thenReturn(compra);

        compraDAO.actualizarEstado(idCompra, "CANCELADA");

        assertEquals("CANCELADA", compra.getEstado());
        verify(entityManager).merge(compra);
    }

    @Test
    void testActualizarEstado_WithMaxLongId() {
        Long idCompra = Long.MAX_VALUE;
        when(entityManager.find(Compra.class, idCompra)).thenReturn(null);

        assertDoesNotThrow(() -> {
            compraDAO.actualizarEstado(idCompra, "COMPLETADA");
        });

        verify(entityManager).find(Compra.class, idCompra);
        verify(entityManager, never()).merge(any());
    }

    @Test
    void testActualizarEstado_WithMinLongId() {
        Long idCompra = Long.MIN_VALUE;
        when(entityManager.find(Compra.class, idCompra)).thenReturn(null);

        assertDoesNotThrow(() -> {
            compraDAO.actualizarEstado(idCompra, "COMPLETADA");
        });

        verify(entityManager).find(Compra.class, idCompra);
        verify(entityManager, never()).merge(any());
    }

    @Test
    void testActualizarEstado_WithUnicodeCharacters() {
        Long idCompra = 1L;
        String unicodeEstado = "ESTADO_🚀_测试_العربية";

        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class))).thenReturn(compra);

        compraDAO.actualizarEstado(idCompra, unicodeEstado);

        assertEquals(unicodeEstado, compra.getEstado());
        verify(entityManager).merge(compra);
    }

    @Test
    void testActualizarEstado_WithNewlineCharacters() {
        Long idCompra = 1L;
        String estadoWithNewlines = "ESTADO\nCON\nSALTOS";

        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class))).thenReturn(compra);

        compraDAO.actualizarEstado(idCompra, estadoWithNewlines);

        assertEquals(estadoWithNewlines, compra.getEstado());
        verify(entityManager).merge(compra);
    }

    @Test
    void testActualizarEstado_WithEntityNotFoundException() {
        Long idCompra = 1L;
        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class)))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Entity not found"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraDAO.actualizarEstado(idCompra, "COMPLETADA");
        });

        assertTrue(exception.getMessage().contains("No se pudo actualizar el estado"));
        verify(entityManager).merge(compra);
    }

// Tests para combinaciones de estados

    @Test
    void testActualizarEstado_AllStateTransitions() {
        Long idCompra = 1L;
        String[] estados = {"PENDIENTE", "PAGADA", "COMPLETADA", "CANCELADA", "EN_PROCESO", "RECHAZADA"};

        for (String estadoOrigen : estados) {
            for (String estadoDestino : estados) {
                compra.setEstado(estadoOrigen);
                when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
                when(entityManager.merge(any(Compra.class))).thenReturn(compra);

                compraDAO.actualizarEstado(idCompra, estadoDestino);

                assertEquals(estadoDestino, compra.getEstado());
                reset(entityManager);
            }
        }
    }

    @Test
    void testActualizarEstado_RapidSuccessiveUpdates() {
        Long idCompra = 1L;

        when(entityManager.find(Compra.class, idCompra)).thenReturn(compra);
        when(entityManager.merge(any(Compra.class))).thenReturn(compra);

        compraDAO.actualizarEstado(idCompra, "PENDIENTE");
        compraDAO.actualizarEstado(idCompra, "PAGADA");
        compraDAO.actualizarEstado(idCompra, "COMPLETADA");

        assertEquals("COMPLETADA", compra.getEstado());
        verify(entityManager, times(3)).merge(compra);
    }
}