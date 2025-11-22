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
import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    TestFrm cut;
    Level originalLevel;

    public static class TestEntity {
        private String id;
        private String nombre;

        public TestEntity() {}
        public TestEntity(String id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
    }

    public class TestFrm extends DefaultFrm<TestEntity> {
        boolean throwException = false;

        @Override
        protected FacesContext getFacesContext() {
            return facesContext;
        }

        @Override
        protected InventarioDefaultDataAccess<TestEntity> getDao() {
            return dao;
        }

        @Override
        protected TestEntity nuevoRegistro() {
            return new TestEntity();
        }

        @Override
        protected TestEntity buscarRegistroPorId(Object id) {
            if(throwException) throw new RuntimeException("Error forzado");
            if (id.equals("1")) return new TestEntity("1", "Test");
            return null;
        }

        @Override
        protected String getIdAsText(TestEntity r) {
            if(throwException) throw new RuntimeException("Error forzado");
            return r.getId();
        }

        @Override
        protected TestEntity getIdByText(String id) {
            if(throwException) throw new RuntimeException("Error forzado");
            if ("1".equals(id)) return new TestEntity("1", "Test");
            return null;
        }
    }

    @BeforeEach
    void setUp() {
        Logger logger = Logger.getLogger(DefaultFrm.class.getName());
        originalLevel = logger.getLevel();
        logger.setLevel(Level.OFF);

        cut = new TestFrm();
    }

    @AfterEach
    void tearDown() {
        Logger.getLogger(DefaultFrm.class.getName()).setLevel(originalLevel);
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
        TestEntity entity = new TestEntity("1", "Juan");

        String key = cut.getModelo().getRowKey(entity);
        assertEquals("1", key);

        assertNull(cut.getModelo().getRowKey(null));

        cut.throwException = true;
        assertNull(cut.getModelo().getRowKey(entity));
    }

    @Test
    void testLazyDataModel_GetRowData() {
        cut.inicializar();

        TestEntity entity = cut.getModelo().getRowData("1");
        assertNotNull(entity);
        assertEquals("1", entity.getId());

        assertNull(cut.getModelo().getRowData(null));

        cut.throwException = true;
        assertNull(cut.getModelo().getRowData("1"));
    }

    @Test
    void testLazyDataModel_Count() throws Exception {
        cut.inicializar();
        when(dao.count()).thenReturn(10L);

        int count = cut.getModelo().count(Collections.emptyMap());
        assertEquals(10, count);

        when(dao.count()).thenThrow(new RuntimeException("DB Error"));
        int countError = cut.getModelo().count(Collections.emptyMap());
        assertEquals(0, countError);
    }

    @Test
    void testLazyDataModel_Load() throws Exception {
        cut.inicializar();
        List<TestEntity> list = Collections.singletonList(new TestEntity("1", "A"));
        when(dao.findRange(anyInt(), anyInt())).thenReturn(list);

        List<TestEntity> result = cut.getModelo().load(0, 10, Collections.emptyMap(), Collections.emptyMap());
        assertEquals(1, result.size());

        when(dao.findRange(anyInt(), anyInt())).thenThrow(new RuntimeException("DB Error"));
        List<TestEntity> resultError = cut.getModelo().load(0, 10, Collections.emptyMap(), Collections.emptyMap());
        assertTrue(resultError.isEmpty());
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
    void testBtnGuardarHandler_Null() {
        cut.setRegistro(null);
        cut.btnGuardarHandler(null);
        verify(dao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_NombreVacio() {
        cut.setRegistro(new TestEntity("1", ""));
        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_WARN
        ));
        verify(dao, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_Exito() throws Exception {
        cut.setRegistro(new TestEntity("1", "Juan"));
        cut.btnGuardarHandler(null);

        verify(dao).crear(any());
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_INFO
        ));
        assertNull(cut.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, cut.getEstado());
    }

    @Test
    void testBtnGuardarHandler_Excepcion() throws Exception {
        cut.setRegistro(new TestEntity("1", "Juan"));
        doThrow(new RuntimeException("Error")).when(dao).crear(any());

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_ERROR
        ));
    }

    @Test
    void testBtnEliminarHandler_Null() {
        cut.setRegistro(null);
        cut.btnEliminarHandler(null);
        verify(dao, never()).eliminar(any());
    }

    @Test
    void testBtnEliminarHandler_Exito() throws Exception {
        cut.setRegistro(new TestEntity("1", "Juan"));
        cut.btnEliminarHandler(null);

        verify(dao).eliminar(any());
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_INFO
        ));
        assertNull(cut.getRegistro());
    }

    @Test
    void testBtnEliminarHandler_Excepcion() throws Exception {
        cut.setRegistro(new TestEntity("1", "Juan"));
        doThrow(new RuntimeException("Error")).when(dao).eliminar(any());

        cut.btnEliminarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_ERROR
        ));
    }

    @Test
    void testBtnModificarHandler_Null() {
        cut.setRegistro(null);
        cut.btnModificarHandler(null);
        verify(dao, never()).modificar(any());
    }

    @Test
    void testBtnModificarHandler_NombreVacio() {
        cut.setRegistro(new TestEntity("1", null));
        cut.btnModificarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_WARN
        ));
        verify(dao, never()).modificar(any());
    }

    @Test
    void testBtnModificarHandler_Exito() throws Exception {
        cut.setRegistro(new TestEntity("1", "Juan"));
        cut.btnModificarHandler(null);

        verify(dao).modificar(any());
        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_INFO
        ));
        assertNull(cut.getRegistro());
    }

    @Test
    void testBtnModificarHandler_Excepcion() throws Exception {
        cut.setRegistro(new TestEntity("1", "Juan"));
        doThrow(new RuntimeException("Error")).when(dao).modificar(any());

        cut.btnModificarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_ERROR
        ));
    }

    @Test
    void testEsNombreVacio_Reflection() {
        TestEntity valido = new TestEntity("1", "Juan");
        assertFalse(cut.esNombreVacio(valido));

        TestEntity vacio = new TestEntity("1", "");
        assertTrue(cut.esNombreVacio(vacio));

        TestEntity nulo = new TestEntity("1", null);
        assertTrue(cut.esNombreVacio(nulo));
    }

    @Test
    void testEsNombreVacio_ReflectionError() {
        Object objetoSinMetodo = new Object();
        DefaultFrm<Object> cutObj = new DefaultFrm<Object>() {
            @Override protected FacesContext getFacesContext() { return null; }
            @Override protected InventarioDefaultDataAccess<Object> getDao() { return null; }
            @Override protected Object nuevoRegistro() { return null; }
            @Override protected Object buscarRegistroPorId(Object id) { return null; }
            @Override protected String getIdAsText(Object r) { return null; }
            @Override protected Object getIdByText(String id) { return null; }
        };

        assertTrue(cutObj.esNombreVacio(objetoSinMetodo));
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