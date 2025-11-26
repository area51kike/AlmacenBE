package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.ws;

import jakarta.websocket.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SessionHandlerTest {

    @InjectMocks
    SessionHandler cut; // Component Under Test

    @Mock
    Session mockSession;

    @Test
    void testAddSession() {
        // Act
        cut.addSession(mockSession);

        // Assert
        Set<Session> sessions = cut.getSessions();
        assertNotNull(sessions, "La lista de sesiones no debería ser nula");
        assertEquals(1, sessions.size(), "Debería haber 1 sesión después de agregar");
        assertTrue(sessions.contains(mockSession), "La sesión agregada debería estar en el set");
    }

    @Test
    void testRemoveSession() {
        // Arrange
        cut.addSession(mockSession);
        assertEquals(1, cut.getSessions().size(), "Precondición: debe haber 1 sesión");

        // Act
        cut.removeSession(mockSession);

        // Assert
        Set<Session> sessions = cut.getSessions();
        assertNotNull(sessions);
        assertTrue(sessions.isEmpty(), "El set debería estar vacío después de eliminar");
        assertFalse(sessions.contains(mockSession));
    }

    @Test
    void testGetSessions_InicialmenteVacio() {
        // Act
        Set<Session> sessions = cut.getSessions();

        // Assert
        assertNotNull(sessions);
        assertTrue(sessions.isEmpty(), "El set debería inicializarse vacío");
    }
}