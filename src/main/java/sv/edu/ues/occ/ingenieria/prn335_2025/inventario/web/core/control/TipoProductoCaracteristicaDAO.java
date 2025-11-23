package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProductoCaracteristica;

import java.util.List;

@Stateless
public class TipoProductoCaracteristicaDAO extends InventarioDefaultDataAccess<TipoProductoCaracteristica> {

    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    public TipoProductoCaracteristicaDAO() {
        super(TipoProductoCaracteristica.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<TipoProductoCaracteristica> findByIdTipoProducto(Long idTipoProducto) {
        return em.createQuery("SELECT tpc FROM TipoProductoCaracteristica tpc WHERE tpc.tipoProducto.id = :idTipoProducto", TipoProductoCaracteristica.class)
                .setParameter("idTipoProducto", idTipoProducto)
                .getResultList();
    }

    public TipoProductoCaracteristica findById(Long id) {
        return em.find(TipoProductoCaracteristica.class, id);
    }

    public Long obtenerMaximoId() {
        try {
            return em.createQuery(
                    "SELECT COALESCE(MAX(t.id), 0) FROM TipoProductoCaracteristica t", Long.class
            ).getSingleResult();
        } catch (Exception e) {
            return 0L;
        }
    }
}