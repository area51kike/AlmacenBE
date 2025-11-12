package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.CompraDetalle;

import java.io.Serializable;
import java.util.UUID;


@Stateless
@LocalBean
public class CompraDetalleDao extends InventarioDefaultDataAccess<CompraDetalle> implements Serializable {

    // Nombre de la unidad de persistencia (asegúrate de que coincida con tu persistence.xml)
    @PersistenceContext(unitName = "inventarioPU")
    EntityManager em;

    public CompraDetalleDao() {
        super(CompraDetalle.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public CompraDetalle findById(UUID id) {
        return getEntityManager().find(CompraDetalle.class, id);
    }
}