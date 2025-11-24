package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Cliente;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteDAOTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ClienteDAO clienteDAO;

    private Cliente cliente;
    private UUID testUUID;

    @BeforeEach
    void setUp() {
        clienteDAO = new ClienteDAO();
        clienteDAO.em = entityManager;

        testUUID = UUID.randomUUID();
        cliente = new Cliente();
        cliente.setId(testUUID);
        cliente.setNombre("Juan Pérez");
        cliente.setDui("012345678");
        cliente.setNit("0614-123456-101-2");
        cliente.setActivo(true);
    }

    @Test
    void testGetEntityManager() {
        EntityManager result = clienteDAO.getEntityManager();
        assertNotNull(result);
        assertEquals(entityManager, result);
    }

    @Test
    void testConstructor() {
        ClienteDAO dao = new ClienteDAO();
        assertNotNull(dao);
    }

    @Test
    void testConstructorWithParameter() {
        ClienteDAO dao = new ClienteDAO();
        assertNotNull(dao);
    }

    @Test
    void testFindById_Success() {
        when(entityManager.find(Cliente.class, testUUID)).thenReturn(cliente);

        Cliente result = clienteDAO.findById(testUUID);

        assertNotNull(result);
        assertEquals(testUUID, result.getId());
        assertEquals("Juan Pérez", result.getNombre());
        assertEquals("012345678", result.getDui());
        assertEquals("0614-123456-101-2", result.getNit());
        assertTrue(result.getActivo());
        verify(entityManager).find(Cliente.class, testUUID);
    }

    @Test
    void testFindById_NotFound() {
        UUID id = UUID.randomUUID();
        when(entityManager.find(Cliente.class, id)).thenReturn(null);

        Cliente result = clienteDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Cliente.class, id);
    }


    @Test
    void testFindById_Exception() {
        UUID id = UUID.randomUUID();
        when(entityManager.find(Cliente.class, id))
                .thenThrow(new RuntimeException("Database connection error"));

        assertThrows(RuntimeException.class, () -> clienteDAO.findById(id));
        verify(entityManager).find(Cliente.class, id);
    }

    @Test
    void testFindById_InactiveCliente() {
        cliente.setActivo(false);
        when(entityManager.find(Cliente.class, testUUID)).thenReturn(cliente);

        Cliente result = clienteDAO.findById(testUUID);

        assertNotNull(result);
        assertFalse(result.getActivo());
        verify(entityManager).find(Cliente.class, testUUID);
    }

    @Test
    void testFindById_WithNullDui() {
        cliente.setDui(null);
        when(entityManager.find(Cliente.class, testUUID)).thenReturn(cliente);

        Cliente result = clienteDAO.findById(testUUID);

        assertNotNull(result);
        assertNull(result.getDui());
        verify(entityManager).find(Cliente.class, testUUID);
    }

    @Test
    void testFindById_WithNullNit() {
        cliente.setNit(null);
        when(entityManager.find(Cliente.class, testUUID)).thenReturn(cliente);

        Cliente result = clienteDAO.findById(testUUID);

        assertNotNull(result);
        assertNull(result.getNit());
        verify(entityManager).find(Cliente.class, testUUID);
    }

    @Test
    void testFindById_WithEmptyNombre() {
        cliente.setNombre("");
        when(entityManager.find(Cliente.class, testUUID)).thenReturn(cliente);

        Cliente result = clienteDAO.findById(testUUID);

        assertNotNull(result);
        assertEquals("", result.getNombre());
        verify(entityManager).find(Cliente.class, testUUID);
    }

    @Test
    void testFindById_WithNullNombre() {
        cliente.setNombre(null);
        when(entityManager.find(Cliente.class, testUUID)).thenReturn(cliente);

        Cliente result = clienteDAO.findById(testUUID);

        assertNotNull(result);
        assertNull(result.getNombre());
        verify(entityManager).find(Cliente.class, testUUID);
    }

    @Test
    void testFindById_WithMaxLengthNombre() {
        String maxNombre = "A".repeat(155);
        cliente.setNombre(maxNombre);
        when(entityManager.find(Cliente.class, testUUID)).thenReturn(cliente);

        Cliente result = clienteDAO.findById(testUUID);

        assertNotNull(result);
        assertEquals(155, result.getNombre().length());
        verify(entityManager).find(Cliente.class, testUUID);
    }

    @Test
    void testFindById_WithMaxLengthDui() {
        String maxDui = "123456789";
        cliente.setDui(maxDui);
        when(entityManager.find(Cliente.class, testUUID)).thenReturn(cliente);

        Cliente result = clienteDAO.findById(testUUID);

        assertNotNull(result);
        assertEquals(9, result.getDui().length());
        verify(entityManager).find(Cliente.class, testUUID);
    }

    @Test
    void testFindById_WithMaxLengthNit() {
        String maxNit = "01234567890123";
        cliente.setNit(maxNit);
        when(entityManager.find(Cliente.class, testUUID)).thenReturn(cliente);

        Cliente result = clienteDAO.findById(testUUID);

        assertNotNull(result);
        assertEquals(14, result.getNit().length());
        verify(entityManager).find(Cliente.class, testUUID);
    }

    @Test
    void testFindById_MultipleCalls() {
        when(entityManager.find(Cliente.class, testUUID)).thenReturn(cliente);

        Cliente result1 = clienteDAO.findById(testUUID);
        Cliente result2 = clienteDAO.findById(testUUID);

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getId(), result2.getId());
        verify(entityManager, times(2)).find(Cliente.class, testUUID);
    }

    @Test
    void testFindById_DifferentUUIDs() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Cliente cliente1 = new Cliente();
        cliente1.setId(id1);
        cliente1.setNombre("Cliente 1");

        Cliente cliente2 = new Cliente();
        cliente2.setId(id2);
        cliente2.setNombre("Cliente 2");

        when(entityManager.find(Cliente.class, id1)).thenReturn(cliente1);
        when(entityManager.find(Cliente.class, id2)).thenReturn(cliente2);

        Cliente result1 = clienteDAO.findById(id1);
        Cliente result2 = clienteDAO.findById(id2);

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1.getId(), result2.getId());
        assertEquals("Cliente 1", result1.getNombre());
        assertEquals("Cliente 2", result2.getNombre());
    }






    @Test
    void testFindById_WithNullActivo() {
        cliente.setActivo(null);
        when(entityManager.find(Cliente.class, testUUID)).thenReturn(cliente);

        Cliente result = clienteDAO.findById(testUUID);

        assertNotNull(result);
        assertNull(result.getActivo());
        verify(entityManager).find(Cliente.class, testUUID);
    }

    @Test
    void testFindById_WithSpecialCharactersInNombre() {
        cliente.setNombre("José María O'Connor");
        when(entityManager.find(Cliente.class, testUUID)).thenReturn(cliente);

        Cliente result = clienteDAO.findById(testUUID);

        assertNotNull(result);
        assertEquals("José María O'Connor", result.getNombre());
        verify(entityManager).find(Cliente.class, testUUID);
    }
}