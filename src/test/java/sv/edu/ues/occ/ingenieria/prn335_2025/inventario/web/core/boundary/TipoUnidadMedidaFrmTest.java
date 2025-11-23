package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.context.FacesContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoUnidadMedidaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoUnidadMedida;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.model.LazyDataModel;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoUnidadMedidaFrmTest {

    @Mock
    FacesContext facesContext;

    @Mock
    TipoUnidadMedidaDAO tipoUnidadMedidaDAO;

    @Mock
    LazyDataModel<TipoUnidadMedida> mockModelo;

    @InjectMocks
    TipoUnidadMedidaFrm cut;

    private MockedStatic<FacesContext> mockedFacesContext;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private TipoUnidadMedida mockEntity;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));
        System.setErr(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));

        mockedFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

        mockEntity = new TipoUnidadMedida();
        mockEntity.setId(1);
        mockEntity.setNombre("Litro");

        try {
            Class<?> currentClass = cut.getClass();
            Field modeloField = null;
            while (currentClass != null) {
                try {
                    modeloField = currentClass.getDeclaredField("modelo");
                    break;
                } catch (NoSuchFieldException e) {
                    currentClass = currentClass.getSuperclass();
                }
            }
            if (modeloField != null) {
                modeloField.setAccessible(true);
                modeloField.set(cut, mockModelo);
            }
        } catch (Exception e) {
        }
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        if (mockedFacesContext != null) {
            mockedFacesContext.close();
        }
    }

    @Test
    void testInit() {
        assertEquals("Unidad de Medida", cut.nombreBean);
        assertNotNull(cut.getFacesContext());
        assertNotNull(cut.getDao());
    }

    @Test
    void testNuevoRegistro() {
        TipoUnidadMedida nuevo = cut.nuevoRegistro();
        assertNotNull(nuevo);
    }

    @Test
    void testBuscarRegistroPorId_Exito() {
        when(mockModelo.getWrappedData()).thenReturn(Collections.emptyList());
        when(tipoUnidadMedidaDAO.findAll()).thenReturn(List.of(mockEntity));

        TipoUnidadMedida result = cut.buscarRegistroPorId(1);
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void testBuscarRegistroPorId_NoVacio() {
        when(mockModelo.getWrappedData()).thenReturn(List.of(new TipoUnidadMedida()));

        TipoUnidadMedida result = cut.buscarRegistroPorId(1);
        assertNull(result);
        verify(tipoUnidadMedidaDAO, never()).findAll();
    }

    @Test
    void testBuscarRegistroPorId_NoEncontrado() {
        when(mockModelo.getWrappedData()).thenReturn(Collections.emptyList());
        when(tipoUnidadMedidaDAO.findAll()).thenReturn(List.of(mockEntity));

        TipoUnidadMedida result = cut.buscarRegistroPorId(99);
        assertNull(result);
    }

    @Test
    void testBuscarRegistroPorId_TipoIncorrecto() {
        assertNull(cut.buscarRegistroPorId("no-int"));
        assertNull(cut.buscarRegistroPorId(null));
    }

    @Test
    void testGetIdAsText() {
        assertEquals("1", cut.getIdAsText(mockEntity));
        assertNull(cut.getIdAsText(new TipoUnidadMedida()));
        assertNull(cut.getIdAsText(null));
    }

    @Test
    void testGetIdByText_Exito() {
        when(mockModelo.getWrappedData()).thenReturn(List.of(mockEntity));

        TipoUnidadMedida result = cut.getIdByText("1");
        assertNotNull(result);
        assertEquals(mockEntity, result);
    }

    @Test
    void testGetIdByText_NoEncontrado() {
        when(mockModelo.getWrappedData()).thenReturn(List.of(mockEntity));

        TipoUnidadMedida result = cut.getIdByText("99");
        assertNull(result);
    }

    @Test
    void testGetIdByText_FormatoInvalido() {
        when(mockModelo.getWrappedData()).thenReturn(List.of(mockEntity));

        TipoUnidadMedida result = cut.getIdByText("texto");
        assertNull(result);
    }

    @Test
    void testGetIdByText_CondicionesIniciales() {
        assertNull(cut.getIdByText(null));

        when(mockModelo.getWrappedData()).thenReturn(Collections.emptyList());
        assertNull(cut.getIdByText("1"));

        try {
            Class<?> currentClass = cut.getClass();
            Field modeloField = null;
            while (currentClass != null) {
                try { modeloField = currentClass.getDeclaredField("modelo"); break; }
                catch (NoSuchFieldException e) { currentClass = currentClass.getSuperclass(); }
            }
            if (modeloField != null) {
                modeloField.setAccessible(true);
                modeloField.set(cut, null);
            }
            assertNull(cut.getIdByText("1"));
        } catch (Exception e) {
        }
    }
}