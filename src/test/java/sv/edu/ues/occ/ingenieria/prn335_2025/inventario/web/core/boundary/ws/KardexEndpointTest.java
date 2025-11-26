package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.ws;

import jakarta.websocket.CloseReason;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.KardexEvent;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// LENIENT evita el error "UnnecessaryStubbingException" y permite tests más flexibles
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KardexEndpointTest {

    @InjectMocks
    KardexEndpoint cut;

    @Mock
    Session mockSession1;
    @Mock
    Session mockSession2;
    @Mock
    RemoteEndpoint.Basic mockRemote1;
    @Mock
    RemoteEndpoint.Basic mockRemote2;
    @Mock
    CloseReason mockCloseReason;
    @Mock
    KardexEvent mockEvent;

    // Variable para guardar el nivel original del logger y restaurarlo después
    private Level originalLogLevel;

    @BeforeEach
    void setUp() {
        // --- SILENCIAR LOGS ---
        // Obtenemos el logger de la clase que estamos probando
        Logger appLogger = Logger.getLogger(KardexEndpoint.class.getName());
        // Guardamos el nivel actual para no afectar otros tests
        originalLogLevel = appLogger.getLevel();
        // Apagamos el logger para que no imprima errores "GRAVE" simulados en la consola
        appLogger.setLevel(Level.OFF);

        // --- CONFIGURACIÓN DE MOCKS ---
        // 1. Configurar IDs (para lógica interna)
        when(mockSession1.getId()).thenReturn("S1");
        when(mockSession2.getId()).thenReturn("S2");

        // 2. Configurar RemoteEndpoint (para enviar mensajes)
        when(mockSession1.getBasicRemote()).thenReturn(mockRemote1);
        when(mockSession2.getBasicRemote()).thenReturn(mockRemote2);

        // 3. IMPORTANTE: Simular que las sesiones están ABIERTAS por defecto.
        when(mockSession1.isOpen()).thenReturn(true);
        when(mockSession2.isOpen()).thenReturn(true);

        // 4. Limpiar el estado estático
        cut.getSessions().clear();
    }

    @AfterEach
    void tearDown() {
        // --- RESTAURAR LOGS ---
        // Es buena práctica restaurar el nivel del logger al finalizar
        Logger appLogger = Logger.getLogger(KardexEndpoint.class.getName());
        appLogger.setLevel(originalLogLevel);
    }

    // --- Ciclo de Vida ---

    @Test
    void testOnOpen() throws IOException {
        // Act
        cut.onOpen(mockSession1);

        // Assert
        assertTrue(cut.getSessions().contains(mockSession1));
        assertEquals(1, cut.getSessions().size());
        // Verifica el mensaje de bienvenida
        verify(mockRemote1).sendText(contains("Conectado"));
    }

    @Test
    void testOnClose() {
        // Arrange
        cut.getSessions().add(mockSession1);

        // Act
        cut.onClose(mockSession1, mockCloseReason);

        // Assert
        assertTrue(cut.getSessions().isEmpty());
    }

    @Test
    void testOnError() {
        // Arrange
        cut.getSessions().add(mockSession1);
        Throwable error = new RuntimeException("Test Error");

        // Act
        cut.onError(mockSession1, error);

        // Assert
        assertFalse(cut.getSessions().contains(mockSession1));
    }

    @Test
    void testOnMessage() throws IOException {
        // Act
        cut.onMessage("Hola", mockSession1);

        // Assert
        verify(mockRemote1).sendText("Servidor recibió: Hola");
    }

    // --- Envío de Mensajes ---

    @Test
    void testEnviarMensaje_Exito() throws IOException {
        // Act
        cut.enviarMensaje(mockSession1, "Test");

        // Assert
        verify(mockRemote1).sendText("Test");
    }

    @Test
    void testEnviarMensaje_SesionCerrada() throws IOException {
        // Arrange: Sobrescribimos para simular cierre
        when(mockSession1.isOpen()).thenReturn(false);

        // Act
        cut.enviarMensaje(mockSession1, "No debe enviarse");

        // Assert
        verify(mockRemote1, never()).sendText(anyString());
    }

    @Test
    void testEnviarMensaje_IOException() throws IOException {
        // Arrange
        // Simulamos que el método sendText lanza una excepción
        doThrow(new IOException("Fallo de red")).when(mockRemote1).sendText(anyString());

        // Act
        cut.enviarMensaje(mockSession1, "Mensaje que falla");

        // Assert
        // Verificamos que se INTENTÓ llamar (aunque falló internamente y se logueó silenciosamente)
        verify(mockRemote1).sendText("Mensaje que falla");
    }

    // --- Broadcast ---

    @Test
    void testBroadcast_ExitoATodos() throws IOException {
        // Arrange
        cut.getSessions().add(mockSession1);
        cut.getSessions().add(mockSession2);

        // Act
        cut.broadcast("Hola a todos");

        // Assert
        verify(mockRemote1).sendText("Hola a todos");
        verify(mockRemote2).sendText("Hola a todos");
    }

    @Test
    void testBroadcast_Parcial() throws IOException {
        // Arrange
        cut.getSessions().add(mockSession1);
        cut.getSessions().add(mockSession2);
        // Simulamos que la sesión 2 se cerró inesperadamente
        when(mockSession2.isOpen()).thenReturn(false);

        // Act
        cut.broadcast("Hola");

        // Assert
        verify(mockRemote1).sendText("Hola"); // S1 recibe
        verify(mockRemote2, never()).sendText(anyString()); // S2 no recibe
    }

    @Test
    void testBroadcast_FalloDeEnvio() throws IOException {
        // Arrange
        cut.getSessions().add(mockSession1);
        cut.getSessions().add(mockSession2);
        // Simulamos error de red solo en sesión 1
        doThrow(new IOException("Error S1")).when(mockRemote1).sendText(anyString());

        // Act
        cut.broadcast("Aviso");

        // Assert
        verify(mockRemote1).sendText("Aviso"); // Intentó enviar (y falló)
        verify(mockRemote2).sendText("Aviso"); // Intentó y funcionó
    }

    // --- Eventos CDI ---

    @Test
    void testOnKardexEvent() throws IOException {
        // Arrange
        cut.getSessions().add(mockSession1);
        when(mockEvent.getMensaje()).thenReturn("Nuevo Producto");

        // Act
        cut.onKardexEvent(mockEvent);

        // Assert
        verify(mockRemote1).sendText("Nuevo Producto");
    }
}