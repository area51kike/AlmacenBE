package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProducto;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@FacesConverter(value = "productoTipoProductoConverter", managed = true)
public class ProductoTipoProductoConverter implements Converter<ProductoTipoProducto> {

    private static final Logger LOGGER = Logger.getLogger(ProductoTipoProductoConverter.class.getName());

    @Inject
    private ProductoTipoProductoDAO ProductoTipoProductoDAO;

    @Override
    public ProductoTipoProducto getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            UUID id = UUID.fromString(value);
            ProductoTipoProducto result = ProductoTipoProductoDAO.find(id);

            if (result == null) {
                LOGGER.log(Level.WARNING, "No se encontró ProductoTipoProducto con ID: {0}", id);
            }

            return result;
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "UUID inválido: " + value, e);
            throw new ConverterException("UUID inválido: " + value, e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al convertir ProductoTipoProducto", e);
            throw new ConverterException("Error al buscar ProductoTipoProducto", e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ProductoTipoProducto value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return value.getId().toString();
    }
}