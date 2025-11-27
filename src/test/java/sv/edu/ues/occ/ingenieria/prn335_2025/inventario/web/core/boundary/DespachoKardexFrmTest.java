package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.*;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DespachoKardexFrmTest {

    @Mock
    private FacesContext facesContext;

    @Mock
    private KardexDAO kardexDAO;

    @Mock
    private AlmacenDAO almacenDAO;

    @Mock
    private VentaDetalleDAO ventaDetalleDAO;

    @Mock
    private KardexDetalleDAO kardexDetalleDAO;

    @Mock
    private VentaDAO ventaDAO;

    @Mock
    private ActionEvent actionEvent;

    @InjectMocks
    private DespachoKardexFrm despachoKardexFrm;

    private Kardex kardexTest;
    private VentaDetalle ventaDetalleTest;
    private Almacen almacenTest;
    private Venta ventaTest;
    private Producto productoTest;

    @BeforeEach
    void setUp() {
        // Crear entidades de prueba
        kardexTest = new Kardex();
        kardexTest.setId(UUID.randomUUID());
        kardexTest.setTipoMovimiento("SALIDA");
        kardexTest.setFecha(OffsetDateTime.now());
        kardexTest.setCantidad(BigDecimal.TEN);
        kardexTest.setPrecio(BigDecimal.valueOf(100));

        productoTest = new Producto();
        productoTest.setId(UUID.randomUUID());
        productoTest.setNombreProducto("Producto Test");

        ventaTest = new Venta();
        ventaTest.setId(UUID.randomUUID());
        ventaTest.setEstado("PENDIENTE");

        ventaDetalleTest = new VentaDetalle();
        ventaDetalleTest.setId(UUID.randomUUID());
        ventaDetalleTest.setIdProducto(productoTest);
        ventaDetalleTest.setIdVenta(ventaTest);
        ventaDetalleTest.setCantidad(BigDecimal.TEN);
        ventaDetalleTest.setPrecio(BigDecimal.valueOf(100));
        ventaDetalleTest.setEstado("PENDIENTE");

        // Almacen usa Integer como ID
        almacenTest = new Almacen();
        almacenTest.setId(1);
        almacenTest.setObservaciones("Almacén Principal");
        almacenTest.setActivo(true);
    }

    @Test
    void nuevoRegistro_DebeCrearKardexConValoresPorDefecto() {
        // When
        Kardex resultado = despachoKardexFrm.nuevoRegistro();

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertEquals("SALIDA", resultado.getTipoMovimiento());
        assertNotNull(resultado.getFecha());
        assertEquals(BigDecimal.ZERO, resultado.getCantidad());
        assertEquals(BigDecimal.ZERO, resultado.getPrecio());
        assertEquals(BigDecimal.ZERO, resultado.getCantidadActual());
        assertEquals(BigDecimal.ZERO, resultado.getPrecioActual());
    }

    @Test
    void buscarRegistroPorId_ConUUIDValido_DebeRetornarKardex() {
        // Given
        UUID id = kardexTest.getId();
        List<Kardex> listaKardex = new ArrayList<>();
        listaKardex.add(kardexTest);
        when(kardexDAO.findAll()).thenReturn(listaKardex);

        // When
        Kardex resultado = despachoKardexFrm.buscarRegistroPorId(id);

        // Then
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
    }

    @Test
    void buscarRegistroPorId_ConIdNull_DebeRetornarNull() {
        // When
        Kardex resultado = despachoKardexFrm.buscarRegistroPorId(null);

        // Then
        assertNull(resultado);
    }

    @Test
    void buscarRegistroPorId_ConStringValido_DebeRetornarKardex() {
        // Given
        UUID id = kardexTest.getId();
        List<Kardex> listaKardex = new ArrayList<>();
        listaKardex.add(kardexTest);
        when(kardexDAO.findAll()).thenReturn(listaKardex);

        // When
        Kardex resultado = despachoKardexFrm.buscarRegistroPorId(id.toString());

        // Then
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
    }

    @Test
    void buscarRegistroPorId_ConStringInvalido_DebeRetornarNull() {
        // When
        Kardex resultado = despachoKardexFrm.buscarRegistroPorId("invalid-uuid");

        // Then
        assertNull(resultado);
    }

    @Test
    void getIdAsText_ConKardexValido_DebeRetornarStringDeId() {
        // Given
        UUID id = kardexTest.getId();

        // When
        String resultado = despachoKardexFrm.getIdAsText(kardexTest);

        // Then
        assertNotNull(resultado);
        assertEquals(id.toString(), resultado);
    }

    @Test
    void getIdAsText_ConKardexNull_DebeRetornarNull() {
        // When
        String resultado = despachoKardexFrm.getIdAsText(null);

        // Then
        assertNull(resultado);
    }

    @Test
    void getIdByText_ConStringValido_DebeRetornarKardex() {
        // Given
        UUID id = kardexTest.getId();
        List<Kardex> listaKardex = new ArrayList<>();
        listaKardex.add(kardexTest);
        when(kardexDAO.findAll()).thenReturn(listaKardex);

        // When
        Kardex resultado = despachoKardexFrm.getIdByText(id.toString());

        // Then
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
    }

    @Test
    void getIdByText_ConStringNull_DebeRetornarNull() {
        // When
        Kardex resultado = despachoKardexFrm.getIdByText(null);

        // Then
        assertNull(resultado);
    }

    @Test
    void getIdByText_ConStringInvalido_DebeRetornarNull() {
        // When
        Kardex resultado = despachoKardexFrm.getIdByText("invalid-uuid");

        // Then
        assertNull(resultado);
    }

    @Test
    void getListaAlmacenes_DebeFiltrarAlmacenesActivos() {
        // Given
        Almacen almacenInactivo = new Almacen();
        almacenInactivo.setId(2);
        almacenInactivo.setActivo(false);

        List<Almacen> listaAlmacenes = new ArrayList<>();
        listaAlmacenes.add(almacenTest);
        listaAlmacenes.add(almacenInactivo);

        when(almacenDAO.findAll()).thenReturn(listaAlmacenes);

        // When
        List<Almacen> resultado = despachoKardexFrm.getListaAlmacenes();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActivo());
    }

    @Test
    void prepararKardex_DebeInicializarKardexYDetalle() {
        // When
        despachoKardexFrm.prepararKardex(ventaDetalleTest);

        // Then
        assertNotNull(despachoKardexFrm.getRegistro());
        assertEquals(productoTest, despachoKardexFrm.getRegistro().getIdProducto());
        assertEquals(ventaDetalleTest, despachoKardexFrm.getRegistro().getIdVentaDetalle());
        assertEquals(BigDecimal.TEN, despachoKardexFrm.getRegistro().getCantidad());
        assertEquals(BigDecimal.valueOf(100), despachoKardexFrm.getRegistro().getPrecio());
        assertNull(despachoKardexFrm.getRegistro().getIdCompraDetalle());
        assertNotNull(despachoKardexFrm.getDetalleKardex());
        assertEquals(ventaDetalleTest, despachoKardexFrm.getDetalleActual());
    }

    @Test
    void btnRecibirHandler_SinAlmacen_DebeMostrarError() {
        // Given
        despachoKardexFrm.prepararKardex(ventaDetalleTest);
        despachoKardexFrm.getRegistro().setIdAlmacen(null);

        // When
        despachoKardexFrm.btnRecibirHandler(actionEvent);

        // Then
        verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
        verify(kardexDAO, never()).crear(any());
    }

    @Test
    void btnRecibirHandler_ConDatosValidos_DebeGuardarYActualizarEstado() {
        // Given
        despachoKardexFrm.prepararKardex(ventaDetalleTest);
        despachoKardexFrm.getRegistro().setIdAlmacen(almacenTest);

        when(ventaDetalleDAO.todosDetallesDespachados(any())).thenReturn(false);

        // When
        despachoKardexFrm.btnRecibirHandler(actionEvent);

        // Then
        verify(kardexDAO).crear(any(Kardex.class));
        verify(kardexDetalleDAO).crear(any(KardexDetalle.class));
        verify(ventaDetalleDAO).modificar(ventaDetalleTest);
        assertEquals("DESPACHADO", ventaDetalleTest.getEstado());
        verify(facesContext, atLeastOnce()).addMessage(isNull(), any(FacesMessage.class));
    }

    @Test
    void btnRecibirHandler_CuandoTodosDetallesDespachados_DebeActualizarEstadoVenta() {
        // Given
        despachoKardexFrm.prepararKardex(ventaDetalleTest);
        despachoKardexFrm.getRegistro().setIdAlmacen(almacenTest);

        when(ventaDetalleDAO.todosDetallesDespachados(ventaTest.getId())).thenReturn(true);

        // When
        despachoKardexFrm.btnRecibirHandler(actionEvent);

        // Then
        verify(kardexDAO).crear(any(Kardex.class));
        verify(ventaDAO).actualizarEstado(ventaTest.getId(), "DESPACHADA");
        verify(facesContext, atLeast(2)).addMessage(isNull(), any(FacesMessage.class));
    }

    @Test
    void btnRecibirHandler_ConExcepcion_DebeMostrarError() {
        // Given
        despachoKardexFrm.prepararKardex(ventaDetalleTest);
        despachoKardexFrm.getRegistro().setIdAlmacen(almacenTest);

        doThrow(new RuntimeException("Error de prueba")).when(kardexDAO).crear(any());

        // When
        despachoKardexFrm.btnRecibirHandler(actionEvent);

        // Then
        verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
    }

    @Test
    void getDetalleActual_DebeRetornarDetalleActual() {
        // Given
        despachoKardexFrm.setDetalleActual(ventaDetalleTest);

        // When
        VentaDetalle resultado = despachoKardexFrm.getDetalleActual();

        // Then
        assertEquals(ventaDetalleTest, resultado);
    }

    @Test
    void setDetalleActual_DebeAsignarDetalleActual() {
        // When
        despachoKardexFrm.setDetalleActual(ventaDetalleTest);

        // Then
        assertEquals(ventaDetalleTest, despachoKardexFrm.getDetalleActual());
    }

    @Test
    void getDetalleKardex_DebeRetornarDetalleKardex() {
        // When
        KardexDetalle resultado = despachoKardexFrm.getDetalleKardex();

        // Then
        assertNotNull(resultado);
    }

    @Test
    void limpiar_DebeLimpiarTodosLosCampos() {
        // Given
        despachoKardexFrm.prepararKardex(ventaDetalleTest);
        despachoKardexFrm.getRegistro().setIdAlmacen(almacenTest);

        // When
        despachoKardexFrm.limpiar();

        // Then
        assertNull(despachoKardexFrm.getRegistro());
        assertNotNull(despachoKardexFrm.getDetalleKardex());
        assertNull(despachoKardexFrm.getDetalleActual());
    }

    @Test
    void getDao_DebeRetornarKardexDAO() {
        // When
        InventarioDefaultDataAccess<Kardex> resultado = despachoKardexFrm.getDao();

        // Then
        assertNotNull(resultado);
        assertEquals(kardexDAO, resultado);
    }

    @Test
    void prepararKardex_DebeInicializarKardexDetalleConIdUnico() {
        // When
        despachoKardexFrm.prepararKardex(ventaDetalleTest);

        // Then
        KardexDetalle detalle = despachoKardexFrm.getDetalleKardex();
        assertNotNull(detalle);
        assertNotNull(detalle.getId());
        assertTrue(detalle.getActivo());
        assertEquals(despachoKardexFrm.getRegistro(), detalle.getIdKardex());
    }

    @Test
    void btnRecibirHandler_DebeGuardarDetalleKardexConKardexAsociado() {
        // Given
        despachoKardexFrm.prepararKardex(ventaDetalleTest);
        despachoKardexFrm.getRegistro().setIdAlmacen(almacenTest);

        ArgumentCaptor<KardexDetalle> detalleCaptor = ArgumentCaptor.forClass(KardexDetalle.class);

        // When
        despachoKardexFrm.btnRecibirHandler(actionEvent);

        // Then
        verify(kardexDetalleDAO).crear(detalleCaptor.capture());
        KardexDetalle detalleGuardado = detalleCaptor.getValue();
        assertNotNull(detalleGuardado);
        assertNotNull(detalleGuardado.getIdKardex());
    }

    @Test
    void prepararKardex_DebeLimpiarReferenciaExternaYObservaciones() {
        // When
        despachoKardexFrm.prepararKardex(ventaDetalleTest);

        // Then
        Kardex kardex = despachoKardexFrm.getRegistro();
        assertNull(kardex.getReferenciaExterna());
        assertNull(kardex.getIdAlmacen());
        assertNull(kardex.getObservaciones());
    }


}