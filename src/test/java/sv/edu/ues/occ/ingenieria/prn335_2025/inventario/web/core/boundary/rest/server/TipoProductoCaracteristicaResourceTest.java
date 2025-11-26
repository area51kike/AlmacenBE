package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProducto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProductoCaracteristica;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para TipoProductoCaracteristicaResource
 * Verifica el comportamiento de todos los endpoints REST para características de tipo_producto
 */
class TipoProductoCaracteristicaResourceTest {

    @Mock
    private TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDAO;

    @Mock
    private CaracteristicaDAO caracteristicaDAO;

    @Mock
    private TipoProductoDAO tipoProductoDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    private TipoProductoCaracteristicaResource resource;
    private TipoProducto tipoProducto;
    private Caracteristica caracteristica;
    private TipoProductoCaracteristica tpcRelacion;
    private Long tipoProductoId;
    private Long tpcId;
    private Integer caracteristicaId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resource = new TipoProductoCaracteristicaResource();

        // Inyección manual de los DAOs
        resource.tipoProductoCaracteristicaDAO = tipoProductoCaracteristicaDAO;
        resource.caracteristicaDAO = caracteristicaDAO;
        resource.tipoProductoDAO = tipoProductoDAO;

        // Datos de prueba
        tipoProductoId = 1L;
        tpcId = 1L;
        caracteristicaId = 1;

        // Configurar TipoProducto
        tipoProducto = new TipoProducto();
        tipoProducto.setId(tipoProductoId);
        tipoProducto.setNombre("Electrónicos");
        tipoProducto.setActivo(true);

        // Configurar Caracteristica
        caracteristica = new Caracteristica();
        caracteristica.setId(caracteristicaId);
        caracteristica.setNombre("Color");
        caracteristica.setActivo(true);

        // Configurar TipoProductoCaracteristica
        tpcRelacion = new TipoProductoCaracteristica();
        tpcRelacion.setId(tpcId);
        tpcRelacion.setTipoProducto(tipoProducto);
        tpcRelacion.setCaracteristica(caracteristica);
        tpcRelacion.setObligatorio(true);
        tpcRelacion.setFechaCreacion(OffsetDateTime.now());
    }

    // ==================== TESTS DE getCaracteristicas ====================

    @Test
    void testGetCaracteristicas_IdTipoProductoNull_DebeRetornarBadRequest() {
        // Act
        Response response = resource.getCaracteristicas(null);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idTipoProducto must be a positive number", response.getHeaderString("Error"));
        verify(tipoProductoDAO, never()).findById(any());
    }

    @Test
    void testGetCaracteristicas_IdTipoProductoCero_DebeRetornarBadRequest() {
        // Act
        Response response = resource.getCaracteristicas(0L);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idTipoProducto must be a positive number", response.getHeaderString("Error"));
        verify(tipoProductoDAO, never()).findById(any());
    }

    @Test
    void testGetCaracteristicas_IdTipoProductoNegativo_DebeRetornarBadRequest() {
        // Act
        Response response = resource.getCaracteristicas(-1L);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idTipoProducto must be a positive number", response.getHeaderString("Error"));
        verify(tipoProductoDAO, never()).findById(any());
    }

    @Test
    void testGetCaracteristicas_TipoProductoNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(tipoProductoDAO.findById(tipoProductoId)).thenReturn(null);

        // Act
        Response response = resource.getCaracteristicas(tipoProductoId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("TipoProducto with id " + tipoProductoId + " not found", response.getHeaderString("Not-Found"));
        verify(tipoProductoDAO).findById(tipoProductoId);
        verify(tipoProductoCaracteristicaDAO, never()).findByIdTipoProducto(any());
    }

    @Test
    void testGetCaracteristicas_TipoProductoConRelaciones_DebeRetornarOk() {
        // Arrange
        List<TipoProductoCaracteristica> relaciones = new ArrayList<>();
        relaciones.add(tpcRelacion);

        // Agregar segunda relación
        TipoProductoCaracteristica tpcRelacion2 = new TipoProductoCaracteristica();
        Caracteristica caracteristica2 = new Caracteristica();
        caracteristica2.setId(2);
        caracteristica2.setNombre("Tamaño");
        tpcRelacion2.setCaracteristica(caracteristica2);
        relaciones.add(tpcRelacion2);

        when(tipoProductoDAO.findById(tipoProductoId)).thenReturn(tipoProducto);
        when(tipoProductoCaracteristicaDAO.findByIdTipoProducto(tipoProductoId)).thenReturn(relaciones);

        // Act
        Response response = resource.getCaracteristicas(tipoProductoId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Caracteristica> resultado = (List<Caracteristica>) response.getEntity();
        assertEquals(2, resultado.size());
        assertEquals("Color", resultado.get(0).getNombre());
        assertEquals("Tamaño", resultado.get(1).getNombre());

        verify(tipoProductoDAO).findById(tipoProductoId);
        verify(tipoProductoCaracteristicaDAO).findByIdTipoProducto(tipoProductoId);
    }

    @Test
    void testGetCaracteristicas_TipoProductoSinRelaciones_DebeRetornarOkConListaVacia() {
        // Arrange
        List<TipoProductoCaracteristica> relacionesVacias = new ArrayList<>();
        when(tipoProductoDAO.findById(tipoProductoId)).thenReturn(tipoProducto);
        when(tipoProductoCaracteristicaDAO.findByIdTipoProducto(tipoProductoId)).thenReturn(relacionesVacias);

        // Act
        Response response = resource.getCaracteristicas(tipoProductoId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Caracteristica> resultado = (List<Caracteristica>) response.getEntity();
        assertTrue(resultado.isEmpty());
    }

    @Test
    void testGetCaracteristicas_RelacionesNull_DebeRetornarOkConListaVacia() {
        // Arrange
        when(tipoProductoDAO.findById(tipoProductoId)).thenReturn(tipoProducto);
        when(tipoProductoCaracteristicaDAO.findByIdTipoProducto(tipoProductoId)).thenReturn(null);

        // Act
        Response response = resource.getCaracteristicas(tipoProductoId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Caracteristica> resultado = (List<Caracteristica>) response.getEntity();
        assertTrue(resultado.isEmpty());
    }

    @Test
    void testGetCaracteristicas_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(tipoProductoDAO.findById(tipoProductoId))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        Response response = resource.getCaracteristicas(tipoProductoId);

        // Assert
        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-Exception"));
        verify(tipoProductoDAO).findById(tipoProductoId);
    }

    // ==================== TESTS DE findById ====================

    @Test
    void testFindById_IdTPCNull_DebeRetornarBadRequest() {
        // Act
        Response response = resource.findById(tipoProductoId, null);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idTPC must be a positive number", response.getHeaderString("Error"));
        verify(tipoProductoCaracteristicaDAO, never()).findById(any());
    }

    @Test
    void testFindById_IdTPCCero_DebeRetornarBadRequest() {
        // Act
        Response response = resource.findById(tipoProductoId, 0L);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idTPC must be a positive number", response.getHeaderString("Error"));
        verify(tipoProductoCaracteristicaDAO, never()).findById(any());
    }

    @Test
    void testFindById_IdTPCNegativo_DebeRetornarBadRequest() {
        // Act
        Response response = resource.findById(tipoProductoId, -1L);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idTPC must be a positive number", response.getHeaderString("Error"));
        verify(tipoProductoCaracteristicaDAO, never()).findById(any());
    }

    @Test
    void testFindById_RelacionExisteYCoincideTipoProducto_DebeRetornarOk() {
        // Arrange
        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);

        // Act
        Response response = resource.findById(tipoProductoId, tpcId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        TipoProductoCaracteristica resultado = (TipoProductoCaracteristica) response.getEntity();
        assertEquals(tpcId, resultado.getId());
        assertEquals(tipoProductoId, resultado.getTipoProducto().getId());

        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
    }

    @Test
    void testFindById_RelacionNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(null);

        // Act
        Response response = resource.findById(tipoProductoId, tpcId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + tpcId + " not found", response.getHeaderString("Not-Found"));
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
    }

    @Test
    void testFindById_RelacionSinTipoProducto_DebeRetornarInternalServerError() {
        // Arrange
        tpcRelacion.setTipoProducto(null);
        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);

        // Act
        Response response = resource.findById(tipoProductoId, tpcId);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getHeaderString("Server-Exception").contains("Cannot access db"));
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
    }

    @Test
    void testFindById_RelacionPerteneceOtroTipoProducto_DebeRetornarNotFound() {
        // Arrange
        TipoProducto otroTipoProducto = new TipoProducto();
        otroTipoProducto.setId(999L);
        tpcRelacion.setTipoProducto(otroTipoProducto);

        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);

        // Act
        Response response = resource.findById(tipoProductoId, tpcId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Caracteristica does not belong to this tipo_producto", response.getHeaderString("Not-Found"));
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
    }

    @Test
    void testFindById_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(tipoProductoCaracteristicaDAO.findById(tpcId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = resource.findById(tipoProductoId, tpcId);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getHeaderString("Server-Exception").contains("Cannot access db"));
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
    }

    // ==================== TESTS DE create ====================

    @Test
    void testCreate_IdTipoProductoNull_DebeRetornarBadRequest() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();

        // Act
        Response response = resource.create(null, entity, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idTipoProducto must be a positive number", response.getHeaderString("Error"));
        verify(tipoProductoDAO, never()).findById(any());
    }

    @Test
    void testCreate_IdTipoProductoCero_DebeRetornarBadRequest() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();

        // Act
        Response response = resource.create(0L, entity, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idTipoProducto must be a positive number", response.getHeaderString("Error"));
        verify(tipoProductoDAO, never()).findById(any());
    }

    @Test
    void testCreate_EntityNull_DebeRetornarBadRequest() {
        // Act
        Response response = resource.create(tipoProductoId, null, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("caracteristica is required", response.getHeaderString("Error"));
        verify(tipoProductoDAO, never()).findById(any());
    }

    @Test
    void testCreate_EntitySinCaracteristica_DebeRetornarBadRequest() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        // No se establece caracteristica

        // Act
        Response response = resource.create(tipoProductoId, entity, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("caracteristica is required", response.getHeaderString("Error"));
        verify(tipoProductoDAO, never()).findById(any());
    }

    @Test
    void testCreate_ConIdNoNull_DebeRetornarBadRequest() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        entity.setId(tpcId); // ID no null
        entity.setCaracteristica(caracteristica);

        // Act
        Response response = resource.create(tipoProductoId, entity, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("id must be null for new records", response.getHeaderString("Error"));
        verify(tipoProductoDAO, never()).findById(any());
    }

    @Test
    void testCreate_TipoProductoNoExiste_DebeRetornarNotFound() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        entity.setCaracteristica(caracteristica);

        when(tipoProductoDAO.findById(tipoProductoId)).thenReturn(null);

        // Act
        Response response = resource.create(tipoProductoId, entity, uriInfo);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("TipoProducto with id " + tipoProductoId + " not found", response.getHeaderString("Not-Found"));
        verify(tipoProductoDAO).findById(tipoProductoId);
        verify(caracteristicaDAO, never()).findById(any());
        verify(tipoProductoCaracteristicaDAO, never()).crear(any());
    }

    @Test
    void testCreate_CaracteristicaConIdNull_DebeRetornarBadRequest() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        Caracteristica caracteristicaSinId = new Caracteristica();
        // No establecer ID
        entity.setCaracteristica(caracteristicaSinId);

        when(tipoProductoDAO.findById(tipoProductoId)).thenReturn(tipoProducto);

        // Act
        Response response = resource.create(tipoProductoId, entity, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("caracteristica.id cannot be null", response.getHeaderString("Error"));
        verify(tipoProductoDAO).findById(tipoProductoId);
        verify(caracteristicaDAO, never()).findById(any());
    }

    @Test
    void testCreate_CaracteristicaNoExiste_DebeRetornarNotFound() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        entity.setCaracteristica(caracteristica);

        when(tipoProductoDAO.findById(tipoProductoId)).thenReturn(tipoProducto);
        when(caracteristicaDAO.findById(caracteristicaId)).thenReturn(null);

        // Act
        Response response = resource.create(tipoProductoId, entity, uriInfo);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Caracteristica with id " + caracteristicaId + " not found", response.getHeaderString("Not-Found"));
        verify(tipoProductoDAO).findById(tipoProductoId);
        verify(caracteristicaDAO).findById(caracteristicaId);
        verify(tipoProductoCaracteristicaDAO, never()).crear(any());
    }

    @Test
    void testCreate_DatosValidos_DebeRetornarCreated() throws Exception {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        entity.setCaracteristica(caracteristica);
        entity.setObligatorio(true);

        Long nuevoId = 100L;
        URI locationUri = new URI("http://localhost:8080/api/tipo_producto/" + tipoProductoId + "/caracteristica/" + nuevoId);

        when(tipoProductoDAO.findById(tipoProductoId)).thenReturn(tipoProducto);
        when(caracteristicaDAO.findById(caracteristicaId)).thenReturn(caracteristica);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        doAnswer(invocation -> {
            TipoProductoCaracteristica tpc = invocation.getArgument(0);
            tpc.setId(nuevoId);
            return null;
        }).when(tipoProductoCaracteristicaDAO).crear(any(TipoProductoCaracteristica.class));

        // Act
        Response response = resource.create(tipoProductoId, entity, uriInfo);

        // Assert
        assertEquals(201, response.getStatus());
        assertNotNull(response.getLocation());
        assertEquals(locationUri, response.getLocation());
        assertNotNull(response.getEntity());

        verify(tipoProductoDAO).findById(tipoProductoId);
        verify(caracteristicaDAO).findById(caracteristicaId);
        verify(tipoProductoCaracteristicaDAO).crear(any(TipoProductoCaracteristica.class));
    }

    @Test
    void testCreate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        entity.setCaracteristica(caracteristica);

        when(tipoProductoDAO.findById(tipoProductoId)).thenReturn(tipoProducto);
        when(caracteristicaDAO.findById(caracteristicaId)).thenReturn(caracteristica);
        doThrow(new RuntimeException("Error al crear"))
                .when(tipoProductoCaracteristicaDAO).crear(any());

        // Act
        Response response = resource.create(tipoProductoId, entity, uriInfo);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getHeaderString("Server-Exception").contains("Error creating"));
        verify(tipoProductoDAO).findById(tipoProductoId);
        verify(caracteristicaDAO).findById(caracteristicaId);
        verify(tipoProductoCaracteristicaDAO).crear(any(TipoProductoCaracteristica.class));
    }

    // ==================== TESTS DE update ====================

    @Test
    void testUpdate_IdTipoProductoNull_DebeRetornarBadRequest() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();

        // Act
        Response response = resource.update(null, tpcId, entity);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("IDs cannot be null", response.getHeaderString("Error"));
        verify(tipoProductoCaracteristicaDAO, never()).findById(any());
    }

    @Test
    void testUpdate_IdTPCNull_DebeRetornarBadRequest() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();

        // Act
        Response response = resource.update(tipoProductoId, null, entity);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("IDs cannot be null", response.getHeaderString("Error"));
        verify(tipoProductoCaracteristicaDAO, never()).findById(any());
    }

    @Test
    void testUpdate_EntityNull_DebeRetornarBadRequest() {
        // Act
        Response response = resource.update(tipoProductoId, tpcId, null);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("Request body is required", response.getHeaderString("Error"));
        verify(tipoProductoCaracteristicaDAO, never()).findById(any());
    }

    @Test
    void testUpdate_RelacionNoExiste_DebeRetornarNotFound() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(null);

        // Act
        Response response = resource.update(tipoProductoId, tpcId, entity);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + tpcId + " not found", response.getHeaderString("Not-Found"));
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
        verify(tipoProductoCaracteristicaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_RelacionPerteneceOtroTipoProducto_DebeRetornarNotFound() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        TipoProducto otroTipoProducto = new TipoProducto();
        otroTipoProducto.setId(999L);
        tpcRelacion.setTipoProducto(otroTipoProducto);

        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);

        // Act
        Response response = resource.update(tipoProductoId, tpcId, entity);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Caracteristica does not belong to this tipo_producto", response.getHeaderString("Not-Found"));
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
        verify(tipoProductoCaracteristicaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ActualizarTipoProductoConIdNull_DebeRetornarBadRequest() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        TipoProducto tipoProductoConIdNull = new TipoProducto();
        // No establecer ID
        entity.setTipoProducto(tipoProductoConIdNull);

        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);

        // Act
        Response response = resource.update(tipoProductoId, tpcId, entity);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("tipoProducto.id cannot be null", response.getHeaderString("Error"));
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
        verify(tipoProductoDAO, never()).findById(any());
    }

    @Test
    void testUpdate_ActualizarTipoProductoConTPNoExiste_DebeRetornarNotFound() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        Long nuevoTipoProductoId = 999L;
        TipoProducto nuevoTipoProducto = new TipoProducto();
        nuevoTipoProducto.setId(nuevoTipoProductoId);
        entity.setTipoProducto(nuevoTipoProducto);

        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);
        when(tipoProductoDAO.findById(nuevoTipoProductoId)).thenReturn(null);

        // Act
        Response response = resource.update(tipoProductoId, tpcId, entity);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("TipoProducto with id " + nuevoTipoProductoId + " not found", response.getHeaderString("Not-Found"));
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
        verify(tipoProductoDAO).findById(nuevoTipoProductoId);
        verify(tipoProductoCaracteristicaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ActualizarCaracteristicaConIdNull_DebeRetornarBadRequest() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        Caracteristica caracteristicaConIdNull = new Caracteristica();
        // No establecer ID
        entity.setCaracteristica(caracteristicaConIdNull);

        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);

        // Act
        Response response = resource.update(tipoProductoId, tpcId, entity);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("caracteristica.id cannot be null", response.getHeaderString("Error"));
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
        verify(caracteristicaDAO, never()).findById(any());
    }

    @Test
    void testUpdate_ActualizarCaracteristicaConCaracteristicaNoExiste_DebeRetornarNotFound() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        Integer nuevaCaracteristicaId = 999;
        Caracteristica nuevaCaracteristica = new Caracteristica();
        nuevaCaracteristica.setId(nuevaCaracteristicaId);
        entity.setCaracteristica(nuevaCaracteristica);

        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);
        when(caracteristicaDAO.findById(nuevaCaracteristicaId)).thenReturn(null);

        // Act
        Response response = resource.update(tipoProductoId, tpcId, entity);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Caracteristica with id " + nuevaCaracteristicaId + " not found", response.getHeaderString("Not-Found"));
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
        verify(caracteristicaDAO).findById(nuevaCaracteristicaId);
        verify(tipoProductoCaracteristicaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ActualizarSoloObligatorio_DebeActualizarCorrectamente() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        entity.setObligatorio(false);

        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);
        when(tipoProductoCaracteristicaDAO.modificar(any(TipoProductoCaracteristica.class))).thenReturn(tpcRelacion);

        // Act
        Response response = resource.update(tipoProductoId, tpcId, entity);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
        verify(tipoProductoCaracteristicaDAO).modificar(any(TipoProductoCaracteristica.class));
        verify(tipoProductoDAO, never()).findById(any());
        verify(caracteristicaDAO, never()).findById(any());
    }

    @Test
    void testUpdate_ActualizarSoloFechaCreacion_DebeActualizarCorrectamente() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        entity.setFechaCreacion(OffsetDateTime.now().plusDays(1));

        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);
        when(tipoProductoCaracteristicaDAO.modificar(any(TipoProductoCaracteristica.class))).thenReturn(tpcRelacion);

        // Act
        Response response = resource.update(tipoProductoId, tpcId, entity);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
        verify(tipoProductoCaracteristicaDAO).modificar(any(TipoProductoCaracteristica.class));
    }

    @Test
    void testUpdate_ActualizarTipoProducto_DebeCambiarRelacion() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        Long nuevoTipoProductoId = 999L;
        TipoProducto nuevoTipoProducto = new TipoProducto();
        nuevoTipoProducto.setId(nuevoTipoProductoId);
        entity.setTipoProducto(nuevoTipoProducto);

        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);
        when(tipoProductoDAO.findById(nuevoTipoProductoId)).thenReturn(nuevoTipoProducto);
        when(tipoProductoCaracteristicaDAO.modificar(any(TipoProductoCaracteristica.class))).thenReturn(tpcRelacion);

        // Act
        Response response = resource.update(tipoProductoId, tpcId, entity);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoProductoDAO).findById(nuevoTipoProductoId);
        verify(tipoProductoCaracteristicaDAO).modificar(any(TipoProductoCaracteristica.class));
    }

    @Test
    void testUpdate_ActualizarCaracteristica_DebeCambiarRelacion() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        Integer nuevaCaracteristicaId = 999;
        Caracteristica nuevaCaracteristica = new Caracteristica();
        nuevaCaracteristica.setId(nuevaCaracteristicaId);
        entity.setCaracteristica(nuevaCaracteristica);

        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);
        when(caracteristicaDAO.findById(nuevaCaracteristicaId)).thenReturn(nuevaCaracteristica);
        when(tipoProductoCaracteristicaDAO.modificar(any(TipoProductoCaracteristica.class))).thenReturn(tpcRelacion);

        // Act
        Response response = resource.update(tipoProductoId, tpcId, entity);

        // Assert
        assertEquals(200, response.getStatus());
        verify(caracteristicaDAO).findById(nuevaCaracteristicaId);
        verify(tipoProductoCaracteristicaDAO).modificar(any(TipoProductoCaracteristica.class));
    }

    @Test
    void testUpdate_ActualizarAmbos_DebeActualizarCorrectamente() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();

        Long nuevoTipoProductoId = 999L;
        TipoProducto nuevoTipoProducto = new TipoProducto();
        nuevoTipoProducto.setId(nuevoTipoProductoId);
        entity.setTipoProducto(nuevoTipoProducto);

        Integer nuevaCaracteristicaId = 999;
        Caracteristica nuevaCaracteristica = new Caracteristica();
        nuevaCaracteristica.setId(nuevaCaracteristicaId);
        entity.setCaracteristica(nuevaCaracteristica);

        entity.setObligatorio(false);

        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);
        when(tipoProductoDAO.findById(nuevoTipoProductoId)).thenReturn(nuevoTipoProducto);
        when(caracteristicaDAO.findById(nuevaCaracteristicaId)).thenReturn(nuevaCaracteristica);
        when(tipoProductoCaracteristicaDAO.modificar(any(TipoProductoCaracteristica.class))).thenReturn(tpcRelacion);

        // Act
        Response response = resource.update(tipoProductoId, tpcId, entity);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoProductoDAO).findById(nuevoTipoProductoId);
        verify(caracteristicaDAO).findById(nuevaCaracteristicaId);
        verify(tipoProductoCaracteristicaDAO).modificar(any(TipoProductoCaracteristica.class));
    }

    @Test
    void testUpdate_SinCambiosEnRelaciones_DebeActualizarCorrectamente() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        // No establecer relaciones en entity

        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);
        when(tipoProductoCaracteristicaDAO.modificar(any(TipoProductoCaracteristica.class))).thenReturn(tpcRelacion);

        // Act
        Response response = resource.update(tipoProductoId, tpcId, entity);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
        verify(tipoProductoCaracteristicaDAO).modificar(any(TipoProductoCaracteristica.class));
        verify(tipoProductoDAO, never()).findById(any());
        verify(caracteristicaDAO, never()).findById(any());
    }

    @Test
    void testUpdate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        TipoProductoCaracteristica entity = new TipoProductoCaracteristica();
        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);
        when(tipoProductoCaracteristicaDAO.modificar(any()))
                .thenThrow(new RuntimeException("Error al actualizar"));

        // Act
        Response response = resource.update(tipoProductoId, tpcId, entity);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getHeaderString("Server-Exception").contains("Error updating"));
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
        verify(tipoProductoCaracteristicaDAO).modificar(any(TipoProductoCaracteristica.class));
    }

    // ==================== TESTS DE delete ====================

    @Test
    void testDelete_IdTPCNull_DebeRetornarBadRequest() {
        // Act
        Response response = resource.delete(tipoProductoId, null);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idTPC must be a positive number", response.getHeaderString("Error"));
        verify(tipoProductoCaracteristicaDAO, never()).findById(any());
    }

    @Test
    void testDelete_IdTPCCero_DebeRetornarBadRequest() {
        // Act
        Response response = resource.delete(tipoProductoId, 0L);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idTPC must be a positive number", response.getHeaderString("Error"));
        verify(tipoProductoCaracteristicaDAO, never()).findById(any());
    }

    @Test
    void testDelete_IdTPCNegativo_DebeRetornarBadRequest() {
        // Act
        Response response = resource.delete(tipoProductoId, -1L);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idTPC must be a positive number", response.getHeaderString("Error"));
        verify(tipoProductoCaracteristicaDAO, never()).findById(any());
    }

    @Test
    void testDelete_RelacionExiste_DebeRetornarNoContent() {
        // Arrange
        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);
        doNothing().when(tipoProductoCaracteristicaDAO).eliminar(tpcRelacion);

        // Act
        Response response = resource.delete(tipoProductoId, tpcId);

        // Assert
        assertEquals(204, response.getStatus());
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
        verify(tipoProductoCaracteristicaDAO).eliminar(tpcRelacion);
    }

    @Test
    void testDelete_RelacionNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(null);

        // Act
        Response response = resource.delete(tipoProductoId, tpcId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + tpcId + " not found", response.getHeaderString("Not-Found"));
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
        verify(tipoProductoCaracteristicaDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_RelacionPerteneceOtroTipoProducto_DebeRetornarNotFound() {
        // Arrange
        TipoProducto otroTipoProducto = new TipoProducto();
        otroTipoProducto.setId(999L);
        tpcRelacion.setTipoProducto(otroTipoProducto);

        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);

        // Act
        Response response = resource.delete(tipoProductoId, tpcId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Caracteristica does not belong to this tipo_producto", response.getHeaderString("Not-Found"));
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
        verify(tipoProductoCaracteristicaDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tpcRelacion);
        doThrow(new RuntimeException("Error al eliminar"))
                .when(tipoProductoCaracteristicaDAO).eliminar(any());

        // Act
        Response response = resource.delete(tipoProductoId, tpcId);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getHeaderString("Server-Exception").contains("Cannot delete record"));
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
        verify(tipoProductoCaracteristicaDAO).eliminar(any());
    }
}