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
import static org.mockito.ArgumentMatchers.*;
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

    // Tests para el constructor y métodos básicos
    @Test
    void testConstructor_SetsEntityClass() {
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        assertEquals(Compra.class, dao.getEntityClass());
    }

    @Test
    void testGetEntityManager_ReturnsCorrectEntityManager() {
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        assertSame(mockEm, dao.getEntityManager());
    }

    // Tests para find() y findById()
    @Test
    void testFind_CompraValida_DevuelveCompra() {
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
    void testFind_IdNulo_LanzaIllegalArgumentException() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dao.find(null);
        });
        assertEquals("El ID no puede ser nulo", exception.getMessage());
        verify(mockEm, never()).find(any(), any());
    }

    @Test
    void testFind_ExcepcionEnFind_LanzaIllegalStateException() {
        // Arrange
        Object id = 1L;
        EntityManager mockEm = mock(EntityManager.class);
        doThrow(new RuntimeException("Database error")).when(mockEm).find(Compra.class, id);

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            dao.find(id);
        });
        assertEquals("Error al buscar el registro por ID", exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
    }

    @Test
    void testFindById_CompraValida_DevuelveCompra() {
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
    void testFindById_IdNulo_LanzaIllegalArgumentException() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            dao.findById(null);
        });
        verify(mockEm, never()).find(any(), any());
    }

    // Tests para findAll()
    @Test
    void testFindAll_ListaValida_DevuelveLista() {
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
    void testFindAll_ExcepcionEnQuery_LanzaIllegalStateException() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        doThrow(new RuntimeException("Query error")).when(mockEm).getCriteriaBuilder();

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            dao.findAll();
        });
        assertEquals("Error al acceder a todos los registros", exception.getMessage());
    }

    @Test
    void testFindAll_ListaVacia_DevuelveListaVacia() {
        // Arrange
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
        doReturn(List.of()).when(query).getResultList();

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act
        List<Compra> resultado = dao.findAll();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // Tests para findRange()
    @Test
    void testFindRange_ParametrosValidos_DevuelveSublista() {
        // Arrange
        Compra c1 = new Compra();
        Compra c2 = new Compra();
        List<Compra> esperado = Arrays.asList(c1, c2);

        EntityManager mockEm = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Compra> cq = mock(CriteriaQuery.class);
        Root<Compra> root = mock(Root.class);
        TypedQuery<Compra> query = mock(TypedQuery.class);

        // Configuración MÁS SIMPLE usando when().thenReturn()
        when(mockEm.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Compra.class)).thenReturn(cq);
        when(cq.from(Compra.class)).thenReturn(root);
        when(cq.select(root)).thenReturn(cq);

        // Para orderBy, podemos usar any() con when().thenReturn()
        when(cq.orderBy(any(jakarta.persistence.criteria.Order.class))).thenReturn(cq);

        when(mockEm.createQuery(cq)).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act
        List<Compra> resultado = dao.findRange(0, 2);

        // Assert
        assertEquals(esperado, resultado);
        verify(query).setFirstResult(0);
        verify(query).setMaxResults(2);
        verify(query).getResultList();
    }

    @Test
    void testFindRange_FirstNegativo_LanzaIllegalArgumentException() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dao.findRange(-1, 10);
        });
        assertEquals("Parámetros inválidos", exception.getMessage());
        verify(mockEm, never()).getCriteriaBuilder();
    }

    @Test
    void testFindRange_PageSizeCero_LanzaIllegalArgumentException() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dao.findRange(0, 0);
        });
        assertEquals("Parámetros inválidos", exception.getMessage());
    }

    @Test
    void testFindRange_PageSizeNegativo_LanzaIllegalArgumentException() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            dao.findRange(0, -5);
        });
    }

    @Test
    void testFindRange_ExcepcionEnQuery_LanzaRuntimeException() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        doThrow(new RuntimeException("Range query error")).when(mockEm).getCriteriaBuilder();

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dao.findRange(0, 10);
        });
        assertEquals("Error al obtener rango de registros", exception.getMessage());
    }

    // Tests para count()
    @Test
    void testCount_ConRegistros_DevuelveCountCorrecto() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Long> cq = mock(CriteriaQuery.class);
        Root<Compra> root = mock(Root.class);
        TypedQuery<Long> query = mock(TypedQuery.class);

        doReturn(cb).when(mockEm).getCriteriaBuilder();
        doReturn(cq).when(cb).createQuery(Long.class);
        doReturn(root).when(cq).from(Compra.class);
        doReturn(cq).when(cq).select(any());
        doReturn(query).when(mockEm).createQuery(cq);
        doReturn(25L).when(query).getSingleResult();

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act
        Long resultado = dao.count();

        // Assert
        assertEquals(25L, resultado);
        verify(mockEm, times(1)).getCriteriaBuilder();
        verify(query, times(1)).getSingleResult();
    }

    @Test
    void testCount_ExcepcionEnQuery_LanzaIllegalStateException() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        doThrow(new RuntimeException("Count error")).when(mockEm).getCriteriaBuilder();

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            dao.count();
        });
        assertEquals("Error al contar registros", exception.getMessage());
    }

    // Tests para contar()
    @Test
    void testContar_ConRegistros_DevuelveIntCorrecto() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Long> cq = mock(CriteriaQuery.class);
        Root<Compra> root = mock(Root.class);
        TypedQuery<Long> query = mock(TypedQuery.class);

        doReturn(cb).when(mockEm).getCriteriaBuilder();
        doReturn(cq).when(cb).createQuery(Long.class);
        doReturn(root).when(cq).from(Compra.class);
        doReturn(cq).when(cq).select(any());
        doReturn(query).when(mockEm).createQuery(cq);
        doReturn(15L).when(query).getSingleResult();

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act
        int resultado = dao.contar();

        // Assert
        assertEquals(15, resultado);
        verify(query, times(1)).getSingleResult();
    }

    @Test
    void testContar_EntityManagerNulo_DevuelveMenosUno() {
        // Arrange
        InventarioDefaultDataAccess<Compra> dao = new InventarioDefaultDataAccess<>(Compra.class) {
            @Override
            public EntityManager getEntityManager() {
                return null;
            }
        };

        // Act
        int resultado = dao.contar();

        // Assert
        assertEquals(-1, resultado);
    }

    @Test
    void testContar_ExcepcionEnQuery_LanzaIllegalStateException() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        doThrow(new RuntimeException("Contar error")).when(mockEm).getCriteriaBuilder();

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            dao.contar();
        });
        assertEquals("dao.AccesoDB", exception.getMessage());
    }

    @Test
    void testContar_CeroRegistros_DevuelveCero() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Long> cq = mock(CriteriaQuery.class);
        Root<Compra> root = mock(Root.class);
        TypedQuery<Long> query = mock(TypedQuery.class);

        doReturn(cb).when(mockEm).getCriteriaBuilder();
        doReturn(cq).when(cb).createQuery(Long.class);
        doReturn(root).when(cq).from(Compra.class);
        doReturn(cq).when(cq).select(any());
        doReturn(query).when(mockEm).createQuery(cq);
        doReturn(0L).when(query).getSingleResult();

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act
        int resultado = dao.contar();

        // Assert
        assertEquals(0, resultado);
    }

    // Tests para crear()
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
    void testCrear_RegistroNulo_LanzaIllegalArgumentException() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dao.crear(null);
        });
        assertEquals("El registro no puede ser nulo", exception.getMessage());
        verify(mockEm, never()).persist(any());
    }

    @Test
    void testCrear_ExcepcionEnPersist_LanzaRuntimeException() {
        // Arrange
        Compra compra = new Compra();
        EntityManager mockEm = mock(EntityManager.class);
        doThrow(new RuntimeException("Persist error")).when(mockEm).persist(compra);

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dao.crear(compra);
        });
        assertEquals("Error al crear el registro", exception.getMessage());
    }

    // Tests para modificar()
    @Test
    void testModificar_CompraValida_MergeExitoso() {
        // Arrange
        Compra compra = new Compra();
        EntityManager mockEm = mock(EntityManager.class);
        doReturn(compra).when(mockEm).merge(compra);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act
        Compra resultado = dao.modificar(compra);

        // Assert
        assertSame(compra, resultado);
        verify(mockEm, times(1)).merge(compra);
    }

    @Test
    void testModificar_RegistroNulo_LanzaIllegalArgumentException() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dao.modificar(null);
        });
        assertEquals("El registro no puede ser nulo", exception.getMessage());
        verify(mockEm, never()).merge(any());
    }

    @Test
    void testModificar_ExcepcionEnMerge_LanzaRuntimeException() {
        // Arrange
        Compra compra = new Compra();
        EntityManager mockEm = mock(EntityManager.class);
        doThrow(new RuntimeException("Merge error")).when(mockEm).merge(compra);

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dao.modificar(compra);
        });
        assertEquals("Error al modificar el registro", exception.getMessage());
    }

    // Tests para eliminar()
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
    void testEliminar_EntidadNula_LanzaIllegalArgumentException() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dao.eliminar(null);
        });
        assertEquals("La entidad no puede ser nula", exception.getMessage());
        verify(mockEm, never()).remove(any());
    }


    @Test
    void testEliminar_ExcepcionEnRemove_LanzaRuntimeException() {
        // Arrange
        Compra compra = new Compra();
        EntityManager mockEm = mock(EntityManager.class);
        doReturn(true).when(mockEm).contains(compra);
        doThrow(new RuntimeException("Remove error")).when(mockEm).remove(compra);

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dao.eliminar(compra);
        });
        assertEquals("Error al eliminar el registro", exception.getMessage());
    }

    // Tests para eliminarPorId()
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
    void testEliminarPorId_RegistroNoExiste_LanzaRuntimeException() {
        // Arrange
        Object id = 999L;
        EntityManager mockEm = mock(EntityManager.class);
        doReturn(null).when(mockEm).find(Compra.class, id);

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dao.eliminarPorId(id);
        });
        assertEquals("Error al eliminar el registro por ID", exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Registro no encontrado", exception.getCause().getMessage());

        verify(mockEm, times(1)).find(Compra.class, id);
        verify(mockEm, never()).remove(any());
    }

    @Test
    void testEliminarPorId_IdNulo_LanzaIllegalArgumentException() {
        // Arrange
        EntityManager mockEm = mock(EntityManager.class);
        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dao.eliminarPorId(null);
        });
        assertEquals("El ID no puede ser nulo", exception.getMessage());
        verify(mockEm, never()).find(any(), any());
        verify(mockEm, never()).remove(any());
    }

    @Test
    void testEliminarPorId_ExcepcionEnFind_LanzaRuntimeException() {
        // Arrange
        Object id = 1L;
        EntityManager mockEm = mock(EntityManager.class);
        doThrow(new RuntimeException("Find error")).when(mockEm).find(Compra.class, id);

        InventarioDefaultDataAccess<Compra> dao = getDAO(mockEm);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dao.eliminarPorId(id);
        });
        assertEquals("Error al eliminar el registro por ID", exception.getMessage());
    }

    @Test
    void testEliminar_EntityManagerNulo_LanzaRuntimeException() {
        // Arrange
        Compra compra = new Compra();
        InventarioDefaultDataAccess<Compra> dao = new InventarioDefaultDataAccess<>(Compra.class) {
            @Override
            public EntityManager getEntityManager() {
                return null;
            }
        };

        // Act & Assert - Cambiar a RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dao.eliminar(compra);
        });
        assertEquals("Error al eliminar el registro", exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("EntityManager no disponible", exception.getCause().getMessage());
    }

    @Test
    void testCrear_EntityManagerNulo_LanzaRuntimeException() {
        // Arrange
        Compra compra = new Compra();
        InventarioDefaultDataAccess<Compra> dao = new InventarioDefaultDataAccess<>(Compra.class) {
            @Override
            public EntityManager getEntityManager() {
                return null;
            }
        };

        // Act & Assert - Cambiar a RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dao.crear(compra);
        });
        assertEquals("Error al crear el registro", exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("EntityManager no disponible", exception.getCause().getMessage());
    }

    @Test
    void testModificar_EntityManagerNulo_LanzaRuntimeException() {
        // Arrange
        Compra compra = new Compra();
        InventarioDefaultDataAccess<Compra> dao = new InventarioDefaultDataAccess<>(Compra.class) {
            @Override
            public EntityManager getEntityManager() {
                return null;
            }
        };

        // Act & Assert - Cambiar a RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dao.modificar(compra);
        });
        assertEquals("Error al modificar el registro", exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("EntityManager no disponible", exception.getCause().getMessage());
    }

    @Test
    void testEliminarPorId_EntityManagerNulo_LanzaRuntimeException() {
        // Arrange
        Object id = 1L;
        InventarioDefaultDataAccess<Compra> dao = new InventarioDefaultDataAccess<>(Compra.class) {
            @Override
            public EntityManager getEntityManager() {
                return null;
            }
        };

        // Act & Assert - Cambiar a RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dao.eliminarPorId(id);
        });
        assertEquals("Error al eliminar el registro por ID", exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("EntityManager no disponible", exception.getCause().getMessage());
    }

    @Test
    void testFindRange_EntityManagerNulo_LanzaRuntimeException() {
        // Arrange
        InventarioDefaultDataAccess<Compra> dao = new InventarioDefaultDataAccess<>(Compra.class) {
            @Override
            public EntityManager getEntityManager() {
                return null;
            }
        };

        // Act & Assert - Cambiar a RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dao.findRange(0, 10);
        });
        assertEquals("Error al obtener rango de registros", exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("EntityManager no disponible", exception.getCause().getMessage());
    }

    @Test
    void testCount_EntityManagerNulo_LanzaIllegalStateException() {
        // Arrange
        InventarioDefaultDataAccess<Compra> dao = new InventarioDefaultDataAccess<>(Compra.class) {
            @Override
            public EntityManager getEntityManager() {
                return null;
            }
        };

        // Act & Assert - Cambiar la verificación del mensaje
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            dao.count();
        });
        assertEquals("Error al contar registros", exception.getMessage());
        // Si quieres verificar la causa interna:
        // assertTrue(exception.getCause() instanceof IllegalStateException);
        // assertEquals("EntityManager no disponible", exception.getCause().getMessage());
    }

    @Test
    void testFindAll_EntityManagerNulo_LanzaIllegalStateException() {
        // Arrange
        InventarioDefaultDataAccess<Compra> dao = new InventarioDefaultDataAccess<>(Compra.class) {
            @Override
            public EntityManager getEntityManager() {
                return null;
            }
        };

        // Act & Assert - Cambiar la verificación del mensaje
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            dao.findAll();
        });
        assertEquals("Error al acceder a todos los registros", exception.getMessage());
    }

    @Test
    void testFind_EntityManagerNulo_LanzaIllegalStateException() {
        // Arrange
        Object id = 1L;
        InventarioDefaultDataAccess<Compra> dao = new InventarioDefaultDataAccess<>(Compra.class) {
            @Override
            public EntityManager getEntityManager() {
                return null;
            }
        };

        // Act & Assert - Cambiar la verificación del mensaje
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            dao.find(id);
        });
        assertEquals("Error al buscar el registro por ID", exception.getMessage());
    }
}