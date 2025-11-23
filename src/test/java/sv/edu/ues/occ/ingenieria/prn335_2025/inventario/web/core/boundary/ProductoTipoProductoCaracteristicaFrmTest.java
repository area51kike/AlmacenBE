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
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoTipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProducto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProductoCaracteristica;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProductoCaracteristica;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoTipoProductoCaracteristicaFrmTest {

    @Mock
    ProductoTipoProductoCaracteristicaDAO productoTipoProductoCaracteristicaDAO;

    @Mock
    ProductoTipoProductoDAO productoTipoProductoDAO;

    @Mock
    TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDAO;

    @Mock
    FacesContext facesContext;

    @InjectMocks
    ProductoTipoProductoCaracteristicaFrm cut;

    private MockedStatic<FacesContext> mockedFacesContext;

    // Variables para silenciar la consola
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private ProductoTipoProductoCaracteristica mockEntity;
    private ProductoTipoProducto mockPTP;
    private TipoProductoCaracteristica mockTPC;
    private UUID uuidEntity;
    private UUID uuidPTP;
    private Long idTPC;

    @BeforeEach
    void setUp() {
        // --- SILENCIADOR DE CONSOLA ---
        System.setOut(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));
        System.setErr(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));
        // ------------------------------

        mockedFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

        uuidEntity = UUID.randomUUID();
        uuidPTP = UUID.randomUUID();
        idTPC = 100L;

        mockPTP = new ProductoTipoProducto();
        mockPTP.setId(uuidPTP);

        mockTPC = new TipoProductoCaracteristica();
        mockTPC.setId(idTPC);
        Caracteristica c = new Caracteristica();
        c.setNombre("Color");
        mockTPC.setCaracteristica(c);

        mockEntity = new ProductoTipoProductoCaracteristica();
        mockEntity.setId(uuidEntity);
        mockEntity.setIdProductoTipoProducto(mockPTP);
        mockEntity.setIdTipoProductoCaracteristica(mockTPC);

        cut.setRegistro(mockEntity);
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
    void testInicializar() {
        when(productoTipoProductoDAO.findAll()).thenReturn(List.of(mockPTP));
        when(tipoProductoCaracteristicaDAO.findAll()).thenReturn(List.of(mockTPC));
        when(productoTipoProductoCaracteristicaDAO.count()).thenReturn(5L);

        cut.inicializar();

        assertNotNull(cut.getModelo());
        assertFalse(cut.getListaProductoTipoProducto().isEmpty());
        assertFalse(cut.getListaTipoProductoCaracteristica().isEmpty());
        assertEquals(5, cut.getModelo().count(Collections.emptyMap()));
    }

    @Test
    void testInicializar_ExcepcionCarga() {
        when(productoTipoProductoDAO.findAll()).thenThrow(new RuntimeException("DB Error"));

        cut.inicializar();

        assertNull(cut.getListaProductoTipoProducto());
    }

    @Test
    void testModelo_Load() {
        cut.inicializar();
        when(productoTipoProductoCaracteristicaDAO.findRange(anyInt(), anyInt())).thenReturn(List.of(mockEntity));

        List<ProductoTipoProductoCaracteristica> result = cut.getModelo().load(0, 10, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productoTipoProductoCaracteristicaDAO).findRange(0, 10);
    }

    @Test
    void testModelo_Load_Exception() {
        cut.inicializar();
        when(productoTipoProductoCaracteristicaDAO.findRange(anyInt(), anyInt())).thenThrow(new RuntimeException("DB Error"));

        List<ProductoTipoProductoCaracteristica> result = cut.getModelo().load(0, 10, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void testModelo_Count_Exception() {
        cut.inicializar();
        when(productoTipoProductoCaracteristicaDAO.count()).thenThrow(new RuntimeException("DB Error"));

        int count = cut.getModelo().count(Collections.emptyMap());
        assertEquals(0, count);
    }

    @Test
    void testModelo_GetRowKey() {
        cut.inicializar();
        assertEquals(uuidEntity.toString(), cut.getModelo().getRowKey(mockEntity));
        assertNull(cut.getModelo().getRowKey(null));
    }

    @Test
    void testModelo_GetRowData() {
        cut.inicializar();
        when(productoTipoProductoCaracteristicaDAO.findById(uuidEntity)).thenReturn(mockEntity);

        ProductoTipoProductoCaracteristica result = cut.getModelo().getRowData(uuidEntity.toString());
        assertEquals(mockEntity, result);

        assertNull(cut.getModelo().getRowData(null));
        assertNull(cut.getModelo().getRowData("invalid-uuid"));
    }

    @Test
    void testGetDao() {
        assertEquals(productoTipoProductoCaracteristicaDAO, cut.getDao());
    }

    @Test
    void testNuevoRegistro() {
        ProductoTipoProductoCaracteristica nuevo = cut.nuevoRegistro();
        assertNotNull(nuevo);
    }

    @Test
    void testBuscarRegistroPorId() {
        when(productoTipoProductoCaracteristicaDAO.findById(uuidEntity)).thenReturn(mockEntity);

        assertEquals(mockEntity, cut.buscarRegistroPorId(uuidEntity));
        assertEquals(mockEntity, cut.buscarRegistroPorId(uuidEntity.toString()));

        assertNull(cut.buscarRegistroPorId(123));
        assertNull(cut.buscarRegistroPorId("invalid-uuid"));
    }

    @Test
    void testGetIdAsText() {
        assertEquals(uuidEntity.toString(), cut.getIdAsText(mockEntity));
        assertNull(cut.getIdAsText(null));
    }

    @Test
    void testGetIdByText() {
        when(productoTipoProductoCaracteristicaDAO.findById(uuidEntity)).thenReturn(mockEntity);

        assertEquals(mockEntity, cut.getIdByText(uuidEntity.toString()));
        assertNull(cut.getIdByText(null));
        assertNull(cut.getIdByText("invalid-uuid"));
    }

    @Test
    void testEsNombreVacio() {
        ProductoTipoProductoCaracteristica valido = new ProductoTipoProductoCaracteristica();
        valido.setIdProductoTipoProducto(mockPTP);
        valido.setIdTipoProductoCaracteristica(mockTPC);

        assertFalse(cut.esNombreVacio(valido));

        ProductoTipoProductoCaracteristica invalido1 = new ProductoTipoProductoCaracteristica();
        invalido1.setIdProductoTipoProducto(null);
        invalido1.setIdTipoProductoCaracteristica(mockTPC);
        assertTrue(cut.esNombreVacio(invalido1));

        ProductoTipoProductoCaracteristica invalido2 = new ProductoTipoProductoCaracteristica();
        invalido2.setIdProductoTipoProducto(mockPTP);
        invalido2.setIdTipoProductoCaracteristica(null);
        assertTrue(cut.esNombreVacio(invalido2));
    }

    @Test
    void testSelectionHandler() {
        SelectEvent<ProductoTipoProductoCaracteristica> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(mockEntity);

        cut.selectionHandler(event);

        assertEquals(uuidPTP, cut.getIdProductoTipoProductoSeleccionado());
        assertEquals(idTPC, cut.getIdTipoProductoCaracteristicaSeleccionado());

        ProductoTipoProductoCaracteristica vacio = new ProductoTipoProductoCaracteristica();
        when(event.getObject()).thenReturn(vacio);
        cut.selectionHandler(event);
        assertNull(cut.getIdProductoTipoProductoSeleccionado());
        assertNull(cut.getIdTipoProductoCaracteristicaSeleccionado());
    }

    @Test
    void testBtnNuevoHandler() {
        cut.setIdProductoTipoProductoSeleccionado(uuidPTP);
        cut.btnNuevoHandler(null);
        assertNull(cut.getIdProductoTipoProductoSeleccionado());
        assertNull(cut.getIdTipoProductoCaracteristicaSeleccionado());
    }

    @Test
    void testBtnGuardarHandler_Exito() {
        cut.setIdProductoTipoProductoSeleccionado(uuidPTP);
        cut.setIdTipoProductoCaracteristicaSeleccionado(idTPC);

        when(productoTipoProductoDAO.findById(uuidPTP)).thenReturn(mockPTP);
        when(tipoProductoCaracteristicaDAO.findById(idTPC)).thenReturn(mockTPC);

        cut.btnGuardarHandler(null);

        // CORRECCIÓN: Verificamos sobre mockEntity porque cut.getRegistro() es null después de guardar
        assertEquals(mockPTP, mockEntity.getIdProductoTipoProducto());
        assertEquals(mockTPC, mockEntity.getIdTipoProductoCaracteristica());

        verify(productoTipoProductoCaracteristicaDAO).crear(any());
    }

    @Test
    void testBtnGuardarHandler_PTPNoEncontrado() {
        cut.setIdProductoTipoProductoSeleccionado(uuidPTP);
        when(productoTipoProductoDAO.findById(uuidPTP)).thenReturn(null);

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("ProductoTipoProducto no encontrado")));
        verify(productoTipoProductoCaracteristicaDAO, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_TPCNoEncontrado() {
        cut.setIdProductoTipoProductoSeleccionado(uuidPTP);
        cut.setIdTipoProductoCaracteristicaSeleccionado(idTPC);

        when(productoTipoProductoDAO.findById(uuidPTP)).thenReturn(mockPTP);
        when(tipoProductoCaracteristicaDAO.findById(idTPC)).thenReturn(null);

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("TipoProductoCaracteristica no encontrado")));
        verify(productoTipoProductoCaracteristicaDAO, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_Excepcion() {
        cut.setIdProductoTipoProductoSeleccionado(uuidPTP);
        when(productoTipoProductoDAO.findById(uuidPTP)).thenThrow(new RuntimeException("Error DB"));

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m -> m.getDetail().contains("Error al guardar")));
    }

    @Test
    void testGetSetters() {
        List<ProductoTipoProducto> ptpList = new ArrayList<>();
        cut.setListaProductoTipoProducto(ptpList);
        assertEquals(ptpList, cut.getListaProductoTipoProducto());

        List<TipoProductoCaracteristica> tpcList = new ArrayList<>();
        cut.setListaTipoProductoCaracteristica(tpcList);
        assertEquals(tpcList, cut.getListaTipoProductoCaracteristica());

        cut.setIdProductoTipoProductoSeleccionado(uuidPTP);
        assertEquals(uuidPTP, cut.getIdProductoTipoProductoSeleccionado());

        cut.setIdTipoProductoCaracteristicaSeleccionado(idTPC);
        assertEquals(idTPC, cut.getIdTipoProductoCaracteristicaSeleccionado());
    }
}

