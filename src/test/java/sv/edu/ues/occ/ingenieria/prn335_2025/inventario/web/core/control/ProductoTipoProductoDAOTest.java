package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProducto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProducto;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoTipoProductoDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<ProductoTipoProducto> typedQuery;

    @InjectMocks
    private ProductoTipoProductoDAO dao;

    private ProductoTipoProducto testEntity;
    private UUID testId;
    private UUID testProductoId;

    @BeforeEach
    void setUp() throws Exception {
        // Inyectar el EntityManager manualmente usando reflexión
        Field emField = ProductoTipoProductoDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(dao, entityManager);

        // Inicializar datos de prueba
        testId = UUID.randomUUID();
        testProductoId = UUID.randomUUID();

        // Crear entidades relacionadas
        Producto producto = new Producto();
        producto.setId(testProductoId);
        producto.setNombreProducto("Producto Test");
        producto.setActivo(true);

        TipoProducto tipoProducto = new TipoProducto();
        tipoProducto.setId(1L);
        tipoProducto.setNombre("Tipo Test");

        // Crear entidad de prueba
        testEntity = new ProductoTipoProducto();
        testEntity.setId(testId);
        testEntity.setIdProducto(producto);
        testEntity.setIdTipoProducto(tipoProducto);
        testEntity.setFechaCreacion(OffsetDateTime.now());
        testEntity.setActivo(true);
        testEntity.setObservaciones("Test observaciones");
    }

    @Test
    void testGetEntityManager() {
        // Act
        EntityManager em = dao.getEntityManager();

        // Assert
        assertNotNull(em);
        assertEquals(entityManager, em);
    }

    @Test
    void testGetEntityClass() {
        // Act
        Class<ProductoTipoProducto> entityClass = dao.getEntityClass();

        // Assert
        assertNotNull(entityClass);
        assertEquals(ProductoTipoProducto.class, entityClass);
    }

    @Test
    void testFindById_Success() {
        // Arrange
        when(entityManager.find(ProductoTipoProducto.class, testId))
                .thenReturn(testEntity);

        // Act
        ProductoTipoProducto result = dao.findById(testId);

        // Assert
        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals(testProductoId, result.getIdProducto().getId());
        assertEquals("Test observaciones", result.getObservaciones());
        verify(entityManager, times(1)).find(ProductoTipoProducto.class, testId);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(entityManager.find(ProductoTipoProducto.class, testId))
                .thenReturn(null);

        // Act
        ProductoTipoProducto result = dao.findById(testId);

        // Assert
        assertNull(result);
        verify(entityManager, times(1)).find(ProductoTipoProducto.class, testId);
    }

    @Test
    void testFindById_NullId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dao.findById(null)
        );

        assertEquals("El ID no puede ser nulo", exception.getMessage());
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void testFindById_EntityManagerNull() throws Exception {
        // Arrange - Set EntityManager to null
        Field emField = ProductoTipoProductoDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(dao, null);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dao.findById(testId)
        );

        assertTrue(exception.getMessage().contains("Error al buscar el registro por ID"));
    }

    @Test
    void testFindById_ExceptionThrown() {
        // Arrange
        when(entityManager.find(ProductoTipoProducto.class, testId))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dao.findById(testId)
        );

        assertTrue(exception.getMessage().contains("Error al buscar el registro por ID"));
        assertNotNull(exception.getCause());
    }

    @Test
    void testFindByid_Success() {
        // Arrange
        List<ProductoTipoProducto> expectedList = new ArrayList<>();
        expectedList.add(testEntity);

        when(entityManager.createQuery(anyString(), eq(ProductoTipoProducto.class)))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idProducto"), any(UUID.class)))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<ProductoTipoProducto> result = dao.findByid(testProductoId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testId, result.get(0).getId());
        assertEquals(testProductoId, result.get(0).getIdProducto().getId());

        verify(entityManager, times(1))
                .createQuery(anyString(), eq(ProductoTipoProducto.class));
        verify(typedQuery, times(1)).setParameter("idProducto", testProductoId);
        verify(typedQuery, times(1)).getResultList();
    }

    @Test
    void testFindByid_EmptyList() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(ProductoTipoProducto.class)))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idProducto"), any(UUID.class)))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(new ArrayList<>());

        // Act
        List<ProductoTipoProducto> result = dao.findByid(testProductoId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByid_MultipleResults() {
        // Arrange
        ProductoTipoProducto entity2 = new ProductoTipoProducto();
        entity2.setId(UUID.randomUUID());
        entity2.setIdProducto(testEntity.getIdProducto());

        List<ProductoTipoProducto> expectedList = new ArrayList<>();
        expectedList.add(testEntity);
        expectedList.add(entity2);

        when(entityManager.createQuery(anyString(), eq(ProductoTipoProducto.class)))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idProducto"), any(UUID.class)))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<ProductoTipoProducto> result = dao.findByid(testProductoId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testFindByid_NullId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dao.findByid(null)
        );

        assertEquals("El ID del producto no puede ser nulo", exception.getMessage());
        verify(entityManager, never()).createQuery(anyString(), any());
    }

    @Test
    void testFindByid_EntityManagerNull() throws Exception {
        // Arrange - Set EntityManager to null
        Field emField = ProductoTipoProductoDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(dao, null);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dao.findByid(testProductoId)
        );

        assertTrue(exception.getMessage().contains("Error al buscar relaciones por producto"));
    }

    @Test
    void testFindByid_ExceptionThrown() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(ProductoTipoProducto.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dao.findByid(testProductoId)
        );

        assertTrue(exception.getMessage().contains("Error al buscar relaciones por producto"));
        assertNotNull(exception.getCause());
    }

    @Test
    void testCrear_Success() {
        // Arrange
        doNothing().when(entityManager).persist(any(ProductoTipoProducto.class));

        // Act
        dao.crear(testEntity);

        // Assert
        verify(entityManager, times(1)).persist(testEntity);
    }

    @Test
    void testModificar_Success() {
        // Arrange
        when(entityManager.merge(any(ProductoTipoProducto.class)))
                .thenReturn(testEntity);

        // Act
        ProductoTipoProducto result = dao.modificar(testEntity);

        // Assert
        assertNotNull(result);
        assertEquals(testEntity.getId(), result.getId());
        verify(entityManager, times(1)).merge(testEntity);
    }

    @Test
    void testEliminar_Success() {
        // Arrange
        when(entityManager.contains(testEntity)).thenReturn(false);
        when(entityManager.merge(testEntity)).thenReturn(testEntity);
        doNothing().when(entityManager).remove(any(ProductoTipoProducto.class));

        // Act
        dao.eliminar(testEntity);

        // Assert
        verify(entityManager, times(1)).merge(testEntity);
        verify(entityManager, times(1)).remove(any(ProductoTipoProducto.class));
    }

    @Test
    void testEliminar_EntityAttached() {
        // Arrange
        when(entityManager.contains(testEntity)).thenReturn(true);
        doNothing().when(entityManager).remove(any(ProductoTipoProducto.class));

        // Act
        dao.eliminar(testEntity);

        // Assert
        verify(entityManager, never()).merge(any());
        verify(entityManager, times(1)).remove(testEntity);
    }
}