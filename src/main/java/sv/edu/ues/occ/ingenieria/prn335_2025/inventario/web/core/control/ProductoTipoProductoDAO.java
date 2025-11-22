package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProducto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class ProductoTipoProductoDAO extends InventarioDefaultDataAccess<ProductoTipoProducto> implements Serializable {

    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    public ProductoTipoProductoDAO() {
        super(ProductoTipoProducto.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public ProductoTipoProducto findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }
            return em.find(ProductoTipoProducto.class, id);
        } catch (Exception ex) {
            throw new IllegalStateException("Error al buscar el registro por ID", ex);
        }
    }

    /**
     * Busca todas las relaciones ProductoTipoProducto para un producto específico
     * @param idProducto UUID del producto
     * @return Lista de relaciones ProductoTipoProducto
     */
    public List<ProductoTipoProducto> findByid(UUID idProducto) {
        if (idProducto == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }

            return em.createQuery(
                            "SELECT ptp FROM ProductoTipoProducto ptp WHERE ptp.idProducto.id = :idProducto",
                            ProductoTipoProducto.class)
                    .setParameter("idProducto", idProducto)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al buscar relaciones por producto: " + e.getMessage(), e);
        }
    }
}