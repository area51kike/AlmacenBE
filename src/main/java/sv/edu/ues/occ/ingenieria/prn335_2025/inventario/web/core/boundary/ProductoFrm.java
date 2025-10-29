package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;

import java.io.Serializable;
import java.util.UUID;

@Named
@ViewScoped
public class ProductoFrm extends DefaultFrm<Producto> implements Serializable {

    @Inject
    FacesContext facesContext;

    @Inject
    ProductoDAO productoDAO;

    public ProductoFrm() {
        this.nombreBean = "Productos";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected InventarioDefaultDataAccess<Producto> getDao() {
        return productoDAO;
    }

    @Override
    protected Producto nuevoRegistro() {
        Producto producto = new Producto();
        return producto;
    }

    @Override
    protected Producto buscarRegistroPorId(Object id) {
        if (id != null && id instanceof java.util.UUID buscado && !this.modelo.getWrappedData().isEmpty()) {
            for (Producto p : (Iterable<Producto>) productoDAO.findAll()) {
                if (p.getId().equals(buscado)) {
                    return p;
                }
            }
        }
        return null;
    }

    @Override
    protected String getIdAsText(Producto r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected Producto getIdByText(String id) {
        if (id != null && this.modelo != null && !this.modelo.getWrappedData().isEmpty()) {
            try {
                java.util.UUID buscado = java.util.UUID.fromString(id);
                return this.modelo.getWrappedData().stream()
                        .filter(r -> r.getId() != null && r.getId().equals(buscado))
                        .findFirst()
                        .orElse(null);
            } catch (IllegalArgumentException e) {
                System.err.println("ID no es un UUID válido: " + id);
                return null;
            }
        }
        return null;
    }

    @Override
    protected boolean esNombreVacio(Producto registro) {
        if (registro == null) return true;
        String nombre = registro.getNombreProducto();
        return nombre == null || nombre.trim().isEmpty();
    }
    // Dentro de ProductoDao.java (Data Access Object para la entidad Producto)

// Asume que la clase hereda de InventarioDefaultDataAccess<Producto>


}