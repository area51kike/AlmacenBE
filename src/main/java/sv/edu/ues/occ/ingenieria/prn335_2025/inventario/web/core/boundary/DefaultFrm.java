package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase abstracta que proporciona funcionalidad base para formularios JSF con operaciones CRUD
 * Utiliza LazyDataModel de PrimeFaces para paginación eficiente
 *
 * @param <T> El tipo de entidad que será gestionada por este formulario
 */
public abstract class DefaultFrm<T> implements Serializable {

    // Estado actual del formulario (CREAR, MODIFICAR, NADA)
    ESTADO_CRUD estado = ESTADO_CRUD.NADA;

    protected String nombreBean;

    // Modelo de datos lazy para PrimeFaces DataTable
    protected LazyDataModel<T> modelo;

    // Registro actualmente seleccionado o en edición
    protected T registro;

    // Tamaño de página por defecto para la paginación
    protected int pageSize = 8;

    // =================== MÉTODOS ABSTRACTOS QUE DEBEN IMPLEMENTARSE ===================

    /**
     * Obtiene el contexto de JSF
     * @return FacesContext actual
     */
    protected abstract FacesContext getFacesContext();

    /**
     * Obtiene el DAO correspondiente para la entidad
     * @return DAO que gestiona la persistencia de la entidad
     */
    protected abstract InventarioDefaultDataAccess<T> getDao();

    /**
     * Crea una nueva instancia de la entidad
     * @return Nueva instancia de T
     */
    protected abstract T nuevoRegistro();

    /**
     * Busca un registro por su ID
     * @param id Identificador del registro
     * @return Registro encontrado o null
     */
    protected abstract T buscarRegistroPorId(Object id);

    /**
     * Convierte el ID de una entidad a String
     * @param r Entidad
     * @return ID como String
     */
    protected abstract String getIdAsText(T r);

    /**
     * Obtiene una entidad a partir de su ID en formato String
     * @param id ID en formato String
     * @return Entidad correspondiente
     */
    protected abstract T getIdByText(String id);

    // =================== INICIALIZACIÓN ===================

    @PostConstruct
    public void inicializar() {
        inicializarRegistros();
    }

    /**
     * Inicializa el LazyDataModel para la tabla de PrimeFaces
     * Implementa los métodos necesarios para paginación, búsqueda y selección
     */
    public void inicializarRegistros() {
        this.modelo = new LazyDataModel<T>() {

            /**
             * Obtiene la clave única de una fila para identificación
             * @param object Entidad de la fila
             * @return ID como String
             */
            @Override
            public String getRowKey(T object) {
                if (object != null) {
                    try {
                        String id = getIdAsText(object);
                        System.out.println("🔍 getRowKey: " + id);
                        return id;
                    } catch (Exception e) {
                        Logger.getLogger(DefaultFrm.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
                return null;
            }

            /**
             * Obtiene una entidad a partir de su clave de fila
             * @param rowKey Clave de la fila
             * @return Entidad correspondiente
             */
            @Override
            public T getRowData(String rowKey) {
                if (rowKey != null) {
                    try {
                        System.out.println("🔍 getRowData: " + rowKey);
                        return getIdByText(rowKey);
                    } catch (Exception e) {
                        Logger.getLogger(DefaultFrm.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
                return null;
            }

            /**
             * ⭐ MEJORADO: Ahora usa Long en lugar de int
             * Cuenta el total de registros en la base de datos
             * @param map Filtros aplicados (no implementado)
             * @return Cantidad total de registros
             */
            @Override
            public int count(Map<String, FilterMeta> map) {
                try {
                    // ⭐ MEJORA: count() ahora retorna Long
                    Long total = getDao().count();
                    return total.intValue();
                } catch (Exception e) {
                    Logger.getLogger(DefaultFrm.class.getName()).log(Level.SEVERE, null, e);
                }
                return 0;
            }

            /**
             * Carga un rango específico de registros para la página actual
             * @param first Índice del primer registro
             * @param max Cantidad máxima de registros a cargar
             * @param sortBy Criterios de ordenamiento (no implementado)
             * @param filterBy Filtros aplicados (no implementado)
             * @return Lista de entidades para la página actual
             */
            @Override
            public List<T> load(int first, int max, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                try {
                    System.out.println("🔍 LazyDataModel.load llamado con first=" + first + ", max=" + max);
                    // ⭐ MEJORA: findRange ahora incluye ordenamiento automático por ID
                    return getDao().findRange(first, max);
                } catch (Exception e) {
                    Logger.getLogger(DefaultFrm.class.getName()).log(Level.SEVERE, null, e);
                }
                return Collections.emptyList();
            }
        };
    }

    // =================== MANEJADORES DE EVENTOS ===================

    /**
     * Maneja la selección de una fila en la tabla
     * Cambia el estado a MODIFICAR
     * @param r Evento de selección con la entidad seleccionada
     */
    public void selectionHandler(SelectEvent<T> r) {
        if (r != null) {
            this.registro = r.getObject();
            this.estado = ESTADO_CRUD.MODIFICAR;
            System.out.println("✅ Registro seleccionado: " + this.registro);
        } else {
            System.out.println("⚠ Evento de selección nulo");
        }
    }

    /**
     * Maneja el clic en el botón "Nuevo"
     * Crea una nueva instancia y cambia el estado a CREAR
     * @param actionEvent Evento de acción
     */
    public void btnNuevoHandler(ActionEvent actionEvent) {
        this.registro = nuevoRegistro();
        this.estado = ESTADO_CRUD.CREAR;
        System.out.println("🆕 Botón Nuevo presionado, estado = " + this.estado);
    }

    /**
     * Maneja el clic en el botón "Cancelar"
     * Limpia el registro y vuelve al estado NADA
     * @param actionEvent Evento de acción
     */
    public void btnCancelarHandler(ActionEvent actionEvent) {
        this.registro = null;
        this.estado = ESTADO_CRUD.NADA;
        System.out.println("🚫 Operación cancelada");
    }

    /**
     * Maneja el clic en el botón "Guardar"
     * Valida y persiste el nuevo registro
     * @param actionEvent Evento de acción
     */
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                // Validación de nombre vacío
                if (esNombreVacio(this.registro)) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN,
                                    "Atención", "El nombre no puede estar vacío"));
                    return;
                }

                // Crear el registro en la base de datos
                getDao().crear(this.registro);

                // Limpiar formulario y recargar datos
                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                this.modelo = null;
                inicializarRegistros();

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Éxito", "Registro guardado correctamente"));

                System.out.println("✅ Registro guardado exitosamente");

            } catch (Exception e) {
                e.printStackTrace();
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error al guardar", e.getMessage()));
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atención", "No hay registro para guardar"));
        }
    }

    /**
     * Maneja el clic en el botón "Eliminar"
     * Elimina el registro seleccionado de la base de datos
     * @param actionEvent Evento de acción
     */
    public void btnEliminarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                // ⭐ MEJORA: eliminar() ahora maneja entidades detached correctamente
                getDao().eliminar(this.registro);

                // Limpiar y recargar
                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarRegistros();

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Éxito", "Registro eliminado correctamente"));

                System.out.println("🗑️ Registro eliminado exitosamente");

            } catch (Exception e) {
                System.err.println("Error en eliminación:");
                e.printStackTrace();
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error al eliminar", e.getMessage()));
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atención", "No hay registro seleccionado para eliminar"));
        }
    }

    /**
     * ⭐ MEJORADO: Ahora captura la entidad actualizada retornada por modificar()
     * Maneja el clic en el botón "Modificar"
     * Actualiza el registro seleccionado en la base de datos
     * @param actionEvent Evento de acción
     */
    public void btnModificarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                // Validación de nombre vacío
                if (esNombreVacio(this.registro)) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN,
                                    "Atención", "El nombre no puede estar vacío"));
                    return;
                }

                // ⭐ MEJORA: modificar() ahora retorna la entidad actualizada
                T actualizado = getDao().modificar(this.registro);

                // Limpiar y recargar
                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarRegistros();

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Éxito", "Registro modificado correctamente"));

                System.out.println("✏️ Registro modificado exitosamente");

            } catch (Exception e) {
                e.printStackTrace();
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error al modificar", e.getMessage()));
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atención", "No hay registro seleccionado para modificar"));
        }
    }

    // =================== MÉTODOS AUXILIARES ===================

    /**
     * Valida si el nombre de la entidad está vacío
     * Usa reflexión para obtener el método getNombre()
     * @param registro Entidad a validar
     * @return true si el nombre está vacío o null
     */
    protected boolean esNombreVacio(T registro) {
        try {
            // Usar reflexión para obtener el nombre
            java.lang.reflect.Method metodoGetNombre = registro.getClass().getMethod("getNombre");
            String nombre = (String) metodoGetNombre.invoke(registro);

            boolean vacio = nombre == null || nombre.trim().isEmpty();
            if (vacio) {
                System.out.println("⚠️ Validación: Nombre vacío detectado");
            }
            return vacio;

        } catch (Exception e) {
            System.err.println("Error validando nombre: " + e.getMessage());
            return true; // Si hay error, consideramos que está vacío por seguridad
        }
    }

    // =================== GETTERS Y SETTERS ===================

    public ESTADO_CRUD getEstado() {
        return estado;
    }

    public void setEstado(ESTADO_CRUD estado) {
        this.estado = estado;
    }

    public String getNombreBean() {
        return nombreBean;
    }

    public void setNombreBean(String nombreBean) {
        this.nombreBean = nombreBean;
    }

    public T getRegistro() {
        return registro;
    }

    public void setRegistro(T registro) {
        this.registro = registro;
    }

    public LazyDataModel<T> getModelo() {
        return modelo;
    }

    public void setModelo(LazyDataModel<T> modelo) {
        this.modelo = modelo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}