package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Caracteristica;

import java.io.Serializable;
import java.util.List;

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

    public Caracteristica findById(Integer id) {
        try {
            if (id == null) {
                return null;
            }
            return getEntityManager().find(Caracteristica.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Encuentra todas las características asociadas a un tipo de unidad de medida específico
     */
    public List<Caracteristica> findByTipoUnidadMedida(Integer idTipoUnidadMedida) {
        try {
            if (idTipoUnidadMedida == null) {
                return List.of();
            }

            TypedQuery<Caracteristica> query = em.createQuery(
                    "SELECT c FROM Caracteristica c WHERE c.idTipoUnidadMedida.id = :idTipoUnidadMedida",
                    Caracteristica.class
            );
            query.setParameter("idTipoUnidadMedida", idTipoUnidadMedida);
            return query.getResultList();

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

}