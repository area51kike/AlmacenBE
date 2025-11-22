package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.annotation.Resource;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.jms.*;

import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@LocalBean
public class NotificadorKardex {

    @Resource(lookup = "jms/JmsFactory")
    ConnectionFactory connectionFactory;

    @Resource(lookup = "jms/JmsQueue")
    Queue queue;

    public void notificarCambioKardex(String mensaje) {
        Connection cnx = null;
        Session session = null;
        MessageProducer producer = null;

        try {
            cnx = connectionFactory.createConnection();
            session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(queue);

            TextMessage textMessage = session.createTextMessage(mensaje + " - " + System.currentTimeMillis());
            producer.send(textMessage);

            System.out.println("Mensaje enviado: " + textMessage.getText());

        } catch (Exception e) {
            Logger.getLogger(NotificadorKardex.class.getName()).log(Level.SEVERE, "Error al enviar mensaje", e);
        } finally {
            // Cerrar recursos en orden inverso
            try {
                if (producer != null) producer.close();
                if (session != null) session.close();
                if (cnx != null) cnx.close();
            } catch (JMSException e) {
                Logger.getLogger(NotificadorKardex.class.getName()).log(Level.WARNING, "Error al cerrar recursos JMS", e);
            }
        }
    }
}