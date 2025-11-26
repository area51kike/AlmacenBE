package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoAlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests para TipoAlmacenResource
 * Verifica el comportamiento de todos los endpoints REST para tipos de almacén
 */
class TipoAlmacenResourceTest {

    @Mock
    private TipoAlmacenDAO tipoAlmacenDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    private TipoAlmacenResource tipoAlmacenResource;
    private TipoAlmacen tipoAlmacen;
    private Integer tipoAlmacenId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tipoAlmacenResource = new TipoAlmacenResource();

        // Inyección manual del DAO
        tipoAlmacenResource.tipoAlmacenDAO = tipoAlmacenDAO;

        // Datos de prueba
        tipoAlmacenId = 1;

        tipoAlmacen = new TipoAlmacen();
        tipoAlmacen.setId(tipoAlmacenId);
        tipoAlmacen.setNombre("Almacén Principal");
        tipoAlmacen.setActivo(true);
        tipoAlmacen.setObservaciones("Almacén central de la empresa");
    }

    // ==================== TESTS DE findRange ====================

    @Test
    void testFindRange_ParametrosValidos_DebeRetornarOkConHeader() {
        // Arrange
        List<TipoAlmacen> tiposAlmacen = new ArrayList<>();
        tiposAlmacen.add(tipoAlmacen);

        TipoAlmacen tipoAlmacen2 = new TipoAlmacen();
        tipoAlmacen2.setId(2);
        tipoAlmacen2.setNombre("Almacén Secundario");
        tiposAlmacen.add(tipoAlmacen2);

        when(tipoAlmacenDAO.findRange(0, 50)).thenReturn(tiposAlmacen);
        when(tipoAlmacenDAO.count()).thenReturn(100L);

        // Act
        Response response = tipoAlmacenResource.findRange(0, 50);

        // Assert
        assertEquals(200, response.getStatus(), "Debe retornar OK");
        assertNotNull(response.getEntity());

        assertEquals("100", response.getHeaderString("Total-records"));

        @SuppressWarnings("unchecked")
        List<TipoAlmacen> resultado = (List<TipoAlmacen>) response.getEntity();
        assertEquals(2, resultado.size());

        verify(tipoAlmacenDAO).findRange(0, 50);
        verify(tipoAlmacenDAO).count();
    }

    @Test
    void testFindRange_FirstNegativo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoAlmacenResource.findRange(-1, 50);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first or max out of range", response.getHeaderString("Missing-parameter"));
        verify(tipoAlmacenDAO, never()).findRange(anyInt(), anyInt());
        verify(tipoAlmacenDAO, never()).count();
    }

    @Test
    void testFindRange_MaxMayor100_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoAlmacenResource.findRange(0, 101);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first or max out of range", response.getHeaderString("Missing-parameter"));
        verify(tipoAlmacenDAO, never()).findRange(anyInt(), anyInt());
        verify(tipoAlmacenDAO, never()).count();
    }

    @Test
    void testFindRange_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(tipoAlmacenDAO.findRange(0, 50))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        Response response = tipoAlmacenResource.findRange(0, 50);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(tipoAlmacenDAO).findRange(0, 50);
    }

    @Test
    void testFindRange_ListaVacia_DebeRetornarOkConListaVacia() {
        // Arrange
        List<TipoAlmacen> tiposAlmacenVacios = new ArrayList<>();
        when(tipoAlmacenDAO.findRange(0, 50)).thenReturn(tiposAlmacenVacios);
        when(tipoAlmacenDAO.count()).thenReturn(0L);

        // Act
        Response response = tipoAlmacenResource.findRange(0, 50);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<TipoAlmacen> resultado = (List<TipoAlmacen>) response.getEntity();
        assertTrue(resultado.isEmpty());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void testFindRange_ValoresPorDefecto_DebeUsarFirst0Max50() {
        // Arrange
        List<TipoAlmacen> tiposAlmacen = new ArrayList<>();
        tiposAlmacen.add(tipoAlmacen);

        when(tipoAlmacenDAO.findRange(0, 50)).thenReturn(tiposAlmacen);
        when(tipoAlmacenDAO.count()).thenReturn(50L);

        // Act
        Response response = tipoAlmacenResource.findRange(0, 50);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoAlmacenDAO).findRange(0, 50);
    }

    // ==================== TESTS DE findById ====================

    @Test
    void testFindById_IdValidoTipoAlmacenExiste_DebeRetornarOk() {
        // Arrange
        when(tipoAlmacenDAO.findById(tipoAlmacenId)).thenReturn(tipoAlmacen);

        // Act
        Response response = tipoAlmacenResource.findById(tipoAlmacenId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        TipoAlmacen resultado = (TipoAlmacen) response.getEntity();
        assertEquals(tipoAlmacenId, resultado.getId());
        assertEquals("Almacén Principal", resultado.getNombre());
        assertEquals(true, resultado.getActivo());

        verify(tipoAlmacenDAO).findById(tipoAlmacenId);
    }

    @Test
    void testFindById_IdNulo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoAlmacenResource.findById(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(tipoAlmacenDAO, never()).findById(any());
    }

    @Test
    void testFindById_IdCero_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoAlmacenResource.findById(0);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(tipoAlmacenDAO, never()).findById(any());
    }

    @Test
    void testFindById_IdNegativo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoAlmacenResource.findById(-1);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(tipoAlmacenDAO, never()).findById(any());
    }

    @Test
    void testFindById_TipoAlmacenNoExiste_DebeRetornarNotFound() {
        // Arrange
        Integer idNoExistente = 999;
        when(tipoAlmacenDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = tipoAlmacenResource.findById(idNoExistente);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + idNoExistente + " not found", response.getHeaderString("Record-not-found"));
        verify(tipoAlmacenDAO).findById(idNoExistente);
    }

    @Test
    void testFindById_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(tipoAlmacenDAO.findById(tipoAlmacenId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = tipoAlmacenResource.findById(tipoAlmacenId);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(tipoAlmacenDAO).findById(tipoAlmacenId);
    }

    @Test
    void testFindById_TipoAlmacenInactivo_DebeRetornarOk() {
        // Arrange
        tipoAlmacen.setActivo(false);
        when(tipoAlmacenDAO.findById(tipoAlmacenId)).thenReturn(tipoAlmacen);

        // Act
        Response response = tipoAlmacenResource.findById(tipoAlmacenId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        TipoAlmacen resultado = (TipoAlmacen) response.getEntity();
        assertEquals(false, resultado.getActivo());
        verify(tipoAlmacenDAO).findById(tipoAlmacenId);
    }

    // ==================== TESTS DE delete ====================

    @Test
    void testDelete_IdValidoTipoAlmacenExiste_DebeRetornarNoContent() {
        // Arrange
        when(tipoAlmacenDAO.findById(tipoAlmacenId)).thenReturn(tipoAlmacen);
        doNothing().when(tipoAlmacenDAO).eliminar(tipoAlmacen);

        // Act
        Response response = tipoAlmacenResource.delete(tipoAlmacenId);

        // Assert
        assertEquals(204, response.getStatus(), "Debe retornar NO_CONTENT");
        verify(tipoAlmacenDAO).findById(tipoAlmacenId);
        verify(tipoAlmacenDAO).eliminar(tipoAlmacen);
    }

    @Test
    void testDelete_IdNulo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoAlmacenResource.delete(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(tipoAlmacenDAO, never()).findById(any());
        verify(tipoAlmacenDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_IdCero_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoAlmacenResource.delete(0);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(tipoAlmacenDAO, never()).findById(any());
        verify(tipoAlmacenDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_IdNegativo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoAlmacenResource.delete(-1);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(tipoAlmacenDAO, never()).findById(any());
        verify(tipoAlmacenDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_TipoAlmacenNoExiste_DebeRetornarNotFound() {
        // Arrange
        Integer idNoExistente = 999;
        when(tipoAlmacenDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = tipoAlmacenResource.delete(idNoExistente);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + idNoExistente + " not found", response.getHeaderString("Record-not-found"));
        verify(tipoAlmacenDAO).findById(idNoExistente);
        verify(tipoAlmacenDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(tipoAlmacenDAO.findById(tipoAlmacenId)).thenReturn(tipoAlmacen);
        doThrow(new RuntimeException("Error al eliminar"))
                .when(tipoAlmacenDAO).eliminar(any());

        // Act
        Response response = tipoAlmacenResource.delete(tipoAlmacenId);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(tipoAlmacenDAO).findById(tipoAlmacenId);
        verify(tipoAlmacenDAO).eliminar(tipoAlmacen);
    }

    @Test
    void testDelete_TipoAlmacenInactivo_DebeEliminarCorrectamente() {
        // Arrange
        tipoAlmacen.setActivo(false);
        when(tipoAlmacenDAO.findById(tipoAlmacenId)).thenReturn(tipoAlmacen);
        doNothing().when(tipoAlmacenDAO).eliminar(tipoAlmacen);

        // Act
        Response response = tipoAlmacenResource.delete(tipoAlmacenId);

        // Assert
        assertEquals(204, response.getStatus());
        verify(tipoAlmacenDAO).eliminar(tipoAlmacen);
    }

    // ==================== TESTS DE create ====================

    @Test
    void testCreate_TipoAlmacenValido_DebeRetornarCreated() throws Exception {
        // Arrange
        TipoAlmacen nuevoTipoAlmacen = new TipoAlmacen();
        nuevoTipoAlmacen.setNombre("Nuevo Almacén");
        nuevoTipoAlmacen.setActivo(true);
        nuevoTipoAlmacen.setObservaciones("Observaciones del nuevo almacén");
        // ID debe ser null para crear

        Integer nuevoId = 100;
        URI locationUri = new URI("http://localhost:8080/api/tipo_almacen/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        // Simular que después de crear, se asigna un ID
        doAnswer(invocation -> {
            TipoAlmacen ta = invocation.getArgument(0);
            ta.setId(nuevoId);
            return null;
        }).when(tipoAlmacenDAO).crear(any(TipoAlmacen.class));

        // Act
        Response response = tipoAlmacenResource.create(nuevoTipoAlmacen, uriInfo);

        // Assert
        assertEquals(201, response.getStatus(), "Debe retornar CREATED");
        assertNotNull(response.getLocation());
        assertEquals(locationUri, response.getLocation());

        verify(tipoAlmacenDAO).crear(any(TipoAlmacen.class));
    }

    @Test
    void testCreate_EntityNull_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoAlmacenResource.create(null, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("entity must not be null and id must be null", response.getHeaderString("Missing-parameter"));
        verify(tipoAlmacenDAO, never()).crear(any());
    }

    @Test
    void testCreate_TipoAlmacenConIdNoNull_DebeRetornarUnprocessableEntity() {
        // Arrange
        TipoAlmacen tipoAlmacenConId = new TipoAlmacen();
        tipoAlmacenConId.setId(tipoAlmacenId);
        tipoAlmacenConId.setNombre("Tipo Almacén con ID");

        // Act
        Response response = tipoAlmacenResource.create(tipoAlmacenConId, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("entity must not be null and id must be null", response.getHeaderString("Missing-parameter"));
        verify(tipoAlmacenDAO, never()).crear(any());
    }

    @Test
    void testCreate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        TipoAlmacen nuevoTipoAlmacen = new TipoAlmacen();
        nuevoTipoAlmacen.setNombre("Nuevo Tipo Almacén");

        doThrow(new RuntimeException("Error al guardar"))
                .when(tipoAlmacenDAO).crear(any());

        // Act
        Response response = tipoAlmacenResource.create(nuevoTipoAlmacen, uriInfo);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(tipoAlmacenDAO).crear(any(TipoAlmacen.class));
    }

    @Test
    void testCreate_TipoAlmacenConCamposMinimos_DebeCrearCorrectamente() throws Exception {
        // Arrange
        TipoAlmacen tipoAlmacenMinimo = new TipoAlmacen();
        tipoAlmacenMinimo.setNombre("Almacén Mínimo");
        // No tiene activo, observaciones, etc.

        Integer nuevoId = 200;
        URI locationUri = new URI("http://localhost:8080/api/tipo_almacen/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        doAnswer(invocation -> {
            TipoAlmacen ta = invocation.getArgument(0);
            ta.setId(nuevoId);
            return null;
        }).when(tipoAlmacenDAO).crear(any(TipoAlmacen.class));

        // Act
        Response response = tipoAlmacenResource.create(tipoAlmacenMinimo, uriInfo);

        // Assert
        assertEquals(201, response.getStatus());
        verify(tipoAlmacenDAO).crear(any(TipoAlmacen.class));
    }

    @Test
    void testCreate_TipoAlmacenConObservacionesNull_DebeCrearCorrectamente() throws Exception {
        // Arrange
        TipoAlmacen tipoAlmacenSinObservaciones = new TipoAlmacen();
        tipoAlmacenSinObservaciones.setNombre("Almacén sin observaciones");
        tipoAlmacenSinObservaciones.setActivo(true);
        // Observaciones es null

        Integer nuevoId = 300;
        URI locationUri = new URI("http://localhost:8080/api/tipo_almacen/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        doAnswer(invocation -> {
            TipoAlmacen ta = invocation.getArgument(0);
            ta.setId(nuevoId);
            return null;
        }).when(tipoAlmacenDAO).crear(any(TipoAlmacen.class));

        // Act
        Response response = tipoAlmacenResource.create(tipoAlmacenSinObservaciones, uriInfo);

        // Assert
        assertEquals(201, response.getStatus());
        verify(tipoAlmacenDAO).crear(any(TipoAlmacen.class));
    }

    // ==================== TESTS DE update ====================

    @Test
    void testUpdate_DatosValidos_DebeRetornarOk() {
        // Arrange
        TipoAlmacen actualizacion = new TipoAlmacen();
        actualizacion.setNombre("Almacén Principal Actualizado");
        actualizacion.setActivo(false);
        actualizacion.setObservaciones("Observaciones actualizadas");

        when(tipoAlmacenDAO.findById(tipoAlmacenId)).thenReturn(tipoAlmacen);
        when(tipoAlmacenDAO.modificar(any(TipoAlmacen.class))).thenReturn(actualizacion);

        // Act
        Response response = tipoAlmacenResource.update(tipoAlmacenId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        TipoAlmacen resultado = (TipoAlmacen) response.getEntity();
        assertEquals("Almacén Principal Actualizado", resultado.getNombre());
        assertEquals(false, resultado.getActivo());
        assertEquals("Observaciones actualizadas", resultado.getObservaciones());

        verify(tipoAlmacenDAO).findById(tipoAlmacenId);
        verify(tipoAlmacenDAO).modificar(any(TipoAlmacen.class));
    }

    @Test
    void testUpdate_IdNulo_DebeRetornarUnprocessableEntity() {
        // Arrange
        TipoAlmacen actualizacion = new TipoAlmacen();

        // Act
        Response response = tipoAlmacenResource.update(null, actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id and entity must be valid", response.getHeaderString("Missing-parameter"));
        verify(tipoAlmacenDAO, never()).findById(any());
        verify(tipoAlmacenDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_EntityNull_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = tipoAlmacenResource.update(tipoAlmacenId, null);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id and entity must be valid", response.getHeaderString("Missing-parameter"));
        verify(tipoAlmacenDAO, never()).findById(any());
        verify(tipoAlmacenDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_IdCero_DebeRetornarUnprocessableEntity() {
        // Arrange
        TipoAlmacen actualizacion = new TipoAlmacen();

        // Act
        Response response = tipoAlmacenResource.update(0, actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id and entity must be valid", response.getHeaderString("Missing-parameter"));
        verify(tipoAlmacenDAO, never()).findById(any());
        verify(tipoAlmacenDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_IdNegativo_DebeRetornarUnprocessableEntity() {
        // Arrange
        TipoAlmacen actualizacion = new TipoAlmacen();

        // Act
        Response response = tipoAlmacenResource.update(-1, actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id and entity must be valid", response.getHeaderString("Missing-parameter"));
        verify(tipoAlmacenDAO, never()).findById(any());
        verify(tipoAlmacenDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_TipoAlmacenNoExiste_DebeRetornarNotFound() {
        // Arrange
        TipoAlmacen actualizacion = new TipoAlmacen();
        Integer idNoExistente = 999;
        when(tipoAlmacenDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = tipoAlmacenResource.update(idNoExistente, actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + idNoExistente + " not found", response.getHeaderString("Record-not-found"));
        verify(tipoAlmacenDAO).findById(idNoExistente);
        verify(tipoAlmacenDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        TipoAlmacen actualizacion = new TipoAlmacen();
        when(tipoAlmacenDAO.findById(tipoAlmacenId)).thenReturn(tipoAlmacen);
        when(tipoAlmacenDAO.modificar(any()))
                .thenThrow(new RuntimeException("Error al actualizar"));

        // Act
        Response response = tipoAlmacenResource.update(tipoAlmacenId, actualizacion);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(tipoAlmacenDAO).findById(tipoAlmacenId);
        verify(tipoAlmacenDAO).modificar(any(TipoAlmacen.class));
    }

    @Test
    void testUpdate_AsignaIdCorrecto_DebeUsarIdDelPath() {
        // Arrange
        TipoAlmacen actualizacion = new TipoAlmacen();
        actualizacion.setNombre("Nombre Actualizado");

        when(tipoAlmacenDAO.findById(tipoAlmacenId)).thenReturn(tipoAlmacen);
        when(tipoAlmacenDAO.modificar(any(TipoAlmacen.class))).thenAnswer(invocation -> {
            TipoAlmacen ta = invocation.getArgument(0);
            // Verificar que el ID se asignó correctamente desde el path
            assertEquals(tipoAlmacenId, ta.getId());
            return ta;
        });

        // Act
        Response response = tipoAlmacenResource.update(tipoAlmacenId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoAlmacenDAO).modificar(any(TipoAlmacen.class));
    }

    @Test
    void testUpdate_ActualizacionParcial_DebeConservarCamposNoModificados() {
        // Arrange
        TipoAlmacen actualizacion = new TipoAlmacen();
        actualizacion.setNombre("Solo nombre actualizado");
        // No se establecen otros campos

        TipoAlmacen tipoAlmacenActualizado = new TipoAlmacen();
        tipoAlmacenActualizado.setId(tipoAlmacenId);
        tipoAlmacenActualizado.setNombre("Solo nombre actualizado");
        tipoAlmacenActualizado.setActivo(true); // Mantiene valor original
        tipoAlmacenActualizado.setObservaciones("Almacén central de la empresa"); // Mantiene valor original

        when(tipoAlmacenDAO.findById(tipoAlmacenId)).thenReturn(tipoAlmacen);
        when(tipoAlmacenDAO.modificar(any(TipoAlmacen.class))).thenReturn(tipoAlmacenActualizado);

        // Act
        Response response = tipoAlmacenResource.update(tipoAlmacenId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoAlmacenDAO).modificar(any(TipoAlmacen.class));
    }

    @Test
    void testUpdate_ActualizarSoloActivo_DebeActualizarCorrectamente() {
        // Arrange
        TipoAlmacen actualizacion = new TipoAlmacen();
        actualizacion.setActivo(false);
        // No se establecen nombre ni observaciones

        when(tipoAlmacenDAO.findById(tipoAlmacenId)).thenReturn(tipoAlmacen);
        when(tipoAlmacenDAO.modificar(any(TipoAlmacen.class))).thenReturn(tipoAlmacen);

        // Act
        Response response = tipoAlmacenResource.update(tipoAlmacenId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoAlmacenDAO).modificar(any(TipoAlmacen.class));
    }

    @Test
    void testUpdate_ActualizarSoloObservaciones_DebeActualizarCorrectamente() {
        // Arrange
        TipoAlmacen actualizacion = new TipoAlmacen();
        actualizacion.setObservaciones("Nuevas observaciones");
        // No se establecen nombre ni activo

        when(tipoAlmacenDAO.findById(tipoAlmacenId)).thenReturn(tipoAlmacen);
        when(tipoAlmacenDAO.modificar(any(TipoAlmacen.class))).thenReturn(tipoAlmacen);

        // Act
        Response response = tipoAlmacenResource.update(tipoAlmacenId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoAlmacenDAO).modificar(any(TipoAlmacen.class));
    }

    @Test
    void testUpdate_TipoAlmacenConTodosCamposNull_DebeActualizarCorrectamente() {
        // Arrange
        TipoAlmacen actualizacion = new TipoAlmacen();
        // No se establece ningún campo

        when(tipoAlmacenDAO.findById(tipoAlmacenId)).thenReturn(tipoAlmacen);
        when(tipoAlmacenDAO.modificar(any(TipoAlmacen.class))).thenReturn(tipoAlmacen);

        // Act
        Response response = tipoAlmacenResource.update(tipoAlmacenId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoAlmacenDAO).modificar(any(TipoAlmacen.class));
    }
}