package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.model.LazyDataModel;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ClienteDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Cliente;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Venta;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaFrmTest {

    @Mock
    VentaDAO ventaDao;

    @Mock
    ClienteDAO clienteDao;

    @Mock
    FacesContext facesContext;

    @Mock
    LazyDataModel<Venta> mockModelo;

    @InjectMocks
    VentaFrm cut;

    private MockedStatic<FacesContext> mockedFacesContext;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private Venta mockVenta;
    private Cliente mockCliente;
    private UUID uuidVenta;
    private UUID uuidCliente;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));
        System.setErr(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));

        mockedFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

        uuidVenta = UUID.randomUUID();
        uuidCliente = UUID.randomUUID();

        mockCliente = new Cliente();
        mockCliente.setId(uuidCliente);
        mockCliente.setNombre("Cliente Test");

        mockVenta = new Venta();
        mockVenta.setId(uuidVenta);
        mockVenta.setIdCliente(mockCliente);
        mockVenta.setEstado("CREADA");
        mockVenta.setFecha(OffsetDateTime.now());

        cut.setRegistro(mockVenta);

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
    void testInicializar() {
        when(clienteDao.findAll()).thenReturn(List.of(mockCliente));

        cut.inicializar();

        assertEquals("Gestion de Ventas", cut.nombreBean);
        assertNotNull(cut.getDao());
        assertNotNull(cut.getClientesDisponibles());
        assertFalse(cut.getClientesDisponibles().isEmpty());
        assertNotNull(cut.getEstadosDisponibles());
    }

    @Test
    void testInicializar_Excepcion() {
        when(clienteDao.findAll()).thenThrow(new RuntimeException("DB Error"));

        cut.inicializar();

        assertNotNull(cut.getClientesDisponibles());
        assertTrue(cut.getClientesDisponibles().isEmpty());
    }

    @Test
    void testNuevoRegistro() {
        Venta nuevo = cut.nuevoRegistro();
        assertNotNull(nuevo);
        assertNotNull(nuevo.getId());
        assertNotNull(nuevo.getIdCliente());
        assertNotNull(nuevo.getFecha());

        // ANTES (Falla porque espera null, pero el código asigna "CREADA")
        // assertNull(nuevo.getEstado());

        // DESPUÉS (Espera el valor correcto)
        assertEquals("CREADA", nuevo.getEstado());
    }

    @Test
    void testBuscarRegistroPorId_Exito() {
        when(ventaDao.findById(uuidVenta)).thenReturn(mockVenta);

        Venta result = cut.buscarRegistroPorId(uuidVenta);
        assertNotNull(result);
        assertEquals(uuidVenta, result.getId());

        result = cut.buscarRegistroPorId(uuidVenta.toString());
        assertNotNull(result);
        assertEquals(uuidVenta, result.getId());
    }

    @Test
    void testBuscarRegistroPorId_Fallo() {
        assertNull(cut.buscarRegistroPorId(null));

        assertNull(cut.buscarRegistroPorId("invalid-uuid"));

        when(ventaDao.findById(any(UUID.class))).thenThrow(new RuntimeException("DB Error"));
        assertNull(cut.buscarRegistroPorId(uuidVenta));
    }

    @Test
    void testGetIdAsText() {
        assertEquals(uuidVenta.toString(), cut.getIdAsText(mockVenta));
        assertNull(cut.getIdAsText(null));
        assertNull(cut.getIdAsText(new Venta()));
    }

    @Test
    void testGetIdByText_Exito() {
        when(ventaDao.findById(uuidVenta)).thenReturn(mockVenta);

        Venta result = cut.getIdByText(uuidVenta.toString());
        assertNotNull(result);
        assertEquals(uuidVenta, result.getId());
    }

    @Test
    void testGetIdByText_Fallo() {
        assertNull(cut.getIdByText(null));
        assertNull(cut.getIdByText(""));
        assertNull(cut.getIdByText("invalid-uuid"));
    }

    @Test
    void testEsNombreVacio() {
        // 1. Caso Válido
        Venta valido = new Venta();
        valido.setIdCliente(mockCliente);
        valido.setEstado("CREADA");
        valido.setFecha(OffsetDateTime.now());
        assertFalse(cut.esNombreVacio(valido));

        // 2. Caso Null (Dispara mensaje "Debe seleccionar un cliente" porque entra en el primer if)
        assertTrue(cut.esNombreVacio(null));
        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("Debe seleccionar un cliente")));
        clearInvocations(facesContext); // IMPORTANTE: Limpiar para la siguiente verificación

        // 3. Sin Cliente (Dispara mensaje)
        Venta sinCliente = new Venta();
        sinCliente.setEstado("CREADA");
        sinCliente.setFecha(OffsetDateTime.now());
        assertTrue(cut.esNombreVacio(sinCliente));
        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("Debe seleccionar un cliente")));
        clearInvocations(facesContext);

        // 4. Cliente Vacio (Dispara mensaje)
        Venta clienteVacio = new Venta();
        clienteVacio.setIdCliente(new Cliente());
        clienteVacio.setEstado("CREADA");
        clienteVacio.setFecha(OffsetDateTime.now());
        assertTrue(cut.esNombreVacio(clienteVacio));
        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("Debe seleccionar un cliente")));
        clearInvocations(facesContext);

        // 5. Sin Estado (Dispara mensaje de estado)
        Venta sinEstado = new Venta();
        sinEstado.setIdCliente(mockCliente);
        sinEstado.setFecha(OffsetDateTime.now());
        assertTrue(cut.esNombreVacio(sinEstado));
        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("Debe seleccionar un estado")));
        clearInvocations(facesContext);

        // 6. Sin Fecha (Dispara mensaje de fecha)
        Venta sinFecha = new Venta();
        sinFecha.setIdCliente(mockCliente);
        sinFecha.setEstado("CREADA");
        assertTrue(cut.esNombreVacio(sinFecha));
        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("Debe seleccionar una fecha")));
    }

    @Test
    void testBtnGuardarHandler_RegistroNull() {
        cut.setRegistro(null);
        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_WARN && m.getDetail().contains("No hay registro")
        ));
        verify(ventaDao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_ValidacionFallida() {
        cut.getRegistro().setEstado(null); // Invalida
        cut.btnGuardarHandler(null);

        verify(ventaDao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_ClienteNoEncontrado() {
        when(clienteDao.findById(uuidCliente)).thenReturn(null);

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_ERROR && m.getDetail().contains("Cliente no encontrado")
        ));
        verify(ventaDao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_Exito() {
        when(clienteDao.findById(uuidCliente)).thenReturn(mockCliente);

        cut.btnGuardarHandler(null);

        assertEquals(mockCliente, mockVenta.getIdCliente());
        verify(ventaDao).crear(mockVenta);

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());

        assertNull(cut.getRegistro());
    }

    @Test
    void testBtnGuardarHandler_Excepcion() {
        when(clienteDao.findById(uuidCliente)).thenReturn(mockCliente);
        doThrow(new RuntimeException("Error Fatal")).when(ventaDao).crear(any());

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_ERROR && m.getDetail().contains("Error Fatal")
        ));
    }

    @Test
    void testGetClientesDisponibles() {
        when(clienteDao.findAll()).thenReturn(List.of(mockCliente));

        List<Cliente> result = cut.getClientesDisponibles();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(mockCliente, result.get(0));

        // Cache
        cut.getClientesDisponibles();
        verify(clienteDao, times(1)).findAll();
    }

    @Test
    void testGetClientesDisponibles_Exception() {
        when(clienteDao.findAll()).thenThrow(new RuntimeException("Error"));

        List<Cliente> result = cut.getClientesDisponibles();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetEstadosDisponibles() {
        List<String> estados = cut.getEstadosDisponibles();
        assertNotNull(estados);
        assertTrue(estados.contains("CREADA"));
    }
}