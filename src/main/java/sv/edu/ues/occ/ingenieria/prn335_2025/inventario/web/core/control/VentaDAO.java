package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Venta;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@LocalBean
public class VentaDAO extends InventarioDefaultDataAccess<Venta> {

    private static final Logger LOGGER = Logger.getLogger(VentaDAO.class.getName());

    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    public VentaDAO() {
        super(Venta.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }
    public void actualizarEstado(UUID idVenta, String nuevoEstado) {
        try {
            Venta venta = em.find(Venta.class, idVenta);
            if (venta != null) {
                venta.setEstado(nuevoEstado);
                em.merge(venta);
            }
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Error al actualizar estado de venta", e);
            throw new RuntimeException("No se pudo actualizar el estado de la venta", e);
        }
    }
    public List<Venta> findByEstado(String estado) {
        return em.createQuery("SELECT c FROM Venta c WHERE c.estado = :estado", Venta.class)
                .setParameter("estado", estado)
                .getResultList();
    }

    public Venta findById(UUID id) {
        try {
            if (id == null) {
                return null;
            }
            return getEntityManager().find(Venta.class, id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en findById: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<Venta> findRange(int first, int pageSize) {
        try {
            LOGGER.log(Level.INFO, "📊 findRange llamado - first: {0}, pageSize: {1}",
                    new Object[]{first, pageSize});

            List<Venta> resultado = em.createQuery(
                            "SELECT v FROM Venta v JOIN FETCH v.idCliente ORDER BY v.fecha DESC",
                            Venta.class)
                    .setFirstResult(first)
                    .setMaxResults(pageSize)
                    .getResultList();

            LOGGER.log(Level.INFO, "✅ findRange retornó {0} registros", resultado.size());
            return resultado;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error en findRange: " + e.getMessage(), e);
            return List.of();
        }
    }
    public List<Venta> buscarPorCliente(UUID idCliente) {
        try {
            return em.createQuery(
                            "SELECT v FROM Venta v " +
                                    "LEFT JOIN FETCH v.idCliente " +
                                    "WHERE v.idCliente.id = :idCliente",
                            Venta.class)
                    .setParameter("idCliente", idCliente)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Error buscando ventas por cliente: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public Long count() {
        try {
            LOGGER.log(Level.INFO, "📊 count() llamado");

            Long total = em.createQuery(
                            "SELECT COUNT(v) FROM Venta v",
                            Long.class)
                    .getSingleResult();

            LOGGER.log(Level.INFO, "✅ Total de ventas: {0}", total);
            return total;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error en count: " + e.getMessage(), e);
            return 0L;
        }
    }
}