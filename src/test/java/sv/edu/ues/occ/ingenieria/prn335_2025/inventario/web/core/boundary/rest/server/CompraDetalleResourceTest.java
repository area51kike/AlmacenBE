package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.CompraDetalle;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para CompraDetalleResource
 * Verifica el comportamiento de todos los endpoints REST para detalles de compra
 */
class CompraDetalleResourceTest {

    @Mock
    private CompraDetalleDAO compraDetalleDAO;

    @Mock
    private CompraDAO compraDAO;

    @Mock
    private ProductoDAO productoDAO;

    private CompraDetalleResource compraDetalleResource;
    private Compra compra;
    private Producto producto;
    private CompraDetalle compraDetalle;
    private UUID detalleId;
    private Long compraId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        compraDetalleResource = new CompraDetalleResource();

        // Inyección manual de los DAOs
        compraDetalleResource.compraDetalleDAO = compraDetalleDAO;
        compraDetalleResource.compraDAO = compraDAO;
        compraDetalleResource.productoDAO = productoDAO;

        // Datos de prueba
        compraId = 1L;
        detalleId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        compra = new Compra();
        compra.setId(compraId);

        producto = new Producto();
        producto.setId(UUID.fromString("223e4567-e89b-12d3-a456-426614174000"));
        producto.setNombreProducto("Laptop Dell");
        producto.setActivo(true);

        compraDetalle = new CompraDetalle();
        compraDetalle.setId(detalleId);
        compraDetalle.setIdCompra(compra);
        compraDetalle.setIdProducto(producto);
        compraDetalle.setCantidad(new BigDecimal("2.00"));
        compraDetalle.setPrecio(new BigDecimal("1500.00"));
        compraDetalle.setEstado("ACTIVO");
        compraDetalle.setObservaciones("Laptop para desarrollo");
    }

    // ==================== TESTS DE getDetallesByCompra ====================

    @Test
    void testGetDetallesByCompra_CompraExisteConDetalles_DebeRetornarOk() {
        // Arrange
        CompraDetalle detalle2 = new CompraDetalle();
        detalle2.setId(UUID.fromString("323e4567-e89b-12d3-a456-426614174000"));
        detalle2.setIdCompra(compra);

        List<CompraDetalle> todosDetalles = new ArrayList<>();
        todosDetalles.add(compraDetalle);
        todosDetalles.add(detalle2);

        when(compraDAO.findById(compraId)).thenReturn(compra);
        when(compraDetalleDAO.findAll()).thenReturn(todosDetalles);

        // Act
        Response response = compraDetalleResource.getDetallesByCompra(compraId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<CompraDetalle> resultado = (List<CompraDetalle>) response.getEntity();
        assertEquals(2, resultado.size());

        verify(compraDAO).findById(compraId);
        verify(compraDetalleDAO).findAll();
    }

    @Test
    void testGetDetallesByCompra_CompraNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(compraDAO.findById(compraId)).thenReturn(null);

        // Act
        Response response = compraDetalleResource.getDetallesByCompra(compraId);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Compra no encontrada"));
        verify(compraDAO).findById(compraId);
        verify(compraDetalleDAO, never()).findAll();
    }

    @Test
    void testGetDetallesByCompra_CompraExisteSinDetalles_DebeRetornarOkConListaVacia() {
        // Arrange
        List<CompraDetalle> todosDetalles = new ArrayList<>();
        // Agregar un detalle que NO pertenece a esta compra
        CompraDetalle detalleOtraCompra = new CompraDetalle();
        Compra otraCompra = new Compra();
        otraCompra.setId(999L);
        detalleOtraCompra.setIdCompra(otraCompra);
        todosDetalles.add(detalleOtraCompra);

        when(compraDAO.findById(compraId)).thenReturn(compra);
        when(compraDetalleDAO.findAll()).thenReturn(todosDetalles);

        // Act
        Response response = compraDetalleResource.getDetallesByCompra(compraId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<CompraDetalle> resultado = (List<CompraDetalle>) response.getEntity();
        assertTrue(resultado.isEmpty());

        verify(compraDAO).findById(compraId);
        verify(compraDetalleDAO).findAll();
    }

    @Test
    void testGetDetallesByCompra_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(compraDAO.findById(compraId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = compraDetalleResource.getDetallesByCompra(compraId);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al obtener detalles"));
        verify(compraDAO).findById(compraId);
    }

    // ==================== TESTS DE getDetalleById ====================

    @Test
    void testGetDetalleById_DetalleExisteYCoincideCompra_DebeRetornarOk() {
        // Arrange
        when(compraDetalleDAO.findById(detalleId)).thenReturn(compraDetalle);

        // Act
        Response response = compraDetalleResource.getDetalleById(compraId, detalleId.toString());

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        CompraDetalle resultado = (CompraDetalle) response.getEntity();
        assertEquals(detalleId, resultado.getId());

        verify(compraDetalleDAO).findById(detalleId);
    }

    @Test
    void testGetDetalleById_UUIDInvalido_DebeRetornarBadRequest() {
        // Act
        Response response = compraDetalleResource.getDetalleById(compraId, "uuid-invalido");

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("ID de detalle inválido"));
        verify(compraDetalleDAO, never()).findById(any());
    }

    @Test
    void testGetDetalleById_DetalleNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(compraDetalleDAO.findById(detalleId)).thenReturn(null);

        // Act
        Response response = compraDetalleResource.getDetalleById(compraId, detalleId.toString());

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Detalle no encontrado"));
        verify(compraDetalleDAO).findById(detalleId);
    }

    @Test
    void testGetDetalleById_DetalleSinCompra_DebeRetornarBadRequest() {
        // Arrange
        compraDetalle.setIdCompra(null);
        when(compraDetalleDAO.findById(detalleId)).thenReturn(compraDetalle);

        // Act
        Response response = compraDetalleResource.getDetalleById(compraId, detalleId.toString());

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece"));
        verify(compraDetalleDAO).findById(detalleId);
    }

    @Test
    void testGetDetalleById_DetallePerteneceOtraCompra_DebeRetornarBadRequest() {
        // Arrange
        Compra otraCompra = new Compra();
        otraCompra.setId(999L);
        compraDetalle.setIdCompra(otraCompra);
        when(compraDetalleDAO.findById(detalleId)).thenReturn(compraDetalle);

        // Act
        Response response = compraDetalleResource.getDetalleById(compraId, detalleId.toString());

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece"));
        verify(compraDetalleDAO).findById(detalleId);
    }

    // ==================== TESTS DE createDetalle ====================

    @Test
    void testCreateDetalle_DatosValidos_DebeRetornarCreated() {
        // Arrange
        CompraDetalle nuevoDetalle = new CompraDetalle();
        nuevoDetalle.setIdProductoUUID(producto.getId());
        nuevoDetalle.setCantidad(new BigDecimal("3.00"));
        nuevoDetalle.setPrecio(new BigDecimal("500.00"));

        when(compraDAO.findById(compraId)).thenReturn(compra);
        when(productoDAO.findById(producto.getId())).thenReturn(producto);
        doNothing().when(compraDetalleDAO).crear(any(CompraDetalle.class));

        // Act
        Response response = compraDetalleResource.createDetalle(compraId, nuevoDetalle);

        // Assert
        assertEquals(201, response.getStatus());
        assertNotNull(response.getEntity());

        verify(compraDAO).findById(compraId);
        verify(productoDAO).findById(producto.getId());
        verify(compraDetalleDAO).crear(any(CompraDetalle.class));
    }

    @Test
    void testCreateDetalle_CompraNoExiste_DebeRetornarNotFound() {
        // Arrange
        CompraDetalle nuevoDetalle = new CompraDetalle();
        when(compraDAO.findById(compraId)).thenReturn(null);

        // Act
        Response response = compraDetalleResource.createDetalle(compraId, nuevoDetalle);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Compra no encontrada"));
        verify(compraDAO).findById(compraId);
        verify(compraDetalleDAO, never()).crear(any());
    }

    @Test
    void testCreateDetalle_SinIdProducto_DebeRetornarBadRequest() {
        // Arrange
        CompraDetalle nuevoDetalle = new CompraDetalle();
        nuevoDetalle.setCantidad(new BigDecimal("1.00"));
        nuevoDetalle.setPrecio(new BigDecimal("100.00"));

        when(compraDAO.findById(compraId)).thenReturn(compra);

        // Act
        Response response = compraDetalleResource.createDetalle(compraId, nuevoDetalle);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("ID del producto es obligatorio"));
        verify(compraDAO).findById(compraId);
        verify(productoDAO, never()).findById(any());
        verify(compraDetalleDAO, never()).crear(any());
    }

    @Test
    void testCreateDetalle_CantidadCero_DebeRetornarBadRequest() {
        // Arrange
        CompraDetalle nuevoDetalle = new CompraDetalle();
        nuevoDetalle.setIdProductoUUID(producto.getId());
        nuevoDetalle.setCantidad(BigDecimal.ZERO);
        nuevoDetalle.setPrecio(new BigDecimal("100.00"));

        when(compraDAO.findById(compraId)).thenReturn(compra);

        // Act
        Response response = compraDetalleResource.createDetalle(compraId, nuevoDetalle);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("cantidad debe ser mayor a cero"));
        verify(compraDAO).findById(compraId);
        verify(compraDetalleDAO, never()).crear(any());
    }

    @Test
    void testCreateDetalle_PrecioNegativo_DebeRetornarBadRequest() {
        // Arrange
        CompraDetalle nuevoDetalle = new CompraDetalle();
        nuevoDetalle.setIdProductoUUID(producto.getId());
        nuevoDetalle.setCantidad(new BigDecimal("1.00"));
        nuevoDetalle.setPrecio(new BigDecimal("-50.00"));

        when(compraDAO.findById(compraId)).thenReturn(compra);

        // Act
        Response response = compraDetalleResource.createDetalle(compraId, nuevoDetalle);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("precio no puede ser negativo"));
        verify(compraDAO).findById(compraId);
        verify(compraDetalleDAO, never()).crear(any());
    }

    @Test
    void testCreateDetalle_ProductoNoExiste_DebeRetornarNotFound() {
        // Arrange
        CompraDetalle nuevoDetalle = new CompraDetalle();
        nuevoDetalle.setIdProductoUUID(producto.getId());
        nuevoDetalle.setCantidad(new BigDecimal("1.00"));
        nuevoDetalle.setPrecio(new BigDecimal("100.00"));

        when(compraDAO.findById(compraId)).thenReturn(compra);
        when(productoDAO.findById(producto.getId())).thenReturn(null);

        // Act
        Response response = compraDetalleResource.createDetalle(compraId, nuevoDetalle);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Producto no encontrado"));
        verify(compraDAO).findById(compraId);
        verify(productoDAO).findById(producto.getId());
        verify(compraDetalleDAO, never()).crear(any());
    }

    @Test
    void testCreateDetalle_ProductoInactivo_DebeRetornarBadRequest() {
        // Arrange
        producto.setActivo(false);
        CompraDetalle nuevoDetalle = new CompraDetalle();
        nuevoDetalle.setIdProductoUUID(producto.getId());
        nuevoDetalle.setCantidad(new BigDecimal("1.00"));
        nuevoDetalle.setPrecio(new BigDecimal("100.00"));

        when(compraDAO.findById(compraId)).thenReturn(compra);
        when(productoDAO.findById(producto.getId())).thenReturn(producto);

        // Act
        Response response = compraDetalleResource.createDetalle(compraId, nuevoDetalle);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("producto no está activo"));
        verify(compraDAO).findById(compraId);
        verify(productoDAO).findById(producto.getId());
        verify(compraDetalleDAO, never()).crear(any());
    }

    // ==================== TESTS DE updateDetalle ====================

    @Test
    void testUpdateDetalle_DatosValidos_DebeRetornarOk() {
        // Arrange
        CompraDetalle actualizacion = new CompraDetalle();
        actualizacion.setCantidad(new BigDecimal("5.00"));
        actualizacion.setPrecio(new BigDecimal("1200.00"));
        actualizacion.setObservaciones("Actualizado");

        when(compraDetalleDAO.findById(detalleId)).thenReturn(compraDetalle);
        when(compraDetalleDAO.modificar(any(CompraDetalle.class))).thenReturn(compraDetalle);

        // Act
        Response response = compraDetalleResource.updateDetalle(compraId, detalleId.toString(), actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        verify(compraDetalleDAO).findById(detalleId);
        verify(compraDetalleDAO).modificar(any(CompraDetalle.class));
    }

    @Test
    void testUpdateDetalle_UUIDInvalido_DebeRetornarBadRequest() {
        // Arrange
        CompraDetalle actualizacion = new CompraDetalle();

        // Act
        Response response = compraDetalleResource.updateDetalle(compraId, "uuid-invalido", actualizacion);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("ID de detalle inválido"));
        verify(compraDetalleDAO, never()).findById(any());
    }

    @Test
    void testUpdateDetalle_DetalleNoExiste_DebeRetornarNotFound() {
        // Arrange
        CompraDetalle actualizacion = new CompraDetalle();
        when(compraDetalleDAO.findById(detalleId)).thenReturn(null);

        // Act
        Response response = compraDetalleResource.updateDetalle(compraId, detalleId.toString(), actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Detalle no encontrado"));
        verify(compraDetalleDAO).findById(detalleId);
        verify(compraDetalleDAO, never()).modificar(any());
    }

    @Test
    void testUpdateDetalle_DetallePerteneceOtraCompra_DebeRetornarBadRequest() {
        // Arrange
        Compra otraCompra = new Compra();
        otraCompra.setId(999L);
        compraDetalle.setIdCompra(otraCompra);

        CompraDetalle actualizacion = new CompraDetalle();
        when(compraDetalleDAO.findById(detalleId)).thenReturn(compraDetalle);

        // Act
        Response response = compraDetalleResource.updateDetalle(compraId, detalleId.toString(), actualizacion);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece"));
        verify(compraDetalleDAO).findById(detalleId);
        verify(compraDetalleDAO, never()).modificar(any());
    }

    @Test
    void testUpdateDetalle_CantidadInvalida_DebeRetornarBadRequest() {
        // Arrange
        CompraDetalle actualizacion = new CompraDetalle();
        actualizacion.setCantidad(BigDecimal.ZERO);

        when(compraDetalleDAO.findById(detalleId)).thenReturn(compraDetalle);

        // Act
        Response response = compraDetalleResource.updateDetalle(compraId, detalleId.toString(), actualizacion);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("cantidad debe ser mayor a cero"));
        verify(compraDetalleDAO).findById(detalleId);
        verify(compraDetalleDAO, never()).modificar(any());
    }

    @Test
    void testUpdateDetalle_PrecioNegativo_DebeRetornarBadRequest() {
        // Arrange
        CompraDetalle actualizacion = new CompraDetalle();
        actualizacion.setPrecio(new BigDecimal("-10.00"));

        when(compraDetalleDAO.findById(detalleId)).thenReturn(compraDetalle);

        // Act
        Response response = compraDetalleResource.updateDetalle(compraId, detalleId.toString(), actualizacion);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("precio no puede ser negativo"));
        verify(compraDetalleDAO).findById(detalleId);
        verify(compraDetalleDAO, never()).modificar(any());
    }

    // ==================== TESTS DE deleteDetalle ====================

    @Test
    void testDeleteDetalle_DetalleExiste_DebeRetornarNoContent() {
        // Arrange
        when(compraDetalleDAO.findById(detalleId)).thenReturn(compraDetalle);
        doNothing().when(compraDetalleDAO).eliminar(compraDetalle);

        // Act
        Response response = compraDetalleResource.deleteDetalle(compraId, detalleId.toString());

        // Assert
        assertEquals(204, response.getStatus());
        verify(compraDetalleDAO).findById(detalleId);
        verify(compraDetalleDAO).eliminar(compraDetalle);
    }

    @Test
    void testDeleteDetalle_UUIDInvalido_DebeRetornarBadRequest() {
        // Act
        Response response = compraDetalleResource.deleteDetalle(compraId, "uuid-invalido");

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("ID de detalle inválido"));
        verify(compraDetalleDAO, never()).findById(any());
    }

    @Test
    void testDeleteDetalle_DetalleNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(compraDetalleDAO.findById(detalleId)).thenReturn(null);

        // Act
        Response response = compraDetalleResource.deleteDetalle(compraId, detalleId.toString());

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Detalle no encontrado"));
        verify(compraDetalleDAO).findById(detalleId);
        verify(compraDetalleDAO, never()).eliminar(any());
    }

    @Test
    void testDeleteDetalle_DetallePerteneceOtraCompra_DebeRetornarBadRequest() {
        // Arrange
        Compra otraCompra = new Compra();
        otraCompra.setId(999L);
        compraDetalle.setIdCompra(otraCompra);

        when(compraDetalleDAO.findById(detalleId)).thenReturn(compraDetalle);

        // Act
        Response response = compraDetalleResource.deleteDetalle(compraId, detalleId.toString());

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece"));
        verify(compraDetalleDAO).findById(detalleId);
        verify(compraDetalleDAO, never()).eliminar(any());
    }

    // ==================== TESTS DE countDetallesByCompra ====================

    @Test
    void testCountDetallesByCompra_CompraExisteConDetalles_DebeRetornarOk() {
        // Arrange
        List<CompraDetalle> todosDetalles = new ArrayList<>();
        todosDetalles.add(compraDetalle);

        CompraDetalle detalleOtraCompra = new CompraDetalle();
        Compra otraCompra = new Compra();
        otraCompra.setId(999L);
        detalleOtraCompra.setIdCompra(otraCompra);
        todosDetalles.add(detalleOtraCompra);

        when(compraDAO.findById(compraId)).thenReturn(compra);
        when(compraDetalleDAO.findAll()).thenReturn(todosDetalles);

        // Act
        Response response = compraDetalleResource.countDetallesByCompra(compraId);

        // Assert
        assertEquals(200, response.getStatus());
        assertTrue(response.getEntity().toString().contains("\"count\": 1"));

        verify(compraDAO).findById(compraId);
        verify(compraDetalleDAO).findAll();
    }

    @Test
    void testCountDetallesByCompra_CompraNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(compraDAO.findById(compraId)).thenReturn(null);

        // Act
        Response response = compraDetalleResource.countDetallesByCompra(compraId);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Compra no encontrada"));
        verify(compraDAO).findById(compraId);
        verify(compraDetalleDAO, never()).findAll();
    }

    // ==================== TESTS DE getTotalCompra ====================

    @Test
    void testGetTotalCompra_CompraExisteConDetalles_DebeRetornarOk() {
        // Arrange
        CompraDetalle detalle2 = new CompraDetalle();
        detalle2.setIdCompra(compra);
        detalle2.setCantidad(new BigDecimal("1.00"));
        detalle2.setPrecio(new BigDecimal("500.00"));

        List<CompraDetalle> todosDetalles = new ArrayList<>();
        todosDetalles.add(compraDetalle); // 2 * 1500 = 3000
        todosDetalles.add(detalle2);      // 1 * 500 = 500
        // Total esperado: 3500

        when(compraDAO.findById(compraId)).thenReturn(compra);
        when(compraDetalleDAO.findAll()).thenReturn(todosDetalles);

        // Act
        Response response = compraDetalleResource.getTotalCompra(compraId);

        // Assert
        assertEquals(200, response.getStatus());
        assertTrue(response.getEntity().toString().contains("\"total\": 3500"));

        verify(compraDAO).findById(compraId);
        verify(compraDetalleDAO).findAll();
    }

    @Test
    void testGetTotalCompra_CompraNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(compraDAO.findById(compraId)).thenReturn(null);

        // Act
        Response response = compraDetalleResource.getTotalCompra(compraId);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Compra no encontrada"));
        verify(compraDAO).findById(compraId);
        verify(compraDetalleDAO, never()).findAll();
    }

    @Test
    void testGetTotalCompra_DetallesConValoresNulos_DebeCalcularCorrectamente() {
        // Arrange
        CompraDetalle detalleNulo = new CompraDetalle();
        detalleNulo.setIdCompra(compra);
        detalleNulo.setCantidad(null);
        detalleNulo.setPrecio(new BigDecimal("100.00"));

        CompraDetalle detalleNulo2 = new CompraDetalle();
        detalleNulo2.setIdCompra(compra);
        detalleNulo2.setCantidad(new BigDecimal("2.00"));
        detalleNulo2.setPrecio(null);

        List<CompraDetalle> todosDetalles = new ArrayList<>();
        todosDetalles.add(detalleNulo);   // null * 100 = 0
        todosDetalles.add(detalleNulo2);  // 2 * null = 0
        // Total esperado: 0

        when(compraDAO.findById(compraId)).thenReturn(compra);
        when(compraDetalleDAO.findAll()).thenReturn(todosDetalles);

        // Act
        Response response = compraDetalleResource.getTotalCompra(compraId);

        // Assert
        assertEquals(200, response.getStatus());
        assertTrue(response.getEntity().toString().contains("\"total\": 0"));

        verify(compraDAO).findById(compraId);
        verify(compraDetalleDAO).findAll();
    }
}