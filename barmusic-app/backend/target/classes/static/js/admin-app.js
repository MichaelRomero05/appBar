/* ============================================================================
   ARCHIVO: admin-app.js
   ============================================================================
   Logica del panel de administrador:
   1) Ver y gestionar (Completar/Eliminar) las solicitudes de TODOS los clientes.
   2) Ver notificaciones enviadas y crear una nueva (texto y/o imagen).
   ============================================================================ */

const usuario = protegerPagina("ADMINISTRADOR", "index.html");

const botonesPestana = document.querySelectorAll(".pestana");
const secciones = {
    tabGestion: document.getElementById("tabGestion"),
    tabNotificaciones: document.getElementById("tabNotificaciones"),
};
const mensajeGeneral = document.getElementById("mensajeGeneral");

botonesPestana.forEach((boton) => {
    boton.addEventListener("click", () => {
        botonesPestana.forEach((b) => b.classList.remove("activa"));
        boton.classList.add("activa");

        Object.keys(secciones).forEach((idSeccion) => {
            secciones[idSeccion].style.display = (idSeccion === boton.dataset.tab) ? "block" : "none";
        });

        if (boton.dataset.tab === "tabGestion") cargarSolicitudesActivas();
        if (boton.dataset.tab === "tabNotificaciones") cargarNotificacionesAdmin();
    });
});

// ---------------------------------------------------------------------
// PESTAÑA 1: GESTION DE SOLICITUDES (Pagina 9 del mockup)
// ---------------------------------------------------------------------
async function cargarSolicitudesActivas() {
    const contenedor = document.getElementById("listaGestion");
    try {
        const solicitudes = await apiFetch("/api/solicitudes");

        if (solicitudes.length === 0) {
            contenedor.innerHTML = "<p>No hay solicitudes pendientes por ahora.</p>";
            return;
        }

        contenedor.innerHTML = solicitudes.map((s) => `
            <div class="item-cancion" style="border-radius: var(--radio-borde); flex-wrap: wrap;">
                <div class="avatar-cancion"></div>
                <div class="info-cancion">
                    <div class="titulo">${escaparTexto(s.catalogo.nombre)}</div>
                    <div class="subtitulo">${escaparTexto(s.catalogo.artistas)} · Pedida por ${escaparTexto(s.usuarioSolicita.nombre)}</div>
                </div>
                <div class="acciones-admin">
                    <button class="boton boton-exito" onclick="completarSolicitud(${s.id})">Completar ✓</button>
                    <button class="boton boton-peligro" onclick="eliminarSolicitud(${s.id})">Eliminar ✗</button>
                </div>
            </div>
        `).join("");
    } catch (error) {
        contenedor.innerHTML = `<p class="mensaje error">${error.message}</p>`;
    }
}

async function completarSolicitud(id) {
    try {
        await apiFetch(`/api/solicitudes/${id}/completar`, { method: "POST" });
        cargarSolicitudesActivas();
    } catch (error) { mostrarMensaje(error.message, "error"); }
}

async function eliminarSolicitud(id) {
    try {
        await apiFetch(`/api/solicitudes/${id}/eliminar`, { method: "POST" });
        cargarSolicitudesActivas();
    } catch (error) { mostrarMensaje(error.message, "error"); }
}

// ---------------------------------------------------------------------
// PESTAÑA 2: NOTIFICACIONES (Paginas 10 y 11 del mockup)
// ---------------------------------------------------------------------
const vistaLista = document.getElementById("vistaListaNotificaciones");
const vistaFormulario = document.getElementById("vistaFormularioNotificacion");

document.getElementById("botonMostrarFormulario").addEventListener("click", () => {
    vistaLista.style.display = "none";
    vistaFormulario.style.display = "block";
});

document.getElementById("botonCancelarNotificacion").addEventListener("click", () => {
    vistaFormulario.style.display = "none";
    vistaLista.style.display = "block";
});

document.getElementById("botonEnviarNotificacion").addEventListener("click", async () => {
    const texto = document.getElementById("inputTextoNotificacion").value.trim();
    const archivoImagen = document.getElementById("inputImagenNotificacion").files[0];

    if (!texto && !archivoImagen) {
        mostrarMensaje("Escribe un texto o sube una imagen para poder enviar la notificación.", "error");
        return;
    }

    // "FormData" es la forma correcta de mandar texto + un archivo juntos
    // en una sola peticion HTTP (multipart/form-data). El navegador arma
    // automaticamente el formato correcto, nosotros solo agregamos los campos.
    const datosFormulario = new FormData();
    if (texto) datosFormulario.append("texto", texto);
    if (archivoImagen) datosFormulario.append("imagen", archivoImagen);

    try {
        // OJO: NO ponemos el header "Content-Type" a mano; el navegador lo
        // arma solo (con el "boundary" correcto) cuando usamos FormData.
        await apiFetch("/api/notificaciones", { method: "POST", body: datosFormulario });

        document.getElementById("inputTextoNotificacion").value = "";
        document.getElementById("inputImagenNotificacion").value = "";

        mostrarMensaje("Notificación enviada con éxito.", "exito");
        vistaFormulario.style.display = "none";
        vistaLista.style.display = "block";
        cargarNotificacionesAdmin();
    } catch (error) {
        mostrarMensaje(error.message, "error");
    }
});

async function cargarNotificacionesAdmin() {
    const contenedor = document.getElementById("listaNotificacionesAdmin");
    try {
        const notificaciones = await apiFetch("/api/notificaciones");

        if (notificaciones.length === 0) {
            contenedor.innerHTML = "<p>Aún no has enviado notificaciones.</p>";
            return;
        }

        contenedor.innerHTML = notificaciones.map((n) => `
            <div class="tarjeta-notificacion">
                ${n.texto ? `<h3>${escaparTexto(n.texto)}</h3>` : ""}
                ${n.rutaImagen ? `<img src="${API_BASE_URL}${n.rutaImagen}" alt="Imagen de la notificación">` : ""}
                <div class="fecha">${formatearFecha(n.fechaCreacion)}</div>
            </div>
        `).join("");
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

function escaparTexto(texto) {
    const div = document.createElement("div");
    div.textContent = texto ?? "";
    return div.innerHTML;
}

document.getElementById("botonCerrarSesion").addEventListener("click", () => {
    cerrarSesion();
    window.location.href = "index.html";
});

// Al cargar la pagina, mostramos primero la gestion de solicitudes
cargarSolicitudesActivas();
