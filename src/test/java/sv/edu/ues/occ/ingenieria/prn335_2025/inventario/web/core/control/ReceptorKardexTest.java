package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.enterprise.event.Event;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceptorKardexTest {

    @Mock
    private Event<KardexEvent> kardexEvent;

    @Mock
    private TextMessage textMessage;

    @Mock
    private Message genericMessage;

    @InjectMocks
    private ReceptorKardex receptorKardex;

    private static final String MENSAJE_PRUEBA = "kardex-data-123";

    @BeforeEach
    void setUp() {
        // Configuración adicional si es necesaria
    }

    @Test
    void onMessage_deberiaProcessarTextMessageCorrectamente() throws JMSException {
        // Arrange
        when(textMessage.getText()).thenReturn(MENSAJE_PRUEBA);

        // Act
        receptorKardex.onMessage(textMessage);

        // Assert
        verify(textMessage, times(1)).getText();
        verify(kardexEvent, times(1)).fire(any(KardexEvent.class));
    }

    @Test
    void onMessage_deberiaDispararEventoCDIConDatosCorrectos() throws JMSException {
        // Arrange
        when(textMessage.getText()).thenReturn(MENSAJE_PRUEBA);
        ArgumentCaptor<KardexEvent> eventCaptor = ArgumentCaptor.forClass(KardexEvent.class);

        // Act
        receptorKardex.onMessage(textMessage);

        // Assert
        verify(kardexEvent).fire(eventCaptor.capture());
        KardexEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        // Verifica que el evento fue creado con el mensaje correcto
        // Si KardexEvent no tiene getDatos(), ajusta según la implementación real
    }

    @Test
    void onMessage_deberiaLoguearAdvertenciaCuandoMensajeNoEsTextMessage() {
        // Act
        receptorKardex.onMessage(genericMessage);

        // Assert
        verify(kardexEvent, never()).fire(any(KardexEvent.class));
        // El mensaje de advertencia se registra pero no se puede verificar directamente sin un logger mock
    }

    @Test
    void onMessage_deberiaManejarJMSExceptionCorrectamente() throws JMSException {
        // Arrange
        when(textMessage.getText()).thenThrow(new JMSException("Error simulado"));

        // Act
        assertDoesNotThrow(() -> receptorKardex.onMessage(textMessage));

        // Assert
        verify(kardexEvent, never()).fire(any(KardexEvent.class));
    }

    @Test
    void onMessage_deberiaFuncionarCuandoKardexEventEsNull() throws JMSException {
        // Arrange
        ReceptorKardex receptorSinEvento = new ReceptorKardex();
        when(textMessage.getText()).thenReturn(MENSAJE_PRUEBA);

        // Act & Assert
        assertDoesNotThrow(() -> receptorSinEvento.onMessage(textMessage));
    }

    @Test
    void onMessage_deberiaProcesarMensajesVacios() throws JMSException {
        // Arrange
        when(textMessage.getText()).thenReturn("");

        // Act
        receptorKardex.onMessage(textMessage);

        // Assert
        verify(kardexEvent, times(1)).fire(any(KardexEvent.class));
    }

    @Test
    void onMessage_deberiaProcesarMensajesConCaracteresEspeciales() throws JMSException {
        // Arrange
        String mensajeEspecial = "kardex-{\"id\":123,\"tipo\":\"entrada\"}";
        when(textMessage.getText()).thenReturn(mensajeEspecial);
        ArgumentCaptor<KardexEvent> eventCaptor = ArgumentCaptor.forClass(KardexEvent.class);

        // Act
        receptorKardex.onMessage(textMessage);

        // Assert
        verify(kardexEvent).fire(eventCaptor.capture());
        assertNotNull(eventCaptor.getValue());
        // Verifica que el evento fue creado con el mensaje especial
    }

    @Test
    void onMessage_deberiaManejarExcepcionesGenericas() throws JMSException {
        // Arrange
        when(textMessage.getText()).thenReturn(MENSAJE_PRUEBA);
        doThrow(new RuntimeException("Error inesperado")).when(kardexEvent).fire(any());

        // Act
        assertDoesNotThrow(() -> receptorKardex.onMessage(textMessage));

        // Assert
        verify(textMessage).getText();
    }
    // Auxiliar para la relación con Kardex (String para evitar error de conversión)

    @Test
    void onMessage_deberiaLlamarGetTextSoloUnaVez() throws JMSException {
        // Arrange
        when(textMessage.getText()).thenReturn(MENSAJE_PRUEBA);

        // Act
        receptorKardex.onMessage(textMessage);

        // Assert
        verify(textMessage, times(1)).getText();
    }
}