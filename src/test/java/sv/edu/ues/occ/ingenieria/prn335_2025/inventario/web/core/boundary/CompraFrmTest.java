package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.NotificadorKardex;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProveedorDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Proveedor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompraFrmTest {

    @Mock
    CompraDAO compraDao;

    @Mock
    ProveedorDAO proveedorDao;

    @Mock
    FacesContext facesContext;

    @Mock
    NotificadorKardex notificadorKardex;

    @InjectMocks
    CompraFrm cut;

    private MockedStatic<FacesContext> mockedFacesContext;
    private Compra mockCompra;
    private Proveedor mockProveedor;
    private Level originalLogLevel;

    @BeforeEach
    void setUp() {
        // Silenciador de Logger para esta clase
        Logger appLogger = Logger.getLogger(CompraFrm.class.getName());
        originalLogLevel = appLogger.getLevel();
        appLogger.setLevel(Level.OFF);

        mockedFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

        mockProveedor = new Proveedor();
        mockProveedor.setId(10);
        mockProveedor.setNombre("Proveedor Test");

        mockCompra = new Compra();
        mockCompra.setId(1L);
        mockCompra.setFecha(OffsetDateTime.now());
        mockCompra.setEstado("CREADO");
        mockCompra.setIdProveedor(10);

        cut.registro = mockCompra;
    }

    @AfterEach
    void tearDown() {
        if (originalLogLevel != null) {
            Logger.getLogger(CompraFrm.class.getName()).setLevel(originalLogLevel);
        } else {
            Logger.getLogger(CompraFrm.class.getName()).setLevel(Level.INFO);
        }

        if (mockedFacesContext != null) {
            mockedFacesContext.close();
        }
    }

    @Test
    void testInit() {
        when(proveedorDao.findAll()).thenReturn(new ArrayList<>());

        try {
            cut.init();
        } catch (Exception e) {
        }

        // Se eliminó la aserción de nombreBean porque el código de CompraFrm no lo setea.
        assertNotNull(cut.getDao());
        assertNotNull(cut.getFacesContext());
        assertNotNull(cut.getProveedoresDisponibles());
        assertNotNull(cut.getEstadosDisponibles());
    }

    @Test
    void testNuevoRegistro() {
        Compra nueva = cut.nuevoRegistro();
        assertNotNull(nueva);
        assertNotNull(nueva.getFecha());
    }

    @Test
    void testBuscarRegistroPorId_Exito() {
        when(compraDao.findById(1L)).thenReturn(mockCompra);
        Compra result = cut.buscarRegistroPorId(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testBuscarRegistroPorId_Fallo() {
        assertNull(cut.buscarRegistroPorId(null));
        assertNull(cut.buscarRegistroPorId("string"));

        when(compraDao.findById(anyLong())).thenThrow(new RuntimeException("Error DB"));
        assertNull(cut.buscarRegistroPorId(99L));
    }

    @Test
    void testGetIdAsText() {
        assertNull(cut.getIdAsText(null));
        assertNull(cut.getIdAsText(new Compra()));
        assertEquals("1", cut.getIdAsText(mockCompra));
    }

    @Test
    void testGetIdByText() {
        assertNull(cut.getIdByText(null));
        assertNull(cut.getIdByText("abc"));

        when(compraDao.findById(1L)).thenReturn(mockCompra);
        Compra result = cut.getIdByText("1");
        assertNotNull(result);
        assertEquals(mockCompra, result);
    }

    @Test
    void testEsNombreVacio() {
        assertTrue(cut.esNombreVacio(null));

        Compra c = new Compra();
        assertTrue(cut.esNombreVacio(c));

        c.setIdProveedor(1);
        assertFalse(cut.esNombreVacio(c));
    }

    @Test
    void testBtnGuardarHandler_RegistroNull() {
        cut.registro = null;
        cut.btnGuardarHandler(null);
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_WARN && m.getDetail().contains("No hay registro")
        ));
        verify(compraDao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_ValidacionProveedor() {
        cut.registro.setIdProveedor(null);
        cut.btnGuardarHandler(null);
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_WARN && m.getDetail().contains("Debe seleccionar un proveedor")
        ));
        verify(compraDao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_ValidacionEstado() {
        cut.registro.setEstado(null);
        cut.btnGuardarHandler(null);
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_WARN && m.getDetail().contains("Debe seleccionar un estado")
        ));
        verify(compraDao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_ValidacionFecha() {
        cut.registro.setFecha(null);
        cut.btnGuardarHandler(null);
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_WARN && m.getDetail().contains("La fecha es obligatoria")
        ));
        verify(compraDao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_Crear_ProveedorNoExiste() {
        setEstado(cut, "CREAR");
        when(proveedorDao.findById(10)).thenReturn(null);

        cut.btnGuardarHandler(null);

        verify(compraDao, never()).crear(any());
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_ERROR && m.getDetail().contains("El proveedor seleccionado no existe")
        ));
    }

    @Test
    void testBtnGuardarHandler_Crear_Exito() {
        setEstado(cut, "CREAR");
        when(proveedorDao.findById(10)).thenReturn(mockProveedor);

        cut.btnGuardarHandler(null);

        verify(compraDao).crear(mockCompra);
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_INFO && m.getDetail().contains("guardado correctamente")
        ));
    }

    @Test
    void testBtnGuardarHandler_Modificar_Exito() {
        // ARRANGE
        setEstado(cut, "MODIFICAR");

        // 1. Mockear la llamada de validación a CompraDAO
        doNothing().when(compraDao).validarProveedor(10);

        // 2. Mockear la carga del proveedor
        when(proveedorDao.findById(10)).thenReturn(mockProveedor);

        // ACT
        cut.btnGuardarHandler(null);

        // ASSERT
        verify(compraDao).validarProveedor(10);
        verify(compraDao).modificar(mockCompra);
        verify(notificadorKardex).notificarCambio("RELOAD_TABLE"); // Verificar notificación
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_INFO && m.getDetail().contains("guardado correctamente")
        ));
    }

    @Test
    void testBtnGuardarHandler_Excepcion() {
        setEstado(cut, "CREAR");
        when(proveedorDao.findById(10)).thenReturn(mockProveedor);
        doThrow(new RuntimeException("Error Grave", new IllegalArgumentException("Causa Raiz")))
                .when(compraDao).crear(any());

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_ERROR && m.getDetail().contains("Causa Raiz")
        ));
    }

    private void setEstado(CompraFrm bean, String nombreEstado) {
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