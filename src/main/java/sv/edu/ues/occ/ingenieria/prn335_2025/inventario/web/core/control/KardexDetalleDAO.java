package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.KardexDetalle;
import java.io.Serializable;
@Stateless
@LocalBean
public class KardexDetalleDAO extends InventarioDefaultDataAccess<KardexDetalle> implements Serializable {
    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    public KardexDetalleDAO(Class<KardexDetalle> entityClass) {
        super(entityClass);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public KardexDetalleDAO() { super(KardexDetalle.class); }
}
