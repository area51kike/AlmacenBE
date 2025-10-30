package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.AlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoAlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Almacen;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class AlmacenFrm extends DefaultFrm<Almacen> implements Serializable {

    @Inject
    FacesContext facesContext;

    @Inject
    AlmacenDAO almacenDAO;

    @Inject
    TipoAlmacenDAO tipoAlmacenDAO;

    private List<TipoAlmacen> listaTiposAlmacen;

    public AlmacenFrm() {
        this.nombreBean = "Almacén";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected InventarioDefaultDataAccess<Almacen> getDao() {
        return almacenDAO;
    }

    @Override
    protected Almacen nuevoRegistro() {
        Almacen almacen = new Almacen();
        almacen.setActivo(true);
        return almacen;
    }

    @Override
    protected Almacen buscarRegistroPorId(Object id) {
        if (id != null && id instanceof Integer buscado) {
            try {
                // ✅ USA EL MÉTODO findById - NO cargues todos los registros
                return almacenDAO.findById(buscado);
            } catch (Exception e) {
                System.err.println("Error buscando almacén con ID: " + id);
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected String getIdAsText(Almacen r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected Almacen getIdByText(String id) {
        if (id != null && !id.trim().isEmpty()) {
            try {
                Integer buscado = Integer.parseInt(id.trim());
                // ✅ Buscar directamente en la base de datos
                return buscarRegistroPorId(buscado);
            } catch (NumberFormatException e) {
                System.err.println("ID no es un número válido: " + id);
            }
        }
        return null;
    }

    // --- SOBREESCRIBIR LA VALIDACIÓN DE NOMBRE ---
    @Override
    protected boolean esNombreVacio(Almacen registro) {
        // Como Almacen no tiene campo 'nombre', retornamos false
        return false;
    }

    // --- SOBREESCRIBIR EL MÉTODO GUARDAR PARA AGREGAR VALIDACIÓN ---
    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        // Validación personalizada antes de guardar
        if (this.registro != null && this.registro.getIdTipoAlmacen() == null) {
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error de validación",
                            "Debe seleccionar un tipo de almacén"));
            return;
        }

        // Llamar al método original
        super.btnGuardarHandler(actionEvent);
    }

    // --- CARGA DE LISTA DE TIPOS DE ALMACÉN ---
    public List<TipoAlmacen> getListaTiposAlmacen() {
        if (listaTiposAlmacen == null) {
            try {
                listaTiposAlmacen = tipoAlmacenDAO.findAll();
            } catch (Exception e) {
                System.err.println("Error cargando tipos de almacén:");
                e.printStackTrace();
                listaTiposAlmacen = List.of();
            }
        }
        return listaTiposAlmacen;
    }

    public void setListaTiposAlmacen(List<TipoAlmacen> listaTiposAlmacen) {
        this.listaTiposAlmacen = listaTiposAlmacen;
    }
}