package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InventarioDefaultDataAccessTest {

    InventarioDefaultDataAccess<Compra> getDAO(EntityManager em) {
        return new InventarioDefaultDataAccess<>(Compra.class) {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    @Test
    void testCrear_CompraValida_PersistExitoso() {
        // Arrange
        Compra compra = new Compra();
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act
        dao.crear(compra);

        // Assert
        verify(mockEm, times(1)).persist(compra);
    }

    @Test
    void testCrear_RegistroNulo_LanzaExcepcion() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            dao.crear(null);
        });
        verify(mockEm, never()).persist(any());
    }

    @Test
    void testModificar_CompraValida_MergeExitoso() {
        // Arrange
        Compra compra = new Compra();
        EntityManager mockEm = mock(EntityManager.class);
        doReturn(compra).when(mockEm).merge(compra);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act
        dao.modificar(compra);

        // Assert
        verify(mockEm, times(1)).merge(compra);
    }

    @Test
    void testModificar_RegistroNulo_LanzaExcepcion() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            dao.modificar(null);
        });
        verify(mockEm, never()).merge(any());
    }

    @Test
    void testFind_DevuelveCompra() {
        // Arrange
        Object id = 1L;
        Compra esperado = new Compra();
        EntityManager mockEm = mock(EntityManager.class);
        doReturn(esperado).when(mockEm).find(Compra.class, id);

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act
        Compra resultado = dao.find(id);

        // Assert
        assertSame(esperado, resultado);
        verify(mockEm, times(1)).find(Compra.class, id);
    }

    @Test
    void testFind_IdNulo_LanzaExcepcion() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            dao.find(null);
        });
        verify(mockEm, never()).find(any(), any());
    }

    @Test
    void testFindById_DevuelveCompra() {
        // Arrange
        Object id = 1L;
        Compra esperado = new Compra();
        EntityManager mockEm = mock(EntityManager.class);
        doReturn(esperado).when(mockEm).find(Compra.class, id);

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act
        Compra resultado = dao.findById(id);

        // Assert
        assertSame(esperado, resultado);
        verify(mockEm, times(1)).find(Compra.class, id);
    }

    @Test
    void testFindById_IdNulo_LanzaExcepcion() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            dao.findById(null);
        });
        verify(mockEm, never()).find(any(), any());
    }

    @Test
    void testFindAll_DevuelveLista() {
        // Arrange
        Compra c1 = new Compra();
        Compra c2 = new Compra();
        List<Compra> esperado = Arrays.asList(c1, c2);

        EntityManager mockEm = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Compra> cq = mock(CriteriaQuery.class);
        Root<Compra> root = mock(Root.class);
        TypedQuery<Compra> query = mock(TypedQuery.class);

        doReturn(cb).when(mockEm).getCriteriaBuilder();
        doReturn(cq).when(cb).createQuery(Compra.class);
        doReturn(root).when(cq).from(Compra.class);
        doReturn(cq).when(cq).select(root);
        doReturn(query).when(mockEm).createQuery(cq);
        doReturn(esperado).when(query).getResultList();

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act
        List<Compra> resultado = dao.findAll();

        // Assert
        assertEquals(esperado, resultado);
        verify(mockEm, times(1)).getCriteriaBuilder();
        verify(query, times(1)).getResultList();
    }

    @Test
    void testFindRange_DevuelveSublista() {
        // Arrange
        Compra c1 = new Compra();
        Compra c2 = new Compra();
        List<Compra> esperado = Arrays.asList(c1, c2);

        EntityManager mockEm = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Compra> cq = mock(CriteriaQuery.class);
        Root<Compra> root = mock(Root.class);
        TypedQuery<Compra> query = mock(TypedQuery.class);

        doReturn(cb).when(mockEm).getCriteriaBuilder();
        doReturn(cq).when(cb).createQuery(Compra.class);
        doReturn(root).when(cq).from(Compra.class);
        doReturn(cq).when(cq).select(root);
        doReturn(query).when(mockEm).createQuery(cq);
        doReturn(query).when(query).setFirstResult(anyInt());
        doReturn(query).when(query).setMaxResults(anyInt());
        doReturn(esperado).when(query).getResultList();

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act
        List<Compra> resultado = dao.findRange(0, 2);

        // Assert
        assertEquals(esperado, resultado);
        verify(query, times(1)).setFirstResult(0);
        verify(query, times(1)).setMaxResults(2);
        verify(query, times(1)).getResultList();
    }

    @Test
    void testFindRange_ParametrosNegativos_CorrigeValores() {
        // Arrange
        Compra c1 = new Compra();
        List<Compra> esperado = Arrays.asList(c1);

        EntityManager mockEm = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Compra> cq = mock(CriteriaQuery.class);
        Root<Compra> root = mock(Root.class);
        TypedQuery<Compra> query = mock(TypedQuery.class);

        doReturn(cb).when(mockEm).getCriteriaBuilder();
        doReturn(cq).when(cb).createQuery(Compra.class);
        doReturn(root).when(cq).from(Compra.class);
        doReturn(cq).when(cq).select(root);
        doReturn(query).when(mockEm).createQuery(cq);
        doReturn(query).when(query).setFirstResult(anyInt());
        doReturn(query).when(query).setMaxResults(anyInt());
        doReturn(esperado).when(query).getResultList();

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act
        List<Compra> resultado = dao.findRange(-5, -10);

        // Assert
        assertNotNull(resultado);
        // Los valores negativos se corrigen internamente a 0 y 10
        verify(query, times(1)).getResultList();
    }

    @Test
    void testEliminar_EntidadGestionada_EliminaDirectamente() {
        // Arrange
        Compra compra = new Compra();
        EntityManager mockEm = mock(EntityManager.class);
        doReturn(true).when(mockEm).contains(compra);
        doNothing().when(mockEm).remove(compra);

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act
        dao.eliminar(compra);

        // Assert
        verify(mockEm, times(1)).contains(compra);
        verify(mockEm, never()).merge(any());
        verify(mockEm, times(1)).remove(compra);
    }

    @Test
    void testEliminar_EntidadNoGestionada_MergeYElimina() {
        // Arrange
        Compra compra = new Compra();
        EntityManager mockEm = mock(EntityManager.class);
        doReturn(false).when(mockEm).contains(compra);
        doReturn(compra).when(mockEm).merge(compra);
        doNothing().when(mockEm).remove(compra);

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act
        dao.eliminar(compra);

        // Assert
        verify(mockEm, times(1)).contains(compra);
        verify(mockEm, times(1)).merge(compra);
        verify(mockEm, times(1)).remove(compra);
    }

    @Test
    void testEliminar_EntidadNula_LanzaExcepcion() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            dao.eliminar(null);
        });
        verify(mockEm, never()).remove(any());
    }

    @Test
    void testEliminarPorId_RegistroExiste_EliminaExitoso() {
        // Arrange
        Object id = 1L;
        Compra compra = new Compra();
        EntityManager mockEm = mock(EntityManager.class);
        doReturn(compra).when(mockEm).find(Compra.class, id);
        doNothing().when(mockEm).remove(compra);

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act
        dao.eliminarPorId(id);

        // Assert
        verify(mockEm, times(1)).find(Compra.class, id);
        verify(mockEm, times(1)).remove(compra);
    }

    @Test
    void testEliminarPorId_RegistroNoExiste_LanzaExcepcion() {
        // Arrange
        Object id = 999L;
        EntityManager mockEm = mock(EntityManager.class);
        doReturn(null).when(mockEm).find(Compra.class, id);

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            dao.eliminarPorId(id);
        });
        verify(mockEm, times(1)).find(Compra.class, id);
        verify(mockEm, never()).remove(any());
    }

    @Test
    void testEliminarPorId_IdNulo_LanzaExcepcion() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            dao.eliminarPorId(null);
        });
        verify(mockEm, never()).find(any(), any());
        verify(mockEm, never()).remove(any());
    }
}