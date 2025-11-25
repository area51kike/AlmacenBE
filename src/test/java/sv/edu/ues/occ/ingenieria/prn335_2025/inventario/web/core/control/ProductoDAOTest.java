package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoDAOTest {

    @Mock
    private EntityManager entityManager;

    private ProductoDAO productoDAO;
    private Producto producto;
    private UUID testUUID;

    @BeforeEach
    void setUp() throws Exception {
        productoDAO = new ProductoDAO();

        // Inyectar el EntityManager mock usando reflexión
        Field emField = ProductoDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(productoDAO, entityManager);

        testUUID = UUID.randomUUID();
        producto = new Producto();
        producto.setId(testUUID);
        producto.setNombreProducto("Laptop Dell XPS 15");
        producto.setReferenciaExterna("REF-12345");
        producto.setActivo(true);
        producto.setComentarios("Producto de alta gama");
    }

    @Test
    void testGetEntityManager() {
        EntityManager result = productoDAO.getEntityManager();
        assertNotNull(result);
        assertEquals(entityManager, result);
    }

    @Test
    void testConstructor() {
        ProductoDAO dao = new ProductoDAO();
        assertNotNull(dao);
    }

    // ===== CASOS EXITOSOS DE findById =====

    @Test
    void testFindById_Success() {
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);

        Producto result = productoDAO.findById(testUUID);

        assertNotNull(result);
        assertEquals(testUUID, result.getId());
        assertEquals("Laptop Dell XPS 15", result.getNombreProducto());
        assertEquals("REF-12345", result.getReferenciaExterna());
        assertTrue(result.getActivo());
        assertEquals("Producto de alta gama", result.getComentarios());
        verify(entityManager).find(Producto.class, testUUID);
    }

    @Test
    void testFindById_MultipleCalls() {
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);

        Producto result1 = productoDAO.findById(testUUID);
        Producto result2 = productoDAO.findById(testUUID);

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getId(), result2.getId());
        verify(entityManager, times(2)).find(Producto.class, testUUID);
    }

    @Test
    void testFindById_DifferentUUIDs() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Producto producto1 = new Producto();
        producto1.setId(id1);
        producto1.setNombreProducto("Producto 1");

        Producto producto2 = new Producto();
        producto2.setId(id2);
        producto2.setNombreProducto("Producto 2");

        when(entityManager.find(Producto.class, id1)).thenReturn(producto1);
        when(entityManager.find(Producto.class, id2)).thenReturn(producto2);

        Producto result1 = productoDAO.findById(id1);
        Producto result2 = productoDAO.findById(id2);

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1.getId(), result2.getId());
        assertEquals("Producto 1", result1.getNombreProducto());
        assertEquals("Producto 2", result2.getNombreProducto());
    }

    @Test
    void testFindById_NotFound() {
        UUID id = UUID.randomUUID();
        when(entityManager.find(Producto.class, id)).thenReturn(null);

        Producto result = productoDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Producto.class, id);
    }

    // ===== CASOS DE ERROR DE findById =====

    @Test
    void testFindById_NullId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productoDAO.findById(null);
        });

        assertEquals("El ID no puede ser nulo", exception.getMessage());
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void testFindById_EntityManagerException() {
        UUID id = UUID.randomUUID();
        when(entityManager.find(Producto.class, id))
                .thenThrow(new PersistenceException("Database connection error"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            productoDAO.findById(id);
        });

        assertEquals("Error al buscar el registro por ID", exception.getMessage());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof PersistenceException);
        verify(entityManager).find(Producto.class, id);
    }

    @Test
    void testFindById_RuntimeException() {
        UUID id = UUID.randomUUID();
        when(entityManager.find(Producto.class, id))
                .thenThrow(new RuntimeException("Unexpected error"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            productoDAO.findById(id);
        });

        assertEquals("Error al buscar el registro por ID", exception.getMessage());
        verify(entityManager).find(Producto.class, id);
    }

    @Test
    void testFindById_EntityManagerNull() throws Exception {
        // Simular que el EntityManager es null
        Field emField = ProductoDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(productoDAO, null);

        UUID id = UUID.randomUUID();

        // Aunque el EM es null, el catch general atrapa el NullPointerException
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            productoDAO.findById(id);
        });

        // El mensaje será "Error al buscar el registro por ID" porque cae en el catch general
        assertEquals("Error al buscar el registro por ID", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    // ===== VALIDACIÓN DE CAMPOS =====

    @Test
    void testFindById_InactiveProducto() {
        producto.setActivo(false);
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);

        Producto result = productoDAO.findById(testUUID);

        assertNotNull(result);
        assertFalse(result.getActivo());
        verify(entityManager).find(Producto.class, testUUID);
    }

    @Test
    void testFindById_WithNullFields() {
        producto.setNombreProducto(null);
        producto.setReferenciaExterna(null);
        producto.setComentarios(null);
        producto.setActivo(null);

        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);

        Producto result = productoDAO.findById(testUUID);

        assertNotNull(result);
        assertNull(result.getNombreProducto());
        assertNull(result.getReferenciaExterna());
        assertNull(result.getComentarios());
        assertNull(result.getActivo());
        verify(entityManager).find(Producto.class, testUUID);
    }

    @Test
    void testFindById_WithEmptyStrings() {
        producto.setNombreProducto("");
        producto.setReferenciaExterna("");
        producto.setComentarios("");

        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);

        Producto result = productoDAO.findById(testUUID);

        assertNotNull(result);
        assertEquals("", result.getNombreProducto());
        assertEquals("", result.getReferenciaExterna());
        assertEquals("", result.getComentarios());
        verify(entityManager).find(Producto.class, testUUID);
    }

    @Test
    void testFindById_WithMaxLengthNombreProducto() {
        String maxNombre = "A".repeat(155);
        producto.setNombreProducto(maxNombre);
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);

        Producto result = productoDAO.findById(testUUID);

        assertNotNull(result);
        assertEquals(155, result.getNombreProducto().length());
        verify(entityManager).find(Producto.class, testUUID);
    }

    @Test
    void testFindById_WithLongReferenciaExterna() {
        String longRef = "REF-" + "X".repeat(5000);
        producto.setReferenciaExterna(longRef);
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);

        Producto result = productoDAO.findById(testUUID);

        assertNotNull(result);
        assertTrue(result.getReferenciaExterna().length() > 5000);
        verify(entityManager).find(Producto.class, testUUID);
    }

    @Test
    void testFindById_WithLongComentarios() {
        String longComentarios = "Comentario: " + "X".repeat(5000);
        producto.setComentarios(longComentarios);
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);

        Producto result = productoDAO.findById(testUUID);

        assertNotNull(result);
        assertTrue(result.getComentarios().length() > 5000);
        verify(entityManager).find(Producto.class, testUUID);
    }

    @Test
    void testFindById_WithSpecialCharacters() {
        producto.setNombreProducto("Computadora 15\" Core i7 & Ryzen 9 - 2024/2025");
        producto.setReferenciaExterna("REF-ABC-123/XYZ");
        producto.setComentarios("Comentario con caracteres: áéíóú ñÑ @#$%");

        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);

        Producto result = productoDAO.findById(testUUID);

        assertNotNull(result);
        assertEquals("Computadora 15\" Core i7 & Ryzen 9 - 2024/2025", result.getNombreProducto());
        assertEquals("REF-ABC-123/XYZ", result.getReferenciaExterna());
        assertEquals("Comentario con caracteres: áéíóú ñÑ @#$%", result.getComentarios());
        verify(entityManager).find(Producto.class, testUUID);
    }

    // ===== TESTS PARA EQUALS Y HASHCODE =====

    @Test
    void testProducto_Equals_SameObject() {
        assertTrue(producto.equals(producto));
    }

    @Test
    void testProducto_Equals_Null() {
        assertFalse(producto.equals(null));
    }

    @Test
    void testProducto_Equals_DifferentClass() {
        assertFalse(producto.equals("Not a Producto"));
    }

    @Test
    void testProducto_Equals_SameId() {
        Producto producto2 = new Producto();
        producto2.setId(testUUID);
        producto2.setNombreProducto("Different Name");

        assertTrue(producto.equals(producto2));
    }

    @Test
    void testProducto_Equals_DifferentId() {
        Producto producto2 = new Producto();
        producto2.setId(UUID.randomUUID());
        producto2.setNombreProducto("Laptop Dell XPS 15");

        assertFalse(producto.equals(producto2));
    }

    @Test
    void testProducto_Equals_NullId() {
        Producto producto1 = new Producto();
        producto1.setId(null);
        producto1.setNombreProducto("Producto 1");

        Producto producto2 = new Producto();
        producto2.setId(null);
        producto2.setNombreProducto("Producto 2");

        assertFalse(producto1.equals(producto2));
    }

    @Test
    void testProducto_HashCode() {
        int hashCode1 = producto.hashCode();
        int hashCode2 = producto.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testProducto_HashCode_SameClass() {
        Producto producto2 = new Producto();
        producto2.setId(UUID.randomUUID());

        assertEquals(producto.hashCode(), producto2.hashCode());
    }

    // ===== TESTS PARA UUID ESPECIALES =====

    @Test
    void testFindById_WithRandomUUIDs() {
        for (int i = 0; i < 5; i++) {
            UUID randomUUID = UUID.randomUUID();
            Producto randomProducto = new Producto();
            randomProducto.setId(randomUUID);
            randomProducto.setNombreProducto("Producto " + i);

            when(entityManager.find(Producto.class, randomUUID)).thenReturn(randomProducto);

            Producto result = productoDAO.findById(randomUUID);

            assertNotNull(result);
            assertEquals(randomUUID, result.getId());
        }
    }

    @Test
    void testFindById_WithNameBasedUUID() {
        // UUID basado en nombre (determinístico)
        UUID nameBasedUUID = UUID.nameUUIDFromBytes("test-producto".getBytes());
        producto.setId(nameBasedUUID);

        when(entityManager.find(Producto.class, nameBasedUUID)).thenReturn(producto);

        Producto result = productoDAO.findById(nameBasedUUID);

        assertNotNull(result);
        assertEquals(nameBasedUUID, result.getId());
        verify(entityManager).find(Producto.class, nameBasedUUID);
    }

    // ===== TESTS DE INTEGRACIÓN DE findById =====

    @Test
    void testFindById_SequentialCalls() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        Producto p1 = new Producto();
        p1.setId(id1);
        p1.setNombreProducto("Producto 1");

        Producto p2 = new Producto();
        p2.setId(id2);
        p2.setNombreProducto("Producto 2");

        when(entityManager.find(Producto.class, id1)).thenReturn(p1);
        when(entityManager.find(Producto.class, id2)).thenReturn(p2);
        when(entityManager.find(Producto.class, id3)).thenReturn(null);

        Producto result1 = productoDAO.findById(id1);
        Producto result2 = productoDAO.findById(id2);
        Producto result3 = productoDAO.findById(id3);

        assertNotNull(result1);
        assertNotNull(result2);
        assertNull(result3);
        assertEquals("Producto 1", result1.getNombreProducto());
        assertEquals("Producto 2", result2.getNombreProducto());
    }

    @Test
    void testFindById_AfterException() {
        UUID validId = UUID.randomUUID();
        UUID errorId = UUID.randomUUID();

        // Crear un producto con el validId correcto
        Producto validProducto = new Producto();
        validProducto.setId(validId);
        validProducto.setNombreProducto("Producto Válido");

        when(entityManager.find(Producto.class, errorId))
                .thenThrow(new RuntimeException("Error"));
        when(entityManager.find(Producto.class, validId)).thenReturn(validProducto);

        // Primera llamada lanza excepción
        assertThrows(IllegalStateException.class, () -> productoDAO.findById(errorId));

        // Segunda llamada debe funcionar normalmente
        Producto result = productoDAO.findById(validId);
        assertNotNull(result);
        assertEquals(validId, result.getId());
    }
}