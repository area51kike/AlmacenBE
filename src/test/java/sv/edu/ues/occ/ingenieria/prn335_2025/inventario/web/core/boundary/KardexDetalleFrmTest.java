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
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.KardexDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.KardexDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Kardex;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.KardexDetalle;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KardexDetalleFrmTest {

    @Mock
    KardexDetalleDAO kardexDetalleDAO;

    @Mock
    KardexDAO kardexDAO;

    @Mock
    FacesContext facesContext;

    @Mock
    LazyDataModel<KardexDetalle> mockModelo;

    @InjectMocks
    KardexDetalleFrm cut;

    private MockedStatic<FacesContext> mockedFacesContext;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private KardexDetalle mockEntity;
    private Kardex mockKardex;
    private UUID uuidEntity;
    private UUID uuidKardex;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));
        System.setErr(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));

        mockedFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

        uuidEntity = UUID.randomUUID();
        uuidKardex = UUID.randomUUID();

        mockKardex = new Kardex();
        mockKardex.setId(uuidKardex);

        mockEntity = new KardexDetalle();
        mockEntity.setId(uuidEntity);
        mockEntity.setIdKardex(mockKardex);
        mockEntity.setActivo(true);

        cut.setRegistro(mockEntity);

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
        when(kardexDAO.findAll()).thenReturn(List.of(mockKardex));

        cut.inicializar();

        assertEquals("kardexDetalleFrm", cut.nombreBean);
        assertNotNull(cut.getListaKardex());
        assertFalse(cut.getListaKardex().isEmpty());
        assertNotNull(cut.getDao());
        assertNotNull(cut.getFacesContext());
    }

    @Test
    void testNuevoRegistro() {
        KardexDetalle nuevo = cut.nuevoRegistro();
        assertNotNull(nuevo);
        assertNotNull(nuevo.getId());
        assertTrue(nuevo.getActivo());
        assertNull(cut.getIdKardexSeleccionado());
    }

    @Test
    void testBuscarRegistroPorId() {
        when(kardexDetalleDAO.findById(uuidEntity)).thenReturn(mockEntity);

        assertEquals(mockEntity, cut.buscarRegistroPorId(uuidEntity));
        assertEquals(mockEntity, cut.buscarRegistroPorId(uuidEntity.toString()));

        assertNull(cut.buscarRegistroPorId(123));
        assertNull(cut.buscarRegistroPorId("invalid-uuid"));
    }

    @Test
    void testGetIdAsText() {
        assertEquals(uuidEntity.toString(), cut.getIdAsText(mockEntity));
        assertNull(cut.getIdAsText(null));
        assertNull(cut.getIdAsText(new KardexDetalle()));
    }

    @Test
    void testGetIdByText() {
        when(kardexDetalleDAO.findById(uuidEntity)).thenReturn(mockEntity);

        assertEquals(mockEntity, cut.getIdByText(uuidEntity.toString()));
        assertNull(cut.getIdByText(null));
        assertNull(cut.getIdByText(""));
        assertNull(cut.getIdByText("invalid-uuid"));
    }

    @Test
    void testSelectionHandler() {
        SelectEvent<KardexDetalle> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(mockEntity);

        cut.selectionHandler(event);

        assertEquals(uuidKardex.toString(), cut.getIdKardexSeleccionado());

        KardexDetalle sinKardex = new KardexDetalle();
        when(event.getObject()).thenReturn(sinKardex);

        cut.setIdKardexSeleccionado(null);
        cut.selectionHandler(event);
        assertNull(cut.getIdKardexSeleccionado());
    }

    @Test
    void testBtnGuardarHandler_RegistroNull() {
        cut.setRegistro(null);
        cut.btnGuardarHandler(null);
        verify(kardexDetalleDAO, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_ValidacionKardex() {
        cut.setIdKardexSeleccionado(null);
        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_WARN && m.getDetail().contains("Debe seleccionar un Kardex")
        ));
        verify(kardexDetalleDAO, never()).crear(any());

        cut.setIdKardexSeleccionado("");
        cut.btnGuardarHandler(null);
        verify(kardexDetalleDAO, never()).crear(any());
    }

    @Test
    void testBtnGuardarHandler_Exito() {
        cut.setIdKardexSeleccionado(uuidKardex.toString());
        setEstado(cut, "CREAR");

        when(kardexDAO.findById(uuidKardex)).thenReturn(mockKardex);

        cut.btnGuardarHandler(null);

        assertEquals(mockKardex, mockEntity.getIdKardex());
        verify(kardexDetalleDAO).crear(mockEntity);

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
        assertTrue(captor.getValue().getDetail().contains("creado correctamente"));

        assertNull(cut.getRegistro());
    }

    @Test
    void testBtnGuardarHandler_Excepcion() {
        cut.setIdKardexSeleccionado(uuidKardex.toString());
        setEstado(cut, "CREAR");

        when(kardexDAO.findById(uuidKardex)).thenReturn(mockKardex);
        doThrow(new RuntimeException("Error Fatal")).when(kardexDetalleDAO).crear(any());

        cut.btnGuardarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_ERROR && m.getSummary().contains("Error al guardar")
        ));
    }

    @Test
    void testBtnModificarHandler_RegistroNull() {
        cut.setRegistro(null);
        cut.btnModificarHandler(null);
        verify(kardexDetalleDAO, never()).modificar(any());
    }

    @Test
    void testBtnModificarHandler_ValidacionKardex() {
        cut.setIdKardexSeleccionado(null);
        cut.btnModificarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_WARN && m.getDetail().contains("Debe seleccionar un Kardex")
        ));
        verify(kardexDetalleDAO, never()).modificar(any());
    }

    @Test
    void testBtnModificarHandler_Exito() {
        cut.setIdKardexSeleccionado(uuidKardex.toString());
        setEstado(cut, "MODIFICAR");

        when(kardexDAO.findById(uuidKardex)).thenReturn(mockKardex);

        cut.btnModificarHandler(null);

        assertEquals(mockKardex, mockEntity.getIdKardex());
        verify(kardexDetalleDAO).modificar(mockEntity);

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
        assertTrue(captor.getValue().getDetail().contains("actualizado correctamente"));

        assertNull(cut.getRegistro());
    }

    @Test
    void testBtnModificarHandler_Excepcion() {
        cut.setIdKardexSeleccionado(uuidKardex.toString());
        setEstado(cut, "MODIFICAR");

        when(kardexDAO.findById(uuidKardex)).thenReturn(mockKardex);
        doThrow(new RuntimeException("Error Update")).when(kardexDetalleDAO).modificar(any());

        cut.btnModificarHandler(null);

        verify(facesContext).addMessage(isNull(), argThat(m ->
                m.getSeverity() == FacesMessage.SEVERITY_ERROR && m.getSummary().contains("Error al modificar")
        ));
    }

    @Test
    void testGetSetters() {
        List<Kardex> kList = new ArrayList<>();
        cut.setListaKardex(kList);
        assertEquals(kList, cut.getListaKardex());

        String idStr = uuidKardex.toString();
        cut.setIdKardexSeleccionado(idStr);
        assertEquals(idStr, cut.getIdKardexSeleccionado());
    }

    private void setEstado(KardexDetalleFrm bean, String nombreEstado) {
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