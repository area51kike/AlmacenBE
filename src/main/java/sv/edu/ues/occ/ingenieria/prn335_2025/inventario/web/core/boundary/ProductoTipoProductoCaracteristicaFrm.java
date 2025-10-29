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

@Named
@ViewScoped
public class ProductoTipoProductoCaracteristicaFrm extends DefaultFrm<ProductoTipoProductoCaracteristica> implements Serializable {

    @EJB
    private ProductoTipoProductoCaracteristicaDAO productoTipoProductoCaracteristicaDAO;

    @EJB
    private ProductoTipoProductoDAO productoTipoProductoDAO;

    @EJB
    private TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDAO;

    private List<ProductoTipoProducto> listaProductoTipoProducto;
    private List<TipoProductoCaracteristica> listaTipoProductoCaracteristica;

    @PostConstruct
    @Override
    public void inicializar() {
        super.inicializar();
        cargarListas();
        inicializarModelo();
    }

    private void inicializarModelo() {
        this.modelo = new LazyDataModel<ProductoTipoProductoCaracteristica>() {
            @Override
            public int count(Map<String, FilterMeta> map) {
                try {
                    int count = productoTipoProductoCaracteristicaDAO.count();
                    System.out.println("Count de registros: " + count);
                    return count;
                } catch (Exception e) {
                    System.err.println("Error en count: " + e.getMessage());
                    e.printStackTrace();
                    return 0;
                }
            }

            @Override
            public List<ProductoTipoProductoCaracteristica> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                try {
                    System.out.println("load() llamado con first=" + first + ", pageSize=" + pageSize);
                    List<ProductoTipoProductoCaracteristica> registros = productoTipoProductoCaracteristicaDAO.findRange(first, pageSize);
                    System.out.println("Registros cargados: " + registros.size());

                    // Debug: mostrar los registros cargados
                    registros.forEach(r -> {
                        String nombreCaracteristica = (r.getIdTipoProductoCaracteristica() != null &&
                                r.getIdTipoProductoCaracteristica().getCaracteristica() != null) ?
                                r.getIdTipoProductoCaracteristica().getCaracteristica().getNombre() : "null";

                        System.out.println("  - ID: " + r.getId() +
                                ", PTP: " + (r.getIdProductoTipoProducto() != null ? r.getIdProductoTipoProducto().getId() : "null") +
                                ", TPC: " + nombreCaracteristica);
                    });

                    return registros;
                } catch (Exception e) {
                    System.err.println("Error al cargar registros: " + e.getMessage());
                    e.printStackTrace();
                    return List.of();
                }
            }

            @Override
            public String getRowKey(ProductoTipoProductoCaracteristica object) {
                if (object != null && object.getId() != null) {
                    return object.getId().toString();
                }
                return null;
            }

            @Override
            public ProductoTipoProductoCaracteristica getRowData(String rowKey) {
                if (rowKey != null && !rowKey.isEmpty()) {
                    try {
                        UUID id = UUID.fromString(rowKey);
                        return productoTipoProductoCaracteristicaDAO.findById(id);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Error al convertir rowKey a UUID: " + e.getMessage());
                    }
                }
                return null;
            }
        };
    }

    private void cargarListas() {
        try {
            this.listaProductoTipoProducto = productoTipoProductoDAO.findAll();
            this.listaTipoProductoCaracteristica = tipoProductoCaracteristicaDAO.findAll();

            System.out.println("ProductoTipoProducto cargados: " +
                    (listaProductoTipoProducto != null ? listaProductoTipoProducto.size() : 0));
            System.out.println("TipoProductoCaracteristica cargados: " +
                    (listaTipoProductoCaracteristica != null ? listaTipoProductoCaracteristica.size() : 0));

            if (listaProductoTipoProducto != null && !listaProductoTipoProducto.isEmpty()) {
                listaProductoTipoProducto.forEach(ptp ->
                        System.out.println("  - ProductoTipoProducto: " + ptp.getId()));
            }

            if (listaTipoProductoCaracteristica != null && !listaTipoProductoCaracteristica.isEmpty()) {
                listaTipoProductoCaracteristica.forEach(tpc -> {
                    String nombreCaracteristica = (tpc.getCaracteristica() != null) ?
                            tpc.getCaracteristica().getNombre() : "Sin nombre";
                    System.out.println("  - TipoProductoCaracteristica: " + tpc.getId() +
                            " - Característica: " + nombreCaracteristica);
                });
            }
        } catch (Exception e) {
            System.err.println("Error al cargar listas: " + e.getMessage());
            e.printStackTrace();
        }
    }
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
        System.out.println("Nuevo registro creado");
        return nuevo;
    }

    @Override
    protected ProductoTipoProductoCaracteristica buscarRegistroPorId(Object id) {
        if (id instanceof UUID) {
            return productoTipoProductoCaracteristicaDAO.findById((UUID) id);
        } else if (id instanceof String) {
            try {
                return productoTipoProductoCaracteristicaDAO.findById(UUID.fromString((String) id));
            } catch (IllegalArgumentException e) {
                System.err.println("Error al convertir String a UUID: " + e.getMessage());
            }
        }
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
                return buscarRegistroPorId(UUID.fromString(id));
            } catch (IllegalArgumentException e) {
                System.err.println("Error al convertir ID: " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected boolean esNombreVacio(ProductoTipoProductoCaracteristica registro) {
        boolean vacio = registro.getIdProductoTipoProducto() == null ||
                registro.getIdTipoProductoCaracteristica() == null;
        if (vacio) {
            System.out.println("Validación falló: ProductoTipoProducto=" +
                    registro.getIdProductoTipoProducto() +
                    ", TipoProductoCaracteristica=" +
                    registro.getIdTipoProductoCaracteristica());
        }
        return vacio;
    }

    // Getters y Setters
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