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

    public void crear(Compra registro) throws IllegalArgumentException {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }

        if (registro.getIdProveedor() == null) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor.");
        }

        Proveedor proveedorExistente = em.find(Proveedor.class, registro.getIdProveedor());
        if (proveedorExistente == null) {
            throw new IllegalArgumentException("El proveedor con ID " + registro.getIdProveedor() + " no existe.");
        }

        registro.setProveedor(proveedorExistente); // Opcional

        em.persist(registro);
    }



    // Dentro de CompraDao.java
// Asegúrate de que esta clase herede de InventarioDefaultDataAccess<Compra>
// y que getEntityManager() esté implementado.

    public Compra findById(Long id) {
        // Usamos el método find() del EntityManager
        return getEntityManager().find(Compra.class, id);
    }
    private Long generarIdCompraManual() {
        Long maxId = (Long) em.createQuery("SELECT COALESCE(MAX(c.id), 0) FROM Compra c").getSingleResult();
        return maxId + 1;
    }

}
