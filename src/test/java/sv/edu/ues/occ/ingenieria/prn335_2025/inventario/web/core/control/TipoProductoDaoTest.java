package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProducto;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoProductoDaoTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<TipoProducto> typedQuery;

    @InjectMocks
    private TipoProductoDao tipoProductoDao;

    private TipoProducto tipoProducto;

    @BeforeEach
    void setUp() {
        tipoProductoDao = new TipoProductoDao();
        tipoProductoDao.em = entityManager;

        tipoProducto = new TipoProducto();
        tipoProducto.setId(1L);
        tipoProducto.setNombre("Electrónicos");
        tipoProducto.setActivo(true);
        tipoProducto.setComentarios("Productos electrónicos");
    }

    @Test
    void testGetEntityManager() {
        EntityManager result = tipoProductoDao.getEntityManager();
        assertNotNull(result);
        assertEquals(entityManager, result);
    }

    @Test
    void testConstructor() {
        TipoProductoDao dao = new TipoProductoDao();
        assertNotNull(dao);
    }

    @Test
    void testFindAll_Success() {
        List<TipoProducto> expectedList = new ArrayList<>();
        expectedList.add(tipoProducto);

        TipoProducto tipo2 = new TipoProducto();
        tipo2.setId(2L);
        tipo2.setNombre("Ropa");
        expectedList.add(tipo2);

        when(entityManager.createQuery(anyString(), eq(TipoProducto.class)))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<TipoProducto> result = tipoProductoDao.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Electrónicos", result.get(0).getNombre());
        assertEquals("Ropa", result.get(1).getNombre());

        verify(entityManager).createQuery(
                "SELECT t FROM TipoProducto t ORDER BY t.nombre",
                TipoProducto.class
        );
        verify(typedQuery).getResultList();
    }

    @Test
    void testFindAll_EmptyList() {
        List<TipoProducto> emptyList = new ArrayList<>();

        when(entityManager.createQuery(anyString(), eq(TipoProducto.class)))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(emptyList);

        List<TipoProducto> result = tipoProductoDao.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAll_Exception() {
        when(entityManager.createQuery(anyString(), eq(TipoProducto.class)))
                .thenThrow(new RuntimeException("Database error"));

        List<TipoProducto> result = tipoProductoDao.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindById_Success() {
        Long id = 1L;
        when(entityManager.find(TipoProducto.class, id)).thenReturn(tipoProducto);

        TipoProducto result = tipoProductoDao.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Electrónicos", result.getNombre());
        verify(entityManager).find(TipoProducto.class, id);
    }

    @Test
    void testFindById_NotFound() {
        Long id = 999L;
        when(entityManager.find(TipoProducto.class, id)).thenReturn(null);

        TipoProducto result = tipoProductoDao.findById(id);

        assertNull(result);
        verify(entityManager).find(TipoProducto.class, id);
    }

    @Test
    void testFindById_NullId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tipoProductoDao.findById(null)
        );

        assertEquals("El ID no puede ser nulo", exception.getMessage());
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void testFindById_EntityManagerNull() {
        tipoProductoDao.em = null;
        Long id = 1L;

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tipoProductoDao.findById(id)
        );

        assertEquals("EntityManager no disponible", exception.getMessage());
    }

    @Test
    void testFindById_ErrorGeneral() {
        // Configura un escenario donde em.find() lance una excepción
        // Por ejemplo, usando Mockito para simular una excepción en find()
    }

    @Test
    void testFindById_Exception() {
        Long id = 1L;
        when(entityManager.find(TipoProducto.class, id))
                .thenThrow(new RuntimeException("Database connection error"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tipoProductoDao.findById(id)
        );

        assertEquals("Error al buscar el registro por ID", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void testFindById_WithParentType() {
        TipoProducto padre = new TipoProducto();
        padre.setId(2L);
        padre.setNombre("Categoría Padre");

        tipoProducto.setIdTipoProductoPadre(padre);

        when(entityManager.find(TipoProducto.class, 1L)).thenReturn(tipoProducto);

        TipoProducto result = tipoProductoDao.findById(1L);

        assertNotNull(result);
        assertNotNull(result.getIdTipoProductoPadre());
        assertEquals(2L, result.getIdTipoProductoPadre().getId());
    }

    @Test
    void testFindAll_OrderedByName() {
        List<TipoProducto> expectedList = new ArrayList<>();

        TipoProducto tipo1 = new TipoProducto();
        tipo1.setId(1L);
        tipo1.setNombre("A - Primero");

        TipoProducto tipo2 = new TipoProducto();
        tipo2.setId(2L);
        tipo2.setNombre("B - Segundo");

        TipoProducto tipo3 = new TipoProducto();
        tipo3.setId(3L);
        tipo3.setNombre("C - Tercero");

        expectedList.add(tipo1);
        expectedList.add(tipo2);
        expectedList.add(tipo3);

        when(entityManager.createQuery(anyString(), eq(TipoProducto.class)))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<TipoProducto> result = tipoProductoDao.findAll();

        assertEquals(3, result.size());
        assertEquals("A - Primero", result.get(0).getNombre());
        assertEquals("B - Segundo", result.get(1).getNombre());
        assertEquals("C - Tercero", result.get(2).getNombre());
    }
}