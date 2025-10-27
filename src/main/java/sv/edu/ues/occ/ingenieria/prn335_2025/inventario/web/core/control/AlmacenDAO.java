package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Almacen;

import java.io.Serializable;
@Stateless
@LocalBean
public class AlmacenDAO extends InventarioDefaultDataAccess<Almacen> implements Serializable {
    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    public AlmacenDAO(Class<Almacen> entityClass) {
        super(entityClass);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public AlmacenDAO() { super(Almacen.class); }
}
