package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProducto;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests para TipoProductoResource
 * Verifica el comportamiento de todos los endpoints REST para tipos de producto
 */
class TipoProductoResourceTest {

    @Mock
    private TipoProductoDAO tipoProductoDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    private TipoProductoResource tipoProductoResource;
    private TipoProducto tipoProducto;
    private Long tipoProductoId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tipoProductoResource = new TipoProductoResource();

        // Inyección manual del DAO
        tipoProductoResource.tipoProductoDao = tipoProductoDAO;

        // Datos de prueba
        tipoProductoId = 1L;

        tipoProducto = new TipoProducto();
        tipoProducto.setId(tipoProductoId);
        tipoProducto.setNombre("Electrónicos");
        tipoProducto.setActivo(true);
        tipoProducto.setComentarios("Productos electrónicos en general");
    }

    // ==================== TESTS DE findRange ====================

    @Test
    void testFindRange_ParametrosValidos_DebeRetornarOkConHeader() {
        // Arrange
        List<TipoProducto> tiposProducto = new ArrayList<>();
        tiposProducto.add(tipoProducto);

        TipoProducto tipoProducto2 = new TipoProducto();
        tipoProducto2.setId(2L);
        tipoProducto2.setNombre("Ropa");
        tiposProducto.add(tipoProducto2);

        when(tipoProductoDAO.findRange(0, 50)).thenReturn(tiposProducto);
        when(tipoProductoDAO.count()).thenReturn(100L);

        // Act
        Response response = tipoProductoResource.findRange(0, 50);

        // Assert
        assertEquals(200, response.getStatus(), "Debe retornar OK");
        assertNotNull(response.getEntity());

        assertEquals("100", response.getHeaderString("Total-records"));

        @SuppressWarnings("unchecked")
        List<TipoProducto> resultado = (List<TipoProducto>) response.getEntity();
        assertEquals(2, resultado.size());

        verify(tipoProductoDAO).findRange(0, 50);
        verify(tipoProductoDAO).count();
    }

    @Test
    void testFindRange_FirstNegativo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoProductoResource.findRange(-1, 50);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first o max out of range", response.getHeaderString("Missing.parameter"));
        verify(tipoProductoDAO, never()).findRange(anyInt(), anyInt());
        verify(tipoProductoDAO, never()).count();
    }

    @Test
    void testFindRange_MaxMayor100_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoProductoResource.findRange(0, 101);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first o max out of range", response.getHeaderString("Missing.parameter"));
        verify(tipoProductoDAO, never()).findRange(anyInt(), anyInt());
        verify(tipoProductoDAO, never()).count();
    }

    @Test
    void testFindRange_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(tipoProductoDAO.findRange(0, 50))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        Response response = tipoProductoResource.findRange(0, 50);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(tipoProductoDAO).findRange(0, 50);
    }

    @Test
    void testFindRange_ListaVacia_DebeRetornarOkConListaVacia() {
        // Arrange
        List<TipoProducto> tiposProductoVacios = new ArrayList<>();
        when(tipoProductoDAO.findRange(0, 50)).thenReturn(tiposProductoVacios);
        when(tipoProductoDAO.count()).thenReturn(0L);

        // Act
        Response response = tipoProductoResource.findRange(0, 50);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<TipoProducto> resultado = (List<TipoProducto>) response.getEntity();
        assertTrue(resultado.isEmpty());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void testFindRange_ValoresPorDefecto_DebeUsarFirst0Max50() {
        // Arrange
        List<TipoProducto> tiposProducto = new ArrayList<>();
        tiposProducto.add(tipoProducto);

        when(tipoProductoDAO.findRange(0, 50)).thenReturn(tiposProducto);
        when(tipoProductoDAO.count()).thenReturn(50L);

        // Act
        Response response = tipoProductoResource.findRange(0, 50);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoProductoDAO).findRange(0, 50);
    }

    // ==================== TESTS DE findById ====================

    @Test
    void testFindById_IdValidoTipoProductoExiste_DebeRetornarOk() {
        // Arrange
        when(tipoProductoDAO.findById(tipoProductoId)).thenReturn(tipoProducto);

        // Act
        Response response = tipoProductoResource.findById(tipoProductoId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        TipoProducto resultado = (TipoProducto) response.getEntity();
        assertEquals(tipoProductoId, resultado.getId());
        assertEquals("Electrónicos", resultado.getNombre());
        assertEquals(true, resultado.getActivo());

        verify(tipoProductoDAO).findById(tipoProductoId);
    }

    @Test
    void testFindById_IdNulo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoProductoResource.findById(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first o max out of range", response.getHeaderString("Missing.parameter"));
        verify(tipoProductoDAO, never()).findById(any());
    }

    @Test
    void testFindById_IdCero_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoProductoResource.findById(0L);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first o max out of range", response.getHeaderString("Missing.parameter"));
        verify(tipoProductoDAO, never()).findById(any());
    }

    @Test
    void testFindById_IdNegativo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoProductoResource.findById(-1L);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first o max out of range", response.getHeaderString("Missing.parameter"));
        verify(tipoProductoDAO, never()).findById(any());
    }

    @Test
    void testFindById_TipoProductoNoExiste_DebeRetornarNotFound() {
        // Arrange
        Long idNoExistente = 999L;
        when(tipoProductoDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = tipoProductoResource.findById(idNoExistente);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + idNoExistente + " not found", response.getHeaderString("Record-not-found"));
        verify(tipoProductoDAO).findById(idNoExistente);
    }

    @Test
    void testFindById_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(tipoProductoDAO.findById(tipoProductoId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = tipoProductoResource.findById(tipoProductoId);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(tipoProductoDAO).findById(tipoProductoId);
    }

    @Test
    void testFindById_TipoProductoInactivo_DebeRetornarOk() {
        // Arrange
        tipoProducto.setActivo(false);
        when(tipoProductoDAO.findById(tipoProductoId)).thenReturn(tipoProducto);

        // Act
        Response response = tipoProductoResource.findById(tipoProductoId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        TipoProducto resultado = (TipoProducto) response.getEntity();
        assertEquals(false, resultado.getActivo());
        verify(tipoProductoDAO).findById(tipoProductoId);
    }

    // ==================== TESTS DE delete ====================

    @Test
    void testDelete_IdValidoTipoProductoExiste_DebeRetornarNoContent() {
        // Arrange
        when(tipoProductoDAO.findById(tipoProductoId)).thenReturn(tipoProducto);
        doNothing().when(tipoProductoDAO).eliminar(tipoProducto);

        // Act
        Response response = tipoProductoResource.delete(tipoProductoId);

        // Assert
        assertEquals(204, response.getStatus(), "Debe retornar NO_CONTENT");
        verify(tipoProductoDAO).findById(tipoProductoId);
        verify(tipoProductoDAO).eliminar(tipoProducto);
    }

    @Test
    void testDelete_IdNulo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoProductoResource.delete(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first o max out of range", response.getHeaderString("Missing.parameter"));
        verify(tipoProductoDAO, never()).findById(any());
        verify(tipoProductoDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_IdCero_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoProductoResource.delete(0L);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first o max out of range", response.getHeaderString("Missing.parameter"));
        verify(tipoProductoDAO, never()).findById(any());
        verify(tipoProductoDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_IdNegativo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoProductoResource.delete(-1L);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first o max out of range", response.getHeaderString("Missing.parameter"));
        verify(tipoProductoDAO, never()).findById(any());
        verify(tipoProductoDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_TipoProductoNoExiste_DebeRetornarNotFound() {
        // Arrange
        Long idNoExistente = 999L;
        when(tipoProductoDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = tipoProductoResource.delete(idNoExistente);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + idNoExistente + " not found", response.getHeaderString("Record-not-found"));
        verify(tipoProductoDAO).findById(idNoExistente);
        verify(tipoProductoDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(tipoProductoDAO.findById(tipoProductoId)).thenReturn(tipoProducto);
        doThrow(new RuntimeException("Error al eliminar"))
                .when(tipoProductoDAO).eliminar(any());

        // Act
        Response response = tipoProductoResource.delete(tipoProductoId);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(tipoProductoDAO).findById(tipoProductoId);
        verify(tipoProductoDAO).eliminar(tipoProducto);
    }

    @Test
    void testDelete_TipoProductoInactivo_DebeEliminarCorrectamente() {
        // Arrange
        tipoProducto.setActivo(false);
        when(tipoProductoDAO.findById(tipoProductoId)).thenReturn(tipoProducto);
        doNothing().when(tipoProductoDAO).eliminar(tipoProducto);

        // Act
        Response response = tipoProductoResource.delete(tipoProductoId);

        // Assert
        assertEquals(204, response.getStatus());
        verify(tipoProductoDAO).eliminar(tipoProducto);
    }

    // ==================== TESTS DE create ====================

    @Test
    void testCreate_TipoProductoValido_DebeRetornarCreated() throws Exception {
        // Arrange
        TipoProducto nuevoTipoProducto = new TipoProducto();
        nuevoTipoProducto.setNombre("Nuevo Tipo Producto");
        nuevoTipoProducto.setActivo(true);
        nuevoTipoProducto.setComentarios("Comentarios del nuevo tipo");
        // ID debe ser null para crear
        // Sin padre

        Long nuevoId = 100L;
        URI locationUri = new URI("http://localhost:8080/api/tipo_producto/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        // Simular que después de crear, se asigna un ID
        doAnswer(invocation -> {
            TipoProducto tp = invocation.getArgument(0);
            tp.setId(nuevoId);
            return null;
        }).when(tipoProductoDAO).crear(any(TipoProducto.class));

        // Act
        Response response = tipoProductoResource.create(nuevoTipoProducto, uriInfo);

        // Assert
        assertEquals(201, response.getStatus(), "Debe retornar CREATED");
        assertNotNull(response.getLocation());
        assertEquals(locationUri, response.getLocation());

        verify(tipoProductoDAO).crear(any(TipoProducto.class));
        verify(tipoProductoDAO, never()).findById(any()); // No se busca padre
    }

    @Test
    void testCreate_TipoProductoConPadreValido_DebeRetornarCreated() throws Exception {
        // Arrange
        TipoProducto padre = new TipoProducto();
        padre.setId(50L);
        padre.setNombre("Categoría Principal");

        TipoProducto nuevoTipoProducto = new TipoProducto();
        nuevoTipoProducto.setNombre("Subcategoría");
        nuevoTipoProducto.setIdTipoProductoPadre(padre);

        Long nuevoId = 101L;
        URI locationUri = new URI("http://localhost:8080/api/tipo_producto/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);
        when(tipoProductoDAO.findById(50L)).thenReturn(padre);

        doAnswer(invocation -> {
            TipoProducto tp = invocation.getArgument(0);
            tp.setId(nuevoId);
            return null;
        }).when(tipoProductoDAO).crear(any(TipoProducto.class));

        // Act
        Response response = tipoProductoResource.create(nuevoTipoProducto, uriInfo);

        // Assert
        assertEquals(201, response.getStatus());
        assertNotNull(response.getLocation());
        assertEquals(locationUri, response.getLocation());

        verify(tipoProductoDAO).findById(50L); // Se busca el padre
        verify(tipoProductoDAO).crear(any(TipoProducto.class));
    }

    @Test
    void testCreate_TipoProductoConPadreNoExiste_DebeRetornarUnprocessableEntity() {
        // Arrange
        TipoProducto padre = new TipoProducto();
        padre.setId(999L); // ID que no existe

        TipoProducto nuevoTipoProducto = new TipoProducto();
        nuevoTipoProducto.setNombre("Tipo con padre inexistente");
        nuevoTipoProducto.setIdTipoProductoPadre(padre);

        when(tipoProductoDAO.findById(999L)).thenReturn(null);

        // Act
        Response response = tipoProductoResource.create(nuevoTipoProducto, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("Parent type product does not exist", response.getHeaderString("Missing.parameter"));
        verify(tipoProductoDAO).findById(999L);
        verify(tipoProductoDAO, never()).crear(any());
    }

    @Test
    void testCreate_TipoProductoConPadreSinId_DebeCrearCorrectamente() throws Exception {
        // Arrange
        TipoProducto padre = new TipoProducto();
        // No establecer ID (padre sin ID)

        TipoProducto nuevoTipoProducto = new TipoProducto();
        nuevoTipoProducto.setNombre("Tipo con padre sin ID");
        nuevoTipoProducto.setIdTipoProductoPadre(padre);

        Long nuevoId = 102L;
        URI locationUri = new URI("http://localhost:8080/api/tipo_producto/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        doAnswer(invocation -> {
            TipoProducto tp = invocation.getArgument(0);
            tp.setId(nuevoId);
            return null;
        }).when(tipoProductoDAO).crear(any(TipoProducto.class));

        // Act
        Response response = tipoProductoResource.create(nuevoTipoProducto, uriInfo);

        // Assert
        assertEquals(201, response.getStatus());
        verify(tipoProductoDAO, never()).findById(any()); // No se busca padre porque no tiene ID
        verify(tipoProductoDAO).crear(any(TipoProducto.class));
    }

    @Test
    void testCreate_EntityNull_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoProductoResource.create(null, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("entity must not be null and id must be null", response.getHeaderString("Missing.parameter"));
        verify(tipoProductoDAO, never()).crear(any());
    }

    @Test
    void testCreate_TipoProductoConIdNoNull_DebeRetornarUnprocessableEntity() {
        // Arrange
        TipoProducto tipoProductoConId = new TipoProducto();
        tipoProductoConId.setId(tipoProductoId); // ID no null
        tipoProductoConId.setNombre("Tipo Producto con ID");

        // Act
        Response response = tipoProductoResource.create(tipoProductoConId, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("entity must not be null and id must be null", response.getHeaderString("Missing.parameter"));
        verify(tipoProductoDAO, never()).crear(any());
    }

    @Test
    void testCreate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        TipoProducto nuevoTipoProducto = new TipoProducto();
        nuevoTipoProducto.setNombre("Nuevo Tipo Producto");

        doThrow(new RuntimeException("Error al guardar"))
                .when(tipoProductoDAO).crear(any());

        // Act
        Response response = tipoProductoResource.create(nuevoTipoProducto, uriInfo);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(tipoProductoDAO).crear(any(TipoProducto.class));
    }

    @Test
    void testCreate_TipoProductoConCamposMinimos_DebeCrearCorrectamente() throws Exception {
        // Arrange
        TipoProducto tipoProductoMinimo = new TipoProducto();
        tipoProductoMinimo.setNombre("Tipo Mínimo");
        // No tiene activo, comentarios, etc.

        Long nuevoId = 200L;
        URI locationUri = new URI("http://localhost:8080/api/tipo_producto/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        doAnswer(invocation -> {
            TipoProducto tp = invocation.getArgument(0);
            tp.setId(nuevoId);
            return null;
        }).when(tipoProductoDAO).crear(any(TipoProducto.class));

        // Act
        Response response = tipoProductoResource.create(tipoProductoMinimo, uriInfo);

        // Assert
        assertEquals(201, response.getStatus());
        verify(tipoProductoDAO).crear(any(TipoProducto.class));
    }

    @Test
    void testCreate_TipoProductoSinPadre_DebeCrearCorrectamente() throws Exception {
        // Arrange
        TipoProducto nuevoTipoProducto = new TipoProducto();
        nuevoTipoProducto.setNombre("Tipo sin Padre");
        nuevoTipoProducto.setActivo(true);
        // No establecer padre

        Long nuevoId = 300L;
        URI locationUri = new URI("http://localhost:8080/api/tipo_producto/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        doAnswer(invocation -> {
            TipoProducto tp = invocation.getArgument(0);
            tp.setId(nuevoId);
            return null;
        }).when(tipoProductoDAO).crear(any(TipoProducto.class));

        // Act
        Response response = tipoProductoResource.create(nuevoTipoProducto, uriInfo);

        // Assert
        assertEquals(201, response.getStatus());
        verify(tipoProductoDAO).crear(any(TipoProducto.class));
        verify(tipoProductoDAO, never()).findById(any()); // No se busca padre
    }

    @Test
    void testCreate_TipoProductoConPadreNull_DebeCrearCorrectamente() throws Exception {
        // Arrange
        TipoProducto nuevoTipoProducto = new TipoProducto();
        nuevoTipoProducto.setNombre("Tipo con Padre Null");
        nuevoTipoProducto.setIdTipoProductoPadre(null); // Padre explícitamente null

        Long nuevoId = 400L;
        URI locationUri = new URI("http://localhost:8080/api/tipo_producto/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        doAnswer(invocation -> {
            TipoProducto tp = invocation.getArgument(0);
            tp.setId(nuevoId);
            return null;
        }).when(tipoProductoDAO).crear(any(TipoProducto.class));

        // Act
        Response response = tipoProductoResource.create(nuevoTipoProducto, uriInfo);

        // Assert
        assertEquals(201, response.getStatus());
        verify(tipoProductoDAO).crear(any(TipoProducto.class));
        verify(tipoProductoDAO, never()).findById(any()); // No se busca padre
    }

}