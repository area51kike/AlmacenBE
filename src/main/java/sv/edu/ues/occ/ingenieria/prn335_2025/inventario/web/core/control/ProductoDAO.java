package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;

import java.util.UUID;

@Stateless
public class ProductoDAO extends InventarioDefaultDataAccess<Producto> {

    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    public ProductoDAO() {
        super(Producto.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(Producto registro) throws IllegalArgumentException {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }

            if (registro.getId() == null) {
                registro.setId(UUID.randomUUID());
            }

            em.persist(registro);
        } catch (Exception ex) {
            throw new IllegalStateException("Error al crear el registro", ex);
        }
    }
}