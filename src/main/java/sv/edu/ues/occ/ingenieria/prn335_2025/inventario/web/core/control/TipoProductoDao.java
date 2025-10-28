package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProducto;

import java.io.Serializable;

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
}
