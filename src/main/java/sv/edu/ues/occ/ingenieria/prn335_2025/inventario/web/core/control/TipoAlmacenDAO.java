package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;
import java.io.Serializable;

@Stateless
@LocalBean
public class TipoAlmacenDAO extends InventarioDefaultDataAccess<TipoAlmacen> implements Serializable {

    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    // ✅ Constructor vacío PRIMERO
    public TipoAlmacenDAO() {
        super(TipoAlmacen.class);
    }

    // Constructor con parámetro
    public TipoAlmacenDAO(Class<TipoAlmacen> entityClass) {
        super(entityClass);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    // ✅ Método para el Converter
    public TipoAlmacen findById(Integer id) {
        try {
            if (id == null) {
                return null;
            }
            return getEntityManager().find(TipoAlmacen.class, id);
        } catch (Exception e) {
            System.err.println("Error en TipoAlmacenDAO.findById: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}