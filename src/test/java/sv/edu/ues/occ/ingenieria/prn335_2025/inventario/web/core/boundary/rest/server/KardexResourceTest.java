package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.*;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para KardexResource
 * Verifica el comportamiento de todos los endpoints REST para movimientos de inventario
 */
class KardexResourceTest {

    @Mock
    private KardexDAO kardexDAO;

    @Mock
    private ProductoDAO productoDAO;

    @Mock
    private AlmacenDAO almacenDAO;

    @Mock
    private CompraDetalleDAO compraDetalleDAO;

    @Mock
    private VentaDetalleDAO ventaDetalleDAO;

    private KardexResource kardexResource;
    private Kardex kardex;
    private Producto producto;
    private Almacen almacen;
    private CompraDetalle compraDetalle;
    private VentaDetalle ventaDetalle;
    private UUID kardexId;
    private UUID productoId;
    private Integer almacenId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        kardexResource = new KardexResource();

        // Inyección manual de los DAOs (sin private en KardexResource)
        kardexResource.kardexDAO = kardexDAO;
        kardexResource.productoDAO = productoDAO;
        kardexResource.almacenDAO = almacenDAO;
        kardexResource.compraDetalleDAO = compraDetalleDAO;
        kardexResource.ventaDetalleDAO = ventaDetalleDAO;

        // Datos de prueba
        kardexId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        productoId = UUID.fromString("223e4567-e89b-12d3-a456-426614174000");
        almacenId = 1;

        producto = new Producto();
        producto.setId(productoId);
        producto.setNombreProducto("Laptop Dell");
        producto.setActivo(true);

        almacen = new Almacen();
        almacen.setId(almacenId);

        compraDetalle = new CompraDetalle();
        compraDetalle.setId(UUID.fromString("323e4567-e89b-12d3-a456-426614174000"));

        ventaDetalle = new VentaDetalle();
        ventaDetalle.setId(UUID.fromString("423e4567-e89b-12d3-a456-426614174000"));

        kardex = new Kardex();
        kardex.setId(kardexId);
        kardex.setIdProducto(producto);
        kardex.setIdAlmacen(almacen);
        kardex.setTipoMovimiento("ENTRADA");
        kardex.setCantidad(new BigDecimal("10.00"));
        kardex.setPrecio(new BigDecimal("1500.00"));
        kardex.setCantidadActual(new BigDecimal("50.00"));
        kardex.setPrecioActual(new BigDecimal("1450.00"));
        kardex.setFecha(OffsetDateTime.now());
        kardex.setObservaciones("Entrada por compra");
    }

    // ==================== TESTS DE getAllKardex ====================

    @Test
    void testGetAllKardex_SinFiltros_DebeRetornarTodos() {
        // Arrange
        List<Kardex> kardexList = new ArrayList<>();
        kardexList.add(kardex);

        when(kardexDAO.findAll()).thenReturn(kardexList);

        // Act
        Response response = kardexResource.getAllKardex(null, null, null);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Kardex> resultado = (List<Kardex>) response.getEntity();
        assertEquals(1, resultado.size());

        verify(kardexDAO).findAll();
        verify(kardexDAO, never()).findByTipoMovimiento(any());
        verify(kardexDAO, never()).findByRangoFechas(any(), any());
    }

    @Test
    void testGetAllKardex_ConTipoMovimiento_DebeFiltrarPorTipo() {
        // Arrange
        List<Kardex> kardexList = new ArrayList<>();
        kardexList.add(kardex);

        when(kardexDAO.findByTipoMovimiento("ENTRADA")).thenReturn(kardexList);

        // Act
        Response response = kardexResource.getAllKardex("ENTRADA", null, null);

        // Assert
        assertEquals(200, response.getStatus());
        verify(kardexDAO).findByTipoMovimiento("ENTRADA");
        verify(kardexDAO, never()).findAll();
    }

    @Test
    void testGetAllKardex_ConRangoFechas_DebeFiltrarPorFechas() {
        // Arrange
        List<Kardex> kardexList = new ArrayList<>();
        kardexList.add(kardex);

        when(kardexDAO.findByRangoFechas(any(), any())).thenReturn(kardexList);

        // Act
        Response response = kardexResource.getAllKardex(null, "2024-01-01T00:00:00Z", "2024-12-31T23:59:59Z");

        // Assert
        assertEquals(200, response.getStatus());
        verify(kardexDAO).findByRangoFechas(any(), any());
        verify(kardexDAO, never()).findAll();
    }

    @Test
    void testGetAllKardex_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(kardexDAO.findAll()).thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = kardexResource.getAllKardex(null, null, null);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al obtener kardex"));
        verify(kardexDAO).findAll();
    }

    // ==================== TESTS DE getKardexById ====================

    @Test
    void testGetKardexById_KardexExiste_DebeRetornarOk() {
        // Arrange
        when(kardexDAO.findById(kardexId)).thenReturn(kardex);

        // Act
        Response response = kardexResource.getKardexById(kardexId.toString());

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        Kardex resultado = (Kardex) response.getEntity();
        assertEquals(kardexId, resultado.getId());

        verify(kardexDAO).findById(kardexId);
    }

    @Test
    void testGetKardexById_UUIDInvalido_DebeRetornarBadRequest() {
        // Act
        Response response = kardexResource.getKardexById("uuid-invalido");

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("ID de kardex inválido"));
        verify(kardexDAO, never()).findById(any());
    }

    @Test
    void testGetKardexById_KardexNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(kardexDAO.findById(kardexId)).thenReturn(null);

        // Act
        Response response = kardexResource.getKardexById(kardexId.toString());

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Registro de kardex no encontrado"));
        verify(kardexDAO).findById(kardexId);
    }

    // ==================== TESTS DE createKardex ====================

    @Test
    void testCreateKardex_DatosValidos_DebeRetornarCreated() {
        // Arrange
        Kardex nuevoKardex = new Kardex();
        nuevoKardex.setIdProductoUUID(productoId);
        nuevoKardex.setTipoMovimiento("ENTRADA");
        nuevoKardex.setCantidad(new BigDecimal("5.00"));
        nuevoKardex.setPrecio(new BigDecimal("1000.00"));

        when(productoDAO.findById(productoId)).thenReturn(producto);
        doNothing().when(kardexDAO).crear(any(Kardex.class));

        // Act
        Response response = kardexResource.createKardex(nuevoKardex);

        // Assert
        assertEquals(201, response.getStatus());
        assertNotNull(response.getEntity());

        verify(productoDAO).findById(productoId);
        verify(kardexDAO).crear(any(Kardex.class));
    }

    @Test
    void testCreateKardex_SinIdProducto_DebeRetornarBadRequest() {
        // Arrange
        Kardex nuevoKardex = new Kardex();
        nuevoKardex.setTipoMovimiento("ENTRADA");
        nuevoKardex.setCantidad(new BigDecimal("5.00"));

        // Act
        Response response = kardexResource.createKardex(nuevoKardex);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("ID del producto es obligatorio"));
        verify(kardexDAO, never()).crear(any());
    }

    @Test
    void testCreateKardex_SinTipoMovimiento_DebeRetornarBadRequest() {
        // Arrange
        Kardex nuevoKardex = new Kardex();
        nuevoKardex.setIdProductoUUID(productoId);
        nuevoKardex.setCantidad(new BigDecimal("5.00"));

        // Act
        Response response = kardexResource.createKardex(nuevoKardex);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("tipo de movimiento es obligatorio"));
        verify(kardexDAO, never()).crear(any());
    }

    @Test
    void testCreateKardex_CantidadCero_DebeRetornarBadRequest() {
        // Arrange
        Kardex nuevoKardex = new Kardex();
        nuevoKardex.setIdProductoUUID(productoId);
        nuevoKardex.setTipoMovimiento("ENTRADA");
        nuevoKardex.setCantidad(BigDecimal.ZERO);

        // Act
        Response response = kardexResource.createKardex(nuevoKardex);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("cantidad debe ser mayor a cero"));
        verify(kardexDAO, never()).crear(any());
    }

    @Test
    void testCreateKardex_ProductoNoExiste_DebeRetornarNotFound() {
        // Arrange
        Kardex nuevoKardex = new Kardex();
        nuevoKardex.setIdProductoUUID(productoId);
        nuevoKardex.setTipoMovimiento("ENTRADA");
        nuevoKardex.setCantidad(new BigDecimal("5.00"));

        when(productoDAO.findById(productoId)).thenReturn(null);

        // Act
        Response response = kardexResource.createKardex(nuevoKardex);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Producto no encontrado"));
        verify(productoDAO).findById(productoId);
        verify(kardexDAO, never()).crear(any());
    }

    @Test
    void testCreateKardex_ConAlmacenValido_DebeAsignarAlmacen() {
        // Arrange
        Kardex nuevoKardex = new Kardex();
        nuevoKardex.setIdProductoUUID(productoId);
        nuevoKardex.setIdAlmacenInt(almacenId);
        nuevoKardex.setTipoMovimiento("ENTRADA");
        nuevoKardex.setCantidad(new BigDecimal("5.00"));

        when(productoDAO.findById(productoId)).thenReturn(producto);
        when(almacenDAO.findById(almacenId)).thenReturn(almacen);
        doNothing().when(kardexDAO).crear(any(Kardex.class));

        // Act
        Response response = kardexResource.createKardex(nuevoKardex);

        // Assert
        assertEquals(201, response.getStatus());
        verify(almacenDAO).findById(almacenId);
        verify(kardexDAO).crear(any(Kardex.class));
    }

    @Test
    void testCreateKardex_AlmacenNoExiste_DebeRetornarNotFound() {
        // Arrange
        Kardex nuevoKardex = new Kardex();
        nuevoKardex.setIdProductoUUID(productoId);
        nuevoKardex.setIdAlmacenInt(almacenId);
        nuevoKardex.setTipoMovimiento("ENTRADA");
        nuevoKardex.setCantidad(new BigDecimal("5.00"));

        when(productoDAO.findById(productoId)).thenReturn(producto);
        when(almacenDAO.findById(almacenId)).thenReturn(null);

        // Act
        Response response = kardexResource.createKardex(nuevoKardex);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Almacén no encontrado"));
        verify(almacenDAO).findById(almacenId);
        verify(kardexDAO, never()).crear(any());
    }

    // ==================== TESTS DE updateKardex ====================

    @Test
    void testUpdateKardex_DatosValidos_DebeRetornarOk() {
        // Arrange
        Kardex actualizacion = new Kardex();
        actualizacion.setCantidad(new BigDecimal("15.00"));
        actualizacion.setPrecio(new BigDecimal("1600.00"));
        actualizacion.setObservaciones("Actualizado");

        when(kardexDAO.findById(kardexId)).thenReturn(kardex);
        when(kardexDAO.modificar(any(Kardex.class))).thenReturn(kardex);

        // Act
        Response response = kardexResource.updateKardex(kardexId.toString(), actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        verify(kardexDAO).findById(kardexId);
        verify(kardexDAO).modificar(any(Kardex.class));
    }

    @Test
    void testUpdateKardex_UUIDInvalido_DebeRetornarBadRequest() {
        // Arrange
        Kardex actualizacion = new Kardex();

        // Act
        Response response = kardexResource.updateKardex("uuid-invalido", actualizacion);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("ID de kardex inválido"));
        verify(kardexDAO, never()).findById(any());
    }

    @Test
    void testUpdateKardex_KardexNoExiste_DebeRetornarNotFound() {
        // Arrange
        Kardex actualizacion = new Kardex();
        when(kardexDAO.findById(kardexId)).thenReturn(null);

        // Act
        Response response = kardexResource.updateKardex(kardexId.toString(), actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Kardex no encontrado"));
        verify(kardexDAO).findById(kardexId);
        verify(kardexDAO, never()).modificar(any());
    }

    // ==================== TESTS DE deleteKardex ====================

    @Test
    void testDeleteKardex_KardexExiste_DebeRetornarNoContent() {
        // Arrange
        when(kardexDAO.findById(kardexId)).thenReturn(kardex);
        doNothing().when(kardexDAO).eliminar(kardex);

        // Act
        Response response = kardexResource.deleteKardex(kardexId.toString());

        // Assert
        assertEquals(204, response.getStatus());
        verify(kardexDAO).findById(kardexId);
        verify(kardexDAO).eliminar(kardex);
    }

    @Test
    void testDeleteKardex_UUIDInvalido_DebeRetornarBadRequest() {
        // Act
        Response response = kardexResource.deleteKardex("uuid-invalido");

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("ID de kardex inválido"));
        verify(kardexDAO, never()).findById(any());
    }

    @Test
    void testDeleteKardex_KardexNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(kardexDAO.findById(kardexId)).thenReturn(null);

        // Act
        Response response = kardexResource.deleteKardex(kardexId.toString());

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Kardex no encontrado"));
        verify(kardexDAO).findById(kardexId);
        verify(kardexDAO, never()).eliminar(any());
    }

    // ==================== TESTS DE getKardexByProducto ====================

    @Test
    void testGetKardexByProducto_ProductoExisteConMovimientos_DebeRetornarOk() {
        // Arrange
        List<Kardex> kardexList = new ArrayList<>();
        kardexList.add(kardex);

        when(productoDAO.findById(productoId)).thenReturn(producto);
        when(kardexDAO.findByProducto(productoId)).thenReturn(kardexList);

        // Act
        Response response = kardexResource.getKardexByProducto(productoId.toString());

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Kardex> resultado = (List<Kardex>) response.getEntity();
        assertEquals(1, resultado.size());

        verify(productoDAO).findById(productoId);
        verify(kardexDAO).findByProducto(productoId);
    }

    @Test
    void testGetKardexByProducto_UUIDInvalido_DebeRetornarBadRequest() {
        // Act
        Response response = kardexResource.getKardexByProducto("uuid-invalido");

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("ID de producto inválido"));
        verify(productoDAO, never()).findById(any());
    }

    @Test
    void testGetKardexByProducto_ProductoNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(productoDAO.findById(productoId)).thenReturn(null);

        // Act
        Response response = kardexResource.getKardexByProducto(productoId.toString());

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Producto no encontrado"));
        verify(productoDAO).findById(productoId);
        verify(kardexDAO, never()).findByProducto(any());
    }

    // ==================== TESTS DE getUltimoMovimientoProducto ====================

    @Test
    void testGetUltimoMovimientoProducto_ConMovimientos_DebeRetornarOk() {
        // Arrange
        when(kardexDAO.findUltimoMovimiento(productoId, null)).thenReturn(kardex);

        // Act
        Response response = kardexResource.getUltimoMovimientoProducto(productoId.toString(), null);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        Kardex resultado = (Kardex) response.getEntity();
        assertEquals(kardexId, resultado.getId());

        verify(kardexDAO).findUltimoMovimiento(productoId, null);
    }

    @Test
    void testGetUltimoMovimientoProducto_ConAlmacen_DebeRetornarOk() {
        // Arrange
        when(kardexDAO.findUltimoMovimiento(productoId, almacenId)).thenReturn(kardex);

        // Act
        Response response = kardexResource.getUltimoMovimientoProducto(productoId.toString(), almacenId);

        // Assert
        assertEquals(200, response.getStatus());
        verify(kardexDAO).findUltimoMovimiento(productoId, almacenId);
    }

    @Test
    void testGetUltimoMovimientoProducto_SinMovimientos_DebeRetornarNotFound() {
        // Arrange
        when(kardexDAO.findUltimoMovimiento(productoId, null)).thenReturn(null);

        // Act
        Response response = kardexResource.getUltimoMovimientoProducto(productoId.toString(), null);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("No se encontraron movimientos"));
        verify(kardexDAO).findUltimoMovimiento(productoId, null);
    }

    @Test
    void testGetUltimoMovimientoProducto_UUIDInvalido_DebeRetornarBadRequest() {
        // Act
        Response response = kardexResource.getUltimoMovimientoProducto("uuid-invalido", null);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("ID de producto inválido"));
        verify(kardexDAO, never()).findUltimoMovimiento(any(), any());
    }

    // ==================== TESTS DE getKardexByAlmacen ====================

    @Test
    void testGetKardexByAlmacen_AlmacenExisteConMovimientos_DebeRetornarOk() {
        // Arrange
        List<Kardex> kardexList = new ArrayList<>();
        kardexList.add(kardex);

        when(almacenDAO.findById(almacenId)).thenReturn(almacen);
        when(kardexDAO.findByAlmacen(almacenId)).thenReturn(kardexList);

        // Act
        Response response = kardexResource.getKardexByAlmacen(almacenId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Kardex> resultado = (List<Kardex>) response.getEntity();
        assertEquals(1, resultado.size());

        verify(almacenDAO).findById(almacenId);
        verify(kardexDAO).findByAlmacen(almacenId);
    }

    @Test
    void testGetKardexByAlmacen_AlmacenNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(almacenDAO.findById(almacenId)).thenReturn(null);

        // Act
        Response response = kardexResource.getKardexByAlmacen(almacenId);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Almacén no encontrado"));
        verify(almacenDAO).findById(almacenId);
        verify(kardexDAO, never()).findByAlmacen(any());
    }

    // ==================== TESTS DE getKardexByCompraDetalle ====================

    @Test
    void testGetKardexByCompraDetalle_ConMovimientos_DebeRetornarOk() {
        // Arrange
        UUID compraDetalleId = UUID.fromString("523e4567-e89b-12d3-a456-426614174000");
        List<Kardex> kardexList = new ArrayList<>();
        kardexList.add(kardex);

        when(kardexDAO.findByCompraDetalle(compraDetalleId)).thenReturn(kardexList);

        // Act
        Response response = kardexResource.getKardexByCompraDetalle(compraDetalleId.toString());

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Kardex> resultado = (List<Kardex>) response.getEntity();
        assertEquals(1, resultado.size());

        verify(kardexDAO).findByCompraDetalle(compraDetalleId);
    }

    @Test
    void testGetKardexByCompraDetalle_UUIDInvalido_DebeRetornarBadRequest() {
        // Act
        Response response = kardexResource.getKardexByCompraDetalle("uuid-invalido");

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("ID de compra detalle inválido"));
        verify(kardexDAO, never()).findByCompraDetalle(any());
    }

    // ==================== TESTS DE getKardexByVentaDetalle ====================

    @Test
    void testGetKardexByVentaDetalle_ConMovimientos_DebeRetornarOk() {
        // Arrange
        UUID ventaDetalleId = UUID.fromString("623e4567-e89b-12d3-a456-426614174000");
        List<Kardex> kardexList = new ArrayList<>();
        kardexList.add(kardex);

        when(kardexDAO.findByVentaDetalle(ventaDetalleId)).thenReturn(kardexList);

        // Act
        Response response = kardexResource.getKardexByVentaDetalle(ventaDetalleId.toString());

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Kardex> resultado = (List<Kardex>) response.getEntity();
        assertEquals(1, resultado.size());

        verify(kardexDAO).findByVentaDetalle(ventaDetalleId);
    }

    @Test
    void testGetKardexByVentaDetalle_UUIDInvalido_DebeRetornarBadRequest() {
        // Act
        Response response = kardexResource.getKardexByVentaDetalle("uuid-invalido");

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("ID de venta detalle inválido"));
        verify(kardexDAO, never()).findByVentaDetalle(any());
    }

// ==================== TESTS ADICIONALES PARA MEJORAR COBERTURA ====================

    @Test
    void testCreateKardex_ConCompraDetalleValido_DebeAsignarCompraDetalle() {
        // Arrange
        UUID compraDetalleId = UUID.fromString("723e4567-e89b-12d3-a456-426614174000");
        Kardex nuevoKardex = new Kardex();
        nuevoKardex.setIdProductoUUID(productoId);
        nuevoKardex.setIdCompraDetalleUUID(compraDetalleId);
        nuevoKardex.setTipoMovimiento("ENTRADA");
        nuevoKardex.setCantidad(new BigDecimal("5.00"));

        when(productoDAO.findById(productoId)).thenReturn(producto);
        when(compraDetalleDAO.findById(compraDetalleId)).thenReturn(compraDetalle);
        doNothing().when(kardexDAO).crear(any(Kardex.class));

        // Act
        Response response = kardexResource.createKardex(nuevoKardex);

        // Assert
        assertEquals(201, response.getStatus());
        verify(compraDetalleDAO).findById(compraDetalleId);
        verify(kardexDAO).crear(any(Kardex.class));
    }

    @Test
    void testCreateKardex_CompraDetalleNoExiste_DebeRetornarNotFound() {
        // Arrange
        UUID compraDetalleId = UUID.fromString("823e4567-e89b-12d3-a456-426614174000");
        Kardex nuevoKardex = new Kardex();
        nuevoKardex.setIdProductoUUID(productoId);
        nuevoKardex.setIdCompraDetalleUUID(compraDetalleId);
        nuevoKardex.setTipoMovimiento("ENTRADA");
        nuevoKardex.setCantidad(new BigDecimal("5.00"));

        when(productoDAO.findById(productoId)).thenReturn(producto);
        when(compraDetalleDAO.findById(compraDetalleId)).thenReturn(null);

        // Act
        Response response = kardexResource.createKardex(nuevoKardex);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Detalle de compra no encontrado"));
        verify(compraDetalleDAO).findById(compraDetalleId);
        verify(kardexDAO, never()).crear(any());
    }

    @Test
    void testCreateKardex_ConVentaDetalleValido_DebeAsignarVentaDetalle() {
        // Arrange
        UUID ventaDetalleId = UUID.fromString("923e4567-e89b-12d3-a456-426614174000");
        Kardex nuevoKardex = new Kardex();
        nuevoKardex.setIdProductoUUID(productoId);
        nuevoKardex.setIdVentaDetalleUUID(ventaDetalleId);
        nuevoKardex.setTipoMovimiento("SALIDA");
        nuevoKardex.setCantidad(new BigDecimal("3.00"));

        when(productoDAO.findById(productoId)).thenReturn(producto);
        when(ventaDetalleDAO.findById(ventaDetalleId)).thenReturn(ventaDetalle);
        doNothing().when(kardexDAO).crear(any(Kardex.class));

        // Act
        Response response = kardexResource.createKardex(nuevoKardex);

        // Assert
        assertEquals(201, response.getStatus());
        verify(ventaDetalleDAO).findById(ventaDetalleId);
        verify(kardexDAO).crear(any(Kardex.class));
    }

    @Test
    void testCreateKardex_VentaDetalleNoExiste_DebeRetornarNotFound() {
        // Arrange
        UUID ventaDetalleId = UUID.fromString("a23e4567-e89b-12d3-a456-426614174000");
        Kardex nuevoKardex = new Kardex();
        nuevoKardex.setIdProductoUUID(productoId);
        nuevoKardex.setIdVentaDetalleUUID(ventaDetalleId);
        nuevoKardex.setTipoMovimiento("SALIDA");
        nuevoKardex.setCantidad(new BigDecimal("3.00"));

        when(productoDAO.findById(productoId)).thenReturn(producto);
        when(ventaDetalleDAO.findById(ventaDetalleId)).thenReturn(null);

        // Act
        Response response = kardexResource.createKardex(nuevoKardex);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Detalle de venta no encontrado"));
        verify(ventaDetalleDAO).findById(ventaDetalleId);
        verify(kardexDAO, never()).crear(any());
    }

    @Test
    void testUpdateKardex_ActualizarTodosLosCamposPermitidos_DebeRetornarOk() {
        // Arrange
        Kardex actualizacion = new Kardex();
        actualizacion.setCantidad(new BigDecimal("20.00"));
        actualizacion.setPrecio(new BigDecimal("1700.00"));
        actualizacion.setCantidadActual(new BigDecimal("60.00"));
        actualizacion.setPrecioActual(new BigDecimal("1550.00"));
        actualizacion.setObservaciones("Observaciones actualizadas");
        actualizacion.setReferenciaExterna("REF-12345");

        when(kardexDAO.findById(kardexId)).thenReturn(kardex);
        when(kardexDAO.modificar(any(Kardex.class))).thenReturn(kardex);

        // Act
        Response response = kardexResource.updateKardex(kardexId.toString(), actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(kardexDAO).modificar(any(Kardex.class));
    }

    @Test
    void testGetAllKardex_ConExcepcionEnFiltroTipoMovimiento_DebeRetornarInternalServerError() {
        // Arrange
        when(kardexDAO.findByTipoMovimiento("ENTRADA"))
                .thenThrow(new RuntimeException("Error en filtro"));

        // Act
        Response response = kardexResource.getAllKardex("ENTRADA", null, null);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al obtener kardex"));
        verify(kardexDAO).findByTipoMovimiento("ENTRADA");
    }

    @Test
    void testGetAllKardex_ConExcepcionEnFiltroFechas_DebeRetornarInternalServerError() {
        // Arrange
        when(kardexDAO.findByRangoFechas(any(), any()))
                .thenThrow(new RuntimeException("Error en fechas"));

        // Act
        Response response = kardexResource.getAllKardex(null, "2024-01-01T00:00:00Z", "2024-12-31T23:59:59Z");

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al obtener kardex"));
        verify(kardexDAO).findByRangoFechas(any(), any());
    }
}
