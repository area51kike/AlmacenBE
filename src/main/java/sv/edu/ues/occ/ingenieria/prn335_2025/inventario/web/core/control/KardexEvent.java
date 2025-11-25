package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;
/**
 * Evento CDI para notificaciones de Kardex
 */
public class KardexEvent {
    private final String mensaje;

    public KardexEvent(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }
}