package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProveedorDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Proveedor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para CompraResource
 * Verifica el comportamiento de todos los endpoints REST para compras de proveedores
 */
class CompraResourceTest {

    @Mock
    private CompraDAO compraDAO;

    @Mock
    private ProveedorDAO proveedorDAO;

    private CompraResource compraResource;
    private Proveedor proveedor;
    private Compra compra;
    private Integer proveedorId;
    private Long compraId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        compraResource = new CompraResource();

        // Inyección manual de los DAOs (sin private en CompraResource)
        compraResource.compraDAO = compraDAO;
        compraResource.proveedorDAO = proveedorDAO;

        // Datos de prueba
        proveedorId = 1;
        compraId = 100L;

        proveedor = new Proveedor();
        proveedor.setId(proveedorId);
        proveedor.setNombre("Proveedor ABC");
        proveedor.setActivo(true);

        compra = new Compra();
        compra.setId(compraId);
        compra.setIdProveedor(proveedorId);
        compra.setFecha(OffsetDateTime.now());
        compra.setEstado("PENDIENTE");
        compra.setObservaciones("Compra de prueba");
    }

    // ==================== TESTS DE getComprasByProveedor ====================

    @Test
    void testGetComprasByProveedor_ProveedorExisteConCompras_DebeRetornarOk() {
        // Arrange
        Compra compra2 = new Compra();
        compra2.setId(101L);
        compra2.setIdProveedor(proveedorId);

        List<Compra> todasCompras = new ArrayList<>();
        todasCompras.add(compra);
        todasCompras.add(compra2);

        when(proveedorDAO.findById(proveedorId)).thenReturn(proveedor);
        when(compraDAO.findAll()).thenReturn(todasCompras);

        // Act
        Response response = compraResource.getComprasByProveedor(proveedorId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Compra> resultado = (List<Compra>) response.getEntity();
        assertEquals(2, resultado.size());

        verify(proveedorDAO).findById(proveedorId);
        verify(compraDAO).findAll();
    }

    @Test
    void testGetComprasByProveedor_ProveedorNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(proveedorDAO.findById(proveedorId)).thenReturn(null);

        // Act
        Response response = compraResource.getComprasByProveedor(proveedorId);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Proveedor no encontrado"));
        verify(proveedorDAO).findById(proveedorId);
        verify(compraDAO, never()).findAll();
    }

    @Test
    void testGetComprasByProveedor_ProveedorExisteSinCompras_DebeRetornarOkConListaVacia() {
        // Arrange
        List<Compra> todasCompras = new ArrayList<>();
        // Agregar compra de otro proveedor
        Compra compraOtroProveedor = new Compra();
        compraOtroProveedor.setIdProveedor(999);
        todasCompras.add(compraOtroProveedor);

        when(proveedorDAO.findById(proveedorId)).thenReturn(proveedor);
        when(compraDAO.findAll()).thenReturn(todasCompras);

        // Act
        Response response = compraResource.getComprasByProveedor(proveedorId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Compra> resultado = (List<Compra>) response.getEntity();
        assertTrue(resultado.isEmpty());

        verify(proveedorDAO).findById(proveedorId);
        verify(compraDAO).findAll();
    }

    @Test
    void testGetComprasByProveedor_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(proveedorDAO.findById(proveedorId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = compraResource.getComprasByProveedor(proveedorId);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al obtener compras"));
        verify(proveedorDAO).findById(proveedorId);
    }

    // ==================== TESTS DE getCompraById ====================

    @Test
    void testGetCompraById_CompraExisteYCoincideProveedor_DebeRetornarOk() {
        // Arrange
        when(compraDAO.findById(compraId)).thenReturn(compra);

        // Act
        Response response = compraResource.getCompraById(proveedorId, compraId);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        Compra resultado = (Compra) response.getEntity();
        assertEquals(compraId, resultado.getId());

        verify(compraDAO).findById(compraId);
    }

    @Test
    void testGetCompraById_CompraNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(compraDAO.findById(compraId)).thenReturn(null);

        // Act
        Response response = compraResource.getCompraById(proveedorId, compraId);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Compra no encontrada"));
        verify(compraDAO).findById(compraId);
    }

    @Test
    void testGetCompraById_CompraPerteneceOtroProveedor_DebeRetornarBadRequest() {
        // Arrange
        compra.setIdProveedor(999); // Diferente al proveedor del path
        when(compraDAO.findById(compraId)).thenReturn(compra);

        // Act
        Response response = compraResource.getCompraById(proveedorId, compraId);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece al proveedor"));
        verify(compraDAO).findById(compraId);
    }

    @Test
    void testGetCompraById_CompraConProveedorNull_DebeRetornarInternalServerError() {
        // Arrange
        compra.setIdProveedor(null);
        when(compraDAO.findById(compraId)).thenReturn(compra);

        // Act
        Response response = compraResource.getCompraById(proveedorId, compraId);

        // Assert
        assertEquals(500, response.getStatus());  // ✅ CORREGIDO
        assertTrue(response.getEntity().toString().contains("Error al obtener compra"));
        verify(compraDAO).findById(compraId);
    }

    // ==================== TESTS DE createCompra ====================

    @Test
    void testCreateCompra_DatosValidos_DebeRetornarCreated() {
        // Arrange
        Compra nuevaCompra = new Compra();
        nuevaCompra.setFecha(OffsetDateTime.now());
        nuevaCompra.setEstado("PENDIENTE");

        when(proveedorDAO.findById(proveedorId)).thenReturn(proveedor);
        doNothing().when(compraDAO).crear(any(Compra.class));

        // Act
        Response response = compraResource.createCompra(proveedorId, nuevaCompra);

        // Assert
        assertEquals(201, response.getStatus());
        assertNotNull(response.getEntity());

        // Verificar que se asignó el proveedor del path
        assertEquals(proveedorId, nuevaCompra.getIdProveedor());

        verify(proveedorDAO).findById(proveedorId);
        verify(compraDAO).crear(any(Compra.class));
    }

    @Test
    void testCreateCompra_ProveedorNoExiste_DebeRetornarNotFound() {
        // Arrange
        Compra nuevaCompra = new Compra();
        when(proveedorDAO.findById(proveedorId)).thenReturn(null);

        // Act
        Response response = compraResource.createCompra(proveedorId, nuevaCompra);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Proveedor no encontrado"));
        verify(proveedorDAO).findById(proveedorId);
        verify(compraDAO, never()).crear(any());
    }

    @Test
    void testCreateCompra_FechaNull_DebeRetornarBadRequest() {
        // Arrange
        Compra nuevaCompra = new Compra();
        nuevaCompra.setFecha(null); // Fecha requerida

        when(proveedorDAO.findById(proveedorId)).thenReturn(proveedor);

        // Act
        Response response = compraResource.createCompra(proveedorId, nuevaCompra);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("fecha es obligatoria"));
        verify(proveedorDAO).findById(proveedorId);
        verify(compraDAO, never()).crear(any());
    }

    @Test
    void testCreateCompra_ConExcepcionEnDAO_DebeRetornarInternalServerError() {
        // Arrange
        Compra nuevaCompra = new Compra();
        nuevaCompra.setFecha(OffsetDateTime.now());

        when(proveedorDAO.findById(proveedorId)).thenReturn(proveedor);
        doThrow(new RuntimeException("Error al guardar"))
                .when(compraDAO).crear(any());

        // Act
        Response response = compraResource.createCompra(proveedorId, nuevaCompra);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al crear compra"));
        verify(proveedorDAO).findById(proveedorId);
        verify(compraDAO).crear(any(Compra.class));
    }

    // ==================== TESTS DE updateCompra ====================

    @Test
    void testUpdateCompra_DatosValidos_DebeRetornarOk() {
        // Arrange
        Compra actualizacion = new Compra();
        actualizacion.setFecha(OffsetDateTime.now().plusDays(1));
        actualizacion.setEstado("PAGADA");
        actualizacion.setObservaciones("Actualizado");

        when(compraDAO.findById(compraId)).thenReturn(compra);
        when(compraDAO.modificar(any(Compra.class))).thenReturn(compra);

        // Act
        Response response = compraResource.updateCompra(proveedorId, compraId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        verify(compraDAO).findById(compraId);
        verify(compraDAO).modificar(any(Compra.class));
    }

    @Test
    void testUpdateCompra_CompraNoExiste_DebeRetornarNotFound() {
        // Arrange
        Compra actualizacion = new Compra();
        when(compraDAO.findById(compraId)).thenReturn(null);

        // Act
        Response response = compraResource.updateCompra(proveedorId, compraId, actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Compra no encontrada"));
        verify(compraDAO).findById(compraId);
        verify(compraDAO, never()).modificar(any());
    }

    @Test
    void testUpdateCompra_CompraPerteneceOtroProveedor_DebeRetornarBadRequest() {
        // Arrange
        compra.setIdProveedor(999); // Diferente proveedor
        Compra actualizacion = new Compra();
        when(compraDAO.findById(compraId)).thenReturn(compra);

        // Act
        Response response = compraResource.updateCompra(proveedorId, compraId, actualizacion);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece al proveedor"));
        verify(compraDAO).findById(compraId);
        verify(compraDAO, never()).modificar(any());
    }

    @Test
    void testUpdateCompra_MantieneIdYProveedor_DebeConservarValoresOriginales() {
        // Arrange
        Compra actualizacion = new Compra();
        actualizacion.setFecha(OffsetDateTime.now().plusDays(1));
        actualizacion.setEstado("PAGADA");

        when(compraDAO.findById(compraId)).thenReturn(compra);
        when(compraDAO.modificar(any(Compra.class))).thenAnswer(invocation -> {
            Compra c = invocation.getArgument(0);
            // Verificar que mantiene ID y proveedor original
            assertEquals(compraId, c.getId());
            assertEquals(proveedorId, c.getIdProveedor());
            return c;
        });

        // Act
        Response response = compraResource.updateCompra(proveedorId, compraId, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        verify(compraDAO).modificar(any(Compra.class));
    }

    // ==================== TESTS DE deleteCompra ====================

    @Test
    void testDeleteCompra_CompraExiste_DebeRetornarNoContent() {
        // Arrange
        when(compraDAO.findById(compraId)).thenReturn(compra);
        doNothing().when(compraDAO).eliminar(compra);

        // Act
        Response response = compraResource.deleteCompra(proveedorId, compraId);

        // Assert
        assertEquals(204, response.getStatus());
        verify(compraDAO).findById(compraId);
        verify(compraDAO).eliminar(compra);
    }

    @Test
    void testDeleteCompra_CompraNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(compraDAO.findById(compraId)).thenReturn(null);

        // Act
        Response response = compraResource.deleteCompra(proveedorId, compraId);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Compra no encontrada"));
        verify(compraDAO).findById(compraId);
        verify(compraDAO, never()).eliminar(any());
    }

    @Test
    void testDeleteCompra_CompraPerteneceOtroProveedor_DebeRetornarBadRequest() {
        // Arrange
        compra.setIdProveedor(999);
        when(compraDAO.findById(compraId)).thenReturn(compra);

        // Act
        Response response = compraResource.deleteCompra(proveedorId, compraId);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece al proveedor"));
        verify(compraDAO).findById(compraId);
        verify(compraDAO, never()).eliminar(any());
    }

    // ==================== TESTS DE countComprasByProveedor ====================

    @Test
    void testCountComprasByProveedor_ProveedorExisteConCompras_DebeRetornarOk() {
        // Arrange
        List<Compra> todasCompras = new ArrayList<>();
        todasCompras.add(compra);

        Compra compraOtroProveedor = new Compra();
        compraOtroProveedor.setIdProveedor(999);
        todasCompras.add(compraOtroProveedor);

        when(proveedorDAO.findById(proveedorId)).thenReturn(proveedor);
        when(compraDAO.findAll()).thenReturn(todasCompras);

        // Act
        Response response = compraResource.countComprasByProveedor(proveedorId);

        // Assert
        assertEquals(200, response.getStatus());
        assertTrue(response.getEntity().toString().contains("\"count\": 1"));

        verify(proveedorDAO).findById(proveedorId);
        verify(compraDAO).findAll();
    }

    @Test
    void testCountComprasByProveedor_ProveedorNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(proveedorDAO.findById(proveedorId)).thenReturn(null);

        // Act
        Response response = compraResource.countComprasByProveedor(proveedorId);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Proveedor no encontrado"));
        verify(proveedorDAO).findById(proveedorId);
        verify(compraDAO, never()).findAll();
    }

    @Test
    void testCountComprasByProveedor_ProveedorExisteSinCompras_DebeRetornarCero() {
        // Arrange
        List<Compra> todasCompras = new ArrayList<>();
        // Solo compras de otros proveedores
        Compra compraOtroProveedor = new Compra();
        compraOtroProveedor.setIdProveedor(999);
        todasCompras.add(compraOtroProveedor);

        when(proveedorDAO.findById(proveedorId)).thenReturn(proveedor);
        when(compraDAO.findAll()).thenReturn(todasCompras);

        // Act
        Response response = compraResource.countComprasByProveedor(proveedorId);

        // Assert
        assertEquals(200, response.getStatus());
        assertTrue(response.getEntity().toString().contains("\"count\": 0"));

        verify(proveedorDAO).findById(proveedorId);
        verify(compraDAO).findAll();
    }
}