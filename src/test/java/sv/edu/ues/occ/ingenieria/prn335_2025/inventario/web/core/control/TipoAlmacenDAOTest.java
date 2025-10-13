package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TipoAlmacenDAOTest {

    @Test
    void crear() {
        // ... (tu prueba existente)
    }


    @Test
    void modificarExitoso() {
        // Crear una instancia de TipoAlmacen para prueba
        TipoAlmacen existente = new TipoAlmacen();
        existente.setNombre("Tipo Almacen Modificado");
        existente.setActivo(false);

        // Crear un mock de EntityManager
        EntityManager mockEm = Mockito.mock(EntityManager.class);

        // Crear una instancia real de TipoAlmacenDAO
        TipoAlmacenDAO cut = new TipoAlmacenDAO();

        // Crear un spy de la instancia real para mockear getEntityManager()
        TipoAlmacenDAO spyCut = Mockito.spy(cut);
        Mockito.doReturn(mockEm).when(spyCut).getEntityManager();


        spyCut.modificar(existente);

        // Verificar que se llamó a merge con el objeto correcto
        Mockito.verify(mockEm).merge(existente);
    }

    @Test
    void modificarConNullLanzaExcepcion() {
        TipoAlmacenDAO cut = new TipoAlmacenDAO();

        assertThrows(IllegalArgumentException.class, () -> {
            cut.modificar(null);
        });
    }

    @Test
    void countExitoso() {
        // Crear mocks para toda la cadena de Criteria API
        EntityManager mockEm = Mockito.mock(EntityManager.class);
        CriteriaBuilder mockCb = Mockito.mock(CriteriaBuilder.class);
        CriteriaQuery<Long> mockCq = Mockito.mock(CriteriaQuery.class);
        Root<TipoAlmacen> mockRoot = Mockito.mock(Root.class);
        TypedQuery<Long> mockQuery = Mockito.mock(TypedQuery.class);

        // Configurar los mocks
        when(mockEm.getCriteriaBuilder()).thenReturn(mockCb);
        when(mockCb.createQuery(Long.class)).thenReturn(mockCq);
        when(mockCq.from(TipoAlmacen.class)).thenReturn(mockRoot);
        when(mockCq.select(any())).thenReturn(mockCq);
        when(mockEm.createQuery(mockCq)).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(5L);

        // Crear instancia real y spy
        TipoAlmacenDAO cut = new TipoAlmacenDAO();
        TipoAlmacenDAO spyCut = Mockito.spy(cut);
        Mockito.doReturn(mockEm).when(spyCut).getEntityManager();


        int resultado = spyCut.count();

        // Verificar el resultado y las interacciones
        assertEquals(5, resultado);
        verify(mockEm).getCriteriaBuilder();
        verify(mockEm).createQuery(mockCq);
        verify(mockQuery).getSingleResult();
    }

    @Test
    void findRangeExitoso() {
        // Crear datos de prueba
        TipoAlmacen tipo1 = new TipoAlmacen();
        TipoAlmacen tipo2 = new TipoAlmacen();
        List<TipoAlmacen> listaEsperada = Arrays.asList(tipo1, tipo2);

        // Crear mocks para toda la cadena de Criteria API
        EntityManager mockEm = Mockito.mock(EntityManager.class);
        CriteriaBuilder mockCb = Mockito.mock(CriteriaBuilder.class);
        CriteriaQuery<TipoAlmacen> mockCq = Mockito.mock(CriteriaQuery.class);
        Root<TipoAlmacen> mockRoot = Mockito.mock(Root.class);
        TypedQuery<TipoAlmacen> mockQuery = Mockito.mock(TypedQuery.class);

        // Configurar los mocks
        when(mockEm.getCriteriaBuilder()).thenReturn(mockCb);
        when(mockCb.createQuery(TipoAlmacen.class)).thenReturn(mockCq);
        when(mockCq.from(TipoAlmacen.class)).thenReturn(mockRoot);
        when(mockCq.select(mockRoot)).thenReturn(mockCq);
        when(mockEm.createQuery(mockCq)).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(listaEsperada);

        // Crear instancia real y spy
        TipoAlmacenDAO cut = new TipoAlmacenDAO();
        TipoAlmacenDAO spyCut = Mockito.spy(cut);
        Mockito.doReturn(mockEm).when(spyCut).getEntityManager();


        List<TipoAlmacen> resultado = spyCut.findRange(0, 2);

        // Verificar el resultado y las interacciones
        assertEquals(2, resultado.size());
        assertEquals(listaEsperada, resultado);
        verify(mockQuery).setFirstResult(0);
        verify(mockQuery).setMaxResults(2);
        verify(mockQuery).getResultList();
    }

    @Test
    void findRangeConParametrosInvalidosLanzaExcepcion() {
        TipoAlmacenDAO cut = new TipoAlmacenDAO();

        assertThrows(IllegalArgumentException.class, () -> {
            cut.findRange(-1, 10); // first negativo
        });

        assertThrows(IllegalArgumentException.class, () -> {
            cut.findRange(0, 0); // max menor a 1
        });
    }
    @Test
    void crearConEntityManagerNulo() {
        // Crear una instancia de TipoAlmacen para prueba
        TipoAlmacen nuevo = new TipoAlmacen();
        nuevo.setNombre("Tipo Almacen 1");
        nuevo.setActivo(true);

        // Crear una instancia real de TipoAlmacenDAO
        TipoAlmacenDAO cut = new TipoAlmacenDAO();

        // Crear un spy de la instancia real para mockear getEntityManager() devolviendo null
        TipoAlmacenDAO spyCut = Mockito.spy(cut);
        Mockito.doReturn(null).when(spyCut).getEntityManager();

        // Verificar que se lanza IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            spyCut.crear(nuevo);
        });
    }

    @Test
    void crearConPersistLanzandoExcepcion() {
        // Crear una instancia de TipoAlmacen para prueba
        TipoAlmacen nuevo = new TipoAlmacen();
        nuevo.setNombre("Tipo Almacen 1");
        nuevo.setActivo(true);

        // Crear un mock de EntityManager que lance excepción en persist
        EntityManager mockEm = Mockito.mock(EntityManager.class);
        doThrow(new RuntimeException("Error de base de datos")).when(mockEm).persist(any());

        // Crear una instancia real de TipoAlmacenDAO
        TipoAlmacenDAO cut = new TipoAlmacenDAO();

        // Crear un spy de la instancia real para mockear getEntityManager()
        TipoAlmacenDAO spyCut = Mockito.spy(cut);
        Mockito.doReturn(mockEm).when(spyCut).getEntityManager();

        // Verificar que se lanza IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            spyCut.crear(nuevo);
        });
    }

    @Test
    void modificarConEntityManagerNulo() {
        // Crear una instancia de TipoAlmacen para prueba
        TipoAlmacen existente = new TipoAlmacen();
        existente.setNombre("Tipo Almacen Modificado");
        existente.setActivo(false);

        // Crear una instancia real de TipoAlmacenDAO
        TipoAlmacenDAO cut = new TipoAlmacenDAO();

        // Crear un spy de la instancia real para mockear getEntityManager() devolviendo null
        TipoAlmacenDAO spyCut = Mockito.spy(cut);
        Mockito.doReturn(null).when(spyCut).getEntityManager();

        // Verificar que se lanza IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            spyCut.modificar(existente);
        });
    }

    @Test
    void modificarConMergeLanzandoExcepcion() {
        // Crear una instancia de TipoAlmacen para prueba
        TipoAlmacen existente = new TipoAlmacen();
        existente.setNombre("Tipo Almacen Modificado");
        existente.setActivo(false);

        // Crear un mock de EntityManager que lance excepción en merge
        EntityManager mockEm = Mockito.mock(EntityManager.class);
        doThrow(new RuntimeException("Error de base de datos")).when(mockEm).merge(any());

        // Crear una instancia real de TipoAlmacenDAO
        TipoAlmacenDAO cut = new TipoAlmacenDAO();

        // Crear un spy de la instancia real para mockear getEntityManager()
        TipoAlmacenDAO spyCut = Mockito.spy(cut);
        Mockito.doReturn(mockEm).when(spyCut).getEntityManager();

        // Verificar que se lanza IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            spyCut.modificar(existente);
        });
    }

    @Test
    void countConEntityManagerNulo() {
        // Crear una instancia real de TipoAlmacenDAO
        TipoAlmacenDAO cut = new TipoAlmacenDAO();

        // Crear un spy de la instancia real para mockear getEntityManager() devolviendo null
        TipoAlmacenDAO spyCut = Mockito.spy(cut);
        Mockito.doReturn(null).when(spyCut).getEntityManager();

        // Verificar que se lanza IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            spyCut.count();
        });
    }

    @Test
    void countConGetSingleResultLanzandoExcepcion() {
        // Crear mocks para toda la cadena de Criteria API
        EntityManager mockEm = Mockito.mock(EntityManager.class);
        CriteriaBuilder mockCb = Mockito.mock(CriteriaBuilder.class);
        CriteriaQuery<Long> mockCq = Mockito.mock(CriteriaQuery.class);
        Root<TipoAlmacen> mockRoot = Mockito.mock(Root.class);
        TypedQuery<Long> mockQuery = Mockito.mock(TypedQuery.class);

        // Configurar los mocks
        when(mockEm.getCriteriaBuilder()).thenReturn(mockCb);
        when(mockCb.createQuery(Long.class)).thenReturn(mockCq);
        when(mockCq.from(TipoAlmacen.class)).thenReturn(mockRoot);
        when(mockCq.select(any())).thenReturn(mockCq);
        when(mockEm.createQuery(mockCq)).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenThrow(new RuntimeException("Error en consulta"));

        // Crear instancia real y spy
        TipoAlmacenDAO cut = new TipoAlmacenDAO();
        TipoAlmacenDAO spyCut = Mockito.spy(cut);
        Mockito.doReturn(mockEm).when(spyCut).getEntityManager();

        // Verificar que se lanza IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            spyCut.count();
        });
    }

    @Test
    void findRangeConEntityManagerNulo() {
        // Crear una instancia real de TipoAlmacenDAO
        TipoAlmacenDAO cut = new TipoAlmacenDAO();

        // Crear un spy de la instancia real para mockear getEntityManager() devolviendo null
        TipoAlmacenDAO spyCut = Mockito.spy(cut);
        Mockito.doReturn(null).when(spyCut).getEntityManager();

        // Verificar que se lanza IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            spyCut.findRange(0, 10);
        });
    }

    @Test
    void findRangeConGetResultListLanzandoExcepcion() {
        // Crear mocks para toda la cadena de Criteria API
        EntityManager mockEm = Mockito.mock(EntityManager.class);
        CriteriaBuilder mockCb = Mockito.mock(CriteriaBuilder.class);
        CriteriaQuery<TipoAlmacen> mockCq = Mockito.mock(CriteriaQuery.class);
        Root<TipoAlmacen> mockRoot = Mockito.mock(Root.class);
        TypedQuery<TipoAlmacen> mockQuery = Mockito.mock(TypedQuery.class);

        // Configurar los mocks
        when(mockEm.getCriteriaBuilder()).thenReturn(mockCb);
        when(mockCb.createQuery(TipoAlmacen.class)).thenReturn(mockCq);
        when(mockCq.from(TipoAlmacen.class)).thenReturn(mockRoot);
        when(mockCq.select(mockRoot)).thenReturn(mockCq);
        when(mockEm.createQuery(mockCq)).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("Error en consulta"));

        // Crear instancia real y spy
        TipoAlmacenDAO cut = new TipoAlmacenDAO();
        TipoAlmacenDAO spyCut = Mockito.spy(cut);
        Mockito.doReturn(mockEm).when(spyCut).getEntityManager();

        // Verificar que se lanza IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            spyCut.findRange(0, 10);
        });
    }
    @Test
    void crearConRegistroNuloLanzaExcepcion() {
        TipoAlmacenDAO cut = new TipoAlmacenDAO();
        assertThrows(IllegalArgumentException.class, () -> cut.crear(null));
    }
    @Test
    void crearExitoso() {
        TipoAlmacen nuevo = new TipoAlmacen();
        nuevo.setNombre("Nuevo");
        nuevo.setActivo(true);

        EntityManager mockEm = Mockito.mock(EntityManager.class);

        TipoAlmacenDAO cut = new TipoAlmacenDAO();
        TipoAlmacenDAO spyCut = Mockito.spy(cut);
        Mockito.doReturn(mockEm).when(spyCut).getEntityManager();

        spyCut.crear(nuevo);

        verify(mockEm).persist(nuevo);
    }
    @Test
    void testConstructor() {
        // Verifica que el constructor se inicialice correctamente
        TipoAlmacenDAO cut = new TipoAlmacenDAO();
        assertNotNull(cut);

    }
    @Test
    void getEntityManagerExitoso() {
        // Crear instancia del DAO
        TipoAlmacenDAO cut = new TipoAlmacenDAO();

        // Crear mock del EntityManager
        EntityManager mockEm = Mockito.mock(EntityManager.class);

        // Inyectar el mock usando reflexión (ya que el campo es package-private)
        cut.em = mockEm;

        // Verificar que getEntityManager() devuelve el mock configurado
        EntityManager resultado = cut.getEntityManager();
        assertSame(mockEm, resultado);
    }

    @Test
    void getEntityManagerNulo() {
        TipoAlmacenDAO cut = new TipoAlmacenDAO();
        cut.em = null; // Forzar valor nulo

        assertNull(cut.getEntityManager());
    }

}