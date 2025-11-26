package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoUnidadMedidaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.UnidadMedidaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoUnidadMedida;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.UnidadMedida;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests para UnidadMedidaResource
 * Verifica el comportamiento de todos los endpoints REST para unidades de medida
 */
class UnidadMedidaResourceTest {

    @Mock
    private UnidadMedidaDAO unidadMedidaDAO;

    @Mock
    private TipoUnidadMedidaDAO tipoUnidadMedidaDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    private UnidadMedidaResource unidadMedidaResource;
    private UnidadMedida unidadMedida;
    private TipoUnidadMedida tipoUnidadMedida;
    private Integer unidadMedidaId;
    private Integer tipoUnidadMedidaId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        unidadMedidaResource = new UnidadMedidaResource();

        // Inyección manual de los DAOs
        unidadMedidaResource.unidadMedidaDAO = unidadMedidaDAO;
        unidadMedidaResource.tipoUnidadMedidaDAO = tipoUnidadMedidaDAO;

        // Datos de prueba
        unidadMedidaId = 1;
        tipoUnidadMedidaId = 1;

        // Configurar TipoUnidadMedida
        tipoUnidadMedida = new TipoUnidadMedida();
        tipoUnidadMedida.setId(tipoUnidadMedidaId);
        tipoUnidadMedida.setNombre("Longitud");
        tipoUnidadMedida.setActivo(true);

        // Configurar UnidadMedida
        unidadMedida = new UnidadMedida();
        unidadMedida.setId(unidadMedidaId);
        unidadMedida.setIdTipoUnidadMedida(tipoUnidadMedida);
        unidadMedida.setEquivalencia(new BigDecimal("1.00"));
        unidadMedida.setExpresionRegular("^[0-9]+(\\.[0-9]{1,2})?$");
        unidadMedida.setActivo(true);
        unidadMedida.setComentarios("Unidad base de longitud");
    }

    // ==================== TESTS DE findRange ====================

    @Test
    void testFindRange_ParametrosValidos_DebeRetornarOkConHeader() {
        // Arrange
        List<UnidadMedida> unidadesMedida = new ArrayList<>();
        unidadesMedida.add(unidadMedida);

        UnidadMedida unidadMedida2 = new UnidadMedida();
        unidadMedida2.setId(2);
        unidadMedida2.setEquivalencia(new BigDecimal("100.00"));
        unidadesMedida.add(unidadMedida2);

        when(unidadMedidaDAO.findRange(0, 50)).thenReturn(unidadesMedida);
        when(unidadMedidaDAO.count()).thenReturn(100L);

        // Act
        Response response = unidadMedidaResource.findRange(0, 50);

        // Assert
        assertEquals(200, response.getStatus(), "Debe retornar OK");
        assertNotNull(response.getEntity());

        assertEquals("100", response.getHeaderString("Total-records"));

        @SuppressWarnings("unchecked")
        List<UnidadMedida> resultado = (List<UnidadMedida>) response.getEntity();
        assertEquals(2, resultado.size());

        verify(unidadMedidaDAO).findRange(0, 50);
        verify(unidadMedidaDAO).count();
    }

    @Test
    void testFindRange_FirstNegativo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = unidadMedidaResource.findRange(-1, 50);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first or max out of range", response.getHeaderString("Missing-parameter"));
        verify(unidadMedidaDAO, never()).findRange(anyInt(), anyInt());
        verify(unidadMedidaDAO, never()).count();
    }

    @Test
    void testFindRange_MaxMayor100_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = unidadMedidaResource.findRange(0, 101);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first or max out of range", response.getHeaderString("Missing-parameter"));
        verify(unidadMedidaDAO, never()).findRange(anyInt(), anyInt());
        verify(unidadMedidaDAO, never()).count();
    }

    @Test
    void testFindRange_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(unidadMedidaDAO.findRange(0, 50))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        Response response = unidadMedidaResource.findRange(0, 50);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(unidadMedidaDAO).findRange(0, 50);
    }

    @Test
    void testFindRange_ListaVacia_DebeRetornarOkConListaVacia() {
        // Arrange
        List<UnidadMedida> unidadesMedidaVacios = new ArrayList<>();
        when(unidadMedidaDAO.findRange(0, 50)).thenReturn(unidadesMedidaVacios);
        when(unidadMedidaDAO.count()).thenReturn(0L);

        // Act
        Response response = unidadMedidaResource.findRange(0, 50);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<UnidadMedida> resultado = (List<UnidadMedida>) response.getEntity();
        assertTrue(resultado.isEmpty());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    // ==================== TESTS DE findById ====================

    @Test
    void testFindById_IdValidoUnidadMedidaExiste_DebeRetornarOk() {
        // Arrange
        when(unidadMedidaDAO.findById(unidadMedidaId)).thenReturn(unidadMedida);

        // Act
        Response response = unidadMedidaResource.findById(unidadMedidaId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        UnidadMedida resultado = (UnidadMedida) response.getEntity();
        assertEquals(unidadMedidaId, resultado.getId());
        assertEquals(new BigDecimal("1.00"), resultado.getEquivalencia());
        assertEquals("^[0-9]+(\\.[0-9]{1,2})?$", resultado.getExpresionRegular());
        assertEquals(true, resultado.getActivo());

        verify(unidadMedidaDAO).findById(unidadMedidaId);
    }

    @Test
    void testFindById_IdNulo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = unidadMedidaResource.findById(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(unidadMedidaDAO, never()).findById(any());
    }

    @Test
    void testFindById_IdCero_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = unidadMedidaResource.findById(0);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(unidadMedidaDAO, never()).findById(any());
    }

    @Test
    void testFindById_IdNegativo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = unidadMedidaResource.findById(-1);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(unidadMedidaDAO, never()).findById(any());
    }

    @Test
    void testFindById_UnidadMedidaNoExiste_DebeRetornarNotFound() {
        // Arrange
        Integer idNoExistente = 999;
        when(unidadMedidaDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = unidadMedidaResource.findById(idNoExistente);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + idNoExistente + " not found", response.getHeaderString("Record-not-found"));
        verify(unidadMedidaDAO).findById(idNoExistente);
    }

    @Test
    void testFindById_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(unidadMedidaDAO.findById(unidadMedidaId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = unidadMedidaResource.findById(unidadMedidaId);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(unidadMedidaDAO).findById(unidadMedidaId);
    }

    // ==================== TESTS DE delete ====================

    @Test
    void testDelete_IdValidoUnidadMedidaExiste_DebeRetornarNoContent() {
        // Arrange
        when(unidadMedidaDAO.findById(unidadMedidaId)).thenReturn(unidadMedida);
        doNothing().when(unidadMedidaDAO).eliminar(unidadMedida);

        // Act
        Response response = unidadMedidaResource.delete(unidadMedidaId);

        // Assert
        assertEquals(204, response.getStatus(), "Debe retornar NO_CONTENT");
        verify(unidadMedidaDAO).findById(unidadMedidaId);
        verify(unidadMedidaDAO).eliminar(unidadMedida);
    }

    @Test
    void testDelete_IdNulo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = unidadMedidaResource.delete(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(unidadMedidaDAO, never()).findById(any());
        verify(unidadMedidaDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_UnidadMedidaNoExiste_DebeRetornarNotFound() {
        // Arrange
        Integer idNoExistente = 999;
        when(unidadMedidaDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = unidadMedidaResource.delete(idNoExistente);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + idNoExistente + " not found", response.getHeaderString("Record-not-found"));
        verify(unidadMedidaDAO).findById(idNoExistente);
        verify(unidadMedidaDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(unidadMedidaDAO.findById(unidadMedidaId)).thenReturn(unidadMedida);
        doThrow(new RuntimeException("Error al eliminar"))
                .when(unidadMedidaDAO).eliminar(any());

        // Act
        Response response = unidadMedidaResource.delete(unidadMedidaId);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(unidadMedidaDAO).findById(unidadMedidaId);
        verify(unidadMedidaDAO).eliminar(unidadMedida);
    }

    // ==================== TESTS DE create ====================

    @Test
    void testCreate_UnidadMedidaValido_DebeRetornarCreated() throws Exception {
        // Arrange
        UnidadMedida nuevaUnidadMedida = new UnidadMedida();
        nuevaUnidadMedida.setEquivalencia(new BigDecimal("0.01"));
        nuevaUnidadMedida.setExpresionRegular("^cm$");
        nuevaUnidadMedida.setActivo(true);
        nuevaUnidadMedida.setComentarios("Centímetro");
        // ID debe ser null para crear
        // Sin TipoUnidadMedida

        Integer nuevoId = 100;
        URI locationUri = new URI("http://localhost:8080/api/unidad_medida/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        // Simular que después de crear, se asigna un ID
        doAnswer(invocation -> {
            UnidadMedida um = invocation.getArgument(0);
            um.setId(nuevoId);
            return null;
        }).when(unidadMedidaDAO).crear(any(UnidadMedida.class));

        // Act
        Response response = unidadMedidaResource.create(nuevaUnidadMedida, uriInfo);

        // Assert
        assertEquals(201, response.getStatus(), "Debe retornar CREATED");
        assertNotNull(response.getLocation());
        assertEquals(locationUri, response.getLocation());

        verify(unidadMedidaDAO).crear(any(UnidadMedida.class));
        verify(tipoUnidadMedidaDAO, never()).findById(any()); // No se busca TipoUnidadMedida
    }

    @Test
    void testCreate_UnidadMedidaConTipoUnidadMedidaValido_DebeRetornarCreated() throws Exception {
        // Arrange
        UnidadMedida nuevaUnidadMedida = new UnidadMedida();
        nuevaUnidadMedida.setEquivalencia(new BigDecimal("1000.00"));
        nuevaUnidadMedida.setIdTipoUnidadMedida(tipoUnidadMedida);

        Integer nuevoId = 101;
        URI locationUri = new URI("http://localhost:8080/api/unidad_medida/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);
        when(tipoUnidadMedidaDAO.findById(tipoUnidadMedidaId)).thenReturn(tipoUnidadMedida);

        doAnswer(invocation -> {
            UnidadMedida um = invocation.getArgument(0);
            um.setId(nuevoId);
            return null;
        }).when(unidadMedidaDAO).crear(any(UnidadMedida.class));

        // Act
        Response response = unidadMedidaResource.create(nuevaUnidadMedida, uriInfo);

        // Assert
        assertEquals(201, response.getStatus());
        assertNotNull(response.getLocation());
        assertEquals(locationUri, response.getLocation());

        verify(tipoUnidadMedidaDAO).findById(tipoUnidadMedidaId); // Se busca el TipoUnidadMedida
        verify(unidadMedidaDAO).crear(any(UnidadMedida.class));
    }

    @Test
    void testCreate_UnidadMedidaConTipoUnidadMedidaNoExiste_DebeRetornarUnprocessableEntity() {
        // Arrange
        TipoUnidadMedida tipoNoExistente = new TipoUnidadMedida();
        tipoNoExistente.setId(999); // ID que no existe

        UnidadMedida nuevaUnidadMedida = new UnidadMedida();
        nuevaUnidadMedida.setEquivalencia(new BigDecimal("1.00"));
        nuevaUnidadMedida.setIdTipoUnidadMedida(tipoNoExistente);

        when(tipoUnidadMedidaDAO.findById(999)).thenReturn(null);

        // Act
        Response response = unidadMedidaResource.create(nuevaUnidadMedida, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("TipoUnidadMedida does not exist in database", response.getHeaderString("Missing-parameter"));
        verify(tipoUnidadMedidaDAO).findById(999);
        verify(unidadMedidaDAO, never()).crear(any());
    }

    @Test
    void testCreate_UnidadMedidaConTipoUnidadMedidaSinId_DebeCrearCorrectamente() throws Exception {
        // Arrange
        TipoUnidadMedida tipoSinId = new TipoUnidadMedida();
        // No establecer ID

        UnidadMedida nuevaUnidadMedida = new UnidadMedida();
        nuevaUnidadMedida.setEquivalencia(new BigDecimal("1.00"));
        nuevaUnidadMedida.setIdTipoUnidadMedida(tipoSinId);

        Integer nuevoId = 102;
        URI locationUri = new URI("http://localhost:8080/api/unidad_medida/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        doAnswer(invocation -> {
            UnidadMedida um = invocation.getArgument(0);
            um.setId(nuevoId);
            return null;
        }).when(unidadMedidaDAO).crear(any(UnidadMedida.class));

        // Act
        Response response = unidadMedidaResource.create(nuevaUnidadMedida, uriInfo);

        // Assert
        assertEquals(201, response.getStatus());
        verify(tipoUnidadMedidaDAO, never()).findById(any()); // No se busca porque no tiene ID
        verify(unidadMedidaDAO).crear(any(UnidadMedida.class));
    }

    @Test
    void testCreate_EntityNull_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = unidadMedidaResource.create(null, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("entity must not be null and id must be null", response.getHeaderString("Missing-parameter"));
        verify(unidadMedidaDAO, never()).crear(any());
    }

    @Test
    void testCreate_UnidadMedidaConIdNoNull_DebeRetornarUnprocessableEntity() {
        // Arrange
        UnidadMedida unidadMedidaConId = new UnidadMedida();
        unidadMedidaConId.setId(unidadMedidaId); // ID no null
        unidadMedidaConId.setEquivalencia(new BigDecimal("1.00"));

        // Act
        Response response = unidadMedidaResource.create(unidadMedidaConId, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("entity must not be null and id must be null", response.getHeaderString("Missing-parameter"));
        verify(unidadMedidaDAO, never()).crear(any());
    }

    @Test
    void testCreate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        UnidadMedida nuevaUnidadMedida = new UnidadMedida();
        nuevaUnidadMedida.setEquivalencia(new BigDecimal("1.00"));

        doThrow(new RuntimeException("Error al guardar"))
                .when(unidadMedidaDAO).crear(any());

        // Act
        Response response = unidadMedidaResource.create(nuevaUnidadMedida, uriInfo);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(unidadMedidaDAO).crear(any(UnidadMedida.class));
    }

    @Test
    void testCreate_ConIllegalArgumentException_DebeRetornarUnprocessableEntity() {
        // Arrange
        UnidadMedida nuevaUnidadMedida = new UnidadMedida();
        nuevaUnidadMedida.setEquivalencia(new BigDecimal("1.00"));

        doThrow(new IllegalArgumentException("Datos inválidos"))
                .when(unidadMedidaDAO).crear(any());

        // Act
        Response response = unidadMedidaResource.create(nuevaUnidadMedida, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("Datos inválidos", response.getHeaderString("Validation-error"));
        verify(unidadMedidaDAO).crear(any(UnidadMedida.class));
    }

    // ==================== TESTS DE update ====================

    @Test
    void testUpdate_DatosValidos_DebeRetornarOk() {
        // Arrange
        UnidadMedida actualizacion = new UnidadMedida();
        actualizacion.setEquivalencia(new BigDecimal("0.10"));
        actualizacion.setExpresionRegular("^dm$");
        actualizacion.setActivo(false);
        actualizacion.setComentarios("Decímetro actualizado");

        when(unidadMedidaDAO.findById(unidadMedidaId)).thenReturn(unidadMedida);
        when(unidadMedidaDAO.modificar(any(UnidadMedida.class))).thenReturn(actualizacion);

        // Act
        Response response = unidadMedidaResource.update(unidadMedidaId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        UnidadMedida resultado = (UnidadMedida) response.getEntity();
        assertEquals(new BigDecimal("0.10"), resultado.getEquivalencia());
        assertEquals("^dm$", resultado.getExpresionRegular());
        assertEquals(false, resultado.getActivo());
        assertEquals("Decímetro actualizado", resultado.getComentarios());

        verify(unidadMedidaDAO).findById(unidadMedidaId);
        verify(unidadMedidaDAO).modificar(any(UnidadMedida.class));
        verify(tipoUnidadMedidaDAO, never()).findById(any()); // No se actualiza TipoUnidadMedida
    }

    @Test
    void testUpdate_UnidadMedidaConNuevoTipoUnidadMedida_DebeActualizarCorrectamente() {
        // Arrange
        TipoUnidadMedida nuevoTipo = new TipoUnidadMedida();
        nuevoTipo.setId(2);
        nuevoTipo.setNombre("Peso");

        UnidadMedida actualizacion = new UnidadMedida();
        actualizacion.setIdTipoUnidadMedida(nuevoTipo);
        actualizacion.setEquivalencia(new BigDecimal("1.00"));

        when(unidadMedidaDAO.findById(unidadMedidaId)).thenReturn(unidadMedida);
        when(tipoUnidadMedidaDAO.findById(2)).thenReturn(nuevoTipo);
        when(unidadMedidaDAO.modificar(any(UnidadMedida.class))).thenReturn(actualizacion);

        // Act
        Response response = unidadMedidaResource.update(unidadMedidaId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoUnidadMedidaDAO).findById(2); // Se busca el nuevo TipoUnidadMedida
        verify(unidadMedidaDAO).modificar(any(UnidadMedida.class));
    }

    @Test
    void testUpdate_UnidadMedidaConTipoUnidadMedidaNoExiste_DebeRetornarUnprocessableEntity() {
        // Arrange
        TipoUnidadMedida tipoNoExistente = new TipoUnidadMedida();
        tipoNoExistente.setId(999); // ID que no existe

        UnidadMedida actualizacion = new UnidadMedida();
        actualizacion.setIdTipoUnidadMedida(tipoNoExistente);
        actualizacion.setEquivalencia(new BigDecimal("1.00"));

        when(unidadMedidaDAO.findById(unidadMedidaId)).thenReturn(unidadMedida);
        when(tipoUnidadMedidaDAO.findById(999)).thenReturn(null);

        // Act
        Response response = unidadMedidaResource.update(unidadMedidaId, actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("TipoUnidadMedida does not exist in database", response.getHeaderString("Missing-parameter"));
        verify(tipoUnidadMedidaDAO).findById(999);
        verify(unidadMedidaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_UnidadMedidaConTipoUnidadMedidaSinId_DebeActualizarCorrectamente() {
        // Arrange
        TipoUnidadMedida tipoSinId = new TipoUnidadMedida();
        // No establecer ID

        UnidadMedida actualizacion = new UnidadMedida();
        actualizacion.setIdTipoUnidadMedida(tipoSinId);
        actualizacion.setEquivalencia(new BigDecimal("1.00"));

        when(unidadMedidaDAO.findById(unidadMedidaId)).thenReturn(unidadMedida);
        when(unidadMedidaDAO.modificar(any(UnidadMedida.class))).thenReturn(actualizacion);

        // Act
        Response response = unidadMedidaResource.update(unidadMedidaId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoUnidadMedidaDAO, never()).findById(any()); // No se busca porque no tiene ID
        verify(unidadMedidaDAO).modificar(any(UnidadMedida.class));
    }

    @Test
    void testUpdate_IdNulo_DebeRetornarUnprocessableEntity() {
        // Arrange
        UnidadMedida actualizacion = new UnidadMedida();

        // Act
        Response response = unidadMedidaResource.update(null, actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id and entity must be valid", response.getHeaderString("Missing-parameter"));
        verify(unidadMedidaDAO, never()).findById(any());
        verify(unidadMedidaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_EntityNull_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = unidadMedidaResource.update(unidadMedidaId, null);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id and entity must be valid", response.getHeaderString("Missing-parameter"));
        verify(unidadMedidaDAO, never()).findById(any());
        verify(unidadMedidaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_UnidadMedidaNoExiste_DebeRetornarNotFound() {
        // Arrange
        UnidadMedida actualizacion = new UnidadMedida();
        Integer idNoExistente = 999;
        when(unidadMedidaDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = unidadMedidaResource.update(idNoExistente, actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + idNoExistente + " not found", response.getHeaderString("Record-not-found"));
        verify(unidadMedidaDAO).findById(idNoExistente);
        verify(unidadMedidaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        UnidadMedida actualizacion = new UnidadMedida();
        when(unidadMedidaDAO.findById(unidadMedidaId)).thenReturn(unidadMedida);
        when(unidadMedidaDAO.modificar(any()))
                .thenThrow(new RuntimeException("Error al actualizar"));

        // Act
        Response response = unidadMedidaResource.update(unidadMedidaId, actualizacion);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(unidadMedidaDAO).findById(unidadMedidaId);
        verify(unidadMedidaDAO).modificar(any(UnidadMedida.class));
    }

    @Test
    void testUpdate_AsignaIdCorrecto_DebeUsarIdDelPath() {
        // Arrange
        UnidadMedida actualizacion = new UnidadMedida();
        actualizacion.setEquivalencia(new BigDecimal("2.00"));

        when(unidadMedidaDAO.findById(unidadMedidaId)).thenReturn(unidadMedida);
        when(unidadMedidaDAO.modificar(any(UnidadMedida.class))).thenAnswer(invocation -> {
            UnidadMedida um = invocation.getArgument(0);
            // Verificar que el ID se asignó correctamente desde el path
            assertEquals(unidadMedidaId, um.getId());
            return um;
        });

        // Act
        Response response = unidadMedidaResource.update(unidadMedidaId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(unidadMedidaDAO).modificar(any(UnidadMedida.class));
    }

    @Test
    void testUpdate_ActualizacionParcial_DebeConservarCamposNoModificados() {
        // Arrange
        UnidadMedida actualizacion = new UnidadMedida();
        actualizacion.setEquivalencia(new BigDecimal("0.50"));
        // No se establecen otros campos

        UnidadMedida unidadMedidaActualizada = new UnidadMedida();
        unidadMedidaActualizada.setId(unidadMedidaId);
        unidadMedidaActualizada.setEquivalencia(new BigDecimal("0.50"));
        unidadMedidaActualizada.setIdTipoUnidadMedida(tipoUnidadMedida); // Mantiene valor original
        unidadMedidaActualizada.setExpresionRegular("^[0-9]+(\\.[0-9]{1,2})?$"); // Mantiene valor original
        unidadMedidaActualizada.setActivo(true); // Mantiene valor original
        unidadMedidaActualizada.setComentarios("Unidad base de longitud"); // Mantiene valor original

        when(unidadMedidaDAO.findById(unidadMedidaId)).thenReturn(unidadMedida);
        when(unidadMedidaDAO.modificar(any(UnidadMedida.class))).thenReturn(unidadMedidaActualizada);

        // Act
        Response response = unidadMedidaResource.update(unidadMedidaId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(unidadMedidaDAO).modificar(any(UnidadMedida.class));
    }
}