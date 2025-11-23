package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.context.FacesContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProveedorDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Proveedor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProveedorFrmTest {

    @Mock
    FacesContext facesContext;

    @Mock
    ProveedorDAO proveedorDao;

    @InjectMocks
    ProveedorFrm cut;

    private MockedStatic<FacesContext> mockedFacesContext;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private Proveedor mockProveedor;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));
        System.setErr(new PrintStream(new OutputStream() { @Override public void write(int b) {} }));

        mockedFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

        mockProveedor = new Proveedor();
        mockProveedor.setId(1);
        mockProveedor.setNombre("Proveedor Test");
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
    void testInit() {
        assertEquals("Proveedores", cut.nombreBean);
        assertNotNull(cut.getFacesContext());
        assertNotNull(cut.getDao());
    }

    @Test
    void testNuevoRegistro() {
        Proveedor nuevo = cut.nuevoRegistro();
        assertNotNull(nuevo);
    }

    @Test
    void testBuscarRegistroPorId() {
        assertNull(cut.buscarRegistroPorId(1));
        assertNull(cut.buscarRegistroPorId(null));
    }

    @Test
    void testGetIdAsText() {
        assertNull(cut.getIdAsText(null));
        assertNull(cut.getIdAsText(new Proveedor()));
        assertEquals("1", cut.getIdAsText(mockProveedor));
    }

    @Test
    void testGetIdByText_Exito() {
        List<Proveedor> lista = new ArrayList<>();
        lista.add(mockProveedor);
        when(proveedorDao.findAll()).thenReturn(lista);

        Proveedor resultado = cut.getIdByText("1");
        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
    }

    @Test
    void testGetIdByText_NoEncontrado() {
        List<Proveedor> lista = new ArrayList<>();
        lista.add(mockProveedor);
        when(proveedorDao.findAll()).thenReturn(lista);

        Proveedor resultado = cut.getIdByText("99");
        assertNull(resultado);
    }

    @Test
    void testGetIdByText_ListaVacia() {
        when(proveedorDao.findAll()).thenReturn(Collections.emptyList());

        Proveedor resultado = cut.getIdByText("1");
        assertNull(resultado);
    }

    @Test
    void testGetIdByText_FormatoInvalido() {
        Proveedor resultado = cut.getIdByText("no-es-numero");
        assertNull(resultado);
    }

    @Test
    void testGetIdByText_NullOrBlank() {
        assertNull(cut.getIdByText(null));
        assertNull(cut.getIdByText(""));
        assertNull(cut.getIdByText("   "));

        verify(proveedorDao, never()).findAll();
    }
}