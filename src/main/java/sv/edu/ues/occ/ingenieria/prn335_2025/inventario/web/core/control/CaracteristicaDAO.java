package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Caracteristica;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Stateless
@LocalBean
public class CaracteristicaDAO extends InventarioDefaultDataAccess<Caracteristica> implements Serializable {

    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    public CaracteristicaDAO() {
        super(Caracteristica.class);
    }
    public Caracteristica findById(Long id) {
        if (id == null) {
            System.err.println("CaracteristicaDAO.findById: ID es nulo");
            return null;
        }

        try {
            if (em == null) {
                System.err.println("CaracteristicaDAO.findById: EntityManager no disponible");
                return null;
            }
            Caracteristica result = em.find(Caracteristica.class, id);
            System.out.println("CaracteristicaDAO.findById(" + id + "): " +
                    (result != null ? result.getNombre() : "null"));
            return result;
        } catch (Exception ex) {
            System.err.println("CaracteristicaDAO.findById: Error al buscar característica - " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public List<Caracteristica> findAll() {
        try {
            return em.createQuery("SELECT c FROM Caracteristica c ORDER BY c.nombre", Caracteristica.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Error al cargar Caracteristica: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
