package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.ws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.Serializable;

@ApplicationScoped
@ServerEndpoint("/compras")
public class KardexEndpoint implements Serializable {
    @Inject
    SessionHandler sessionHandler;
    @OnOpen
    public void AbrirConexion(Session session) {
        sessionHandler.addSession(session);
    }
    @OnClose
    public void CerraConexxion(Session session) {
        sessionHandler.removeSession(session);
    }
    public void enviarMensajeBroadcast(String mensaje) {
        for (Session session : sessionHandler.getSessions()) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(mensaje);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
