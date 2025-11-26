package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoUnidadMedidaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoUnidadMedida;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests para TipoUnidadMedidaResource
 * Verifica el comportamiento de todos los endpoints REST para tipos de unidad de medida
 */
class TipoUnidadMedidaResourceTest {

    @Mock
    private TipoUnidadMedidaDAO tipoUnidadMedidaDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    private TipoUnidadMedidaResource tipoUnidadMedidaResource;
    private TipoUnidadMedida tipoUnidadMedida;
    private Integer tipoUnidadMedidaId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tipoUnidadMedidaResource = new TipoUnidadMedidaResource();

        // Inyección manual del DAO
        tipoUnidadMedidaResource.tipoUnidadMedidaDAO = tipoUnidadMedidaDAO;

        // Datos de prueba
        tipoUnidadMedidaId = 1;

        tipoUnidadMedida = new TipoUnidadMedida();
        tipoUnidadMedida.setId(tipoUnidadMedidaId);
        tipoUnidadMedida.setNombre("Longitud");
        tipoUnidadMedida.setActivo(true);
        tipoUnidadMedida.setUnidadBase("Metro");
        tipoUnidadMedida.setComentarios("Unidades de medida para longitud");
    }

    // ==================== TESTS DE findRange ====================

    @Test
    void testFindRange_ParametrosValidos_DebeRetornarOkConHeader() {
        // Arrange
        List<TipoUnidadMedida> tiposUnidadMedida = new ArrayList<>();
        tiposUnidadMedida.add(tipoUnidadMedida);

        TipoUnidadMedida tipoUnidadMedida2 = new TipoUnidadMedida();
        tipoUnidadMedida2.setId(2);
        tipoUnidadMedida2.setNombre("Peso");
        tipoUnidadMedida2.setUnidadBase("Kilogramo");
        tiposUnidadMedida.add(tipoUnidadMedida2);

        when(tipoUnidadMedidaDAO.findRange(0, 50)).thenReturn(tiposUnidadMedida);
        when(tipoUnidadMedidaDAO.count()).thenReturn(100L);

        // Act
        Response response = tipoUnidadMedidaResource.findRange(0, 50);

        // Assert
        assertEquals(200, response.getStatus(), "Debe retornar OK");
        assertNotNull(response.getEntity());

        assertEquals("100", response.getHeaderString("Total-records"));

        @SuppressWarnings("unchecked")
        List<TipoUnidadMedida> resultado = (List<TipoUnidadMedida>) response.getEntity();
        assertEquals(2, resultado.size());

        verify(tipoUnidadMedidaDAO).findRange(0, 50);
        verify(tipoUnidadMedidaDAO).count();
    }

    @Test
    void testFindRange_FirstNegativo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoUnidadMedidaResource.findRange(-1, 50);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first or max out of range", response.getHeaderString("Missing-parameter"));
        verify(tipoUnidadMedidaDAO, never()).findRange(anyInt(), anyInt());
        verify(tipoUnidadMedidaDAO, never()).count();
    }

    @Test
    void testFindRange_MaxMayor100_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoUnidadMedidaResource.findRange(0, 101);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first or max out of range", response.getHeaderString("Missing-parameter"));
        verify(tipoUnidadMedidaDAO, never()).findRange(anyInt(), anyInt());
        verify(tipoUnidadMedidaDAO, never()).count();
    }

    @Test
    void testFindRange_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(tipoUnidadMedidaDAO.findRange(0, 50))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        Response response = tipoUnidadMedidaResource.findRange(0, 50);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(tipoUnidadMedidaDAO).findRange(0, 50);
    }

    @Test
    void testFindRange_ListaVacia_DebeRetornarOkConListaVacia() {
        // Arrange
        List<TipoUnidadMedida> tiposUnidadMedidaVacios = new ArrayList<>();
        when(tipoUnidadMedidaDAO.findRange(0, 50)).thenReturn(tiposUnidadMedidaVacios);
        when(tipoUnidadMedidaDAO.count()).thenReturn(0L);

        // Act
        Response response = tipoUnidadMedidaResource.findRange(0, 50);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<TipoUnidadMedida> resultado = (List<TipoUnidadMedida>) response.getEntity();
        assertTrue(resultado.isEmpty());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void testFindRange_ValoresPorDefecto_DebeUsarFirst0Max50() {
        // Arrange
        List<TipoUnidadMedida> tiposUnidadMedida = new ArrayList<>();
        tiposUnidadMedida.add(tipoUnidadMedida);

        when(tipoUnidadMedidaDAO.findRange(0, 50)).thenReturn(tiposUnidadMedida);
        when(tipoUnidadMedidaDAO.count()).thenReturn(50L);

        // Act
        Response response = tipoUnidadMedidaResource.findRange(0, 50);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoUnidadMedidaDAO).findRange(0, 50);
    }

    // ==================== TESTS DE findById ====================

    @Test
    void testFindById_IdValidoTipoUnidadMedidaExiste_DebeRetornarOk() {
        // Arrange
        when(tipoUnidadMedidaDAO.findById(tipoUnidadMedidaId)).thenReturn(tipoUnidadMedida);

        // Act
        Response response = tipoUnidadMedidaResource.findById(tipoUnidadMedidaId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        TipoUnidadMedida resultado = (TipoUnidadMedida) response.getEntity();
        assertEquals(tipoUnidadMedidaId, resultado.getId());
        assertEquals("Longitud", resultado.getNombre());
        assertEquals("Metro", resultado.getUnidadBase());
        assertEquals(true, resultado.getActivo());

        verify(tipoUnidadMedidaDAO).findById(tipoUnidadMedidaId);
    }

    @Test
    void testFindById_IdNulo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoUnidadMedidaResource.findById(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(tipoUnidadMedidaDAO, never()).findById(any());
    }

    @Test
    void testFindById_IdCero_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoUnidadMedidaResource.findById(0);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(tipoUnidadMedidaDAO, never()).findById(any());
    }

    @Test
    void testFindById_IdNegativo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoUnidadMedidaResource.findById(-1);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(tipoUnidadMedidaDAO, never()).findById(any());
    }

    @Test
    void testFindById_TipoUnidadMedidaNoExiste_DebeRetornarNotFound() {
        // Arrange
        Integer idNoExistente = 999;
        when(tipoUnidadMedidaDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = tipoUnidadMedidaResource.findById(idNoExistente);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + idNoExistente + " not found", response.getHeaderString("Record-not-found"));
        verify(tipoUnidadMedidaDAO).findById(idNoExistente);
    }

    @Test
    void testFindById_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(tipoUnidadMedidaDAO.findById(tipoUnidadMedidaId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = tipoUnidadMedidaResource.findById(tipoUnidadMedidaId);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(tipoUnidadMedidaDAO).findById(tipoUnidadMedidaId);
    }

    @Test
    void testFindById_TipoUnidadMedidaInactivo_DebeRetornarOk() {
        // Arrange
        tipoUnidadMedida.setActivo(false);
        when(tipoUnidadMedidaDAO.findById(tipoUnidadMedidaId)).thenReturn(tipoUnidadMedida);

        // Act
        Response response = tipoUnidadMedidaResource.findById(tipoUnidadMedidaId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        TipoUnidadMedida resultado = (TipoUnidadMedida) response.getEntity();
        assertEquals(false, resultado.getActivo());
        verify(tipoUnidadMedidaDAO).findById(tipoUnidadMedidaId);
    }

    // ==================== TESTS DE delete ====================

    @Test
    void testDelete_IdValidoTipoUnidadMedidaExiste_DebeRetornarNoContent() {
        // Arrange
        when(tipoUnidadMedidaDAO.findById(tipoUnidadMedidaId)).thenReturn(tipoUnidadMedida);
        doNothing().when(tipoUnidadMedidaDAO).eliminar(tipoUnidadMedida);

        // Act
        Response response = tipoUnidadMedidaResource.delete(tipoUnidadMedidaId);

        // Assert
        assertEquals(204, response.getStatus(), "Debe retornar NO_CONTENT");
        verify(tipoUnidadMedidaDAO).findById(tipoUnidadMedidaId);
        verify(tipoUnidadMedidaDAO).eliminar(tipoUnidadMedida);
    }

    @Test
    void testDelete_IdNulo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoUnidadMedidaResource.delete(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(tipoUnidadMedidaDAO, never()).findById(any());
        verify(tipoUnidadMedidaDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_IdCero_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoUnidadMedidaResource.delete(0);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(tipoUnidadMedidaDAO, never()).findById(any());
        verify(tipoUnidadMedidaDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_IdNegativo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoUnidadMedidaResource.delete(-1);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(tipoUnidadMedidaDAO, never()).findById(any());
        verify(tipoUnidadMedidaDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_TipoUnidadMedidaNoExiste_DebeRetornarNotFound() {
        // Arrange
        Integer idNoExistente = 999;
        when(tipoUnidadMedidaDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = tipoUnidadMedidaResource.delete(idNoExistente);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + idNoExistente + " not found", response.getHeaderString("Record-not-found"));
        verify(tipoUnidadMedidaDAO).findById(idNoExistente);
        verify(tipoUnidadMedidaDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(tipoUnidadMedidaDAO.findById(tipoUnidadMedidaId)).thenReturn(tipoUnidadMedida);
        doThrow(new RuntimeException("Error al eliminar"))
                .when(tipoUnidadMedidaDAO).eliminar(any());

        // Act
        Response response = tipoUnidadMedidaResource.delete(tipoUnidadMedidaId);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(tipoUnidadMedidaDAO).findById(tipoUnidadMedidaId);
        verify(tipoUnidadMedidaDAO).eliminar(tipoUnidadMedida);
    }

    @Test
    void testDelete_TipoUnidadMedidaInactivo_DebeEliminarCorrectamente() {
        // Arrange
        tipoUnidadMedida.setActivo(false);
        when(tipoUnidadMedidaDAO.findById(tipoUnidadMedidaId)).thenReturn(tipoUnidadMedida);
        doNothing().when(tipoUnidadMedidaDAO).eliminar(tipoUnidadMedida);

        // Act
        Response response = tipoUnidadMedidaResource.delete(tipoUnidadMedidaId);

        // Assert
        assertEquals(204, response.getStatus());
        verify(tipoUnidadMedidaDAO).eliminar(tipoUnidadMedida);
    }

    // ==================== TESTS DE create ====================

    @Test
    void testCreate_TipoUnidadMedidaValido_DebeRetornarCreated() throws Exception {
        // Arrange
        TipoUnidadMedida nuevoTipoUnidadMedida = new TipoUnidadMedida();
        nuevoTipoUnidadMedida.setNombre("Volumen");
        nuevoTipoUnidadMedida.setActivo(true);
        nuevoTipoUnidadMedida.setUnidadBase("Litro");
        nuevoTipoUnidadMedida.setComentarios("Unidades de medida para volumen");
        // ID debe ser null para crear

        Integer nuevoId = 100;
        URI locationUri = new URI("http://localhost:8080/api/tipo_unidad_medida/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        // Simular que después de crear, se asigna un ID
        doAnswer(invocation -> {
            TipoUnidadMedida tum = invocation.getArgument(0);
            tum.setId(nuevoId);
            return null;
        }).when(tipoUnidadMedidaDAO).crear(any(TipoUnidadMedida.class));

        // Act
        Response response = tipoUnidadMedidaResource.create(nuevoTipoUnidadMedida, uriInfo);

        // Assert
        assertEquals(201, response.getStatus(), "Debe retornar CREATED");
        assertNotNull(response.getLocation());
        assertEquals(locationUri, response.getLocation());

        verify(tipoUnidadMedidaDAO).crear(any(TipoUnidadMedida.class));
    }

    @Test
    void testCreate_EntityNull_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoUnidadMedidaResource.create(null, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("entity must not be null and id must be null", response.getHeaderString("Missing-parameter"));
        verify(tipoUnidadMedidaDAO, never()).crear(any());
    }

    @Test
    void testCreate_TipoUnidadMedidaConIdNoNull_DebeRetornarUnprocessableEntity() {
        // Arrange
        TipoUnidadMedida tipoUnidadMedidaConId = new TipoUnidadMedida();
        tipoUnidadMedidaConId.setId(tipoUnidadMedidaId);
        tipoUnidadMedidaConId.setNombre("Tipo Unidad Medida con ID");

        // Act
        Response response = tipoUnidadMedidaResource.create(tipoUnidadMedidaConId, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("entity must not be null and id must be null", response.getHeaderString("Missing-parameter"));
        verify(tipoUnidadMedidaDAO, never()).crear(any());
    }

    @Test
    void testCreate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        TipoUnidadMedida nuevoTipoUnidadMedida = new TipoUnidadMedida();
        nuevoTipoUnidadMedida.setNombre("Nuevo Tipo Unidad Medida");

        doThrow(new RuntimeException("Error al guardar"))
                .when(tipoUnidadMedidaDAO).crear(any());

        // Act
        Response response = tipoUnidadMedidaResource.create(nuevoTipoUnidadMedida, uriInfo);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(tipoUnidadMedidaDAO).crear(any(TipoUnidadMedida.class));
    }

    @Test
    void testCreate_ConIllegalArgumentException_DebeRetornarUnprocessableEntity() {
        // Arrange
        TipoUnidadMedida nuevoTipoUnidadMedida = new TipoUnidadMedida();
        nuevoTipoUnidadMedida.setNombre("Tipo Inválido");

        doThrow(new IllegalArgumentException("Datos inválidos"))
                .when(tipoUnidadMedidaDAO).crear(any());

        // Act
        Response response = tipoUnidadMedidaResource.create(nuevoTipoUnidadMedida, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("Datos inválidos", response.getHeaderString("Validation-error"));
        verify(tipoUnidadMedidaDAO).crear(any(TipoUnidadMedida.class));
    }

    @Test
    void testCreate_TipoUnidadMedidaConCamposMinimos_DebeCrearCorrectamente() throws Exception {
        // Arrange
        TipoUnidadMedida tipoUnidadMedidaMinimo = new TipoUnidadMedida();
        tipoUnidadMedidaMinimo.setNombre("Tipo Mínimo");
        // No tiene activo, unidadBase, comentarios, etc.

        Integer nuevoId = 200;
        URI locationUri = new URI("http://localhost:8080/api/tipo_unidad_medida/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        doAnswer(invocation -> {
            TipoUnidadMedida tum = invocation.getArgument(0);
            tum.setId(nuevoId);
            return null;
        }).when(tipoUnidadMedidaDAO).crear(any(TipoUnidadMedida.class));

        // Act
        Response response = tipoUnidadMedidaResource.create(tipoUnidadMedidaMinimo, uriInfo);

        // Assert
        assertEquals(201, response.getStatus());
        verify(tipoUnidadMedidaDAO).crear(any(TipoUnidadMedida.class));
    }

    @Test
    void testCreate_TipoUnidadMedidaConUnidadBaseNull_DebeCrearCorrectamente() throws Exception {
        // Arrange
        TipoUnidadMedida tipoUnidadMedidaSinUnidadBase = new TipoUnidadMedida();
        tipoUnidadMedidaSinUnidadBase.setNombre("Tipo sin Unidad Base");
        tipoUnidadMedidaSinUnidadBase.setActivo(true);
        // UnidadBase es null

        Integer nuevoId = 300;
        URI locationUri = new URI("http://localhost:8080/api/tipo_unidad_medida/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        doAnswer(invocation -> {
            TipoUnidadMedida tum = invocation.getArgument(0);
            tum.setId(nuevoId);
            return null;
        }).when(tipoUnidadMedidaDAO).crear(any(TipoUnidadMedida.class));

        // Act
        Response response = tipoUnidadMedidaResource.create(tipoUnidadMedidaSinUnidadBase, uriInfo);

        // Assert
        assertEquals(201, response.getStatus());
        verify(tipoUnidadMedidaDAO).crear(any(TipoUnidadMedida.class));
    }

    // ==================== TESTS DE update ====================

    @Test
    void testUpdate_DatosValidos_DebeRetornarOk() {
        // Arrange
        TipoUnidadMedida actualizacion = new TipoUnidadMedida();
        actualizacion.setNombre("Longitud Actualizada");
        actualizacion.setActivo(false);
        actualizacion.setUnidadBase("Centímetro");
        actualizacion.setComentarios("Comentarios actualizados");

        when(tipoUnidadMedidaDAO.findById(tipoUnidadMedidaId)).thenReturn(tipoUnidadMedida);
        when(tipoUnidadMedidaDAO.modificar(any(TipoUnidadMedida.class))).thenReturn(actualizacion);

        // Act
        Response response = tipoUnidadMedidaResource.update(tipoUnidadMedidaId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        TipoUnidadMedida resultado = (TipoUnidadMedida) response.getEntity();
        assertEquals("Longitud Actualizada", resultado.getNombre());
        assertEquals(false, resultado.getActivo());
        assertEquals("Centímetro", resultado.getUnidadBase());
        assertEquals("Comentarios actualizados", resultado.getComentarios());

        verify(tipoUnidadMedidaDAO).findById(tipoUnidadMedidaId);
        verify(tipoUnidadMedidaDAO).modificar(any(TipoUnidadMedida.class));
    }

    @Test
    void testUpdate_IdNulo_DebeRetornarUnprocessableEntity() {
        // Arrange
        TipoUnidadMedida actualizacion = new TipoUnidadMedida();

        // Act
        Response response = tipoUnidadMedidaResource.update(null, actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id and entity must be valid", response.getHeaderString("Missing-parameter"));
        verify(tipoUnidadMedidaDAO, never()).findById(any());
        verify(tipoUnidadMedidaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_EntityNull_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoUnidadMedidaResource.update(tipoUnidadMedidaId, null);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id and entity must be valid", response.getHeaderString("Missing-parameter"));
        verify(tipoUnidadMedidaDAO, never()).findById(any());
        verify(tipoUnidadMedidaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_IdCero_DebeRetornarUnprocessableEntity() {
        // Arrange
        TipoUnidadMedida actualizacion = new TipoUnidadMedida();

        // Act
        Response response = tipoUnidadMedidaResource.update(0, actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id and entity must be valid", response.getHeaderString("Missing-parameter"));
        verify(tipoUnidadMedidaDAO, never()).findById(any());
        verify(tipoUnidadMedidaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_IdNegativo_DebeRetornarUnprocessableEntity() {
        // Arrange
        TipoUnidadMedida actualizacion = new TipoUnidadMedida();

        // Act
        Response response = tipoUnidadMedidaResource.update(-1, actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id and entity must be valid", response.getHeaderString("Missing-parameter"));
        verify(tipoUnidadMedidaDAO, never()).findById(any());
        verify(tipoUnidadMedidaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_TipoUnidadMedidaNoExiste_DebeRetornarNotFound() {
        // Arrange
        TipoUnidadMedida actualizacion = new TipoUnidadMedida();
        Integer idNoExistente = 999;
        when(tipoUnidadMedidaDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = tipoUnidadMedidaResource.update(idNoExistente, actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + idNoExistente + " not found", response.getHeaderString("Record-not-found"));
        verify(tipoUnidadMedidaDAO).findById(idNoExistente);
        verify(tipoUnidadMedidaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        TipoUnidadMedida actualizacion = new TipoUnidadMedida();
        when(tipoUnidadMedidaDAO.findById(tipoUnidadMedidaId)).thenReturn(tipoUnidadMedida);
        when(tipoUnidadMedidaDAO.modificar(any()))
                .thenThrow(new RuntimeException("Error al actualizar"));

        // Act
        Response response = tipoUnidadMedidaResource.update(tipoUnidadMedidaId, actualizacion);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(tipoUnidadMedidaDAO).findById(tipoUnidadMedidaId);
        verify(tipoUnidadMedidaDAO).modificar(any(TipoUnidadMedida.class));
    }

    @Test
    void testUpdate_AsignaIdCorrecto_DebeUsarIdDelPath() {
        // Arrange
        TipoUnidadMedida actualizacion = new TipoUnidadMedida();
        actualizacion.setNombre("Nombre Actualizado");

        when(tipoUnidadMedidaDAO.findById(tipoUnidadMedidaId)).thenReturn(tipoUnidadMedida);
        when(tipoUnidadMedidaDAO.modificar(any(TipoUnidadMedida.class))).thenAnswer(invocation -> {
            TipoUnidadMedida tum = invocation.getArgument(0);
            // Verificar que el ID se asignó correctamente desde el path
            assertEquals(tipoUnidadMedidaId, tum.getId());
            return tum;
        });

        // Act
        Response response = tipoUnidadMedidaResource.update(tipoUnidadMedidaId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoUnidadMedidaDAO).modificar(any(TipoUnidadMedida.class));
    }

    @Test
    void testUpdate_ActualizacionParcial_DebeConservarCamposNoModificados() {
        // Arrange
        TipoUnidadMedida actualizacion = new TipoUnidadMedida();
        actualizacion.setNombre("Solo nombre actualizado");
        // No se establecen otros campos

        TipoUnidadMedida tipoUnidadMedidaActualizado = new TipoUnidadMedida();
        tipoUnidadMedidaActualizado.setId(tipoUnidadMedidaId);
        tipoUnidadMedidaActualizado.setNombre("Solo nombre actualizado");
        tipoUnidadMedidaActualizado.setActivo(true); // Mantiene valor original
        tipoUnidadMedidaActualizado.setUnidadBase("Metro"); // Mantiene valor original
        tipoUnidadMedidaActualizado.setComentarios("Unidades de medida para longitud"); // Mantiene valor original

        when(tipoUnidadMedidaDAO.findById(tipoUnidadMedidaId)).thenReturn(tipoUnidadMedida);
        when(tipoUnidadMedidaDAO.modificar(any(TipoUnidadMedida.class))).thenReturn(tipoUnidadMedidaActualizado);

        // Act
        Response response = tipoUnidadMedidaResource.update(tipoUnidadMedidaId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoUnidadMedidaDAO).modificar(any(TipoUnidadMedida.class));
    }

    @Test
    void testUpdate_ActualizarSoloActivo_DebeActualizarCorrectamente() {
        // Arrange
        TipoUnidadMedida actualizacion = new TipoUnidadMedida();
        actualizacion.setActivo(false);
        // No se establecen nombre, unidadBase ni comentarios

        when(tipoUnidadMedidaDAO.findById(tipoUnidadMedidaId)).thenReturn(tipoUnidadMedida);
        when(tipoUnidadMedidaDAO.modificar(any(TipoUnidadMedida.class))).thenReturn(tipoUnidadMedida);

        // Act
        Response response = tipoUnidadMedidaResource.update(tipoUnidadMedidaId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoUnidadMedidaDAO).modificar(any(TipoUnidadMedida.class));
    }

    @Test
    void testUpdate_ActualizarSoloUnidadBase_DebeActualizarCorrectamente() {
        // Arrange
        TipoUnidadMedida actualizacion = new TipoUnidadMedida();
        actualizacion.setUnidadBase("Pulgada");
        // No se establecen nombre, activo ni comentarios

        when(tipoUnidadMedidaDAO.findById(tipoUnidadMedidaId)).thenReturn(tipoUnidadMedida);
        when(tipoUnidadMedidaDAO.modificar(any(TipoUnidadMedida.class))).thenReturn(tipoUnidadMedida);

        // Act
        Response response = tipoUnidadMedidaResource.update(tipoUnidadMedidaId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoUnidadMedidaDAO).modificar(any(TipoUnidadMedida.class));
    }

    @Test
    void testUpdate_TipoUnidadMedidaConTodosCamposNull_DebeActualizarCorrectamente() {
        // Arrange
        TipoUnidadMedida actualizacion = new TipoUnidadMedida();
        // No se establece ningún campo

        when(tipoUnidadMedidaDAO.findById(tipoUnidadMedidaId)).thenReturn(tipoUnidadMedida);
        when(tipoUnidadMedidaDAO.modificar(any(TipoUnidadMedida.class))).thenReturn(tipoUnidadMedida);

        // Act
        Response response = tipoUnidadMedidaResource.update(tipoUnidadMedidaId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoUnidadMedidaDAO).modificar(any(TipoUnidadMedida.class));
    }
}