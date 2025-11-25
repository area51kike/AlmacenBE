package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                propertyValue = "jms/JmsFactory"  // ← CORREGIDO: usa el mismo que en NotificadorKardex
        )
})
public class ReceptorKardex implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(ReceptorKardex.class.getName());

    @Inject
    Event<KardexEvent> kardexEvent;  // ← Inyectar evento CDI

    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            try {
                String texto = ((TextMessage) message).getText();
                LOGGER.log(Level.INFO, "=== Mensaje JMS recibido en ReceptorKardex ===");
                LOGGER.log(Level.INFO, "Contenido: {0}", texto);

                // Procesar la lógica de negocio
                procesarKardex(texto);

                // Disparar evento CDI para notificar a WebSocket
                if (kardexEvent != null) {
                    kardexEvent.fire(new KardexEvent(texto));
                    LOGGER.log(Level.INFO, "=== Evento CDI disparado ===");
                } else {
                    LOGGER.log(Level.WARNING, "kardexEvent es null, no se puede disparar");
                }

            } catch (JMSException e) {
                LOGGER.log(Level.SEVERE, "Error al procesar mensaje JMS", e);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error inesperado", e);
            }
        } else {
            LOGGER.log(Level.WARNING, "Mensaje recibido no es de tipo TextMessage");
        }
    }

    private void procesarKardex(String datos) {
        LOGGER.log(Level.INFO, "Procesando kardex con datos: {0}", datos);

        // Aquí va tu lógica de negocio:
        // - Actualizar reportes
        // - Recalcular inventarios
        // - Generar alertas
        // - Guardar en BD
    }
}