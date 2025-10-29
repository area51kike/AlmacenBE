package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Venta;

import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
// Extendemos de la clase base que acabas de proporcionar
public class VentaDao extends InventarioDefaultDataAccess<Venta> {

    // 💡 IMPORTANTE: Asegúrate de que esta unidad de persistencia coincida con tu persistence.xml
    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    public VentaDao() {
        super(Venta.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }
    // Dentro de VentaDao.java (Data Access Object para la entidad Venta)

// Asume que la clase hereda de InventarioDefaultDataAccess<Venta>

    public Venta findById(UUID id) {
        // Aquí usamos el EntityManager para buscar la entidad Venta
        return getEntityManager().find(Venta.class, id);
    }

    // Aquí puedes agregar métodos específicos para Venta si los necesitas,
    // pero los métodos CRUD base ya están en InventarioDefaultDataAccess.
    public List<Venta> findRange(int first, int pageSize) {
        return em.createQuery("SELECT v FROM Venta v JOIN FETCH v.idCliente", Venta.class)
                .setFirstResult(first)
                .setMaxResults(pageSize)
                .getResultList();
    }

}