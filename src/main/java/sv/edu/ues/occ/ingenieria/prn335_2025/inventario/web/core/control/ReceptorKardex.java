package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.ws.KardexEndpoint;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(
                propertyName = "destinationLookup",
                propertyValue = "jms/JmsQueue"
        ),
        @ActivationConfigProperty(
                propertyName = "destinationType",
                propertyValue = "jakarta.jms.Queue"
        ),
        @ActivationConfigProperty(
                propertyName = "connectionFactoryLookup",
                propertyValue = "jms/QueueConnectionFactory"
        )
})
public class ReceptorKardex implements MessageListener {

    @Inject  // ← Inyectar el WebSocket endpoint
    KardexEndpoint kardexEndpoint;

    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            try {
                String texto = ((TextMessage) message).getText();
                System.out.println("=== Mensaje JMS recibido en ReceptorKardex ===");
                System.out.println("Contenido: " + texto);

                // Procesar la lógica de negocio
                procesarKardex(texto);

                // Notificar a TODOS los clientes WebSocket conectados
                try {
                    kardexEndpoint.broadcast(texto);
                    System.out.println("=== Broadcast enviado a clientes WebSocket ===");
                } catch (Exception e) {
                    System.err.println("Error al enviar broadcast a WebSocket: " + e.getMessage());
                    e.printStackTrace();
                }

            } catch (JMSException e) {
                System.err.println("Error al procesar mensaje JMS: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Mensaje recibido no es de tipo TextMessage");
        }
    }

    private void procesarKardex(String datos) {
        // Aquí va tu lógica de negocio
        System.out.println("Procesando kardex con datos: " + datos);

        // Ejemplos de lo que podrías hacer:
        // - Actualizar reportes
        // - Recalcular inventarios
        // - Generar alertas
        // - Guardar en BD
        // etc.
    }
}