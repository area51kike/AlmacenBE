package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Cliente;

import java.io.Serializable;

@Stateless
@LocalBean
public class ClienteDAO extends InventarioDefaultDataAccess<Cliente> implements Serializable {

    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    public ClienteDAO(Class<Cliente> entityClass) {
        super(entityClass);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public ClienteDAO() { super(Cliente.class); }
}