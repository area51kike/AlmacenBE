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

@Named
@ViewScoped
public class TipoProductoCaracteristicaFrm extends DefaultFrm<TipoProductoCaracteristica> implements Serializable {

    @EJB
    private TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDAO;

    @EJB
    private TipoProductoDao tipoProductoDAO;

    @EJB
    private CaracteristicaDAO caracteristicaDAO;

    private List<TipoProducto> listaTipoProductos;
    private List<Caracteristica> listaCaracteristicas;

    @PostConstruct
    @Override
    public void inicializar() {
        super.inicializar();
        cargarListas();
        inicializarModelo();
    }

    private void inicializarModelo() {
        this.modelo = new LazyDataModel<TipoProductoCaracteristica>() {
            @Override
            public int count(Map<String, FilterMeta> map) {
                try {
                    int count = tipoProductoCaracteristicaDAO.count();
                    System.out.println("Count de registros: " + count);
                    return count;
                } catch (Exception e) {
                    System.err.println("Error en count: " + e.getMessage());
                    e.printStackTrace();
                    return 0;
                }
            }

            @Override
            public List<TipoProductoCaracteristica> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                try {
                    List<TipoProductoCaracteristica> registros = tipoProductoCaracteristicaDAO.findRange(first, pageSize);
                    System.out.println("Registros cargados: " + registros.size());
                    return registros;
                } catch (Exception e) {
                    System.err.println("Error al cargar registros: " + e.getMessage());
                    e.printStackTrace();
                    return List.of();
                }
            }

            @Override
            public String getRowKey(TipoProductoCaracteristica object) {
                if (object != null && object.getId() != null) {
                    return object.getId().toString();
                }
                return null;
            }

            @Override
            public TipoProductoCaracteristica getRowData(String rowKey) {
                if (rowKey != null && !rowKey.isEmpty()) {
                    try {
                        Long id = Long.parseLong(rowKey);
                        return tipoProductoCaracteristicaDAO.findById(id);
                    } catch (NumberFormatException e) {
                        System.err.println("Error al convertir rowKey: " + e.getMessage());
                    }
                }
                return null;
            }
        };
    }

    private void cargarListas() {
        try {
            this.listaTipoProductos = tipoProductoDAO.findAll();
            this.listaCaracteristicas = caracteristicaDAO.findAll();
            System.out.println("Tipos de producto cargados: " + (listaTipoProductos != null ? listaTipoProductos.size() : 0));
            System.out.println("Características cargadas: " + (listaCaracteristicas != null ? listaCaracteristicas.size() : 0));

            if (listaTipoProductos != null && !listaTipoProductos.isEmpty()) {
                listaTipoProductos.forEach(tp ->
                        System.out.println("  - TipoProducto: " + tp.getId() + " - " + tp.getNombre()));
            }

            if (listaCaracteristicas != null && !listaCaracteristicas.isEmpty()) {
                listaCaracteristicas.forEach(c ->
                        System.out.println("  - Caracteristica: " + c.getId() + " - " + c.getNombre()));
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
    protected TipoProductoCaracteristicaDAO getDao() {
        return tipoProductoCaracteristicaDAO;
    }

    @Override
    protected TipoProductoCaracteristica nuevoRegistro() {
        TipoProductoCaracteristica nuevo = new TipoProductoCaracteristica();
        nuevo.setFechaCreacion(OffsetDateTime.now());
        System.out.println("Nuevo registro creado");
        return nuevo;
    }

    @Override
    protected TipoProductoCaracteristica buscarRegistroPorId(Object id) {
        if (id instanceof Long) {
            return tipoProductoCaracteristicaDAO.findById((Long) id);
        }
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
                return buscarRegistroPorId(Long.parseLong(id));
            } catch (NumberFormatException e) {
                System.err.println("Error al convertir ID: " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected boolean esNombreVacio(TipoProductoCaracteristica registro) {
        boolean vacio = registro.getTipoProducto() == null || registro.getCaracteristica() == null;
        if (vacio) {
            System.out.println("Validación falló: TipoProducto=" + registro.getTipoProducto() +
                    ", Caracteristica=" + registro.getCaracteristica());
        }
        return vacio;
    }

    // Getters y Setters
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