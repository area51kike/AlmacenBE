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
        EstadoCompra estado = EstadoCompra.valueOf("CREADA");
        assertEquals(EstadoCompra.CREADA, estado);

        // Opcional: Probar todos para asegurar existencia
        assertEquals(EstadoCompra.CREADA, EstadoCompra.valueOf("CREADA"));
        assertEquals(EstadoCompra.ANULADA, EstadoCompra.valueOf("ANULADA"));
        assertEquals(EstadoCompra.RECIBIDA, EstadoCompra.valueOf("RECIBIDA"));
        assertEquals(EstadoCompra.PAGADA, EstadoCompra.valueOf("PAGADA"));
    }
}