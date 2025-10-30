package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Caracteristica;

import java.util.Arrays;
import java.util.List;

class CaracteristicaDAOTest {

    private CaracteristicaDAO caracteristicaDAO;
    private EntityManager emMock;

    @BeforeEach
    void setUp() {
        // Crear un mock del EntityManager
        emMock = mock(EntityManager.class);

        // Crear la instancia de CaracteristicaDAO
        caracteristicaDAO = new CaracteristicaDAO();
        caracteristicaDAO.em = emMock;  // Inyectar el mock en la instancia de DAO
    }

    @Test
    void testFindById_ValidId() {
        // Preparar datos de prueba
        Long id = 1L;
        Caracteristica caracteristicaMock = new Caracteristica();
        caracteristicaMock.setId(1);
        caracteristicaMock.setNombre("Característica de prueba");

        // Configurar el mock del EntityManager para que devuelva el objeto cuando se llama a em.find()
        when(emMock.find(Caracteristica.class, id)).thenReturn(caracteristicaMock);

        // Llamar al método que estamos probando
        Caracteristica result = caracteristicaDAO.findById(id);

        // Verificar resultados
        assertNotNull(result);
        assertEquals("Característica de prueba", result.getNombre());
        verify(emMock).find(Caracteristica.class, id);  // Verificar que se llamó a em.find() correctamente
    }

    @Test
    void testFindById_NullId() {
        // Llamar al método con un ID nulo
        Caracteristica result = caracteristicaDAO.findById(null);

        // Verificar que el resultado sea nulo
        assertNull(result);
    }

    @Test
    void testFindById_NotFound() {
        // Preparar datos
        Long id = 1L;

        // Configurar el mock para que retorne null cuando no se encuentre el objeto
        when(emMock.find(Caracteristica.class, id)).thenReturn(null);

        // Llamar al método
        Caracteristica result = caracteristicaDAO.findById(id);

        // Verificar que el resultado sea nulo
        assertNull(result);
    }

    @Test
    void testFindAll() {
        // Preparar datos de prueba
        Caracteristica c1 = new Caracteristica();
        c1.setId(1);
        c1.setNombre("Característica 1");

        Caracteristica c2 = new Caracteristica();
        c2.setId(2);
        c2.setNombre("Característica 2");

        List<Caracteristica> expectedList = Arrays.asList(c1, c2);

        // Configurar el mock para que devuelva la lista
        when(emMock.createQuery("SELECT c FROM Caracteristica c ORDER BY c.nombre", Caracteristica.class))
                .thenReturn(mock(javax.persistence.TypedQuery.class));
        javax.persistence.TypedQuery<Caracteristica> queryMock = mock(javax.persistence.TypedQuery.class);
        when(queryMock.getResultList()).thenReturn(expectedList);
        when(emMock.createQuery(anyString(), eq(Caracteristica.class))).thenReturn(queryMock);

        // Llamar al método
        List<Caracteristica> result = caracteristicaDAO.findAll();

        // Verificar resultados
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Característica 1", result.get(0).getNombre());
        assertEquals("Característica 2", result.get(1).getNombre());
    }

    @Test
    void testFindAll_EmptyList() {
        // Configurar el mock para devolver una lista vacía
        when(emMock.createQuery("SELECT c FROM Caracteristica c ORDER BY c.nombre", Caracteristica.class))
                .thenReturn(mock(javax.persistence.TypedQuery.class));
        javax.persistence.TypedQuery<Caracteristica> queryMock = mock(javax.persistence.TypedQuery.class);
        when(queryMock.getResultList()).thenReturn(Arrays.asList());
        when(emMock.createQuery(anyString(), eq(Caracteristica.class))).thenReturn(queryMock);

        // Llamar al método
        List<Caracteristica> result = caracteristicaDAO.findAll();

        // Verificar que la lista esté vacía
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
