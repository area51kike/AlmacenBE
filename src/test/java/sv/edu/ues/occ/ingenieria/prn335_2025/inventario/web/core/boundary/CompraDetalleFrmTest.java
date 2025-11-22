package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDetalleDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.CompraDetalle;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;
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
class CompraDetalleFrmTest {

    @Mock
    CompraDetalleDao compraDetalleDao;

    @Mock
    CompraDAO compraDao;

    @Mock
    ProductoDAO productoDao;

    @Mock
    FacesContext facesContext;

    @InjectMocks
    CompraDetalleFrm cut;

    private MockedStatic<FacesContext> mockedFacesContext;
    private CompraDetalle mockDetalle;
    private Compra mockCompra;
    private Producto mockProducto;
    private UUID uuidPrueba;

    // Variable para guardar el nivel original del logger y restaurarlo
    private Level originalLogLevel;

    @BeforeEach
    void setUp() {
        // --- SILENCIADOR DE CONSOLA ---
        // Obtenemos el logger de la clase que estamos probando
        Logger appLogger = Logger.getLogger(CompraDetalleFrm.class.getName());
        // Guardamos el nivel actual para ser educados y restaurarlo después
        originalLogLevel = appLogger.getLevel();
        // Lo apagamos completamente para este test. ¡Adiós texto rojo!
        appLogger.setLevel(Level.OFF);
        // ------------------------------

        mockedFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

        uuidPrueba = UUID.randomUUID();
        mockDetalle = new CompraDetalle();
        mockDetalle.setId(uuidPrueba);
        mockDetalle.setCantidad(BigDecimal.TEN);
        mockDetalle.setPrecio(BigDecimal.valueOf(100.00));
        mockDetalle.setEstado("PENDIENTE");

        mockCompra = new Compra();
        mockCompra.setId(1L);

        mockProducto = new Producto();
        mockProducto.setId(UUID.randomUUID());
        mockProducto.setNombreProducto("Producto Test");

        mockDetalle.setIdCompra(mockCompra);
        mockDetalle.setIdProducto(mockProducto);

        cut.registro = mockDetalle;
    }

    @AfterEach
    void tearDown() {
        // Restauramos el nivel del logger para no afectar otros tests fuera de esta clase
        if (originalLogLevel != null) {
            Logger.getLogger(CompraDetalleFrm.class.getName()).setLevel(originalLogLevel);
        } else {
            // Si era null (default), lo dejamos en INFO o ALL
            Logger.getLogger(CompraDetalleFrm.class.getName()).setLevel(Level.INFO);
        }

        if (mockedFacesContext != null) {
            mockedFacesContext.close();
        }
    }

    @Test
    void testInicializar_Exito() {
        when(compraDao.findAll()).thenReturn(List.of(mockCompra));
        when(productoDao.findAll()).thenReturn(List.of(mockProducto));

        cut.inicializar();

        assertEquals("Gestión de Detalle de Compra", cut.nombreBean);
        assertFalse(cut.getComprasDisponibles().isEmpty());
        assertFalse(cut.getProductosDisponibles().isEmpty());
        assertFalse(cut.getEstadosDisponibles().isEmpty());
    }

    @Test
    void testInicializar_Excepcion() {
        // Este test generaba texto rojo. Ahora debería estar en silencio.
        when(compraDao.findAll()).thenThrow(new RuntimeException("DB Error"));

        cut.inicializar();

        assertTrue(cut.getComprasDisponibles().isEmpty());
        assertTrue(cut.getProductosDisponibles().isEmpty());
    }

    @Test
    void testGettersLazy() {
        when(compraDao.findAll()).thenReturn(new ArrayList<>());
        when(productoDao.findAll()).thenReturn(new ArrayList<>());

        assertNotNull(cut.getComprasDisponibles());
        assertNotNull(cut.getProductosDisponibles());

        verify(compraDao).findAll();
        verify(productoDao).findAll();
    }

    @Test
    void testGetDao() {
        assertEquals(compraDetalleDao, cut.getDao());
    }

    @Test
    void testNuevoRegistro() {
        CompraDetalle nuevo = cut.nuevoRegistro();
        assertNotNull(nuevo);
        assertNotNull(nuevo.getId());
        assertEquals(BigDecimal.ZERO, nuevo.getCantidad());
        assertEquals(BigDecimal.ZERO, nuevo.getPrecio());
        assertEquals("PENDIENTE", nuevo.getEstado());
        assertNotNull(nuevo.getIdCompra());
        assertNotNull(nuevo.getIdProducto());
    }

    @Test
    void testBuscarRegistroPorId() {
        when(compraDetalleDao.findById(uuidPrueba)).thenReturn(mockDetalle);

        CompraDetalle result = cut.buscarRegistroPorId(uuidPrueba);
        assertNotNull(result);
        assertEquals(uuidPrueba, result.getId());

        assertNull(cut.buscarRegistroPorId("no-uuid"));
        assertNull(cut.buscarRegistroPorId(null));
    }

    @Test
    void testGetIdAsText() {
        assertEquals(uuidPrueba.toString(), cut.getIdAsText(mockDetalle));
        assertNull(cut.getIdAsText(null));
        assertNull(cut.getIdAsText(new CompraDetalle()));
    }

    @Test
    void testGetIdByText() {
        when(compraDetalleDao.findById(uuidPrueba)).thenReturn(mockDetalle);

        CompraDetalle result = cut.getIdByText(uuidPrueba.toString());
        assertNotNull(result);

        assertNull(cut.getIdByText(null));
        assertNull(cut.getIdByText(""));
        // Este caso generaba un warning en consola. Ahora debe estar en silencio.
        assertNull(cut.getIdByText("invalid-uuid"));
    }

    @Test
    void testEsNombreVacio_Validaciones() {
        CompraDetalle invalido = new CompraDetalle();

        invalido.setIdCompra(null);
        assertTrue(cut.esNombreVacio(invalido));
        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("Debe seleccionar una Compra")));

        invalido.setIdCompra(mockCompra);
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

        invalido.setEstado("RECIBIDO");
        assertFalse(cut.esNombreVacio(invalido));
    }

    @Test
    void testBtnGuardarHandler_RegistroNull() {
        cut.registro = null;
        cut.btnGuardarHandler(null);
        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("No hay registro")));
        verify(compraDetalleDao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_ValidacionFalla() {
        cut.registro.setCantidad(BigDecimal.valueOf(-1));
        cut.btnGuardarHandler(null);
        verify(compraDetalleDao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_CompraNoEncontrada() {
        when(compraDao.findById(1L)).thenReturn(null);
        when(productoDao.findById(any(UUID.class))).thenReturn(mockProducto);

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("No se encontró la Compra")));
        verify(compraDetalleDao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_ProductoNoEncontrado() {
        when(compraDao.findById(1L)).thenReturn(mockCompra);
        when(productoDao.findById(any(UUID.class))).thenReturn(null);

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("No se encontró el Producto")));
        verify(compraDetalleDao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_Exito() {
        when(compraDao.findById(1L)).thenReturn(mockCompra);
        when(productoDao.findById(any(UUID.class))).thenReturn(mockProducto);

        cut.btnGuardarHandler(null);

        verify(compraDetalleDao).crear(any(CompraDetalle.class));
        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("guardado correctamente")));
        assertNull(cut.registro);
    }

    @Test
    void testBtnGuardarHandler_Excepcion() {
        // Este generaba error SEVERE en consola. Ahora silencio.
        when(compraDao.findById(1L)).thenReturn(mockCompra);
        when(productoDao.findById(any(UUID.class))).thenReturn(mockProducto);
        doThrow(new RuntimeException("Error DB")).when(compraDetalleDao).crear(any());

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("Ocurrió un error")));
    }

    @Test
    void testBtnModificarHandler_RegistroNull() {
        cut.registro = null;
        cut.btnModificarHandler(null);
        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("No hay registro")));
        verify(compraDetalleDao, never()).modificar(any());
    }

    @Test
    void testBtnModificarHandler_ValidacionFalla() {
        cut.registro.setEstado("");
        cut.btnModificarHandler(null);
        verify(compraDetalleDao, never()).modificar(any());
    }

    @Test
    void testBtnModificarHandler_EntidadesNoEncontradas() {
        when(compraDao.findById(1L)).thenReturn(null);
        when(productoDao.findById(any(UUID.class))).thenReturn(mockProducto);

        cut.btnModificarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("No se encontraron las entidades")));
        verify(compraDetalleDao, never()).modificar(any());
    }

    @Test
    void testBtnModificarHandler_Exito() {
        when(compraDao.findById(1L)).thenReturn(mockCompra);
        when(productoDao.findById(any(UUID.class))).thenReturn(mockProducto);

        cut.btnModificarHandler(null);

        verify(compraDetalleDao).modificar(any(CompraDetalle.class));
        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("modificado correctamente")));
        assertNull(cut.registro);
    }

    @Test
    void testBtnModificarHandler_Excepcion() {
        // Este generaba error SEVERE en consola. Ahora silencio.
        when(compraDao.findById(1L)).thenReturn(mockCompra);
        when(productoDao.findById(any(UUID.class))).thenReturn(mockProducto);
        doThrow(new RuntimeException("Error Update")).when(compraDetalleDao).modificar(any());

        cut.btnModificarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("Ocurrió un error")));
    }

    @Test
    void testDaoExceptions_ObtenerCompra() {
        // Este generaba error SEVERE en consola. Ahora silencio.
        when(compraDao.findById(anyLong())).thenThrow(new RuntimeException("DAO Error"));
        when(productoDao.findById(any(UUID.class))).thenReturn(mockProducto);

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("No se encontró la Compra")));
    }

    @Test
    void testDaoExceptions_ObtenerProducto() {
        // Este generaba error SEVERE en consola. Ahora silencio.
        when(compraDao.findById(anyLong())).thenReturn(mockCompra);
        when(productoDao.findById(any(UUID.class))).thenThrow(new RuntimeException("DAO Error"));

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("No se encontró el Producto")));
    }
}