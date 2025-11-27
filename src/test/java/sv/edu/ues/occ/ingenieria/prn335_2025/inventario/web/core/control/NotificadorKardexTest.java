package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.jms.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificadorKardexTest {

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private Queue queue;

    @Mock
    private Connection connection;

    @Mock
    private Session session;

    @Mock
    private MessageProducer producer;

    @Mock
    private TextMessage textMessage;

    @InjectMocks
    private NotificadorKardex notificadorKardex;

    @BeforeEach
    void setUp() throws Exception {
        // Inyectar manualmente los recursos @Resource usando reflexión
        Field connectionFactoryField = NotificadorKardex.class.getDeclaredField("connectionFactory");
        connectionFactoryField.setAccessible(true);
        connectionFactoryField.set(notificadorKardex, connectionFactory);

        Field queueField = NotificadorKardex.class.getDeclaredField("queue");
        queueField.setAccessible(true);
        queueField.set(notificadorKardex, queue);
    }

    @Test
    void notificarCambio_DebeEnviarMensajeCorrectamente() throws JMSException {
        // Given
        String mensajeEsperado = "Cambio en kardex: producto X";

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createProducer(queue)).thenReturn(producer);
        when(session.createTextMessage(mensajeEsperado)).thenReturn(textMessage);

        // When
        notificadorKardex.notificarCambio(mensajeEsperado);

        // Then
        verify(connectionFactory).createConnection();
        verify(connection).createSession(false, Session.AUTO_ACKNOWLEDGE);
        verify(session).createProducer(queue);
        verify(session).createTextMessage(mensajeEsperado);
        verify(producer).send(textMessage);
        verify(session).close();
        verify(connection).close();
    }

    @Test
    void notificarCambio_CuandoConnectionFactoryRetornaNulo_NoDebeEnviarMensaje() throws JMSException {
        // Given
        when(connectionFactory.createConnection()).thenReturn(null);

        // When
        notificadorKardex.notificarCambio("mensaje");

        // Then
        verify(connectionFactory).createConnection();
        verify(connection, never()).createSession(anyBoolean(), anyInt());
        verify(session, never()).createProducer(any());
        verify(producer, never()).send(any());
    }

    @Test
    void notificarCambio_CuandoSessionEsNula_NoDebeEnviarMensaje() throws JMSException {
        // Given
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(null);

        // When
        notificadorKardex.notificarCambio("mensaje");

        // Then
        verify(connectionFactory).createConnection();
        verify(connection).createSession(false, Session.AUTO_ACKNOWLEDGE);
        verify(session, never()).createProducer(any());
        verify(producer, never()).send(any());
        verify(connection).close();
    }

    @Test
    void notificarCambio_CuandoOcurreJMSException_DebeManejarExcepcion() throws JMSException {
        // Given
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenThrow(new JMSException("Error de prueba"));

        // When
        assertDoesNotThrow(() -> notificadorKardex.notificarCambio("mensaje"));

        // Then
        verify(connectionFactory).createConnection();
        verify(connection).createSession(false, Session.AUTO_ACKNOWLEDGE);
        verify(connection).close();
    }

    @Test
    void notificarCambio_CuandoFallaAlCerrarSession_DebeCerrarConnection() throws JMSException {
        // Given
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createProducer(queue)).thenReturn(producer);
        when(session.createTextMessage(anyString())).thenReturn(textMessage);
        doThrow(new JMSException("Error al cerrar sesión")).when(session).close();

        // When
        notificadorKardex.notificarCambio("mensaje");

        // Then
        verify(session).close();
        verify(connection).close(); // Debe cerrar la conexión aunque falle la sesión
    }

    @Test
    void notificarCambio_CuandoFallaAlCerrarConnection_DebeManejarExcepcion() throws JMSException {
        // Given
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createProducer(queue)).thenReturn(producer);
        when(session.createTextMessage(anyString())).thenReturn(textMessage);
        doThrow(new JMSException("Error al cerrar conexión")).when(connection).close();

        // When
        assertDoesNotThrow(() -> notificadorKardex.notificarCambio("mensaje"));

        // Then
        verify(connection).close();
    }

    @Test
    void notificarCambio_DebeEnviarMensajeConTextoEspecifico() throws JMSException {
        // Given
        String mensajeEspecifico = "Actualización: Stock reducido a 10 unidades";
        ArgumentCaptor<String> mensajeCaptor = ArgumentCaptor.forClass(String.class);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createProducer(queue)).thenReturn(producer);
        when(session.createTextMessage(anyString())).thenReturn(textMessage);

        // When
        notificadorKardex.notificarCambio(mensajeEspecifico);

        // Then
        verify(session).createTextMessage(mensajeCaptor.capture());
        assertEquals(mensajeEspecifico, mensajeCaptor.getValue());
    }

    @Test
    void notificarCambio_ConMensajeVacio_DebeEnviarMensajeVacio() throws JMSException {
        // Given
        String mensajeVacio = "";

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createProducer(queue)).thenReturn(producer);
        when(session.createTextMessage(mensajeVacio)).thenReturn(textMessage);

        // When
        notificadorKardex.notificarCambio(mensajeVacio);

        // Then
        verify(session).createTextMessage(mensajeVacio);
        verify(producer).send(textMessage);
    }

    @Test
    void notificarCambio_DebeUsarAutoAcknowledge() throws JMSException {
        // Given
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);
        when(session.createProducer(queue)).thenReturn(producer);
        when(session.createTextMessage(anyString())).thenReturn(textMessage);

        // When
        notificadorKardex.notificarCambio("mensaje");

        // Then
        verify(connection).createSession(false, Session.AUTO_ACKNOWLEDGE);
    }
}