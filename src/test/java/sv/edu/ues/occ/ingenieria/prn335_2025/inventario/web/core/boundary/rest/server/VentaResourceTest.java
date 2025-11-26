package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ClienteDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Cliente;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Venta;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para VentaResource
 * Verifica el comportamiento de todos los endpoints REST para ventas
 */
class VentaResourceTest {

    @Mock
    private VentaDAO ventaDAO;

    @Mock
    private ClienteDAO clienteDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    private VentaResource ventaResource;
    private Venta venta;
    private Cliente cliente;
    private UUID clienteId;
    private UUID ventaId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ventaResource = new VentaResource();

        // Inyección manual de los DAOs
        ventaResource.ventaDAO = ventaDAO;
        ventaResource.clienteDAO = clienteDAO;

        // Datos de prueba
        clienteId = UUID.randomUUID();
        ventaId = UUID.randomUUID();

        // Configurar Cliente
        cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNombre("Juan Pérez");
        cliente.setDui("12345678-9");
        cliente.setNit("0614-010789-123-4");
        cliente.setActivo(true);

        // Configurar Venta
        venta = new Venta();
        venta.setId(ventaId);
        venta.setIdCliente(cliente);
        venta.setFecha(OffsetDateTime.now());
        venta.setEstado("COMPLETADA");
        venta.setObservaciones("Venta realizada con éxito");
    }

    // ==================== TESTS DE getVentasproCliente ====================

    @Test
    void testGetVentasproCliente_IdClienteValido_DebeRetornarOk() {
        // Arrange
        List<Venta> ventas = new ArrayList<>();
        ventas.add(venta);

        Venta venta2 = new Venta();
        venta2.setId(UUID.randomUUID());
        venta2.setIdCliente(cliente);
        venta2.setEstado("PENDIENTE");
        ventas.add(venta2);

        when(clienteDAO.findById(clienteId)).thenReturn(cliente);
        when(ventaDAO.buscarPorCliente(clienteId)).thenReturn(ventas);

        // Act
        Response response = ventaResource.getVentasproCliente(clienteId);

        // Assert
        assertEquals(200, response.getStatus(), "Debe retornar OK");
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Venta> resultado = (List<Venta>) response.getEntity();
        assertEquals(2, resultado.size());

        verify(clienteDAO).findById(clienteId);
        verify(ventaDAO).buscarPorCliente(clienteId);
    }

    @Test
    void testGetVentasproCliente_IdClienteNulo_DebeRetornarBadRequest() {
        // Act
        Response response = ventaResource.getVentasproCliente(null);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idCliente no debe ser nulo", response.getHeaderString("Error"));
        verify(clienteDAO, never()).findById(any());
        verify(ventaDAO, never()).buscarPorCliente(any());
    }

    @Test
    void testGetVentasproCliente_ClienteNoExiste_DebeRetornarNotFound() {
        // Arrange
        UUID idNoExistente = UUID.randomUUID();
        when(clienteDAO.findById(idNoExistente)).thenReturn(null);

        // Act
        Response response = ventaResource.getVentasproCliente(idNoExistente);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Cliente with id " + idNoExistente + " not found", response.getHeaderString("Not-Found"));
        verify(clienteDAO).findById(idNoExistente);
        verify(ventaDAO, never()).buscarPorCliente(any());
    }

    @Test
    void testGetVentasproCliente_ListaVacia_DebeRetornarOkConListaVacia() {
        // Arrange
        when(clienteDAO.findById(clienteId)).thenReturn(cliente);
        when(ventaDAO.buscarPorCliente(clienteId)).thenReturn(List.of());

        // Act
        Response response = ventaResource.getVentasproCliente(clienteId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Venta> resultado = (List<Venta>) response.getEntity();
        assertTrue(resultado.isEmpty());
    }

    @Test
    void testGetVentasproCliente_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(clienteDAO.findById(clienteId)).thenReturn(cliente);
        when(ventaDAO.buscarPorCliente(clienteId))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        Response response = ventaResource.getVentasproCliente(clienteId);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getHeaderString("Server-Exception").contains("Error:"));
        verify(clienteDAO).findById(clienteId);
        verify(ventaDAO).buscarPorCliente(clienteId);
    }

    // ==================== TESTS DE findById ====================

    @Test
    void testFindById_IdsValidosVentaExiste_DebeRetornarOk() {
        // Arrange
        when(ventaDAO.findById(ventaId)).thenReturn(venta);

        // Act
        Response response = ventaResource.findById(clienteId, ventaId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        Venta resultado = (Venta) response.getEntity();
        assertEquals(ventaId, resultado.getId());
        assertEquals("COMPLETADA", resultado.getEstado());
        assertEquals("Venta realizada con éxito", resultado.getObservaciones());

        verify(ventaDAO).findById(ventaId);
    }

    @Test
    void testFindById_IdVentaNulo_DebeRetornarBadRequest() {
        // Act
        Response response = ventaResource.findById(clienteId, null);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idVenta no debe ser nulo", response.getHeaderString("Error"));
        verify(ventaDAO, never()).findById(any());
    }

    @Test
    void testFindById_VentaNoExiste_DebeRetornarNotFound() {
        // Arrange
        UUID idVentaNoExistente = UUID.randomUUID();
        when(ventaDAO.findById(idVentaNoExistente)).thenReturn(null);

        // Act
        Response response = ventaResource.findById(clienteId, idVentaNoExistente);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + idVentaNoExistente + " not found", response.getHeaderString("Not-Found"));
        verify(ventaDAO).findById(idVentaNoExistente);
    }

    @Test
    void testFindById_VentaNoPerteneceAlCliente_DebeRetornarNotFound() {
        // Arrange
        UUID otroClienteId = UUID.randomUUID();
        Cliente otroCliente = new Cliente();
        otroCliente.setId(otroClienteId);

        Venta ventaOtroCliente = new Venta();
        ventaOtroCliente.setId(ventaId);
        ventaOtroCliente.setIdCliente(otroCliente);

        when(ventaDAO.findById(ventaId)).thenReturn(ventaOtroCliente);

        // Act
        Response response = ventaResource.findById(clienteId, ventaId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Venta no pertenece a este cliente", response.getHeaderString("Not-Found"));
        verify(ventaDAO).findById(ventaId);
    }

    @Test
    void testFindById_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(ventaDAO.findById(ventaId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = ventaResource.findById(clienteId, ventaId);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getHeaderString("Server-Exception").contains("Cannot access db"));
        verify(ventaDAO).findById(ventaId);
    }

    // ==================== TESTS DE create ====================

    @Test
    void testCreate_VentaValida_DebeRetornarCreated() throws Exception {
        // Arrange
        Venta nuevaVenta = new Venta();
        nuevaVenta.setFecha(OffsetDateTime.now());
        nuevaVenta.setEstado("PENDIENTE");
        nuevaVenta.setObservaciones("Nueva venta");
        // ID debe ser null para crear

        UUID nuevoId = UUID.randomUUID();
        URI locationUri = new URI("http://localhost:8080/api/cliente/" + clienteId + "/venta/" + nuevoId);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(locationUri);
        when(clienteDAO.findById(clienteId)).thenReturn(cliente);

        doAnswer(invocation -> {
            Venta v = invocation.getArgument(0);
            v.setId(nuevoId);
            return null;
        }).when(ventaDAO).crear(any(Venta.class));

        // Act
        Response response = ventaResource.create(clienteId, nuevaVenta, uriInfo);

        // Assert
        assertEquals(201, response.getStatus(), "Debe retornar CREATED");
        assertNotNull(response.getLocation());
        assertEquals(locationUri, response.getLocation());
        assertNotNull(response.getEntity());

        verify(clienteDAO).findById(clienteId);
        verify(ventaDAO).crear(any(Venta.class));
    }

    @Test
    void testCreate_IdClienteNulo_DebeRetornarBadRequest() {
        // Arrange
        Venta nuevaVenta = new Venta();

        // Act
        Response response = ventaResource.create(null, nuevaVenta, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idCliente no debe ser nulo", response.getHeaderString("Error"));
        verify(clienteDAO, never()).findById(any());
        verify(ventaDAO, never()).crear(any());
    }

    @Test
    void testCreate_EntityNull_DebeRetornarBadRequest() {
        // Act
        Response response = ventaResource.create(clienteId, null, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("Request body is required", response.getHeaderString("Error"));
        verify(clienteDAO, never()).findById(any());
        verify(ventaDAO, never()).crear(any());
    }

    @Test
    void testCreate_VentaConIdNoNull_DebeRetornarBadRequest() {
        // Arrange
        Venta ventaConId = new Venta();
        ventaConId.setId(ventaId); // ID no null

        // Act
        Response response = ventaResource.create(clienteId, ventaConId, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idVenta must be null for new records", response.getHeaderString("Error"));
        verify(clienteDAO, never()).findById(any());
        verify(ventaDAO, never()).crear(any());
    }

    @Test
    void testCreate_ClienteNoExiste_DebeRetornarNotFound() {
        // Arrange
        Venta nuevaVenta = new Venta();
        UUID idClienteNoExistente = UUID.randomUUID();
        when(clienteDAO.findById(idClienteNoExistente)).thenReturn(null);

        // Act
        Response response = ventaResource.create(idClienteNoExistente, nuevaVenta, uriInfo);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Cliente with id " + idClienteNoExistente + " not found", response.getHeaderString("Not-Found"));
        verify(clienteDAO).findById(idClienteNoExistente);
        verify(ventaDAO, never()).crear(any());
    }

    @Test
    void testCreate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        Venta nuevaVenta = new Venta();
        when(clienteDAO.findById(clienteId)).thenReturn(cliente);
        doThrow(new RuntimeException("Error al guardar"))
                .when(ventaDAO).crear(any());

        // Act
        Response response = ventaResource.create(clienteId, nuevaVenta, uriInfo);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getHeaderString("Server-Exception").contains("Error creating"));
        verify(clienteDAO).findById(clienteId);
        verify(ventaDAO).crear(any(Venta.class));
    }

    // ==================== TESTS DE update ====================

    @Test
    void testUpdate_DatosValidos_DebeRetornarOk() {
        // Arrange
        Venta actualizacion = new Venta();
        actualizacion.setObservaciones("Observaciones actualizadas");
        actualizacion.setEstado("CANCELADA");
        actualizacion.setFecha(OffsetDateTime.now().plusDays(1));

        when(ventaDAO.findById(ventaId)).thenReturn(venta);
        when(ventaDAO.modificar(any(Venta.class))).thenReturn(venta);

        // Act
        Response response = ventaResource.update(clienteId, ventaId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        verify(ventaDAO).findById(ventaId);
        verify(ventaDAO).modificar(any(Venta.class));
    }

    @Test
    void testUpdate_ActualizarCliente_DebeActualizarCorrectamente() {
        // Arrange
        UUID nuevoClienteId = UUID.randomUUID();
        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setId(nuevoClienteId);

        Venta actualizacion = new Venta();
        actualizacion.setIdCliente(nuevoCliente);
        actualizacion.setObservaciones("Cambio de cliente");

        when(ventaDAO.findById(ventaId)).thenReturn(venta);
        when(clienteDAO.findById(nuevoClienteId)).thenReturn(nuevoCliente);
        when(ventaDAO.modificar(any(Venta.class))).thenReturn(venta);

        // Act
        Response response = ventaResource.update(clienteId, ventaId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(clienteDAO).findById(nuevoClienteId);
        verify(ventaDAO).modificar(any(Venta.class));
    }

    @Test
    void testUpdate_IdsNulos_DebeRetornarBadRequest() {
        // Arrange
        Venta actualizacion = new Venta();

        // Act
        Response response = ventaResource.update(null, null, actualizacion);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("IDs cannot be null", response.getHeaderString("Error"));
        verify(ventaDAO, never()).findById(any());
        verify(ventaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_EntityNull_DebeRetornarBadRequest() {
        // Act
        Response response = ventaResource.update(clienteId, ventaId, null);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("Request body is required", response.getHeaderString("Error"));
        verify(ventaDAO, never()).findById(any());
        verify(ventaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_VentaNoExiste_DebeRetornarNotFound() {
        // Arrange
        Venta actualizacion = new Venta();
        UUID idVentaNoExistente = UUID.randomUUID();
        when(ventaDAO.findById(idVentaNoExistente)).thenReturn(null);

        // Act
        Response response = ventaResource.update(clienteId, idVentaNoExistente, actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + idVentaNoExistente + " not found", response.getHeaderString("Not-Found"));
        verify(ventaDAO).findById(idVentaNoExistente);
        verify(ventaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_VentaNoPerteneceAlCliente_DebeRetornarNotFound() {
        // Arrange
        UUID otroClienteId = UUID.randomUUID();
        Cliente otroCliente = new Cliente();
        otroCliente.setId(otroClienteId);

        Venta ventaOtroCliente = new Venta();
        ventaOtroCliente.setId(ventaId);
        ventaOtroCliente.setIdCliente(otroCliente);

        Venta actualizacion = new Venta();

        when(ventaDAO.findById(ventaId)).thenReturn(ventaOtroCliente);

        // Act
        Response response = ventaResource.update(clienteId, ventaId, actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Venta no pertenece al cliente", response.getHeaderString("Not-Found"));
        verify(ventaDAO).findById(ventaId);
        verify(ventaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_NuevoClienteNoExiste_DebeRetornarNotFound() {
        // Arrange
        UUID idClienteNoExistente = UUID.randomUUID();
        Cliente clienteNoExistente = new Cliente();
        clienteNoExistente.setId(idClienteNoExistente);

        Venta actualizacion = new Venta();
        actualizacion.setIdCliente(clienteNoExistente);

        when(ventaDAO.findById(ventaId)).thenReturn(venta);
        when(clienteDAO.findById(idClienteNoExistente)).thenReturn(null);

        // Act
        Response response = ventaResource.update(clienteId, ventaId, actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Cliente with id " + idClienteNoExistente + " not found", response.getHeaderString("Not-Found"));
        verify(clienteDAO).findById(idClienteNoExistente);
        verify(ventaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ClienteBodySinId_DebeRetornarBadRequest() {
        // Arrange
        Cliente clienteSinId = new Cliente();
        // No establecer ID

        Venta actualizacion = new Venta();
        actualizacion.setIdCliente(clienteSinId);

        when(ventaDAO.findById(ventaId)).thenReturn(venta);

        // Act
        Response response = ventaResource.update(clienteId, ventaId, actualizacion);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idCliente.id cannot be null", response.getHeaderString("Error"));
        verify(ventaDAO).findById(ventaId);
        verify(clienteDAO, never()).findById(any());
        verify(ventaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        Venta actualizacion = new Venta();
        when(ventaDAO.findById(ventaId)).thenReturn(venta);
        when(ventaDAO.modificar(any()))
                .thenThrow(new RuntimeException("Error al actualizar"));

        // Act
        Response response = ventaResource.update(clienteId, ventaId, actualizacion);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getHeaderString("Server-Exception").contains("Error updating"));
        verify(ventaDAO).findById(ventaId);
        verify(ventaDAO).modificar(any(Venta.class));
    }

    // ==================== TESTS DE delete ====================

    @Test
    void testDelete_IdsValidos_DebeRetornarNoContent() {
        // Arrange
        when(ventaDAO.findById(ventaId)).thenReturn(venta);
        doNothing().when(ventaDAO).eliminar(venta);

        // Act
        Response response = ventaResource.delete(clienteId, ventaId);

        // Assert
        assertEquals(204, response.getStatus(), "Debe retornar NO_CONTENT");
        verify(ventaDAO).findById(ventaId);
        verify(ventaDAO).eliminar(venta);
    }

    @Test
    void testDelete_IdVentaNulo_DebeRetornarBadRequest() {
        // Act
        Response response = ventaResource.delete(clienteId, null);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("idVenta no debe ser nulo", response.getHeaderString("Error"));
        verify(ventaDAO, never()).findById(any());
        verify(ventaDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_VentaNoExiste_DebeRetornarNotFound() {
        // Arrange
        UUID idVentaNoExistente = UUID.randomUUID();
        when(ventaDAO.findById(idVentaNoExistente)).thenReturn(null);

        // Act
        Response response = ventaResource.delete(clienteId, idVentaNoExistente);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Record with id " + idVentaNoExistente + " not found", response.getHeaderString("Not-Found"));
        verify(ventaDAO).findById(idVentaNoExistente);
        verify(ventaDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_VentaNoPerteneceAlCliente_DebeRetornarNotFound() {
        // Arrange
        UUID otroClienteId = UUID.randomUUID();
        Cliente otroCliente = new Cliente();
        otroCliente.setId(otroClienteId);

        Venta ventaOtroCliente = new Venta();
        ventaOtroCliente.setId(ventaId);
        ventaOtroCliente.setIdCliente(otroCliente);

        when(ventaDAO.findById(ventaId)).thenReturn(ventaOtroCliente);

        // Act
        Response response = ventaResource.delete(clienteId, ventaId);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Venta no pertenece a este cliente", response.getHeaderString("Not-Found"));
        verify(ventaDAO).findById(ventaId);
        verify(ventaDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(ventaDAO.findById(ventaId)).thenReturn(venta);
        doThrow(new RuntimeException("Error al eliminar"))
                .when(ventaDAO).eliminar(any());

        // Act
        Response response = ventaResource.delete(clienteId, ventaId);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getHeaderString("Server-Exception").contains("Cannot delete record"));
        verify(ventaDAO).findById(ventaId);
        verify(ventaDAO).eliminar(venta);
    }

    @Test
    void testUpdate_ActualizacionParcial_DebeConservarCamposNoModificados() {
        // Arrange
        Venta actualizacion = new Venta();
        actualizacion.setObservaciones("Solo actualizar observaciones");
        // No se establecen otros campos

        when(ventaDAO.findById(ventaId)).thenReturn(venta);
        when(ventaDAO.modificar(any(Venta.class))).thenReturn(venta);

        // Act
        Response response = ventaResource.update(clienteId, ventaId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(ventaDAO).modificar(any(Venta.class));
    }
}