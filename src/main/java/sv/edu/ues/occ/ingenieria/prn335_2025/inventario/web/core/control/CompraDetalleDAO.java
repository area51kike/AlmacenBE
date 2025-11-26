package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.CompraDetalle;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


@Stateless
@LocalBean
public class CompraDetalleDAO extends InventarioDefaultDataAccess<CompraDetalle> implements Serializable {

    // Nombre de la unidad de persistencia
    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    public CompraDetalleDAO() {
        super(CompraDetalle.class);
    }
    public Long contarDetallesRecibidosPorCompra(Long idCompra) {
        try {
            return em.createQuery(
                            "SELECT COUNT(d) FROM CompraDetalle d " +
                                    "WHERE d.idCompra.id = :idCompra AND d.estado = 'RECIBIDO'",
                            Long.class)
                    .setParameter("idCompra", idCompra)
                    .getSingleResult();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Error al contar detalles recibidos", e);
            return 0L;
        }
    }
    public List<CompraDetalle> findByIdCompra(Long idCompra) {
        return em.createQuery("SELECT cd FROM CompraDetalle cd WHERE cd.idCompra.id = :idCompra", CompraDetalle.class)
                .setParameter("idCompra", idCompra)
                .getResultList();
    }
    public BigDecimal obtenerTotalCompra(Long idCompra) {
        BigDecimal total = em.createQuery(
                        "SELECT COALESCE(SUM(cd.cantidad * cd.precio), 0) FROM CompraDetalle cd WHERE cd.idCompra.id = :idCompra",
                        BigDecimal.class)
                .setParameter("idCompra", idCompra)
                .getSingleResult();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }
    public boolean todosDetallesRecibidos(Long idCompra) {
        Long total = contarDetallesPorCompra(idCompra);
        Long recibidos = contarDetallesRecibidosPorCompra(idCompra);
        return total > 0 && total.equals(recibidos);
    }
    public Long contarDetallesPorCompra(Long idCompra) {
        try {
            return em.createQuery(
                            "SELECT COUNT(d) FROM CompraDetalle d WHERE d.idCompra.id = :idCompra",
                            Long.class)
                    .setParameter("idCompra", idCompra)
                    .getSingleResult();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Error al contar detalles", e);
            return 0L;
        }
    }

    public CompraDetalle findById(UUID id) {
        return getEntityManager().find(CompraDetalle.class, id);
    }
}