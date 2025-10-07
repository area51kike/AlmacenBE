package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;

import java.io.Serializable;
import java.util.List;

@Stateless
@LocalBean
public class TipoAlmacenDAO extends InventarioDefaultDataAccess<TipoAlmacen> implements Serializable {
    public TipoAlmacenDAO() { super(TipoAlmacen.class); }

    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }
    public List<TipoAlmacen> findRange(int first, int max) throws IllegalArgumentException {
        if (first < 0 || max < 1) {
            throw new IllegalArgumentException();
        }

        try {
            EntityManager em = getEntityManager();

            if (em != null) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<TipoAlmacen> cq = cb.createQuery(TipoAlmacen.class);
                Root<TipoAlmacen> rootEntry = cq.from(TipoAlmacen.class);
                CriteriaQuery<TipoAlmacen> all = cq.select(rootEntry);
                TypedQuery<TipoAlmacen> allQuery = em.createQuery(all);
                allQuery.setFirstResult(first);
                allQuery.setMaxResults(max);
                return allQuery.getResultList();
            }

        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

        throw new IllegalStateException("No se puede acceder al repositorio");
    }
}
