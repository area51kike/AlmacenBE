package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ClienteDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Cliente;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests para ClienteResource
 * Verifica el comportamiento de todos los endpoints REST
 */
class ClienteResourceTest {

    @Mock
    private ClienteDAO clienteDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    private ClienteResource clienteResource;
    private Cliente cliente;
    private UUID clienteId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clienteResource = new ClienteResource();

        // Inyección manual del DAO
        clienteResource.clienteDAO = clienteDAO;

        // Datos de prueba
        clienteId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNombre("Juan Pérez");
        cliente.setDui("123456789");
        cliente.setNit("0614-271300-123-4");
        cliente.setActivo(true);
    }

    // ==================== TESTS DE findRange ====================

    @Test
    void testFindRange_ParametrosValidos_DebeRetornarOkConHeader() {
        // Arrange
        List<Cliente> clientes = new ArrayList<>();
        clientes.add(cliente);

        Cliente cliente2 = new Cliente();
        cliente2.setId(UUID.fromString("223e4567-e89b-12d3-a456-426614174000"));
        cliente2.setNombre("María García");
        clientes.add(cliente2);

        when(clienteDAO.findRange(0, 50)).thenReturn(clientes);
        when(clienteDAO.count()).thenReturn(100L);

        // Act
        Response response = clienteResource.findRange(0, 50);

        // Assert
        assertEquals(200, response.getStatus(), "Debe retornar OK");
        assertNotNull(response.getEntity());

        // CORRECCIÓN: getHeaderString() retorna String, no Long
        assertEquals("100", response.getHeaderString("Total-records"));

        @SuppressWarnings("unchecked")
        List<Cliente> resultado = (List<Cliente>) response.getEntity();
        assertEquals(2, resultado.size());

        verify(clienteDAO).findRange(0, 50);
        verify(clienteDAO).count();
    }
    @Test
    void testFindRange_FirstNegativo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = clienteResource.findRange(-1, 50);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(clienteDAO, never()).findRange(anyInt(), anyInt());
        verify(clienteDAO, never()).count();
    }

    @Test
    void testFindRange_MaxMayor100_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = clienteResource.findRange(0, 101);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(clienteDAO, never()).findRange(anyInt(), anyInt());
        verify(clienteDAO, never()).count();
    }

    @Test
    void testFindRange_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(clienteDAO.findRange(0, 50))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        Response response = clienteResource.findRange(0, 50);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(clienteDAO).findRange(0, 50);
    }

    // ==================== TESTS DE findById ====================

    @Test
    void testFindById_IdValidoClienteExiste_DebeRetornarOk() {
        // Arrange
        when(clienteDAO.findById(clienteId)).thenReturn(cliente);

        // Act
        Response response = clienteResource.findById(clienteId.toString());

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        Cliente resultado = (Cliente) response.getEntity();
        assertEquals(clienteId, resultado.getId());
        assertEquals("Juan Pérez", resultado.getNombre());

        verify(clienteDAO).findById(clienteId);
    }

    @Test
    void testFindById_IdNulo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = clienteResource.findById(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(clienteDAO, never()).findById(any());
    }

    @Test
    void testFindById_IdVacio_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = clienteResource.findById("   ");

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(clienteDAO, never()).findById(any());
    }

    @Test
    void testFindById_UUIDInvalido_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = clienteResource.findById("uuid-invalido");

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(clienteDAO, never()).findById(any());
    }

    @Test
    void testFindById_ClienteNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(clienteDAO.findById(clienteId)).thenReturn(null);

        // Act
        Response response = clienteResource.findById(clienteId.toString());

        // Assert
        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Record-not-found"));
        verify(clienteDAO).findById(clienteId);
    }

    @Test
    void testFindById_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(clienteDAO.findById(clienteId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = clienteResource.findById(clienteId.toString());

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
    }

    // ==================== TESTS DE delete ====================

    @Test
    void testDelete_IdValidoClienteExiste_DebeRetornarNoContent() {
        // Arrange
        when(clienteDAO.findById(clienteId)).thenReturn(cliente);
        doNothing().when(clienteDAO).eliminar(cliente);

        // Act
        Response response = clienteResource.delete(clienteId.toString());

        // Assert
        assertEquals(204, response.getStatus(), "Debe retornar NO_CONTENT");
        verify(clienteDAO).findById(clienteId);
        verify(clienteDAO).eliminar(cliente);
    }

    @Test
    void testDelete_IdNulo_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = clienteResource.delete(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(clienteDAO, never()).findById(any());
        verify(clienteDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_IdVacio_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = clienteResource.delete("   ");

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(clienteDAO, never()).findById(any());
        verify(clienteDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_UUIDInvalido_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = clienteResource.delete("uuid-invalido");

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(clienteDAO, never()).findById(any());
        verify(clienteDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_ClienteNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(clienteDAO.findById(clienteId)).thenReturn(null);

        // Act
        Response response = clienteResource.delete(clienteId.toString());

        // Assert
        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Record-not-found"));
        verify(clienteDAO).findById(clienteId);
        verify(clienteDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(clienteDAO.findById(clienteId)).thenReturn(cliente);
        doThrow(new RuntimeException("Error al eliminar"))
                .when(clienteDAO).eliminar(any());

        // Act
        Response response = clienteResource.delete(clienteId.toString());

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(clienteDAO).findById(clienteId);
        verify(clienteDAO).eliminar(cliente);
    }

    // ==================== TESTS DE create ====================

    @Test
    void testCreate_ClienteValido_DebeRetornarCreated() throws Exception {
        // Arrange
        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setNombre("Carlos López");
        nuevoCliente.setDui("987654321");
        nuevoCliente.setActivo(true);

        UUID nuevoId = UUID.fromString("323e4567-e89b-12d3-a456-426614174000");
        URI locationUri = new URI("http://localhost:8080/api/cliente/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);

        // Simular que después de crear, se asigna un ID
        doAnswer(invocation -> {
            Cliente c = invocation.getArgument(0);
            c.setId(nuevoId);
            return null;
        }).when(clienteDAO).crear(any(Cliente.class));

        // Act
        Response response = clienteResource.create(nuevoCliente, uriInfo);

        // Assert
        assertEquals(201, response.getStatus(), "Debe retornar CREATED");
        assertNotNull(response.getLocation());
        assertEquals(locationUri, response.getLocation());

        verify(clienteDAO).crear(any(Cliente.class));
    }

    @Test
    void testCreate_EntityNull_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = clienteResource.create(null, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(clienteDAO, never()).crear(any());
    }

    @Test
    void testCreate_ClienteConIdNoNull_DebeRetornarUnprocessableEntity() {
        // Arrange
        Cliente clienteConId = new Cliente();
        clienteConId.setId(clienteId);
        clienteConId.setNombre("Cliente con ID");

        // Act
        Response response = clienteResource.create(clienteConId, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(clienteDAO, never()).crear(any());
    }

    @Test
    void testCreate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setNombre("Cliente Nuevo");

        doThrow(new RuntimeException("Error al guardar"))
                .when(clienteDAO).crear(any());

        // Act
        Response response = clienteResource.create(nuevoCliente, uriInfo);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(clienteDAO).crear(any(Cliente.class));
    }

    // ==================== TESTS DE update ====================

    @Test
    void testUpdate_DatosValidos_DebeRetornarOk() {
        // Arrange
        Cliente actualizacion = new Cliente();
        actualizacion.setNombre("Juan Pérez Actualizado");
        actualizacion.setDui("123456788");
        actualizacion.setNit("0614-271300-123-5");
        actualizacion.setActivo(false);

        when(clienteDAO.findById(clienteId)).thenReturn(cliente);
        when(clienteDAO.modificar(any(Cliente.class))).thenReturn(actualizacion);

        // Act
        Response response = clienteResource.update(clienteId.toString(), actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        Cliente resultado = (Cliente) response.getEntity();
        assertEquals("Juan Pérez Actualizado", resultado.getNombre());
        assertEquals("123456788", resultado.getDui());
        assertEquals(false, resultado.getActivo());

        verify(clienteDAO).findById(clienteId);
        verify(clienteDAO).modificar(any(Cliente.class));
    }

    @Test
    void testUpdate_IdNulo_DebeRetornarUnprocessableEntity() {
        // Arrange
        Cliente actualizacion = new Cliente();

        // Act
        Response response = clienteResource.update(null, actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(clienteDAO, never()).findById(any());
        verify(clienteDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_EntityNull_DebeRetornarUnprocessableEntity() {
        // Act
        Response response = clienteResource.update(clienteId.toString(), null);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(clienteDAO, never()).findById(any());
        verify(clienteDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_IdVacio_DebeRetornarUnprocessableEntity() {
        // Arrange
        Cliente actualizacion = new Cliente();

        // Act
        Response response = clienteResource.update("   ", actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(clienteDAO, never()).findById(any());
        verify(clienteDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_UUIDInvalido_DebeRetornarUnprocessableEntity() {
        // Arrange
        Cliente actualizacion = new Cliente();

        // Act
        Response response = clienteResource.update("uuid-invalido", actualizacion);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(clienteDAO, never()).findById(any());
        verify(clienteDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ClienteNoExiste_DebeRetornarNotFound() {
        // Arrange
        Cliente actualizacion = new Cliente();
        when(clienteDAO.findById(clienteId)).thenReturn(null);

        // Act
        Response response = clienteResource.update(clienteId.toString(), actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Record-not-found"));
        verify(clienteDAO).findById(clienteId);
        verify(clienteDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        Cliente actualizacion = new Cliente();
        when(clienteDAO.findById(clienteId)).thenReturn(cliente);
        when(clienteDAO.modificar(any()))
                .thenThrow(new RuntimeException("Error al actualizar"));

        // Act
        Response response = clienteResource.update(clienteId.toString(), actualizacion);

        // Assert
        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
        verify(clienteDAO).findById(clienteId);
        verify(clienteDAO).modificar(any(Cliente.class));
    }

    @Test
    void testUpdate_AsignaIdCorrecto_DebeUsarIdDelPath() {
        // Arrange
        Cliente actualizacion = new Cliente();
        actualizacion.setNombre("Nombre Actualizado");

        when(clienteDAO.findById(clienteId)).thenReturn(cliente);
        when(clienteDAO.modificar(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente c = invocation.getArgument(0);
            // Verificar que el ID se asignó correctamente
            assertEquals(clienteId, c.getId());
            return c;
        });

        // Act
        Response response = clienteResource.update(clienteId.toString(), actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(clienteDAO).modificar(any(Cliente.class));
    }
}