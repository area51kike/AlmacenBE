/**
 * Cliente WebSocket para notificaciones de Kardex
 * Incluir en la página JSF donde se muestra la tabla de compras
 */

let ws = null;
let reconnectInterval = null;
const MAX_RECONNECT_ATTEMPTS = 5;
let reconnectAttempts = 0;

/**
 * Conectar al WebSocket
 */
function conectarWebSocket() {
    try {
        // Determinar el protocolo (ws o wss)
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const host = window.location.host;

        // Construir la URL del WebSocket
        const wsUrl = `${protocol}//${host}/JPQL2-1.0-SNAPSHOT/notificadorKardex`;

        console.log('Conectando a WebSocket:', wsUrl);

        ws = new WebSocket(wsUrl);

        ws.onopen = function(event) {
            console.log('✅ Conexión WebSocket establecida');
            reconnectAttempts = 0; // Reset contador

            // Mostrar notificación al usuario (opcional)
            mostrarNotificacion('Conectado al servidor', 'info');
        };

        ws.onmessage = function(event) {
            console.log('📨 Mensaje recibido:', event.data);

            // Parsear el mensaje
            const mensaje = event.data;

            // Mostrar notificación
            mostrarNotificacion(mensaje, 'success');

            // Recargar la tabla de PrimeFaces
            recargarTabla();
        };

        ws.onerror = function(error) {
            console.error('❌ Error en WebSocket:', error);
            mostrarNotificacion('Error en la conexión', 'error');
        };

        ws.onclose = function(event) {
            console.log('🔌 Conexión WebSocket cerrada', event);

            // Intentar reconectar
            if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                reconnectAttempts++;
                console.log(`Reintentando conexión (${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})...`);

                reconnectInterval = setTimeout(conectarWebSocket, 3000);
            } else {
                console.error('Se alcanzó el máximo de intentos de reconexión');
                mostrarNotificacion('Conexión perdida. Recarga la página.', 'error');
            }
        };

    } catch (error) {
        console.error('Error al crear WebSocket:', error);
    }
}

/**
 * Recargar la tabla de PrimeFaces
 */
function recargarTabla() {
    console.log('🔄 Recargando tabla...');

    // Opción 1: Usando PrimeFaces Remote Command
    if (typeof recargarTablaRemote !== 'undefined') {
        recargarTablaRemote();
    }

    // Opción 2: Actualizar directamente con PrimeFaces
    if (typeof PF !== 'undefined') {
        // Actualizar el componente de la tabla
        PF('tabla').clearFilters(); // Limpiar filtros si existen

        // Usar Ajax de PrimeFaces para actualizar
        if (typeof PrimeFaces !== 'undefined' && PrimeFaces.ajax) {
            PrimeFaces.ajax.Request.handle({
                source: 'tabla',
                update: 'tabla',
                process: '@this'
            });
        }
    }

    console.log('✅ Tabla recargada');
}

/**
 * Mostrar notificación al usuario
 */
function mostrarNotificacion(mensaje, tipo) {
    // Usar PrimeFaces Growl si está disponible
    if (typeof PF !== 'undefined' && PF('growl')) {
        const severidad = tipo === 'error' ? 'error' :
            tipo === 'warning' ? 'warn' :
                tipo === 'info' ? 'info' : 'success';

        PF('growl').renderMessage({
            summary: 'Notificación Kardex',
            detail: mensaje,
            severity: severidad
        });
    } else {
        // Fallback: console
        console.log(`[${tipo.toUpperCase()}] ${mensaje}`);
    }
}

/**
 * Cerrar conexión WebSocket
 */
function desconectarWebSocket() {
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.close();
        console.log('Conexión WebSocket cerrada manualmente');
    }

    if (reconnectInterval) {
        clearTimeout(reconnectInterval);
    }
}

// Conectar automáticamente al cargar la página
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Iniciando conexión WebSocket...');
    conectarWebSocket();
});

// Cerrar conexión al salir de la página
window.addEventListener('beforeunload', function() {
    desconectarWebSocket();
});