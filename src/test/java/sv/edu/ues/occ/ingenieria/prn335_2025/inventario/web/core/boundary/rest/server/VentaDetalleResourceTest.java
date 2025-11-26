package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Venta;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.VentaDetalle;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests para VentaDetalleResource
 * Verifica el comportamiento de todos los endpoints REST para detalles de venta
 */
class VentaDetalleResourceTest {

    @Mock
    private VentaDetalleDAO ventaDetalleDAO;

    @Mock
    private VentaDAO ventaDAO;

    @Mock
    private ProductoDAO productoDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    private VentaDetalleResource ventaDetalleResource;
    private VentaDetalle ventaDetalle;
    private Venta venta;
    private Producto producto;
    private UUID ventaDetalleId;
    private UUID ventaId;
    private UUID productoId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ventaDetalleResource = new VentaDetalleResource();

        // Inyección manual de los DAOs
        ventaDetalleResource.ventaDetalleDao = ventaDetalleDAO;
        ventaDetalleResource.ventaDao = ventaDAO;
        ventaDetalleResource.productoDAO = productoDAO;

        // Datos de prueba
        ventaDetalleId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ventaId = UUID.fromString("223e4567-e89b-12d3-a456-426614174000");
        productoId = UUID.fromString("323e4567-e89b-12d3-a456-426614174000");

        // Configurar Venta
        venta = new Venta();
        venta.setId(ventaId);

        // Configurar Producto
        producto = new Producto();
        producto.setId(productoId);
        producto.setNombreProducto("Laptop Dell XPS");

        // Configurar VentaDetalle
        ventaDetalle = new VentaDetalle();
        ventaDetalle.setId(ventaDetalleId);
        ventaDetalle.setIdVenta(venta);
        ventaDetalle.setIdProducto(producto);
        ventaDetalle.setCantidad(new BigDecimal("2.00"));
        ventaDetalle.setPrecio(new BigDecimal("1500.00"));
        ventaDetalle.setEstado("ACTIVO");
        ventaDetalle.setObservaciones("Venta normal");
    }

    // ==================== TESTS DE findRange ====================

    @Test
    void testFindRange_ParametrosValidos_DebeRetornarOkConHeader() {
        // Arrange
        List<VentaDetalle> ventasDetalle = new ArrayList<>();
        ventasDetalle.add(ventaDetalle);

        VentaDetalle ventaDetalle2 = new VentaDetalle();
        ventaDetalle2.setId(UUID.fromString("423e4567-e89b-12d3-a456-426614174000"));
        ventaDetalle2.setCantidad(new BigDecimal("1.00"));
        ventasDetalle.add(ventaDetalle2);

        when(ventaDetalleDAO.findRange(0, 50)).thenReturn(ventasDetalle);
        when(ventaDetalleDAO.count()).thenReturn(100L);

        // Act
        Response response = ventaDetalleResource.findRange(0, 50);

        // Assert
        assertEquals(200, response.getStatus(), "Debe retornar OK");
        assertNotNull(response.getEntity());

        assertEquals("100", response.getHeaderString("Total-records"));

        @SuppressWarnings("unchecked")
        List<VentaDetalle> resultado = (List<VentaDetalle>) response.getEntity();
        assertEquals(2, resultado.size());

        verify(ventaDetalleDAO).findRange(0, 50);
        verify(ventaDetalleDAO).count();
    }

    @Test
    void testFindRange_FirstNegativo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = ventaDetalleResource.findRange(-1, 50);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first or max out of range", response.getHeaderString("Missing-parameter"));
        verify(ventaDetalleDAO, never()).findRange(anyInt(), anyInt());
        verify(ventaDetalleDAO, never()).count();
    }

    @Test
    void testFindRange_MaxMayor100_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = ventaDetalleResource.findRange(0, 101);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first or max out of range", response.getHeaderString("Missing-parameter"));
        verify(ventaDetalleDAO, never()).findRange(anyInt(), anyInt());
        verify(ventaDetalleDAO, never()).count();
    }

    @Test
    void testFindRange_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(ventaDetalleDAO.findRange(0, 50))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        Response response = ventaDetalleResource.findRange(0, 50);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(ventaDetalleDAO).findRange(0, 50);
    }

    // ==================== TESTS DE findById ====================

    @Test
    void testFindById_IdValidoVentaDetalleExiste_DebeRetornarOk() {
        // Arrange
        String idString = ventaDetalleId.toString();
        when(ventaDetalleDAO.findById(ventaDetalleId)).thenReturn(ventaDetalle);

        // Act
        Response response = ventaDetalleResource.findById(idString);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        VentaDetalle resultado = (VentaDetalle) response.getEntity();
        assertEquals(ventaDetalleId, resultado.getId());
        assertEquals(new BigDecimal("2.00"), resultado.getCantidad());
        assertEquals(new BigDecimal("1500.00"), resultado.getPrecio());
        assertEquals("ACTIVO", resultado.getEstado());

        verify(ventaDetalleDAO).findById(ventaDetalleId);
    }

    @Test
    void testFindById_IdNulo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = ventaDetalleResource.findById(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id cannot be null or empty", response.getHeaderString("Missing-parameter"));
        verify(ventaDetalleDAO, never()).findById(any());
    }

    @Test
    void testFindById_IdVacio_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = ventaDetalleResource.findById("   ");

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id cannot be null or empty", response.getHeaderString("Missing-parameter"));
        verify(ventaDetalleDAO, never()).findById(any());
    }

    @Test
    void testFindById_UUIDInvalido_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = ventaDetalleResource.findById("uuid-invalido");

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("Invalid UUID format", response.getHeaderString("Missing-parameter"));
        verify(ventaDetalleDAO, never()).findById(any());
    }

    @Test
    void testFindById_VentaDetalleNoExiste_DebeRetornarNotFound() {
        // Arrange
        UUID idNoExistente = UUID.fromString("999e4567-e89b-12d3-a456-426614174000");
        String idString = idNoExistente.toString();
        when(ventaDetalleDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = ventaDetalleResource.findById(idString);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + idString + " not found", response.getHeaderString("Record-not-found"));
        verify(ventaDetalleDAO).findById(idNoExistente);
    }

    @Test
    void testFindById_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        String idString = ventaDetalleId.toString();
        when(ventaDetalleDAO.findById(ventaDetalleId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = ventaDetalleResource.findById(idString);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(ventaDetalleDAO).findById(ventaDetalleId);
    }

    // ==================== TESTS DE delete ====================

    @Test
    void testDelete_IdValidoVentaDetalleExiste_DebeRetornarNoContent() {
        // Arrange
        String idString = ventaDetalleId.toString();
        when(ventaDetalleDAO.findById(ventaDetalleId)).thenReturn(ventaDetalle);
        doNothing().when(ventaDetalleDAO).eliminar(ventaDetalle);

        // Act
        Response response = ventaDetalleResource.delete(idString);

        // Assert
        assertEquals(204, response.getStatus(), "Debe retornar NO_CONTENT");
        verify(ventaDetalleDAO).findById(ventaDetalleId);
        verify(ventaDetalleDAO).eliminar(ventaDetalle);
    }

    @Test
    void testDelete_IdNulo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = ventaDetalleResource.delete(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id cannot be null or empty", response.getHeaderString("Missing-parameter"));
        verify(ventaDetalleDAO, never()).findById(any());
        verify(ventaDetalleDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_IdVacio_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = ventaDetalleResource.delete("   ");

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id cannot be null or empty", response.getHeaderString("Missing-parameter"));
        verify(ventaDetalleDAO, never()).findById(any());
        verify(ventaDetalleDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_UUIDInvalido_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = ventaDetalleResource.delete("uuid-invalido");

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("Invalid UUID format", response.getHeaderString("Missing-parameter"));
        verify(ventaDetalleDAO, never()).findById(any());
        verify(ventaDetalleDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_VentaDetalleNoExiste_DebeRetornarNotFound() {
        // Arrange
        UUID idNoExistente = UUID.fromString("999e4567-e89b-12d3-a456-426614174000");
        String idString = idNoExistente.toString();
        when(ventaDetalleDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = ventaDetalleResource.delete(idString);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + idString + " not found", response.getHeaderString("Record-not-found"));
        verify(ventaDetalleDAO).findById(idNoExistente);
        verify(ventaDetalleDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        String idString = ventaDetalleId.toString();
        when(ventaDetalleDAO.findById(ventaDetalleId)).thenReturn(ventaDetalle);
        doThrow(new RuntimeException("Error al eliminar"))
                .when(ventaDetalleDAO).eliminar(any());

        // Act
        Response response = ventaDetalleResource.delete(idString);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(ventaDetalleDAO).findById(ventaDetalleId);
        verify(ventaDetalleDAO).eliminar(ventaDetalle);
    }

    // ==================== TESTS DE create ====================

    @Test
    void testCreate_VentaDetalleValido_DebeRetornarCreated() throws Exception {
        // Arrange
        VentaDetalle nuevaVentaDetalle = new VentaDetalle();
        nuevaVentaDetalle.setCantidad(new BigDecimal("3.00"));
        nuevaVentaDetalle.setPrecio(new BigDecimal("2000.00"));
        nuevaVentaDetalle.setEstado("ACTIVO");
        nuevaVentaDetalle.setObservaciones("Nueva venta");

        // Establecer relaciones con IDs
        Venta ventaRelacion = new Venta();
        ventaRelacion.setId(ventaId);
        nuevaVentaDetalle.setIdVenta(ventaRelacion);

        Producto productoRelacion = new Producto();
        productoRelacion.setId(productoId);
        nuevaVentaDetalle.setIdProducto(productoRelacion);

        UUID nuevoId = UUID.fromString("523e4567-e89b-12d3-a456-426614174000");
        URI locationUri = new URI("http://localhost:8080/api/venta_detalle/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);
        when(ventaDAO.findById(ventaId)).thenReturn(venta);
        when(productoDAO.findById(productoId)).thenReturn(producto);

        // Simular que después de crear, se asigna un ID
        doAnswer(invocation -> {
            VentaDetalle vd = invocation.getArgument(0);
            vd.setId(nuevoId);
            return null;
        }).when(ventaDetalleDAO).crear(any(VentaDetalle.class));

        // Act
        Response response = ventaDetalleResource.create(nuevaVentaDetalle, uriInfo);

        // Assert
        assertEquals(201, response.getStatus(), "Debe retornar CREATED");
        assertNotNull(response.getLocation());
        assertEquals(locationUri, response.getLocation());

        verify(ventaDAO).findById(ventaId);
        verify(productoDAO).findById(productoId);
        verify(ventaDetalleDAO).crear(any(VentaDetalle.class));
    }

    @Test
    void testCreate_VentaDetalleSinVenta_DebeRetornarUnprocessableEntity() {
        // Arrange
        VentaDetalle nuevaVentaDetalle = new VentaDetalle();
        nuevaVentaDetalle.setCantidad(new BigDecimal("1.00"));
        // No establecer Venta

        Producto productoRelacion = new Producto();
        productoRelacion.setId(productoId);
        nuevaVentaDetalle.setIdProducto(productoRelacion);

        // Act
        Response response = ventaDetalleResource.create(nuevaVentaDetalle, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("Venta is required", response.getHeaderString("Missing-parameter"));
        verify(ventaDAO, never()).findById(any());
        verify(productoDAO, never()).findById(any());
        verify(ventaDetalleDAO, never()).crear(any());
    }

    @Test
    void testCreate_VentaDetalleVentaSinId_DebeRetornarUnprocessableEntity() {
        // Arrange
        VentaDetalle nuevaVentaDetalle = new VentaDetalle();
        nuevaVentaDetalle.setCantidad(new BigDecimal("1.00"));

        Venta ventaSinId = new Venta();
        // No establecer ID
        nuevaVentaDetalle.setIdVenta(ventaSinId);

        Producto productoRelacion = new Producto();
        productoRelacion.setId(productoId);
        nuevaVentaDetalle.setIdProducto(productoRelacion);

        // Act
        Response response = ventaDetalleResource.create(nuevaVentaDetalle, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("Venta is required", response.getHeaderString("Missing-parameter"));
        verify(ventaDAO, never()).findById(any());
        verify(productoDAO, never()).findById(any());
        verify(ventaDetalleDAO, never()).crear(any());
    }

    @Test
    void testCreate_VentaDetalleSinProducto_DebeRetornarUnprocessableEntity() {
        // Arrange
        VentaDetalle nuevaVentaDetalle = new VentaDetalle();
        nuevaVentaDetalle.setCantidad(new BigDecimal("1.00"));

        Venta ventaRelacion = new Venta();
        ventaRelacion.setId(ventaId);
        nuevaVentaDetalle.setIdVenta(ventaRelacion);
        // No establecer Producto

        // Act
        Response response = ventaDetalleResource.create(nuevaVentaDetalle, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("Producto is required", response.getHeaderString("Missing-parameter"));
        verify(ventaDAO, never()).findById(any());
        verify(productoDAO, never()).findById(any());
        verify(ventaDetalleDAO, never()).crear(any());
    }

    @Test
    void testCreate_VentaNoExiste_DebeRetornarUnprocessableEntity() {
        // Arrange
        VentaDetalle nuevaVentaDetalle = new VentaDetalle();
        nuevaVentaDetalle.setCantidad(new BigDecimal("1.00"));

        Venta ventaRelacion = new Venta();
        ventaRelacion.setId(ventaId);
        nuevaVentaDetalle.setIdVenta(ventaRelacion);

        Producto productoRelacion = new Producto();
        productoRelacion.setId(productoId);
        nuevaVentaDetalle.setIdProducto(productoRelacion);

        when(ventaDAO.findById(ventaId)).thenReturn(null);

        // Act
        Response response = ventaDetalleResource.create(nuevaVentaDetalle, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("Venta with id " + ventaId + " does not exist in database", response.getHeaderString("Missing-parameter"));
        verify(ventaDAO).findById(ventaId);
        verify(productoDAO, never()).findById(any());
        verify(ventaDetalleDAO, never()).crear(any());
    }

    @Test
    void testCreate_ProductoNoExiste_DebeRetornarUnprocessableEntity() {
        // Arrange
        VentaDetalle nuevaVentaDetalle = new VentaDetalle();
        nuevaVentaDetalle.setCantidad(new BigDecimal("1.00"));

        Venta ventaRelacion = new Venta();
        ventaRelacion.setId(ventaId);
        nuevaVentaDetalle.setIdVenta(ventaRelacion);

        Producto productoRelacion = new Producto();
        productoRelacion.setId(productoId);
        nuevaVentaDetalle.setIdProducto(productoRelacion);

        when(ventaDAO.findById(ventaId)).thenReturn(venta);
        when(productoDAO.findById(productoId)).thenReturn(null);

        // Act
        Response response = ventaDetalleResource.create(nuevaVentaDetalle, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("Producto with id " + productoId + " does not exist in database", response.getHeaderString("Missing-parameter"));
        verify(ventaDAO).findById(ventaId);
        verify(productoDAO).findById(productoId);
        verify(ventaDetalleDAO, never()).crear(any());
    }

    @Test
    void testCreate_EntityNull_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = ventaDetalleResource.create(null, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("entity must not be null and id must be null", response.getHeaderString("Missing-parameter"));
        verify(ventaDetalleDAO, never()).crear(any());
    }

    @Test
    void testCreate_VentaDetalleConIdNoNull_DebeRetornarUnprocessableEntity() {
        // Arrange
        VentaDetalle ventaDetalleConId = new VentaDetalle();
        ventaDetalleConId.setId(ventaDetalleId); // ID no null
        ventaDetalleConId.setCantidad(new BigDecimal("1.00"));

        // Act
        Response response = ventaDetalleResource.create(ventaDetalleConId, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("entity must not be null and id must be null", response.getHeaderString("Missing-parameter"));
        verify(ventaDetalleDAO, never()).crear(any());
    }

    @Test
    void testCreate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        VentaDetalle nuevaVentaDetalle = new VentaDetalle();
        nuevaVentaDetalle.setCantidad(new BigDecimal("1.00"));

        Venta ventaRelacion = new Venta();
        ventaRelacion.setId(ventaId);
        nuevaVentaDetalle.setIdVenta(ventaRelacion);

        Producto productoRelacion = new Producto();
        productoRelacion.setId(productoId);
        nuevaVentaDetalle.setIdProducto(productoRelacion);

        when(ventaDAO.findById(ventaId)).thenReturn(venta);
        when(productoDAO.findById(productoId)).thenReturn(producto);
        doThrow(new RuntimeException("Error al guardar"))
                .when(ventaDetalleDAO).crear(any());

        // Act
        Response response = ventaDetalleResource.create(nuevaVentaDetalle, uriInfo);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(ventaDetalleDAO).crear(any(VentaDetalle.class));
    }

    // ==================== TESTS DE update ====================

    @Test
    void testUpdate_DatosValidos_DebeRetornarOk() {
        // Arrange
        String idString = ventaDetalleId.toString();
        VentaDetalle actualizacion = new VentaDetalle();
        actualizacion.setCantidad(new BigDecimal("5.00"));
        actualizacion.setPrecio(new BigDecimal("1800.00"));
        actualizacion.setEstado("CANCELADO");
        actualizacion.setObservaciones("Venta actualizada");

        when(ventaDetalleDAO.findById(ventaDetalleId)).thenReturn(ventaDetalle);
        when(ventaDetalleDAO.modificar(any(VentaDetalle.class))).thenReturn(actualizacion);

        // Act
        Response response = ventaDetalleResource.update(idString, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        VentaDetalle resultado = (VentaDetalle) response.getEntity();
        assertEquals(new BigDecimal("5.00"), resultado.getCantidad());
        assertEquals(new BigDecimal("1800.00"), resultado.getPrecio());
        assertEquals("CANCELADO", resultado.getEstado());
        assertEquals("Venta actualizada", resultado.getObservaciones());

        verify(ventaDetalleDAO).findById(ventaDetalleId);
        verify(ventaDetalleDAO).modificar(any(VentaDetalle.class));
        verify(ventaDAO, never()).findById(any());
        verify(productoDAO, never()).findById(any());
    }

    @Test
    void testUpdate_VentaDetalleConNuevaVenta_DebeActualizarCorrectamente() {
        // Arrange
        String idString = ventaDetalleId.toString();
        UUID nuevaVentaId = UUID.fromString("623e4567-e89b-12d3-a456-426614174000");
        Venta nuevaVenta = new Venta();
        nuevaVenta.setId(nuevaVentaId);

        VentaDetalle actualizacion = new VentaDetalle();
        actualizacion.setCantidad(new BigDecimal("2.00"));

        Venta ventaRelacion = new Venta();
        ventaRelacion.setId(nuevaVentaId);
        actualizacion.setIdVenta(ventaRelacion);

        when(ventaDetalleDAO.findById(ventaDetalleId)).thenReturn(ventaDetalle);
        when(ventaDAO.findById(nuevaVentaId)).thenReturn(nuevaVenta);
        when(ventaDetalleDAO.modificar(any(VentaDetalle.class))).thenReturn(actualizacion);

        // Act
        Response response = ventaDetalleResource.update(idString, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(ventaDAO).findById(nuevaVentaId);
        verify(ventaDetalleDAO).modificar(any(VentaDetalle.class));
    }

    @Test
    void testUpdate_VentaDetalleConNuevoProducto_DebeActualizarCorrectamente() {
        // Arrange
        String idString = ventaDetalleId.toString();
        UUID nuevoProductoId = UUID.fromString("723e4567-e89b-12d3-a456-426614174000");
        Producto nuevoProducto = new Producto();
        nuevoProducto.setId(nuevoProductoId);

        VentaDetalle actualizacion = new VentaDetalle();
        actualizacion.setCantidad(new BigDecimal("2.00"));

        Producto productoRelacion = new Producto();
        productoRelacion.setId(nuevoProductoId);
        actualizacion.setIdProducto(productoRelacion);

        when(ventaDetalleDAO.findById(ventaDetalleId)).thenReturn(ventaDetalle);
        when(productoDAO.findById(nuevoProductoId)).thenReturn(nuevoProducto);
        when(ventaDetalleDAO.modificar(any(VentaDetalle.class))).thenReturn(actualizacion);

        // Act
        Response response = ventaDetalleResource.update(idString, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(productoDAO).findById(nuevoProductoId);
        verify(ventaDetalleDAO).modificar(any(VentaDetalle.class));
    }

    @Test
    void testUpdate_VentaDetalleConVentaNoExiste_DebeRetornarUnprocessableEntity() {
        // Arrange
        String idString = ventaDetalleId.toString();
        UUID ventaNoExistente = UUID.fromString("999e4567-e89b-12d3-a456-426614174000");

        VentaDetalle actualizacion = new VentaDetalle();
        actualizacion.setCantidad(new BigDecimal("2.00"));

        Venta ventaRelacion = new Venta();
        ventaRelacion.setId(ventaNoExistente);
        actualizacion.setIdVenta(ventaRelacion);

        when(ventaDetalleDAO.findById(ventaDetalleId)).thenReturn(ventaDetalle);
        when(ventaDAO.findById(ventaNoExistente)).thenReturn(null);

        // Act
        Response response = ventaDetalleResource.update(idString, actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("Venta with id " + ventaNoExistente + " does not exist in database", response.getHeaderString("Missing-parameter"));
        verify(ventaDAO).findById(ventaNoExistente);
        verify(ventaDetalleDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_VentaDetalleConProductoNoExiste_DebeRetornarUnprocessableEntity() {
        // Arrange
        String idString = ventaDetalleId.toString();
        UUID productoNoExistente = UUID.fromString("999e4567-e89b-12d3-a456-426614174000");

        VentaDetalle actualizacion = new VentaDetalle();
        actualizacion.setCantidad(new BigDecimal("2.00"));

        Producto productoRelacion = new Producto();
        productoRelacion.setId(productoNoExistente);
        actualizacion.setIdProducto(productoRelacion);

        when(ventaDetalleDAO.findById(ventaDetalleId)).thenReturn(ventaDetalle);
        when(productoDAO.findById(productoNoExistente)).thenReturn(null);

        // Act
        Response response = ventaDetalleResource.update(idString, actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("Producto with id " + productoNoExistente + " does not exist in database", response.getHeaderString("Missing-parameter"));
        verify(productoDAO).findById(productoNoExistente);
        verify(ventaDetalleDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_IdNulo_DebeRetornarUnprocessableEntity() {
        // Arrange
        VentaDetalle actualizacion = new VentaDetalle();

        // Act
        Response response = ventaDetalleResource.update(null, actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id and entity must be valid", response.getHeaderString("Missing-parameter"));
        verify(ventaDetalleDAO, never()).findById(any());
        verify(ventaDetalleDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_EntityNull_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = ventaDetalleResource.update(ventaDetalleId.toString(), null);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id and entity must be valid", response.getHeaderString("Missing-parameter"));
        verify(ventaDetalleDAO, never()).findById(any());
        verify(ventaDetalleDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_UUIDInvalido_DebeRetornarUnprocessableEntity() {
        // Arrange
        VentaDetalle actualizacion = new VentaDetalle();

        // Act
        Response response = ventaDetalleResource.update("uuid-invalido", actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("Invalid UUID format", response.getHeaderString("Missing-parameter"));
        verify(ventaDetalleDAO, never()).findById(any());
        verify(ventaDetalleDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_VentaDetalleNoExiste_DebeRetornarNotFound() {
        // Arrange
        UUID idNoExistente = UUID.fromString("999e4567-e89b-12d3-a456-426614174000");
        String idString = idNoExistente.toString();
        VentaDetalle actualizacion = new VentaDetalle();
        when(ventaDetalleDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = ventaDetalleResource.update(idString, actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + idString + " not found", response.getHeaderString("Record-not-found"));
        verify(ventaDetalleDAO).findById(idNoExistente);
        verify(ventaDetalleDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        String idString = ventaDetalleId.toString();
        VentaDetalle actualizacion = new VentaDetalle();
        when(ventaDetalleDAO.findById(ventaDetalleId)).thenReturn(ventaDetalle);
        when(ventaDetalleDAO.modificar(any()))
                .thenThrow(new RuntimeException("Error al actualizar"));

        // Act
        Response response = ventaDetalleResource.update(idString, actualizacion);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(ventaDetalleDAO).findById(ventaDetalleId);
        verify(ventaDetalleDAO).modificar(any(VentaDetalle.class));
    }

    @Test
    void testUpdate_AsignaIdCorrecto_DebeUsarIdDelPath() {
        // Arrange
        String idString = ventaDetalleId.toString();
        VentaDetalle actualizacion = new VentaDetalle();
        actualizacion.setCantidad(new BigDecimal("3.00"));

        when(ventaDetalleDAO.findById(ventaDetalleId)).thenReturn(ventaDetalle);
        when(ventaDetalleDAO.modificar(any(VentaDetalle.class))).thenAnswer(invocation -> {
            VentaDetalle vd = invocation.getArgument(0);
            // Verificar que el ID se asignó correctamente desde el path
            assertEquals(ventaDetalleId, vd.getId());
            return vd;
        });

        // Act
        Response response = ventaDetalleResource.update(idString, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(ventaDetalleDAO).modificar(any(VentaDetalle.class));
    }
}