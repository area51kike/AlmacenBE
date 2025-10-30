package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
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
    private ProductoTipoProductoDAO ProductoTipoProductoDAO;

    @EJB
    private TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDAO;

    // Listas para los selectores del formulario
    private List<ProductoTipoProducto> listaProductoTipoProducto;
    private List<TipoProductoCaracteristica> listaTipoProductoCaracteristica;

    /**
     * Inicialización del formulario
     * Carga las listas necesarias y configura el modelo lazy
     */
    @PostConstruct
    @Override
    public void inicializar() {
        super.inicializar(); // Llama al init del padre
        cargarListas();
        inicializarModelo(); // Sobrescribe el modelo con configuración específica
    }

    /**
     * Configura el LazyDataModel específico para ProductoTipoProductoCaracteristica
     * Sobrescribe el modelo genérico del padre para agregar lógica personalizada
     */
    private void inicializarModelo() {
        this.modelo = new LazyDataModel<ProductoTipoProductoCaracteristica>() {

            /**
             * ⭐ MEJORADO: Ahora maneja Long retornado por count()
             */
            @Override
            public int count(Map<String, FilterMeta> map) {
                try {
                    // ⭐ MEJORA: count() ahora retorna Long
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

            /**
             * Carga un rango de registros con información detallada de debug
             * ⭐ MEJORA: Ahora se beneficia del ordenamiento automático por ID
             */
            @Override
            public List<ProductoTipoProductoCaracteristica> load(int first, int pageSize,
                                                                 Map<String, SortMeta> sortBy,
                                                                 Map<String, FilterMeta> filterBy) {
                try {
                    System.out.println("🔄 load() llamado con first=" + first + ", pageSize=" + pageSize);

                    // ⭐ MEJORA: findRange ahora incluye ordenamiento automático por ID
                    List<ProductoTipoProductoCaracteristica> registros =
                            productoTipoProductoCaracteristicaDAO.findRange(first, pageSize);

                    System.out.println("✅ Registros cargados: " + registros.size());

                    // Debug detallado de los registros cargados
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

            /**
             * Obtiene la clave única de la fila basada en el UUID
             */
            @Override
            public String getRowKey(ProductoTipoProductoCaracteristica object) {
                if (object != null && object.getId() != null) {
                    String key = object.getId().toString();
                    System.out.println("🔑 getRowKey: " + key);
                    return key;
                }
                return null;
            }

            /**
             * Recupera una entidad específica por su clave de fila
             */
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
     * para poblar los selectores del formulario
     */
    private void cargarListas() {
        try {
            this.listaProductoTipoProducto = ProductoTipoProductoDAO.findAll();
            this.listaTipoProductoCaracteristica = tipoProductoCaracteristicaDAO.findAll();

            System.out.println("📋 ProductoTipoProducto cargados: " +
                    (listaProductoTipoProducto != null ? listaProductoTipoProducto.size() : 0));
            System.out.println("📋 TipoProductoCaracteristica cargados: " +
                    (listaTipoProductoCaracteristica != null ? listaTipoProductoCaracteristica.size() : 0));

            // Debug: mostrar los ProductoTipoProducto cargados
            if (listaProductoTipoProducto != null && !listaProductoTipoProducto.isEmpty()) {
                listaProductoTipoProducto.forEach(ptp ->
                        System.out.println("  ▪ ProductoTipoProducto ID: " + ptp.getId()));
            }

            // Debug: mostrar las TipoProductoCaracteristica cargadas
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
}