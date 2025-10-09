package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;

import java.io.Serializable;
import java.util.List;

@Stateless
@LocalBean
public class TipoAlmacenDAO extends InventarioDefaultDataAccess<TipoAlmacen> implements Serializable {
    public TipoAlmacenDAO() {
        super(TipoAlmacen.class);
    }

    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<TipoAlmacen> findRange(int first, int max) throws IllegalArgumentException {
        if (first < 0 || max < 1) {
            throw new IllegalArgumentException();
        }

        try {
            EntityManager em = getEntityManager();

            if (em != null) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<TipoAlmacen> cq = cb.createQuery(TipoAlmacen.class);
                Root<TipoAlmacen> rootEntry = cq.from(TipoAlmacen.class);
                CriteriaQuery<TipoAlmacen> all = cq.select(rootEntry);
                TypedQuery<TipoAlmacen> allQuery = em.createQuery(all);
                allQuery.setFirstResult(first);
                allQuery.setMaxResults(max);
                return allQuery.getResultList();
            }

        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

        throw new IllegalStateException("No se puede acceder al repositorio");
    }

    @Override
    public void eliminar(TipoAlmacen registro) throws IllegalArgumentException, IllegalAccessException {
        if (registro == null || registro.getId() == null) {
            throw new IllegalArgumentException("El registro o su ID no pueden ser nulos");
        }

        try {
            EntityManager em = getEntityManager();

            if (em != null) {
                // Verificar si la entidad está gestionada
                if (!em.contains(registro)) {
                    // Si no está gestionada, buscarla primero
                    TipoAlmacen registroGestionado = em.find(TipoAlmacen.class, registro.getId());

                    if (registroGestionado == null) {
                        throw new IllegalArgumentException("El registro con ID " + registro.getId() + " no existe");
                    }

                    em.remove(registroGestionado);
                } else {
                    em.remove(registro);
                }
            } else {
                throw new IllegalStateException("No se puede acceder al repositorio");
            }

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception ex) {
            throw new IllegalAccessException("Error al eliminar el registro: " + ex.getMessage());
        }
    }

    public Integer obtenerProximoId() {
        try {
            EntityManager em = getEntityManager();

            if (em != null) {
                // Obtener el máximo ID actual de la tabla
                Query maxQuery = em.createQuery(
                        "SELECT MAX(t.id) FROM TipoAlmacen t"
                );
                Integer maxId = (Integer) maxQuery.getSingleResult();
                System.out.println("DEBUG: Max ID actual = " + maxId);

                if (maxId != null && maxId > 0) {
                    return maxId + 1;
                }
            }
            return 1;
        } catch (Exception e) {
            System.out.println("DEBUG: Error obteniendo próximo ID: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }
}
