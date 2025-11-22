package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.context.FacesContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ClienteDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.model.LazyDataModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteFrmTest {

    @Mock
    FacesContext facesContext;

    @Mock
    ClienteDAO clienteDAO;

    // Mockeamos el modelo de Primefaces que está en la clase padre
    @Mock
    LazyDataModel<Cliente> mockModelo;

    @InjectMocks
    ClienteFrm cut;

    private Cliente mockCliente;
    private UUID uuidPrueba;

    @BeforeEach
    void setUp() {
        uuidPrueba = UUID.randomUUID();

        mockCliente = new Cliente();
        mockCliente.setId(uuidPrueba);
        mockCliente.setNombre("Juan Perez");

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
            System.err.println("Aviso: No se pudo inyectar mockModelo por reflexión: " + e.getMessage());
        }
    }

    @Test
    void testInit() {
        assertEquals("Cliente", cut.nombreBean);
        assertNotNull(cut.getFacesContext());
        assertNotNull(cut.getDao());
    }

    @Test
    void testNuevoRegistro() {
        Cliente nuevo = cut.nuevoRegistro();
        assertNotNull(nuevo);
    }


    @Test
    void testEsNombreVacio() {
        assertTrue(cut.esNombreVacio(null), "Debe ser true si el registro es null");
        assertTrue(cut.esNombreVacio(new Cliente()), "Debe ser true si el nombre es null");

        Cliente c = new Cliente();
        c.setNombre("");
        assertTrue(cut.esNombreVacio(c), "Debe ser true si el nombre está vacío");

        c.setNombre("   ");
        assertTrue(cut.esNombreVacio(c), "Debe ser true si el nombre son espacios");

        c.setNombre("Juan");
        assertFalse(cut.esNombreVacio(c), "Debe ser false si tiene nombre");
    }

    @Test
    void testGetIdAsText() {
        assertNull(cut.getIdAsText(null));
        assertNull(cut.getIdAsText(new Cliente())); // ID null

        assertEquals(uuidPrueba.toString(), cut.getIdAsText(mockCliente));
    }

    @Test
    void testBuscarRegistroPorId_Exito() {
        List<Cliente> listaEnMemoria = new ArrayList<>();
        listaEnMemoria.add(mockCliente);
        when(mockModelo.getWrappedData()).thenReturn(listaEnMemoria);
        when(clienteDAO.findAll()).thenReturn(listaEnMemoria);
        Cliente resultado = cut.buscarRegistroPorId(uuidPrueba);

        assertNotNull(resultado);
        assertEquals(uuidPrueba, resultado.getId());
    }

    @Test
    void testBuscarRegistroPorId_NoEncontrado_EnLista() {
        List<Cliente> listaModelo = new ArrayList<>();
        listaModelo.add(new Cliente());
        when(mockModelo.getWrappedData()).thenReturn(listaModelo);
        Cliente otro = new Cliente();
        otro.setId(UUID.randomUUID());
        when(clienteDAO.findAll()).thenReturn(Collections.singletonList(otro));

        Cliente resultado = cut.buscarRegistroPorId(uuidPrueba);

        assertNull(resultado);
    }

    @Test
    void testBuscarRegistroPorId_ModeloVacio() {
        when(mockModelo.getWrappedData()).thenReturn(Collections.emptyList());
        Cliente resultado = cut.buscarRegistroPorId(uuidPrueba);
        assertNull(resultado);
        verify(clienteDAO, never()).findAll();
    }

    @Test
    void testBuscarRegistroPorId_IdInvalido() {
        // Caso 1: Null
        assertNull(cut.buscarRegistroPorId(null));

        // Caso 2: No es UUID (es un String o Integer)
        assertNull(cut.buscarRegistroPorId("texto-no-uuid"));
        assertNull(cut.buscarRegistroPorId(123));
    }

    @Test
    void testGetIdByText_Exito() {
        // Simulamos que la tabla tiene datos cargados (wrappedData)
        List<Cliente> listaTabla = new ArrayList<>();
        listaTabla.add(mockCliente); // Este tiene el UUID que buscamos

        Cliente otro = new Cliente();
        otro.setId(UUID.randomUUID());
        listaTabla.add(otro);

        when(mockModelo.getWrappedData()).thenReturn(listaTabla);

        // Ejecutar
        Cliente resultado = cut.getIdByText(uuidPrueba.toString());

        assertNotNull(resultado);
        assertEquals(uuidPrueba, resultado.getId());
    }

    @Test
    void testGetIdByText_NoEncontrado_EnStream() {
        List<Cliente> listaTabla = new ArrayList<>();
        Cliente otro = new Cliente();
        otro.setId(UUID.randomUUID());
        listaTabla.add(otro);

        when(mockModelo.getWrappedData()).thenReturn(listaTabla);
        Cliente resultado = cut.getIdByText(uuidPrueba.toString());

        assertNull(resultado);
    }

    @Test
    void testGetIdByText_FormatoInvalido() {
        // Simulamos que el modelo tiene datos para entrar al try
        List<Cliente> listaTabla = new ArrayList<>();
        listaTabla.add(mockCliente);
        when(mockModelo.getWrappedData()).thenReturn(listaTabla);
        Cliente resultado = cut.getIdByText("esto-no-es-uuid");
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
            e.printStackTrace();
        }
    }
}