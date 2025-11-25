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
}