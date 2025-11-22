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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.OutputStream;
import java.io.PrintStream;
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

    // Variable para guardar el flujo de error original y restaurarlo
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        // --- SILENCIADOR DE CONSOLA (System.err) ---
        // Redirigimos la salida de error a un "agujero negro" para ocultar los stack traces
        // generados por e.printStackTrace() en el código probado.
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                // No hacer nada
            }
        }));
        // ------------------------------------------

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
    }

    @AfterEach
    void tearDown() {
        // Restauramos la salida de error original para no afectar otros tests
        System.setErr(originalErr);

        if (mockedFacesContext != null) {
            mockedFacesContext.close();
        }
    }

    @Test
    void testInit() {
        assertEquals("Característica", cut.nombreBean);
        assertNotNull(cut.getDao());
        // Verifica que getFacesContext devuelve el mock configurado estáticamente
        assertNotNull(cut.getFacesContext());
    }

    @Test
    void testNuevoRegistro() {
        Caracteristica nuevo = cut.nuevoRegistro();
        assertNotNull(nuevo);
        assertTrue(nuevo.getActivo());
    }

    @Test
    void testBuscarRegistroPorId_Exito() throws Exception {
        when(caracteristicaDAO.findById(1)).thenReturn(mockCaracteristica);
        Caracteristica result = cut.buscarRegistroPorId(1);
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void testBuscarRegistroPorId_Fallos() throws Exception {
        assertNull(cut.buscarRegistroPorId(null));
        assertNull(cut.buscarRegistroPorId("texto")); // No es Integer

        when(caracteristicaDAO.findById(anyInt())).thenThrow(new RuntimeException("Error DB"));
        assertNull(cut.buscarRegistroPorId(99)); // Catch exception
    }

    @Test
    void testGetIdAsText() {
        assertNull(cut.getIdAsText(null));
        assertNull(cut.getIdAsText(new Caracteristica())); // ID null
        assertEquals("1", cut.getIdAsText(mockCaracteristica));
    }

    @Test
    void testGetIdByText() throws Exception {
        assertNull(cut.getIdByText(null));
        assertNull(cut.getIdByText(""));
        assertNull(cut.getIdByText("abc")); // NumberFormat

        when(caracteristicaDAO.findById(1)).thenReturn(mockCaracteristica);
        Caracteristica result = cut.getIdByText("1");
        assertNotNull(result);
    }

    @Test
    void testGetTiposUnidadMedida_Exito() {
        List<TipoUnidadMedida> lista = new ArrayList<>();
        lista.add(mockTipo);
        TipoUnidadMedida inactivo = new TipoUnidadMedida(); inactivo.setActivo(false);
        lista.add(inactivo);

        when(tipoUnidadMedidaDAO.findAll()).thenReturn(lista);

        List<TipoUnidadMedida> resultado = cut.getTiposUnidadMedida();

        assertEquals(1, resultado.size()); // Solo el activo
        assertEquals(mockTipo, resultado.get(0));

        // Verificar que usa caché (segunda llamada no va a BD)
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
        // 1. Getter devuelve el del registro si existe
        cut.registro.setIdTipoUnidadMedida(mockTipo);
        assertEquals(5, cut.getIdTipoSeleccionado());

        // 2. Setter busca en BD y asigna
        cut.registro.setIdTipoUnidadMedida(null);
        when(tipoUnidadMedidaDAO.findById(5)).thenReturn(mockTipo);

        cut.setIdTipoSeleccionado(5);

        assertEquals(mockTipo, cut.registro.getIdTipoUnidadMedida());

        // 3. Getter devuelve null si todo es null
        cut.registro = null;
        cut.setIdTipoSeleccionado(null); // reset variable local
        assertNull(cut.getIdTipoSeleccionado());
    }

    @Test
    void testSetIdTipoSeleccionado_Exception() throws Exception {
        when(tipoUnidadMedidaDAO.findById(anyInt())).thenThrow(new RuntimeException("Error"));
        cut.setIdTipoSeleccionado(10);
        // No debe fallar, catch silencioso
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
        verify(caracteristicaDAO, never()).crear(any());
    }

    @Test
    void testGuardar_Crear_Exito() throws Exception {
        CaracteristicaFrm spyCut = spy(cut);

        Integer idTipo = 5;
        when(tipoUnidadMedidaDAO.findById(idTipo)).thenReturn(mockTipo);

        spyCut.setIdTipoSeleccionado(idTipo);
        spyCut.registro = mockCaracteristica;
        spyCut.registro.setIdTipoUnidadMedida(null);
        setEstado(spyCut, "CREAR");

        spyCut.guardar();
        verify(tipoUnidadMedidaDAO, times(2)).findById(idTipo);

        // Verificamos sobre el objeto mock, no sobre el registro del bean que se hace null
        assertEquals(mockTipo, mockCaracteristica.getIdTipoUnidadMedida());

        verify(caracteristicaDAO).crear(mockCaracteristica);
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_INFO &&
                        m.getDetail().contains("Registro creado")
        ));
    }

    @Test
    void testGuardar_Modificar_Exito() throws Exception {
        // Configuración
        when(tipoUnidadMedidaDAO.findById(5)).thenReturn(mockTipo);

        cut.registro = mockCaracteristica;
        cut.setIdTipoSeleccionado(5); // Llama al DAO
        cut.registro.setIdTipoUnidadMedida(mockTipo); // Ya tiene tipo
        setEstado(cut, "MODIFICAR");

        clearInvocations(tipoUnidadMedidaDAO);

        cut.guardar();

        verify(tipoUnidadMedidaDAO, never()).findById(any());
        verify(caracteristicaDAO).modificar(mockCaracteristica);
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSummary().equals("Éxito")
        ));
    }

    @Test
    void testGuardar_Excepcion() {
        cut.registro = mockCaracteristica;

        when(tipoUnidadMedidaDAO.findById(5)).thenReturn(mockTipo);
        cut.setIdTipoSeleccionado(5);

        setEstado(cut, "CREAR");

        doThrow(new RuntimeException("Error Fatal")).when(caracteristicaDAO).crear(any());

        cut.guardar();

        // El e.printStackTrace() se va al agujero negro, no saldrá en consola
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_ERROR &&
                        m.getDetail().contains("Error Fatal")
        ));
    }

    @Test
    void testGuardar_RegistroNull() {
        cut.registro = null;
        cut.guardar();
        verify(caracteristicaDAO, never()).crear(any());
        verify(caracteristicaDAO, never()).modificar(any());
    }

    @Test
    void testEliminar_Exito() throws Exception {
        cut.registro = mockCaracteristica;

        cut.eliminar();

        verify(caracteristicaDAO).eliminar(mockCaracteristica);
        assertNull(cut.registro);
    }

    @Test
    void testEliminar_Excepcion() throws Exception {
        cut.registro = mockCaracteristica;
        doThrow(new RuntimeException("No se puede borrar")).when(caracteristicaDAO).eliminar(any());
        cut.eliminar();

        verify(caracteristicaDAO).eliminar(mockCaracteristica);

        assertNotNull(cut.registro);
    }

    @Test
    void testEliminar_RegistroNull() {
        cut.registro = null;
        cut.eliminar();
        verify(caracteristicaDAO, never()).eliminar(any());
    }

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