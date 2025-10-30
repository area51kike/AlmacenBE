package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

/**
 * Clase base para operaciones CRUD de entidades JPA.
 */
public abstract class InventarioDefaultDataAccess<T> implements InventarioDAOInterface<T> {

    final Class<T> entityClass;

    public InventarioDefaultDataAccess(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public abstract EntityManager getEntityManager();

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public T find(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }
            return em.find(entityClass, id);
        } catch (Exception ex) {
            throw new IllegalStateException("Error al buscar el registro por ID", ex);
        }
    }

    public T findById(Object id) {
        return find(id);
    }

    public List<T> findAll() {
        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root);
            TypedQuery<T> query = em.createQuery(cq);
            return query.getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("Error al acceder a todos los registros", ex);
        }
    }

    public List<T> findRange(int first, int pageSize) {
        if (first < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root).orderBy(cb.asc(root.get("id")));

            TypedQuery<T> query = em.createQuery(cq);
            query.setFirstResult(first);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener rango de registros", e);
        }
    }

    public Long count() {
        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<T> root = cq.from(entityClass);
            cq.select(cb.count(root));
            TypedQuery<Long> query = em.createQuery(cq);
            return query.getSingleResult();
        } catch (Exception ex) {
            throw new IllegalStateException("Error al contar registros", ex);
        }
    }

    public void crear(T registro) {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }
            em.persist(registro);
        } catch (Exception ex) {
            throw new RuntimeException("Error al crear el registro", ex);
        }
    }

    public T modificar(T registro) {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }
            return em.merge(registro);
        } catch (Exception ex) {
            throw new RuntimeException("Error al modificar el registro", ex);
        }
    }

    public void eliminar(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }

            if (!em.contains(entity)) {
                entity = em.merge(entity);
            }
            em.remove(entity);

        } catch (Exception ex) {
            throw new RuntimeException("Error al eliminar el registro", ex);
        }
    }

    public void eliminarPorId(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }

            T registro = em.find(entityClass, id);
            if (registro != null) {
                em.remove(registro);
            } else {
                throw new IllegalArgumentException("Registro no encontrado");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error al eliminar el registro por ID", ex);
        }
    }
}
