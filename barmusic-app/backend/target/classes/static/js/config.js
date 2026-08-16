/* ============================================================================
   ARCHIVO: config.js
   ============================================================================
   Este archivo se carga PRIMERO en todas las paginas HTML. Centraliza cosas
   que se repiten en toda la app, para que si algo cambia, lo edites en UN
   solo lugar en vez de buscarlo en 10 archivos distintos.
   ============================================================================ */

// -----------------------------------------------------------------------
// DIRECCION BASE DEL BACKEND (API en Java).
// -----------------------------------------------------------------------
// Mientras pruebas todo en TU computador, deja esto tal cual.
// Cuando publiques la app en internet de verdad, cambia esta linea por la
// URL real de tu servidor, por ejemplo:
//     const API_BASE_URL = "https://api.mibar.com";
const API_BASE_URL = "http://localhost:8080";

/**
 * Funcion ayudante para hacer peticiones a la API sin repetir codigo.
 * Uso: const datos = await apiFetch("/api/catalogo");
 */
async function apiFetch(ruta, opciones = {}) {
    const respuesta = await fetch(API_BASE_URL + ruta, opciones);

    if (!respuesta.ok) {
        // Intentamos leer el mensaje de error que mando el backend
        let mensajeError = "Ocurrio un error al comunicarse con el servidor";
        try {
            const cuerpo = await respuesta.json();
            mensajeError = cuerpo.mensaje || mensajeError;
        } catch (e) { /* el backend no devolvio JSON, usamos el mensaje generico */ }
        throw new Error(mensajeError);
    }

    // Si la respuesta no trae contenido (ej: un DELETE), devolvemos null
    const texto = await respuesta.text();
    return texto ? JSON.parse(texto) : null;
}

/**
 * Guarda los datos del usuario que inicio sesion en el "sessionStorage" del
 * navegador (se borra solo cuando el usuario cierra la pestaña).
 * NOTA DIDACTICA: para un proyecto academico esto es suficiente. Para un
 * sistema en produccion real de una empresa, se recomienda usar tokens
 * JWT + Spring Security en vez de guardar el usuario "tal cual".
 */
function guardarSesion(usuario) {
    sessionStorage.setItem("usuarioActual", JSON.stringify(usuario));
}

function obtenerSesion() {
    const datos = sessionStorage.getItem("usuarioActual");
    return datos ? JSON.parse(datos) : null;
}

function cerrarSesion() {
    sessionStorage.removeItem("usuarioActual");
}

/**
 * Protege una pagina: si no hay sesion iniciada (o el rol no coincide),
 * redirige automaticamente al login correspondiente.
 * Uso en la parte de arriba de cada pagina protegida:
 *     protegerPagina("CLIENTE", "cliente-login.html");
 */
function protegerPagina(rolEsperado, paginaLogin) {
    const usuario = obtenerSesion();
    if (!usuario || usuario.rol !== rolEsperado) {
        window.location.href = paginaLogin;
    }
    return usuario;
}

// Formatea una fecha ISO (la que manda Java) a un formato legible en español
function formatearFecha(fechaIso) {
    if (!fechaIso) return "";
    const fecha = new Date(fechaIso);
    return fecha.toLocaleString("es-CO", {
        day: "2-digit", month: "2-digit", year: "numeric",
        hour: "2-digit", minute: "2-digit"
    });
}
