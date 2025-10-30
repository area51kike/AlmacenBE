package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Caracteristica;
import java.io.Serializable;

@Stateless
@LocalBean
public class CaracteristicaDAO extends InventarioDefaultDataAccess<Caracteristica> implements Serializable {

    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    public CaracteristicaDAO() {
        super(Caracteristica.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(Caracteristica registro) throws IllegalArgumentException {

        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }

        try {
            // ✅ Generar ID usando la secuencia de PostgreSQL
            if (registro.getId() == null || registro.getId() == 0) {
                Query query = em.createNativeQuery("SELECT nextval('caracteristica_id_caracteristica_seq'::regclass)");
                Number nextId = (Number) query.getSingleResult();
                registro.setId(nextId.intValue());
            }

            super.crear(registro);

        } catch (Exception ex) {
            throw new IllegalStateException("Error al crear el registro", ex);
        }
    }
}