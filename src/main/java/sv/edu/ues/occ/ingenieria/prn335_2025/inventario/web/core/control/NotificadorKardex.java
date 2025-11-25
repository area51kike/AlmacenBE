package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.jms.*;

import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class NotificadorKardex {

    private static final Logger LOGGER = Logger.getLogger(NotificadorKardex.class.getName());

    @Resource(lookup = "jms/JmsFactory")
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "jms/JmsQueue")
    private Queue queue;

    public void notificarCambio(String mensaje) {
        Connection cnx = null;
        Session session = null;

        try {
            // Crear conexión
            cnx = connectionFactory.createConnection();
            if (cnx == null) {
                LOGGER.log(Level.WARNING, "No se pudo crear la conexión JMS");
                return;
            }

            // Crear sesión
            session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
            if (session == null) {
                LOGGER.log(Level.WARNING, "No se pudo crear la sesión JMS");
                return;
            }

            // Crear productor
            MessageProducer producer = session.createProducer(queue);

            // Crear y enviar mensaje
            TextMessage textMessage = session.createTextMessage(mensaje);
            producer.send(textMessage);

            System.out.println("Mensaje enviado: " + mensaje);

        } catch (JMSException e) {
            LOGGER.log(Level.SEVERE, "Error al enviar mensaje JMS", e);
            e.printStackTrace();
        } finally {
            // Cerrar recursos en orden inverso
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    LOGGER.log(Level.SEVERE, "Error al cerrar sesión", e);
                    e.printStackTrace();
                }
            }

            if (cnx != null) {
                try {
                    cnx.close();
                } catch (JMSException e) {
                    LOGGER.log(Level.SEVERE, "Error al cerrar conexión", e);
                    e.printStackTrace();
                }
            }
        }
    }
}