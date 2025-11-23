package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Venta;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.VentaDetalle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaDetalleFrmTest {

    @Mock
    VentaDetalleDAO ventaDetalleDao;

    @Mock
    VentaDAO ventaDao;

    @Mock
    ProductoDAO productoDao;

    @Mock
    FacesContext facesContext;

    @InjectMocks
    VentaDetalleFrm cut;

    private MockedStatic<FacesContext> mockedFacesContext;
    private VentaDetalle mockDetalle;
    private Venta mockVenta;
    private Producto mockProducto;
    private UUID uuidPrueba;
    private Level originalLogLevel;

    @BeforeEach
    void setUp() {
        Logger appLogger = Logger.getLogger(VentaDetalleFrm.class.getName());
        originalLogLevel = appLogger.getLevel();
        appLogger.setLevel(Level.OFF);

        mockedFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

        uuidPrueba = UUID.randomUUID();
        mockDetalle = new VentaDetalle();
        mockDetalle.setId(uuidPrueba);
        mockDetalle.setCantidad(BigDecimal.TEN);
        mockDetalle.setPrecio(BigDecimal.valueOf(150.00));
        mockDetalle.setEstado("PENDIENTE");

        mockVenta = new Venta();
        mockVenta.setId(UUID.randomUUID());

        mockProducto = new Producto();
        mockProducto.setId(UUID.randomUUID());
        mockProducto.setNombreProducto("Producto Venta Test");

        mockDetalle.setIdVenta(mockVenta);
        mockDetalle.setIdProducto(mockProducto);

        cut.registro = mockDetalle;
    }

    @AfterEach
    void tearDown() {
        if (originalLogLevel != null) {
            Logger.getLogger(VentaDetalleFrm.class.getName()).setLevel(originalLogLevel);
        } else {
            Logger.getLogger(VentaDetalleFrm.class.getName()).setLevel(Level.INFO);
        }

        if (mockedFacesContext != null) {
            mockedFacesContext.close();
        }
    }

    @Test
    void testInicializar_Exito() {
        when(ventaDao.findAll()).thenReturn(List.of(mockVenta));
        when(productoDao.findAll()).thenReturn(List.of(mockProducto));

        cut.inicializar();

        assertEquals("Gestión de Detalle de Venta", cut.nombreBean);
        assertFalse(cut.getVentasDisponibles().isEmpty());
        assertFalse(cut.getProductosDisponibles().isEmpty());
        assertFalse(cut.getEstadosDisponibles().isEmpty());
    }

    @Test
    void testInicializar_Excepcion() {
        when(ventaDao.findAll()).thenThrow(new RuntimeException("DB Error"));

        cut.inicializar();

        assertTrue(cut.getVentasDisponibles().isEmpty());
        assertTrue(cut.getProductosDisponibles().isEmpty());
    }

    @Test
    void testGettersLazy() {
        when(ventaDao.findAll()).thenReturn(new ArrayList<>());
        when(productoDao.findAll()).thenReturn(new ArrayList<>());

        assertNotNull(cut.getVentasDisponibles());
        assertNotNull(cut.getProductosDisponibles());

        verify(ventaDao).findAll();
        verify(productoDao).findAll();
    }

    @Test
    void testGetDao() {
        assertEquals(ventaDetalleDao, cut.getDao());
    }

    @Test
    void testNuevoRegistro() {
        VentaDetalle nuevo = cut.nuevoRegistro();
        assertNotNull(nuevo);
        assertNotNull(nuevo.getId());
        assertEquals(BigDecimal.ZERO, nuevo.getCantidad());
        assertEquals(BigDecimal.ZERO, nuevo.getPrecio());
        assertEquals("PENDIENTE", nuevo.getEstado());
        assertNotNull(nuevo.getIdVenta());
        assertNotNull(nuevo.getIdProducto());
    }

    @Test
    void testBuscarRegistroPorId() {
        when(ventaDetalleDao.findById(uuidPrueba)).thenReturn(mockDetalle);

        VentaDetalle result = cut.buscarRegistroPorId(uuidPrueba);
        assertNotNull(result);
        assertEquals(uuidPrueba, result.getId());

        assertNull(cut.buscarRegistroPorId("no-uuid"));
        assertNull(cut.buscarRegistroPorId(null));
    }

    @Test
    void testGetIdAsText() {
        assertEquals(uuidPrueba.toString(), cut.getIdAsText(mockDetalle));
        assertNull(cut.getIdAsText(null));
        assertNull(cut.getIdAsText(new VentaDetalle()));
    }

    @Test
    void testGetIdByText() {
        when(ventaDetalleDao.findById(uuidPrueba)).thenReturn(mockDetalle);

        VentaDetalle result = cut.getIdByText(uuidPrueba.toString());
        assertNotNull(result);

        assertNull(cut.getIdByText(null));
        assertNull(cut.getIdByText(""));
        assertNull(cut.getIdByText("invalid-uuid"));
    }

    @Test
    void testEsNombreVacio_Validaciones() {
        VentaDetalle invalido = new VentaDetalle();

        invalido.setIdVenta(null);
        assertTrue(cut.esNombreVacio(invalido));
        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("Debe seleccionar una Venta")));

        invalido.setIdVenta(mockVenta);
        invalido.setIdProducto(null);
        assertTrue(cut.esNombreVacio(invalido));

        invalido.setIdProducto(mockProducto);
        invalido.setCantidad(BigDecimal.ZERO);
        assertTrue(cut.esNombreVacio(invalido));

        invalido.setCantidad(BigDecimal.TEN);
        invalido.setPrecio(BigDecimal.valueOf(-1));
        assertTrue(cut.esNombreVacio(invalido));

        invalido.setPrecio(BigDecimal.TEN);
        invalido.setEstado(null);
        assertTrue(cut.esNombreVacio(invalido));

        invalido.setEstado("ENTREGADO");
        assertFalse(cut.esNombreVacio(invalido));
    }

    @Test
    void testBtnGuardarHandler_RegistroNull() {
        cut.registro = null;
        cut.btnGuardarHandler(null);
        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("No hay registro")));
        verify(ventaDetalleDao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_ValidacionFalla() {
        cut.registro.setCantidad(BigDecimal.valueOf(-5));
        cut.btnGuardarHandler(null);
        verify(ventaDetalleDao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_VentaNoEncontrada() {
        when(ventaDao.findById(any(UUID.class))).thenReturn(null);
        when(productoDao.findById(any(UUID.class))).thenReturn(mockProducto);

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("No se encontró la Venta")));
        verify(ventaDetalleDao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_ProductoNoEncontrado() {
        when(ventaDao.findById(any(UUID.class))).thenReturn(mockVenta);
        when(productoDao.findById(any(UUID.class))).thenReturn(null);

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("No se encontró el Producto")));
        verify(ventaDetalleDao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_Exito() {
        when(ventaDao.findById(any(UUID.class))).thenReturn(mockVenta);
        when(productoDao.findById(any(UUID.class))).thenReturn(mockProducto);

        cut.btnGuardarHandler(null);

        verify(ventaDetalleDao).crear(any(VentaDetalle.class));
        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("guardado correctamente")));
        assertNull(cut.registro);
    }

    @Test
    void testBtnGuardarHandler_Excepcion() {
        when(ventaDao.findById(any(UUID.class))).thenReturn(mockVenta);
        when(productoDao.findById(any(UUID.class))).thenReturn(mockProducto);
        doThrow(new RuntimeException("Error DB")).when(ventaDetalleDao).crear(any());

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("Ocurrió un error")));
    }

    @Test
    void testBtnModificarHandler_RegistroNull() {
        cut.registro = null;
        cut.btnModificarHandler(null);
        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("No hay registro")));
        verify(ventaDetalleDao, never()).modificar(any());
    }

    @Test
    void testBtnModificarHandler_ValidacionFalla() {
        cut.registro.setEstado("");
        cut.btnModificarHandler(null);
        verify(ventaDetalleDao, never()).modificar(any());
    }

    @Test
    void testBtnModificarHandler_EntidadesNoEncontradas() {
        when(ventaDao.findById(any(UUID.class))).thenReturn(null);
        when(productoDao.findById(any(UUID.class))).thenReturn(mockProducto);

        cut.btnModificarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("No se encontraron las entidades")));
        verify(ventaDetalleDao, never()).modificar(any());
    }

    @Test
    void testBtnModificarHandler_Exito() {
        when(ventaDao.findById(any(UUID.class))).thenReturn(mockVenta);
        when(productoDao.findById(any(UUID.class))).thenReturn(mockProducto);

        cut.btnModificarHandler(null);

        verify(ventaDetalleDao).modificar(any(VentaDetalle.class));
        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("modificado correctamente")));
        assertNull(cut.registro);
    }

    @Test
    void testBtnModificarHandler_Excepcion() {
        when(ventaDao.findById(any(UUID.class))).thenReturn(mockVenta);
        when(productoDao.findById(any(UUID.class))).thenReturn(mockProducto);
        doThrow(new RuntimeException("Error Update")).when(ventaDetalleDao).modificar(any());

        cut.btnModificarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("Ocurrió un error")));
    }

    @Test
    void testDaoExceptions_ObtenerVenta() {
        when(ventaDao.findById(any(UUID.class))).thenThrow(new RuntimeException("DAO Error"));
        when(productoDao.findById(any(UUID.class))).thenReturn(mockProducto);

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("No se encontró la Venta")));
    }

    @Test
    void testDaoExceptions_ObtenerProducto() {
        when(ventaDao.findById(any(UUID.class))).thenReturn(mockVenta);
        when(productoDao.findById(any(UUID.class))).thenThrow(new RuntimeException("DAO Error"));

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("No se encontró el Producto")));
    }
}