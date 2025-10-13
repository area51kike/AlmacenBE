package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ClienteDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Cliente;

import java.io.Serializable;

@Named
@ViewScoped
public class ClienteFrm extends DefaultFrm<Cliente> implements Serializable {

    @Inject
    FacesContext facesContext;

    @Inject
    ClienteDAO clienteDao;

    public ClienteFrm() {
        this.nombreBean = "Cliente";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected InventarioDefaultDataAccess<Cliente> getDao() {
        return clienteDao;
    }

    @Override
    protected Cliente nuevoRegistro() {
        Cliente cliente = new Cliente();
        return cliente;
    }

    @Override
    protected Cliente buscarRegistroPorId(Object id) {
        if (id != null && id instanceof java.util.UUID buscado && !this.modelo.getWrappedData().isEmpty()) {
            for (Cliente c : (Iterable<Cliente>) clienteDao.findAll()) {
                if (c.getId().equals(buscado)) {
                    return c;
                }
            }
        }
        return null;
    }

    @Override
    protected String getIdAsText(Cliente r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected Cliente getIdByText(String id) {
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
    protected boolean esNombreVacio(Cliente registro) {
        if (registro == null) return true;
        String nombre = registro.getNombre();
        return nombre == null || nombre.trim().isEmpty();
    }
}