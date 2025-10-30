package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;
import jakarta.ejb.Stateless;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Proveedor;


import java.io.Serializable;
@Stateless
@LocalBean
public class CompraDao extends InventarioDefaultDataAccess <Compra> {
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
    public void eliminar(Compra registro) {
        // Llamamos a la lógica de eliminación que ya está definida en la clase base.
        // El contenedor EJB se asegura de hacer el COMMIT después de que esta línea termine.
        super.eliminar(registro);
    }

    @Override
    public void crear(Compra entidad) {
        // Llamamos a la lógica de eliminación que ya está definida en la clase base.
        // El contenedor EJB se asegura de hacer el COMMIT después de que esta línea termine.
        super.crear(entidad);
    }

    @Override
    public Compra findById(Object id) {

        return super.findById(id);

    }
}