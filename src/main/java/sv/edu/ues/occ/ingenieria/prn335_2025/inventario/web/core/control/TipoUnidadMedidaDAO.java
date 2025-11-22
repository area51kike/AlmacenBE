package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoUnidadMedida;

import java.io.Serializable;

@Stateless
@LocalBean
public class TipoUnidadMedidaDAO extends InventarioDefaultDataAccess<TipoUnidadMedida> implements Serializable {

    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    public TipoUnidadMedidaDAO() {
        super(TipoUnidadMedida.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public TipoUnidadMedida findById(Integer id) {
        try {
            if (id == null) {
                return null;
            }
            return getEntityManager().find(TipoUnidadMedida.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void crear(TipoUnidadMedida registro) throws IllegalArgumentException {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }

            if (registro.getId() == null || registro.getId() == 0) {
                Query query = em.createNativeQuery("SELECT nextval('tipo_unidad_medida_id_tipo_unidad_medida_seq'::regclass)");
                Number nextId = (Number) query.getSingleResult();
                registro.setId(nextId.intValue());
            }

            em.persist(registro);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalStateException("Error al crear el registro", ex);
        }
    }
}