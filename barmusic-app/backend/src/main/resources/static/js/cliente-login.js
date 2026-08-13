/* ============================================================================
   ARCHIVO: cliente-login.js
   ============================================================================
   Logica de la pantalla de login del CLIENTE.
   ============================================================================ */

// Buscamos los elementos del HTML por su "id" para poder leerlos/modificarlos
const inputUsuario = document.getElementById("inputUsuario");
const inputContrasena = document.getElementById("inputContrasena");
const botonIngresar = document.getElementById("botonIngresar");
const botonQr = document.getElementById("botonQr");
const mensajeError = document.getElementById("mensajeError");

// Si el cliente ya tenia sesion iniciada, lo mandamos directo al catalogo
const sesionExistente = obtenerSesion();
if (sesionExistente && sesionExistente.rol === "CLIENTE") {
    window.location.href = "cliente-app.html";
}

// Cuando el usuario da clic en "Ingresar"...
botonIngresar.addEventListener("click", async () => {
    ocultarError();

    const login = inputUsuario.value.trim();
    const contrasena = inputContrasena.value;

    if (!login || !contrasena) {
        mostrarError("Por favor completa usuario y contraseña.");
        return;
    }

    try {
        // Llamamos al backend: POST /api/auth/login
        const usuario = await apiFetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ login, contrasena })
        });

        if (usuario.rol !== "CLIENTE") {
            mostrarError("Esta cuenta no es de cliente. Usa el login de administrador.");
            return;
        }

        guardarSesion(usuario);
        window.location.href = "cliente-app.html"; // Vamos al catalogo de canciones
    } catch (error) {
        mostrarError(error.message);
    }
});

// Permite dar "Enter" en vez de tener que hacer clic en el boton
inputContrasena.addEventListener("keyup", (evento) => {
    if (evento.key === "Enter") botonIngresar.click();
});

// El boton QR nos lleva a la pantalla de escaneo (pagina 3 del mockup)
botonQr.addEventListener("click", () => {
    window.location.href = "cliente-qr.html";
});

function mostrarError(texto) {
    mensajeError.textContent = texto;
    mensajeError.style.display = "block";
}
function ocultarError() {
    mensajeError.style.display = "none";
}
