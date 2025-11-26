package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.KardexDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.KardexDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Kardex;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.KardexDetalle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para KardexDetalleResource
 * Verifica el comportamiento de todos los endpoints REST para detalles de kardex
 */
class KardexDetalleResourceTest {

    @Mock
    private KardexDetalleDAO kardexDetalleDAO;

    @Mock
    private KardexDAO kardexDAO;

    private KardexDetalleResource kardexDetalleResource;
    private Kardex kardex;
    private KardexDetalle kardexDetalle;
    private UUID kardexId;
    private UUID detalleId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        kardexDetalleResource = new KardexDetalleResource();

        // Inyección manual de los DAOs (sin private en KardexDetalleResource)
        kardexDetalleResource.kardexDetalleDAO = kardexDetalleDAO;
        kardexDetalleResource.kardexDAO = kardexDAO;

        // Datos de prueba
        kardexId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        detalleId = UUID.fromString("223e4567-e89b-12d3-a456-426614174000");

        kardex = new Kardex();
        kardex.setId(kardexId);

        kardexDetalle = new KardexDetalle();
        kardexDetalle.setId(detalleId);
        kardexDetalle.setIdKardex(kardex);
        kardexDetalle.setLote("LOTE-001");
        kardexDetalle.setActivo(true);
    }

    // ==================== TESTS DE getDetallesByKardex ====================

    @Test
    void testGetDetallesByKardex_KardexExisteConDetalles_DebeRetornarOk() {
        // Arrange
        KardexDetalle detalle2 = new KardexDetalle();
        detalle2.setId(UUID.fromString("323e4567-e89b-12d3-a456-426614174000"));
        detalle2.setIdKardex(kardex);

        List<KardexDetalle> todosDetalles = new ArrayList<>();
        todosDetalles.add(kardexDetalle);
        todosDetalles.add(detalle2);

        when(kardexDAO.findById(kardexId)).thenReturn(kardex);
        when(kardexDetalleDAO.findAll()).thenReturn(todosDetalles);

        // Act
        Response response = kardexDetalleResource.getDetallesByKardex(kardexId.toString());

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<KardexDetalle> resultado = (List<KardexDetalle>) response.getEntity();
        assertEquals(2, resultado.size());

        verify(kardexDAO).findById(kardexId);
        verify(kardexDetalleDAO).findAll();
    }

    @Test
    void testGetDetallesByKardex_KardexNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(kardexDAO.findById(kardexId)).thenReturn(null);

        // Act
        Response response = kardexDetalleResource.getDetallesByKardex(kardexId.toString());

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Kardex no encontrado"));
        verify(kardexDAO).findById(kardexId);
        verify(kardexDetalleDAO, never()).findAll();
    }

    @Test
    void testGetDetallesByKardex_UUIDInvalido_DebeRetornarBadRequest() {
        // Act
        Response response = kardexDetalleResource.getDetallesByKardex("uuid-invalido");

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("ID de kardex inválido"));
        verify(kardexDAO, never()).findById(any());
    }

    @Test
    void testGetDetallesByKardex_KardexExisteSinDetalles_DebeRetornarOkConListaVacia() {
        // Arrange
        List<KardexDetalle> todosDetalles = new ArrayList<>();
        // Agregar detalle que NO pertenece a este kardex
        KardexDetalle detalleOtroKardex = new KardexDetalle();
        Kardex otroKardex = new Kardex();
        otroKardex.setId(UUID.fromString("999e4567-e89b-12d3-a456-426614174000"));
        detalleOtroKardex.setIdKardex(otroKardex);
        todosDetalles.add(detalleOtroKardex);

        when(kardexDAO.findById(kardexId)).thenReturn(kardex);
        when(kardexDetalleDAO.findAll()).thenReturn(todosDetalles);

        // Act
        Response response = kardexDetalleResource.getDetallesByKardex(kardexId.toString());

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<KardexDetalle> resultado = (List<KardexDetalle>) response.getEntity();
        assertTrue(resultado.isEmpty());

        verify(kardexDAO).findById(kardexId);
        verify(kardexDetalleDAO).findAll();
    }

    // ==================== TESTS DE getDetalleById ====================

    @Test
    void testGetDetalleById_DetalleExisteYCoincideKardex_DebeRetornarOk() {
        // Arrange
        when(kardexDetalleDAO.findById(detalleId)).thenReturn(kardexDetalle);

        // Act
        Response response = kardexDetalleResource.getDetalleById(kardexId.toString(), detalleId.toString());

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        KardexDetalle resultado = (KardexDetalle) response.getEntity();
        assertEquals(detalleId, resultado.getId());

        verify(kardexDetalleDAO).findById(detalleId);
    }

    @Test
    void testGetDetalleById_UUIDInvalido_DebeRetornarBadRequest() {
        // Act
        Response response = kardexDetalleResource.getDetalleById(kardexId.toString(), "uuid-invalido");

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("ID inválido"));
        verify(kardexDetalleDAO, never()).findById(any());
    }

    @Test
    void testGetDetalleById_DetalleNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(kardexDetalleDAO.findById(detalleId)).thenReturn(null);

        // Act
        Response response = kardexDetalleResource.getDetalleById(kardexId.toString(), detalleId.toString());

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Detalle no encontrado"));
        verify(kardexDetalleDAO).findById(detalleId);
    }

    @Test
    void testGetDetalleById_DetalleSinKardex_DebeRetornarBadRequest() {
        // Arrange
        kardexDetalle.setIdKardex(null);
        when(kardexDetalleDAO.findById(detalleId)).thenReturn(kardexDetalle);

        // Act
        Response response = kardexDetalleResource.getDetalleById(kardexId.toString(), detalleId.toString());

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece al kardex"));
        verify(kardexDetalleDAO).findById(detalleId);
    }

    @Test
    void testGetDetalleById_DetallePerteneceOtroKardex_DebeRetornarBadRequest() {
        // Arrange
        Kardex otroKardex = new Kardex();
        otroKardex.setId(UUID.fromString("999e4567-e89b-12d3-a456-426614174000"));
        kardexDetalle.setIdKardex(otroKardex);
        when(kardexDetalleDAO.findById(detalleId)).thenReturn(kardexDetalle);

        // Act
        Response response = kardexDetalleResource.getDetalleById(kardexId.toString(), detalleId.toString());

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece al kardex"));
        verify(kardexDetalleDAO).findById(detalleId);
    }

    // ==================== TESTS DE getDetallesActivos ====================

    @Test
    void testGetDetallesActivos_KardexExisteConDetallesActivos_DebeRetornarOk() {
        // Arrange
        KardexDetalle detalleInactivo = new KardexDetalle();
        detalleInactivo.setId(UUID.fromString("423e4567-e89b-12d3-a456-426614174000"));
        detalleInactivo.setIdKardex(kardex);
        detalleInactivo.setActivo(false);

        List<KardexDetalle> todosDetalles = new ArrayList<>();
        todosDetalles.add(kardexDetalle); // activo
        todosDetalles.add(detalleInactivo); // inactivo

        when(kardexDAO.findById(kardexId)).thenReturn(kardex);
        when(kardexDetalleDAO.findAll()).thenReturn(todosDetalles);

        // Act
        Response response = kardexDetalleResource.getDetallesActivos(kardexId.toString());

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<KardexDetalle> resultado = (List<KardexDetalle>) response.getEntity();
        assertEquals(1, resultado.size()); // Solo el activo
        assertTrue(resultado.get(0).getActivo());

        verify(kardexDAO).findById(kardexId);
        verify(kardexDetalleDAO).findAll();
    }

    @Test
    void testGetDetallesActivos_KardexNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(kardexDAO.findById(kardexId)).thenReturn(null);

        // Act
        Response response = kardexDetalleResource.getDetallesActivos(kardexId.toString());

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Kardex no encontrado"));
        verify(kardexDAO).findById(kardexId);
    }

    // ==================== TESTS DE createDetalle ====================

    @Test
    void testCreateDetalle_DatosValidos_DebeRetornarCreated() {
        // Arrange
        KardexDetalle nuevoDetalle = new KardexDetalle();
        nuevoDetalle.setLote("LOTE-NUEVO");

        when(kardexDAO.findById(kardexId)).thenReturn(kardex);
        doNothing().when(kardexDetalleDAO).crear(any(KardexDetalle.class));

        // Act
        Response response = kardexDetalleResource.createDetalle(kardexId.toString(), nuevoDetalle);

        // Assert
        assertEquals(201, response.getStatus());
        assertNotNull(response.getEntity());

        // Verificar que se asignó el kardex del path
        assertEquals(kardex, nuevoDetalle.getIdKardex());
        // Verificar que se activó por defecto
        assertTrue(nuevoDetalle.getActivo());

        verify(kardexDAO).findById(kardexId);
        verify(kardexDetalleDAO).crear(any(KardexDetalle.class));
    }

    @Test
    void testCreateDetalle_KardexNoExiste_DebeRetornarNotFound() {
        // Arrange
        KardexDetalle nuevoDetalle = new KardexDetalle();
        when(kardexDAO.findById(kardexId)).thenReturn(null);

        // Act
        Response response = kardexDetalleResource.createDetalle(kardexId.toString(), nuevoDetalle);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Kardex no encontrado"));
        verify(kardexDAO).findById(kardexId);
        verify(kardexDetalleDAO, never()).crear(any());
    }

    @Test
    void testCreateDetalle_SinLote_DebeRetornarBadRequest() {
        // Arrange
        KardexDetalle nuevoDetalle = new KardexDetalle();
        nuevoDetalle.setLote(null);

        when(kardexDAO.findById(kardexId)).thenReturn(kardex);

        // Act
        Response response = kardexDetalleResource.createDetalle(kardexId.toString(), nuevoDetalle);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("número de lote es obligatorio"));
        verify(kardexDAO).findById(kardexId);
        verify(kardexDetalleDAO, never()).crear(any());
    }

    @Test
    void testCreateDetalle_LoteVacio_DebeRetornarBadRequest() {
        // Arrange
        KardexDetalle nuevoDetalle = new KardexDetalle();
        nuevoDetalle.setLote("   ");

        when(kardexDAO.findById(kardexId)).thenReturn(kardex);

        // Act
        Response response = kardexDetalleResource.createDetalle(kardexId.toString(), nuevoDetalle);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("número de lote es obligatorio"));
        verify(kardexDAO).findById(kardexId);
        verify(kardexDetalleDAO, never()).crear(any());
    }

    // ==================== TESTS DE updateDetalle ====================

    @Test
    void testUpdateDetalle_DatosValidos_DebeRetornarOk() {
        // Arrange
        KardexDetalle actualizacion = new KardexDetalle();
        actualizacion.setLote("LOTE-ACTUALIZADO");
        actualizacion.setActivo(false);

        when(kardexDetalleDAO.findById(detalleId)).thenReturn(kardexDetalle);
        when(kardexDetalleDAO.modificar(any(KardexDetalle.class))).thenReturn(kardexDetalle);

        // Act
        Response response = kardexDetalleResource.updateDetalle(kardexId.toString(), detalleId.toString(), actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        verify(kardexDetalleDAO).findById(detalleId);
        verify(kardexDetalleDAO).modificar(any(KardexDetalle.class));
    }

    @Test
    void testUpdateDetalle_UUIDInvalido_DebeRetornarBadRequest() {
        // Arrange
        KardexDetalle actualizacion = new KardexDetalle();

        // Act
        Response response = kardexDetalleResource.updateDetalle(kardexId.toString(), "uuid-invalido", actualizacion);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("ID inválido"));
        verify(kardexDetalleDAO, never()).findById(any());
    }

    @Test
    void testUpdateDetalle_DetalleNoExiste_DebeRetornarNotFound() {
        // Arrange
        KardexDetalle actualizacion = new KardexDetalle();
        when(kardexDetalleDAO.findById(detalleId)).thenReturn(null);

        // Act
        Response response = kardexDetalleResource.updateDetalle(kardexId.toString(), detalleId.toString(), actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Detalle no encontrado"));
        verify(kardexDetalleDAO).findById(detalleId);
        verify(kardexDetalleDAO, never()).modificar(any());
    }

    @Test
    void testUpdateDetalle_DetallePerteneceOtroKardex_DebeRetornarBadRequest() {
        // Arrange
        Kardex otroKardex = new Kardex();
        otroKardex.setId(UUID.fromString("999e4567-e89b-12d3-a456-426614174000"));
        kardexDetalle.setIdKardex(otroKardex);

        KardexDetalle actualizacion = new KardexDetalle();
        when(kardexDetalleDAO.findById(detalleId)).thenReturn(kardexDetalle);

        // Act
        Response response = kardexDetalleResource.updateDetalle(kardexId.toString(), detalleId.toString(), actualizacion);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece al kardex"));
        verify(kardexDetalleDAO).findById(detalleId);
        verify(kardexDetalleDAO, never()).modificar(any());
    }

    // ==================== TESTS DE deleteDetalle ====================

    @Test
    void testDeleteDetalle_HardDelete_DebeRetornarNoContent() {
        // Arrange
        when(kardexDetalleDAO.findById(detalleId)).thenReturn(kardexDetalle);
        doNothing().when(kardexDetalleDAO).eliminar(kardexDetalle);

        // Act
        Response response = kardexDetalleResource.deleteDetalle(kardexId.toString(), detalleId.toString(), false);

        // Assert
        assertEquals(204, response.getStatus());
        verify(kardexDetalleDAO).findById(detalleId);
        verify(kardexDetalleDAO).eliminar(kardexDetalle);
        verify(kardexDetalleDAO, never()).modificar(any());
    }

    @Test
    void testDeleteDetalle_SoftDelete_DebeRetornarOk() {
        // Arrange
        when(kardexDetalleDAO.findById(detalleId)).thenReturn(kardexDetalle);
        when(kardexDetalleDAO.modificar(any(KardexDetalle.class))).thenReturn(kardexDetalle);

        // Act
        Response response = kardexDetalleResource.deleteDetalle(kardexId.toString(), detalleId.toString(), true);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertFalse(kardexDetalle.getActivo()); // Debe estar desactivado

        verify(kardexDetalleDAO).findById(detalleId);
        verify(kardexDetalleDAO).modificar(any(KardexDetalle.class));
        verify(kardexDetalleDAO, never()).eliminar(any());
    }

    @Test
    void testDeleteDetalle_UUIDInvalido_DebeRetornarBadRequest() {
        // Act
        Response response = kardexDetalleResource.deleteDetalle(kardexId.toString(), "uuid-invalido", false);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("ID inválido"));
        verify(kardexDetalleDAO, never()).findById(any());
    }

    @Test
    void testDeleteDetalle_DetalleNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(kardexDetalleDAO.findById(detalleId)).thenReturn(null);

        // Act
        Response response = kardexDetalleResource.deleteDetalle(kardexId.toString(), detalleId.toString(), false);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Detalle no encontrado"));
        verify(kardexDetalleDAO).findById(detalleId);
        verify(kardexDetalleDAO, never()).eliminar(any());
    }

    // ==================== TESTS DE countDetallesByKardex ====================

    @Test
    void testCountDetallesByKardex_KardexExiste_DebeRetornarOk() {
        // Arrange
        KardexDetalle detalleInactivo = new KardexDetalle();
        detalleInactivo.setIdKardex(kardex);
        detalleInactivo.setActivo(false);

        List<KardexDetalle> todosDetalles = new ArrayList<>();
        todosDetalles.add(kardexDetalle); // activo
        todosDetalles.add(detalleInactivo); // inactivo

        when(kardexDAO.findById(kardexId)).thenReturn(kardex);
        when(kardexDetalleDAO.findAll()).thenReturn(todosDetalles);

        // Act - Contar todos
        Response response = kardexDetalleResource.countDetallesByKardex(kardexId.toString(), false);

        // Assert
        assertEquals(200, response.getStatus());
        assertTrue(response.getEntity().toString().contains("\"count\": 2"));

        verify(kardexDAO).findById(kardexId);
        verify(kardexDetalleDAO).findAll();
    }

    @Test
    void testCountDetallesByKardex_SoloActivos_DebeRetornarSoloActivos() {
        // Arrange
        KardexDetalle detalleInactivo = new KardexDetalle();
        detalleInactivo.setIdKardex(kardex);
        detalleInactivo.setActivo(false);

        List<KardexDetalle> todosDetalles = new ArrayList<>();
        todosDetalles.add(kardexDetalle); // activo
        todosDetalles.add(detalleInactivo); // inactivo

        when(kardexDAO.findById(kardexId)).thenReturn(kardex);
        when(kardexDetalleDAO.findAll()).thenReturn(todosDetalles);

        // Act - Contar solo activos
        Response response = kardexDetalleResource.countDetallesByKardex(kardexId.toString(), true);

        // Assert
        assertEquals(200, response.getStatus());
        assertTrue(response.getEntity().toString().contains("\"count\": 1")); // Solo 1 activo

        verify(kardexDAO).findById(kardexId);
        verify(kardexDetalleDAO).findAll();
    }

    @Test
    void testCountDetallesByKardex_KardexNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(kardexDAO.findById(kardexId)).thenReturn(null);

        // Act
        Response response = kardexDetalleResource.countDetallesByKardex(kardexId.toString(), false);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Kardex no encontrado"));
        verify(kardexDAO).findById(kardexId);
    }

    // ==================== TESTS DE getLotesByKardex ====================

    @Test
    void testGetLotesByKardex_KardexExisteConLotes_DebeRetornarOk() {
        // Arrange
        KardexDetalle detalle2 = new KardexDetalle();
        detalle2.setIdKardex(kardex);
        detalle2.setLote("LOTE-002");

        KardexDetalle detalleSinLote = new KardexDetalle();
        detalleSinLote.setIdKardex(kardex);
        detalleSinLote.setLote(null);

        List<KardexDetalle> todosDetalles = new ArrayList<>();
        todosDetalles.add(kardexDetalle); // LOTE-001
        todosDetalles.add(detalle2); // LOTE-002
        todosDetalles.add(detalleSinLote); // sin lote

        when(kardexDAO.findById(kardexId)).thenReturn(kardex);
        when(kardexDetalleDAO.findAll()).thenReturn(todosDetalles);

        // Act
        Response response = kardexDetalleResource.getLotesByKardex(kardexId.toString());

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<String> resultado = (List<String>) response.getEntity();
        assertEquals(2, resultado.size()); // Solo 2 lotes válidos
        assertTrue(resultado.contains("LOTE-001"));
        assertTrue(resultado.contains("LOTE-002"));

        verify(kardexDAO).findById(kardexId);
        verify(kardexDetalleDAO).findAll();
    }

    @Test
    void testGetLotesByKardex_KardexNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(kardexDAO.findById(kardexId)).thenReturn(null);

        // Act
        Response response = kardexDetalleResource.getLotesByKardex(kardexId.toString());

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Kardex no encontrado"));
        verify(kardexDAO).findById(kardexId);
    }

// ==================== TESTS ADICIONALES PARA MEJORAR COBERTURA ====================

    @Test
    void testGetDetallesByKardex_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(kardexDAO.findById(kardexId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = kardexDetalleResource.getDetallesByKardex(kardexId.toString());

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al obtener detalles"));
        verify(kardexDAO).findById(kardexId);
    }

    @Test
    void testGetDetalleById_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(kardexDetalleDAO.findById(detalleId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = kardexDetalleResource.getDetalleById(kardexId.toString(), detalleId.toString());

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al obtener detalle"));
        verify(kardexDetalleDAO).findById(detalleId);
    }

    @Test
    void testGetDetallesActivos_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(kardexDAO.findById(kardexId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = kardexDetalleResource.getDetallesActivos(kardexId.toString());

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al obtener detalles activos"));
        verify(kardexDAO).findById(kardexId);
    }

    @Test
    void testCreateDetalle_ConExcepcionEnDAO_DebeRetornarInternalServerError() {
        // Arrange
        KardexDetalle nuevoDetalle = new KardexDetalle();
        nuevoDetalle.setLote("LOTE-NUEVO");

        when(kardexDAO.findById(kardexId)).thenReturn(kardex);
        doThrow(new RuntimeException("Error al guardar"))
                .when(kardexDetalleDAO).crear(any());

        // Act
        Response response = kardexDetalleResource.createDetalle(kardexId.toString(), nuevoDetalle);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al crear detalle"));
        verify(kardexDAO).findById(kardexId);
        verify(kardexDetalleDAO).crear(any(KardexDetalle.class));
    }

    @Test
    void testUpdateDetalle_ConExcepcionEnDAO_DebeRetornarInternalServerError() {
        // Arrange
        KardexDetalle actualizacion = new KardexDetalle();
        actualizacion.setLote("LOTE-ACTUALIZADO");

        when(kardexDetalleDAO.findById(detalleId)).thenReturn(kardexDetalle);
        when(kardexDetalleDAO.modificar(any()))
                .thenThrow(new RuntimeException("Error al actualizar"));

        // Act
        Response response = kardexDetalleResource.updateDetalle(kardexId.toString(), detalleId.toString(), actualizacion);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al actualizar detalle"));
        verify(kardexDetalleDAO).findById(detalleId);
        verify(kardexDetalleDAO).modificar(any(KardexDetalle.class));
    }

    @Test
    void testDeleteDetalle_HardDeleteConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(kardexDetalleDAO.findById(detalleId)).thenReturn(kardexDetalle);
        doThrow(new RuntimeException("Error al eliminar"))
                .when(kardexDetalleDAO).eliminar(any());

        // Act
        Response response = kardexDetalleResource.deleteDetalle(kardexId.toString(), detalleId.toString(), false);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al eliminar detalle"));
        verify(kardexDetalleDAO).findById(detalleId);
        verify(kardexDetalleDAO).eliminar(any());
    }

    @Test
    void testDeleteDetalle_SoftDeleteConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(kardexDetalleDAO.findById(detalleId)).thenReturn(kardexDetalle);
        when(kardexDetalleDAO.modificar(any()))
                .thenThrow(new RuntimeException("Error al desactivar"));

        // Act
        Response response = kardexDetalleResource.deleteDetalle(kardexId.toString(), detalleId.toString(), true);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al eliminar detalle"));
        verify(kardexDetalleDAO).findById(detalleId);
        verify(kardexDetalleDAO).modificar(any(KardexDetalle.class));
    }

    @Test
    void testCountDetallesByKardex_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(kardexDAO.findById(kardexId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = kardexDetalleResource.countDetallesByKardex(kardexId.toString(), false);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al contar detalles"));
        verify(kardexDAO).findById(kardexId);
    }

    @Test
    void testGetLotesByKardex_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(kardexDAO.findById(kardexId))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = kardexDetalleResource.getLotesByKardex(kardexId.toString());

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al obtener lotes"));
        verify(kardexDAO).findById(kardexId);
    }

    @Test
    void testCreateDetalle_ConIllegalArgumentException_DebeRetornarBadRequest() {
        // Arrange
        KardexDetalle nuevoDetalle = new KardexDetalle();
        nuevoDetalle.setLote("LOTE-NUEVO");

        when(kardexDAO.findById(kardexId)).thenReturn(kardex);
        doThrow(new IllegalArgumentException("Datos inválidos"))
                .when(kardexDetalleDAO).crear(any());

        // Act
        Response response = kardexDetalleResource.createDetalle(kardexId.toString(), nuevoDetalle);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Datos inválidos"));
        verify(kardexDAO).findById(kardexId);
        verify(kardexDetalleDAO).crear(any(KardexDetalle.class));
    }

    @Test
    void testUpdateDetalle_SoloActivoNull_DebeMantenerOtrosCampos() {
        // Arrange
        KardexDetalle actualizacion = new KardexDetalle();
        actualizacion.setLote("LOTE-ACTUALIZADO");
        // activo es null - no debe cambiar

        when(kardexDetalleDAO.findById(detalleId)).thenReturn(kardexDetalle);
        when(kardexDetalleDAO.modificar(any(KardexDetalle.class))).thenReturn(kardexDetalle);

        // Act
        Response response = kardexDetalleResource.updateDetalle(kardexId.toString(), detalleId.toString(), actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        // Verificar que el lote se actualizó pero el activo se mantuvo
        assertEquals("LOTE-ACTUALIZADO", kardexDetalle.getLote());
        assertTrue(kardexDetalle.getActivo()); // Se mantuvo true

        verify(kardexDetalleDAO).modificar(any(KardexDetalle.class));
    }

    @Test
    void testUpdateDetalle_SoloLoteNull_DebeMantenerLoteOriginal() {
        // Arrange
        KardexDetalle actualizacion = new KardexDetalle();
        actualizacion.setActivo(false);
        // lote es null - no debe cambiar

        String loteOriginal = kardexDetalle.getLote();

        when(kardexDetalleDAO.findById(detalleId)).thenReturn(kardexDetalle);
        when(kardexDetalleDAO.modificar(any(KardexDetalle.class))).thenReturn(kardexDetalle);

        // Act
        Response response = kardexDetalleResource.updateDetalle(kardexId.toString(), detalleId.toString(), actualizacion);

        // Assert
        assertEquals(200, response.getStatus());
        // Verificar que el activo se actualizó pero el lote se mantuvo
        assertEquals(loteOriginal, kardexDetalle.getLote());
        assertFalse(kardexDetalle.getActivo()); // Cambió a false

        verify(kardexDetalleDAO).modificar(any(KardexDetalle.class));
    }
}