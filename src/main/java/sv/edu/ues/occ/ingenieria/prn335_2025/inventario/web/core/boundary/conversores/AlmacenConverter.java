package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.AlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Almacen;

import java.util.logging.Level;
import java.util.logging.Logger;

@RequestScoped
@FacesConverter(value = "almacenConverter", managed = true)
public class AlmacenConverter implements Converter<Almacen> {

    private static final Logger LOGGER = Logger.getLogger(AlmacenConverter.class.getName());

    @Inject
    AlmacenDAO almacenDao;

    @Override
    public Almacen getAsObject(FacesContext context, UIComponent component, String value) {
        LOGGER.log(Level.INFO, "getAsObject - Recibido value: [{0}]", value);

        if (value == null || value.trim().isEmpty() || "null".equals(value)) {
            LOGGER.log(Level.INFO, "getAsObject - Retornando null (value vacío)");
            return null;
        }

        try {
            Integer id = Integer.valueOf(value.trim());
            LOGGER.log(Level.INFO, "getAsObject - Buscando Almacen con ID: {0}", id);

            Almacen almacen = almacenDao.findById(id);

            if (almacen != null) {
                LOGGER.log(Level.INFO, "getAsObject - ✓ Encontrado: {0}", almacen);
            } else {
                LOGGER.log(Level.SEVERE, "getAsObject - ✗ NO encontrado con ID: {0}", id);
            }

            return almacen;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "getAsObject - Error parseando ID: " + value, e);
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "getAsObject - Error inesperado", e);
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Almacen value) {
        if (value == null || value.getId() == null) {
            LOGGER.log(Level.INFO, "getAsString - Retornando vacío (value null)");
            return "";
        }

        String result = value.getId().toString();
        LOGGER.log(Level.INFO, "getAsString - Almacen {0} -> String [{1}]",
                new Object[]{value, result});

        return result;
    }
}