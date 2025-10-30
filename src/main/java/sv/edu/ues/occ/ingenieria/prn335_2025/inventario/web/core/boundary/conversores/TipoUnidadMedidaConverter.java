package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoUnidadMedidaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoUnidadMedida;

@FacesConverter(value = "tipoUnidadMedidaConverter", managed = true)
@ApplicationScoped
public class TipoUnidadMedidaConverter implements Converter<TipoUnidadMedida> {

    @Inject
    private TipoUnidadMedidaDAO tipoUnidadMedidaDAO;

    @Override
    public TipoUnidadMedida getAsObject(FacesContext context, UIComponent component, String value) {

        if (value == null || value.trim().isEmpty() || value.equals("null") || value.equals("")) {
            return null;
        }

        try {
            Integer id = Integer.valueOf(value);
            TipoUnidadMedida resultado = tipoUnidadMedidaDAO.findById(id);
            if (resultado != null) {
                System.out.println("Encontrado: ID=" + resultado.getId() + ", Nombre=" + resultado.getNombre());
            } else {
                System.out.println("NO encontrado con ID: " + id);
            }

            return resultado;
        } catch (Exception e) {
            System.err.println("ERROR en Converter: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, TipoUnidadMedida value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return value.getId().toString();
    }
}