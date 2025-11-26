package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests para ProductoResource
 * Verifica el comportamiento de todos los endpoints REST
 */
class ProductoResourceTest {

    @Mock
    private ProductoDAO productoDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    private ProductoResource productoResource;
    private Producto producto;
    private UUID productoId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productoResource = new ProductoResource();

        // Inyección manual del DAO (sin private en ProductoResource)
        productoResource.productoDAO = productoDAO;

        // Datos de prueba
        productoId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        producto = new Producto();
        producto.setId(productoId);
        producto.setNombreProducto("Laptop Dell");
        producto.setReferenciaExterna("REF-LAP-001");
        producto.setActivo(true);
        producto.setComentarios("Laptop para desarrollo");
    }

    // ==================== TESTS DE findRange ====================

    @Test
    void testFindRange_ParametrosValidos_DebeRetornarOkConHeader() {
        // Arrange
        List<Producto> productos = new ArrayList<>();
        productos.add(producto);

        Producto producto2 = new Producto();
        producto2.setId(UUID.fromString("223e4567-e89b-12d3-a456-426614174000"));
        producto2.setNombreProducto("Monitor HP");
        productos.add(producto2);

        when(productoDAO.findRange(0, 50)).thenReturn(productos);
        when(productoDAO.count()).thenReturn(100L);

        // Act
        Response response = productoResource.findRange(0, 50);

        // Assert
        assertEquals(200, response.getStatus(), "Debe retornar OK");
        assertNotNull(response.getEntity());
        assertEquals("100", response.getHeaderString("Total-records"));

        @SuppressWarnings("unchecked")
        List<Producto> resultado = (List<Producto>) response.getEntity();
        assertEquals(2, resultado.size());

        verify(productoDAO).findRange(0, 50);
        verify(productoDAO).count();
    }

    @Test
    void testFindRange_FirstNegativo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = productoResource.findRange(-1, 50);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(productoDAO, never()).findRange(anyInt(), anyInt());
        verify(productoDAO, never()).count();
    }

    @Test
    void testFindRange_MaxMayor100_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = productoResource.findRange(0, 101);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(productoDAO, never()).findRange(anyInt(), anyInt());
        verify(productoDAO, never()).count();
    }

    @Test
    void testFindRange_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(productoDAO.findRange(0, 50))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        Response response = productoResource.findRange(0, 50);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(productoDAO).findRange(0, 50);
    }

    // ==================== TESTS DE findById ====================

    @Test
    void testFindById_IdValidoProductoExiste_DebeRetornarOk() {
        // Arrange
        when(productoDAO.findById(productoId)).thenReturn(producto);

        // Act
        Response response = productoResource.findById(productoId.toString());

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        Producto resultado = (Producto) response.getEntity();
        assertEquals(productoId, resultado.getId());
        assertEquals("Laptop Dell", resultado.getNombreProducto());

        verify(productoDAO).findById(productoId);
    }

    @Test
    void testFindById_IdNulo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = productoResource.findById(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(productoDAO, never()).findById(any());
    }

    @Test
    void testFindById_IdVacio_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = productoResource.findById("   ");

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(productoDAO, never()).findById(any());
    }

    @Test
    void testFindById_UUIDInvalido_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = productoResource.findById("uuid-invalido");

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(productoDAO, never()).findById(any());
    }

    @Test
    void testFindById_ProductoNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(productoDAO.findById(productoId)).thenReturn(null);

        // Act
        Response response = productoResource.findById(productoId.toString());

        // Assert
        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Record-not-found"));
        verify(productoDAO).findById(productoId);
    }

    @Test
    void testFindById_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(productoDAO.findById(productoId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = productoResource.findById(productoId.toString());

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
    }

    // ==================== TESTS DE delete ====================

    @Test
    void testDelete_IdValidoProductoExiste_DebeRetornarNoContent() {
        // Arrange
        when(productoDAO.findById(productoId)).thenReturn(producto);
        doNothing().when(productoDAO).eliminar(producto);

        // Act
        Response response = productoResource.delete(productoId.toString());

        // Assert
        assertEquals(204, response.getStatus(), "Debe retornar NO_CONTENT");
        verify(productoDAO).findById(productoId);
        verify(productoDAO).eliminar(producto);
    }

    @Test
    void testDelete_IdNulo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = productoResource.delete(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(productoDAO, never()).findById(any());
        verify(productoDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_IdVacio_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = productoResource.delete("   ");

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(productoDAO, never()).findById(any());
        verify(productoDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_UUIDInvalido_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = productoResource.delete("uuid-invalido");

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(productoDAO, never()).findById(any());
        verify(productoDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_ProductoNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(productoDAO.findById(productoId)).thenReturn(null);

        // Act
        Response response = productoResource.delete(productoId.toString());

        // Assert
        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Record-not-found"));
        verify(productoDAO).findById(productoId);
        verify(productoDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(productoDAO.findById(productoId)).thenReturn(producto);
        doThrow(new RuntimeException("Error al eliminar"))
                .when(productoDAO).eliminar(any());

        // Act
        Response response = productoResource.delete(productoId.toString());

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(productoDAO).findById(productoId);
        verify(productoDAO).eliminar(producto);
    }

    // ==================== TESTS DE create ====================

    @Test
    void testCreate_ProductoValido_DebeRetornarCreated() throws Exception {
        // Arrange
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombreProducto("Teclado Mecánico");
        nuevoProducto.setActivo(true);

        UUID nuevoId = UUID.fromString("323e4567-e89b-12d3-a456-426614174000");
        URI locationUri = new URI("http://localhost:8080/api/producto/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        // Simular que después de crear, se asigna un ID
        doAnswer(invocation -> {
            Producto p = invocation.getArgument(0);
            p.setId(nuevoId);
            return null;
        }).when(productoDAO).crear(any(Producto.class));

        // Act
        Response response = productoResource.create(nuevoProducto, uriInfo);

        // Assert
        assertEquals(201, response.getStatus(), "Debe retornar CREATED");
        assertNotNull(response.getLocation());
        assertEquals(locationUri, response.getLocation());

        verify(productoDAO).crear(any(Producto.class));
    }

    @Test
    void testCreate_EntityNull_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = productoResource.create(null, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(productoDAO, never()).crear(any());
    }

    @Test
    void testCreate_ProductoConIdNoNull_DebeRetornarUnprocessableEntity() {
        // Arrange
        Producto productoConId = new Producto();
        productoConId.setId(productoId);
        productoConId.setNombreProducto("Producto con ID");

        // Act
        Response response = productoResource.create(productoConId, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(productoDAO, never()).crear(any());
    }

    @Test
    void testCreate_ConIllegalArgumentException_DebeRetornarValidationError() {
        // Arrange
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombreProducto("Producto Nuevo");

        doThrow(new IllegalArgumentException("Datos inválidos"))
                .when(productoDAO).crear(any());

        // Act
        Response response = productoResource.create(nuevoProducto, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("Datos inválidos", response.getHeaderString("Validation-error"));
        verify(productoDAO).crear(any(Producto.class));
    }

    @Test
    void testCreate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombreProducto("Producto Nuevo");

        doThrow(new RuntimeException("Error al guardar"))
                .when(productoDAO).crear(any());

        // Act
        Response response = productoResource.create(nuevoProducto, uriInfo);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(productoDAO).crear(any(Producto.class));
    }

    // ==================== TESTS DE update ====================

    @Test
    void testUpdate_DatosValidos_DebeRetornarOk() {
        // Arrange
        Producto actualizacion = new Producto();
        actualizacion.setNombreProducto("Laptop Dell Actualizada");
        actualizacion.setReferenciaExterna("REF-LAP-002");
        actualizacion.setActivo(false);
        actualizacion.setComentarios("Actualizado");

        when(productoDAO.findById(productoId)).thenReturn(producto);
        when(productoDAO.modificar(any(Producto.class))).thenReturn(actualizacion);

        // Act
        Response response = productoResource.update(productoId.toString(), actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        Producto resultado = (Producto) response.getEntity();
        assertEquals("Laptop Dell Actualizada", resultado.getNombreProducto());
        assertEquals("REF-LAP-002", resultado.getReferenciaExterna());
        assertEquals(false, resultado.getActivo());

        verify(productoDAO).findById(productoId);
        verify(productoDAO).modificar(any(Producto.class));
    }

    @Test
    void testUpdate_IdNulo_DebeRetornarUnprocessableEntity() {
        // Arrange
        Producto actualizacion = new Producto();

        // Act
        Response response = productoResource.update(null, actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(productoDAO, never()).findById(any());
        verify(productoDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_EntityNull_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = productoResource.update(productoId.toString(), null);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(productoDAO, never()).findById(any());
        verify(productoDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_IdVacio_DebeRetornarUnprocessableEntity() {
        // Arrange
        Producto actualizacion = new Producto();

        // Act
        Response response = productoResource.update("   ", actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(productoDAO, never()).findById(any());
        verify(productoDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_UUIDInvalido_DebeRetornarUnprocessableEntity() {
        // Arrange
        Producto actualizacion = new Producto();

        // Act
        Response response = productoResource.update("uuid-invalido", actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(productoDAO, never()).findById(any());
        verify(productoDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ProductoNoExiste_DebeRetornarNotFound() {
        // Arrange
        Producto actualizacion = new Producto();
        when(productoDAO.findById(productoId)).thenReturn(null);

        // Act
        Response response = productoResource.update(productoId.toString(), actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Record-not-found"));
        verify(productoDAO).findById(productoId);
        verify(productoDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        Producto actualizacion = new Producto();
        when(productoDAO.findById(productoId)).thenReturn(producto);
        when(productoDAO.modificar(any()))
                .thenThrow(new RuntimeException("Error al actualizar"));

        // Act
        Response response = productoResource.update(productoId.toString(), actualizacion);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(productoDAO).findById(productoId);
        verify(productoDAO).modificar(any(Producto.class));
    }

    @Test
    void testUpdate_AsignaIdCorrecto_DebeUsarIdDelPath() {
        // Arrange
        Producto actualizacion = new Producto();
        actualizacion.setNombreProducto("Nombre Actualizado");

        when(productoDAO.findById(productoId)).thenReturn(producto);
        when(productoDAO.modificar(any(Producto.class))).thenAnswer(invocation -> {
            Producto p = invocation.getArgument(0);
            // Verificar que el ID se asignó correctamente
            assertEquals(productoId, p.getId());
            return p;
        });

        // Act
        Response response = productoResource.update(productoId.toString(), actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(productoDAO).modificar(any(Producto.class));
    }
}