package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProducto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Stateless
@LocalBean
public class TipoProductoDao extends InventarioDefaultDataAccess<TipoProducto> implements Serializable {

    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    @Override

    public EntityManager getEntityManager() {
        return em;
    }

    public TipoProductoDao() { super(TipoProducto.class); }


    public List<TipoProducto> findAll() {
        try {
            return em.createQuery("SELECT t FROM TipoProducto t ORDER BY t.nombre", TipoProducto.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Error al cargar TipoProducto: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public TipoProducto findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        EntityManager em = getEntityManager();
        if (em == null) {
            throw new IllegalStateException("EntityManager no disponible");
        }

        try {
            return em.find(TipoProducto.class, id);
        } catch (Exception ex) {
            throw new IllegalStateException("Error al buscar el registro por ID", ex);
        }
    }
}
