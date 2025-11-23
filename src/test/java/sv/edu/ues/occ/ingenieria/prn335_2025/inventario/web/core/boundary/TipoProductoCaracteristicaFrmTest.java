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
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProducto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProductoCaracteristica;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoProductoCaracteristicaFrmTest {

    @Mock
    TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDAO;

    @Mock
    TipoProductoDAO tipoProductoDAO;

    @Mock
    CaracteristicaDAO caracteristicaDAO;

    @Mock
    FacesContext facesContext;

    @InjectMocks
    TipoProductoCaracteristicaFrm cut;

    private MockedStatic<FacesContext> mockedFacesContext;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private TipoProductoCaracteristica mockEntity;
    private TipoProducto mockTipoProducto;
    private Caracteristica mockCaracteristica;
    private Long idEntity;
    private Long idTipoProducto;
    private Integer idCaracteristica;

    @BeforeEach
    void setUp() {
        // Silenciador de consola
        System.setOut(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));
        System.setErr(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));

        mockedFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

        idEntity = 1L;
        idTipoProducto = 50L;
        idCaracteristica = 10;

        mockTipoProducto = new TipoProducto();
        mockTipoProducto.setId(idTipoProducto);
        mockTipoProducto.setNombre("Tipo Test");

        mockCaracteristica = new Caracteristica();
        mockCaracteristica.setId(idCaracteristica);
        mockCaracteristica.setNombre("Caract Test");

        mockEntity = new TipoProductoCaracteristica();
        mockEntity.setId(idEntity);
        mockEntity.setTipoProducto(mockTipoProducto);
        mockEntity.setCaracteristica(mockCaracteristica);
        mockEntity.setFechaCreacion(OffsetDateTime.now());
        mockEntity.setObligatorio(false);

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
        when(tipoProductoDAO.findAll()).thenReturn(List.of(mockTipoProducto));
        when(caracteristicaDAO.findAll()).thenReturn(List.of(mockCaracteristica));
        when(tipoProductoCaracteristicaDAO.count()).thenReturn(5L);

        cut.inicializar();

        assertNotNull(cut.getModelo());
        assertFalse(cut.getListaTipoProductos().isEmpty());
        assertFalse(cut.getListaCaracteristicas().isEmpty());
        assertEquals(5, cut.getModelo().count(Collections.emptyMap()));
    }

    @Test
    void testInicializar_Excepciones() {
        // CORRECCIÓN: Solo mockeamos el primero porque al fallar, salta al catch y no ejecuta el segundo.
        when(tipoProductoDAO.findAll()).thenThrow(new RuntimeException("Error Tipo"));
        // when(caracteristicaDAO.findAll()).thenThrow(...) -> ELIMINADO para evitar UnnecessaryStubbingException

        cut.inicializar();

        assertNull(cut.getListaTipoProductos());
        // listaCaracteristicas será null porque nunca llegó a esa línea
        assertNull(cut.getListaCaracteristicas());
    }

    @Test
    void testModelo_Load() {
        cut.inicializar();
        when(tipoProductoCaracteristicaDAO.findRange(anyInt(), anyInt())).thenReturn(List.of(mockEntity));

        List<TipoProductoCaracteristica> result = cut.getModelo().load(0, 10, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testModelo_Load_Exception() {
        cut.inicializar();
        when(tipoProductoCaracteristicaDAO.findRange(anyInt(), anyInt())).thenThrow(new RuntimeException("DB Error"));

        List<TipoProductoCaracteristica> result = cut.getModelo().load(0, 10, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void testModelo_Count_Exception() {
        cut.inicializar();
        when(tipoProductoCaracteristicaDAO.count()).thenThrow(new RuntimeException("DB Error"));

        int count = cut.getModelo().count(Collections.emptyMap());
        assertEquals(0, count);
    }

    @Test
    void testModelo_GetRowKey() {
        cut.inicializar();
        assertEquals(idEntity.toString(), cut.getModelo().getRowKey(mockEntity));
        assertNull(cut.getModelo().getRowKey(null));
    }

    @Test
    void testModelo_GetRowData() {
        cut.inicializar();
        when(tipoProductoCaracteristicaDAO.findById(idEntity)).thenReturn(mockEntity);

        TipoProductoCaracteristica result = cut.getModelo().getRowData(idEntity.toString());
        assertEquals(mockEntity, result);

        assertNull(cut.getModelo().getRowData(null));
        assertNull(cut.getModelo().getRowData("invalid-long"));
    }

    @Test
    void testGetDao() {
        assertEquals(tipoProductoCaracteristicaDAO, cut.getDao());
    }

    @Test
    void testNuevoRegistro() {
        when(tipoProductoCaracteristicaDAO.obtenerMaximoId()).thenReturn(10L);

        TipoProductoCaracteristica nuevo = cut.nuevoRegistro();
        assertNotNull(nuevo);
        assertEquals(11L, nuevo.getId());
        assertNotNull(nuevo.getFechaCreacion());
        assertFalse(nuevo.getObligatorio());
    }

    @Test
    void testCrear_MetodoPublico() {
        cut.crear(mockEntity);
        verify(tipoProductoCaracteristicaDAO).crear(mockEntity);
    }

    @Test
    void testCrear_MetodoPublico_Null() {
        assertThrows(IllegalArgumentException.class, () -> cut.crear(null));
    }

    @Test
    void testCrear_MetodoPublico_Excepcion() {
        doThrow(new RuntimeException("Error")).when(tipoProductoCaracteristicaDAO).crear(any());
        assertThrows(RuntimeException.class, () -> cut.crear(mockEntity));
    }

    @Test
    void testBuscarRegistroPorId() {
        when(tipoProductoCaracteristicaDAO.findById(idEntity)).thenReturn(mockEntity);

        assertEquals(mockEntity, cut.buscarRegistroPorId(idEntity));
        assertEquals(mockEntity, cut.buscarRegistroPorId(idEntity.toString()));

        assertNull(cut.buscarRegistroPorId("invalid"));
        assertNull(cut.buscarRegistroPorId(null));
    }

    @Test
    void testGetIdAsText() {
        assertEquals(idEntity.toString(), cut.getIdAsText(mockEntity));
        assertNull(cut.getIdAsText(null));
    }

    @Test
    void testGetIdByText() {
        when(tipoProductoCaracteristicaDAO.findById(idEntity)).thenReturn(mockEntity);

        assertEquals(mockEntity, cut.getIdByText(idEntity.toString()));
        assertNull(cut.getIdByText(null));
        assertNull(cut.getIdByText("invalid"));
    }

    @Test
    void testEsNombreVacio() {
        TipoProductoCaracteristica valido = new TipoProductoCaracteristica();
        valido.setTipoProducto(mockTipoProducto);
        valido.setCaracteristica(mockCaracteristica);
        assertFalse(cut.esNombreVacio(valido));

        TipoProductoCaracteristica invalido1 = new TipoProductoCaracteristica();
        invalido1.setTipoProducto(null);
        invalido1.setCaracteristica(mockCaracteristica);
        assertTrue(cut.esNombreVacio(invalido1));

        TipoProductoCaracteristica invalido2 = new TipoProductoCaracteristica();
        invalido2.setTipoProducto(mockTipoProducto);
        invalido2.setCaracteristica(null);
        assertTrue(cut.esNombreVacio(invalido2));
    }

    @Test
    void testSelectionHandler() {
        SelectEvent<TipoProductoCaracteristica> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(mockEntity);

        cut.selectionHandler(event);

        assertEquals(idTipoProducto, cut.getIdTipoProductoSeleccionado());
        assertEquals(idCaracteristica, cut.getIdCaracteristicaSeleccionada());

        cut.selectionHandler(null);
    }

    @Test
    void testBtnNuevoHandler() {
        cut.btnNuevoHandler(null);
        assertNotNull(cut.getRegistro());
    }

    @Test
    void testBtnGuardarHandler_RegistroNull() {
        cut.setRegistro(null);
        cut.btnGuardarHandler(null);
        verify(tipoProductoCaracteristicaDAO, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_ValidacionIDs() {
        cut.setIdTipoProductoSeleccionado(null);
        cut.setIdCaracteristicaSeleccionada(null);

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_WARN && m.getDetail().contains("Debe seleccionar")
        ));
        verify(tipoProductoCaracteristicaDAO, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_Crear_Exito() {
        cut.setIdTipoProductoSeleccionado(idTipoProducto);
        cut.setIdCaracteristicaSeleccionada(idCaracteristica);
        setEstado(cut, "CREAR");

        when(tipoProductoDAO.findById(idTipoProducto)).thenReturn(mockTipoProducto);
        when(caracteristicaDAO.findById(idCaracteristica)).thenReturn(mockCaracteristica);

        cut.btnGuardarHandler(null);

        assertEquals(mockTipoProducto, mockEntity.getTipoProducto());
        assertEquals(mockCaracteristica, mockEntity.getCaracteristica());

        verify(tipoProductoCaracteristicaDAO).crear(any());
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_INFO && m.getDetail().contains("creado correctamente")
        ));
    }

    @Test
    void testBtnGuardarHandler_Modificar_Exito() {
        cut.setIdTipoProductoSeleccionado(idTipoProducto);
        cut.setIdCaracteristicaSeleccionada(idCaracteristica);
        setEstado(cut, "MODIFICAR");

        when(tipoProductoDAO.findById(idTipoProducto)).thenReturn(mockTipoProducto);
        when(caracteristicaDAO.findById(idCaracteristica)).thenReturn(mockCaracteristica);

        cut.btnGuardarHandler(null);

        verify(tipoProductoCaracteristicaDAO).modificar(mockEntity);
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_INFO && m.getDetail().contains("modificado correctamente")
        ));
    }

    @Test
    void testBtnGuardarHandler_Excepcion() {
        cut.setIdTipoProductoSeleccionado(idTipoProducto);
        cut.setIdCaracteristicaSeleccionada(idCaracteristica);
        setEstado(cut, "CREAR");

        when(tipoProductoDAO.findById(idTipoProducto)).thenThrow(new RuntimeException("Error Fatal"));

        cut.btnGuardarHandler(null);

        // CORRECCIÓN: Verificamos getSummary()
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_ERROR && m.getSummary().contains("Error al guardar")
        ));
    }

    @Test
    void testBtnModificarHandler_RegistroNull() {
        cut.setRegistro(null);
        cut.btnModificarHandler(null);
        verify(tipoProductoCaracteristicaDAO, never()).modificar(any());
    }

    @Test
    void testBtnModificarHandler_ValidacionIDs() {
        cut.setIdTipoProductoSeleccionado(null);

        cut.btnModificarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_WARN && m.getDetail().contains("Debe seleccionar")
        ));
    }

    @Test
    void testBtnModificarHandler_Exito() {
        cut.setIdTipoProductoSeleccionado(idTipoProducto);
        cut.setIdCaracteristicaSeleccionada(idCaracteristica);

        when(tipoProductoDAO.findById(idTipoProducto)).thenReturn(mockTipoProducto);
        when(caracteristicaDAO.findById(idCaracteristica)).thenReturn(mockCaracteristica);

        cut.btnModificarHandler(null);

        assertEquals(mockTipoProducto, mockEntity.getTipoProducto());
        assertEquals(mockCaracteristica, mockEntity.getCaracteristica());

        verify(tipoProductoCaracteristicaDAO).modificar(mockEntity);

        assertNull(cut.getIdTipoProductoSeleccionado());
        assertNull(cut.getIdCaracteristicaSeleccionada());

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_INFO && m.getDetail().contains("modificado correctamente")
        ));
    }

    @Test
    void testBtnModificarHandler_Excepcion() {
        cut.setIdTipoProductoSeleccionado(idTipoProducto);
        cut.setIdCaracteristicaSeleccionada(idCaracteristica);

        when(tipoProductoDAO.findById(idTipoProducto)).thenThrow(new RuntimeException("Error Update"));

        cut.btnModificarHandler(null);

        // CORRECCIÓN: Verificamos getSummary()
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_ERROR && m.getSummary().contains("Error al modificar")
        ));
    }

    @Test
    void testGetSetters() {
        List<TipoProducto> tpList = new ArrayList<>();
        cut.setListaTipoProductos(tpList);
        assertEquals(tpList, cut.getListaTipoProductos());

        List<Caracteristica> cList = new ArrayList<>();
        cut.setListaCaracteristicas(cList);
        assertEquals(cList, cut.getListaCaracteristicas());

        cut.setIdTipoProductoSeleccionado(idTipoProducto);
        assertEquals(idTipoProducto, cut.getIdTipoProductoSeleccionado());

        cut.setIdCaracteristicaSeleccionada(idCaracteristica);
        assertEquals(idCaracteristica, cut.getIdCaracteristicaSeleccionada());
    }

    private void setEstado(TipoProductoCaracteristicaFrm bean, String nombreEstado) {
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