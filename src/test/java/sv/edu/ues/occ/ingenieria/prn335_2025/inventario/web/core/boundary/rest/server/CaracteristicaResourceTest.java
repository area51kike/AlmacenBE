package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoUnidadMedidaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoUnidadMedida;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests para CaracteristicaResource
 * Verifica el comportamiento de todos los endpoints REST
 */
class CaracteristicaResourceTest {

    @Mock
    private CaracteristicaDAO caracteristicaDAO;

    @Mock
    private TipoUnidadMedidaDAO tipoUnidadMedidaDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    private CaracteristicaResource caracteristicaResource;
    private TipoUnidadMedida tipoUnidadMedida;
    private Caracteristica caracteristica;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        caracteristicaResource = new CaracteristicaResource();

        // Inyección manual de los DAOs
        caracteristicaResource.caracteristicaDAO = caracteristicaDAO;
        caracteristicaResource.tipoUnidadMedidaDAO = tipoUnidadMedidaDAO;

        // Datos de prueba
        tipoUnidadMedida = new TipoUnidadMedida();
        tipoUnidadMedida.setId(1);
        tipoUnidadMedida.setNombre("Longitud");
        tipoUnidadMedida.setActivo(true);
        tipoUnidadMedida.setUnidadBase("metro");

        caracteristica = new Caracteristica();
        caracteristica.setId(10);
        caracteristica.setNombre("Altura");
        caracteristica.setIdTipoUnidadMedida(tipoUnidadMedida);
        caracteristica.setActivo(true);
        caracteristica.setDescripcion("Medida de altura");
    }

    // ==================== TESTS DE getCaracteristicasPorTipo ====================

    @Test
    void testGetCaracteristicasPorTipo_ConIdNull_DebeRetornarBadRequest() {
        // Act
        Response response = caracteristicaResource.getCaracteristicasPorTipo(null);

        // Assert
        assertEquals(400, response.getStatus(), "Debe retornar BAD_REQUEST");
        assertTrue(response.getEntity().toString().contains("no puede ser nulo"));
        verify(tipoUnidadMedidaDAO, never()).findById(any());
        verify(caracteristicaDAO, never()).findByTipoUnidadMedida(any());
    }

    @Test
    void testGetCaracteristicasPorTipo_TipoNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(tipoUnidadMedidaDAO.findById(1)).thenReturn(null);

        // Act
        Response response = caracteristicaResource.getCaracteristicasPorTipo(1);

        // Assert
        assertEquals(404, response.getStatus(), "Debe retornar NOT_FOUND");
        assertTrue(response.getEntity().toString().contains("No existe el tipo de unidad de medida"));
        verify(tipoUnidadMedidaDAO).findById(1);
        verify(caracteristicaDAO, never()).findByTipoUnidadMedida(any());
    }

    @Test
    void testGetCaracteristicasPorTipo_ConCaracteristicas_DebeRetornarOk() {
        // Arrange
        Caracteristica caracteristica2 = new Caracteristica();
        caracteristica2.setId(11);
        caracteristica2.setNombre("Ancho");
        caracteristica2.setIdTipoUnidadMedida(tipoUnidadMedida);

        List<Caracteristica> caracteristicas = new ArrayList<>();
        caracteristicas.add(caracteristica);
        caracteristicas.add(caracteristica2);

        when(tipoUnidadMedidaDAO.findById(1)).thenReturn(tipoUnidadMedida);
        when(caracteristicaDAO.findByTipoUnidadMedida(1)).thenReturn(caracteristicas);

        // Act
        Response response = caracteristicaResource.getCaracteristicasPorTipo(1);

        // Assert
        assertEquals(200, response.getStatus(), "Debe retornar OK");
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Caracteristica> resultado = (List<Caracteristica>) response.getEntity();
        assertEquals(2, resultado.size(), "Debe retornar 2 características");

        verify(tipoUnidadMedidaDAO).findById(1);
        verify(caracteristicaDAO).findByTipoUnidadMedida(1);
    }

    @Test
    void testGetCaracteristicasPorTipo_ListaVacia_DebeRetornarOk() {
        // Arrange
        when(tipoUnidadMedidaDAO.findById(1)).thenReturn(tipoUnidadMedida);
        when(caracteristicaDAO.findByTipoUnidadMedida(1)).thenReturn(Collections.emptyList());

        // Act
        Response response = caracteristicaResource.getCaracteristicasPorTipo(1);

        // Assert
        assertEquals(200, response.getStatus(), "Debe retornar OK con lista vacía");
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Caracteristica> resultado = (List<Caracteristica>) response.getEntity();
        assertTrue(resultado.isEmpty());

        verify(tipoUnidadMedidaDAO).findById(1);
        verify(caracteristicaDAO).findByTipoUnidadMedida(1);
    }

    @Test
    void testGetCaracteristicasPorTipo_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(tipoUnidadMedidaDAO.findById(1))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        Response response = caracteristicaResource.getCaracteristicasPorTipo(1);

        // Assert
        assertEquals(500, response.getStatus(), "Debe retornar INTERNAL_SERVER_ERROR");
        assertTrue(response.getEntity().toString().contains("Error al obtener características"));
    }

    // ==================== TESTS DE findById ====================

    @Test
    void testFindById_ConIdsNulos_DebeRetornarBadRequest() {
        // Act & Assert
        Response response1 = caracteristicaResource.findById(null, 10);
        assertEquals(400, response1.getStatus());

        Response response2 = caracteristicaResource.findById(1, null);
        assertEquals(400, response2.getStatus());

        Response response3 = caracteristicaResource.findById(null, null);
        assertEquals(400, response3.getStatus());

        verify(caracteristicaDAO, never()).findById(any());
    }

    @Test
    void testFindById_CaracteristicaNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(caracteristicaDAO.findById(10)).thenReturn(null);

        // Act
        Response response = caracteristicaResource.findById(1, 10);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no encontrada"));
        verify(caracteristicaDAO).findById(10);
    }

    @Test
    void testFindById_CaracteristicaConTipoNull_DebeRetornarNotFound() {
        // Arrange
        caracteristica.setIdTipoUnidadMedida(null);
        when(caracteristicaDAO.findById(10)).thenReturn(caracteristica);

        // Act
        Response response = caracteristicaResource.findById(1, 10);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece"));
        verify(caracteristicaDAO).findById(10);
    }

    @Test
    void testFindById_CaracteristicaNoPerteneceAlTipo_DebeRetornarNotFound() {
        // Arrange
        TipoUnidadMedida otroTipo = new TipoUnidadMedida();
        otroTipo.setId(999);
        caracteristica.setIdTipoUnidadMedida(otroTipo);
        when(caracteristicaDAO.findById(10)).thenReturn(caracteristica);

        // Act
        Response response = caracteristicaResource.findById(1, 10);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece"));
        verify(caracteristicaDAO).findById(10);
    }

    @Test
    void testFindById_CaracteristicaExistente_DebeRetornarOk() {
        // Arrange
        when(caracteristicaDAO.findById(10)).thenReturn(caracteristica);

        // Act
        Response response = caracteristicaResource.findById(1, 10);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        Caracteristica resultado = (Caracteristica) response.getEntity();
        assertEquals(10, resultado.getId());
        assertEquals(1, resultado.getIdTipoUnidadMedida().getId());

        verify(caracteristicaDAO).findById(10);
    }

    @Test
    void testFindById_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(caracteristicaDAO.findById(10))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = caracteristicaResource.findById(1, 10);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al buscar"));
    }

    // ==================== TESTS DE create ====================

    @Test
    void testCreate_ConIdTipoNull_DebeRetornarBadRequest() {
        // Arrange
        Caracteristica nueva = new Caracteristica();

        // Act
        Response response = caracteristicaResource.create(null, nueva, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no puede ser nulo"));
        verify(caracteristicaDAO, never()).crear(any());
    }

    @Test
    void testCreate_ConEntityNull_DebeRetornarBadRequest() {
        // Act
        Response response = caracteristicaResource.create(1, null, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no puede ser nula"));
        verify(caracteristicaDAO, never()).crear(any());
    }

    @Test
    void testCreate_TipoNoExiste_DebeRetornarNotFound() {
        // Arrange
        Caracteristica nueva = new Caracteristica();
        when(tipoUnidadMedidaDAO.findById(1)).thenReturn(null);

        // Act
        Response response = caracteristicaResource.create(1, nueva, uriInfo);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("No existe el tipo de unidad de medida"));
        verify(tipoUnidadMedidaDAO).findById(1);
        verify(caracteristicaDAO, never()).crear(any());
    }

    @Test
    void testCreate_CreacionExitosa_DebeRetornarCreated() throws Exception {
        // Arrange
        Caracteristica nueva = new Caracteristica();
        nueva.setNombre("Profundidad");
        nueva.setActivo(true);
        nueva.setDescripcion("Medida de profundidad");

        URI uri = new URI("http://localhost:8080/api/tipo_unidad_medida/1/caracteristica/10");

        when(tipoUnidadMedidaDAO.findById(1)).thenReturn(tipoUnidadMedida);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(uri);

        // Simular que después de crear, se asigna un ID
        doAnswer(invocation -> {
            Caracteristica c = invocation.getArgument(0);
            c.setId(10);
            return null;
        }).when(caracteristicaDAO).crear(any(Caracteristica.class));

        // Act
        Response response = caracteristicaResource.create(1, nueva, uriInfo);

        // Assert
        assertEquals(201, response.getStatus(), "Debe retornar CREATED");
        assertNotNull(response.getEntity());

        Caracteristica resultado = (Caracteristica) response.getEntity();
        assertEquals(1, resultado.getIdTipoUnidadMedida().getId());
        assertNotNull(resultado.getId());

        verify(tipoUnidadMedidaDAO).findById(1);
        verify(caracteristicaDAO).crear(any(Caracteristica.class));
    }

    @Test
    void testCreate_ConIllegalArgumentException_DebeRetornarBadRequest() {
        // Arrange
        Caracteristica nueva = new Caracteristica();
        when(tipoUnidadMedidaDAO.findById(1)).thenReturn(tipoUnidadMedida);
        doThrow(new IllegalArgumentException("Datos inválidos"))
                .when(caracteristicaDAO).crear(any());

        // Act
        Response response = caracteristicaResource.create(1, nueva, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Datos inválidos"));
    }

    @Test
    void testCreate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        Caracteristica nueva = new Caracteristica();
        when(tipoUnidadMedidaDAO.findById(1)).thenReturn(tipoUnidadMedida);
        doThrow(new RuntimeException("Error al guardar"))
                .when(caracteristicaDAO).crear(any());

        // Act
        Response response = caracteristicaResource.create(1, nueva, uriInfo);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al crear"));
    }

    // ==================== TESTS DE update ====================

    @Test
    void testUpdate_ConIdsNulos_DebeRetornarBadRequest() {
        // Arrange
        Caracteristica actualizacion = new Caracteristica();

        // Act & Assert
        Response response1 = caracteristicaResource.update(null, 10, actualizacion);
        assertEquals(400, response1.getStatus());

        Response response2 = caracteristicaResource.update(1, null, actualizacion);
        assertEquals(400, response2.getStatus());

        verify(caracteristicaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ConEntityNull_DebeRetornarBadRequest() {
        // Act
        Response response = caracteristicaResource.update(1, 10, null);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no puede ser nula"));
        verify(caracteristicaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_CaracteristicaNoExiste_DebeRetornarNotFound() {
        // Arrange
        Caracteristica actualizacion = new Caracteristica();
        when(caracteristicaDAO.findById(10)).thenReturn(null);

        // Act
        Response response = caracteristicaResource.update(1, 10, actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no encontrada"));
        verify(caracteristicaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_CaracteristicaNoPerteneceAlTipo_DebeRetornarForbidden() {
        // Arrange
        TipoUnidadMedida otroTipo = new TipoUnidadMedida();
        otroTipo.setId(999);
        caracteristica.setIdTipoUnidadMedida(otroTipo);

        Caracteristica actualizacion = new Caracteristica();
        when(caracteristicaDAO.findById(10)).thenReturn(caracteristica);

        // Act
        Response response = caracteristicaResource.update(1, 10, actualizacion);

        // Assert
        assertEquals(403, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece"));
        verify(caracteristicaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ActualizacionExitosa_DebeRetornarOk() {
        // Arrange
        Caracteristica actualizacion = new Caracteristica();
        actualizacion.setNombre("Nuevo Nombre");
        actualizacion.setActivo(false);
        actualizacion.setDescripcion("Nueva descripción");

        when(caracteristicaDAO.findById(10)).thenReturn(caracteristica);
        when(caracteristicaDAO.modificar(any(Caracteristica.class))).thenReturn(caracteristica);

        // Act
        Response response = caracteristicaResource.update(1, 10, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());

        Caracteristica resultado = (Caracteristica) response.getEntity();
        assertEquals("Nuevo Nombre", resultado.getNombre());
        assertFalse(resultado.getActivo());
        assertEquals("Nueva descripción", resultado.getDescripcion());

        verify(caracteristicaDAO).findById(10);
        verify(caracteristicaDAO).modificar(any(Caracteristica.class));
    }

    @Test
    void testUpdate_CambiarTipo_DebeActualizarTipo() {
        // Arrange
        TipoUnidadMedida nuevoTipo = new TipoUnidadMedida();
        nuevoTipo.setId(2);
        nuevoTipo.setNombre("Peso");

        Caracteristica actualizacion = new Caracteristica();
        actualizacion.setIdTipoUnidadMedida(nuevoTipo);

        when(caracteristicaDAO.findById(10)).thenReturn(caracteristica);
        when(tipoUnidadMedidaDAO.findById(2)).thenReturn(nuevoTipo);
        when(caracteristicaDAO.modificar(any(Caracteristica.class))).thenReturn(caracteristica);

        // Act
        Response response = caracteristicaResource.update(1, 10, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());

        Caracteristica resultado = (Caracteristica) response.getEntity();
        assertEquals(2, resultado.getIdTipoUnidadMedida().getId());

        verify(tipoUnidadMedidaDAO).findById(2);
        verify(caracteristicaDAO).modificar(any(Caracteristica.class));
    }

    @Test
    void testUpdate_NuevoTipoNoExiste_DebeRetornarNotFound() {
        // Arrange
        TipoUnidadMedida nuevoTipo = new TipoUnidadMedida();
        nuevoTipo.setId(999);

        Caracteristica actualizacion = new Caracteristica();
        actualizacion.setIdTipoUnidadMedida(nuevoTipo);

        when(caracteristicaDAO.findById(10)).thenReturn(caracteristica);
        when(tipoUnidadMedidaDAO.findById(999)).thenReturn(null);

        // Act
        Response response = caracteristicaResource.update(1, 10, actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("No existe el tipo de unidad de medida"));
        verify(caracteristicaDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        Caracteristica actualizacion = new Caracteristica();
        when(caracteristicaDAO.findById(10)).thenReturn(caracteristica);
        when(caracteristicaDAO.modificar(any()))
                .thenThrow(new RuntimeException("Error al actualizar"));

        // Act
        Response response = caracteristicaResource.update(1, 10, actualizacion);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al actualizar"));
    }

    // ==================== TESTS DE delete ====================

    @Test
    void testDelete_ConIdsNulos_DebeRetornarBadRequest() {
        // Act & Assert
        Response response1 = caracteristicaResource.delete(null, 10);
        assertEquals(400, response1.getStatus());

        Response response2 = caracteristicaResource.delete(1, null);
        assertEquals(400, response2.getStatus());

        verify(caracteristicaDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_CaracteristicaNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(caracteristicaDAO.findById(10)).thenReturn(null);

        // Act
        Response response = caracteristicaResource.delete(1, 10);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no encontrada"));
        verify(caracteristicaDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_CaracteristicaConTipoNull_DebeRetornarForbidden() {
        // Arrange
        caracteristica.setIdTipoUnidadMedida(null);
        when(caracteristicaDAO.findById(10)).thenReturn(caracteristica);

        // Act
        Response response = caracteristicaResource.delete(1, 10);

        // Assert
        assertEquals(403, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece"));
        verify(caracteristicaDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_CaracteristicaNoPerteneceAlTipo_DebeRetornarForbidden() {
        // Arrange
        TipoUnidadMedida otroTipo = new TipoUnidadMedida();
        otroTipo.setId(999);
        caracteristica.setIdTipoUnidadMedida(otroTipo);
        when(caracteristicaDAO.findById(10)).thenReturn(caracteristica);

        // Act
        Response response = caracteristicaResource.delete(1, 10);

        // Assert
        assertEquals(403, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece"));
        verify(caracteristicaDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_EliminacionExitosa_DebeRetornarNoContent() {
        // Arrange
        when(caracteristicaDAO.findById(10)).thenReturn(caracteristica);
        doNothing().when(caracteristicaDAO).eliminar(any(Caracteristica.class));

        // Act
        Response response = caracteristicaResource.delete(1, 10);

        // Assert
        assertEquals(204, response.getStatus(), "Debe retornar NO_CONTENT");
        verify(caracteristicaDAO).findById(10);
        verify(caracteristicaDAO).eliminar(caracteristica);
    }

    @Test
    void testDelete_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(caracteristicaDAO.findById(10)).thenReturn(caracteristica);
        doThrow(new RuntimeException("Error al eliminar"))
                .when(caracteristicaDAO).eliminar(any());

        // Act
        Response response = caracteristicaResource.delete(1, 10);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al eliminar"));
    }
}