package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KardexDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Kardex> typedQuery;

    @InjectMocks
    private KardexDAO kardexDAO;

    private Kardex kardex;
    private Producto producto;
    private Almacen almacen;
    private CompraDetalle compraDetalle;
    private VentaDetalle ventaDetalle;
    private UUID testUUID;
    private OffsetDateTime testFecha;

    @BeforeEach
    void setUp() throws Exception {
        kardexDAO = new KardexDAO();

        // Usar reflexión para asignar el EntityManager
        java.lang.reflect.Field emField = KardexDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(kardexDAO, entityManager);

        testUUID = UUID.randomUUID();
        testFecha = OffsetDateTime.now();

        // Configurar Producto
        producto = new Producto();
        producto.setId(testUUID);
        producto.setNombreProducto("Producto Test");

        // Configurar Almacen
        almacen = new Almacen();
        almacen.setId(1);
        almacen.setObservaciones("Almacén principal");

        // Configurar CompraDetalle
        compraDetalle = new CompraDetalle();
        compraDetalle.setId(UUID.randomUUID());

        // Configurar VentaDetalle
        ventaDetalle = new VentaDetalle();
        ventaDetalle.setId(UUID.randomUUID());

        // Configurar Kardex
        kardex = new Kardex();
        kardex.setId(testUUID);
        kardex.setIdProducto(producto);
        kardex.setIdAlmacen(almacen);
        kardex.setFecha(testFecha);
        kardex.setTipoMovimiento("ENTRADA");
        kardex.setCantidad(new BigDecimal("100.00"));
        kardex.setPrecio(new BigDecimal("50.00"));
        kardex.setCantidadActual(new BigDecimal("100.00"));
        kardex.setPrecioActual(new BigDecimal("50.00"));
    }

    // ==================== Tests para getEntityManager ====================
    @Test
    void testGetEntityManager_ReturnsEntityManager() {
        EntityManager result = kardexDAO.getEntityManager();
        assertNotNull(result);
        assertEquals(entityManager, result);
    }

    // ==================== Tests para Constructor ====================
    @Test
    void testConstructor_CreatesInstance() {
        KardexDAO dao = new KardexDAO();
        assertNotNull(dao);
    }

    // ==================== Tests para findById ====================
    @Test
    void testFindById_WithValidUUID_ReturnsKardex() {
        when(entityManager.find(Kardex.class, testUUID)).thenReturn(kardex);

        Kardex result = kardexDAO.findById(testUUID);

        assertNotNull(result);
        assertEquals(testUUID, result.getId());
        assertEquals("ENTRADA", result.getTipoMovimiento());
        verify(entityManager).find(Kardex.class, testUUID);
    }

    @Test
    void testFindById_WithNonUUID_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kardexDAO.findById(123);
        });
        assertTrue(exception.getMessage().contains("UUID"));
    }

    @Test
    void testFindById_WithNullId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            kardexDAO.findById(null);
        });
    }

    @Test
    void testFindById_WithNonExistentId_ReturnsNull() {
        UUID nonExistentId = UUID.randomUUID();
        when(entityManager.find(Kardex.class, nonExistentId)).thenReturn(null);

        Kardex result = kardexDAO.findById(nonExistentId);

        assertNull(result);
        verify(entityManager).find(Kardex.class, nonExistentId);
    }

    // ==================== Tests para crear ====================
    @Test
    void testCrear_WithValidKardex_CreatesSuccessfully() {
        // Configurar mocks
        doNothing().when(entityManager).persist(any(Kardex.class));

        // Ejecutar
        kardexDAO.crear(kardex);

        // Verificar
        verify(entityManager).persist(kardex);
        assertNotNull(kardex.getFecha());
    }

    @Test
    void testCrear_WithNullProducto_ThrowsException() {
        kardex.setIdProducto(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            kardexDAO.crear(kardex);
        });

        assertTrue(exception.getMessage().contains("producto") ||
                exception.getMessage().contains("obligatorio"));
        verify(entityManager, never()).persist(any());
    }

    @Test
    void testCrear_WithNullTipoMovimiento_ThrowsException() {
        kardex.setTipoMovimiento(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            kardexDAO.crear(kardex);
        });

        assertTrue(exception.getMessage().contains("tipo de movimiento") ||
                exception.getMessage().contains("obligatorio"));
    }

    @Test
    void testCrear_WithBlankTipoMovimiento_ThrowsException() {
        kardex.setTipoMovimiento("   ");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            kardexDAO.crear(kardex);
        });

        assertTrue(exception.getMessage().contains("tipo de movimiento"));
    }

    @Test
    void testCrear_WithNullCantidad_ThrowsException() {
        kardex.setCantidad(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            kardexDAO.crear(kardex);
        });

        assertTrue(exception.getMessage().contains("cantidad"));
    }

    @Test
    void testCrear_WithNullFecha_SetsFechaAutomatically() {
        kardex.setFecha(null);
        doNothing().when(entityManager).persist(any(Kardex.class));

        kardexDAO.crear(kardex);

        assertNotNull(kardex.getFecha());
        verify(entityManager).persist(kardex);
    }

    @Test
    void testCrear_WithDatabaseException_ThrowsRuntimeException() {
        doThrow(new RuntimeException("Database error")).when(entityManager).persist(any(Kardex.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            kardexDAO.crear(kardex);
        });

        assertTrue(exception.getMessage().contains("kardex") ||
                exception.getMessage().contains("Database error"));
    }

    // ==================== Tests para eliminar ====================
    @Test
    void testEliminar_WithManagedEntity_DeletesSuccessfully() {
        when(entityManager.contains(kardex)).thenReturn(true);
        doNothing().when(entityManager).remove(kardex);

        kardexDAO.eliminar(kardex);

        verify(entityManager).remove(kardex);
    }

    @Test
    void testEliminar_WithDetachedEntity_MergesAndDeletes() {
        when(entityManager.contains(kardex)).thenReturn(false);
        when(entityManager.merge(kardex)).thenReturn(kardex);
        doNothing().when(entityManager).remove(kardex);

        kardexDAO.eliminar(kardex);

        verify(entityManager).merge(kardex);
        verify(entityManager).remove(kardex);
    }

    @Test
    void testEliminar_WithDatabaseException_ThrowsRuntimeException() {
        when(entityManager.contains(kardex)).thenReturn(true);
        doThrow(new RuntimeException("Database error")).when(entityManager).remove(any(Kardex.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            kardexDAO.eliminar(kardex);
        });

        assertTrue(exception.getMessage().contains("kardex") ||
                exception.getMessage().contains("Database error"));
    }

    // ==================== Tests para findByProducto ====================
    @Test
    void testFindByProducto_WithValidUUID_ReturnsListOfKardex() {
        String expectedQuery = "SELECT k FROM Kardex k WHERE k.idProducto.id = :idProducto ORDER BY k.fecha DESC";
        List<Kardex> expectedList = Arrays.asList(kardex);

        when(entityManager.createQuery(eq(expectedQuery), eq(Kardex.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idProducto"), eq(testUUID))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<Kardex> result = kardexDAO.findByProducto(testUUID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUUID, result.get(0).getId());
        verify(typedQuery).setParameter("idProducto", testUUID);
    }

    @Test
    void testFindByProducto_WithValidUUID_ReturnsEmptyList() {
        String expectedQuery = "SELECT k FROM Kardex k WHERE k.idProducto.id = :idProducto ORDER BY k.fecha DESC";

        when(entityManager.createQuery(eq(expectedQuery), eq(Kardex.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idProducto"), eq(testUUID))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(new ArrayList<>());

        List<Kardex> result = kardexDAO.findByProducto(testUUID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByProducto_WithNullUUID_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kardexDAO.findByProducto(null);
        });
        assertTrue(exception.getMessage().contains("producto"));
    }

    @Test
    void testFindByProducto_WithDatabaseException_ThrowsRuntimeException() {
        when(entityManager.createQuery(anyString(), eq(Kardex.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            kardexDAO.findByProducto(testUUID);
        });
    }

    // ==================== Tests para findByTipoMovimiento ====================
    @Test
    void testFindByTipoMovimiento_WithValidTipo_ReturnsListOfKardex() {
        String tipo = "ENTRADA";
        String expectedQuery = "SELECT k FROM Kardex k WHERE k.tipoMovimiento = :tipo ORDER BY k.fecha DESC";
        List<Kardex> expectedList = Arrays.asList(kardex);

        when(entityManager.createQuery(eq(expectedQuery), eq(Kardex.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("tipo"), eq(tipo))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<Kardex> result = kardexDAO.findByTipoMovimiento(tipo);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ENTRADA", result.get(0).getTipoMovimiento());
    }

    @Test
    void testFindByTipoMovimiento_WithNullTipo_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kardexDAO.findByTipoMovimiento(null);
        });
        assertTrue(exception.getMessage().contains("tipo de movimiento"));
    }

    @Test
    void testFindByTipoMovimiento_WithBlankTipo_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kardexDAO.findByTipoMovimiento("   ");
        });
        assertTrue(exception.getMessage().contains("tipo de movimiento"));
    }

    @Test
    void testFindByTipoMovimiento_WithDatabaseException_ThrowsRuntimeException() {
        when(entityManager.createQuery(anyString(), eq(Kardex.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            kardexDAO.findByTipoMovimiento("ENTRADA");
        });
    }

    // ==================== Tests para findByRangoFechas ====================
    @Test
    void testFindByRangoFechas_WithValidRange_ReturnsListOfKardex() {
        OffsetDateTime inicio = testFecha.minusDays(1);
        OffsetDateTime fin = testFecha.plusDays(1);
        String expectedQuery = "SELECT k FROM Kardex k WHERE k.fecha BETWEEN :inicio AND :fin ORDER BY k.fecha DESC";
        List<Kardex> expectedList = Arrays.asList(kardex);

        when(entityManager.createQuery(eq(expectedQuery), eq(Kardex.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("inicio"), eq(inicio))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("fin"), eq(fin))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<Kardex> result = kardexDAO.findByRangoFechas(inicio, fin);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(typedQuery).setParameter("inicio", inicio);
        verify(typedQuery).setParameter("fin", fin);
    }

    @Test
    void testFindByRangoFechas_WithNullStartDate_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            kardexDAO.findByRangoFechas(null, testFecha);
        });
    }

    @Test
    void testFindByRangoFechas_WithNullEndDate_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            kardexDAO.findByRangoFechas(testFecha, null);
        });
    }

    @Test
    void testFindByRangoFechas_WithInvalidRange_ThrowsException() {
        OffsetDateTime inicio = testFecha.plusDays(1);
        OffsetDateTime fin = testFecha.minusDays(1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kardexDAO.findByRangoFechas(inicio, fin);
        });
        assertTrue(exception.getMessage().contains("anterior"));
    }

    // ==================== Tests para findByAlmacen ====================
    @Test
    void testFindByAlmacen_WithValidId_ReturnsListOfKardex() {
        Integer idAlmacen = 1;
        String expectedQuery = "SELECT k FROM Kardex k WHERE k.idAlmacen.id = :idAlmacen ORDER BY k.fecha DESC";
        List<Kardex> expectedList = Arrays.asList(kardex);

        when(entityManager.createQuery(eq(expectedQuery), eq(Kardex.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idAlmacen"), eq(idAlmacen))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<Kardex> result = kardexDAO.findByAlmacen(idAlmacen);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(typedQuery).setParameter("idAlmacen", idAlmacen);
    }

    @Test
    void testFindByAlmacen_WithNullId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kardexDAO.findByAlmacen(null);
        });
        assertTrue(exception.getMessage().contains("almacén"));
    }

    // ==================== Tests para findByCompraDetalle ====================
    @Test
    void testFindByCompraDetalle_WithValidUUID_ReturnsListOfKardex() {
        UUID idCompraDetalle = compraDetalle.getId();
        kardex.setIdCompraDetalle(compraDetalle);
        String expectedQuery = "SELECT k FROM Kardex k WHERE k.idCompraDetalle.id = :idCompraDetalle ORDER BY k.fecha DESC";
        List<Kardex> expectedList = Arrays.asList(kardex);

        when(entityManager.createQuery(eq(expectedQuery), eq(Kardex.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idCompraDetalle"), eq(idCompraDetalle))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<Kardex> result = kardexDAO.findByCompraDetalle(idCompraDetalle);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testFindByCompraDetalle_WithNullUUID_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kardexDAO.findByCompraDetalle(null);
        });
        assertTrue(exception.getMessage().contains("detalle de compra"));
    }

    // ==================== Tests para findByVentaDetalle ====================
    @Test
    void testFindByVentaDetalle_WithValidUUID_ReturnsListOfKardex() {
        UUID idVentaDetalle = ventaDetalle.getId();
        kardex.setIdVentaDetalle(ventaDetalle);
        String expectedQuery = "SELECT k FROM Kardex k WHERE k.idVentaDetalle.id = :idVentaDetalle ORDER BY k.fecha DESC";
        List<Kardex> expectedList = Arrays.asList(kardex);

        when(entityManager.createQuery(eq(expectedQuery), eq(Kardex.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idVentaDetalle"), eq(idVentaDetalle))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<Kardex> result = kardexDAO.findByVentaDetalle(idVentaDetalle);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testFindByVentaDetalle_WithNullUUID_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kardexDAO.findByVentaDetalle(null);
        });
        assertTrue(exception.getMessage().contains("detalle de venta"));
    }

    // ==================== Tests para findUltimoMovimiento ====================
    @Test
    void testFindUltimoMovimiento_WithProductoAndAlmacen_ReturnsKardex() {
        Integer idAlmacen = 1;
        String expectedQuery = "SELECT k FROM Kardex k WHERE k.idProducto.id = :idProducto " +
                "AND k.idAlmacen.id = :idAlmacen ORDER BY k.fecha DESC";
        List<Kardex> expectedList = Arrays.asList(kardex);

        when(entityManager.createQuery(eq(expectedQuery), eq(Kardex.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idProducto"), eq(testUUID))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idAlmacen"), eq(idAlmacen))).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(1)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        Kardex result = kardexDAO.findUltimoMovimiento(testUUID, idAlmacen);

        assertNotNull(result);
        assertEquals(testUUID, result.getId());
        verify(typedQuery).setMaxResults(1);
    }

    @Test
    void testFindUltimoMovimiento_WithProductoOnly_ReturnsKardex() {
        String expectedQuery = "SELECT k FROM Kardex k WHERE k.idProducto.id = :idProducto ORDER BY k.fecha DESC";
        List<Kardex> expectedList = Arrays.asList(kardex);

        when(entityManager.createQuery(eq(expectedQuery), eq(Kardex.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idProducto"), eq(testUUID))).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(1)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        Kardex result = kardexDAO.findUltimoMovimiento(testUUID, null);

        assertNotNull(result);
        assertEquals(testUUID, result.getId());
    }

    @Test
    void testFindUltimoMovimiento_WithNoResults_ReturnsNull() {
        when(entityManager.createQuery(anyString(), eq(Kardex.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(1)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of());

        Kardex result = kardexDAO.findUltimoMovimiento(testUUID, null);

        assertNull(result);
    }

    @Test
    void testFindUltimoMovimiento_WithNullProducto_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kardexDAO.findUltimoMovimiento(null, 1);
        });
        assertTrue(exception.getMessage().contains("producto"));
    }

    // ==================== Tests para validarEntidadesRelacionadas ====================
    @Test
    void testValidarEntidadesRelacionadas_WithAllValidEntities_NoException() {
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);
        when(entityManager.find(Almacen.class, 1)).thenReturn(almacen);

        assertDoesNotThrow(() -> {
            kardexDAO.validarEntidadesRelacionadas(kardex);
        });

        verify(entityManager).find(Producto.class, testUUID);
        verify(entityManager).find(Almacen.class, 1);
    }

    @Test
    void testValidarEntidadesRelacionadas_WithInvalidProducto_ThrowsException() {
        when(entityManager.find(Producto.class, testUUID)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kardexDAO.validarEntidadesRelacionadas(kardex);
        });

        assertTrue(exception.getMessage().contains("producto"));
    }

    @Test
    void testValidarEntidadesRelacionadas_WithInvalidAlmacen_ThrowsException() {
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);
        when(entityManager.find(Almacen.class, 1)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kardexDAO.validarEntidadesRelacionadas(kardex);
        });

        assertTrue(exception.getMessage().contains("almacén"));
    }

    @Test
    void testValidarEntidadesRelacionadas_WithInvalidCompraDetalle_ThrowsException() {
        kardex.setIdCompraDetalle(compraDetalle);
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);
        when(entityManager.find(Almacen.class, 1)).thenReturn(almacen);
        when(entityManager.find(CompraDetalle.class, compraDetalle.getId())).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kardexDAO.validarEntidadesRelacionadas(kardex);
        });

        assertTrue(exception.getMessage().contains("detalle de compra"));
    }

    @Test
    void testValidarEntidadesRelacionadas_WithInvalidVentaDetalle_ThrowsException() {
        kardex.setIdVentaDetalle(ventaDetalle);
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);
        when(entityManager.find(Almacen.class, 1)).thenReturn(almacen);
        when(entityManager.find(VentaDetalle.class, ventaDetalle.getId())).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kardexDAO.validarEntidadesRelacionadas(kardex);
        });

        assertTrue(exception.getMessage().contains("detalle de venta"));
    }

    @Test
    void testValidarEntidadesRelacionadas_WithNullEntities_NoException() {
        Kardex kardexVacio = new Kardex();

        assertDoesNotThrow(() -> {
            kardexDAO.validarEntidadesRelacionadas(kardexVacio);
        });
    }

    @Test
    void testValidarEntidadesRelacionadas_WithAllEntities_ValidatesAll() {
        kardex.setIdCompraDetalle(compraDetalle);
        kardex.setIdVentaDetalle(ventaDetalle);

        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);
        when(entityManager.find(Almacen.class, 1)).thenReturn(almacen);
        when(entityManager.find(CompraDetalle.class, compraDetalle.getId())).thenReturn(compraDetalle);
        when(entityManager.find(VentaDetalle.class, ventaDetalle.getId())).thenReturn(ventaDetalle);

        assertDoesNotThrow(() -> {
            kardexDAO.validarEntidadesRelacionadas(kardex);
        });

        verify(entityManager).find(Producto.class, testUUID);
        verify(entityManager).find(Almacen.class, 1);
        verify(entityManager).find(CompraDetalle.class, compraDetalle.getId());
        verify(entityManager).find(VentaDetalle.class, ventaDetalle.getId());
    }

    // ==================== Tests de métodos utilitarios de la entidad ====================
    @Test
    void testKardex_GetValorTotal_CalculatesCorrectly() {
        BigDecimal valorTotal = kardex.getValorTotal();
        // Usar compareTo en lugar de assertEquals para BigDecimal
        assertEquals(0, new BigDecimal("5000.00").compareTo(valorTotal));
    }

    @Test
    void testKardex_GetValorTotal_WithNullValues_ReturnsZero() {
        kardex.setCantidad(null);
        BigDecimal valorTotal = kardex.getValorTotal();
        assertEquals(BigDecimal.ZERO, valorTotal);
    }

    @Test
    void testKardex_GetValorActual_CalculatesCorrectly() {
        BigDecimal valorActual = kardex.getValorActual();
        // Usar compareTo en lugar de assertEquals para BigDecimal
        assertEquals(0, new BigDecimal("5000.00").compareTo(valorActual));
    }

    @Test
    void testKardex_GetValorActual_WithNullValues_ReturnsZero() {
        kardex.setCantidadActual(null);
        BigDecimal valorActual = kardex.getValorActual();
        assertEquals(BigDecimal.ZERO, valorActual);
    }

    @Test
    void testKardex_EsMovimientoEntrada_WithEntrada_ReturnsTrue() {
        kardex.setTipoMovimiento("ENTRADA");
        assertTrue(kardex.esMovimientoEntrada());
    }

    @Test
    void testKardex_EsMovimientoEntrada_WithCompra_ReturnsTrue() {
        kardex.setTipoMovimiento("COMPRA");
        assertTrue(kardex.esMovimientoEntrada());
    }

    @Test
    void testKardex_EsMovimientoEntrada_WithIngreso_ReturnsTrue() {
        kardex.setTipoMovimiento("INGRESO");
        assertTrue(kardex.esMovimientoEntrada());
    }

    @Test
    void testKardex_EsMovimientoEntrada_WithSalida_ReturnsFalse() {
        kardex.setTipoMovimiento("SALIDA");
        assertFalse(kardex.esMovimientoEntrada());
    }

    @Test
    void testKardex_EsMovimientoEntrada_WithNull_ReturnsNull() {
        kardex.setTipoMovimiento(null);
        assertNull(kardex.esMovimientoEntrada());
    }


        @Test
        void testKardex_EsMovimientoSalida_WithSalida_ReturnsTrue() {
            kardex.setTipoMovimiento("SALIDA");
            assertTrue(kardex.esMovimientoSalida());
        }

        @Test
        void testKardex_EsMovimientoSalida_WithVenta_ReturnsTrue() {
            kardex.setTipoMovimiento("VENTA");
            assertTrue(kardex.esMovimientoSalida());
        }

        @Test
        void testKardex_EsMovimientoSalida_WithEgreso_ReturnsTrue() {
            kardex.setTipoMovimiento("EGRESO");
            assertTrue(kardex.esMovimientoSalida());
        }

        @Test
        void testKardex_EsMovimientoSalida_WithEntrada_ReturnsFalse() {
            kardex.setTipoMovimiento("ENTRADA");
            assertFalse(kardex.esMovimientoSalida());
        }

        @Test
        void testKardex_EsMovimientoSalida_WithNull_ReturnsNull() {
            kardex.setTipoMovimiento(null);
            assertNull(kardex.esMovimientoSalida());
        }

        // ==================== Tests de métodos de conveniencia ====================

        @Test
        void testKardex_GetIdProductoUUID_ReturnsCorrectUUID() {
            UUID result = kardex.getIdProductoUUID();
            assertEquals(testUUID, result);
        }

        @Test
        void testKardex_GetIdProductoUUID_WithNullProducto_ReturnsNull() {
            kardex.setIdProducto(null);
            assertNull(kardex.getIdProductoUUID());
        }

        @Test
        void testKardex_SetIdProductoUUID_CreatesProducto() {
            UUID newUUID = UUID.randomUUID();
            kardex.setIdProductoUUID(newUUID);

            assertNotNull(kardex.getIdProducto());
            assertEquals(newUUID, kardex.getIdProducto().getId());
        }

        @Test
        void testKardex_SetIdProductoUUID_WithNull_SetsProductoToNull() {
            kardex.setIdProductoUUID(null);
            assertNull(kardex.getIdProducto());
        }

        @Test
        void testKardex_GetNombreProducto_ReturnsCorrectName() {
            String result = kardex.getNombreProducto();
            assertEquals("Producto Test", result);
        }

        @Test
        void testKardex_GetNombreProducto_WithNullProducto_ReturnsNull() {
            kardex.setIdProducto(null);
            assertNull(kardex.getNombreProducto());
        }

        @Test
        void testKardex_GetIdAlmacenInt_ReturnsCorrectId() {
            Integer result = kardex.getIdAlmacenInt();
            assertEquals(1, result);
        }

        @Test
        void testKardex_GetIdAlmacenInt_WithNullAlmacen_ReturnsNull() {
            kardex.setIdAlmacen(null);
            assertNull(kardex.getIdAlmacenInt());
        }

        @Test
        void testKardex_SetIdAlmacenInt_CreatesAlmacen() {
            kardex.setIdAlmacenInt(5);

            assertNotNull(kardex.getIdAlmacen());
            assertEquals(5, kardex.getIdAlmacen().getId());
        }

        @Test
        void testKardex_SetIdAlmacenInt_WithNull_SetsAlmacenToNull() {
            kardex.setIdAlmacenInt(null);
            assertNull(kardex.getIdAlmacen());
        }

        @Test
        void testKardex_GetObservacionesAlmacen_ReturnsCorrectObservaciones() {
            String result = kardex.getObservacionesAlmacen();
            assertEquals("Almacén principal", result);
        }

        @Test
        void testKardex_GetObservacionesAlmacen_WithNullAlmacen_ReturnsNull() {
            kardex.setIdAlmacen(null);
            assertNull(kardex.getObservacionesAlmacen());
        }

        @Test
        void testKardex_GetIdCompraDetalleUUID_ReturnsCorrectUUID() {
            kardex.setIdCompraDetalle(compraDetalle);
            UUID result = kardex.getIdCompraDetalleUUID();
            assertEquals(compraDetalle.getId(), result);
        }

        @Test
        void testKardex_GetIdCompraDetalleUUID_WithNull_ReturnsNull() {
            kardex.setIdCompraDetalle(null);
            assertNull(kardex.getIdCompraDetalleUUID());
        }

        @Test
        void testKardex_SetIdCompraDetalleUUID_CreatesCompraDetalle() {
            UUID newUUID = UUID.randomUUID();
            kardex.setIdCompraDetalleUUID(newUUID);

            assertNotNull(kardex.getIdCompraDetalle());
            assertEquals(newUUID, kardex.getIdCompraDetalle().getId());
        }

        @Test
        void testKardex_SetIdCompraDetalleUUID_WithNull_SetsToNull() {
            kardex.setIdCompraDetalleUUID(null);
            assertNull(kardex.getIdCompraDetalle());
        }

        @Test
        void testKardex_GetIdVentaDetalleUUID_ReturnsCorrectUUID() {
            kardex.setIdVentaDetalle(ventaDetalle);
            UUID result = kardex.getIdVentaDetalleUUID();
            assertEquals(ventaDetalle.getId(), result);
        }

        @Test
        void testKardex_GetIdVentaDetalleUUID_WithNull_ReturnsNull() {
            kardex.setIdVentaDetalle(null);
            assertNull(kardex.getIdVentaDetalleUUID());
        }

        @Test
        void testKardex_SetIdVentaDetalleUUID_CreatesVentaDetalle() {
            UUID newUUID = UUID.randomUUID();
            kardex.setIdVentaDetalleUUID(newUUID);

            assertNotNull(kardex.getIdVentaDetalle());
            assertEquals(newUUID, kardex.getIdVentaDetalle().getId());
        }

        @Test
        void testKardex_SetIdVentaDetalleUUID_WithNull_SetsToNull() {
            kardex.setIdVentaDetalleUUID(null);
            assertNull(kardex.getIdVentaDetalle());
        }

        // ==================== Tests de equals y hashCode ====================

        @Test
        void testKardex_Equals_SameObject_ReturnsTrue() {
            assertTrue(kardex.equals(kardex));
        }

        @Test
        void testKardex_Equals_NullObject_ReturnsFalse() {
            assertFalse(kardex.equals(null));
        }

        @Test
        void testKardex_Equals_DifferentClass_ReturnsFalse() {
            assertFalse(kardex.equals("Not a Kardex"));
        }

        @Test
        void testKardex_Equals_SameId_ReturnsTrue() {
            Kardex kardex2 = new Kardex();
            kardex2.setId(testUUID);

            assertTrue(kardex.equals(kardex2));
        }

        @Test
        void testKardex_Equals_DifferentId_ReturnsFalse() {
            Kardex kardex2 = new Kardex();
            kardex2.setId(UUID.randomUUID());

            assertFalse(kardex.equals(kardex2));
        }

        @Test
        void testKardex_Equals_BothIdsNull_ReturnsFalse() {
            Kardex kardex1 = new Kardex();
            Kardex kardex2 = new Kardex();

            assertFalse(kardex1.equals(kardex2));
        }

        @Test
        void testKardex_HashCode_ConsistentWithEquals() {
            Kardex kardex2 = new Kardex();
            kardex2.setId(testUUID);

            assertEquals(kardex.hashCode(), kardex2.hashCode());
        }

        @Test
        void testKardex_ToString_ContainsId() {
            String result = kardex.toString();

            assertNotNull(result);
            assertTrue(result.contains(testUUID.toString()));
            assertTrue(result.contains("ENTRADA"));
        }

        @Test
        void testKardex_ToString_HandlesNullValues() {
            Kardex kardexVacio = new Kardex();
            String result = kardexVacio.toString();

            assertNotNull(result);
            assertTrue(result.contains("Kardex"));
        }

        // ==================== Tests de edge cases ====================

        @Test
        void testCrear_WithMultipleValidations_AllPass() {
            kardex.setFecha(null); // Debe establecerse automáticamente
            doNothing().when(entityManager).persist(any(Kardex.class));

            kardexDAO.crear(kardex);

            assertNotNull(kardex.getFecha());
            verify(entityManager).persist(kardex);
        }

        @Test
        void testFindByProducto_WithMultipleResults_ReturnsAll() {
            Kardex kardex2 = new Kardex();
            kardex2.setId(UUID.randomUUID());
            kardex2.setIdProducto(producto);

            List<Kardex> expectedList = Arrays.asList(kardex, kardex2);

            when(entityManager.createQuery(anyString(), eq(Kardex.class))).thenReturn(typedQuery);
            when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(expectedList);

            List<Kardex> result = kardexDAO.findByProducto(testUUID);

            assertEquals(2, result.size());
        }

        @Test
        void testKardex_TipoMovimiento_CaseInsensitive() {
            kardex.setTipoMovimiento("entrada");
            assertTrue(kardex.esMovimientoEntrada());

            kardex.setTipoMovimiento("SALIDA");
            assertTrue(kardex.esMovimientoSalida());
        }

    @Test
    void testKardex_GetValorTotal_WithLargeNumbers() {
        kardex.setCantidad(new BigDecimal("999999.99"));
        kardex.setPrecio(new BigDecimal("999999.99"));

        BigDecimal expected = new BigDecimal("999999980000.0001");
        BigDecimal result = kardex.getValorTotal();

        // Usar compareTo para comparar BigDecimal correctamente
        assertEquals(0, expected.compareTo(result));
    }

        @Test
        void testKardex_GetValorActual_WithZeroValues() {
            kardex.setCantidadActual(BigDecimal.ZERO);
            kardex.setPrecioActual(BigDecimal.ZERO);

            assertEquals(BigDecimal.ZERO, kardex.getValorActual());
        }
    }