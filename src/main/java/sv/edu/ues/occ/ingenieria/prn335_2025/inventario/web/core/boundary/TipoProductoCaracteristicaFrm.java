package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
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
        super.inicializar(); // Llama al init del padre
        cargarListas();
        inicializarModelo(); // Sobrescribe el modelo con configuración específica
    }

    /**
     * Configura el LazyDataModel específico para TipoProductoCaracteristica
     * Sobrescribe el modelo genérico del padre para agregar lógica personalizada
     */
    private void inicializarModelo() {
        this.modelo = new LazyDataModel<TipoProductoCaracteristica>() {

            /**
             * ⭐ CORREGIDO: Convierte Long a int para compatibilidad con PrimeFaces
             */
            @Override
            public int count(Map<String, FilterMeta> map) {
                try {
                    // ⭐ MEJORA: count() ahora retorna Long, debemos convertirlo a int
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

            /**
             * Carga un rango de registros con información detallada de debug
             * ⭐ MEJORA: Ahora se beneficia del ordenamiento automático por ID
             */
            @Override
            public List<TipoProductoCaracteristica> load(int first, int pageSize,
                                                         Map<String, SortMeta> sortBy,
                                                         Map<String, FilterMeta> filterBy) {
                try {
                    System.out.println("🔄 load() llamado con first=" + first + ", pageSize=" + pageSize);

                    // ⭐ MEJORA: findRange ahora incluye ordenamiento automático por ID
                    List<TipoProductoCaracteristica> registros =
                            tipoProductoCaracteristicaDAO.findRange(first, pageSize);

                    System.out.println("✅ Registros cargados: " + registros.size());

                    // Debug detallado de los registros cargados
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

            /**
             * Obtiene la clave única de la fila basada en el ID
             */
            @Override
            public String getRowKey(TipoProductoCaracteristica object) {
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

            // Debug: mostrar los tipos de producto cargados
            if (listaTipoProductos != null && !listaTipoProductos.isEmpty()) {
                listaTipoProductos.forEach(tp ->
                        System.out.println("  ▪ TipoProducto: " + tp.getId() + " - " + tp.getNombre()));
            }

            // Debug: mostrar las características cargadas
            if (listaCaracteristicas != null && !listaCaracteristicas.isEmpty()) {
                listaCaracteristicas.forEach(c ->
                        System.out.println("  ▪ Caracteristica: " + c.getId() + " - " + c.getNombre()));
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
    protected TipoProductoCaracteristicaDAO getDao() {
        return tipoProductoCaracteristicaDAO;
    }

    @Override
    protected TipoProductoCaracteristica nuevoRegistro() {
        TipoProductoCaracteristica nuevo = new TipoProductoCaracteristica();
        nuevo.setFechaCreacion(OffsetDateTime.now());
        System.out.println("🆕 Nuevo registro TipoProductoCaracteristica creado");
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