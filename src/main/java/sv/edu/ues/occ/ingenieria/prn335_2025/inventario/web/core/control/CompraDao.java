package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@LocalBean
public class CompraDao extends InventarioDefaultDataAccess<Compra> implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(CompraDao.class.getName());

    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    public CompraDao() {
        super(Compra.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(Compra entidad) {
        LOGGER.log(Level.INFO, "Creando nueva compra...");

        try {
            // Si el ID no está asignado, generamos uno basado en el proveedor.
            if (entidad.getId() == null) {
                if (entidad.getIdProveedor() == null) {
                    throw new IllegalArgumentException("El ID del proveedor es obligatorio");
                }

                // Usamos el ID del proveedor como ID de la compra
                Long idCompra = entidad.getIdProveedor().longValue();
                Compra existente = em.find(Compra.class, idCompra);

                // Si ya existe, buscamos un ID alternativo
                if (existente != null) {
                    LOGGER.log(Level.INFO, "ID de compra ya existe, buscando uno alternativo");
                    idCompra = buscarIdProveedorDisponible();
                }

                entidad.setId(idCompra);
                LOGGER.log(Level.INFO, "ID de compra asignado: {0}", idCompra);
            } else {
                LOGGER.log(Level.INFO, "Usando ID proporcionado: {0}", entidad.getId());
            }

            // Mostramos los detalles de la compra
            LOGGER.log(Level.INFO, "Detalles de la compra: {0}", entidad);

            // Guardamos la compra
            super.crear(entidad);
            LOGGER.log(Level.INFO, "Compra creada exitosamente");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear compra", e);

            // Mostramos la causa del error
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }
            LOGGER.log(Level.SEVERE, "Causa raíz: {0} - {1}", new Object[]{causa.getClass().getName(), causa.getMessage()});

            throw e;
        }
    }

    /**
     * Busca un proveedor disponible para asignarle un ID de compra.
     *
     * @return Un ID válido de proveedor.
     * @throws RuntimeException si no hay proveedores disponibles.
     */
    private Long buscarIdProveedorDisponible() {
        try {
            TypedQuery<Integer> query = em.createQuery(
                    "SELECT p.id FROM Proveedor p " +
                            "WHERE p.id NOT IN (SELECT c.id FROM Compra c) " +
                            "ORDER BY p.id",
                    Integer.class
            );
            query.setMaxResults(1);

            List<Integer> resultado = query.getResultList();

            if (resultado.isEmpty()) {
                throw new RuntimeException("No hay IDs de proveedor disponibles.");
            }

            return resultado.get(0).longValue();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error buscando ID de proveedor disponible", e);
            throw new RuntimeException("No se pudo encontrar un ID válido para la compra", e);
        }
    }

    @Override
    public void eliminar(Compra registro) {
        LOGGER.log(Level.INFO, "Eliminando compra con ID: {0}", registro.getId());
        try {
            // Elimina la compra usando el método de la clase base.
            super.eliminar(registro);
            LOGGER.log(Level.INFO, "Compra eliminada exitosamente");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar compra", e);
            throw e;
        }
    }

    @Override
    public Compra findById(Object id) {
        LOGGER.log(Level.FINE, "Buscando compra por ID: {0}", id);
        return super.findById(id);
    }
}
