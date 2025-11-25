package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.NotificadorKardex;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ClienteDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Venta;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Cliente;

@Named("ventaFrm")
@ViewScoped
public class VentaFrm extends DefaultFrm<Venta> implements Serializable {

    @Inject
    private VentaDAO ventaDao;

    @Inject
    private ClienteDAO clienteDao;

    @Inject
    private NotificadorKardex notificadorKardex;

    private List<Cliente> clientesDisponibles;

    private final List<String> estadosDisponibles = List.of("CREADA", "PROCESO", "FINALIZADA", "ANULADA");

    @Override
    public void inicializar() {
        System.out.println("Iniciando VentaFrm...");
        super.inicializar();
        cargarClientes();
        this.nombreBean = "Gestion de Ventas";
        System.out.println("Bean VentaFrm creado - Modelo: " + (modelo != null ? "OK" : "NULL"));
    }

    private void cargarClientes() {
        try {
            this.clientesDisponibles = clienteDao.findAll();
            System.out.println("Clientes cargados: " + clientesDisponibles.size());
        } catch (Exception e) {
            System.err.println("Error al cargar clientes: " + e.getMessage());
            e.printStackTrace();
            this.clientesDisponibles = List.of();
        }
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<Venta> getDao() {
        return ventaDao;
    }

    @Override
    protected Venta nuevoRegistro() {
        System.out.println("Creando nuevo registro Venta");
        Venta v = new Venta();
        v.setId(UUID.randomUUID());
        v.setIdCliente(new Cliente());
        v.setEstado("CREADA");
        v.setFecha(OffsetDateTime.now());
        v.setObservaciones("");
        return v;
    }

    @Override
    protected Venta buscarRegistroPorId(Object id) {
        try {
            if (id != null) {
                UUID uuid = UUID.fromString(id.toString());
                return ventaDao.findById(uuid);
            }
        } catch (Exception e) {
            System.err.println("Error en buscarRegistroPorId: " + e.getMessage());
        }
        return null;
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
            return ventaDao.findById(uuid);
        } catch (IllegalArgumentException e) {
            System.err.println("Error al parsear UUID: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected boolean esNombreVacio(Venta registro) {
        if (registro == null || registro.getIdCliente() == null || registro.getIdCliente().getId() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atencion", "Debe seleccionar un cliente."));
            return true;
        }
        if (registro.getEstado() == null || registro.getEstado().isEmpty()) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atencion", "Debe seleccionar un estado."));
            return true;
        }
        if (registro.getFecha() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atencion", "Debe seleccionar una fecha."));
            return true;
        }
        return false;
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        System.out.println("Intentando guardar venta...");
        if (this.registro != null) {
            try {
                if (esNombreVacio(this.registro)) {
                    System.out.println("Validacion fallida");
                    return;
                }

                UUID idClienteSeleccionado = this.registro.getIdCliente().getId();
                Cliente clienteEntidad = clienteDao.findById(idClienteSeleccionado);

                if (clienteEntidad == null) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Cliente no encontrado"));
                    return;
                }

                this.registro.setIdCliente(clienteEntidad);
                System.out.println("Cliente sincronizado: " + clienteEntidad.getNombre());

                getDao().crear(this.registro);
                System.out.println("Venta guardada con ID: " + this.registro.getId());

                try {
                    notificadorKardex.notificarCambio("NUEVA_VENTA:" + this.registro.getId());
                    System.out.println("Notificacion JMS enviada para venta: " + this.registro.getId());
                } catch (Exception e) {
                    System.err.println("Error al enviar notificacion JMS (no critico): " + e.getMessage());
                }

                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                this.modelo = null;
                inicializarRegistros();

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito", "Registro guardado correctamente"));
            } catch (Exception e) {
                System.err.println("Error al guardar: " + e.getMessage());
                e.printStackTrace();
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", e.getMessage()));
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atencion", "No hay registro para guardar"));
        }
    }

    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        super.btnModificarHandler(actionEvent);

        if (this.registro != null && this.registro.getId() != null) {
            try {
                notificadorKardex.notificarCambio("VENTA_MODIFICADA:" + this.registro.getId());
                System.out.println("Notificacion JMS enviada para modificacion de venta: " + this.registro.getId());
            } catch (Exception e) {
                System.err.println("Error al enviar notificacion JMS (no critico): " + e.getMessage());
            }
        }
    }

    public void btnCerrarVentaHandler(ActionEvent actionEvent) {
        if (this.registro == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atencion", "No hay registro seleccionado"));
            return;
        }

        try {
            System.out.println("Cerrando venta: " + this.registro.getId());

            this.registro.setEstado("FINALIZADA");

            if (this.registro.getIdCliente() != null && this.registro.getIdCliente().getId() != null) {
                Cliente clienteCompleto = clienteDao.findById(this.registro.getIdCliente().getId());
                this.registro.setIdCliente(clienteCompleto);
            }

            getDao().modificar(this.registro);

            System.out.println("Venta finalizada correctamente");

            try {
                notificadorKardex.notificarCambio("VENTA_CERRADA:" + this.registro.getId());
                System.out.println("Notificacion JMS enviada para cierre de venta");
            } catch (Exception e) {
                System.err.println("Error al enviar notificacion JMS (no critico): " + e.getMessage());
            }

            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            this.modelo = null;
            inicializarRegistros();

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Exito", "Venta finalizada correctamente"));

        } catch (Exception e) {
            System.err.println("Error al finalizar venta: " + e.getMessage());
            e.printStackTrace();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo finalizar la venta: " + e.getMessage()));
        }
    }

    public void btnAnularVentaHandler(ActionEvent actionEvent) {
        if (this.registro == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atencion", "No hay registro seleccionado"));
            return;
        }

        try {
            System.out.println("Anulando venta: " + this.registro.getId());

            this.registro.setEstado("ANULADA");

            if (this.registro.getIdCliente() != null && this.registro.getIdCliente().getId() != null) {
                Cliente clienteCompleto = clienteDao.findById(this.registro.getIdCliente().getId());
                this.registro.setIdCliente(clienteCompleto);
            }

            getDao().modificar(this.registro);

            System.out.println("Venta anulada correctamente");

            try {
                notificadorKardex.notificarCambio("VENTA_ANULADA:" + this.registro.getId());
                System.out.println("Notificacion JMS enviada para anulacion de venta");
            } catch (Exception e) {
                System.err.println("Error al enviar notificacion JMS (no critico): " + e.getMessage());
            }

            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            this.modelo = null;
            inicializarRegistros();

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Exito", "Venta anulada correctamente"));

        } catch (Exception e) {
            System.err.println("Error al anular venta: " + e.getMessage());
            e.printStackTrace();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo anular la venta: " + e.getMessage()));
        }
    }

    public List<Cliente> getClientesDisponibles() {
        if (clientesDisponibles == null || clientesDisponibles.isEmpty()) {
            cargarClientes();
        }
        return clientesDisponibles;
    }

    public List<String> getEstadosDisponibles() {
        return estadosDisponibles;
    }
}