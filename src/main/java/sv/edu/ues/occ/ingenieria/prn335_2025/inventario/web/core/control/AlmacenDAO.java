package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Almacen;

import java.io.Serializable;
import java.util.List;

@Stateless
@LocalBean
public class AlmacenDAO extends InventarioDefaultDataAccess<Almacen> implements Serializable {

    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    public AlmacenDAO() {
        super(Almacen.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public Almacen findById(Integer id) {
        try {
            if (id == null) {
                return null;
            }
            return getEntityManager().find(Almacen.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Encuentra todos los almacenes asociados a un tipo de almacén específico
     */
    public List<Almacen> findByTipoAlmacen(Integer idTipoAlmacen) {
        try {
            if (idTipoAlmacen == null) {
                return List.of();
            }

            TypedQuery<Almacen> query = em.createQuery(
                    "SELECT a FROM Almacen a WHERE a.idTipoAlmacen.id = :idTipoAlmacen",
                    Almacen.class
            );
            query.setParameter("idTipoAlmacen", idTipoAlmacen);
            return query.getResultList();

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

}