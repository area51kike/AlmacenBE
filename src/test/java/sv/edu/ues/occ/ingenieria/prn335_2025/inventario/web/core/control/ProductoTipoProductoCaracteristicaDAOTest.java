package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProducto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProductoCaracteristica;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProductoCaracteristica;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoTipoProductoCaracteristicaDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<ProductoTipoProductoCaracteristica> typedQuery;

    @Mock
    private TypedQuery<Long> longTypedQuery;

    @InjectMocks
    private ProductoTipoProductoCaracteristicaDAO dao;

    private ProductoTipoProductoCaracteristica testEntity;
    private UUID testUUID;
    private UUID testPTPId;
    private Long testTPCId;

    @BeforeEach
    void setUp() throws Exception {
        // Inyectar el EntityManager manualmente usando reflexión
        Field emField = ProductoTipoProductoCaracteristicaDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(dao, entityManager);

        // Inicializar datos de prueba
        testUUID = UUID.randomUUID();
        testPTPId = UUID.randomUUID();
        testTPCId = 1L;

        // Crear entidades relacionadas
        ProductoTipoProducto ptp = new ProductoTipoProducto();
        ptp.setId(testPTPId);

        TipoProductoCaracteristica tpc = new TipoProductoCaracteristica();
        tpc.setId(testTPCId);

        Caracteristica caracteristica = new Caracteristica();
        caracteristica.setNombre("Color");
        tpc.setCaracteristica(caracteristica);

        // Crear entidad de prueba
        testEntity = new ProductoTipoProductoCaracteristica();
        testEntity.setId(testUUID);
        testEntity.setIdProductoTipoProducto(ptp);
        testEntity.setIdTipoProductoCaracteristica(tpc);
        testEntity.setValor("Rojo");
    }

    @Test
    void testGetEntityManager() {
        EntityManager em = dao.getEntityManager();
        assertNotNull(em);
        assertEquals(entityManager, em);
    }

    @Test
    void testFindByIdProductoTipoProducto_Success() {
        // Arrange
        List<ProductoTipoProductoCaracteristica> expectedList = new ArrayList<>();
        expectedList.add(testEntity);

        when(entityManager.createQuery(anyString(), eq(ProductoTipoProductoCaracteristica.class)))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idPTP"), any(UUID.class)))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<ProductoTipoProductoCaracteristica> result = dao.findByIdProductoTipoProducto(testPTPId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEntity.getId(), result.get(0).getId());
        verify(entityManager, times(1)).createQuery(anyString(), eq(ProductoTipoProductoCaracteristica.class));
        verify(typedQuery, times(1)).setParameter("idPTP", testPTPId);
    }

    @Test
    void testFindByIdProductoTipoProducto_EmptyList() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(ProductoTipoProductoCaracteristica.class)))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idPTP"), any(UUID.class)))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(new ArrayList<>());

        // Act
        List<ProductoTipoProductoCaracteristica> result = dao.findByIdProductoTipoProducto(testPTPId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByIdProductoTipoProducto_Exception() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(ProductoTipoProductoCaracteristica.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        List<ProductoTipoProductoCaracteristica> result = dao.findByIdProductoTipoProducto(testPTPId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindById_Success() {
        // Arrange
        when(entityManager.find(ProductoTipoProductoCaracteristica.class, testUUID))
                .thenReturn(testEntity);

        // Act
        ProductoTipoProductoCaracteristica result = dao.findById(testUUID);

        // Assert
        assertNotNull(result);
        assertEquals(testUUID, result.getId());
        assertEquals("Rojo", result.getValor());
        verify(entityManager, times(1)).find(ProductoTipoProductoCaracteristica.class, testUUID);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(entityManager.find(ProductoTipoProductoCaracteristica.class, testUUID))
                .thenReturn(null);

        // Act
        ProductoTipoProductoCaracteristica result = dao.findById(testUUID);

        // Assert
        assertNull(result);
    }

    @Test
    void testFindById_Exception() {
        // Arrange
        when(entityManager.find(ProductoTipoProductoCaracteristica.class, testUUID))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ProductoTipoProductoCaracteristica result = dao.findById(testUUID);

        // Assert
        assertNull(result);
    }

    @Test
    void testExisteCaracteristica_True() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(Long.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(eq("idPTP"), any(UUID.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(eq("idTPC"), any(Long.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(1L);

        // Act
        boolean result = dao.existeCaracteristica(testPTPId, testTPCId);

        // Assert
        assertTrue(result);
        verify(longTypedQuery, times(1)).setParameter("idPTP", testPTPId);
        verify(longTypedQuery, times(1)).setParameter("idTPC", testTPCId);
    }

    @Test
    void testExisteCaracteristica_False() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(Long.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(eq("idPTP"), any(UUID.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter(eq("idTPC"), any(Long.class)))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(0L);

        // Act
        boolean result = dao.existeCaracteristica(testPTPId, testTPCId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testExisteCaracteristica_Exception() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(Long.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        boolean result = dao.existeCaracteristica(testPTPId, testTPCId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testCrear_Success() {
        // Arrange
        doNothing().when(entityManager).persist(any(ProductoTipoProductoCaracteristica.class));

        // Act
        dao.crear(testEntity);

        // Assert
        verify(entityManager, times(1)).persist(testEntity);
    }

    @Test
    void testModificar_Success() {
        // Arrange
        when(entityManager.merge(any(ProductoTipoProductoCaracteristica.class)))
                .thenReturn(testEntity);

        // Act
        ProductoTipoProductoCaracteristica result = dao.modificar(testEntity);

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
        doNothing().when(entityManager).remove(any(ProductoTipoProductoCaracteristica.class));

        // Act
        dao.eliminar(testEntity);

        // Assert
        verify(entityManager, times(1)).merge(testEntity);
        verify(entityManager, times(1)).remove(any(ProductoTipoProductoCaracteristica.class));
    }

    @Test
    void testGetEntityClass() {
        // Act
        Class<ProductoTipoProductoCaracteristica> entityClass = dao.getEntityClass();

        // Assert
        assertNotNull(entityClass);
        assertEquals(ProductoTipoProductoCaracteristica.class, entityClass);
    }
}