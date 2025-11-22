// ReceptorKardex.java
package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;

import java.util.logging.Level;
import java.util.logging.Logger;

@MessageDriven(activationConfig = {
        @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
        @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/JmsQueue"),
        @jakarta.ejb.ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
})
public class ReceptorKardex implements MessageListener {

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String contenido = textMessage.getText();
                System.out.println("Mensaje recibido en ReceptorKardex: " + contenido);
                // Lógica para procesar el mensaje recibido de la cola JMS
            }
        } catch (Exception e) {
            Logger.getLogger(ReceptorKardex.class.getName()).log(Level.SEVERE, "Error al procesar mensaje", e);
        }
    }
}