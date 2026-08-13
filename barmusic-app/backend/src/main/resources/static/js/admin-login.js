/* ============================================================================
   ARCHIVO: admin-login.js  (identico en logica a cliente-login.js, pero
   valida que el rol devuelto sea ADMINISTRADOR)
   ============================================================================ */
const inputUsuario = document.getElementById("inputUsuario");
const inputContrasena = document.getElementById("inputContrasena");
const botonIngresar = document.getElementById("botonIngresar");
const mensajeError = document.getElementById("mensajeError");

const sesionExistente = obtenerSesion();
if (sesionExistente && sesionExistente.rol === "ADMINISTRADOR") {
    window.location.href = "admin-app.html";
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

        if (usuario.rol !== "ADMINISTRADOR") {
            mostrarError("Esta cuenta no tiene permisos de administrador.");
            return;
        }

        guardarSesion(usuario);
        window.location.href = "admin-app.html";
    } catch (error) {
        mostrarError(error.message);
    }
});

inputContrasena.addEventListener("keyup", (evento) => {
    if (evento.key === "Enter") botonIngresar.click();
});

function mostrarError(texto) { mensajeError.textContent = texto; mensajeError.style.display = "block"; }
function ocultarError() { mensajeError.style.display = "none"; }
