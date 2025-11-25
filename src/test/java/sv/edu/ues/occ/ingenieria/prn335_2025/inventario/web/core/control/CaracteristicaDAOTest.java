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
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoUnidadMedida;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaracteristicaDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Caracteristica> typedQuery;

    @InjectMocks
    private CaracteristicaDAO caracteristicaDAO;

    private Caracteristica caracteristica;
    private TipoUnidadMedida tipoUnidadMedida;

    @BeforeEach
    void setUp() {
        caracteristicaDAO = new CaracteristicaDAO();
        caracteristicaDAO.em = entityManager;

        tipoUnidadMedida = new TipoUnidadMedida();
        tipoUnidadMedida.setId(1);
        tipoUnidadMedida.setNombre("Kilogramos");

        caracteristica = new Caracteristica();
        caracteristica.setId(1);
        caracteristica.setNombre("Peso");
        caracteristica.setDescripcion("Peso del producto");
        caracteristica.setActivo(true);
        caracteristica.setIdTipoUnidadMedida(tipoUnidadMedida);
    }

    @Test
    void testGetEntityManager_ReturnsEntityManager() {
        EntityManager result = caracteristicaDAO.getEntityManager();
        assertNotNull(result);
        assertEquals(entityManager, result);
    }

    @Test
    void testConstructor_CreatesInstance() {
        CaracteristicaDAO dao = new CaracteristicaDAO();
        assertNotNull(dao);
    }

    @Test
    void testFindById_WithValidId_ReturnsCaracteristica() {
        Integer id = 1;
        when(entityManager.find(Caracteristica.class, id)).thenReturn(caracteristica);

        Caracteristica result = caracteristicaDAO.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Peso", result.getNombre());
        assertEquals("Peso del producto", result.getDescripcion());
        assertTrue(result.getActivo());
        assertNotNull(result.getIdTipoUnidadMedida());
        verify(entityManager).find(Caracteristica.class, id);
    }

    @Test
    void testFindById_WithNonExistentId_ReturnsNull() {
        Integer id = 999;
        when(entityManager.find(Caracteristica.class, id)).thenReturn(null);

        Caracteristica result = caracteristicaDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Caracteristica.class, id);
    }

    @Test
    void testFindById_WithNullId_ReturnsNull() {
        Caracteristica result = caracteristicaDAO.findById(null);

        assertNull(result);
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void testFindById_WithZeroId_ReturnsNull() {
        Integer id = 0;
        when(entityManager.find(Caracteristica.class, id)).thenReturn(null);

        Caracteristica result = caracteristicaDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Caracteristica.class, id);
    }

    @Test
    void testFindById_WithNegativeId_ReturnsNull() {
        Integer id = -1;
        when(entityManager.find(Caracteristica.class, id)).thenReturn(null);

        Caracteristica result = caracteristicaDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Caracteristica.class, id);
    }

    @Test
    void testFindById_WithException_ReturnsNull() {
        Integer id = 1;
        when(entityManager.find(Caracteristica.class, id)).thenThrow(new RuntimeException("Database error"));

        Caracteristica result = caracteristicaDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Caracteristica.class, id);
    }

    @Test
    void testFindById_WithInactiveCaracteristica_ReturnsCaracteristica() {
        caracteristica.setActivo(false);
        Integer id = 1;

        when(entityManager.find(Caracteristica.class, id)).thenReturn(caracteristica);

        Caracteristica result = caracteristicaDAO.findById(id);

        assertNotNull(result);
        assertFalse(result.getActivo());
        verify(entityManager).find(Caracteristica.class, id);
    }

    @Test
    void testFindById_WithNullNombre_ReturnsCaracteristica() {
        caracteristica.setNombre(null);
        Integer id = 1;

        when(entityManager.find(Caracteristica.class, id)).thenReturn(caracteristica);

        Caracteristica result = caracteristicaDAO.findById(id);

        assertNotNull(result);
        assertNull(result.getNombre());
        verify(entityManager).find(Caracteristica.class, id);
    }

    @Test
    void testFindById_WithEmptyNombre_ReturnsCaracteristica() {
        caracteristica.setNombre("");
        Integer id = 1;

        when(entityManager.find(Caracteristica.class, id)).thenReturn(caracteristica);

        Caracteristica result = caracteristicaDAO.findById(id);

        assertNotNull(result);
        assertEquals("", result.getNombre());
        verify(entityManager).find(Caracteristica.class, id);
    }

    @Test
    void testFindById_WithNullDescripcion_ReturnsCaracteristica() {
        caracteristica.setDescripcion(null);
        Integer id = 1;

        when(entityManager.find(Caracteristica.class, id)).thenReturn(caracteristica);

        Caracteristica result = caracteristicaDAO.findById(id);

        assertNotNull(result);
        assertNull(result.getDescripcion());
        verify(entityManager).find(Caracteristica.class, id);
    }

    @Test
    void testFindById_WithNullTipoUnidadMedida_ReturnsCaracteristica() {
        caracteristica.setIdTipoUnidadMedida(null);
        Integer id = 1;

        when(entityManager.find(Caracteristica.class, id)).thenReturn(caracteristica);

        Caracteristica result = caracteristicaDAO.findById(id);

        assertNotNull(result);
        assertNull(result.getIdTipoUnidadMedida());
        verify(entityManager).find(Caracteristica.class, id);
    }

    @Test
    void testFindById_WithLongNombre_ReturnsCaracteristica() {
        String longNombre = "A".repeat(5000);
        caracteristica.setNombre(longNombre);
        Integer id = 1;

        when(entityManager.find(Caracteristica.class, id)).thenReturn(caracteristica);

        Caracteristica result = caracteristicaDAO.findById(id);

        assertNotNull(result);
        assertEquals(5000, result.getNombre().length());
        verify(entityManager).find(Caracteristica.class, id);
    }

    @Test
    void testFindById_WithLongDescripcion_ReturnsCaracteristica() {
        String longDescripcion = "B".repeat(10000);
        caracteristica.setDescripcion(longDescripcion);
        Integer id = 1;

        when(entityManager.find(Caracteristica.class, id)).thenReturn(caracteristica);

        Caracteristica result = caracteristicaDAO.findById(id);

        assertNotNull(result);
        assertEquals(10000, result.getDescripcion().length());
        verify(entityManager).find(Caracteristica.class, id);
    }

    @Test
    void testFindByTipoUnidadMedida_WithValidId_ReturnsListOfCaracteristicas() {
        Integer idTipoUnidadMedida = 1;
        String expectedQuery = "SELECT c FROM Caracteristica c WHERE c.idTipoUnidadMedida.id = :idTipoUnidadMedida";

        Caracteristica caracteristica2 = new Caracteristica();
        caracteristica2.setId(2);
        caracteristica2.setNombre("Volumen");
        caracteristica2.setActivo(true);
        caracteristica2.setIdTipoUnidadMedida(tipoUnidadMedida);

        List<Caracteristica> expectedList = Arrays.asList(caracteristica, caracteristica2);

        when(entityManager.createQuery(eq(expectedQuery), eq(Caracteristica.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idTipoUnidadMedida"), eq(idTipoUnidadMedida))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<Caracteristica> result = caracteristicaDAO.findByTipoUnidadMedida(idTipoUnidadMedida);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Peso", result.get(0).getNombre());
        assertEquals(2, result.get(1).getId());
        assertEquals("Volumen", result.get(1).getNombre());
        verify(entityManager).createQuery(eq(expectedQuery), eq(Caracteristica.class));
        verify(typedQuery).setParameter("idTipoUnidadMedida", idTipoUnidadMedida);
        verify(typedQuery).getResultList();
    }

    @Test
    void testFindByTipoUnidadMedida_WithNoResults_ReturnsEmptyList() {
        Integer idTipoUnidadMedida = 999;
        String expectedQuery = "SELECT c FROM Caracteristica c WHERE c.idTipoUnidadMedida.id = :idTipoUnidadMedida";

        when(entityManager.createQuery(eq(expectedQuery), eq(Caracteristica.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idTipoUnidadMedida"), eq(idTipoUnidadMedida))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of());

        List<Caracteristica> result = caracteristicaDAO.findByTipoUnidadMedida(idTipoUnidadMedida);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(entityManager).createQuery(eq(expectedQuery), eq(Caracteristica.class));
    }

    @Test
    void testFindByTipoUnidadMedida_WithNullId_ReturnsEmptyList() {
        List<Caracteristica> result = caracteristicaDAO.findByTipoUnidadMedida(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(entityManager, never()).createQuery(anyString(), any());
    }

    @Test
    void testFindByTipoUnidadMedida_WithException_ReturnsEmptyList() {
        Integer idTipoUnidadMedida = 1;
        String expectedQuery = "SELECT c FROM Caracteristica c WHERE c.idTipoUnidadMedida.id = :idTipoUnidadMedida";

        when(entityManager.createQuery(eq(expectedQuery), eq(Caracteristica.class)))
                .thenThrow(new RuntimeException("Database error"));

        List<Caracteristica> result = caracteristicaDAO.findByTipoUnidadMedida(idTipoUnidadMedida);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(entityManager).createQuery(eq(expectedQuery), eq(Caracteristica.class));
    }

    @Test
    void testFindByTipoUnidadMedida_WithSingleResult_ReturnsListWithOneElement() {
        Integer idTipoUnidadMedida = 1;
        String expectedQuery = "SELECT c FROM Caracteristica c WHERE c.idTipoUnidadMedida.id = :idTipoUnidadMedida";
        List<Caracteristica> expectedList = List.of(caracteristica);

        when(entityManager.createQuery(eq(expectedQuery), eq(Caracteristica.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idTipoUnidadMedida"), eq(idTipoUnidadMedida))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<Caracteristica> result = caracteristicaDAO.findByTipoUnidadMedida(idTipoUnidadMedida);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(caracteristica.getId(), result.get(0).getId());
    }

    @Test
    void testFindByTipoUnidadMedida_VerifiesQueryString() {
        Integer idTipoUnidadMedida = 1;
        String expectedQuery = "SELECT c FROM Caracteristica c WHERE c.idTipoUnidadMedida.id = :idTipoUnidadMedida";

        when(entityManager.createQuery(eq(expectedQuery), eq(Caracteristica.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idTipoUnidadMedida"), eq(idTipoUnidadMedida))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of());

        caracteristicaDAO.findByTipoUnidadMedida(idTipoUnidadMedida);

        verify(entityManager).createQuery(expectedQuery, Caracteristica.class);
    }

    @Test
    void testFindByTipoUnidadMedida_WithMixedActiveStatus_ReturnsAllCaracteristicas() {
        Integer idTipoUnidadMedida = 1;
        String expectedQuery = "SELECT c FROM Caracteristica c WHERE c.idTipoUnidadMedida.id = :idTipoUnidadMedida";

        Caracteristica caracteristicaInactiva = new Caracteristica();
        caracteristicaInactiva.setId(3);
        caracteristicaInactiva.setNombre("Altura");
        caracteristicaInactiva.setActivo(false);
        caracteristicaInactiva.setIdTipoUnidadMedida(tipoUnidadMedida);

        List<Caracteristica> expectedList = Arrays.asList(caracteristica, caracteristicaInactiva);

        when(entityManager.createQuery(eq(expectedQuery), eq(Caracteristica.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idTipoUnidadMedida"), eq(idTipoUnidadMedida))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<Caracteristica> result = caracteristicaDAO.findByTipoUnidadMedida(idTipoUnidadMedida);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getActivo());
        assertFalse(result.get(1).getActivo());
    }

    @Test
    void testFindByTipoUnidadMedida_WithMultipleResults_PreservesOrder() {
        Integer idTipoUnidadMedida = 1;
        String expectedQuery = "SELECT c FROM Caracteristica c WHERE c.idTipoUnidadMedida.id = :idTipoUnidadMedida";

        Caracteristica car1 = new Caracteristica();
        car1.setId(1);
        car1.setNombre("Primera");

        Caracteristica car2 = new Caracteristica();
        car2.setId(2);
        car2.setNombre("Segunda");

        Caracteristica car3 = new Caracteristica();
        car3.setId(3);
        car3.setNombre("Tercera");

        List<Caracteristica> expectedList = Arrays.asList(car1, car2, car3);

        when(entityManager.createQuery(eq(expectedQuery), eq(Caracteristica.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idTipoUnidadMedida"), eq(idTipoUnidadMedida))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedList);

        List<Caracteristica> result = caracteristicaDAO.findByTipoUnidadMedida(idTipoUnidadMedida);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Primera", result.get(0).getNombre());
        assertEquals("Segunda", result.get(1).getNombre());
        assertEquals("Tercera", result.get(2).getNombre());
    }
}