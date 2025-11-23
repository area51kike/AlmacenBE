package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProducto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProducto;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoTipoProductoFrmTest {

    @Mock
    ProductoTipoProductoDAO productoTipoProductoDAO;

    @Mock
    ProductoDAO productoDAO;

    @Mock
    TipoProductoDAO tipoProductoDAO;

    @Mock
    FacesContext facesContext;

    @InjectMocks
    ProductoTipoProductoFrm cut;

    private MockedStatic<FacesContext> mockedFacesContext;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private ProductoTipoProducto mockEntity;
    private Producto mockProducto;
    private TipoProducto mockTipoProducto;
    private UUID uuidEntity;
    private UUID uuidProducto;
    private Long idTipoProducto;

    @BeforeEach
    void setUp() {
        // Silenciar consola
        System.setOut(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));
        System.setErr(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));

        mockedFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

        uuidEntity = UUID.randomUUID();
        uuidProducto = UUID.randomUUID();
        idTipoProducto = 50L;

        mockProducto = new Producto();
        mockProducto.setId(uuidProducto);
        mockProducto.setNombreProducto("Producto Test");

        mockTipoProducto = new TipoProducto();
        mockTipoProducto.setId(idTipoProducto);
        mockTipoProducto.setNombre("Tipo Test");

        mockEntity = new ProductoTipoProducto();
        mockEntity.setId(uuidEntity);
        mockEntity.setIdProducto(mockProducto);
        mockEntity.setIdTipoProducto(mockTipoProducto);
        mockEntity.setFechaCreacion(OffsetDateTime.now());
        mockEntity.setActivo(true);

        cut.setRegistro(mockEntity);
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
        when(productoDAO.findAll()).thenReturn(List.of(mockProducto));
        when(tipoProductoDAO.findAll()).thenReturn(List.of(mockTipoProducto));

        cut.inicializar();

        assertNotNull(cut.getListaProductos());
        assertNotNull(cut.getListaTipoProductos());
        assertFalse(cut.getListaProductos().isEmpty());
        assertFalse(cut.getListaTipoProductos().isEmpty());
    }

    @Test
    void testInicializar_Excepciones() {
        when(productoDAO.findAll()).thenThrow(new RuntimeException("Error Prod"));
        when(tipoProductoDAO.findAll()).thenThrow(new RuntimeException("Error Tipo"));

        cut.inicializar();

        assertNull(cut.getListaProductos());
        assertNull(cut.getListaTipoProductos());
    }

    @Test
    void testGetDao() {
        assertEquals(productoTipoProductoDAO, cut.getDao());
    }

    @Test
    void testNuevoRegistro() {
        ProductoTipoProducto nuevo = cut.nuevoRegistro();
        assertNotNull(nuevo);
        assertNotNull(nuevo.getId());
        assertNotNull(nuevo.getFechaCreacion());
        assertTrue(nuevo.getActivo());
    }

    @Test
    void testBuscarRegistroPorId() {
        when(productoTipoProductoDAO.findById(uuidEntity)).thenReturn(mockEntity);

        assertEquals(mockEntity, cut.buscarRegistroPorId(uuidEntity));
        assertNull(cut.buscarRegistroPorId("no-uuid"));
        assertNull(cut.buscarRegistroPorId(null));
    }

    @Test
    void testGetIdAsText() {
        assertEquals(uuidEntity.toString(), cut.getIdAsText(mockEntity));
        assertNull(cut.getIdAsText(null));
    }

    @Test
    void testGetIdByText() {
        when(productoTipoProductoDAO.findById(uuidEntity)).thenReturn(mockEntity);

        assertEquals(mockEntity, cut.getIdByText(uuidEntity.toString()));
        assertNull(cut.getIdByText(null));
        assertNull(cut.getIdByText(""));
        assertNull(cut.getIdByText("invalid-uuid"));
    }

    @Test
    void testEsNombreVacio() {
        ProductoTipoProducto valido = new ProductoTipoProducto();
        valido.setIdProducto(mockProducto);
        valido.setIdTipoProducto(mockTipoProducto);
        assertFalse(cut.esNombreVacio(valido));

        ProductoTipoProducto invalido1 = new ProductoTipoProducto();
        invalido1.setIdProducto(null);
        invalido1.setIdTipoProducto(mockTipoProducto);
        assertTrue(cut.esNombreVacio(invalido1));

        ProductoTipoProducto invalido2 = new ProductoTipoProducto();
        invalido2.setIdProducto(mockProducto);
        invalido2.setIdTipoProducto(null);
        assertTrue(cut.esNombreVacio(invalido2));
    }

    @Test
    void testFechaCreacionDate() {
        // Test Getter
        Date date = cut.getFechaCreacionDate();
        assertNotNull(date);

        cut.setRegistro(new ProductoTipoProducto()); // Sin fecha
        assertNotNull(cut.getFechaCreacionDate()); // Retorna new Date()

        cut.setRegistro(null);
        assertNotNull(cut.getFechaCreacionDate());

        // Test Setter
        cut.setRegistro(mockEntity);
        Date nuevaFecha = new Date();
        cut.setFechaCreacionDate(nuevaFecha);

        assertNotNull(mockEntity.getFechaCreacion());
        // Comparamos instantes para evitar lios de zona horaria
        long diff = Math.abs(nuevaFecha.toInstant().toEpochMilli() - mockEntity.getFechaCreacion().toInstant().toEpochMilli());
        assertTrue(diff < 1000); // Margen de 1 segundo

        cut.setFechaCreacionDate(null); // No debe romper
    }

    @Test
    void testSelectionHandler() {
        SelectEvent<ProductoTipoProducto> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(mockEntity);

        cut.selectionHandler(event);

        assertEquals(uuidProducto, cut.getIdProductoSeleccionado());
        assertEquals(idTipoProducto, cut.getIdTipoProductoSeleccionado());

        // Test con objeto vacio dentro
        ProductoTipoProducto vacio = new ProductoTipoProducto();
        when(event.getObject()).thenReturn(vacio);
        cut.selectionHandler(event);
    }

    @Test
    void testSelectionHandler_Null() {
        cut.selectionHandler(null);
    }

    @Test
    void testBtnGuardarHandler_RegistroNull() {
        cut.setRegistro(null);
        cut.btnGuardarHandler(null);
        verify(productoTipoProductoDAO, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_ValidacionFaltanIDs() {
        cut.setIdProductoSeleccionado(null);
        cut.setIdTipoProductoSeleccionado(null);

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_WARN && m.getDetail().contains("Debe seleccionar Producto")
        ));
        verify(productoTipoProductoDAO, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_Crear_Exito() {
        cut.setIdProductoSeleccionado(uuidProducto);
        cut.setIdTipoProductoSeleccionado(idTipoProducto);
        setEstado(cut, "CREAR");

        when(productoDAO.findById(uuidProducto)).thenReturn(mockProducto);
        when(tipoProductoDAO.findById(idTipoProducto)).thenReturn(mockTipoProducto);

        cut.btnGuardarHandler(null);

        // Verificar asignación en el objeto mock antes de que cut.registro sea null
        assertEquals(mockProducto, mockEntity.getIdProducto());
        assertEquals(mockTipoProducto, mockEntity.getIdTipoProducto());

        verify(productoTipoProductoDAO).crear(mockEntity);
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_INFO && m.getDetail().contains("creado correctamente")
        ));
    }

    @Test
    void testBtnGuardarHandler_Modificar_Exito() {
        cut.setIdProductoSeleccionado(uuidProducto);
        cut.setIdTipoProductoSeleccionado(idTipoProducto);
        setEstado(cut, "MODIFICAR");

        when(productoDAO.findById(uuidProducto)).thenReturn(mockProducto);
        when(tipoProductoDAO.findById(idTipoProducto)).thenReturn(mockTipoProducto);

        cut.btnGuardarHandler(null);

        verify(productoTipoProductoDAO).modificar(mockEntity);
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_INFO && m.getDetail().contains("modificado correctamente")
        ));
    }

    @Test
    void testBtnGuardarHandler_Excepcion() {
        cut.setIdProductoSeleccionado(uuidProducto);
        cut.setIdTipoProductoSeleccionado(idTipoProducto);
        setEstado(cut, "CREAR");

        when(productoDAO.findById(uuidProducto)).thenThrow(new RuntimeException("Error Fatal"));

        cut.btnGuardarHandler(null);

        // CORRECCIÓN: Verificamos getSummary() porque ahí es donde tu código pone "Error al guardar"
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_ERROR && m.getSummary().contains("Error al guardar")
        ));
    }

    @Test
    void testBtnModificarHandler_RegistroNull() {
        cut.setRegistro(null);
        cut.btnModificarHandler(null);
        verify(productoTipoProductoDAO, never()).modificar(any());
    }

    @Test
    void testBtnModificarHandler_ValidacionIDs() {
        cut.setIdProductoSeleccionado(null);

        cut.btnModificarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_WARN && m.getDetail().contains("Debe seleccionar Producto")
        ));
        verify(productoTipoProductoDAO, never()).modificar(any());
    }

    @Test
    void testBtnModificarHandler_Exito() {
        cut.setIdProductoSeleccionado(uuidProducto);
        cut.setIdTipoProductoSeleccionado(idTipoProducto);

        when(productoDAO.findById(uuidProducto)).thenReturn(mockProducto);
        when(tipoProductoDAO.findById(idTipoProducto)).thenReturn(mockTipoProducto);

        cut.btnModificarHandler(null);

        assertEquals(mockProducto, mockEntity.getIdProducto());
        assertEquals(mockTipoProducto, mockEntity.getIdTipoProducto());

        verify(productoTipoProductoDAO).modificar(mockEntity);

        // Verificar limpieza
        assertNull(cut.getIdProductoSeleccionado());
        assertNull(cut.getIdTipoProductoSeleccionado());

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_INFO && m.getDetail().contains("modificado correctamente")
        ));
    }

    @Test
    void testBtnModificarHandler_Excepcion() {
        cut.setIdProductoSeleccionado(uuidProducto);
        cut.setIdTipoProductoSeleccionado(idTipoProducto);

        when(productoDAO.findById(uuidProducto)).thenThrow(new RuntimeException("Error Update"));

        cut.btnModificarHandler(null);

        // CORRECCIÓN: Verificamos getSummary() porque ahí es donde tu código pone "Error al modificar"
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_ERROR && m.getSummary().contains("Error al modificar")
        ));
    }

    @Test
    void testGetSetters() {
        List<Producto> pList = new ArrayList<>();
        cut.setListaProductos(pList);
        assertEquals(pList, cut.getListaProductos());

        List<TipoProducto> tpList = new ArrayList<>();
        cut.setListaTipoProductos(tpList);
        assertEquals(tpList, cut.getListaTipoProductos());

        cut.setIdProductoSeleccionado(uuidProducto);
        assertEquals(uuidProducto, cut.getIdProductoSeleccionado());

        cut.setIdTipoProductoSeleccionado(idTipoProducto);
        assertEquals(idTipoProducto, cut.getIdTipoProductoSeleccionado());
    }

    private void setEstado(ProductoTipoProductoFrm bean, String nombreEstado) {
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