package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoTipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.*;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para ProductoTipoProductoCaracteristicaResource
 * Verifica el comportamiento de todos los endpoints REST para características de producto_tipo_producto
 */
class ProductoTipoProductoCaracteristicaResourceTest {

    @Mock
    private ProductoTipoProductoCaracteristicaDAO productoTipoProductoCaracteristicaDAO;

    @Mock
    private ProductoTipoProductoDAO productoTipoProductoDAO;

    @Mock
    private TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    private ProductoTipoProductoCaracteristicaResource resource;
    private ProductoTipoProducto productoTipoProducto;
    private TipoProductoCaracteristica tipoProductoCaracteristica;
    private ProductoTipoProductoCaracteristica ptpCaracteristica;
    private Caracteristica caracteristica;
    private UUID ptpId;
    private UUID ptpcId;
    private Long tpcId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resource = new ProductoTipoProductoCaracteristicaResource();

        // Inyección manual de los DAOs (sin private en el Resource)
        resource.productoTipoProductoCaracteristicaDAO = productoTipoProductoCaracteristicaDAO;
        resource.productoTipoProductoDAO = productoTipoProductoDAO;
        resource.tipoProductoCaracteristicaDAO = tipoProductoCaracteristicaDAO;

        // Datos de prueba
        ptpId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ptpcId = UUID.fromString("223e4567-e89b-12d3-a456-426614174000");
        tpcId = 1L;

        // Configurar ProductoTipoProducto
        productoTipoProducto = new ProductoTipoProducto();
        productoTipoProducto.setId(ptpId);

        // Configurar Caracteristica
        caracteristica = new Caracteristica();
        caracteristica.setNombre("Color");

        // Configurar TipoProductoCaracteristica
        tipoProductoCaracteristica = new TipoProductoCaracteristica();
        tipoProductoCaracteristica.setId(tpcId);
        tipoProductoCaracteristica.setCaracteristica(caracteristica);
        tipoProductoCaracteristica.setObligatorio(true);

        // Configurar ProductoTipoProductoCaracteristica
        ptpCaracteristica = new ProductoTipoProductoCaracteristica();
        ptpCaracteristica.setId(ptpcId);
        ptpCaracteristica.setIdProductoTipoProducto(productoTipoProducto);
        ptpCaracteristica.setIdTipoProductoCaracteristica(tipoProductoCaracteristica);
        ptpCaracteristica.setValor("Rojo");
        ptpCaracteristica.setObservaciones("Color principal");
    }

    // ==================== TESTS DE getCaracteristicas ====================

    @Test
    void testGetCaracteristicas_IdPTPNull_DebeRetornarBadRequest() {
        // Act
        Response response = resource.getCaracteristicas(null);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idPTP cannot be null", response.getHeaderString("Error"));
        verify(productoTipoProductoDAO, never()).findById(any());
    }

    @Test
    void testGetCaracteristicas_PTPExisteConCaracteristicas_DebeRetornarOk() {
        // Arrange
        List<ProductoTipoProductoCaracteristica> caracteristicas = new ArrayList<>();
        caracteristicas.add(ptpCaracteristica);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(productoTipoProducto);
        when(productoTipoProductoCaracteristicaDAO.findByIdProductoTipoProducto(ptpId)).thenReturn(caracteristicas);

        // Act
        Response response = resource.getCaracteristicas(ptpId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> resultado = (List<Map<String, Object>>) response.getEntity();
        assertEquals(1, resultado.size());

        Map<String, Object> primeraCaracteristica = resultado.get(0);
        assertEquals("Color", primeraCaracteristica.get("nombreCaracteristica"));
        assertEquals("Rojo", primeraCaracteristica.get("valor"));
        assertEquals(true, primeraCaracteristica.get("obligatorio"));

        verify(productoTipoProductoDAO).findById(ptpId);
        verify(productoTipoProductoCaracteristicaDAO).findByIdProductoTipoProducto(ptpId);
    }

    @Test
    void testGetCaracteristicas_PTPNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(null);

        // Act
        Response response = resource.getCaracteristicas(ptpId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("ProductoTipoProducto with id " + ptpId + " not found", response.getHeaderString("Not-Found"));
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(productoTipoProductoCaracteristicaDAO, never()).findByIdProductoTipoProducto(any());
    }

    @Test
    void testGetCaracteristicas_PTPExisteSinCaracteristicas_DebeRetornarOkConListaVacia() {
        // Arrange
        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(productoTipoProducto);
        when(productoTipoProductoCaracteristicaDAO.findByIdProductoTipoProducto(ptpId)).thenReturn(new ArrayList<>());

        // Act
        Response response = resource.getCaracteristicas(ptpId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> resultado = (List<Map<String, Object>>) response.getEntity();
        assertTrue(resultado.isEmpty());

        verify(productoTipoProductoDAO).findById(ptpId);
        verify(productoTipoProductoCaracteristicaDAO).findByIdProductoTipoProducto(ptpId);
    }

    @Test
    void testGetCaracteristicas_CaracteristicasNull_DebeRetornarOkConListaVacia() {
        // Arrange
        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(productoTipoProducto);
        when(productoTipoProductoCaracteristicaDAO.findByIdProductoTipoProducto(ptpId)).thenReturn(null);

        // Act
        Response response = resource.getCaracteristicas(ptpId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> resultado = (List<Map<String, Object>>) response.getEntity();
        assertTrue(resultado.isEmpty());

        verify(productoTipoProductoDAO).findById(ptpId);
        verify(productoTipoProductoCaracteristicaDAO).findByIdProductoTipoProducto(ptpId);
    }

    @Test
    void testGetCaracteristicas_CaracteristicaConValorNull_DebeMapearCorrectamente() {
        // Arrange
        ptpCaracteristica.setValor(null);
        ptpCaracteristica.setObservaciones(null);

        List<ProductoTipoProductoCaracteristica> caracteristicas = new ArrayList<>();
        caracteristicas.add(ptpCaracteristica);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(productoTipoProducto);
        when(productoTipoProductoCaracteristicaDAO.findByIdProductoTipoProducto(ptpId)).thenReturn(caracteristicas);

        // Act
        Response response = resource.getCaracteristicas(ptpId);

        // Assert
        assertEquals(200, response.getStatus());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> resultado = (List<Map<String, Object>>) response.getEntity();
        Map<String, Object> caracteristica = resultado.get(0);
        assertEquals("", caracteristica.get("valor"));
        assertEquals("", caracteristica.get("observaciones"));
    }

    @Test
    void testGetCaracteristicas_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(productoTipoProductoDAO.findById(ptpId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = resource.getCaracteristicas(ptpId);

        // Assert
        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-Exception"));
        verify(productoTipoProductoDAO).findById(ptpId);
    }

    // ==================== TESTS DE findById ====================

    @Test
    void testFindById_IdPTPCNull_DebeRetornarBadRequest() {
        // Act
        Response response = resource.findById(ptpId, null);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idPTPC cannot be null", response.getHeaderString("Error"));
        verify(productoTipoProductoCaracteristicaDAO, never()).findById(any());
    }

    @Test
    void testFindById_CaracteristicaExisteYCoincidePTP_DebeRetornarOk() {
        // Arrange
        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(ptpCaracteristica);

        // Act
        Response response = resource.findById(ptpId, ptpcId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        ProductoTipoProductoCaracteristica resultado = (ProductoTipoProductoCaracteristica) response.getEntity();
        assertEquals(ptpcId, resultado.getId());

        verify(productoTipoProductoCaracteristicaDAO).findById(ptpcId);
    }

    @Test
    void testFindById_CaracteristicaNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(null);

        // Act
        Response response = resource.findById(ptpId, ptpcId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + ptpcId + " not found", response.getHeaderString("Not-Found"));
        verify(productoTipoProductoCaracteristicaDAO).findById(ptpcId);
    }

    @Test
    void testFindById_CaracteristicaSinPTP_DebeRetornarNotFound() {
        // Arrange
        ptpCaracteristica.setIdProductoTipoProducto(null);
        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(ptpCaracteristica);

        // Act
        Response response = resource.findById(ptpId, ptpcId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Caracteristica no pertenece a este ProductoTipoProducto", response.getHeaderString("Not-Found"));
        verify(productoTipoProductoCaracteristicaDAO).findById(ptpcId);
    }

    @Test
    void testFindById_CaracteristicaPerteneceOtroPTP_DebeRetornarNotFound() {
        // Arrange
        ProductoTipoProducto otroPTP = new ProductoTipoProducto();
        otroPTP.setId(UUID.fromString("999e4567-e89b-12d3-a456-426614174000"));
        ptpCaracteristica.setIdProductoTipoProducto(otroPTP);

        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(ptpCaracteristica);

        // Act
        Response response = resource.findById(ptpId, ptpcId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Caracteristica no pertenece a este ProductoTipoProducto", response.getHeaderString("Not-Found"));
        verify(productoTipoProductoCaracteristicaDAO).findById(ptpcId);
    }

    @Test
    void testFindById_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = resource.findById(ptpId, ptpcId);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getHeaderString("Server-Exception").contains("Cannot access db"));
        verify(productoTipoProductoCaracteristicaDAO).findById(ptpcId);
    }

    // ==================== TESTS DE create ====================

    @Test
    void testCreate_IdPTPNull_DebeRetornarBadRequest() {
        // Act
        Response response = resource.create(null, new ProductoTipoProductoCaracteristica(), uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idPTP cannot be null", response.getHeaderString("Error"));
        verify(productoTipoProductoDAO, never()).findById(any());
    }

    @Test
    void testCreate_EntityNull_DebeRetornarBadRequest() {
        // Act
        Response response = resource.create(ptpId, null, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idTipoProductoCaracteristica is required", response.getHeaderString("Error"));
        verify(productoTipoProductoDAO, never()).findById(any());
    }

    @Test
    void testCreate_EntitySinTipoProductoCaracteristica_DebeRetornarBadRequest() {
        // Arrange
        ProductoTipoProductoCaracteristica entity = new ProductoTipoProductoCaracteristica();
        // No se establece idTipoProductoCaracteristica

        // Act
        Response response = resource.create(ptpId, entity, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idTipoProductoCaracteristica is required", response.getHeaderString("Error"));
        verify(productoTipoProductoDAO, never()).findById(any());
    }

    @Test
    void testCreate_ConIdNoNull_DebeRetornarBadRequest() {
        // Arrange
        ProductoTipoProductoCaracteristica nuevaCaracteristica = new ProductoTipoProductoCaracteristica();
        nuevaCaracteristica.setId(ptpcId); // ID no null
        nuevaCaracteristica.setIdTipoProductoCaracteristica(tipoProductoCaracteristica);

        // Act
        Response response = resource.create(ptpId, nuevaCaracteristica, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idProductoTipoProductoCaracteristica no debe agregarse", response.getHeaderString("Error"));
        verify(productoTipoProductoDAO, never()).findById(any());
    }

    @Test
    void testCreate_PTPNoExiste_DebeRetornarNotFound() {
        // Arrange
        ProductoTipoProductoCaracteristica nuevaCaracteristica = new ProductoTipoProductoCaracteristica();
        nuevaCaracteristica.setIdTipoProductoCaracteristica(tipoProductoCaracteristica);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(null);

        // Act
        Response response = resource.create(ptpId, nuevaCaracteristica, uriInfo);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("ProductoTipoProducto with id " + ptpId + " not found", response.getHeaderString("Not-Found"));
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(tipoProductoCaracteristicaDAO, never()).findById(any());
        verify(productoTipoProductoCaracteristicaDAO, never()).crear(any());
    }

    @Test
    void testCreate_TipoProductoCaracteristicaNoExiste_DebeRetornarNotFound() {
        // Arrange
        ProductoTipoProductoCaracteristica nuevaCaracteristica = new ProductoTipoProductoCaracteristica();
        nuevaCaracteristica.setIdTipoProductoCaracteristica(tipoProductoCaracteristica);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(productoTipoProducto);
        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(null);

        // Act
        Response response = resource.create(ptpId, nuevaCaracteristica, uriInfo);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("TipoProductoCaracteristica with id " + tpcId + " not found", response.getHeaderString("Not-Found"));
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
        verify(productoTipoProductoCaracteristicaDAO, never()).crear(any());
    }

    @Test
    void testCreate_TipoProductoCaracteristicaConIdNull_DebeRetornarBadRequest() {
        // Arrange
        TipoProductoCaracteristica tpcSinId = new TipoProductoCaracteristica();
        // No establecer ID

        ProductoTipoProductoCaracteristica nuevaCaracteristica = new ProductoTipoProductoCaracteristica();
        nuevaCaracteristica.setIdTipoProductoCaracteristica(tpcSinId);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(productoTipoProducto);

        // Act
        Response response = resource.create(ptpId, nuevaCaracteristica, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idTipoProductoCaracteristica.id no puede ser null", response.getHeaderString("Error"));
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(tipoProductoCaracteristicaDAO, never()).findById(any());
    }

    @Test
    void testCreate_ConPTPEnBodyQueNoCoincide_DebeRetornarBadRequest() {
        // Arrange
        ProductoTipoProducto otroPTP = new ProductoTipoProducto();
        otroPTP.setId(UUID.fromString("999e4567-e89b-12d3-a456-426614174000"));

        ProductoTipoProductoCaracteristica nuevaCaracteristica = new ProductoTipoProductoCaracteristica();
        nuevaCaracteristica.setIdTipoProductoCaracteristica(tipoProductoCaracteristica);
        nuevaCaracteristica.setIdProductoTipoProducto(otroPTP);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(productoTipoProducto);

        // Act
        Response response = resource.create(ptpId, nuevaCaracteristica, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idProductoTipoProducto del json y el del path no coinciden", response.getHeaderString("Error"));
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(tipoProductoCaracteristicaDAO, never()).findById(any());
    }

    @Test
    void testCreate_DatosValidos_DebeRetornarCreated() throws Exception {
        // Arrange
        ProductoTipoProductoCaracteristica nuevaCaracteristica = new ProductoTipoProductoCaracteristica();
        nuevaCaracteristica.setIdTipoProductoCaracteristica(tipoProductoCaracteristica);
        nuevaCaracteristica.setValor("Azul");

        UUID nuevoId = UUID.fromString("323e4567-e89b-12d3-a456-426614174000");
        URI locationUri = new URI("http://localhost:8080/api/producto_tipo_producto/" + ptpId + "/caracteristica/" + nuevoId);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(productoTipoProducto);
        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tipoProductoCaracteristica);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        doAnswer(invocation -> {
            ProductoTipoProductoCaracteristica ptpc = invocation.getArgument(0);
            ptpc.setId(nuevoId);
            return null;
        }).when(productoTipoProductoCaracteristicaDAO).crear(any(ProductoTipoProductoCaracteristica.class));

        // Act
        Response response = resource.create(ptpId, nuevaCaracteristica, uriInfo);

        // Assert
        assertEquals(201, response.getStatus());
        assertNotNull(response.getLocation());
        assertEquals(locationUri, response.getLocation());
        assertNotNull(response.getEntity());

        verify(productoTipoProductoDAO).findById(ptpId);
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
        verify(productoTipoProductoCaracteristicaDAO).crear(any(ProductoTipoProductoCaracteristica.class));
    }

    @Test
    void testCreate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        ProductoTipoProductoCaracteristica nuevaCaracteristica = new ProductoTipoProductoCaracteristica();
        nuevaCaracteristica.setIdTipoProductoCaracteristica(tipoProductoCaracteristica);

        when(productoTipoProductoDAO.findById(ptpId)).thenReturn(productoTipoProducto);
        when(tipoProductoCaracteristicaDAO.findById(tpcId)).thenReturn(tipoProductoCaracteristica);
        doThrow(new RuntimeException("Error al crear"))
                .when(productoTipoProductoCaracteristicaDAO).crear(any());

        // Act
        Response response = resource.create(ptpId, nuevaCaracteristica, uriInfo);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getHeaderString("Server-Exception").contains("Error creating"));
        verify(productoTipoProductoDAO).findById(ptpId);
        verify(tipoProductoCaracteristicaDAO).findById(tpcId);
        verify(productoTipoProductoCaracteristicaDAO).crear(any(ProductoTipoProductoCaracteristica.class));
    }

    // ==================== TESTS DE update ====================

    @Test
    void testUpdate_IdPTPNull_DebeRetornarBadRequest() {
        // Arrange
        ProductoTipoProductoCaracteristica actualizacion = new ProductoTipoProductoCaracteristica();

        // Act
        Response response = resource.update(null, ptpcId, actualizacion);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("IDs cannot be null", response.getHeaderString("Error"));
        verify(productoTipoProductoCaracteristicaDAO, never()).findById(any());
    }

    @Test
    void testUpdate_IdPTPCNull_DebeRetornarBadRequest() {
        // Arrange
        ProductoTipoProductoCaracteristica actualizacion = new ProductoTipoProductoCaracteristica();

        // Act
        Response response = resource.update(ptpId, null, actualizacion);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("IDs cannot be null", response.getHeaderString("Error"));
        verify(productoTipoProductoCaracteristicaDAO, never()).findById(any());
    }

    @Test
    void testUpdate_EntityNull_DebeRetornarBadRequest() {
        // Act
        Response response = resource.update(ptpId, ptpcId, null);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("Request body is required", response.getHeaderString("Error"));
        verify(productoTipoProductoCaracteristicaDAO, never()).findById(any());
    }

    @Test
    void testUpdate_CaracteristicaNoExiste_DebeRetornarNotFound() {
        // Arrange
        ProductoTipoProductoCaracteristica actualizacion = new ProductoTipoProductoCaracteristica();
        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(null);

        // Act
        Response response = resource.update(ptpId, ptpcId, actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + ptpcId + " not found", response.getHeaderString("Not-Found"));
        verify(productoTipoProductoCaracteristicaDAO).findById(ptpcId);
        verify(productoTipoProductoCaracteristicaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_CaracteristicaPerteneceOtroPTP_DebeRetornarNotFound() {
        // Arrange
        ProductoTipoProductoCaracteristica actualizacion = new ProductoTipoProductoCaracteristica();
        ProductoTipoProducto otroPTP = new ProductoTipoProducto();
        otroPTP.setId(UUID.fromString("999e4567-e89b-12d3-a456-426614174000"));
        ptpCaracteristica.setIdProductoTipoProducto(otroPTP);

        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(ptpCaracteristica);

        // Act
        Response response = resource.update(ptpId, ptpcId, actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Caracteristica no pertenece a este ProductoTipoProducto", response.getHeaderString("Not-Found"));
        verify(productoTipoProductoCaracteristicaDAO).findById(ptpcId);
        verify(productoTipoProductoCaracteristicaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ActualizarPTPConIdNull_DebeRetornarBadRequest() {
        // Arrange
        ProductoTipoProductoCaracteristica actualizacion = new ProductoTipoProductoCaracteristica();
        ProductoTipoProducto ptpConIdNull = new ProductoTipoProducto();
        // No establecer ID
        actualizacion.setIdProductoTipoProducto(ptpConIdNull);

        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(ptpCaracteristica);

        // Act
        Response response = resource.update(ptpId, ptpcId, actualizacion);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idProductoTipoProducto.id cannot be null", response.getHeaderString("Error"));
        verify(productoTipoProductoCaracteristicaDAO).findById(ptpcId);
        verify(productoTipoProductoDAO, never()).findById(any());
    }

    @Test
    void testUpdate_ActualizarPTPConPTPNoExiste_DebeRetornarNotFound() {
        // Arrange
        ProductoTipoProductoCaracteristica actualizacion = new ProductoTipoProductoCaracteristica();
        ProductoTipoProducto nuevoPTP = new ProductoTipoProducto();
        nuevoPTP.setId(UUID.fromString("999e4567-e89b-12d3-a456-426614174000"));
        actualizacion.setIdProductoTipoProducto(nuevoPTP);

        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(ptpCaracteristica);
        when(productoTipoProductoDAO.findById(nuevoPTP.getId())).thenReturn(null);

        // Act
        Response response = resource.update(ptpId, ptpcId, actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("ProductoTipoProducto with id " + nuevoPTP.getId() + " not found", response.getHeaderString("Not-Found"));
        verify(productoTipoProductoCaracteristicaDAO).findById(ptpcId);
        verify(productoTipoProductoDAO).findById(nuevoPTP.getId());
        verify(productoTipoProductoCaracteristicaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ActualizarTPCConIdNull_DebeRetornarBadRequest() {
        // Arrange
        ProductoTipoProductoCaracteristica actualizacion = new ProductoTipoProductoCaracteristica();
        TipoProductoCaracteristica tpcConIdNull = new TipoProductoCaracteristica();
        // No establecer ID
        actualizacion.setIdTipoProductoCaracteristica(tpcConIdNull);

        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(ptpCaracteristica);

        // Act
        Response response = resource.update(ptpId, ptpcId, actualizacion);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idTipoProductoCaracteristica.id no puede ser null", response.getHeaderString("Error"));
        verify(productoTipoProductoCaracteristicaDAO).findById(ptpcId);
        verify(tipoProductoCaracteristicaDAO, never()).findById(any());
    }

    @Test
    void testUpdate_ActualizarTPCConTPCNoExiste_DebeRetornarNotFound() {
        // Arrange
        ProductoTipoProductoCaracteristica actualizacion = new ProductoTipoProductoCaracteristica();
        TipoProductoCaracteristica nuevaTPC = new TipoProductoCaracteristica();
        nuevaTPC.setId(999L);
        actualizacion.setIdTipoProductoCaracteristica(nuevaTPC);

        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(ptpCaracteristica);
        when(tipoProductoCaracteristicaDAO.findById(nuevaTPC.getId())).thenReturn(null);

        // Act
        Response response = resource.update(ptpId, ptpcId, actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("TipoProductoCaracteristica with id " + nuevaTPC.getId() + " not found", response.getHeaderString("Not-Found"));
        verify(productoTipoProductoCaracteristicaDAO).findById(ptpcId);
        verify(tipoProductoCaracteristicaDAO).findById(nuevaTPC.getId());
        verify(productoTipoProductoCaracteristicaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_DatosValidos_DebeRetornarOk() {
        // Arrange
        ProductoTipoProductoCaracteristica actualizacion = new ProductoTipoProductoCaracteristica();
        actualizacion.setValor("Verde");
        actualizacion.setObservaciones("Color secundario");

        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(ptpCaracteristica);
        when(productoTipoProductoCaracteristicaDAO.modificar(any(ProductoTipoProductoCaracteristica.class))).thenReturn(ptpCaracteristica);

        // Act
        Response response = resource.update(ptpId, ptpcId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        verify(productoTipoProductoCaracteristicaDAO).findById(ptpcId);
        verify(productoTipoProductoCaracteristicaDAO).modificar(any(ProductoTipoProductoCaracteristica.class));
    }

    @Test
    void testUpdate_ActualizarPTP_DebeCambiarRelacion() {
        // Arrange
        ProductoTipoProducto nuevoPTP = new ProductoTipoProducto();
        nuevoPTP.setId(UUID.fromString("999e4567-e89b-12d3-a456-426614174000"));

        ProductoTipoProductoCaracteristica actualizacion = new ProductoTipoProductoCaracteristica();
        actualizacion.setIdProductoTipoProducto(nuevoPTP);

        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(ptpCaracteristica);
        when(productoTipoProductoDAO.findById(nuevoPTP.getId())).thenReturn(nuevoPTP);
        when(productoTipoProductoCaracteristicaDAO.modificar(any(ProductoTipoProductoCaracteristica.class))).thenReturn(ptpCaracteristica);

        // Act
        Response response = resource.update(ptpId, ptpcId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(productoTipoProductoDAO).findById(nuevoPTP.getId());
        verify(productoTipoProductoCaracteristicaDAO).modificar(any(ProductoTipoProductoCaracteristica.class));
    }

    @Test
    void testUpdate_ActualizarTPC_DebeCambiarRelacion() {
        // Arrange
        TipoProductoCaracteristica nuevaTPC = new TipoProductoCaracteristica();
        nuevaTPC.setId(999L);

        ProductoTipoProductoCaracteristica actualizacion = new ProductoTipoProductoCaracteristica();
        actualizacion.setIdTipoProductoCaracteristica(nuevaTPC);

        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(ptpCaracteristica);
        when(tipoProductoCaracteristicaDAO.findById(nuevaTPC.getId())).thenReturn(nuevaTPC);
        when(productoTipoProductoCaracteristicaDAO.modificar(any(ProductoTipoProductoCaracteristica.class))).thenReturn(ptpCaracteristica);

        // Act
        Response response = resource.update(ptpId, ptpcId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(tipoProductoCaracteristicaDAO).findById(nuevaTPC.getId());
        verify(productoTipoProductoCaracteristicaDAO).modificar(any(ProductoTipoProductoCaracteristica.class));
    }

    @Test
    void testUpdate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        ProductoTipoProductoCaracteristica actualizacion = new ProductoTipoProductoCaracteristica();
        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(ptpCaracteristica);
        when(productoTipoProductoCaracteristicaDAO.modificar(any()))
                .thenThrow(new RuntimeException("Error al actualizar"));

        // Act
        Response response = resource.update(ptpId, ptpcId, actualizacion);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getHeaderString("Server-Exception").contains("Error updating"));
        verify(productoTipoProductoCaracteristicaDAO).findById(ptpcId);
        verify(productoTipoProductoCaracteristicaDAO).modificar(any(ProductoTipoProductoCaracteristica.class));
    }

    // ==================== TESTS DE delete ====================

    @Test
    void testDelete_IdPTPCNull_DebeRetornarBadRequest() {
        // Act
        Response response = resource.delete(ptpId, null);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idPTPC cannot be null", response.getHeaderString("Error"));
        verify(productoTipoProductoCaracteristicaDAO, never()).findById(any());
    }

    @Test
    void testDelete_CaracteristicaExiste_DebeRetornarNoContent() {
        // Arrange
        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(ptpCaracteristica);
        doNothing().when(productoTipoProductoCaracteristicaDAO).eliminar(ptpCaracteristica);

        // Act
        Response response = resource.delete(ptpId, ptpcId);

        // Assert
        assertEquals(204, response.getStatus());
        verify(productoTipoProductoCaracteristicaDAO).findById(ptpcId);
        verify(productoTipoProductoCaracteristicaDAO).eliminar(ptpCaracteristica);
    }

    @Test
    void testDelete_CaracteristicaNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(null);

        // Act
        Response response = resource.delete(ptpId, ptpcId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + ptpcId + " not found", response.getHeaderString("Not-Found"));
        verify(productoTipoProductoCaracteristicaDAO).findById(ptpcId);
        verify(productoTipoProductoCaracteristicaDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_CaracteristicaPerteneceOtroPTP_DebeRetornarNotFound() {
        // Arrange
        ProductoTipoProducto otroPTP = new ProductoTipoProducto();
        otroPTP.setId(UUID.fromString("999e4567-e89b-12d3-a456-426614174000"));
        ptpCaracteristica.setIdProductoTipoProducto(otroPTP);

        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(ptpCaracteristica);

        // Act
        Response response = resource.delete(ptpId, ptpcId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Caracteristica no pertenece a este ProductoTipoProducto", response.getHeaderString("Not-Found"));
        verify(productoTipoProductoCaracteristicaDAO).findById(ptpcId);
        verify(productoTipoProductoCaracteristicaDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(productoTipoProductoCaracteristicaDAO.findById(ptpcId)).thenReturn(ptpCaracteristica);
        doThrow(new RuntimeException("Error al eliminar"))
                .when(productoTipoProductoCaracteristicaDAO).eliminar(any());

        // Act
        Response response = resource.delete(ptpId, ptpcId);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getHeaderString("Server-Exception").contains("Cannot delete record"));
        verify(productoTipoProductoCaracteristicaDAO).findById(ptpcId);
        verify(productoTipoProductoCaracteristicaDAO).eliminar(any());
    }
}