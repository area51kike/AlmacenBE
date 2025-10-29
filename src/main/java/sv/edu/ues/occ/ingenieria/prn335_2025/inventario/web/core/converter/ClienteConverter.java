package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ClienteDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Cliente;

import java.util.UUID;

@FacesConverter(value = "clienteConverter", managed = true)
public class ClienteConverter implements Converter<Cliente> {

    @Inject
    private ClienteDAO clienteDao;

    @Override
    public Cliente getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) return null;
        try {
            UUID id = UUID.fromString(value);
            return clienteDao.findById(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Cliente value) {
        return value != null && value.getId() != null ? value.getId().toString() : "";
    }
}
