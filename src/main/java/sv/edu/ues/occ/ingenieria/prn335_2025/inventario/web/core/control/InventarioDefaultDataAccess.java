package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

/**
 * Clase abstracta que proporciona implementación por defecto de operaciones CRUD
 * para entidades JPA en el sistema de inventario.
 *
 * @param <T> El tipo de entidad que será gestionada por este DAO
 */
public abstract class InventarioDefaultDataAccess<T> implements InventarioDAOInterface<T> {

    // Campo final para la clase de entidad
    // Esta variable guardará el tipo de entidad con la que el DAO trabajará
    final Class<T> entityClass;

    /**
     * Constructor que inicializa el DAO con la clase de entidad específica
     * @param entityClass La clase de la entidad a gestionar
     */
    public InventarioDefaultDataAccess(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Método abstracto que debe ser implementado por las clases hijas
     * para proporcionar el EntityManager específico
     * @return EntityManager para operaciones de persistencia
     */
    public abstract EntityManager getEntityManager();

    /**
     * Getter público para obtener la clase de entidad
     * Útil para operaciones de reflexión y validaciones
     * @return La clase de entidad gestionada por este DAO
     */
    public Class<T> getEntityClass() {
        return entityClass;
    }

    /**
     * Busca una entidad por su ID
     * @param id El identificador de la entidad
     * @return La entidad encontrada o null si no existe
     * @throws IllegalArgumentException si el ID es nulo
     * @throws IllegalStateException si hay problemas con el EntityManager
     */
    public T find(Object id) throws IllegalArgumentException {
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

    /**
     * Alias de find() para compatibilidad
     * @param id El identificador de la entidad
     * @return La entidad encontrada o null si no existe
     */
    public T findById(Object id) throws IllegalArgumentException {
        return find(id);
    }

    /**
     * Recupera todas las entidades de la base de datos
     * @return Lista con todas las entidades
     * @throws IllegalStateException si hay error en la consulta
     */
    public List<T> findAll() throws IllegalArgumentException {
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

    /**
     * Recupera un rango específico de registros con paginación
     * Incluye ordenamiento automático por ID ascendente
     *
     * @param first Índice del primer registro (0-based)
     * @param pageSize Cantidad de registros a recuperar
     * @return Lista de entidades en el rango especificado
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    public List<T> findRange(int first, int pageSize) {
        // Validación estricta de parámetros
        if (first < 0 || pageSize <= 0) {
            throw new IllegalArgumentException(
                    "Parámetros inválidos: first=" + first + ", pageSize=" + pageSize +
                            ". first debe ser >= 0 y pageSize debe ser > 0"
            );
        }

        System.out.println("findRange llamado con first=" + first + ", pageSize=" + pageSize);

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);

            // ⭐ MEJORA: Ordenamiento ascendente por ID para resultados consistentes
            cq.select(root).orderBy(cb.asc(root.get("id")));

            TypedQuery<T> query = em.createQuery(cq);
            query.setFirstResult(first);
            query.setMaxResults(pageSize);

            List<T> results = query.getResultList();
            System.out.println("findRange retornó " + results.size() + " registros");
            return results;
        } catch (Exception e) {
            System.err.println("Error en findRange: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al obtener rango de registros", e);
        }
    }

    /**
     * Cuenta el total de registros en la base de datos
     *
     * @return Cantidad total de registros como Long (mejor para grandes volúmenes)
     * @throws IllegalStateException si hay error en la consulta
     */
    public Long count() throws IllegalArgumentException {
        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }

            // Lógica para contar usando la API de Criteria
            CriteriaBuilder cb = em.getCriteriaBuilder();
            // ⭐ MEJORA: Retorna Long en lugar de int para soportar grandes volúmenes
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<T> root = cq.from(entityClass);

            /*
             * Le dice al traductor: "No me traigas las entidades, solo cuenta cuántas hay"
             * ¿Qué es cb.count()? La función que cuenta registros
             * ¿Qué es .select()? Le dice "esto es lo que quiero obtener"
             * En SQL sería como: SELECT COUNT(*) FROM tabla
             */
            cq.select(cb.count(root));

            /*
             * Es la consulta "preparada" y lista para enviar a la base de datos
             * ¿Por qué <Long>? Porque sabemos que nos va a devolver un número
             * ¿Qué hace createQuery()? Prepara la consulta para ser ejecutada
             */
            TypedQuery<Long> query = em.createQuery(cq);
            return query.getSingleResult();
        } catch (Exception ex) {
            throw new IllegalStateException("Error al contar registros: " + ex.getMessage(), ex);
        }
    }

    /**
     * Crea una nueva entidad en la base de datos
     * @param registro La entidad a crear
     * @throws IllegalArgumentException si el registro es nulo
     * @throws IllegalStateException si hay error en la persistencia
     */
    public void crear(T registro) throws IllegalArgumentException {
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
            throw new RuntimeException("Error al crear el registro: " + ex.getMessage(), ex);
        }
    }

    /**
     * Modifica una entidad existente en la base de datos
     * ⭐ MEJORA: Ahora retorna la entidad actualizada (mejor práctica JPA)
     *
     * @param registro La entidad a modificar
     * @return La entidad actualizada y gestionada por el EntityManager
     * @throws IllegalArgumentException si el registro es nulo
     * @throws IllegalStateException si hay error en la actualización
     */
    public T modificar(T registro) throws IllegalArgumentException {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }
            // ⭐ MEJORA: Retornamos la entidad actualizada
            T actualizado = em.merge(registro);
            return actualizado;
        } catch (Exception ex) {
            throw new RuntimeException("Error al modificar el registro: " + ex.getMessage(), ex);
        }
    }

    /**
     * Elimina una entidad de la base de datos
     * ⭐ MEJORA: Incluye lógica para manejar entidades desconectadas (detached)
     *
     * @param entity La entidad a eliminar
     * @throws IllegalArgumentException si la entidad es nula
     * @throws IllegalStateException si hay error en la eliminación
     */
    public void eliminar(T entity) throws IllegalArgumentException {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }

            // ========== LÓGICA DE ELIMINACIÓN MEJORADA ==========
            if (!em.contains(entity)) {
                // CASO 1: Entidad NO está gestionada (detached)
                // Primero debemos fusionarla para que esté gestionada
                entity = em.merge(entity);
            }

            // CASO 2: Ahora la entidad está gestionada (managed)
            // Podemos eliminarla de forma segura
            em.remove(entity);

        } catch (Exception ex) {
            System.err.println("Error en DAO.eliminar: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Error al eliminar el registro: " + ex.getMessage(), ex);
        }
    }

    /**
     * Elimina una entidad buscándola primero por su ID
     * Útil cuando solo se tiene el identificador
     *
     * @param id El identificador de la entidad a eliminar
     * @throws IllegalArgumentException si el ID es nulo o la entidad no existe
     * @throws IllegalStateException si hay error en la eliminación
     */
    public void eliminarPorId(Object id) throws IllegalArgumentException {
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
                throw new IllegalArgumentException("Registro con ID " + id + " no encontrado");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error al eliminar el registro por ID: " + ex.getMessage(), ex);
        }
    }
}