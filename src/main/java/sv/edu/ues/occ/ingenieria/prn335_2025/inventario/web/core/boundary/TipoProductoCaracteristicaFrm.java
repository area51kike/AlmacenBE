package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProductoCaracteristica;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProducto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Caracteristica;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Formulario JSF para gestionar la relación entre TipoProducto y Caracteristica
 * Permite definir qué características tiene cada tipo de producto
 */
@Named
@ViewScoped
public class TipoProductoCaracteristicaFrm extends DefaultFrm<TipoProductoCaracteristica> implements Serializable {

    @EJB
    private TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDAO;

    @EJB
    private TipoProductoDao tipoProductoDAO;

    @EJB
    private CaracteristicaDAO caracteristicaDAO;

    // Listas para los selectores del formulario
    private List<TipoProducto> listaTipoProductos;
    private List<Caracteristica> listaCaracteristicas;

    /**
     * Inicialización del formulario
     * Carga las listas necesarias y configura el modelo lazy
     */
    @PostConstruct
    @Override
    public void inicializar() {
        System.out.println("🚀 Inicializando TipoProductoCaracteristicaFrm");
        super.inicializar(); // Inicializa el modelo genérico
        cargarListas();
        inicializarModeloEspecifico(); // Sobrescribe con modelo específico
        System.out.println("✅ TipoProductoCaracteristicaFrm inicializado. Estado: " + this.estado);
    }

    /**
     * Configura el LazyDataModel específico para TipoProductoCaracteristica
     * Sobrescribe el modelo genérico del padre para agregar lógica personalizada
     */
    private void inicializarModeloEspecifico() {
        this.modelo = new LazyDataModel<TipoProductoCaracteristica>() {

            @Override
            public int count(Map<String, FilterMeta> map) {
                try {
                    Long total = tipoProductoCaracteristicaDAO.count();
                    int count = total.intValue();
                    System.out.println("📊 Count de registros: " + count);
                    return count;
                } catch (Exception e) {
                    System.err.println("❌ Error en count: " + e.getMessage());
                    e.printStackTrace();
                    return 0;
                }
            }

            @Override
            public List<TipoProductoCaracteristica> load(int first, int pageSize,
                                                         Map<String, SortMeta> sortBy,
                                                         Map<String, FilterMeta> filterBy) {
                try {
                    System.out.println("🔄 load() llamado con first=" + first + ", pageSize=" + pageSize);

                    List<TipoProductoCaracteristica> registros =
                            tipoProductoCaracteristicaDAO.findRange(first, pageSize);

                    System.out.println("✅ Registros cargados: " + registros.size());

                    registros.forEach(r -> {
                        String nombreTipo = (r.getTipoProducto() != null) ?
                                r.getTipoProducto().getNombre() : "null";
                        String nombreCaract = (r.getCaracteristica() != null) ?
                                r.getCaracteristica().getNombre() : "null";

                        System.out.println("  📝 ID: " + r.getId() +
                                ", TipoProducto: " + nombreTipo +
                                ", Caracteristica: " + nombreCaract);
                    });

                    return registros;
                } catch (Exception e) {
                    System.err.println("❌ Error al cargar registros: " + e.getMessage());
                    e.printStackTrace();
                    return List.of();
                }
            }

            @Override
            public String getRowKey(TipoProductoCaracteristica object) {
                if (object != null && object.getId() != null) {
                    String key = object.getId().toString();
                    System.out.println("🔑 getRowKey: " + key);
                    return key;
                }
                return null;
            }

            @Override
            public TipoProductoCaracteristica getRowData(String rowKey) {
                if (rowKey != null && !rowKey.isEmpty()) {
                    try {
                        Long id = Long.parseLong(rowKey);
                        TipoProductoCaracteristica encontrado =
                                tipoProductoCaracteristicaDAO.findById(id);
                        System.out.println("🔍 getRowData para " + rowKey + ": " +
                                (encontrado != null ? "encontrado" : "no encontrado"));
                        return encontrado;
                    } catch (NumberFormatException e) {
                        System.err.println("❌ Error al convertir rowKey a Long: " + e.getMessage());
                    }
                }
                return null;
            }
        };
    }

    /**
     * Carga las listas de TipoProducto y Caracteristica
     * para poblar los selectores del formulario
     */
    private void cargarListas() {
        try {
            this.listaTipoProductos = tipoProductoDAO.findAll();
            this.listaCaracteristicas = caracteristicaDAO.findAll();

            System.out.println("📋 Tipos de producto cargados: " +
                    (listaTipoProductos != null ? listaTipoProductos.size() : 0));
            System.out.println("📋 Características cargadas: " +
                    (listaCaracteristicas != null ? listaCaracteristicas.size() : 0));

            if (listaTipoProductos != null && !listaTipoProductos.isEmpty()) {
                listaTipoProductos.forEach(tp ->
                        System.out.println("  ▪ TipoProducto: " + tp.getId() + " - " + tp.getNombre()));
            }

            if (listaCaracteristicas != null && !listaCaracteristicas.isEmpty()) {
                listaCaracteristicas.forEach(c ->
                        System.out.println("  ▪ Caracteristica: " + c.getId() + " - " + c.getNombre()));
            }
        } catch (Exception e) {
            System.err.println("❌ Error al cargar listas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =================== SOBRESCRITURA DE MÉTODOS DEL PADRE ===================

    /**
     * Maneja la selección de una fila en la tabla
     * Sobrescribe el método del padre para agregar validaciones específicas
     */
    @Override
    public void selectionHandler(SelectEvent<TipoProductoCaracteristica> event) {
        System.out.println("🖱️ selectionHandler invocado");

        if (event != null && event.getObject() != null) {
            TipoProductoCaracteristica seleccionado = event.getObject();
            System.out.println("  ✅ Registro seleccionado: ID=" + seleccionado.getId());

            // Recargar desde la BD para asegurar que tenemos todas las relaciones
            this.registro = tipoProductoCaracteristicaDAO.findById(seleccionado.getId());

            if (this.registro != null) {
                this.estado = ESTADO_CRUD.MODIFICAR;
                System.out.println("  ✅ Estado cambiado a: " + this.estado);

                // Verificar que las relaciones se cargaron
                System.out.println("  📋 TipoProducto: " +
                        (this.registro.getTipoProducto() != null ?
                                this.registro.getTipoProducto().getNombre() : "null"));
                System.out.println("  📋 Caracteristica: " +
                        (this.registro.getCaracteristica() != null ?
                                this.registro.getCaracteristica().getNombre() : "null"));
            } else {
                System.err.println("  ❌ Error: No se pudo cargar el registro completo");
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "No se pudo cargar el registro seleccionado"));
            }
        } else {
            System.err.println("  ❌ Error: Event o registro es null");
        }
    }

    /**
     * Maneja el botón NUEVO
     * Sobrescribe el método del padre para asegurar que las listas estén cargadas
     */
    @Override
    public void btnNuevoHandler(ActionEvent actionEvent) {
        System.out.println("🆕 Botón NUEVO presionado");

        // Asegurar que las listas estén cargadas
        if (listaTipoProductos == null || listaTipoProductos.isEmpty() ||
                listaCaracteristicas == null || listaCaracteristicas.isEmpty()) {
            System.out.println("⚠️ Recargando listas...");
            cargarListas();
        }

        // Llamar al método del padre
        super.btnNuevoHandler(actionEvent);

        System.out.println("✅ Estado después de nuevo: " + this.estado);
        System.out.println("✅ Registro después de nuevo: " +
                (this.registro != null ? "Creado (ID: " + this.registro.getId() + ")" : "null"));
    }

    /**
     * Maneja el botón GUARDAR
     * Sobrescribe para agregar validaciones específicas
     */
    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        System.out.println("💾 Botón GUARDAR presionado");

        if (this.registro == null) {
            System.err.println("❌ Registro es null");
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "No hay registro para guardar"));
            return;
        }

        // Validar relaciones requeridas
        if (this.registro.getTipoProducto() == null) {
            System.err.println("❌ TipoProducto no seleccionado");
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención",
                            "Debe seleccionar un Tipo de Producto"));
            return;
        }

        if (this.registro.getCaracteristica() == null) {
            System.err.println("❌ Caracteristica no seleccionada");
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención",
                            "Debe seleccionar una Característica"));
            return;
        }

        // Asegurar valor por defecto de obligatorio
        if (this.registro.getObligatorio() == null) {
            this.registro.setObligatorio(false);
        }

        System.out.println("  📋 Guardando: TipoProducto=" +
                this.registro.getTipoProducto().getNombre() +
                ", Caracteristica=" + this.registro.getCaracteristica().getNombre());

        // Llamar al método del padre que hace la persistencia
        super.btnGuardarHandler(actionEvent);
    }

    /**
     * Maneja el botón MODIFICAR
     * Sobrescribe para agregar validaciones específicas
     */
    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        System.out.println("✏️ Botón MODIFICAR presionado");

        if (this.registro == null) {
            System.err.println("❌ Registro es null");
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "No hay registro para modificar"));
            return;
        }

        // Validar relaciones requeridas
        if (this.registro.getTipoProducto() == null) {
            System.err.println("❌ TipoProducto no seleccionado");
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención",
                            "Debe seleccionar un Tipo de Producto"));
            return;
        }

        if (this.registro.getCaracteristica() == null) {
            System.err.println("❌ Caracteristica no seleccionada");
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención",
                            "Debe seleccionar una Característica"));
            return;
        }

        System.out.println("  📋 Modificando: ID=" + this.registro.getId() +
                ", TipoProducto=" + this.registro.getTipoProducto().getNombre() +
                ", Caracteristica=" + this.registro.getCaracteristica().getNombre());

        // Llamar al método del padre
        super.btnModificarHandler(actionEvent);
    }

    // =================== IMPLEMENTACIÓN DE MÉTODOS ABSTRACTOS ===================

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected TipoProductoCaracteristicaDAO getDao() {
        return tipoProductoCaracteristicaDAO;
    }

    @Override
    protected TipoProductoCaracteristica nuevoRegistro() {
        TipoProductoCaracteristica nuevo = new TipoProductoCaracteristica();

        // Como tienes @GeneratedValue, no asignes ID manualmente
        // Si NO tuvieras @GeneratedValue, descomenta estas líneas:
        // Long maxId = tipoProductoCaracteristicaDAO.obtenerMaximoId();
        // nuevo.setId(maxId != null ? maxId + 1 : 1L);

        nuevo.setFechaCreacion(OffsetDateTime.now());
        nuevo.setObligatorio(false); // Valor por defecto

        System.out.println("🆕 Nuevo registro TipoProductoCaracteristica creado");
        if (nuevo.getId() != null) {
            System.out.println("   ID asignado: " + nuevo.getId());
        } else {
            System.out.println("   ID será autogenerado por la BD");
        }

        return nuevo;
    }

    @Override
    protected TipoProductoCaracteristica buscarRegistroPorId(Object id) {
        if (id instanceof Long) {
            return tipoProductoCaracteristicaDAO.findById((Long) id);
        } else if (id instanceof String) {
            try {
                Long longId = Long.parseLong((String) id);
                return tipoProductoCaracteristicaDAO.findById(longId);
            } catch (NumberFormatException e) {
                System.err.println("❌ Error al convertir String a Long: " + e.getMessage());
            }
        }
        System.err.println("⚠️ Tipo de ID no soportado: " +
                (id != null ? id.getClass().getName() : "null"));
        return null;
    }

    @Override
    protected String getIdAsText(TipoProductoCaracteristica r) {
        return r != null && r.getId() != null ? r.getId().toString() : null;
    }

    @Override
    protected TipoProductoCaracteristica getIdByText(String id) {
        if (id != null && !id.isEmpty()) {
            try {
                Long longId = Long.parseLong(id);
                return buscarRegistroPorId(longId);
            } catch (NumberFormatException e) {
                System.err.println("❌ Error al convertir ID string a Long: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Valida que el registro tenga tanto TipoProducto como Caracteristica
     * Esta entidad no tiene campo "nombre" directo, sino relaciones
     *
     * IMPORTANTE: Este método sobrescribe el del padre que busca getNombre()
     *
     * @param registro Entidad a validar
     * @return true si falta alguna de las relaciones requeridas
     */
    @Override
    protected boolean esNombreVacio(TipoProductoCaracteristica registro) {
        boolean vacio = registro.getTipoProducto() == null ||
                registro.getCaracteristica() == null;

        if (vacio) {
            System.out.println("⚠️ Validación falló: TipoProducto=" +
                    registro.getTipoProducto() +
                    ", Caracteristica=" +
                    registro.getCaracteristica());
        }
        return vacio;
    }

    // =================== GETTERS Y SETTERS ===================

    public List<TipoProducto> getListaTipoProductos() {
        return listaTipoProductos;
    }

    public void setListaTipoProductos(List<TipoProducto> listaTipoProductos) {
        this.listaTipoProductos = listaTipoProductos;
    }

    public List<Caracteristica> getListaCaracteristicas() {
        return listaCaracteristicas;
    }

    public void setListaCaracteristicas(List<Caracteristica> listaCaracteristicas) {
        this.listaCaracteristicas = listaCaracteristicas;
    }
}