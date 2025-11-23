package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.context.FacesContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.model.LazyDataModel;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoFrmTest {

    @Mock
    FacesContext facesContext;

    @Mock
    ProductoDAO productoDAO;

    @Mock
    LazyDataModel<Producto> mockModelo;

    @InjectMocks
    ProductoFrm cut;

    private Producto mockProducto;
    private UUID uuidPrueba;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        }));

        uuidPrueba = UUID.randomUUID();
        mockProducto = new Producto();
        mockProducto.setId(uuidPrueba);
        mockProducto.setNombreProducto("Producto Test");

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
        System.setErr(originalErr);
    }

    @Test
    void testInit() {
        assertEquals("Productos", cut.nombreBean);
        assertNotNull(cut.getFacesContext());
        assertNotNull(cut.getDao());
    }

    @Test
    void testNuevoRegistro() {
        Producto nuevo = cut.nuevoRegistro();
        assertNotNull(nuevo);
    }

    @Test
    void testEsNombreVacio() {
        assertTrue(cut.esNombreVacio(null));
        assertTrue(cut.esNombreVacio(new Producto()));

        Producto p = new Producto();
        p.setNombreProducto("");
        assertTrue(cut.esNombreVacio(p));

        p.setNombreProducto("   ");
        assertTrue(cut.esNombreVacio(p));

        p.setNombreProducto("Coca Cola");
        assertFalse(cut.esNombreVacio(p));
    }

    @Test
    void testGetIdAsText() {
        assertNull(cut.getIdAsText(null));
        assertNull(cut.getIdAsText(new Producto()));
        assertEquals(uuidPrueba.toString(), cut.getIdAsText(mockProducto));
    }

    @Test
    void testBuscarRegistroPorId_Exito() {
        List<Producto> listaEnMemoria = new ArrayList<>();
        listaEnMemoria.add(mockProducto);
        when(mockModelo.getWrappedData()).thenReturn(listaEnMemoria);

        when(productoDAO.findAll()).thenReturn(listaEnMemoria);

        Producto resultado = cut.buscarRegistroPorId(uuidPrueba);

        assertNotNull(resultado);
        assertEquals(uuidPrueba, resultado.getId());
    }

    @Test
    void testBuscarRegistroPorId_NoEncontrado_EnLista() {
        List<Producto> listaModelo = new ArrayList<>();
        listaModelo.add(new Producto());
        when(mockModelo.getWrappedData()).thenReturn(listaModelo);

        Producto otro = new Producto();
        otro.setId(UUID.randomUUID());
        when(productoDAO.findAll()).thenReturn(Collections.singletonList(otro));

        Producto resultado = cut.buscarRegistroPorId(uuidPrueba);

        assertNull(resultado);
    }

    @Test
    void testBuscarRegistroPorId_ModeloVacio() {
        when(mockModelo.getWrappedData()).thenReturn(Collections.emptyList());

        Producto resultado = cut.buscarRegistroPorId(uuidPrueba);

        assertNull(resultado);
        verify(productoDAO, never()).findAll();
    }

    @Test
    void testBuscarRegistroPorId_IdInvalido() {
        assertNull(cut.buscarRegistroPorId(null));
        assertNull(cut.buscarRegistroPorId("texto-no-uuid"));
        assertNull(cut.buscarRegistroPorId(123));
    }

    @Test
    void testGetIdByText_Exito() {
        List<Producto> listaTabla = new ArrayList<>();
        listaTabla.add(mockProducto);

        Producto otro = new Producto();
        otro.setId(UUID.randomUUID());
        listaTabla.add(otro);

        when(mockModelo.getWrappedData()).thenReturn(listaTabla);

        Producto resultado = cut.getIdByText(uuidPrueba.toString());

        assertNotNull(resultado);
        assertEquals(uuidPrueba, resultado.getId());
    }

    @Test
    void testGetIdByText_NoEncontrado_EnStream() {
        List<Producto> listaTabla = new ArrayList<>();
        Producto otro = new Producto();
        otro.setId(UUID.randomUUID());
        listaTabla.add(otro);

        when(mockModelo.getWrappedData()).thenReturn(listaTabla);

        Producto resultado = cut.getIdByText(uuidPrueba.toString());

        assertNull(resultado);
    }

    @Test
    void testGetIdByText_FormatoInvalido() {
        List<Producto> listaTabla = new ArrayList<>();
        listaTabla.add(mockProducto);
        when(mockModelo.getWrappedData()).thenReturn(listaTabla);

        Producto resultado = cut.getIdByText("esto-no-es-uuid");

        assertNull(resultado);
    }

    @Test
    void testGetIdByText_CondicionesIniciales() {
        assertNull(cut.getIdByText(null));

        when(mockModelo.getWrappedData()).thenReturn(Collections.emptyList());
        assertNull(cut.getIdByText(uuidPrueba.toString()));

        try {
            Class<?> currentClass = cut.getClass();
            Field modeloField = null;
            while (currentClass != null) {
                try { modeloField = currentClass.getDeclaredField("modelo"); break; }
                catch (NoSuchFieldException e) { currentClass = currentClass.getSuperclass(); }
            }
            if (modeloField != null) {
                modeloField.setAccessible(true);
                modeloField.set(cut, null);
            }

            assertNull(cut.getIdByText(uuidPrueba.toString()));

        } catch (Exception e) {
        }
    }
}