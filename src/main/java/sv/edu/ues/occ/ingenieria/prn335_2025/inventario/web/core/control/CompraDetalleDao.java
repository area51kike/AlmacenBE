package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.CompraDetalle;

import java.io.Serializable;
import java.util.UUID;

/**
 * Data Access Object (DAO) para la entidad CompraDetalle.
 * Hereda las operaciones CRUD básicas de InventarioDefaultDataAccess.
 */
@Stateless
@LocalBean
public class CompraDetalleDao extends InventarioDefaultDataAccess<CompraDetalle> implements Serializable {

    // Nombre de la unidad de persistencia (asegúrate de que coincida con tu persistence.xml)
    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    /**
     * Constructor requerido para inicializar la clase base con la entidad CompraDetalle.
     */
    public CompraDetalleDao() {
        super(CompraDetalle.class);
    }

    /**
     * Proporciona la instancia inyectada de EntityManager a la clase base.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * Busca un registro de CompraDetalle por su UUID.
     * @param id El UUID del registro a buscar.
     * @return La entidad CompraDetalle encontrada o null.
     */
    public CompraDetalle findById(UUID id) {
        return getEntityManager().find(CompraDetalle.class, id);
    }
}