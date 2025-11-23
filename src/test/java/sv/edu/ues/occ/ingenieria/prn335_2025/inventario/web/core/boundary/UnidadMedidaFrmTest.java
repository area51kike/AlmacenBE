package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoUnidadMedidaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.UnidadMedidaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoUnidadMedida;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.UnidadMedida;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnidadMedidaFrmTest {

    @Mock
    UnidadMedidaDAO unidadMedidaDAO;

    @Mock
    TipoUnidadMedidaDAO tipoUnidadMedidaDAO;

    @Mock
    FacesContext facesContext;

    @InjectMocks
    UnidadMedidaFrm cut;

    private MockedStatic<FacesContext> mockedFacesContext;
    private final PrintStream originalErr = System.err;

    private UnidadMedida mockUnidad;
    private TipoUnidadMedida mockTipo;

    @BeforeEach
    void setUp() {
        // Silenciador de consola
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        }));

        mockedFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

        mockTipo = new TipoUnidadMedida();
        mockTipo.setId(5);
        mockTipo.setNombre("Longitud");
        mockTipo.setActivo(true); // CORRECCIÓN: Importante para que pase el filtro del stream

        mockUnidad = new UnidadMedida();
        mockUnidad.setId(1);
        // mockUnidad.setNombre("Metro"); // Eliminado porque no existe en la entidad
        mockUnidad.setComentarios("Metro");
        mockUnidad.setActivo(true);

        cut.registro = mockUnidad;
    }

    @AfterEach
    void tearDown() {
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
        UnidadMedida nuevo = cut.nuevoRegistro();
        assertNotNull(nuevo);
        assertTrue(nuevo.getActivo());
    }

    @Test
    void testBuscarRegistroPorId_Exito() throws Exception {
        when(unidadMedidaDAO.findById(1)).thenReturn(mockUnidad);
        UnidadMedida result = cut.buscarRegistroPorId(1);
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void testBuscarRegistroPorId_Fallos() throws Exception {
        assertNull(cut.buscarRegistroPorId(null));
        assertNull(cut.buscarRegistroPorId("texto"));

        when(unidadMedidaDAO.findById(anyInt())).thenThrow(new RuntimeException("Error DB"));
        assertNull(cut.buscarRegistroPorId(99));
    }

    @Test
    void testGetIdAsText() {
        assertNull(cut.getIdAsText(null));
        assertNull(cut.getIdAsText(new UnidadMedida()));
        assertEquals("1", cut.getIdAsText(mockUnidad));
    }

    @Test
    void testGetIdByText() throws Exception {
        assertNull(cut.getIdByText(null));
        assertNull(cut.getIdByText(""));
        assertNull(cut.getIdByText("abc"));

        when(unidadMedidaDAO.findById(1)).thenReturn(mockUnidad);
        UnidadMedida result = cut.getIdByText("1");
        assertNotNull(result);
    }

    @Test
    void testGetTiposUnidadMedida_Exito() {
        List<TipoUnidadMedida> lista = new ArrayList<>();
        lista.add(mockTipo); // Este tiene activo=true (ver setUp)

        TipoUnidadMedida inactivo = new TipoUnidadMedida();
        inactivo.setActivo(false);
        lista.add(inactivo);

        when(tipoUnidadMedidaDAO.findAll()).thenReturn(lista);

        List<TipoUnidadMedida> resultado = cut.getTiposUnidadMedida();

        assertEquals(1, resultado.size());
        assertEquals(mockTipo, resultado.get(0));

        cut.getTiposUnidadMedida();
        verify(tipoUnidadMedidaDAO, times(1)).findAll();
    }

    @Test
    void testGetTiposUnidadMedida_Exception() {
        when(tipoUnidadMedidaDAO.findAll()).thenThrow(new RuntimeException("Error"));

        List<TipoUnidadMedida> resultado = cut.getTiposUnidadMedida();
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void testGetSetIdTipoSeleccionado() throws Exception {
        cut.registro.setIdTipoUnidadMedida(mockTipo);
        assertEquals(5, cut.getIdTipoSeleccionado());

        cut.registro.setIdTipoUnidadMedida(null);
        when(tipoUnidadMedidaDAO.findById(5)).thenReturn(mockTipo);

        cut.setIdTipoSeleccionado(5);

        assertEquals(mockTipo, cut.registro.getIdTipoUnidadMedida());

        cut.registro = null;
        cut.setIdTipoSeleccionado(null);
        assertNull(cut.getIdTipoSeleccionado());
    }

    @Test
    void testSetIdTipoSeleccionado_Exception() throws Exception {
        when(tipoUnidadMedidaDAO.findById(anyInt())).thenThrow(new RuntimeException("Error"));
        cut.setIdTipoSeleccionado(10);
        assertNull(cut.registro.getIdTipoUnidadMedida());
    }

    @Test
    void testPrepararNuevo() {
        cut.prepararNuevo();
        assertNotNull(cut.registro);
    }

    @Test
    void testCancelar() {
        when(tipoUnidadMedidaDAO.findById(5)).thenReturn(mockTipo);
        cut.setIdTipoSeleccionado(5);

        cut.cancelar();

        assertNull(cut.registro);
        assertNull(cut.getIdTipoSeleccionado());
    }

    @Test
    void testGuardar_Validacion_SinTipo() {
        cut.setIdTipoSeleccionado(null);

        String nav = cut.guardar();

        assertNull(nav);
        verify(facesContext).addMessage(eq("frmCrear:cbTipoUnidad"), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_ERROR &&
                        m.getDetail().contains("Debe seleccionar un tipo")
        ));
        verify(unidadMedidaDAO, never()).crear(any());
    }

    @Test
    void testGuardar_Crear_Exito() throws Exception {
        UnidadMedidaFrm spyCut = spy(cut);

        Integer idTipo = 5;
        when(tipoUnidadMedidaDAO.findById(idTipo)).thenReturn(mockTipo);

        spyCut.setIdTipoSeleccionado(idTipo);
        spyCut.registro = mockUnidad;
        spyCut.registro.setIdTipoUnidadMedida(null);
        setEstado(spyCut, "CREAR");

        spyCut.guardar();

        verify(tipoUnidadMedidaDAO, times(2)).findById(idTipo);
        assertEquals(mockTipo, mockUnidad.getIdTipoUnidadMedida());

        verify(unidadMedidaDAO).crear(mockUnidad);
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_INFO &&
                        m.getDetail().contains("Registro creado")
        ));
        assertNull(spyCut.getIdTipoSeleccionado());
    }

    @Test
    void testGuardar_Modificar_Exito() throws Exception {
        when(tipoUnidadMedidaDAO.findById(5)).thenReturn(mockTipo);

        cut.registro = mockUnidad;
        cut.setIdTipoSeleccionado(5);
        cut.registro.setIdTipoUnidadMedida(mockTipo);
        setEstado(cut, "MODIFICAR");

        clearInvocations(tipoUnidadMedidaDAO);

        cut.guardar();

        verify(tipoUnidadMedidaDAO, never()).findById(any());
        verify(unidadMedidaDAO).modificar(mockUnidad);
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSummary().equals("Éxito")
        ));
        assertNull(cut.getIdTipoSeleccionado());
    }

    @Test
    void testGuardar_Excepcion() {
        cut.registro = mockUnidad;

        when(tipoUnidadMedidaDAO.findById(5)).thenReturn(mockTipo);
        cut.setIdTipoSeleccionado(5);

        setEstado(cut, "CREAR");

        doThrow(new RuntimeException("Error Fatal")).when(unidadMedidaDAO).crear(any());

        cut.guardar();

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_ERROR &&
                        m.getDetail().contains("Error Fatal")
        ));
    }

    @Test
    void testGuardar_RegistroNull() {
        cut.registro = null;
        cut.guardar();
        verify(unidadMedidaDAO, never()).crear(any());
        verify(unidadMedidaDAO, never()).modificar(any());
    }

    @Test
    void testEliminar_Exito() throws Exception {
        cut.registro = mockUnidad;

        cut.eliminar();

        verify(unidadMedidaDAO).eliminar(mockUnidad);
        assertNull(cut.registro);
    }

    @Test
    void testEliminar_Excepcion() throws Exception {
        cut.registro = mockUnidad;
        doThrow(new RuntimeException("No se puede borrar")).when(unidadMedidaDAO).eliminar(any());

        cut.eliminar();

        verify(unidadMedidaDAO).eliminar(mockUnidad);
        assertNotNull(cut.registro);
    }

    @Test
    void testEliminar_RegistroNull() {
        cut.registro = null;
        cut.eliminar();
        verify(unidadMedidaDAO, never()).eliminar(any());
    }

    private void setEstado(UnidadMedidaFrm bean, String nombreEstado) {
        try {
            java.lang.reflect.Field field = bean.getClass().getSuperclass().getDeclaredField("estado");
            field.setAccessible(true);
            Object[] enumConstants = field.getType().getEnumConstants();
            for (Object constant : enumConstants) {
                if (constant.toString().equals(nombreEstado)) {
                    field.set(bean, constant);
                    break;
                }
            }
        } catch (Exception e) {
        }
    }
}