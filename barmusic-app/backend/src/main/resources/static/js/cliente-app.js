/* ============================================================================
   ARCHIVO: cliente-app.js
   ============================================================================
   Logica de las 3 pantallas principales del cliente:
   1) Solicitar Cancion (catalogo)
   2) Canciones Solicitadas (mis solicitudes activas)
   3) Notificaciones enviadas por el administrador
   ============================================================================ */

// PASO 1: Verificamos que haya una sesion de CLIENTE valida. Si no, se
// redirige automaticamente al login (ver funcion en config.js).
const usuario = protegerPagina("CLIENTE", "index.html");

// Referencias a elementos del HTML
const botonesPestana = document.querySelectorAll(".pestana, .boton-campana");
const secciones = {
    tabSolicitar: document.getElementById("tabSolicitar"),
    tabSolicitadas: document.getElementById("tabSolicitadas"),
    tabNotificaciones: document.getElementById("tabNotificaciones"),
};
const mensajeGeneral = document.getElementById("mensajeGeneral");

// ---------------------------------------------------------------------
// MANEJO DE PESTAÑAS: al hacer clic, mostramos su seccion y ocultamos
// las demas, ademas de marcar visualmente cual esta "activa".
// ---------------------------------------------------------------------
botonesPestana.forEach((boton) => {
    boton.addEventListener("click", () => {
        botonesPestana.forEach((b) => b.classList.remove("activa"));
        boton.classList.add("activa");

        Object.keys(secciones).forEach((idSeccion) => {
            secciones[idSeccion].style.display = (idSeccion === boton.dataset.tab) ? "block" : "none";
        });

        if (boton.dataset.tab === "tabSolicitar") cargarCatalogo();
        if (boton.dataset.tab === "tabSolicitadas") cargarMisSolicitudes();
        if (boton.dataset.tab === "tabNotificaciones") cargarNotificaciones();
    });
});

// ---------------------------------------------------------------------
// SECCION 1: CATALOGO DE CANCIONES
// ---------------------------------------------------------------------
async function cargarCatalogo() {
    const contenedor = document.getElementById("listaCatalogo");
    try {
        const canciones = await apiFetch("/api/catalogo");

        if (canciones.length === 0) {
            contenedor.innerHTML = "<p>Todavía no hay canciones en el catálogo.</p>";
            return;
        }

        // Construimos el HTML de la lista dinamicamente. "map" recorre cada
        // cancion y genera un pedazo de HTML por cada una; "join" los pega todos.
        contenedor.innerHTML = canciones.map((cancion) => `
            <div class="item-cancion">
                <div class="avatar-cancion"></div>
                <div class="info-cancion">
                    <div class="titulo">${escaparTexto(cancion.nombre)}</div>
                    <div class="subtitulo">${escaparTexto(cancion.artistas)}</div>
                </div>
                <button class="boton-icono check" onclick="solicitarCancion(${cancion.id})" title="Solicitar">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="white"><path d="M9 16.2l-3.5-3.5-1.4 1.4L9 19 20 8l-1.4-1.4z"/></svg>
                </button>
            </div>
        `).join("");
    } catch (error) {
        contenedor.innerHTML = `<p class="mensaje error">${error.message}</p>`;
    }
}

// Filtro de busqueda en vivo (no llama al backend otra vez, solo esconde
// las filas del catalogo que no coinciden con el texto escrito).
document.getElementById("inputBuscar").addEventListener("input", (evento) => {
    const texto = evento.target.value.toLowerCase();
    document.querySelectorAll("#listaCatalogo .item-cancion").forEach((fila) => {
        const contenido = fila.textContent.toLowerCase();
        fila.style.display = contenido.includes(texto) ? "flex" : "none";
    });
});

async function solicitarCancion(idCatalogo) {
    try {
        await apiFetch("/api/solicitudes", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ idCatalogo, idUsuario: usuario.id })
        });
        mostrarMensaje("¡Canción solicitada con éxito!", "exito");
    } catch (error) {
        mostrarMensaje(error.message, "error");
    }
}

// ---------------------------------------------------------------------
// SECCION 2: MIS SOLICITUDES
// ---------------------------------------------------------------------
async function cargarMisSolicitudes() {
    const contenedor = document.getElementById("listaSolicitudes");
    try {
        const solicitudes = await apiFetch(`/api/solicitudes/usuario/${usuario.id}`);

        if (solicitudes.length === 0) {
            contenedor.innerHTML = "<p>No tienes canciones solicitadas.</p>";
            return;
        }

        contenedor.innerHTML = solicitudes.map((solicitud) => `
            <div class="item-cancion">
                <div class="avatar-cancion"></div>
                <div class="info-cancion">
                    <div class="titulo">${escaparTexto(solicitud.catalogo.nombre)}</div>
                    <div class="subtitulo">${escaparTexto(solicitud.catalogo.artistas)}</div>
                </div>
                <button class="boton-icono equis" onclick="cancelarSolicitud(${solicitud.id})" title="Cancelar">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="white"><path d="M18.3 5.71L12 12.01l-6.3-6.3-1.41 1.41 6.3 6.3-6.3 6.3 1.41 1.41 6.3-6.3 6.3 6.3 1.41-1.41-6.3-6.3 6.3-6.3z"/></svg>
                </button>
            </div>
        `).join("");
    } catch (error) {
        contenedor.innerHTML = `<p class="mensaje error">${error.message}</p>`;
    }
}

async function cancelarSolicitud(idSolicitud) {
    try {
        await apiFetch(`/api/solicitudes/${idSolicitud}`, { method: "DELETE" });
        cargarMisSolicitudes(); // Refrescamos la lista despues de cancelar
    } catch (error) {
        mostrarMensaje(error.message, "error");
    }
}

// ---------------------------------------------------------------------
// SECCION 3: NOTIFICACIONES
// ---------------------------------------------------------------------
async function cargarNotificaciones() {
    const contenedor = document.getElementById("listaNotificaciones");
    try {
        const notificaciones = await apiFetch("/api/notificaciones");

        if (notificaciones.length === 0) {
            contenedor.innerHTML = "<p>No hay notificaciones por ahora.</p>";
            return;
        }

        contenedor.innerHTML = notificaciones.map((n) => `
            <div class="tarjeta-notificacion">
                ${n.texto ? `<h3>${escaparTexto(n.texto)}</h3>` : ""}
                ${n.rutaImagen ? `<img src="${API_BASE_URL}${n.rutaImagen}" alt="Imagen de la notificación">` : ""}
                <div class="fecha">${formatearFecha(n.fechaCreacion)}</div>
            </div>
        `).join("");

        document.getElementById("badgeNotificaciones").style.display = "none";
    } catch (error) {
        contenedor.innerHTML = `<p class="mensaje error">${error.message}</p>`;
    }
}

// ---------------------------------------------------------------------
// UTILIDADES
// ---------------------------------------------------------------------
function mostrarMensaje(texto, tipo) {
    mensajeGeneral.textContent = texto;
    mensajeGeneral.className = "mensaje " + tipo;
    mensajeGeneral.style.display = "block";
    setTimeout(() => { mensajeGeneral.style.display = "none"; }, 3000);
}

// Evita que texto escrito por usuarios (nombre de cancion, etc) rompa el
// HTML o permita inyectar codigo (proteccion basica contra XSS).
function escaparTexto(texto) {
    const div = document.createElement("div");
    div.textContent = texto ?? "";
    return div.innerHTML;
}

document.getElementById("botonCerrarSesion").addEventListener("click", () => {
    cerrarSesion();
    window.location.href = "index.html";
});

// Al cargar la pagina por primera vez, mostramos el catalogo
cargarCatalogo();
