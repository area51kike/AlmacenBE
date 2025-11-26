package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.VentaDetalle;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) para la entidad VentaDetalle.
 * Hereda las operaciones CRUD básicas de InventarioDefaultDataAccess.
 */
@Stateless
@LocalBean
public class VentaDetalleDAO extends InventarioDefaultDataAccess<VentaDetalle> implements Serializable {

    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    /**
     * Constructor requerido para inicializar la clase base con la entidad VentaDetalle.
     */
    public VentaDetalleDAO() {
        super(VentaDetalle.class);
    }
    public List<Producto> findProductosByIdVenta(UUID idVenta) {
        return em.createQuery(
                        "SELECT vd.idProducto FROM VentaDetalle vd WHERE vd.idVenta.id = :idVenta",
                        Producto.class
                )
                .setParameter("idVenta", idVenta)
                .getResultList();
    }
    public Long contarDetallesPorVenta(UUID idVenta) {
        try {
            return em.createQuery(
                            "SELECT COUNT(d) FROM VentaDetalle d WHERE d.idVenta.id = :idCompra",
                            Long.class)
                    .setParameter("idCompra", idVenta)
                    .getSingleResult();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Error al contar detalles", e);
            return 0L;
        }
    }
    public Long contarDetallesDespachadosPorVenta(UUID idVenta) {
        try {
            return em.createQuery(
                            "SELECT COUNT(d) FROM VentaDetalle d " +
                                    "WHERE d.idVenta.id = :idVenta AND d.estado = 'DESPACHADO'",
                            Long.class)
                    .setParameter("idVenta", idVenta)
                    .getSingleResult();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Error al contar detalles despachados", e);
            return 0L;
        }
    }
    public List<VentaDetalle> findByIdVenta(UUID idVenta) {
        return em.createQuery("SELECT vd FROM VentaDetalle vd WHERE vd.idVenta.id = :idVenta", VentaDetalle.class)
                .setParameter("idVenta", idVenta)
                .getResultList();
    }

    /**
     * Proporciona la instancia inyectada de EntityManager a la clase base.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
    public boolean todosDetallesDespachados(UUID idVenta) {
        Long total = contarDetallesPorVenta(idVenta);
        Long despachados = contarDetallesDespachadosPorVenta(idVenta);
        return total > 0 && total.equals(despachados);
    }
    public BigDecimal obtenerTotalVenta(UUID idVenta) {
        BigDecimal total = em.createQuery(
                        "SELECT COALESCE(SUM(cd.cantidad * cd.precio), 0) FROM VentaDetalle cd WHERE cd.idVenta.id = :idVenta",
                        BigDecimal.class)
                .setParameter("idVenta", idVenta)
                .getSingleResult();
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Busca un registro de VentaDetalle por su UUID.
     * @param id El UUID del registro a buscar.
     * @return La entidad VentaDetalle encontrada o null.
     */
    public VentaDetalle findById(UUID id) {
        return getEntityManager().find(VentaDetalle.class, id);
    }

    public int contarPorVenta(UUID idVenta) {
        try {
            Long cuenta = em.createQuery(
                            "SELECT COUNT(d) FROM VentaDetalle d WHERE d.idVenta.id = :idVenta",
                            Long.class)
                    .setParameter("idVenta", idVenta)
                    .getSingleResult();
            return cuenta != null ? cuenta.intValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * [NUEVO] Busca los detalles de una venta con PAGINACIÓN (first, max).
     * Requerido por PrimeFaces LazyDataModel.
     */
    public List<VentaDetalle> findPorVenta(UUID idVenta, int first, int max) {
        try {
            return em.createQuery("SELECT vd FROM VentaDetalle vd WHERE vd.idVenta.id = :idVenta", VentaDetalle.class)
                    .setParameter("idVenta", idVenta)
                    .setFirstResult(first)
                    .setMaxResults(max)
                    .getResultList();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }


}