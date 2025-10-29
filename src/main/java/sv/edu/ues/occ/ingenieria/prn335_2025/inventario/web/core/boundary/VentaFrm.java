package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent; // Necesario para btnGuardarHandler
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ClienteDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Venta;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Cliente;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.VentaDetalle;

@Named("ventaFrm") // Nombre para usar en Venta.xhtml
@ViewScoped
public class VentaFrm extends DefaultFrm<Venta> implements Serializable {

    @Inject
    private VentaDao ventaDao;

    @Inject
    private ClienteDAO clienteDao; // DAO para cargar la lista de clientes

    private List<Cliente> clientesDisponibles;

    // Lista de estados disponibles para el SelectOneMenu
    private final List<String> estadosDisponibles = List.of("CREADA", "PROCESO", "FINALIZADA", "ANULADA");

    @Override
    public void inicializar() {
        super.inicializar(); // Inicializa el LazyDataModel
        cargarClientes();
        this.nombreBean = "Gestión de Ventas";
        System.out.println("🧠 Bean VentaFrm creado");

    }

    private void cargarClientes() {
        this.clientesDisponibles = clienteDao.findAll();
        System.out.println("🧠 Clientes cargados: " + clientesDisponibles.size());
    }

    // --- Implementación de Métodos Abstractos ---

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<Venta> getDao() {
        return ventaDao;
    }

    protected Venta nuevoRegistro() {
        Venta v = new Venta();
        v.setId(UUID.randomUUID());

        // ✅ Inicializar cliente para evitar errores en el selectOneMenu
        v.setIdCliente(new Cliente());

        // ✅ Inicializar estado si usás un enum o lista de estados
        v.setEstado(null); // o EstadoVenta.PENDIENTE si tenés un valor por defecto

        // ✅ Inicializar fecha para evitar errores en el calendario
        v.setFecha(OffsetDateTime.now()); // o LocalDateTime si usás eso

        // ✅ Inicializar observaciones para evitar null en el textarea
        v.setObservaciones("");

        return v;
    }


    @Override
    protected Venta buscarRegistroPorId(Object id) {
        return null; // El LazyDataModel lo maneja.
    }

    @Override
    protected String getIdAsText(Venta r) {
        return r != null && r.getId() != null ? r.getId().toString() : null;
    }

    @Override
    protected Venta getIdByText(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        try {
            UUID uuid = UUID.fromString(id);
            return ventaDao.findById(uuid); // ✅ Retorna el objeto real desde la base
        } catch (IllegalArgumentException e) {
            System.err.println("Error al parsear UUID: " + e.getMessage());
            return null;
        }
    }


    @Override
    protected boolean esNombreVacio(Venta registro) {
        // 🛑 CORRECCIÓN CLAVE 2: Tu validación
        // Ahora funciona porque nuevoRegistro() inicializó la entidad Cliente
        if (registro == null || registro.getIdCliente() == null || registro.getIdCliente().getId() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un cliente."));
            return true;
        }
        return false;
    }

    // --- Manejadores de Botones ---

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                // 1. Validar
                if (esNombreVacio(this.registro)) {
                    return;
                }

                // 🛑 CORRECCIÓN CLAVE 3: Sincronizar la Entidad COMPLETA
                // Obtener el ID que JSF seteo en la entidad Cliente vacía
                UUID idClienteSeleccionado = this.registro.getIdCliente().getId();

                // Buscar la entidad Cliente completa
                Cliente clienteEntidad = clienteDao.findById(idClienteSeleccionado);

                // Asignar la entidad completa al registro de Venta antes de guardar.
                this.registro.setIdCliente(clienteEntidad);

                // 2. Persistir
                getDao().crear(this.registro);

                // 3. Limpieza y Notificación
                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarRegistros();
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro guardado correctamente"));
            } catch (Exception e) {
                e.printStackTrace();
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", e.getMessage()));
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "No hay registro para guardar"));
        }
    }

    // El resto de manejadores de botones (btnEliminarHandler, btnModificarHandler, etc.)
    // se heredan correctamente de DefaultFrm.

    // --- Getters para JSF ---

    public List<Cliente> getClientesDisponibles() {
        if (clientesDisponibles == null || clientesDisponibles.isEmpty()) {
            cargarClientes(); // respaldo por si no se cargó en inicializar()
        }
        return clientesDisponibles;
    }


    public List<String> getEstadosDisponibles() {
        return estadosDisponibles;
    }

}
