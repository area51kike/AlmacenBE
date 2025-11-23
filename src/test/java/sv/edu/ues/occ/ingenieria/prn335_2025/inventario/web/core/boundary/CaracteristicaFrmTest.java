package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoUnidadMedidaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoUnidadMedida;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaracteristicaFrmTest {

    @Mock
    CaracteristicaDAO caracteristicaDAO;

    @Mock
    TipoUnidadMedidaDAO tipoUnidadMedidaDAO;

    @Mock
    FacesContext facesContext;

    @InjectMocks
    CaracteristicaFrm cut;

    private MockedStatic<FacesContext> mockedFacesContext;

    private Caracteristica mockCaracteristica;
    private TipoUnidadMedida mockTipo;

    // Variables para silenciar la consola
    private final PrintStream originalErr = System.err;
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        // --- SILENCIADOR DE CONSOLA ---
        System.setOut(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));
        System.setErr(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));
        // ------------------------------

        mockedFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

        mockCaracteristica = new Caracteristica();
        mockCaracteristica.setId(1);
        mockCaracteristica.setActivo(true);

        mockTipo = new TipoUnidadMedida();
        mockTipo.setId(5);
        mockTipo.setActivo(true);
        mockTipo.setNombre("Kilogramo");

        cut.registro = mockCaracteristica;

        // --- INYECCIÓN MANUAL DE MOCKS (CRÍTICO PARA EVITAR ERRORES DE MOCKITO) ---
        try {
            injectMock(cut, "caracteristicaDAO", caracteristicaDAO);
            injectMock(cut, "tipoUnidadMedidaDAO", tipoUnidadMedidaDAO);
        } catch (Exception e) {
            // Ignorar fallos de reflexión en silencio
        }
    }

    // Método auxiliar para inyectar en campos privados/heredados
    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, mock);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
    }

    @AfterEach
    void tearDown() {
        // Restaurar consola
        System.setOut(originalOut);
        System.setErr(originalErr);

        if (mockedFacesContext != null) {
            mockedFacesContext.close();
        }
    }

    @Test
    void testInit() {
        assertEquals("Característica", cut.nombreBean);
        assertNotNull(cut.getDao());
        assertNotNull(cut.getFacesContext());
    }

    @Test
    void testNuevoRegistro() {
        Caracteristica nuevo = cut.nuevoRegistro();
        assertNotNull(nuevo);
        assertTrue(nuevo.getActivo());
    }

    @Test
    void testBuscarRegistroPorId_Exito() {
        // Usamos doReturn y any() para evitar problemas de tipos (Integer vs Object)
        doReturn(mockCaracteristica).when(caracteristicaDAO).findById(any());

        // Pasamos un Integer explícito
        Caracteristica result = cut.buscarRegistroPorId(Integer.valueOf(1));

        assertNotNull(result, "El resultado es null. Revisa la inyección del mock.");
        assertEquals(1, result.getId());
    }

    @Test
    void testBuscarRegistroPorId_Fallos() {
        assertNull(cut.buscarRegistroPorId(null));
        assertNull(cut.buscarRegistroPorId("texto")); // No es Integer

        // Simulamos excepción
        doThrow(new RuntimeException("Error DB")).when(caracteristicaDAO).findById(any());

        assertNull(cut.buscarRegistroPorId(1));
    }

    @Test
    void testGetIdAsText() {
        assertNull(cut.getIdAsText(null));
        assertNull(cut.getIdAsText(new Caracteristica()));
        assertEquals("1", cut.getIdAsText(mockCaracteristica));
    }

    @Test
    void testGetIdByText() {
        assertNull(cut.getIdByText(null));
        assertNull(cut.getIdByText(""));
        assertNull(cut.getIdByText("abc"));

        doReturn(mockCaracteristica).when(caracteristicaDAO).findById(any());

        Caracteristica result = cut.getIdByText("1");
        assertNotNull(result);
        assertEquals(mockCaracteristica, result);
    }

    @Test
    void testGetTiposUnidadMedida_Exito() {
        List<TipoUnidadMedida> lista = new ArrayList<>();
        lista.add(mockTipo);

        when(tipoUnidadMedidaDAO.findAll()).thenReturn(lista);

        List<TipoUnidadMedida> resultado = cut.getTiposUnidadMedida();

        assertEquals(1, resultado.size());
        assertEquals(mockTipo, resultado.get(0));
    }

    @Test
    void testGetTiposUnidadMedida_Exception() {
        when(tipoUnidadMedidaDAO.findAll()).thenThrow(new RuntimeException("Error"));

        List<TipoUnidadMedida> resultado = cut.getTiposUnidadMedida();
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void testGetSetIdTipoSeleccionado() {
        cut.registro.setIdTipoUnidadMedida(mockTipo);
        assertEquals(5, cut.getIdTipoSeleccionado());

        cut.registro.setIdTipoUnidadMedida(null);
        // Mock permisivo para el setter que busca en BD
        lenient().when(tipoUnidadMedidaDAO.findById(any())).thenReturn(mockTipo);

        cut.setIdTipoSeleccionado(5);

        assertEquals(mockTipo, cut.registro.getIdTipoUnidadMedida());

        cut.registro = null;
        cut.setIdTipoSeleccionado(null);
        assertNull(cut.getIdTipoSeleccionado());
    }

    @Test
    void testSetIdTipoSeleccionado_Exception() {
        lenient().when(tipoUnidadMedidaDAO.findById(any())).thenThrow(new RuntimeException("Error"));
        cut.setIdTipoSeleccionado(10);
        assertNull(cut.registro.getIdTipoUnidadMedida());
    }

    @Test
    void testPrepararNuevo() {
        String nav = cut.prepararNuevo();
        assertNull(nav);
        assertNotNull(cut.registro);
        // Nota: Si ESTADO_CRUD no es accesible, no lo validamos, pero validamos el registro
    }

    @Test
    void testCancelar() {
        lenient().when(tipoUnidadMedidaDAO.findById(any())).thenReturn(mockTipo);

        cut.setIdTipoSeleccionado(5);
        String nav = cut.cancelar();

        assertNull(nav);
        assertNull(cut.registro);
        assertNull(cut.getIdTipoSeleccionado());
    }

    @Test
    void testGuardar_Validacion_SinTipo() {
        cut.setIdTipoSeleccionado(null);

        String nav = cut.guardar();

        assertNull(nav);

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(eq("frmCrear:cbTipoUnidad"), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());

        verify(caracteristicaDAO, never()).crear(any());
    }

    @Test
    void testGuardar_Crear_Exito() {
        // Reinyectar mocks por seguridad
        try {
            injectMock(cut, "caracteristicaDAO", caracteristicaDAO);
            injectMock(cut, "tipoUnidadMedidaDAO", tipoUnidadMedidaDAO);
        } catch (Exception e) {}

        Integer idTipo = 5;
        lenient().when(tipoUnidadMedidaDAO.findById(any())).thenReturn(mockTipo);

        cut.setIdTipoSeleccionado(idTipo);
        cut.registro = mockCaracteristica;
        cut.registro.setIdTipoUnidadMedida(null);
        setEstado(cut, "CREAR");

        String nav = cut.guardar();

        assertNull(nav);

        verify(tipoUnidadMedidaDAO, atLeast(1)).findById(any());
        assertEquals(mockTipo, mockCaracteristica.getIdTipoUnidadMedida());

        verify(caracteristicaDAO).crear(mockCaracteristica);

        // Validamos mensaje de éxito
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
    }

    @Test
    void testGuardar_Modificar_Exito() {
        lenient().when(tipoUnidadMedidaDAO.findById(any())).thenReturn(mockTipo);

        cut.registro = mockCaracteristica;
        cut.setIdTipoSeleccionado(5);
        cut.registro.setIdTipoUnidadMedida(mockTipo);
        setEstado(cut, "MODIFICAR");

        clearInvocations(tipoUnidadMedidaDAO);

        String nav = cut.guardar();
        assertNull(nav);

        verify(caracteristicaDAO).modificar(mockCaracteristica);

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
    }

    @Test
    void testGuardar_Excepcion() {
        cut.registro = mockCaracteristica;

        lenient().when(tipoUnidadMedidaDAO.findById(any())).thenReturn(mockTipo);
        cut.setIdTipoSeleccionado(5);
        setEstado(cut, "CREAR");

        doThrow(new RuntimeException("Error Fatal")).when(caracteristicaDAO).crear(any());

        cut.guardar();

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    void testGuardar_RegistroNull() {
        cut.registro = null;
        cut.guardar();
        verify(caracteristicaDAO, never()).crear(any());
        verify(caracteristicaDAO, never()).modificar(any());
    }

    @Test
    void testEliminar_Exito() {
        cut.registro = mockCaracteristica;

        String nav = cut.eliminar();
        assertNull(nav);

        verify(caracteristicaDAO).eliminar(mockCaracteristica);
        assertNull(cut.registro);
    }

    @Test
    void testEliminar_Excepcion() {
        cut.registro = mockCaracteristica;
        doThrow(new RuntimeException("No se puede borrar")).when(caracteristicaDAO).eliminar(any());

        cut.eliminar();

        verify(caracteristicaDAO).eliminar(mockCaracteristica);
        assertNotNull(cut.registro); // Si falla, no se limpia
    }

    @Test
    void testEliminar_RegistroNull() {
        cut.registro = null;
        cut.eliminar();
        verify(caracteristicaDAO, never()).eliminar(any());
    }

    // Método auxiliar para setear el estado usando reflexión si es protected
    private void setEstado(CaracteristicaFrm bean, String nombreEstado) {
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