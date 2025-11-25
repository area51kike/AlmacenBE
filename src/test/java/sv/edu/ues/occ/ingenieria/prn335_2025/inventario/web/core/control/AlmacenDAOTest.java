package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Almacen;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlmacenDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Almacen> typedQuery;

    @InjectMocks
    private AlmacenDAO almacenDAO;

    private Almacen almacen;
    private TipoAlmacen tipoAlmacen;

    @BeforeEach
    void setUp() {
        almacenDAO = new AlmacenDAO();
        almacenDAO.em = entityManager;

        // Configurar TipoAlmacen
        tipoAlmacen = new TipoAlmacen();
        tipoAlmacen.setId(1);
        tipoAlmacen.setNombre("Almacén Principal");

        // Configurar Almacen
        almacen = new Almacen();
        almacen.setId(1);
        almacen.setActivo(true);
        almacen.setObservaciones("Almacén principal");
        almacen.setIdTipoAlmacen(tipoAlmacen);
    }

    @Test
    void testGetEntityManager_ReturnsEntityManager() {
        EntityManager result = almacenDAO.getEntityManager();
        assertNotNull(result);
        assertEquals(entityManager, result);
    }

    @Test
    void testConstructor_CreatesInstance() {
        AlmacenDAO dao = new AlmacenDAO();
        assertNotNull(dao);
    }

    @Test
    void testFindById_WithValidId_ReturnsAlmacen() {
        Integer id = 1;
        when(entityManager.find(Almacen.class, id)).thenReturn(almacen);

        Almacen result = almacenDAO.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Almacén principal", result.getObservaciones());
        assertTrue(result.getActivo());
        assertNotNull(result.getIdTipoAlmacen());
        verify(entityManager).find(Almacen.class, id);
    }

    @Test
    void testFindById_WithNonExistentId_ReturnsNull() {
        Integer id = 999;
        when(entityManager.find(Almacen.class, id)).thenReturn(null);

        Almacen result = almacenDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Almacen.class, id);
    }

    @Test
    void testFindById_WithNullId_ReturnsNull() {
        Almacen result = almacenDAO.findById(null);

        assertNull(result);
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void testFindById_WithZeroId_ReturnsNull() {
        Integer id = 0;
        when(entityManager.find(Almacen.class, id)).thenReturn(null);

        Almacen result = almacenDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Almacen.class, id);
    }

    @Test
    void testFindById_WithNegativeId_ReturnsNull() {
        Integer id = -1;
        when(entityManager.find(Almacen.class, id)).thenReturn(null);

        Almacen result = almacenDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Almacen.class, id);
    }

    @Test
    void testFindById_WithException_ReturnsNull() {
        Integer id = 1;
        when(entityManager.find(Almacen.class, id)).thenThrow(new RuntimeException("Database error"));

        Almacen result = almacenDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Almacen.class, id);
    }

    @Test
    void testFindById_WithInactiveAlmacen_ReturnsAlmacen() {
        almacen.setActivo(false);
        Integer id = 1;

        when(entityManager.find(Almacen.class, id)).thenReturn(almacen);

        Almacen result = almacenDAO.findById(id);

        assertNotNull(result);
        assertFalse(result.getActivo());
        verify(entityManager).find(Almacen.class, id);
    }

    @Test
    void testFindById_WithNullObservaciones_ReturnsAlmacen() {
        almacen.setObservaciones(null);
        Integer id = 1;

        when(entityManager.find(Almacen.class, id)).thenReturn(almacen);

        Almacen result = almacenDAO.findById(id);

        assertNotNull(result);
        assertNull(result.getObservaciones());
        verify(entityManager).find(Almacen.class, id);
    }

    @Test
    void testFindById_WithEmptyObservaciones_ReturnsAlmacen() {
        almacen.setObservaciones("");
        Integer id = 1;

        when(entityManager.find(Almacen.class, id)).thenReturn(almacen);

        Almacen result = almacenDAO.findById(id);

        assertNotNull(result);
        assertEquals("", result.getObservaciones());
        verify(entityManager).find(Almacen.class, id);
    }

    @Test
    void testFindById_WithNullTipoAlmacen_ReturnsAlmacen() {
        almacen.setIdTipoAlmacen(null);
        Integer id = 1;

        when(entityManager.find(Almacen.class, id)).thenReturn(almacen);

        Almacen result = almacenDAO.findById(id);

        assertNotNull(result);
        assertNull(result.getIdTipoAlmacen());
        verify(entityManager).find(Almacen.class, id);
    }


    @Test
    void testFindByTipoAlmacen_WithValidId_ReturnsListOfAlmacenes() {
        Integer idTipoAlmacen = 1;
        String expectedQuery = "SELECT a FROM Almacen a WHERE a.idTipoAlmacen.id = :idTipoAlmacen";

        Almacen almacen2 = new Almacen();
        almacen2.setId(2);
        almacen2.setActivo(true);
        almacen2.setIdTipoAlmacen(tipoAlmacen);

        List<Almacen> expectedList = Arrays.asList(almacen, almacen2);

        when(entityManager.createQuery(eq(expectedQuery), eq(Almacen.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idTipoAlmacen"), eq(idTipoAlmacen))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<Almacen> result = almacenDAO.findByTipoAlmacen(idTipoAlmacen);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
        verify(entityManager).createQuery(eq(expectedQuery), eq(Almacen.class));
        verify(typedQuery).setParameter("idTipoAlmacen", idTipoAlmacen);
        verify(typedQuery).getResultList();
    }

    @Test
    void testFindByTipoAlmacen_WithNoResults_ReturnsEmptyList() {
        Integer idTipoAlmacen = 999;
        String expectedQuery = "SELECT a FROM Almacen a WHERE a.idTipoAlmacen.id = :idTipoAlmacen";

        when(entityManager.createQuery(eq(expectedQuery), eq(Almacen.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idTipoAlmacen"), eq(idTipoAlmacen))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of());

        List<Almacen> result = almacenDAO.findByTipoAlmacen(idTipoAlmacen);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(entityManager).createQuery(eq(expectedQuery), eq(Almacen.class));
    }

    @Test
    void testFindByTipoAlmacen_WithNullId_ReturnsEmptyList() {
        List<Almacen> result = almacenDAO.findByTipoAlmacen(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(entityManager, never()).createQuery(anyString(), any());
    }

    @Test
    void testFindByTipoAlmacen_WithException_ReturnsEmptyList() {
        Integer idTipoAlmacen = 1;
        String expectedQuery = "SELECT a FROM Almacen a WHERE a.idTipoAlmacen.id = :idTipoAlmacen";

        when(entityManager.createQuery(eq(expectedQuery), eq(Almacen.class)))
                .thenThrow(new RuntimeException("Database error"));

        List<Almacen> result = almacenDAO.findByTipoAlmacen(idTipoAlmacen);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(entityManager).createQuery(eq(expectedQuery), eq(Almacen.class));
    }

    @Test
    void testFindByTipoAlmacen_WithSingleResult_ReturnsListWithOneElement() {
        Integer idTipoAlmacen = 1;
        String expectedQuery = "SELECT a FROM Almacen a WHERE a.idTipoAlmacen.id = :idTipoAlmacen";
        List<Almacen> expectedList = List.of(almacen);

        when(entityManager.createQuery(eq(expectedQuery), eq(Almacen.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idTipoAlmacen"), eq(idTipoAlmacen))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<Almacen> result = almacenDAO.findByTipoAlmacen(idTipoAlmacen);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(almacen.getId(), result.get(0).getId());
    }

    @Test
    void testFindByTipoAlmacen_VerifiesQueryString() {
        Integer idTipoAlmacen = 1;
        String expectedQuery = "SELECT a FROM Almacen a WHERE a.idTipoAlmacen.id = :idTipoAlmacen";

        when(entityManager.createQuery(eq(expectedQuery), eq(Almacen.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idTipoAlmacen"), eq(idTipoAlmacen))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of());

        almacenDAO.findByTipoAlmacen(idTipoAlmacen);

        verify(entityManager).createQuery(expectedQuery, Almacen.class);
    }

    @Test
    void testFindByTipoAlmacen_WithMixedActiveStatus_ReturnsAllAlmacenes() {
        Integer idTipoAlmacen = 1;
        String expectedQuery = "SELECT a FROM Almacen a WHERE a.idTipoAlmacen.id = :idTipoAlmacen";

        Almacen almacenInactivo = new Almacen();
        almacenInactivo.setId(3);
        almacenInactivo.setActivo(false);
        almacenInactivo.setIdTipoAlmacen(tipoAlmacen);

        List<Almacen> expectedList = Arrays.asList(almacen, almacenInactivo);

        when(entityManager.createQuery(eq(expectedQuery), eq(Almacen.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idTipoAlmacen"), eq(idTipoAlmacen))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<Almacen> result = almacenDAO.findByTipoAlmacen(idTipoAlmacen);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getActivo());
        assertFalse(result.get(1).getActivo());
    }
}