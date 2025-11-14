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
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoTipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProductoCaracteristica;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProducto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProductoCaracteristica;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Formulario JSF para gestionar la relación entre ProductoTipoProducto y TipoProductoCaracteristica
 * Permite asignar características específicas a tipos de producto
 */
@Named
@ViewScoped
public class ProductoTipoProductoCaracteristicaFrm extends DefaultFrm<ProductoTipoProductoCaracteristica> implements Serializable {

    @EJB
    private ProductoTipoProductoCaracteristicaDAO productoTipoProductoCaracteristicaDAO;

    @EJB
    private ProductoTipoProductoDAO productoTipoProductoDAO;

    @EJB
    private TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDAO;

    // Listas para los selectores del formulario
    private List<ProductoTipoProducto> listaProductoTipoProducto;
    private List<TipoProductoCaracteristica> listaTipoProductoCaracteristica;

    // ⭐ IDs para los selectores (sin converters)
    private UUID idProductoTipoProductoSeleccionado;  // ProductoTipoProducto usa UUID
    private Long idTipoProductoCaracteristicaSeleccionado;  // TipoProductoCaracteristica usa Long

    /**
     * Inicialización del formulario
     * Carga las listas necesarias y configura el modelo lazy
     */
    @PostConstruct
    @Override
    public void inicializar() {
        super.inicializar();
        cargarListas();
        inicializarModelo();
    }

    /**
     * Configura el LazyDataModel específico para ProductoTipoProductoCaracteristica
     */
    private void inicializarModelo() {
        this.modelo = new LazyDataModel<ProductoTipoProductoCaracteristica>() {

            @Override
            public int count(Map<String, FilterMeta> map) {
                try {
                    Long total = productoTipoProductoCaracteristicaDAO.count();
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
            public List<ProductoTipoProductoCaracteristica> load(int first, int pageSize,
                                                                 Map<String, SortMeta> sortBy,
                                                                 Map<String, FilterMeta> filterBy) {
                try {
                    System.out.println("🔄 load() llamado con first=" + first + ", pageSize=" + pageSize);

                    List<ProductoTipoProductoCaracteristica> registros =
                            productoTipoProductoCaracteristicaDAO.findRange(first, pageSize);

                    System.out.println("✅ Registros cargados: " + registros.size());

                    registros.forEach(r -> {
                        String nombreCaracteristica = (r.getIdTipoProductoCaracteristica() != null &&
                                r.getIdTipoProductoCaracteristica().getCaracteristica() != null) ?
                                r.getIdTipoProductoCaracteristica().getCaracteristica().getNombre() : "null";

                        System.out.println("  📝 ID: " + r.getId() +
                                ", PTP: " + (r.getIdProductoTipoProducto() != null ?
                                r.getIdProductoTipoProducto().getId() : "null") +
                                ", TPC: " + nombreCaracteristica);
                    });

                    return registros;
                } catch (Exception e) {
                    System.err.println("❌ Error al cargar registros: " + e.getMessage());
                    e.printStackTrace();
                    return List.of();
                }
            }

            @Override
            public String getRowKey(ProductoTipoProductoCaracteristica object) {
                if (object != null && object.getId() != null) {
                    String key = object.getId().toString();
                    System.out.println("🔑 getRowKey: " + key);
                    return key;
                }
                return null;
            }

            @Override
            public ProductoTipoProductoCaracteristica getRowData(String rowKey) {
                if (rowKey != null && !rowKey.isEmpty()) {
                    try {
                        UUID id = UUID.fromString(rowKey);
                        ProductoTipoProductoCaracteristica encontrado =
                                productoTipoProductoCaracteristicaDAO.findById(id);
                        System.out.println("🔍 getRowData para " + rowKey + ": " +
                                (encontrado != null ? "encontrado" : "no encontrado"));
                        return encontrado;
                    } catch (IllegalArgumentException e) {
                        System.err.println("❌ Error al convertir rowKey a UUID: " + e.getMessage());
                    }
                }
                return null;
            }
        };
    }

    /**
     * Carga las listas de ProductoTipoProducto y TipoProductoCaracteristica
     */
    private void cargarListas() {
        try {
            this.listaProductoTipoProducto = productoTipoProductoDAO.findAll();
            this.listaTipoProductoCaracteristica = tipoProductoCaracteristicaDAO.findAll();

            System.out.println("📋 ProductoTipoProducto cargados: " +
                    (listaProductoTipoProducto != null ? listaProductoTipoProducto.size() : 0));
            System.out.println("📋 TipoProductoCaracteristica cargados: " +
                    (listaTipoProductoCaracteristica != null ? listaTipoProductoCaracteristica.size() : 0));

            if (listaProductoTipoProducto != null && !listaProductoTipoProducto.isEmpty()) {
                listaProductoTipoProducto.forEach(ptp ->
                        System.out.println("  ▪ ProductoTipoProducto ID: " + ptp.getId()));
            }

            if (listaTipoProductoCaracteristica != null && !listaTipoProductoCaracteristica.isEmpty()) {
                listaTipoProductoCaracteristica.forEach(tpc -> {
                    String nombreCaracteristica = (tpc.getCaracteristica() != null) ?
                            tpc.getCaracteristica().getNombre() : "Sin nombre";
                    System.out.println("  ▪ TipoProductoCaracteristica ID: " + tpc.getId() +
                            " - Característica: " + nombreCaracteristica);
                });
            }
        } catch (Exception e) {
            System.err.println("❌ Error al cargar listas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ⭐ Sincroniza los IDs seleccionados con las entidades del registro
     * ProductoTipoProducto usa UUID, TipoProductoCaracteristica usa Long
     */
    private void sincronizarSelecciones() {
        if (registro != null) {
            // Sincronizar ProductoTipoProducto (UUID)
            if (registro.getIdProductoTipoProducto() != null) {
                idProductoTipoProductoSeleccionado = registro.getIdProductoTipoProducto().getId(); // UUID
            } else {
                idProductoTipoProductoSeleccionado = null;
            }

            // Sincronizar TipoProductoCaracteristica (Long)
            if (registro.getIdTipoProductoCaracteristica() != null) {
                idTipoProductoCaracteristicaSeleccionado = registro.getIdTipoProductoCaracteristica().getId(); // Long
            } else {
                idTipoProductoCaracteristicaSeleccionado = null;
            }
        }
    }

    /**
     * ⭐ OVERRIDE CORREGIDO: Maneja la selección de fila con la firma correcta
     */
    @Override
    public void selectionHandler(SelectEvent<ProductoTipoProductoCaracteristica> event) {
        super.selectionHandler(event);
        sincronizarSelecciones();
    }

    /**
     * ⭐ OVERRIDE CORREGIDO: Maneja el botón Nuevo con la firma correcta
     */
    @Override
    public void btnNuevoHandler(ActionEvent event) {
        super.btnNuevoHandler(event);
        idProductoTipoProductoSeleccionado = null;
        idTipoProductoCaracteristicaSeleccionado = null;
    }

    /**
     * ⭐ OVERRIDE CORREGIDO: Maneja el botón Guardar con la firma correcta
     * Convierte los UUIDs seleccionados a entidades antes de guardar
     */
    @Override
    public void btnGuardarHandler(ActionEvent event) {
        try {
            // Convertir UUIDs a entidades antes de guardar
            if (idProductoTipoProductoSeleccionado != null) {
                ProductoTipoProducto ptp = productoTipoProductoDAO.findById(idProductoTipoProductoSeleccionado);
                if (ptp != null) {
                    registro.setIdProductoTipoProducto(ptp);
                    System.out.println("✅ ProductoTipoProducto asignado: " + ptp.getId());
                } else {
                    System.err.println("⚠️ ProductoTipoProducto no encontrado para UUID: " + idProductoTipoProductoSeleccionado);
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "ProductoTipoProducto no encontrado"));
                    return;
                }
            }

            if (idTipoProductoCaracteristicaSeleccionado != null) {
                TipoProductoCaracteristica tpc = tipoProductoCaracteristicaDAO.findById(idTipoProductoCaracteristicaSeleccionado);
                if (tpc != null) {
                    registro.setIdTipoProductoCaracteristica(tpc);
                    String nombreCaracteristica = (tpc.getCaracteristica() != null) ?
                            tpc.getCaracteristica().getNombre() : "Sin nombre";
                    System.out.println("✅ TipoProductoCaracteristica asignado: " + tpc.getId() + " - " + nombreCaracteristica);
                } else {
                    System.err.println("⚠️ TipoProductoCaracteristica no encontrado para UUID: " + idTipoProductoCaracteristicaSeleccionado);
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "TipoProductoCaracteristica no encontrado"));
                    return;
                }
            }

            // Llamar al método padre para continuar con el guardado
            super.btnGuardarHandler(event);
        } catch (Exception e) {
            System.err.println("❌ Error al guardar: " + e.getMessage());
            e.printStackTrace();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al guardar el registro: " + e.getMessage()));
        }
    }

    // =================== IMPLEMENTACIÓN DE MÉTODOS ABSTRACTOS ===================

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected ProductoTipoProductoCaracteristicaDAO getDao() {
        return productoTipoProductoCaracteristicaDAO;
    }

    @Override
    protected ProductoTipoProductoCaracteristica nuevoRegistro() {
        ProductoTipoProductoCaracteristica nuevo = new ProductoTipoProductoCaracteristica();
        System.out.println("🆕 Nuevo registro ProductoTipoProductoCaracteristica creado");
        return nuevo;
    }

    @Override
    protected ProductoTipoProductoCaracteristica buscarRegistroPorId(Object id) {
        if (id instanceof UUID) {
            return productoTipoProductoCaracteristicaDAO.findById((UUID) id);
        } else if (id instanceof String) {
            try {
                UUID uuid = UUID.fromString((String) id);
                return productoTipoProductoCaracteristicaDAO.findById(uuid);
            } catch (IllegalArgumentException e) {
                System.err.println("❌ Error al convertir String a UUID: " + e.getMessage());
            }
        }
        System.err.println("⚠️ Tipo de ID no soportado: " +
                (id != null ? id.getClass().getName() : "null"));
        return null;
    }

    @Override
    protected String getIdAsText(ProductoTipoProductoCaracteristica r) {
        return r != null && r.getId() != null ? r.getId().toString() : null;
    }

    @Override
    protected ProductoTipoProductoCaracteristica getIdByText(String id) {
        if (id != null && !id.isEmpty()) {
            try {
                UUID uuid = UUID.fromString(id);
                return buscarRegistroPorId(uuid);
            } catch (IllegalArgumentException e) {
                System.err.println("❌ Error al convertir ID string a UUID: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Valida que el registro tenga tanto ProductoTipoProducto como TipoProductoCaracteristica
     * Esta entidad no tiene campo "nombre" directo, sino relaciones
     *
     * @param registro Entidad a validar
     * @return true si falta alguna de las relaciones requeridas
     */
    @Override
    protected boolean esNombreVacio(ProductoTipoProductoCaracteristica registro) {
        boolean vacio = registro.getIdProductoTipoProducto() == null ||
                registro.getIdTipoProductoCaracteristica() == null;

        if (vacio) {
            System.out.println("⚠️ Validación falló: ProductoTipoProducto=" +
                    registro.getIdProductoTipoProducto() +
                    ", TipoProductoCaracteristica=" +
                    registro.getIdTipoProductoCaracteristica());
        }
        return vacio;
    }

    // =================== GETTERS Y SETTERS ===================

    public List<ProductoTipoProducto> getListaProductoTipoProducto() {
        return listaProductoTipoProducto;
    }

    public void setListaProductoTipoProducto(List<ProductoTipoProducto> listaProductoTipoProducto) {
        this.listaProductoTipoProducto = listaProductoTipoProducto;
    }

    public List<TipoProductoCaracteristica> getListaTipoProductoCaracteristica() {
        return listaTipoProductoCaracteristica;
    }

    public void setListaTipoProductoCaracteristica(List<TipoProductoCaracteristica> listaTipoProductoCaracteristica) {
        this.listaTipoProductoCaracteristica = listaTipoProductoCaracteristica;
    }

    public UUID getIdProductoTipoProductoSeleccionado() {
        return idProductoTipoProductoSeleccionado;
    }

    public void setIdProductoTipoProductoSeleccionado(UUID idProductoTipoProductoSeleccionado) {
        this.idProductoTipoProductoSeleccionado = idProductoTipoProductoSeleccionado;
    }

    public Long getIdTipoProductoCaracteristicaSeleccionado() {
        return idTipoProductoCaracteristicaSeleccionado;
    }

    public void setIdTipoProductoCaracteristicaSeleccionado(Long idTipoProductoCaracteristicaSeleccionado) {
        this.idTipoProductoCaracteristicaSeleccionado = idTipoProductoCaracteristicaSeleccionado;
    }
}