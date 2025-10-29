package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.model.LazyDataModel;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoAlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoAlmacenFrmTest {

    @Mock
    private FacesContext facesContext;

    @Mock
    private TipoAlmacenDAO tipoAlmacenDAO;

    @Mock
    private LazyDataModel<TipoAlmacen> modeloMock;

    @InjectMocks
    private TipoAlmacenFrm tipoAlmacenFrm;

    private TipoAlmacen tipoAlmacenTest;
    private List<TipoAlmacen> listaTipoAlmacen;

    @BeforeEach
    void setUp() throws Exception {
        // Crear datos de prueba
        tipoAlmacenTest = new TipoAlmacen();
        tipoAlmacenTest.setId(1);
        tipoAlmacenTest.setNombre("Almacen Principal");

        listaTipoAlmacen = new ArrayList<>();
        listaTipoAlmacen.add(tipoAlmacenTest);

        // Inyectar el modelo mock usando reflexión
        Field modeloField = DefaultFrm.class.getDeclaredField("modelo");
        modeloField.setAccessible(true);
        modeloField.set(tipoAlmacenFrm, modeloMock);
    }

    @Test
    void testConstructor() {
        TipoAlmacenFrm frm = new TipoAlmacenFrm();
        assertNotNull(frm);
    }

    @Test
    void testGetFacesContext() {
        FacesContext result = tipoAlmacenFrm.getFacesContext();
        assertNotNull(result);
        assertEquals(facesContext, result);
    }

    @Test
    void testGetDao() {
        assertEquals(tipoAlmacenDAO, tipoAlmacenFrm.getDao());
    }

    @Test
    void testNuevoRegistro() {
        TipoAlmacen nuevo = tipoAlmacenFrm.nuevoRegistro();
        assertNotNull(nuevo);
        assertNull(nuevo.getId());
    }

    @Test
    void testGetIdAsTextConRegistroValido() {
        String resultado = tipoAlmacenFrm.getIdAsText(tipoAlmacenTest);
        assertEquals("1", resultado);
    }

    @Test
    void testGetIdAsTextConRegistroNulo() {
        String resultado = tipoAlmacenFrm.getIdAsText(null);
        assertNull(resultado);
    }

    @Test
    void testGetIdAsTextConIdNulo() {
        TipoAlmacen sinId = new TipoAlmacen();
        String resultado = tipoAlmacenFrm.getIdAsText(sinId);
        assertNull(resultado);
    }

    @Test
    void testBuscarRegistroPorIdConIdValido() {
        // Arrange - Modelo VACÍO para que busque en DAO
        List<TipoAlmacen> listaVacia = new ArrayList<>();
        when(modeloMock.getWrappedData()).thenReturn(listaVacia);
        when(tipoAlmacenDAO.findAll()).thenReturn(listaTipoAlmacen);

        // Act
        TipoAlmacen resultado = tipoAlmacenFrm.buscarRegistroPorId(1);

        // Assert
        assertNotNull(resultado, "Debe encontrar el registro");
        assertEquals(1, resultado.getId());
        assertEquals("Almacen Principal", resultado.getNombre());
        verify(modeloMock, atLeastOnce()).getWrappedData();
        verify(tipoAlmacenDAO).findAll();
    }

    @Test
    void testBuscarRegistroPorIdConIdNulo() {
        // Act
        TipoAlmacen resultado = tipoAlmacenFrm.buscarRegistroPorId(null);

        // Assert
        assertNull(resultado, "Debe retornar null con id nulo");
        verify(modeloMock, never()).getWrappedData();
    }

    @Test
    void testBuscarRegistroPorIdConModeloNoVacio() {
        // Arrange - Modelo CON DATOS, no debe buscar en DAO
        when(modeloMock.getWrappedData()).thenReturn(listaTipoAlmacen);

        // Act
        TipoAlmacen resultado = tipoAlmacenFrm.buscarRegistroPorId(1);

        // Assert
        assertNull(resultado, "Debe retornar null porque el modelo NO está vacío");
        verify(modeloMock).getWrappedData();
        verify(tipoAlmacenDAO, never()).findAll();
    }

    @Test
    void testBuscarRegistroPorIdNoEncontrado() {
        // Arrange
        List<TipoAlmacen> listaVacia = new ArrayList<>();
        when(modeloMock.getWrappedData()).thenReturn(listaVacia);

        // Lista sin el ID buscado
        List<TipoAlmacen> listaSinCoincidencia = new ArrayList<>();
        TipoAlmacen otro = new TipoAlmacen();
        otro.setId(999);
        listaSinCoincidencia.add(otro);
        when(tipoAlmacenDAO.findAll()).thenReturn(listaSinCoincidencia);

        // Act
        TipoAlmacen resultado = tipoAlmacenFrm.buscarRegistroPorId(1);

        // Assert
        assertNull(resultado, "No debe encontrar el registro con ID diferente");
    }

    @Test
    void testGetIdByTextConIdValido() {
        // Arrange
        when(modeloMock.getWrappedData()).thenReturn(listaTipoAlmacen);

        // Act
        TipoAlmacen resultado = tipoAlmacenFrm.getIdByText("1");

        // Assert
        assertNotNull(resultado, "Debe encontrar el registro");
        assertEquals(1, resultado.getId());
        verify(modeloMock, atLeastOnce()).getWrappedData();  // ✅ CAMBIO AQUÍ
    }
    @Test
    void testGetIdByTextConIdNulo() {
        // Act
        TipoAlmacen resultado = tipoAlmacenFrm.getIdByText(null);

        // Assert
        assertNull(resultado, "Debe retornar null con id nulo");
        verify(modeloMock, never()).getWrappedData();
    }

    @Test
    void testGetIdByTextConIdInvalido() {
        // Arrange
        when(modeloMock.getWrappedData()).thenReturn(listaTipoAlmacen);

        // Act
        TipoAlmacen resultado = tipoAlmacenFrm.getIdByText("abc");

        // Assert
        assertNull(resultado, "Debe retornar null con formato inválido");
        verify(modeloMock, atLeastOnce()).getWrappedData();  // ✅ CAMBIO AQUÍ
    }

    @Test
    void testGetIdByTextConModeloVacio() {
        // Arrange
        List<TipoAlmacen> listaVacia = new ArrayList<>();
        when(modeloMock.getWrappedData()).thenReturn(listaVacia);

        // Act
        TipoAlmacen resultado = tipoAlmacenFrm.getIdByText("1");

        // Assert
        assertNull(resultado, "Debe retornar null cuando el modelo está vacío");
        verify(modeloMock, atLeastOnce()).getWrappedData();  // ✅ CAMBIO AQUÍ
    }

    @Test
    void testGetIdByTextConModeloNulo() {
        // Arrange - Forzar modelo a null
        try {
            Field modeloField = DefaultFrm.class.getDeclaredField("modelo");
            modeloField.setAccessible(true);
            modeloField.set(tipoAlmacenFrm, null);
        } catch (Exception e) {
            fail("Error al configurar modelo nulo: " + e.getMessage());
        }

        // Act
        TipoAlmacen resultado = tipoAlmacenFrm.getIdByText("1");

        // Assert
        assertNull(resultado, "Debe manejar modelo nulo correctamente");
    }
}