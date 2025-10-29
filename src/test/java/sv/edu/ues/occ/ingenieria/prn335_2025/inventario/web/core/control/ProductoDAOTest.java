package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
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

        // Usar reflexión para inyectar el EntityManager mock
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
    void testFindById_NotFound() {
        UUID id = UUID.randomUUID();
        when(entityManager.find(Producto.class, id)).thenReturn(null);

        Producto result = productoDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Producto.class, id);
    }

    @Test
    void testFindById_NullId() {
        assertThrows(IllegalArgumentException.class, () -> {
            productoDAO.findById(null);
        });
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void testFindById_Exception() {
        UUID id = UUID.randomUUID();
        when(entityManager.find(Producto.class, id))
                .thenThrow(new RuntimeException("Database connection error"));

        assertThrows(IllegalStateException.class, () -> productoDAO.findById(id));
        verify(entityManager).find(Producto.class, id);
    }

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
    void testFindById_WithNullReferenciaExterna() {
        producto.setReferenciaExterna(null);
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);

        Producto result = productoDAO.findById(testUUID);

        assertNotNull(result);
        assertNull(result.getReferenciaExterna());
        verify(entityManager).find(Producto.class, testUUID);
    }

    @Test
    void testFindById_WithNullComentarios() {
        producto.setComentarios(null);
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);

        Producto result = productoDAO.findById(testUUID);

        assertNotNull(result);
        assertNull(result.getComentarios());
        verify(entityManager).find(Producto.class, testUUID);
    }

    @Test
    void testFindById_WithEmptyNombreProducto() {
        producto.setNombreProducto("");
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);

        Producto result = productoDAO.findById(testUUID);

        assertNotNull(result);
        assertEquals("", result.getNombreProducto());
        verify(entityManager).find(Producto.class, testUUID);
    }

    @Test
    void testFindById_WithNullNombreProducto() {
        producto.setNombreProducto(null);
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);

        Producto result = productoDAO.findById(testUUID);

        assertNotNull(result);
        assertNull(result.getNombreProducto());
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
        String longRef = "REF-" + "X".repeat(500);
        producto.setReferenciaExterna(longRef);
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);

        Producto result = productoDAO.findById(testUUID);

        assertNotNull(result);
        assertTrue(result.getReferenciaExterna().length() > 500);
        verify(entityManager).find(Producto.class, testUUID);
    }

    @Test
    void testFindById_WithLongComentarios() {
        String longComentarios = "Comentario muy largo: " + "X".repeat(1000);
        producto.setComentarios(longComentarios);
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);

        Producto result = productoDAO.findById(testUUID);

        assertNotNull(result);
        assertTrue(result.getComentarios().length() > 1000);
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
    void testFindById_WithNullActivo() {
        producto.setActivo(null);
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);

        Producto result = productoDAO.findById(testUUID);

        assertNotNull(result);
        assertNull(result.getActivo());
        verify(entityManager).find(Producto.class, testUUID);
    }

    @Test
    void testFindById_WithSpecialCharactersInNombreProducto() {
        producto.setNombreProducto("Computadora 15\" Core i7 - 2023");
        when(entityManager.find(Producto.class, testUUID)).thenReturn(producto);

        Producto result = productoDAO.findById(testUUID);

        assertNotNull(result);
        assertEquals("Computadora 15\" Core i7 - 2023", result.getNombreProducto());
        verify(entityManager).find(Producto.class, testUUID);
    }

    @Test
    void testCrear_Success() {
        Producto newProducto = new Producto();
        newProducto.setNombreProducto("Nuevo Producto");
        newProducto.setActivo(true);

        doNothing().when(entityManager).persist(newProducto);

        productoDAO.crear(newProducto);

        assertNotNull(newProducto.getId());
        verify(entityManager).persist(newProducto);
    }

    @Test
    void testCrear_WithExistingId() {
        producto.setId(testUUID);
        doNothing().when(entityManager).persist(producto);

        productoDAO.crear(producto);

        assertEquals(testUUID, producto.getId());
        verify(entityManager).persist(producto);
    }

    @Test
    void testCrear_NullProducto() {
        assertThrows(IllegalArgumentException.class, () -> {
            productoDAO.crear(null);
        });
        verify(entityManager, never()).persist(any());
    }

    @Test
    void testCrear_Exception() {
        Producto newProducto = new Producto();
        newProducto.setNombreProducto("Producto con Error");

        doThrow(new RuntimeException("Database error"))
                .when(entityManager).persist(newProducto);

        assertThrows(IllegalStateException.class, () -> productoDAO.crear(newProducto));
        verify(entityManager).persist(newProducto);
    }
}