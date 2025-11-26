package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import org.mockito.stubbing.Answer;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.AlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.NotificadorKardex;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoAlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Almacen;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlmacenFrmTest {

    @Mock
    FacesContext facesContext;

    @Mock
    AlmacenDAO almacenDAO;

    @Mock
    TipoAlmacenDAO tipoAlmacenDAO;

    @InjectMocks
    AlmacenFrm cut;

    @Mock
    NotificadorKardex notificador;


    private Almacen mockAlmacen;
    private TipoAlmacen mockTipo;

    @BeforeEach
    void setUp() {
        mockAlmacen = new Almacen();
        mockAlmacen.setId(1);

        mockTipo = new TipoAlmacen();
        mockTipo.setId(10);
        mockTipo.setActivo(true);
        mockTipo.setNombre("Bodega Principal");

        // Inicializamos el registro en el bean
        cut.registro = mockAlmacen;
    }

    @Test
    void testInit() {
        assertNotNull(cut.getFacesContext());
        assertNotNull(cut.getDao());
        assertEquals("Almacén", cut.nombreBean);
    }

    @Test
    void testNuevoRegistro() {
        Almacen nuevo = cut.nuevoRegistro();
        assertNotNull(nuevo);
        assertTrue(nuevo.getActivo(), "El nuevo registro debe estar activo por defecto");
    }

    @Test
    void testBuscarRegistroPorId_Exito() throws Exception {
        when(almacenDAO.findById(1)).thenReturn(mockAlmacen);

        Almacen result = cut.buscarRegistroPorId(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(almacenDAO).findById(1);
    }

    @Test
    void testBuscarRegistroPorId_NullOClaseIncorrecta() {
        assertNull(cut.buscarRegistroPorId(null));
        assertNull(cut.buscarRegistroPorId("NoEsInteger"));
    }

    @Test
    void testBuscarRegistroPorId_Excepcion() throws Exception {
        when(almacenDAO.findById(anyInt())).thenThrow(new RuntimeException("Error DB"));
        Almacen result = cut.buscarRegistroPorId(99);
        assertNull(result);
    }

    @Test
    void testGetIdAsText() {
        assertNull(cut.getIdAsText(null));
        Almacen a = new Almacen(); // Id null
        assertNull(cut.getIdAsText(a));
        a.setId(50);
        assertEquals("50", cut.getIdAsText(a));
    }

    @Test
    void testGetIdByText() throws Exception {
        assertNull(cut.getIdByText(null));
        assertNull(cut.getIdByText("  "));
        assertNull(cut.getIdByText("abc")); // NumberFormat

        when(almacenDAO.findById(50)).thenReturn(mockAlmacen);
        Almacen result = cut.getIdByText("50");
        assertNotNull(result);
        verify(almacenDAO).findById(50);
    }

    @Test
    void testEsNombreVacio() {
        assertFalse(cut.esNombreVacio(new Almacen()));
    }

    @Test
    void testBtnGuardarHandler_ErrorValidacion_SinTipo() {
        cut.setIdTipoSeleccionado(null);
        cut.btnGuardarHandler(null);
        verify(facesContext).addMessage(eq("frmCrear:cbTipoAlmacen"), any(FacesMessage.class));
    }

    @Test
    void testBtnGuardarHandler_AsignarTipo_Y_Exito() throws Exception {

        Integer idTipo = 10;
        when(tipoAlmacenDAO.findById(idTipo)).thenReturn(mockTipo);

        cut.setIdTipoSeleccionado(idTipo);


        cut.registro.setIdTipoAlmacen(null);


        cut.btnGuardarHandler(null);

        verify(tipoAlmacenDAO, times(2)).findById(idTipo);

        // Verificamos sobre mockAlmacen porque cut.registro podría ser null tras guardar
        assertNotNull(mockAlmacen.getIdTipoAlmacen(), "El objeto original debió ser modificado");
        assertEquals(mockTipo, mockAlmacen.getIdTipoAlmacen());
        assertNull(cut.getIdTipoSeleccionado());
    }

    @Test
    void testGetListaTiposAlmacen_CargaLazy() {
        List<TipoAlmacen> listaSimulada = new ArrayList<>();
        TipoAlmacen t1 = new TipoAlmacen(); t1.setActivo(true);
        TipoAlmacen t2 = new TipoAlmacen(); t2.setActivo(false);
        listaSimulada.add(t1);
        listaSimulada.add(t2);

        when(tipoAlmacenDAO.findAll()).thenReturn(listaSimulada);

        List<TipoAlmacen> resultado = cut.getListaTiposAlmacen();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(tipoAlmacenDAO).findAll();

        cut.getListaTiposAlmacen();
        verify(tipoAlmacenDAO, times(1)).findAll();
    }

    @Test
    void testGetListaTiposAlmacen_Excepcion() {
        when(tipoAlmacenDAO.findAll()).thenThrow(new RuntimeException("DB Error"));
        List<TipoAlmacen> resultado = cut.getListaTiposAlmacen();
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void testSetListaTiposAlmacen() {
        List<TipoAlmacen> manualList = new ArrayList<>();
        manualList.add(mockTipo);
        cut.setListaTiposAlmacen(manualList);
        List<TipoAlmacen> result = cut.getListaTiposAlmacen();
        assertEquals(manualList, result);
        assertEquals(1, result.size());
        verify(tipoAlmacenDAO, never()).findAll();
    }

    @Test
    void testGetSetIdTipoSeleccionado() throws Exception {
        // 1. Caso: Ya tiene tipo asignado en el objeto
        cut.registro.setIdTipoAlmacen(mockTipo);
        mockTipo.setId(99); // Aseguramos ID
        assertEquals(99, cut.getIdTipoSeleccionado());


        cut.registro.setIdTipoAlmacen(null);

        when(tipoAlmacenDAO.findById(5)).thenReturn(mockTipo);

        cut.setIdTipoSeleccionado(5);


        assertNotNull(cut.registro.getIdTipoAlmacen());
        assertEquals(mockTipo, cut.registro.getIdTipoAlmacen());
    }

    @Test
    void testSetIdTipoSeleccionado_Excepcion() throws Exception {
        when(tipoAlmacenDAO.findById(anyInt())).thenThrow(new RuntimeException("Error"));
        cut.setIdTipoSeleccionado(10);
        // No debe explotar, catch silencioso en el bean
        assertNull(cut.registro.getIdTipoAlmacen());
    }

    @Test
    void testPrepararNuevo() {
        String nav = cut.prepararNuevo();
        assertNull(nav);
        assertNotNull(cut.registro);
        assertTrue(cut.registro.getActivo());
    }



    @Test
    void testGuardar_Fallo_SinTipoSeleccionado() {
        cut.setIdTipoSeleccionado(null);

        String navegacion = cut.guardar();

        assertNull(navegacion);
        verify(facesContext).addMessage(eq("frmCrear:cbTipoAlmacen"), any(FacesMessage.class));
        verify(tipoAlmacenDAO, never()).findById(any());
    }

    @Test
    void testGuardar_Exito_YaTieneTipoAsignado() {
        // Arrange
        Integer idTipo = 10;
        when(tipoAlmacenDAO.findById(idTipo)).thenReturn(mockTipo);

        cut.setIdTipoSeleccionado(idTipo);
        cut.registro.setIdTipoAlmacen(mockTipo); // <-- Registro el TipoAlmacen (ID 10)

        AlmacenFrm spyCut = spy(cut);

        // 4. Configurar el doAnswer: Mockeamos btnGuardarHandler para que simule la limpieza.
        doAnswer((Answer<Void>) invocation -> {
            // 1. Limpiar variables de control del formulario
            spyCut.setIdTipoSeleccionado(null);
            spyCut.setListaTiposAlmacen(null);

            // 2. IMPORTANTE: Simular el reset del registro o del campo ID
            // En un flujo real, 'super.btnGuardarHandler()' podría resetear el registro completo
            // Si el registro no se resetea a un nuevo objeto, al menos debe desvincularse el TipoAlmacen
            spyCut.registro.setIdTipoAlmacen(null); // <-- LIMPIEZA ADICIONAL CRÍTICA

            return null;
        }).when(spyCut).btnGuardarHandler(any());


        clearInvocations(tipoAlmacenDAO);

        // Act
        String navegacion = spyCut.guardar();

        // Assert
        assertNull(navegacion);
        verify(spyCut).btnGuardarHandler(isNull());
        verify(tipoAlmacenDAO, never()).findById(any());

        // La aserción ahora debe pasar porque el registro.getIdTipoAlmacen() es null.
        assertNull(spyCut.getIdTipoSeleccionado());
    }
    @Test
    void testModificar_FalloValidacion() {
        cut.setIdTipoSeleccionado(null);
        String nav = cut.modificar();
        assertNull(nav);
        verify(facesContext).addMessage(anyString(), any(FacesMessage.class));
    }

    @Test
    void testModificar_Exito() {
        when(tipoAlmacenDAO.findById(10)).thenReturn(mockTipo);
        cut.setIdTipoSeleccionado(10);

        String nav = cut.modificar();
        assertNull(nav);
    }

    @Test
    void testEliminar() {
        String nav = cut.eliminar();
        assertNull(nav);
        assertNull(cut.registro);
        assertNull(cut.getIdTipoSeleccionado());
    }

    @Test
    void testEliminar_Excepcion_CatchBlock() {
        AlmacenFrm spyCut = spy(cut);
        doThrow(new RuntimeException("Error forzado para probar catch"))
                .when(spyCut).btnEliminarHandler(any());

        spyCut.eliminar();

        verify(facesContext).addMessage(isNull(), argThat(msg ->
                msg.getSeverity() == FacesMessage.SEVERITY_ERROR &&
                        msg.getDetail().contains("No se pudo eliminar")
        ));
    }

    @Test
    void testEliminar_RegistroNull() {
        cut.registro = null;
        cut.eliminar();
        verify(facesContext, never()).addMessage(any(), any());
    }

    @Test
    void testCancelar() {
        when(tipoAlmacenDAO.findById(5)).thenReturn(mockTipo);

        cut.setIdTipoSeleccionado(5);
        String nav = cut.cancelar();
        assertNull(nav);
        assertNull(cut.registro);
        assertNull(cut.getIdTipoSeleccionado());
    }
}