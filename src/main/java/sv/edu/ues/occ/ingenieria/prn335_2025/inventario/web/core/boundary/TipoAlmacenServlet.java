package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoAlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "TipoAlmacenamiento", urlPatterns = "web/tipo_almacen")
public class TipoAlmacenServlet extends HttpServlet {
    @Inject
    TipoAlmacenDAO taDao;

    // Método para manejar las solicitudes POST (envío de formulario)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String arriba = """
                <html>
                <head>
                </head>
                <body>
                """;

        String abajo = """
                </body>
                </html>        
                """;

        StringBuilder sb = new StringBuilder();
        String nombre = req.getParameter("nombre");

        if(req.getParameter("nombre") == null) {
            sb.append("<p>Debe agregar el nombre del tipo de almacen</p>");
        } else {
            if(nombre!=null) {
                TipoAlmacen tipoAlmacen = new TipoAlmacen();
                tipoAlmacen.setNombre(nombre);
                tipoAlmacen.setActivo(true);
                tipoAlmacen.setObservaciones("creado en clase 26 de agosto 2025");

                try {
                    sb.append("<p>Agregado con exito</p>");
                } catch (Exception ex) {

                    sb.append(ex.getMessage());
                }
            }
        }

        try {
            PrintWriter writer = resp.getWriter();
            writer.println(arriba);
            writer.println(sb.toString());
            writer.println(abajo);
            writer.flush();
            writer.close();
        } catch(Exception ex) {

        }
    }

    // --- NUEVO MÉTODO A AGREGAR ---
    // Método para manejar las solicitudes GET (navegación a la URL)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String htmlForm = """
            <html>
            <head>
                <title>Agregar Tipo de Almacen</title>
            </head>
            <body>
                <h2>Crear Nuevo Tipo de Almacen</h2>
                <form action="web/tipo_almacen" method="post">
                    <label for="nombre">Nombre del tipo de almacen:</label><br>
                    <input type="text" id="nombre" name="nombre" required><br><br>
                    <input type="submit" value="Guardar">
                </form>
            </body>
            </html>
            """;

        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();
        writer.println(htmlForm);
    }
}