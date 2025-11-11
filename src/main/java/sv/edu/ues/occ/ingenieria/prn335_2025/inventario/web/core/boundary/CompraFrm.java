package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProveedorDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Proveedor;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.EstadoCompra;

@Named("compraFrm")
@ViewScoped
public class CompraFrm extends DefaultFrm<Compra> implements Serializable {

    @Inject
    private CompraDao compraDao;

    @Inject
    private ProveedorDAO proveedorDao;

    private List<Proveedor> proveedoresDisponibles;
    private List<EstadoCompra> estadosDisponibles;

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<Compra> getDao() {
        return compraDao;
    }

    @Override
    protected Compra nuevoRegistro() {
        Compra nuevaCompra = new Compra();
        nuevaCompra.setFecha(OffsetDateTime.now()); // Establece la fecha actual
        return nuevaCompra;
    }

    @Override
    protected Compra buscarRegistroPorId(Object id) {
        if (id instanceof Long) {
            try {
                return compraDao.findById((Long) id); // Busca la compra por ID
            } catch (Exception e) {
                Logger.getLogger(CompraFrm.class.getName()).log(Level.SEVERE, "Error buscando Compra por ID", e);
            }
        }
        return null;
    }

    protected void crearEntidad(Compra entidad) throws Exception {
        // Validaciones de campos obligatorios
        if (entidad.getFecha() == null) {
            throw new Exception("La fecha es obligatoria");
        }

        if (entidad.getIdProveedor() == null) {
            throw new Exception("Debe seleccionar un proveedor");
        }

        if (entidad.getEstado() == null || entidad.getEstado().isBlank()) {
            throw new Exception("Debe seleccionar un estado");
        }

        // Verifica si el proveedor existe
        Proveedor proveedorEntity = proveedorDao.findById(entidad.getIdProveedor());
        if (proveedorEntity == null) {
            throw new Exception("El proveedor seleccionado no existe.");
        }

        // El ID será generado por el DAO automáticamente
        compraDao.crear(entidad);
    }

    @Override
    protected String getIdAsText(Compra r) {
        return (r != null && r.getId() != null) ? r.getId().toString() : null; // Convierte el ID a texto
    }

    @Override
    protected Compra getIdByText(String id) {
        try {
            return buscarRegistroPorId(Long.parseLong(id)); // Busca por el ID recibido como texto
        } catch (NumberFormatException e) {
            return null; // Retorna null si el formato no es válido
        }
    }

    @Override
    protected boolean esNombreVacio(Compra registro) {
        return registro == null || registro.getIdProveedor() == null; // Verifica si el registro es vacío
    }

    @PostConstruct
    public void init() {
        super.inicializar();
        // Inicializa las listas de proveedores y estados disponibles
        this.estadosDisponibles = Arrays.asList(EstadoCompra.values());
        this.proveedoresDisponibles = proveedorDao.findAll();
    }

    public List<Proveedor> getProveedoresDisponibles() {
        return proveedoresDisponibles; // Obtiene los proveedores disponibles
    }

    public List<EstadoCompra> getEstadosDisponibles() {
        return estadosDisponibles; // Obtiene los estados disponibles
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atención", "No hay registro para guardar"));
            return;
        }

        try {
            // Validación del proveedor
            if (esNombreVacio(this.registro)) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Atención", "Debe seleccionar un proveedor"));
                return;
            }

            // Validación del estado
            if (registro.getEstado() == null || registro.getEstado().isBlank()) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Atención", "Debe seleccionar un estado"));
                return;
            }

            // Validación de la fecha
            if (registro.getFecha() == null) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Atención", "La fecha es obligatoria"));
                return;
            }

            // Ejecuta la operación según el estado
            if (this.estado == ESTADO_CRUD.CREAR) {
                crearEntidad(this.registro); // Llama al método de creación
            } else if (this.estado == ESTADO_CRUD.MODIFICAR) {
                // Para modificación, validamos que el proveedor exista
                if (this.registro.getIdProveedor() != null) {
                    Proveedor proveedorEntity = proveedorDao.findById(this.registro.getIdProveedor());
                    if (proveedorEntity == null) {
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Error", "El proveedor seleccionado no existe."));
                        return;
                    }
                }
                getDao().modificar(this.registro); // Modifica el registro
            }

            // Limpiar y recargar después de guardar
            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            this.modelo = null;
            inicializarRegistros();

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Registro guardado correctamente"));

        } catch (Exception e) {
            Logger.getLogger(CompraFrm.class.getName()).log(Level.SEVERE, "Error al guardar compra", e);

            // Obtener la causa raíz del error
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }

            String mensajeDetallado = "Error: " + e.getMessage() +
                    "\nCausa raíz: " + causa.getClass().getName() +
                    " - " + causa.getMessage();

            Logger.getLogger(CompraFrm.class.getName()).log(Level.SEVERE, mensajeDetallado, causa);

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al guardar", causa.getMessage()));
        }
    }
}
