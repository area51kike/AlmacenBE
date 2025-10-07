package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoAlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class TipoAlmacenFrm implements Serializable {

    @Inject
    TipoAlmacenDAO taDao;

    private List<TipoAlmacen> listaTipoAlmacen;
    private String nombreBean = "Tipo de Almacén";
    private TipoAlmacen registro = new TipoAlmacen(); // ✅ INICIALIZADO

    @PostConstruct
    public void inicializar() {
        try {
            listaTipoAlmacen = taDao.findRange(0, Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al cargar datos", e.getMessage()));
        }
    }

    public void btnGuardarHandler(ActionEvent event) {
        try {
            taDao.crear(registro);
            // Limpiar formulario después de guardar
            registro = new TipoAlmacen();
            // Recargar lista
            listaTipoAlmacen = taDao.findRange(0, Integer.MAX_VALUE);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Registro guardado correctamente"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al guardar", e.getMessage()));
        }
    }

    public void btnModificarHandler(ActionEvent event) {
        if (this.registro == null || this.registro.getId() == null) {
            // Usar FacesMessage en lugar de ValidatorException
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No hay registro seleccionado para modificar"));
            return;
        }
        try {
            taDao.modificar(registro); // Cambiado a modificar
            registro = new TipoAlmacen();
            listaTipoAlmacen = taDao.findRange(0, Integer.MAX_VALUE);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Registro modificado correctamente"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al modificar", e.getMessage()));
        }
    }

    public void validarNombre(FacesContext facesContext, UIComponent uiComponent, Object nombre) {
        if (nombre == null || nombre.toString().isEmpty()) {
            throw new ValidatorException(
                    new FacesMessage("El nombre no puede estar vacío"));
        }
        String nom = nombre.toString().trim();
        if (nom.length() < 1 || nom.length() > 155) {
            throw new ValidatorException(
                    new FacesMessage("El nombre debe tener entre 1 y 155 caracteres"));
        }
    }

    // Getters y Setters
    public String getNombreBean() {
        return nombreBean;
    }

    public void setNombreBean(String nombreBean) {
        this.nombreBean = nombreBean;
    }

    public List<TipoAlmacen> getListaTipoAlmacen() {
        return listaTipoAlmacen;
    }

    public void setListaTipoAlmacen(List<TipoAlmacen> listaTipoAlmacen) {
        this.listaTipoAlmacen = listaTipoAlmacen;
    }

    public TipoAlmacen getRegistro() {
        return registro;
    }

    public void setRegistro(TipoAlmacen registro) {
        this.registro = registro;
    }
}