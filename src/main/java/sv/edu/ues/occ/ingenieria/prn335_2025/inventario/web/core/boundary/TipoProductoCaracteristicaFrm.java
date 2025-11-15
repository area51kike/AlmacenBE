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
    private Long idTipoProductoSeleccionado;
    private Integer idCaracteristicaSeleccionada;
    public Long getIdTipoProductoSeleccionado() {
        return idTipoProductoSeleccionado;
    }
    public void setIdTipoProductoSeleccionado(Long idTipoProductoSeleccionado) {
        this.idTipoProductoSeleccionado = idTipoProductoSeleccionado;
    }

    public Integer getIdCaracteristicaSeleccionada() {
        return idCaracteristicaSeleccionada;
    }
    public void setIdCaracteristicaSeleccionada(Integer idCaracteristicaSeleccionada) {
        this.idCaracteristicaSeleccionada = idCaracteristicaSeleccionada;
    }


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
        if (event != null && event.getObject() != null) {
            this.registro = event.getObject();
            this.estado = ESTADO_CRUD.MODIFICAR;

            // Sincronizar auxiliares para que los combos muestren el valor actual
            if (registro.getTipoProducto() != null) {
                this.idTipoProductoSeleccionado = registro.getTipoProducto().getId();
            }
            if (registro.getCaracteristica() != null) {
                this.idCaracteristicaSeleccionada = registro.getCaracteristica().getId();
            }
        }
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                // Validación: asegurarse que se seleccionó TipoProducto y Caracteristica
                if (idTipoProductoSeleccionado == null || idCaracteristicaSeleccionada == null) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención",
                                    "Debe seleccionar Tipo de Producto y Característica"));
                    return;
                }

                // Resolver relaciones
                TipoProducto tipo = tipoProductoDAO.findById(idTipoProductoSeleccionado);
                Caracteristica caract = caracteristicaDAO.findById(idCaracteristicaSeleccionada);

                registro.setTipoProducto(tipo);
                registro.setCaracteristica(caract);

                if (estado == ESTADO_CRUD.CREAR) {
                    getDao().crear(registro);
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                                    "Registro creado correctamente"));
                } else if (estado == ESTADO_CRUD.MODIFICAR) {
                    getDao().modificar(registro);
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                                    "Registro modificado correctamente"));
                }

                // Resetear estado
                registro = null;
                estado = ESTADO_CRUD.NADA;
                inicializarRegistros();
                idTipoProductoSeleccionado = null;
                idCaracteristicaSeleccionada = null;

            } catch (Exception e) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", e.getMessage()));
                e.printStackTrace();
            }
        }
    }

    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                // Validación: asegurarse que se seleccionó TipoProducto y Caracteristica
                if (idTipoProductoSeleccionado == null || idCaracteristicaSeleccionada == null) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención",
                                    "Debe seleccionar Tipo de Producto y Característica"));
                    return;
                }

                // Resolver relaciones
                TipoProducto tipo = tipoProductoDAO.findById(idTipoProductoSeleccionado);
                Caracteristica caract = caracteristicaDAO.findById(idCaracteristicaSeleccionada);

                registro.setTipoProducto(tipo);
                registro.setCaracteristica(caract);

                // Llamar al DAO para modificar
                getDao().modificar(this.registro);

                // Resetear estado y refrescar modelo
                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarRegistros();
                this.idTipoProductoSeleccionado = null;
                this.idCaracteristicaSeleccionada = null;

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                                "Registro modificado correctamente"));

            } catch (Exception e) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
                e.printStackTrace();
            }
        }
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

        // Obtener el máximo ID actual y sumarle 1
        Long maxId = tipoProductoCaracteristicaDAO.obtenerMaximoId();
        nuevo.setId(maxId + 1);

        nuevo.setFechaCreacion(OffsetDateTime.now());
        nuevo.setObligatorio(false);

        System.out.println("🆕 Nuevo registro TipoProductoCaracteristica creado con ID: " + nuevo.getId());
        return nuevo;
    }


    /**
     * Método específico para crear un registro de TipoProductoCaracteristica
     * Sobrescribe la lógica genérica del DefaultDAO.
     */
    public void crear(TipoProductoCaracteristica registro) {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }
        try {

            // Usar el DAO específico
            tipoProductoCaracteristicaDAO.crear(registro);

            System.out.println("✅ Registro creado correctamente con ID autogenerado");
        } catch (Exception e) {
            System.err.println("❌ Error al crear registro: " + e.getMessage());
            throw new RuntimeException("Error al crear TipoProductoCaracteristica", e);
        }
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
    @Override
    public void btnNuevoHandler(ActionEvent actionEvent) {
        super.btnNuevoHandler(actionEvent);

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