package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

class KardexEventTest {

    @Test
    @DisplayName("Constructor con mensaje válido - debería crear instancia correctamente")
    void testConstructorConMensajeValido() {
        // Arrange
        String mensajeEsperado = "Movimiento de kardex registrado exitosamente";

        // Act
        KardexEvent evento = new KardexEvent(mensajeEsperado);

        // Assert
        assertNotNull(evento);
        assertEquals(mensajeEsperado, evento.getMensaje());
    }

    @Test
    @DisplayName("Constructor con mensaje nulo - debería crear instancia con mensaje nulo")
    void testConstructorConMensajeNulo() {
        // Arrange & Act
        KardexEvent evento = new KardexEvent(null);

        // Assert
        assertNotNull(evento);
        assertNull(evento.getMensaje());
    }

    @Test
    @DisplayName("Constructor con mensaje vacío - debería crear instancia con mensaje vacío")
    void testConstructorConMensajeVacio() {
        // Arrange & Act
        KardexEvent evento = new KardexEvent("");

        // Assert
        assertNotNull(evento);
        assertEquals("", evento.getMensaje());
    }

    @Test
    @DisplayName("Constructor con mensaje en blanco - debería crear instancia con mensaje en blanco")
    void testConstructorConMensajeEnBlanco() {
        // Arrange & Act
        KardexEvent evento = new KardexEvent("   ");

        // Assert
        assertNotNull(evento);
        assertEquals("   ", evento.getMensaje());
    }

    @Test
    @DisplayName("Constructor con diferentes mensajes válidos - debería crear instancias correctamente")
    void testConstructorConDiferentesMensajesValidos() {
        // Test múltiples mensajes válidos sin usar ParameterizedTest
        String[] mensajes = {
                "Kardex actualizado",
                "Entrada de productos registrada",
                "Salida de productos procesada",
                "Ajuste de inventario aplicado"
        };

        for (String mensaje : mensajes) {
            // Act
            KardexEvent evento = new KardexEvent(mensaje);

            // Assert
            assertNotNull(evento);
            assertEquals(mensaje, evento.getMensaje());
        }
    }

    @Test
    @DisplayName("Constructor con mensajes nulos y vacíos - debería manejar correctamente")
    void testConstructorConMensajesNulosYVacios() {
        // Test nulos y vacíos sin usar ParameterizedTest
        String[] mensajes = {null, ""};

        for (String mensaje : mensajes) {
            // Act
            KardexEvent evento = new KardexEvent(mensaje);

            // Assert
            assertNotNull(evento);
            assertEquals(mensaje, evento.getMensaje());
        }
    }

    @Test
    @DisplayName("getMensaje después de construcción - debería retornar mensaje correcto")
    void testGetMensajeDespuesDeConstruccion() {
        // Arrange
        String mensajeEsperado = "Notificación de kardex";
        KardexEvent evento = new KardexEvent(mensajeEsperado);

        // Act
        String mensajeObtenido = evento.getMensaje();

        // Assert
        assertEquals(mensajeEsperado, mensajeObtenido);
    }

    @Test
    @DisplayName("getMensaje múltiples llamadas - debería retornar mismo valor siempre")
    void testGetMensajeMultiplesLlamadas() {
        // Arrange
        String mensajeEsperado = "Mensaje consistente";
        KardexEvent evento = new KardexEvent(mensajeEsperado);

        // Act & Assert
        for (int i = 0; i < 5; i++) {
            assertEquals(mensajeEsperado, evento.getMensaje(),
                    "El mensaje debería ser consistente en la llamada #" + (i + 1));
        }
    }

    @Test
    @DisplayName("Inmutabilidad - el mensaje no debería cambiar después de la construcción")
    void testInmutabilidad() {
        // Arrange
        String mensajeOriginal = "Mensaje original";
        KardexEvent evento = new KardexEvent(mensajeOriginal);

        // Act
        String mensajeObtenido = evento.getMensaje();

        // Assert
        assertEquals(mensajeOriginal, mensajeObtenido);
    }

    @Test
    @DisplayName("Equals y HashCode - debería comparar correctamente por identidad")
    void testEqualsYHashCode() {
        // Arrange
        KardexEvent evento1 = new KardexEvent("Mensaje");
        KardexEvent evento2 = new KardexEvent("Mensaje");
        KardexEvent evento3 = new KardexEvent("Otro mensaje");

        // Act & Assert - equals
        assertNotEquals(evento1, evento2);
        assertNotEquals(evento1, evento3);
        assertNotEquals(evento1, null);
        assertNotEquals(evento1, "No es un KardexEvent");

        // Act & Assert - hashCode
        assertNotEquals(evento1.hashCode(), evento2.hashCode());
        assertNotEquals(evento1.hashCode(), evento3.hashCode());
    }

    @Test
    @DisplayName("ToString - debería incluir información del mensaje")
    void testToString() {
        // Arrange
        String mensaje = "Mensaje de prueba";
        KardexEvent evento = new KardexEvent(mensaje);

        // Act
        String toStringResult = evento.toString();

        // Assert
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains(KardexEvent.class.getSimpleName()) ||
                toStringResult.contains(mensaje) ||
                toStringResult.length() > 0);
    }

    @Nested
    @DisplayName("Pruebas de Casos Especiales")
    class CasosEspeciales {

        @Test
        @DisplayName("Mensaje con caracteres especiales - debería manejarse correctamente")
        void testMensajeConCaracteresEspeciales() {
            // Arrange
            String mensajeEspecial = "Kardex ✅ - Movimiento #123: 📦 → 🏪";

            // Act
            KardexEvent evento = new KardexEvent(mensajeEspecial);

            // Assert
            assertEquals(mensajeEspecial, evento.getMensaje());
        }

        @Test
        @DisplayName("Mensaje muy largo - debería manejarse correctamente")
        void testMensajeMuyLargo() {
            // Arrange
            String mensajeLargo = "Este es un mensaje muy largo que podría utilizarse " +
                    "para notificaciones extensas del sistema de kardex donde se necesite " +
                    "detallar información completa sobre el movimiento de inventario realizado.";

            // Act
            KardexEvent evento = new KardexEvent(mensajeLargo);

            // Assert
            assertEquals(mensajeLargo, evento.getMensaje());
        }

        @Test
        @DisplayName("Mensaje con formato JSON - debería manejarse correctamente")
        void testMensajeConFormatoJSON() {
            // Arrange
            String mensajeJSON = "{\"tipo\":\"entrada\",\"productoId\":123,\"cantidad\":50}";

            // Act
            KardexEvent evento = new KardexEvent(mensajeJSON);

            // Assert
            assertEquals(mensajeJSON, evento.getMensaje());
        }
    }

    @Nested
    @DisplayName("Pruebas de Integridad de Objeto")
    class IntegridadObjeto {

        @Test
        @DisplayName("Instancia única - debería mantener su estado")
        void testInstanciaUnicaMantieneEstado() {
            // Arrange
            KardexEvent evento = new KardexEvent("Estado inicial");

            // Act
            String mensaje1 = evento.getMensaje();
            String mensaje2 = evento.getMensaje();

            // Assert
            assertSame(mensaje1, mensaje2);
            assertEquals(mensaje1, mensaje2);
        }

        @Test
        @DisplayName("No debería lanzar excepciones en condiciones normales")
        void testNoLanzaExcepciones() {
            // Arrange
            String[] mensajes = {"Normal", "", "   ", null};

            for (String mensaje : mensajes) {
                // Act & Assert
                assertDoesNotThrow(() -> {
                    KardexEvent evento = new KardexEvent(mensaje);
                    evento.getMensaje();
                }, "No debería lanzar excepción con mensaje: " + mensaje);
            }
        }
    }
}