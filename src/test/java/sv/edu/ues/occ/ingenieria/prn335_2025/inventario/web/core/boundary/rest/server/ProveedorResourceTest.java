package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProveedorDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Proveedor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests para ProveedorResource
 * Verifica el comportamiento de todos los endpoints REST
 */
class ProveedorResourceTest {

    @Mock
    private ProveedorDAO proveedorDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    private ProveedorResource proveedorResource;
    private Proveedor proveedor;
    private Integer proveedorId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        proveedorResource = new ProveedorResource();

        // Inyección manual del DAO
        proveedorResource.proveedorDAO = proveedorDAO;

        // Datos de prueba
        proveedorId = 1;

        proveedor = new Proveedor();
        proveedor.setId(proveedorId);
        proveedor.setNombre("Distribuidora El Salvador S.A.");
        proveedor.setRazonSocial("Distribuidora El Salvador Sociedad Anónima");
        proveedor.setNit("0614-123456-101-2");
        proveedor.setActivo(true);
        proveedor.setObservaciones("Proveedor principal de tecnología");
    }

    // ==================== TESTS DE findRange ====================

    @Test
    void testFindRange_ParametrosValidos_DebeRetornarOkConHeader() {
        // Arrange
        List<Proveedor> proveedores = new ArrayList<>();
        proveedores.add(proveedor);

        Proveedor proveedor2 = new Proveedor();
        proveedor2.setId(2);
        proveedor2.setNombre("Importadora Centroamericana S.A.");
        proveedores.add(proveedor2);

        when(proveedorDAO.findRange(0, 50)).thenReturn(proveedores);
        when(proveedorDAO.count()).thenReturn(150L);

        // Act
        Response response = proveedorResource.findRange(0, 50);

        // Assert
        assertEquals(200, response.getStatus(), "Debe retornar OK");
        assertNotNull(response.getEntity());

        assertEquals("150", response.getHeaderString("Total-records"));

        @SuppressWarnings("unchecked")
        List<Proveedor> resultado = (List<Proveedor>) response.getEntity();
        assertEquals(2, resultado.size());

        verify(proveedorDAO).findRange(0, 50);
        verify(proveedorDAO).count();
    }

    @Test
    void testFindRange_FirstNegativo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = proveedorResource.findRange(-1, 50);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first or max out of range", response.getHeaderString("Missing-parameter"));
        verify(proveedorDAO, never()).findRange(anyInt(), anyInt());
        verify(proveedorDAO, never()).count();
    }

    @Test
    void testFindRange_MaxMayor100_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = proveedorResource.findRange(0, 101);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("first or max out of range", response.getHeaderString("Missing-parameter"));
        verify(proveedorDAO, never()).findRange(anyInt(), anyInt());
        verify(proveedorDAO, never()).count();
    }

    @Test
    void testFindRange_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(proveedorDAO.findRange(0, 50))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        Response response = proveedorResource.findRange(0, 50);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(proveedorDAO).findRange(0, 50);
    }

    @Test
    void testFindRange_ListaVacia_DebeRetornarOkConListaVacia() {
        // Arrange
        List<Proveedor> proveedoresVacios = new ArrayList<>();
        when(proveedorDAO.findRange(0, 50)).thenReturn(proveedoresVacios);
        when(proveedorDAO.count()).thenReturn(0L);

        // Act
        Response response = proveedorResource.findRange(0, 50);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Proveedor> resultado = (List<Proveedor>) response.getEntity();
        assertTrue(resultado.isEmpty());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void testFindRange_ValoresPorDefecto_DebeUsarFirst0Max50() {
        // Arrange
        List<Proveedor> proveedores = new ArrayList<>();
        proveedores.add(proveedor);

        when(proveedorDAO.findRange(0, 50)).thenReturn(proveedores);
        when(proveedorDAO.count()).thenReturn(100L);

        // Act
        Response response = proveedorResource.findRange(0, 50);

        // Assert
        assertEquals(200, response.getStatus());
        verify(proveedorDAO).findRange(0, 50);
    }

    // ==================== TESTS DE findById ====================

    @Test
    void testFindById_IdValidoProveedorExiste_DebeRetornarOk() {
        // Arrange
        when(proveedorDAO.findById(proveedorId)).thenReturn(proveedor);

        // Act
        Response response = proveedorResource.findById(proveedorId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        Proveedor resultado = (Proveedor) response.getEntity();
        assertEquals(proveedorId, resultado.getId());
        assertEquals("Distribuidora El Salvador S.A.", resultado.getNombre());
        assertEquals("0614-123456-101-2", resultado.getNit());

        verify(proveedorDAO).findById(proveedorId);
    }

    @Test
    void testFindById_IdNulo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = proveedorResource.findById(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(proveedorDAO, never()).findById(any());
    }

    @Test
    void testFindById_IdCero_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = proveedorResource.findById(0);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(proveedorDAO, never()).findById(any());
    }

    @Test
    void testFindById_IdNegativo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = proveedorResource.findById(-1);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(proveedorDAO, never()).findById(any());
    }

    @Test
    void testFindById_ProveedorNoExiste_DebeRetornarNotFound() {
        // Arrange
        Integer idNoExistente = 999;
        when(proveedorDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = proveedorResource.findById(idNoExistente);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id 999 not found", response.getHeaderString("Record-not-found"));
        verify(proveedorDAO).findById(idNoExistente);
    }

    @Test
    void testFindById_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(proveedorDAO.findById(proveedorId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = proveedorResource.findById(proveedorId);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(proveedorDAO).findById(proveedorId);
    }

    // ==================== TESTS DE delete ====================

    @Test
    void testDelete_IdValidoProveedorExiste_DebeRetornarNoContent() {
        // Arrange
        when(proveedorDAO.findById(proveedorId)).thenReturn(proveedor);
        doNothing().when(proveedorDAO).eliminar(proveedor);

        // Act
        Response response = proveedorResource.delete(proveedorId);

        // Assert
        assertEquals(204, response.getStatus(), "Debe retornar NO_CONTENT");
        verify(proveedorDAO).findById(proveedorId);
        verify(proveedorDAO).eliminar(proveedor);
    }

    @Test
    void testDelete_IdNulo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = proveedorResource.delete(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(proveedorDAO, never()).findById(any());
        verify(proveedorDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_IdCero_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = proveedorResource.delete(0);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(proveedorDAO, never()).findById(any());
        verify(proveedorDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_IdNegativo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = proveedorResource.delete(-1);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id must be greater than 0", response.getHeaderString("Missing-parameter"));
        verify(proveedorDAO, never()).findById(any());
        verify(proveedorDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_ProveedorNoExiste_DebeRetornarNotFound() {
        // Arrange
        Integer idNoExistente = 999;
        when(proveedorDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = proveedorResource.delete(idNoExistente);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id 999 not found", response.getHeaderString("Record-not-found"));
        verify(proveedorDAO).findById(idNoExistente);
        verify(proveedorDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(proveedorDAO.findById(proveedorId)).thenReturn(proveedor);
        doThrow(new RuntimeException("Error al eliminar"))
                .when(proveedorDAO).eliminar(any());

        // Act
        Response response = proveedorResource.delete(proveedorId);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(proveedorDAO).findById(proveedorId);
        verify(proveedorDAO).eliminar(proveedor);
    }

    @Test
    void testDelete_ProveedorInactivo_DebeEliminarCorrectamente() {
        // Arrange
        proveedor.setActivo(false);
        when(proveedorDAO.findById(proveedorId)).thenReturn(proveedor);
        doNothing().when(proveedorDAO).eliminar(proveedor);

        // Act
        Response response = proveedorResource.delete(proveedorId);

        // Assert
        assertEquals(204, response.getStatus());
        verify(proveedorDAO).eliminar(proveedor);
    }

    // ==================== TESTS DE create ====================

    @Test
    void testCreate_ProveedorValido_DebeRetornarCreated() throws Exception {
        // Arrange
        Proveedor nuevoProveedor = new Proveedor();
        nuevoProveedor.setNombre("Nuevo Proveedor S.A.");
        nuevoProveedor.setRazonSocial("Nuevo Proveedor Sociedad Anónima");
        nuevoProveedor.setNit("0614-987654-202-3");
        nuevoProveedor.setActivo(true);
        // ID debe ser null para crear

        Integer nuevoId = 100;
        URI locationUri = new URI("http://localhost:8080/api/proveedor/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        // Simular que después de crear, se asigna un ID
        doAnswer(invocation -> {
            Proveedor p = invocation.getArgument(0);
            p.setId(nuevoId);
            return null;
        }).when(proveedorDAO).crear(any(Proveedor.class));

        // Act
        Response response = proveedorResource.create(nuevoProveedor, uriInfo);

        // Assert
        assertEquals(201, response.getStatus(), "Debe retornar CREATED");
        assertNotNull(response.getLocation());
        assertEquals(locationUri, response.getLocation());

        verify(proveedorDAO).crear(any(Proveedor.class));
    }

    @Test
    void testCreate_EntityNull_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = proveedorResource.create(null, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("entity must not be null and id must be null", response.getHeaderString("Missing-parameter"));
        verify(proveedorDAO, never()).crear(any());
    }

    @Test
    void testCreate_ProveedorConIdNoNull_DebeRetornarUnprocessableEntity() {
        // Arrange
        Proveedor proveedorConId = new Proveedor();
        proveedorConId.setId(proveedorId);
        proveedorConId.setNombre("Proveedor con ID");

        // Act
        Response response = proveedorResource.create(proveedorConId, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("entity must not be null and id must be null", response.getHeaderString("Missing-parameter"));
        verify(proveedorDAO, never()).crear(any());
    }

    @Test
    void testCreate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        Proveedor nuevoProveedor = new Proveedor();
        nuevoProveedor.setNombre("Proveedor Nuevo");

        doThrow(new RuntimeException("Error al guardar"))
                .when(proveedorDAO).crear(any());

        // Act
        Response response = proveedorResource.create(nuevoProveedor, uriInfo);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(proveedorDAO).crear(any(Proveedor.class));
    }

    @Test
    void testCreate_ProveedorConCamposMinimos_DebeCrearCorrectamente() throws Exception {
        // Arrange
        Proveedor proveedorMinimo = new Proveedor();
        proveedorMinimo.setNombre("Proveedor Mínimo");
        // No tiene razón social, nit, etc. pero según la lógica debería funcionar

        Integer nuevoId = 200;
        URI locationUri = new URI("http://localhost:8080/api/proveedor/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        doAnswer(invocation -> {
            Proveedor p = invocation.getArgument(0);
            p.setId(nuevoId);
            return null;
        }).when(proveedorDAO).crear(any(Proveedor.class));

        // Act
        Response response = proveedorResource.create(proveedorMinimo, uriInfo);

        // Assert
        assertEquals(201, response.getStatus());
        verify(proveedorDAO).crear(any(Proveedor.class));
    }

    // ==================== TESTS DE update ====================

    @Test
    void testUpdate_DatosValidos_DebeRetornarOk() {
        // Arrange
        Proveedor actualizacion = new Proveedor();
        actualizacion.setNombre("Distribuidora Actualizada S.A.");
        actualizacion.setRazonSocial("Razón Social Actualizada");
        actualizacion.setNit("0614-123456-999-9");
        actualizacion.setActivo(false);
        actualizacion.setObservaciones("Observaciones actualizadas");

        when(proveedorDAO.findById(proveedorId)).thenReturn(proveedor);
        when(proveedorDAO.modificar(any(Proveedor.class))).thenReturn(actualizacion);

        // Act
        Response response = proveedorResource.update(proveedorId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        Proveedor resultado = (Proveedor) response.getEntity();
        assertEquals("Distribuidora Actualizada S.A.", resultado.getNombre());
        assertEquals("0614-123456-999-9", resultado.getNit());
        assertEquals(false, resultado.getActivo());

        verify(proveedorDAO).findById(proveedorId);
        verify(proveedorDAO).modificar(any(Proveedor.class));
    }

    @Test
    void testUpdate_IdNulo_DebeRetornarUnprocessableEntity() {
        // Arrange
        Proveedor actualizacion = new Proveedor();

        // Act
        Response response = proveedorResource.update(null, actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id and entity must be valid", response.getHeaderString("Missing-parameter"));
        verify(proveedorDAO, never()).findById(any());
        verify(proveedorDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_EntityNull_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = proveedorResource.update(proveedorId, null);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id and entity must be valid", response.getHeaderString("Missing-parameter"));
        verify(proveedorDAO, never()).findById(any());
        verify(proveedorDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_IdCero_DebeRetornarUnprocessableEntity() {
        // Arrange
        Proveedor actualizacion = new Proveedor();

        // Act
        Response response = proveedorResource.update(0, actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id and entity must be valid", response.getHeaderString("Missing-parameter"));
        verify(proveedorDAO, never()).findById(any());
        verify(proveedorDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_IdNegativo_DebeRetornarUnprocessableEntity() {
        // Arrange
        Proveedor actualizacion = new Proveedor();

        // Act
        Response response = proveedorResource.update(-1, actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertEquals("id and entity must be valid", response.getHeaderString("Missing-parameter"));
        verify(proveedorDAO, never()).findById(any());
        verify(proveedorDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ProveedorNoExiste_DebeRetornarNotFound() {
        // Arrange
        Proveedor actualizacion = new Proveedor();
        Integer idNoExistente = 999;
        when(proveedorDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = proveedorResource.update(idNoExistente, actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id 999 not found", response.getHeaderString("Record-not-found"));
        verify(proveedorDAO).findById(idNoExistente);
        verify(proveedorDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        Proveedor actualizacion = new Proveedor();
        when(proveedorDAO.findById(proveedorId)).thenReturn(proveedor);
        when(proveedorDAO.modificar(any()))
                .thenThrow(new RuntimeException("Error al actualizar"));

        // Act
        Response response = proveedorResource.update(proveedorId, actualizacion);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(proveedorDAO).findById(proveedorId);
        verify(proveedorDAO).modificar(any(Proveedor.class));
    }

    @Test
    void testUpdate_AsignaIdCorrecto_DebeUsarIdDelPath() {
        // Arrange
        Proveedor actualizacion = new Proveedor();
        actualizacion.setNombre("Nombre Actualizado");

        when(proveedorDAO.findById(proveedorId)).thenReturn(proveedor);
        when(proveedorDAO.modificar(any(Proveedor.class))).thenAnswer(invocation -> {
            Proveedor p = invocation.getArgument(0);
            // Verificar que el ID se asignó correctamente desde el path
            assertEquals(proveedorId, p.getId());
            return p;
        });

        // Act
        Response response = proveedorResource.update(proveedorId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(proveedorDAO).modificar(any(Proveedor.class));
    }

    @Test
    void testUpdate_ActualizacionParcial_DebeConservarCamposNoModificados() {
        // Arrange
        Proveedor actualizacion = new Proveedor();
        actualizacion.setNombre("Solo nombre actualizado");
        // No se establecen otros campos

        Proveedor proveedorActualizado = new Proveedor();
        proveedorActualizado.setId(proveedorId);
        proveedorActualizado.setNombre("Solo nombre actualizado");
        proveedorActualizado.setRazonSocial("Distribuidora El Salvador Sociedad Anónima"); // Mantiene valor original
        proveedorActualizado.setNit("0614-123456-101-2"); // Mantiene valor original
        proveedorActualizado.setActivo(true); // Mantiene valor original

        when(proveedorDAO.findById(proveedorId)).thenReturn(proveedor);
        when(proveedorDAO.modificar(any(Proveedor.class))).thenReturn(proveedorActualizado);

        // Act
        Response response = proveedorResource.update(proveedorId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(proveedorDAO).modificar(any(Proveedor.class));
    }
}