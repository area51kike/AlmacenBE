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

    // ✅ Constructor vacío PRIMERO
    public AlmacenDAO() {
        super(Almacen.class);
    }

    // Constructor con parámetro
    public AlmacenDAO(Class<Almacen> entityClass) {
        super(entityClass);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public Almacen findById(Integer id) {
        try {
            if (id == null) {
                return null;
            }
            return getEntityManager().find(Almacen.class, id);
        } catch (Exception e) {
            System.err.println("Error en AlmacenDAO.findById: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}