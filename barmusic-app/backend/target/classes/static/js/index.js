/* ============================================================================
   ARCHIVO: index.js
   ============================================================================
   Logica de la pantalla UNICA de login. A diferencia de antes (donde
   cliente-login.js y admin-login.js exigian que el rol coincidiera con la
   pantalla en la que estabas), aqui NO le preguntamos nada al usuario sobre
   su rol: dejamos que el backend lo diga (usuario.rol) y redirigimos solo.
   ============================================================================ */

const inputUsuario = document.getElementById("inputUsuario");
const inputContrasena = document.getElementById("inputContrasena");
const botonIngresar = document.getElementById("botonIngresar");
const botonQr = document.getElementById("botonQr");
const mensajeError = document.getElementById("mensajeError");

// Si ya habia una sesion activa (el usuario no cerro sesion la ultima vez),
// lo mandamos directo a su pantalla correspondiente sin pedirle login otra vez.
const sesionExistente = obtenerSesion();
if (sesionExistente) {
    redirigirSegunRol(sesionExistente);
}

botonIngresar.addEventListener("click", async () => {
    ocultarError();

    const login = inputUsuario.value.trim();
    const contrasena = inputContrasena.value;

    if (!login || !contrasena) {
        mostrarError("Por favor completa usuario y contraseña.");
        return;
    }

    try {
        const usuario = await apiFetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ login, contrasena })
        });

        guardarSesion(usuario);
        redirigirSegunRol(usuario);
    } catch (error) {
        mostrarError(error.message);
    }
});

// Permite dar "Enter" para ingresar sin tener que hacer clic
inputContrasena.addEventListener("keyup", (evento) => {
    if (evento.key === "Enter") botonIngresar.click();
});

// El boton QR nos lleva a la pantalla de escaneo (la logica de ahi tambien
// redirige segun el rol una vez identifica al usuario, ver cliente-qr.js)
botonQr.addEventListener("click", () => {
    window.location.href = "cliente-qr.html";
});

/**
 * Manda al usuario a la pantalla correcta segun el rol que devolvio el
 * backend. Esta es la funcion clave que reemplaza la "eleccion manual" de
 * cliente o administrador que existia antes.
 */
function redirigirSegunRol(usuario) {
    if (usuario.rol === "ADMINISTRADOR") {
        window.location.href = "admin-app.html";
    } else {
        window.location.href = "cliente-app.html";
    }
}

function mostrarError(texto) {
    mensajeError.textContent = texto;
    mensajeError.style.display = "block";
}
function ocultarError() {
    mensajeError.style.display = "none";
}
