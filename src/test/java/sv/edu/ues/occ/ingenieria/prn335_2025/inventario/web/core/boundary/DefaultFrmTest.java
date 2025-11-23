package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultFrmTest {

    @Mock
    FacesContext facesContext;

    @Mock
    InventarioDefaultDataAccess<TestEntity> dao;

    private TestFrm cut;

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private Level originalLogLevel;

    public static class TestEntity {
        private String id;
        private String nombre;

        public TestEntity() {}
        public TestEntity(String id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
        public String getId() { return id; }
        public String getNombre() { return nombre; }
    }

    public class TestFrm extends DefaultFrm<TestEntity> {
        boolean forceException = false;

        @Override protected FacesContext getFacesContext() { return facesContext; }
        @Override protected InventarioDefaultDataAccess<TestEntity> getDao() { return dao; }
        @Override protected TestEntity nuevoRegistro() { return new TestEntity(); }

        @Override
        protected TestEntity buscarRegistroPorId(Object id) {
            if (forceException) throw new RuntimeException("Error DB");
            return new TestEntity((String) id, "Test");
        }

        @Override
        protected String getIdAsText(TestEntity r) {
            if (forceException) throw new RuntimeException("Error DB");
            return r.getId();
        }

        @Override
        protected TestEntity getIdByText(String id) {
            if (forceException) throw new RuntimeException("Error DB");
            return new TestEntity(id, "Test");
        }
    }

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));
        System.setErr(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));

        Logger logger = Logger.getLogger(DefaultFrm.class.getName());
        originalLogLevel = logger.getLevel();
        logger.setLevel(Level.OFF);

        cut = new TestFrm();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        Logger.getLogger(DefaultFrm.class.getName()).setLevel(originalLogLevel);
    }

    @Test
    void testInicializar() {
        cut.inicializar();
        assertNotNull(cut.getModelo());
        assertEquals(8, cut.getPageSize());
    }

    @Test
    void testLazyDataModel_GetRowKey() {
        cut.inicializar();
        TestEntity entity = new TestEntity("50", "Name");
        assertEquals("50", cut.getModelo().getRowKey(entity));
        assertNull(cut.getModelo().getRowKey(null));

        cut.forceException = true;
        assertNull(cut.getModelo().getRowKey(entity));
    }

    @Test
    void testLazyDataModel_GetRowData() {
        cut.inicializar();
        assertNotNull(cut.getModelo().getRowData("50"));
        assertNull(cut.getModelo().getRowData(null));

        cut.forceException = true;
        assertNull(cut.getModelo().getRowData("50"));
    }

    @Test
    void testLazyDataModel_Count() throws Exception {
        cut.inicializar();
        when(dao.count()).thenReturn(100L);
        assertEquals(100, cut.getModelo().count(Collections.emptyMap()));

        when(dao.count()).thenThrow(new RuntimeException("DB"));
        assertEquals(0, cut.getModelo().count(Collections.emptyMap()));
    }

    @Test
    void testLazyDataModel_Load() throws Exception {
        cut.inicializar();
        when(dao.findRange(anyInt(), anyInt())).thenReturn(Collections.singletonList(new TestEntity()));

        List<TestEntity> res = cut.getModelo().load(0, 10, null, null);
        assertFalse(res.isEmpty());

        when(dao.findRange(anyInt(), anyInt())).thenThrow(new RuntimeException("DB"));
        assertTrue(cut.getModelo().load(0, 10, null, null).isEmpty());
    }

    @Test
    void testSelectionHandler() {
        SelectEvent<TestEntity> event = mock(SelectEvent.class);
        TestEntity entity = new TestEntity("1", "Sel");
        when(event.getObject()).thenReturn(entity);

        cut.selectionHandler(event);
        assertEquals(entity, cut.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, cut.getEstado());

        cut.selectionHandler(null);
    }

    @Test
    void testBtnNuevoHandler() {
        cut.btnNuevoHandler(null);
        assertNotNull(cut.getRegistro());
        assertEquals(ESTADO_CRUD.CREAR, cut.getEstado());
    }

    @Test
    void testBtnCancelarHandler() {
        cut.setRegistro(new TestEntity());
        cut.setEstado(ESTADO_CRUD.MODIFICAR);

        cut.btnCancelarHandler(null);

        assertNull(cut.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, cut.getEstado());
    }

    @Test
    void testBtnGuardarHandler_RegistroNull() {
        cut.setRegistro(null);
        cut.btnGuardarHandler(null);
        verify(dao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_NombreVacio() {
        cut.setRegistro(new TestEntity("1", ""));
        cut.btnGuardarHandler(null);

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_WARN, captor.getValue().getSeverity());
        verify(dao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_Exito() throws Exception {
        TestEntity entity = new TestEntity("1", "Juan");
        cut.setRegistro(entity);

        cut.btnGuardarHandler(null);

        verify(dao).crear(entity);
        assertNull(cut.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, cut.getEstado());

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
    }

    @Test
    void testBtnGuardarHandler_Error() throws Exception {
        cut.setRegistro(new TestEntity("1", "Juan"));
        doThrow(new RuntimeException("Error Create")).when(dao).crear(any());

        cut.btnGuardarHandler(null);

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
        assertEquals("Error al guardar", captor.getValue().getSummary());
    }

    @Test
    void testBtnModificarHandler_RegistroNull() {
        cut.setRegistro(null);
        cut.btnModificarHandler(null);
        verify(dao, never()).modificar(any());
    }

    @Test
    void testBtnModificarHandler_NombreVacio() {
        cut.setRegistro(new TestEntity("1", "  "));
        cut.btnModificarHandler(null);

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_WARN, captor.getValue().getSeverity());
        verify(dao, never()).modificar(any());
    }

    @Test
    void testBtnModificarHandler_Exito() throws Exception {
        TestEntity entity = new TestEntity("1", "Juan");
        cut.setRegistro(entity);
        when(dao.modificar(entity)).thenReturn(entity);

        cut.btnModificarHandler(null);

        verify(dao).modificar(entity);
        assertNull(cut.getRegistro());

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
    }

    @Test
    void testBtnModificarHandler_Excepcion() throws Exception {
        TestEntity entity = new TestEntity("1", "Juan");
        cut.setRegistro(entity);
        doThrow(new RuntimeException("Error Update")).when(dao).modificar(any());

        cut.btnModificarHandler(null);

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
        assertEquals("Error al modificar", captor.getValue().getSummary());
    }

    @Test
    void testBtnEliminarHandler_RegistroNull() {
        cut.setRegistro(null);
        cut.btnEliminarHandler(null);
        verify(dao, never()).eliminar(any());
    }

    @Test
    void testBtnEliminarHandler_Exito() throws Exception {
        TestEntity entity = new TestEntity("1", "Juan");
        cut.setRegistro(entity);

        cut.btnEliminarHandler(null);

        verify(dao).eliminar(entity);
        assertNull(cut.getRegistro());

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
    }

    @Test
    void testBtnEliminarHandler_Error() throws Exception {
        cut.setRegistro(new TestEntity("1", "Juan"));
        doThrow(new RuntimeException("Error Delete")).when(dao).eliminar(any());

        cut.btnEliminarHandler(null);

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
        assertEquals("Error al eliminar", captor.getValue().getSummary());
    }

    @Test
    void testEsNombreVacio_ReflectionFail() {
        // Objeto sin método getNombre
        Object dummy = new Object();

        // Creamos instancia anónima cruda para acceder al método protected
        DefaultFrm<Object> rawCut = new DefaultFrm<Object>() {
            @Override protected FacesContext getFacesContext() { return null; }
            @Override protected InventarioDefaultDataAccess<Object> getDao() { return null; }
            @Override protected Object nuevoRegistro() { return null; }
            @Override protected Object buscarRegistroPorId(Object id) { return null; }
            @Override protected String getIdAsText(Object r) { return null; }
            @Override protected Object getIdByText(String id) { return null; }
        };

        // Debe retornar true porque falla la reflexión (no existe getNombre)
        assertTrue(rawCut.esNombreVacio(dummy));
    }

    @Test
    void testGetSetters() {
        cut.setNombreBean("TestBean");
        assertEquals("TestBean", cut.getNombreBean());

        cut.setPageSize(20);
        assertEquals(20, cut.getPageSize());

        cut.setModelo(null);
        assertNull(cut.getModelo());
    }
}