package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Proveedor;
import java.io.Serializable;
@Stateless
@LocalBean
public class ProveedorDAO extends InventarioDefaultDataAccess<Proveedor> implements Serializable {
    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    public ProveedorDAO(Class<Proveedor> entityClass) {
        super(entityClass);
    }

    @Override

    public EntityManager getEntityManager() {
        return em;
    }

    public ProveedorDAO() { super(Proveedor.class); }
    // ⭐ MÉTODO NECESARIO PARA CompraFrm.java
    public Proveedor findById(Integer id) {
        if (id == null) {
            return null;
        }
        // Asumiendo que la clave primaria de Proveedor es Integer (que es lo que el log sugiere)
        return em.find(Proveedor.class, id);
    }
}
