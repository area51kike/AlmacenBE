package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Proveedor;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@LocalBean
public class CompraDAO extends InventarioDefaultDataAccess<Compra> implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(CompraDAO.class.getName());

    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    public CompraDAO() {
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
            // Validar que el proveedor esté asignado
            if (entidad.getIdProveedor() == null) {
                throw new IllegalArgumentException("El ID del proveedor es obligatorio");
            }

            // Buscar el proveedor en la base de datos
            Proveedor proveedor = em.find(Proveedor.class, entidad.getIdProveedor());
            if (proveedor == null) {
                throw new IllegalArgumentException("El proveedor con ID " + entidad.getIdProveedor() + " no existe");
            }

            // Asignar la entidad completa del proveedor
            entidad.setProveedor(proveedor);

            LOGGER.log(Level.INFO, "Guardando compra: {0}", entidad);

            // Persistir usando el método de la clase base
            super.crear(entidad);

            LOGGER.log(Level.INFO, "Compra creada exitosamente con ID: {0}", entidad.getId());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear compra", e);

            // Obtener la causa raíz
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }
            LOGGER.log(Level.SEVERE, "Causa raíz: {0} - {1}",
                    new Object[]{causa.getClass().getName(), causa.getMessage()});

            throw new RuntimeException("Error al crear la compra: " + causa.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Compra registro) {
        LOGGER.log(Level.INFO, "Eliminando compra con ID: {0}", registro.getId());
        try {
            super.eliminar(registro);
            LOGGER.log(Level.INFO, "Compra eliminada exitosamente");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar compra", e);
            throw new RuntimeException("Error al eliminar la compra: " + e.getMessage(), e);
        }
    }

    @Override
    public Compra findById(Object id) {
        LOGGER.log(Level.FINE, "Buscando compra por ID: {0}", id);
        return super.findById(id);
    }
    public List<Compra> buscarPagadasParaRecepcion(int first, int max) {
        return em.createQuery(
                        "SELECT c FROM Compra c WHERE c.estado = 'PAGADA' ORDER BY c.fecha",
                        Compra.class)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList();
    }

    public Long contarPagadasParaRecepcion() {
        return em.createQuery(
                        "SELECT COUNT(c) FROM Compra c WHERE c.estado = 'PAGADA'",
                        Long.class)
                .getSingleResult();
    }
    public void validarProveedor(Integer idProveedor) {
        if (idProveedor != null) {
            Proveedor proveedor = em.find(Proveedor.class, idProveedor);
            if (proveedor == null) {
                throw new IllegalArgumentException("El proveedor con ID " + idProveedor + " no existe");
            }
        }
    }
}