package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProductoCaracteristica;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Stateless
public class ProductoTipoProductoCaracteristicaDAO extends InventarioDefaultDataAccess<ProductoTipoProductoCaracteristica> {

    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    public ProductoTipoProductoCaracteristicaDAO() {
        super(ProductoTipoProductoCaracteristica.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * Busca todas las características asociadas a un ProductoTipoProducto específico
     * @param idPTP UUID del ProductoTipoProducto
     * @return Lista de ProductoTipoProductoCaracteristica
     */
    public List<ProductoTipoProductoCaracteristica> findByIdProductoTipoProducto(UUID idPTP) {
        try {
            return em.createQuery(
                            "SELECT ptpc FROM ProductoTipoProductoCaracteristica ptpc " +
                                    "WHERE ptpc.idProductoTipoProducto.id = :idPTP " +
                                    "ORDER BY ptpc.idTipoProductoCaracteristica.caracteristica.nombre",
                            ProductoTipoProductoCaracteristica.class)
                    .setParameter("idPTP", idPTP)
                    .getResultList();
        } catch (Exception e) {
            // Log del error si tienes logger
            // logger.error("Error al buscar características por ProductoTipoProducto", e);
            return new ArrayList<>();
        }
    }

    /**
     * Busca una característica específica por su ID
     * @param id UUID de la característica
     * @return ProductoTipoProductoCaracteristica o null si no existe
     */
    public ProductoTipoProductoCaracteristica findById(UUID id) {
        try {
            return em.find(ProductoTipoProductoCaracteristica.class, id);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifica si existe una característica específica para un ProductoTipoProducto
     * @param idPTP UUID del ProductoTipoProducto
     * @param idTPC ID de TipoProductoCaracteristica
     * @return true si ya existe la asociación
     */
    public boolean existeCaracteristica(UUID idPTP, Long idTPC) {
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(ptpc) FROM ProductoTipoProductoCaracteristica ptpc " +
                                    "WHERE ptpc.idProductoTipoProducto.id = :idPTP " +
                                    "AND ptpc.idTipoProductoCaracteristica.id = :idTPC",
                            Long.class)
                    .setParameter("idPTP", idPTP)
                    .setParameter("idTPC", idTPC)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }
}