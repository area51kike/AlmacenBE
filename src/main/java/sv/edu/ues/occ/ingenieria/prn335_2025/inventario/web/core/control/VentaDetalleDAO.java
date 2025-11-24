package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.VentaDetalle;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object (DAO) para la entidad VentaDetalle.
 * Hereda las operaciones CRUD básicas de InventarioDefaultDataAccess.
 */
@Stateless
@LocalBean
public class VentaDetalleDAO extends InventarioDefaultDataAccess<VentaDetalle> implements Serializable {

    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    /**
     * Constructor requerido para inicializar la clase base con la entidad VentaDetalle.
     */
    public VentaDetalleDAO() {
        super(VentaDetalle.class);
    }
    public List<Producto> findProductosByIdVenta(UUID idVenta) {
        return em.createQuery(
                        "SELECT vd.idProducto FROM VentaDetalle vd WHERE vd.idVenta.id = :idVenta",
                        Producto.class
                )
                .setParameter("idVenta", idVenta)
                .getResultList();
    }

    /**
     * Proporciona la instancia inyectada de EntityManager a la clase base.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * Busca un registro de VentaDetalle por su UUID.
     * @param id El UUID del registro a buscar.
     * @return La entidad VentaDetalle encontrada o null.
     */
    public VentaDetalle findById(UUID id) {
        return getEntityManager().find(VentaDetalle.class, id);
    }
}