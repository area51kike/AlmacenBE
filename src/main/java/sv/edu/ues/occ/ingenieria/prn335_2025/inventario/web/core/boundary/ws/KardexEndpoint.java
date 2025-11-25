package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.ws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
@ServerEndpoint("/notificadorKardex")  // URL del WebSocket
public class KardexEndpoint {

    private static final Logger LOGGER = Logger.getLogger(KardexEndpoint.class.getName());

    // Set de sesiones activas (clientes conectados)
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        LOGGER.log(Level.INFO, "Cliente conectado: {0}", session.getId());
        LOGGER.log(Level.INFO, "Total de clientes: {0}", sessions.size());
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        sessions.remove(session);
        LOGGER.log(Level.INFO, "Cliente desconectado: {0}. Razón: {1}",
                new Object[]{session.getId(), reason});
        LOGGER.log(Level.INFO, "Total de clientes: {0}", sessions.size());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.log(Level.SEVERE, "Error en sesión " + session.getId(), throwable);
        sessions.remove(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.log(Level.INFO, "Mensaje recibido de {0}: {1}",
                new Object[]{session.getId(), message});
        // Opcional: responder al cliente
        enviarMensaje(session, "Servidor recibió: " + message);
    }

    // Método para enviar mensaje a un cliente específico
    public void enviarMensaje(Session session, String mensaje) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(mensaje);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error enviando mensaje", e);
            }
        }
    }

    // Método para broadcast (enviar a TODOS los clientes conectados)
    public void broadcast(String mensaje) {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(mensaje);
                        LOGGER.log(Level.INFO, "Mensaje enviado a: {0}", session.getId());
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Error enviando broadcast", e);
                    }
                }
            }
        }
        LOGGER.log(Level.INFO, "Broadcast enviado a {0} clientes", sessions.size());
    }

    // Getter para las sesiones (si lo necesitas)
    public Set<Session> getSessions() {
        return sessions;
    }
}