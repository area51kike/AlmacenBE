package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProducto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProducto;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para ProductoTipoProductoResource
 * Verifica el comportamiento de todos los endpoints REST para relaciones producto-tipo_producto
 */
class ProductoTipoProductoResourceTest {

    @Mock
    private ProductoTipoProductoDAO productoTipoProductoDAO;

    @Mock
    private TipoProductoDAO tipoProductoDAO;

    @Mock
    private ProductoDAO productoDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    private ProductoTipoProductoResource resource;
    private Producto producto;
    private TipoProducto tipoProducto;
    private ProductoTipoProducto ptpRelacion;
    private UUID productoId;
    private UUID ptpId;
    private Long tipoProductoId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resource = new ProductoTipoProductoResource();

        // Inyección manual de los DAOs
        resource.productoTipoProductoDAO = productoTipoProductoDAO;
        resource.tipoProductoDAO = tipoProductoDAO;
        resource.productoDAO = productoDAO;

        // Datos de prueba
        productoId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ptpId = UUID.fromString("223e4567-e89b-12d3-a456-426614174000");
        tipoProductoId = 1L;

        // Configurar Producto
        producto = new Producto();
        producto.setId(productoId);
        producto.setNombreProducto("Laptop Dell XPS");

        // Configurar TipoProducto
        tipoProducto = new TipoProducto();
        tipoProducto.setId(tipoProductoId);
        tipoProducto.setNombre("Electrónicos");

        // Configurar ProductoTipoProducto
        ptpRelacion = new ProductoTipoProducto();
        ptpRelacion.setId(ptpId);
        ptpRelacion.setIdProducto(producto);
        ptpRelacion.setIdTipoProducto(tipoProducto);
        ptpRelacion.setActivo(true);
    }

    // ==================== TESTS DE getTiposDeProducto ====================

    @Test
    void testGetTiposDeProducto_IdProductoNull_DebeRetornarBadRequest() {
        // Act
        Response response = resource.getTiposDeProducto(null);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("El ID del producto no puede ser nulo", response.getEntity());
        verify(productoTipoProductoDAO, never()).findByid(any());
    }

    @Test
    void testGetTiposDeProducto_ProductoConRelaciones_DebeRetornarOk() {
        // Arrange
        List<ProductoTipoProducto> relaciones = new ArrayList<>();
        relaciones.add(ptpRelacion);

        // Agregar segunda relación
        ProductoTipoProducto ptpRelacion2 = new ProductoTipoProducto();
        TipoProducto tipoProducto2 = new TipoProducto();
        tipoProducto2.setId(2L);
        tipoProducto2.setNombre("Tecnología");
        ptpRelacion2.setIdTipoProducto(tipoProducto2);
        relaciones.add(ptpRelacion2);

        when(productoTipoProductoDAO.findByid(productoId)).thenReturn(relaciones);

        // Act
        Response response = resource.getTiposDeProducto(productoId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<TipoProducto> resultado = (List<TipoProducto>) response.getEntity();
        assertEquals(2, resultado.size());
        assertEquals("Electrónicos", resultado.get(0).getNombre());
        assertEquals("Tecnología", resultado.get(1).getNombre());

        verify(productoTipoProductoDAO).findByid(productoId);
    }

    @Test
    void testGetTiposDeProducto_ProductoSinRelaciones_DebeRetornarNotFound() {
        // Arrange
        List<ProductoTipoProducto> relacionesVacias = new ArrayList<>();
        when(productoTipoProductoDAO.findByid(productoId)).thenReturn(relacionesVacias);

        // Act
        Response response = resource.getTiposDeProducto(productoId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("No se encontraron tipos de producto para el producto con id: " + productoId, response.getEntity());
        verify(productoTipoProductoDAO).findByid(productoId);
    }

    @Test
    void testGetTiposDeProducto_RelacionesNull_DebeRetornarNotFound() {
        // Arrange
        when(productoTipoProductoDAO.findByid(productoId)).thenReturn(null);

        // Act
        Response response = resource.getTiposDeProducto(productoId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("No se encontraron tipos de producto para el producto con id: " + productoId, response.getEntity());
        verify(productoTipoProductoDAO).findByid(productoId);
    }

    @Test
    void testGetTiposDeProducto_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(productoTipoProductoDAO.findByid(productoId))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        Response response = resource.getTiposDeProducto(productoId);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al obtener tipos de producto"));
        verify(productoTipoProductoDAO).findByid(productoId);
    }

    // ==================== TESTS DE findById ====================

    @Test
    void testFindById_IdPTPNull_DebeRetornarBadRequest() {
        // Act
        Response response = resource.findById(null, productoId);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("El ID no puede ser nulo", response.getEntity());
        verify(productoTipoProductoDAO, never()).findById(any());
    }

    @Test
    void testFindById_RelacionExisteYCoincideProducto_DebeRetornarOk() {
        // Arrange
        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(ptpRelacion);

        // Act
        Response response = resource.findById(ptpId, productoId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        ProductoTipoProducto resultado = (ProductoTipoProducto) response.getEntity();
        assertEquals(ptpId, resultado.getId());
        assertEquals(productoId, resultado.getIdProducto().getId());

        verify(productoTipoProductoDAO).findById(ptpId);
    }

    @Test
    void testFindById_RelacionNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(null);

        // Act
        Response response = resource.findById(ptpId, productoId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Registro con id " + ptpId + " no encontrado", response.getEntity());
        verify(productoTipoProductoDAO).findById(ptpId);
    }

    @Test
    void testFindById_RelacionPerteneceOtroProducto_DebeRetornarNotFound() {
        // Arrange
        Producto otroProducto = new Producto();
        otroProducto.setId(UUID.fromString("999e4567-e89b-12d3-a456-426614174000"));
        ptpRelacion.setIdProducto(otroProducto);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(ptpRelacion);

        // Act
        Response response = resource.findById(ptpId, productoId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("La relación no pertenece al producto especificado", response.getEntity());
        verify(productoTipoProductoDAO).findById(ptpId);
    }

    @Test
    void testFindById_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(productoTipoProductoDAO.findById(ptpId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = resource.findById(ptpId, productoId);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al buscar el registro"));
        verify(productoTipoProductoDAO).findById(ptpId);
    }

    // ==================== TESTS DE delete ====================

    @Test
    void testDelete_IdPTPNull_DebeRetornarBadRequest() {
        // Act
        Response response = resource.delete(null, productoId);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("El ID no puede ser nulo", response.getEntity());
        verify(productoTipoProductoDAO, never()).findById(any());
    }

    @Test
    void testDelete_RelacionExiste_DebeRetornarNoContent() {
        // Arrange
        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(ptpRelacion);
        doNothing().when(productoTipoProductoDAO).eliminar(ptpRelacion);

        // Act
        Response response = resource.delete(ptpId, productoId);

        // Assert
        assertEquals(204, response.getStatus());
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(productoTipoProductoDAO).eliminar(ptpRelacion);
    }

    @Test
    void testDelete_RelacionNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(null);

        // Act
        Response response = resource.delete(ptpId, productoId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Registro con id " + ptpId + " no encontrado", response.getEntity());
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(productoTipoProductoDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_RelacionPerteneceOtroProducto_DebeRetornarForbidden() {
        // Arrange
        Producto otroProducto = new Producto();
        otroProducto.setId(UUID.fromString("999e4567-e89b-12d3-a456-426614174000"));
        ptpRelacion.setIdProducto(otroProducto);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(ptpRelacion);

        // Act
        Response response = resource.delete(ptpId, productoId);

        // Assert
        assertEquals(403, response.getStatus());
        assertEquals("La relación no pertenece al producto especificado", response.getEntity());
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(productoTipoProductoDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(ptpRelacion);
        doThrow(new RuntimeException("Error al eliminar"))
                .when(productoTipoProductoDAO).eliminar(any());

        // Act
        Response response = resource.delete(ptpId, productoId);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al eliminar el registro"));
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(productoTipoProductoDAO).eliminar(any());
    }

    // ==================== TESTS DE create ====================

    @Test
    void testCreate_IdProductoNull_DebeRetornarBadRequest() {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();

        // Act
        Response response = resource.create(entity, null, tipoProductoId, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("Los IDs de producto y tipo de producto no pueden ser nulos", response.getEntity());
        verify(productoDAO, never()).findById(any());
    }

    @Test
    void testCreate_IdTipoProductoNull_DebeRetornarBadRequest() {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();

        // Act
        Response response = resource.create(entity, productoId, null, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("Los IDs de producto y tipo de producto no pueden ser nulos", response.getEntity());
        verify(productoDAO, never()).findById(any());
    }

    @Test
    void testCreate_ProductoNoExiste_DebeRetornarNotFound() {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();
        when(productoDAO.findById(productoId)).thenReturn(null);

        // Act
        Response response = resource.create(entity, productoId, tipoProductoId, uriInfo);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("No existe el producto con el id: " + productoId, response.getEntity());
        verify(productoDAO).findById(productoId);
        verify(tipoProductoDAO, never()).findById(any());
        verify(productoTipoProductoDAO, never()).crear(any());
    }

    @Test
    void testCreate_TipoProductoNoExiste_DebeRetornarNotFound() {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();
        when(productoDAO.findById(productoId)).thenReturn(producto);
        when(tipoProductoDAO.findById(tipoProductoId)).thenReturn(null);

        // Act
        Response response = resource.create(entity, productoId, tipoProductoId, uriInfo);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("No existe el tipo de producto con el id: " + tipoProductoId, response.getEntity());
        verify(productoDAO).findById(productoId);
        verify(tipoProductoDAO).findById(tipoProductoId);
        verify(productoTipoProductoDAO, never()).crear(any());
    }

    @Test
    void testCreate_DatosValidos_DebeRetornarCreated() throws Exception {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();

        UUID nuevoId = UUID.fromString("323e4567-e89b-12d3-a456-426614174000");
        URI locationUri = new URI("http://localhost:8080/api/producto/" + productoId + "/tipo_producto/" + nuevoId);

        when(productoDAO.findById(productoId)).thenReturn(producto);
        when(tipoProductoDAO.findById(tipoProductoId)).thenReturn(tipoProducto);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        doAnswer(invocation -> {
            ProductoTipoProducto ptp = invocation.getArgument(0);
            ptp.setId(nuevoId);
            return null;
        }).when(productoTipoProductoDAO).crear(any(ProductoTipoProducto.class));

        // Act
        Response response = resource.create(entity, productoId, tipoProductoId, uriInfo);

        // Assert
        assertEquals(201, response.getStatus());
        assertNotNull(response.getLocation());
        assertEquals(locationUri, response.getLocation());
        assertNotNull(response.getEntity());

        verify(productoDAO).findById(productoId);
        verify(tipoProductoDAO).findById(tipoProductoId);
        verify(productoTipoProductoDAO).crear(any(ProductoTipoProducto.class));
    }

    @Test
    void testCreate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();

        when(productoDAO.findById(productoId)).thenReturn(producto);
        when(tipoProductoDAO.findById(tipoProductoId)).thenReturn(tipoProducto);
        doThrow(new RuntimeException("Error al crear"))
                .when(productoTipoProductoDAO).crear(any());

        // Act
        Response response = resource.create(entity, productoId, tipoProductoId, uriInfo);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al crear"));
        verify(productoDAO).findById(productoId);
        verify(tipoProductoDAO).findById(tipoProductoId);
        verify(productoTipoProductoDAO).crear(any(ProductoTipoProducto.class));
    }

    // ==================== TESTS DE update ====================

    @Test
    void testUpdate_IdPTPNull_DebeRetornarBadRequest() {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();

        // Act
        Response response = resource.update(null, productoId, entity);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("El ID no puede ser nulo", response.getEntity());
        verify(productoTipoProductoDAO, never()).findById(any());
    }

    @Test
    void testUpdate_EntityNull_DebeRetornarBadRequest() {
        // Act
        Response response = resource.update(ptpId, productoId, null);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("La entidad no puede ser nula", response.getEntity());
        verify(productoTipoProductoDAO, never()).findById(any());
    }

    @Test
    void testUpdate_RelacionNoExiste_DebeRetornarNotFound() {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();
        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(null);

        // Act
        Response response = resource.update(ptpId, productoId, entity);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Registro con id " + ptpId + " no encontrado", response.getEntity());
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(productoTipoProductoDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_RelacionPerteneceOtroProducto_DebeRetornarForbidden() {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();
        Producto otroProducto = new Producto();
        otroProducto.setId(UUID.fromString("999e4567-e89b-12d3-a456-426614174000"));
        ptpRelacion.setIdProducto(otroProducto);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(ptpRelacion);

        // Act
        Response response = resource.update(ptpId, productoId, entity);

        // Assert
        assertEquals(403, response.getStatus());
        assertEquals("La relación no pertenece al producto especificado", response.getEntity());
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(productoTipoProductoDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ActualizarProductoConProductoNoExiste_DebeRetornarNotFound() {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();
        UUID nuevoProductoId = UUID.fromString("999e4567-e89b-12d3-a456-426614174000");
        Producto nuevoProducto = new Producto();
        nuevoProducto.setId(nuevoProductoId);
        entity.setIdProducto(nuevoProducto);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(ptpRelacion);
        when(productoDAO.findById(nuevoProductoId)).thenReturn(null);

        // Act
        Response response = resource.update(ptpId, productoId, entity);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("No existe el producto con el id: " + nuevoProductoId, response.getEntity());
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(productoDAO).findById(nuevoProductoId);
        verify(productoTipoProductoDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ActualizarTipoProductoConTPNoExiste_DebeRetornarNotFound() {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();
        Long nuevoTipoProductoId = 999L;
        TipoProducto nuevoTipoProducto = new TipoProducto();
        nuevoTipoProducto.setId(nuevoTipoProductoId);
        entity.setIdTipoProducto(nuevoTipoProducto);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(ptpRelacion);
        when(tipoProductoDAO.findById(nuevoTipoProductoId)).thenReturn(null);

        // Act
        Response response = resource.update(ptpId, productoId, entity);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("No existe el tipo de producto con el id: " + nuevoTipoProductoId, response.getEntity());
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(tipoProductoDAO).findById(nuevoTipoProductoId);
        verify(productoTipoProductoDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ActualizarSoloProducto_DebeActualizarCorrectamente() {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();
        UUID nuevoProductoId = UUID.fromString("999e4567-e89b-12d3-a456-426614174000");
        Producto nuevoProducto = new Producto();
        nuevoProducto.setId(nuevoProductoId);
        entity.setIdProducto(nuevoProducto);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(ptpRelacion);
        when(productoDAO.findById(nuevoProductoId)).thenReturn(nuevoProducto);
        when(productoTipoProductoDAO.modificar(any(ProductoTipoProducto.class))).thenReturn(ptpRelacion);

        // Act
        Response response = resource.update(ptpId, productoId, entity);

        // Assert
        assertEquals(200, response.getStatus());
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(productoDAO).findById(nuevoProductoId);
        verify(productoTipoProductoDAO).modificar(any(ProductoTipoProducto.class));
    }

    @Test
    void testUpdate_ActualizarSoloTipoProducto_DebeActualizarCorrectamente() {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();
        Long nuevoTipoProductoId = 999L;
        TipoProducto nuevoTipoProducto = new TipoProducto();
        nuevoTipoProducto.setId(nuevoTipoProductoId);
        entity.setIdTipoProducto(nuevoTipoProducto);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(ptpRelacion);
        when(tipoProductoDAO.findById(nuevoTipoProductoId)).thenReturn(nuevoTipoProducto);
        when(productoTipoProductoDAO.modificar(any(ProductoTipoProducto.class))).thenReturn(ptpRelacion);

        // Act
        Response response = resource.update(ptpId, productoId, entity);

        // Assert
        assertEquals(200, response.getStatus());
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(tipoProductoDAO).findById(nuevoTipoProductoId);
        verify(productoTipoProductoDAO).modificar(any(ProductoTipoProducto.class));
    }

    @Test
    void testUpdate_ActualizarAmbos_DebeActualizarCorrectamente() {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();

        UUID nuevoProductoId = UUID.fromString("999e4567-e89b-12d3-a456-426614174000");
        Producto nuevoProducto = new Producto();
        nuevoProducto.setId(nuevoProductoId);
        entity.setIdProducto(nuevoProducto);

        Long nuevoTipoProductoId = 999L;
        TipoProducto nuevoTipoProducto = new TipoProducto();
        nuevoTipoProducto.setId(nuevoTipoProductoId);
        entity.setIdTipoProducto(nuevoTipoProducto);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(ptpRelacion);
        when(productoDAO.findById(nuevoProductoId)).thenReturn(nuevoProducto);
        when(tipoProductoDAO.findById(nuevoTipoProductoId)).thenReturn(nuevoTipoProducto);
        when(productoTipoProductoDAO.modificar(any(ProductoTipoProducto.class))).thenReturn(ptpRelacion);

        // Act
        Response response = resource.update(ptpId, productoId, entity);

        // Assert
        assertEquals(200, response.getStatus());
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(productoDAO).findById(nuevoProductoId);
        verify(tipoProductoDAO).findById(nuevoTipoProductoId);
        verify(productoTipoProductoDAO).modificar(any(ProductoTipoProducto.class));
    }

    @Test
    void testUpdate_SinCambiosEnRelaciones_DebeActualizarCorrectamente() {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();
        // No establecer relaciones en entity

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(ptpRelacion);
        when(productoTipoProductoDAO.modificar(any(ProductoTipoProducto.class))).thenReturn(ptpRelacion);

        // Act
        Response response = resource.update(ptpId, productoId, entity);

        // Assert
        assertEquals(200, response.getStatus());
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(productoTipoProductoDAO).modificar(any(ProductoTipoProducto.class));
        verify(productoDAO, never()).findById(any());
        verify(tipoProductoDAO, never()).findById(any());
    }

    @Test
    void testUpdate_ProductoEnBodySinId_DebeIgnorarActualizacion() {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();
        Producto productoSinId = new Producto();
        // No establecer ID
        entity.setIdProducto(productoSinId);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(ptpRelacion);
        when(productoTipoProductoDAO.modificar(any(ProductoTipoProducto.class))).thenReturn(ptpRelacion);

        // Act
        Response response = resource.update(ptpId, productoId, entity);

        // Assert
        assertEquals(200, response.getStatus());
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(productoTipoProductoDAO).modificar(any(ProductoTipoProducto.class));
        verify(productoDAO, never()).findById(any());
    }

    @Test
    void testUpdate_TipoProductoEnBodySinId_DebeIgnorarActualizacion() {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();
        TipoProducto tipoProductoSinId = new TipoProducto();
        // No establecer ID
        entity.setIdTipoProducto(tipoProductoSinId);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(ptpRelacion);
        when(productoTipoProductoDAO.modificar(any(ProductoTipoProducto.class))).thenReturn(ptpRelacion);

        // Act
        Response response = resource.update(ptpId, productoId, entity);

        // Assert
        assertEquals(200, response.getStatus());
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(productoTipoProductoDAO).modificar(any(ProductoTipoProducto.class));
        verify(tipoProductoDAO, never()).findById(any());
    }

    @Test
    void testUpdate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        ProductoTipoProducto entity = new ProductoTipoProducto();
        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(ptpRelacion);
        when(productoTipoProductoDAO.modificar(any()))
                .thenThrow(new RuntimeException("Error al actualizar"));

        // Act
        Response response = resource.update(ptpId, productoId, entity);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al actualizar"));
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(productoTipoProductoDAO).modificar(any(ProductoTipoProducto.class));
    }
}