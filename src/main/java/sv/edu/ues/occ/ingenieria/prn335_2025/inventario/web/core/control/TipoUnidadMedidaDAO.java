package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoUnidadMedida;

@Stateless
public class TipoUnidadMedidaDAO extends InventarioDefaultDataAccess<TipoUnidadMedida> {

    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    public TipoUnidadMedidaDAO() {
        super(TipoUnidadMedida.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
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

            // Obtener el siguiente valor de la secuencia
            if (registro.getId() == null || registro.getId() == 0) {
                Query query = em.createNativeQuery("SELECT nextval('tipo_unidad_medida_id_tipo_unidad_medida_seq'::regclass)");
                Number nextId = (Number) query.getSingleResult();
                registro.setId(nextId.intValue());
            }

            em.persist(registro);
        } catch (Exception ex) {
            throw new IllegalStateException("Error al crear el registro", ex);
        }
    }
}