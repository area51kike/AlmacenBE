package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProductoCaracteristica;

import java.util.logging.Level;
import java.util.logging.Logger;

@FacesConverter(value = "tipoProductoCaracteristicaConverter", managed = true)
public class TipoProductoCaracteristicaConverter implements Converter<TipoProductoCaracteristica> {

    private static final Logger LOGGER = Logger.getLogger(TipoProductoCaracteristicaConverter.class.getName());

    @Inject
    private TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDAO;

    @Override
    public TipoProductoCaracteristica getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            Long id = Long.parseLong(value);
            TipoProductoCaracteristica result = tipoProductoCaracteristicaDAO.find(id);

            if (result == null) {
                LOGGER.log(Level.WARNING, "No se encontró TipoProductoCaracteristica con ID: {0}", id);
            }

            return result;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "ID inválido: " + value, e);
            throw new ConverterException("ID inválido: " + value, e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al convertir TipoProductoCaracteristica", e);
            throw new ConverterException("Error al buscar TipoProductoCaracteristica", e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, TipoProductoCaracteristica value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return value.getId().toString();
    }
}