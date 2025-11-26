package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ClienteDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Cliente;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteConverterTest {

    @InjectMocks
    ClienteConverter cut; // Component Under Test

    @Mock
    ClienteDAO clienteDao;

    @Mock
    FacesContext context;

    @Mock
    UIComponent component;


    private final PrintStream originalOut = System.out;
    private final PrintStream silentOut = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {

        }
    });

    @BeforeEach
    void setUp() throws Exception {
        System.setOut(silentOut);
        Field daoField = ClienteConverter.class.getDeclaredField("clienteDao");
        daoField.setAccessible(true);
        daoField.set(cut, clienteDao);
    }

    @AfterEach
    void tearDown() {
        // Restaurar la salida estándar
        System.setOut(originalOut);
    }

    @Test
    void testGetAsObject_NullOrBlank() {
        assertNull(cut.getAsObject(context, component, null));
        assertNull(cut.getAsObject(context, component, ""));
        assertNull(cut.getAsObject(context, component, "   "));
    }

    @Test
    void testGetAsObject_IllegalArgumentException() {
        assertNull(cut.getAsObject(context, component, "uuid-invalido"));
        verify(clienteDao, never()).findById(any());
    }

    @Test
    void testGetAsObject_Success() {
        // Branch 5: Flujo exitoso
        UUID uuid = UUID.randomUUID();
        Cliente mockCliente = new Cliente();
        mockCliente.setId(uuid);

        when(clienteDao.findById(uuid)).thenReturn(mockCliente);

        Cliente result = cut.getAsObject(context, component, uuid.toString());

        assertNotNull(result);
        assertEquals(mockCliente, result);
        verify(clienteDao).findById(uuid);
    }


    @Test
    void testGetAsString_NullValue() {

        String result = cut.getAsString(context, component, null);
        assertEquals("", result);
    }

    @Test
    void testGetAsString_NullId() {

        Cliente clienteSinId = new Cliente();
        clienteSinId.setId(null);

        String result = cut.getAsString(context, component, clienteSinId);
        assertEquals("", result);
    }

    @Test
    void testGetAsString_Success() {
        // Branch 3: Flujo exitoso
        UUID uuid = UUID.randomUUID();
        Cliente cliente = new Cliente();
        cliente.setId(uuid);

        String result = cut.getAsString(context, component, cliente);

        assertNotNull(result);
        assertEquals(uuid.toString(), result);
    }
}