package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EstadoCompraTest {

    @Test
    void testEnumConstants() {
        // 1. Prueba el método values() (cubre todos los valores)
        EstadoCompra[] estados = EstadoCompra.values();
        assertNotNull(estados);
        assertTrue(estados.length > 0);

        // 2. Prueba el método valueOf() (cubre la conversión de String a Enum)
        // Probamos con uno de los valores reales
        EstadoCompra estado = EstadoCompra.valueOf("ORDEN");
        assertEquals(EstadoCompra.ORDEN, estado);

        // Opcional: Probar todos para asegurar existencia
        assertEquals(EstadoCompra.CREADA, EstadoCompra.valueOf("CREADA"));
        assertEquals(EstadoCompra.APROBADA, EstadoCompra.valueOf("APROBADA"));
        assertEquals(EstadoCompra.RECHAZADA, EstadoCompra.valueOf("RECHAZADA"));
        assertEquals(EstadoCompra.ANULADA, EstadoCompra.valueOf("ANULADA"));
    }
}