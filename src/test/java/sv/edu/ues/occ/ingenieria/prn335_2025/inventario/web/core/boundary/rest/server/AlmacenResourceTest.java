package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.AlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoAlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Almacen;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests para AlmacenResource usando Mockito + Jersey
 */
class AlmacenResourceTest {

    @Mock
    private AlmacenDAO almacenDAO;

    @Mock
    private TipoAlmacenDAO tipoAlmacenDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    private AlmacenResource almacenResource;
    private TipoAlmacen tipoAlmacen;
    private Almacen almacen;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        almacenResource = new AlmacenResource();

        // Inyectar manualmente los DAOs mockeados
        almacenResource.almacenDAO = almacenDAO;
        almacenResource.tipoAlmacenDAO = tipoAlmacenDAO;

        // Crear datos de prueba
        tipoAlmacen = new TipoAlmacen();
        tipoAlmacen.setId(1);
        tipoAlmacen.setNombre("Almacén Principal");
        tipoAlmacen.setActivo(true);

        almacen = new Almacen();
        almacen.setId(10);
        almacen.setIdTipoAlmacen(tipoAlmacen);
        almacen.setActivo(true);
        almacen.setObservaciones("Almacén de prueba");
    }

    // ==================== TESTS DE getAlmacenesPorTipo ====================

    @Test
    void testGetAlmacenesPorTipo_ConIdNull_DebeRetornarBadRequest() {
        // Act
        Response response = almacenResource.getAlmacenesPorTipo(null);

        // Assert
        assertEquals(400, response.getStatus(), "Debe retornar BAD_REQUEST (400)");
        assertTrue(response.getEntity().toString().contains("no puede ser nulo"));
        verify(tipoAlmacenDAO, never()).findById(any());
        verify(almacenDAO, never()).findByTipoAlmacen(any());
    }

    @Test
    void testGetAlmacenesPorTipo_TipoAlmacenNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(tipoAlmacenDAO.findById(1)).thenReturn(null);

        // Act
        Response response = almacenResource.getAlmacenesPorTipo(1);

        // Assert
        assertEquals(404, response.getStatus(), "Debe retornar NOT_FOUND (404)");
        assertTrue(response.getEntity().toString().contains("No existe el tipo de almacén"));
        verify(tipoAlmacenDAO).findById(1);
        verify(almacenDAO, never()).findByTipoAlmacen(any());
    }

    @Test
    void testGetAlmacenesPorTipo_SinAlmacenes_DebeRetornarNotFound() {
        // Arrange
        when(tipoAlmacenDAO.findById(1)).thenReturn(tipoAlmacen);
        when(almacenDAO.findByTipoAlmacen(1)).thenReturn(Collections.emptyList());

        // Act
        Response response = almacenResource.getAlmacenesPorTipo(1);

        // Assert
        assertEquals(404, response.getStatus(), "Debe retornar NOT_FOUND (404)");
        assertTrue(response.getEntity().toString().contains("No se encontraron almacenes"));
        verify(tipoAlmacenDAO).findById(1);
        verify(almacenDAO).findByTipoAlmacen(1);
    }

    @Test
    void testGetAlmacenesPorTipo_ConAlmacenesExistentes_DebeRetornarOk() {
        // Arrange
        Almacen almacen2 = new Almacen();
        almacen2.setId(11);
        almacen2.setIdTipoAlmacen(tipoAlmacen);

        List<Almacen> almacenes = new ArrayList<>();
        almacenes.add(almacen);
        almacenes.add(almacen2);

        when(tipoAlmacenDAO.findById(1)).thenReturn(tipoAlmacen);
        when(almacenDAO.findByTipoAlmacen(1)).thenReturn(almacenes);

        // Act
        Response response = almacenResource.getAlmacenesPorTipo(1);

        // Assert
        assertEquals(200, response.getStatus(), "Debe retornar OK (200)");
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Almacen> resultado = (List<Almacen>) response.getEntity();
        assertEquals(2, resultado.size(), "Debe retornar 2 almacenes");

        verify(tipoAlmacenDAO).findById(1);
        verify(almacenDAO).findByTipoAlmacen(1);
    }

    @Test
    void testGetAlmacenesPorTipo_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(tipoAlmacenDAO.findById(1)).thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        Response response = almacenResource.getAlmacenesPorTipo(1);

        // Assert
        assertEquals(500, response.getStatus(), "Debe retornar INTERNAL_SERVER_ERROR (500)");
        assertTrue(response.getEntity().toString().contains("Error al obtener almacenes"));
    }

    // ==================== TESTS DE findById ====================

    @Test
    void testFindById_ConIdsNulos_DebeRetornarBadRequest() {
        // Act & Assert
        Response response1 = almacenResource.findById(null, 10);
        assertEquals(400, response1.getStatus());

        Response response2 = almacenResource.findById(1, null);
        assertEquals(400, response2.getStatus());

        Response response3 = almacenResource.findById(null, null);
        assertEquals(400, response3.getStatus());

        verify(almacenDAO, never()).findById(any());
    }

    @Test
    void testFindById_AlmacenNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(almacenDAO.findById(10)).thenReturn(null);

        // Act
        Response response = almacenResource.findById(1, 10);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no encontrado"));
        verify(almacenDAO).findById(10);
    }

    @Test
    void testFindById_AlmacenConTipoAlmacenNull_DebeRetornarNotFound() {
        // Arrange
        almacen.setIdTipoAlmacen(null);
        when(almacenDAO.findById(10)).thenReturn(almacen);

        // Act
        Response response = almacenResource.findById(1, 10);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece"));
        verify(almacenDAO).findById(10);
    }

    @Test
    void testFindById_AlmacenNoPerteneceAlTipo_DebeRetornarNotFound() {
        // Arrange
        TipoAlmacen otroTipo = new TipoAlmacen();
        otroTipo.setId(999);
        almacen.setIdTipoAlmacen(otroTipo);
        when(almacenDAO.findById(10)).thenReturn(almacen);

        // Act
        Response response = almacenResource.findById(1, 10);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece"));
        verify(almacenDAO).findById(10);
    }

    @Test
    void testFindById_AlmacenExistente_DebeRetornarOk() {
        // Arrange
        when(almacenDAO.findById(10)).thenReturn(almacen);

        // Act
        Response response = almacenResource.findById(1, 10);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        Almacen resultado = (Almacen) response.getEntity();
        assertEquals(10, resultado.getId());
        assertEquals(1, resultado.getIdTipoAlmacen().getId());

        verify(almacenDAO).findById(10);
    }

    @Test
    void testFindById_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(almacenDAO.findById(10)).thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = almacenResource.findById(1, 10);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al buscar"));
    }

    // ==================== TESTS DE create ====================

    @Test
    void testCreate_ConIdTipoAlmacenNull_DebeRetornarBadRequest() {
        // Arrange
        Almacen nuevoAlmacen = new Almacen();

        // Act
        Response response = almacenResource.create(null, nuevoAlmacen, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no puede ser nulo"));
        verify(almacenDAO, never()).crear(any());
    }

    @Test
    void testCreate_ConEntityNull_DebeRetornarBadRequest() {
        // Act
        Response response = almacenResource.create(1, null, uriInfo);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no puede ser nula"));
        verify(almacenDAO, never()).crear(any());
    }

    @Test
    void testCreate_TipoAlmacenNoExiste_DebeRetornarNotFound() {
        // Arrange
        Almacen nuevoAlmacen = new Almacen();
        when(tipoAlmacenDAO.findById(1)).thenReturn(null);

        // Act
        Response response = almacenResource.create(1, nuevoAlmacen, uriInfo);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("No existe el tipo de almacén"));
        verify(tipoAlmacenDAO).findById(1);
        verify(almacenDAO, never()).crear(any());
    }

    @Test
    void testCreate_CreacionExitosa_DebeRetornarCreated() throws Exception {
        // Arrange
        Almacen nuevoAlmacen = new Almacen();
        nuevoAlmacen.setActivo(true);
        nuevoAlmacen.setObservaciones("Nuevo almacén");

        URI uri = new URI("http://localhost:8080/api/tipo_almacen/1/almacen/10");

        when(tipoAlmacenDAO.findById(1)).thenReturn(tipoAlmacen);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(uri);

        // Simular que después de crear, el almacén obtiene un ID
        doAnswer(invocation -> {
            Almacen a = invocation.getArgument(0);
            a.setId(10);
            return null;
        }).when(almacenDAO).crear(any(Almacen.class));

        // Act
        Response response = almacenResource.create(1, nuevoAlmacen, uriInfo);

        // Assert
        assertEquals(201, response.getStatus(), "Debe retornar CREATED (201)");
        assertNotNull(response.getEntity());

        Almacen resultado = (Almacen) response.getEntity();
        assertEquals(1, resultado.getIdTipoAlmacen().getId());
        assertNotNull(resultado.getId());

        verify(tipoAlmacenDAO).findById(1);
        verify(almacenDAO).crear(any(Almacen.class));
    }

    @Test
    void testCreate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        Almacen nuevoAlmacen = new Almacen();
        when(tipoAlmacenDAO.findById(1)).thenReturn(tipoAlmacen);
        doThrow(new RuntimeException("Error al guardar")).when(almacenDAO).crear(any());

        // Act
        Response response = almacenResource.create(1, nuevoAlmacen, uriInfo);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al crear"));
    }

    // ==================== TESTS DE update ====================

    @Test
    void testUpdate_ConIdsNulos_DebeRetornarBadRequest() {
        // Arrange
        Almacen actualizacion = new Almacen();

        // Act & Assert
        Response response1 = almacenResource.update(null, 10, actualizacion);
        assertEquals(400, response1.getStatus());

        Response response2 = almacenResource.update(1, null, actualizacion);
        assertEquals(400, response2.getStatus());

        verify(almacenDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ConEntityNull_DebeRetornarBadRequest() {
        // Act
        Response response = almacenResource.update(1, 10, null);

        // Assert
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no puede ser nula"));
        verify(almacenDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_AlmacenNoExiste_DebeRetornarNotFound() {
        // Arrange
        Almacen actualizacion = new Almacen();
        when(almacenDAO.findById(10)).thenReturn(null);

        // Act
        Response response = almacenResource.update(1, 10, actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no encontrado"));
        verify(almacenDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_AlmacenNoPerteneceAlTipo_DebeRetornarForbidden() {
        // Arrange
        TipoAlmacen otroTipo = new TipoAlmacen();
        otroTipo.setId(999);
        almacen.setIdTipoAlmacen(otroTipo);

        Almacen actualizacion = new Almacen();
        when(almacenDAO.findById(10)).thenReturn(almacen);

        // Act
        Response response = almacenResource.update(1, 10, actualizacion);

        // Assert
        assertEquals(403, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece"));
        verify(almacenDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ActualizacionExitosa_DebeRetornarOk() {
        // Arrange
        Almacen actualizacion = new Almacen();
        actualizacion.setActivo(false);
        actualizacion.setObservaciones("Actualizado");

        when(almacenDAO.findById(10)).thenReturn(almacen);
        // CORRECCIÓN: modificar() retorna T, no es void
        when(almacenDAO.modificar(any(Almacen.class))).thenReturn(almacen);

        // Act
        Response response = almacenResource.update(1, 10, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());

        Almacen resultado = (Almacen) response.getEntity();
        assertFalse(resultado.getActivo());
        assertEquals("Actualizado", resultado.getObservaciones());

        verify(almacenDAO).findById(10);
        verify(almacenDAO).modificar(any(Almacen.class));
    }

    @Test
    void testUpdate_CambioTipoAlmacen_DebeActualizarTipo() {
        // Arrange
        TipoAlmacen nuevoTipo = new TipoAlmacen();
        nuevoTipo.setId(2);
        nuevoTipo.setNombre("Nuevo Tipo");

        Almacen actualizacion = new Almacen();
        actualizacion.setActivo(true);
        actualizacion.setIdTipoAlmacen(nuevoTipo);

        when(almacenDAO.findById(10)).thenReturn(almacen);
        when(tipoAlmacenDAO.findById(2)).thenReturn(nuevoTipo);
        // CORRECCIÓN: modificar() retorna T
        when(almacenDAO.modificar(any(Almacen.class))).thenReturn(almacen);

        // Act
        Response response = almacenResource.update(1, 10, actualizacion);

        // Assert
        assertEquals(200, response.getStatus());

        Almacen resultado = (Almacen) response.getEntity();
        assertEquals(2, resultado.getIdTipoAlmacen().getId());

        verify(tipoAlmacenDAO).findById(2);
        verify(almacenDAO).modificar(any(Almacen.class));
    }

    @Test
    void testUpdate_NuevoTipoAlmacenNoExiste_DebeRetornarNotFound() {
        // Arrange
        TipoAlmacen nuevoTipo = new TipoAlmacen();
        nuevoTipo.setId(999);

        Almacen actualizacion = new Almacen();
        actualizacion.setIdTipoAlmacen(nuevoTipo);

        when(almacenDAO.findById(10)).thenReturn(almacen);
        when(tipoAlmacenDAO.findById(999)).thenReturn(null);

        // Act
        Response response = almacenResource.update(1, 10, actualizacion);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("No existe el tipo de almacén"));
        verify(almacenDAO, never()).modificar(any());
    }

    @Test
    void testUpdate_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        Almacen actualizacion = new Almacen();
        when(almacenDAO.findById(10)).thenReturn(almacen);
        when(almacenDAO.modificar(any())).thenThrow(new RuntimeException("Error al actualizar"));

        // Act
        Response response = almacenResource.update(1, 10, actualizacion);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al actualizar"));
    }

    // ==================== TESTS DE delete ====================

    @Test
    void testDelete_ConIdsNulos_DebeRetornarBadRequest() {
        // Act & Assert
        Response response1 = almacenResource.delete(null, 10);
        assertEquals(400, response1.getStatus());

        Response response2 = almacenResource.delete(1, null);
        assertEquals(400, response2.getStatus());

        verify(almacenDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_AlmacenNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(almacenDAO.findById(10)).thenReturn(null);

        // Act
        Response response = almacenResource.delete(1, 10);

        // Assert
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no encontrado"));
        verify(almacenDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_AlmacenConTipoNull_DebeRetornarForbidden() {
        // Arrange
        almacen.setIdTipoAlmacen(null);
        when(almacenDAO.findById(10)).thenReturn(almacen);

        // Act
        Response response = almacenResource.delete(1, 10);

        // Assert
        assertEquals(403, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece"));
        verify(almacenDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_AlmacenNoPerteneceAlTipo_DebeRetornarForbidden() {
        // Arrange
        TipoAlmacen otroTipo = new TipoAlmacen();
        otroTipo.setId(999);
        almacen.setIdTipoAlmacen(otroTipo);
        when(almacenDAO.findById(10)).thenReturn(almacen);

        // Act
        Response response = almacenResource.delete(1, 10);

        // Assert
        assertEquals(403, response.getStatus());
        assertTrue(response.getEntity().toString().contains("no pertenece"));
        verify(almacenDAO, never()).eliminar(any());
    }

    @Test
    void testDelete_EliminacionExitosa_DebeRetornarNoContent() {
        // Arrange
        when(almacenDAO.findById(10)).thenReturn(almacen);
        doNothing().when(almacenDAO).eliminar(any(Almacen.class));

        // Act
        Response response = almacenResource.delete(1, 10);

        // Assert
        assertEquals(204, response.getStatus(), "Debe retornar NO_CONTENT (204)");
        verify(almacenDAO).findById(10);
        verify(almacenDAO).eliminar(almacen);
    }

    @Test
    void testDelete_ConExcepcion_DebeRetornarInternalServerError() {
        // Arrange
        when(almacenDAO.findById(10)).thenReturn(almacen);
        doThrow(new RuntimeException("Error al eliminar")).when(almacenDAO).eliminar(any());

        // Act
        Response response = almacenResource.delete(1, 10);

        // Assert
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al eliminar"));
    }
}