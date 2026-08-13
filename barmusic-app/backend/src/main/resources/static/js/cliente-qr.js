/* ============================================================================
   ARCHIVO: cliente-qr.js
   ============================================================================
   Enciende la camara, detecta el codigo QR y lo manda al backend para
   iniciar sesion automaticamente (endpoint POST /api/auth/login-qr).
   ============================================================================ */

const mensajeEstado = document.getElementById("mensajeEstado");
const botonVolver = document.getElementById("botonVolver");

botonVolver.addEventListener("click", () => {
    window.location.href = "cliente-login.html";
});

// Creamos el "lector" y le decimos en que elemento HTML debe dibujar la camara
const lector = new Html5Qrcode("lectorQr");

// Bandera para evitar procesar el mismo QR varias veces mientras la camara
// sigue prendida (si no, se dispararia el login repetidas veces por segundo).
let yaProcesando = false;

// Configuracion: tamaño del cuadro de escaneo y cuadros por segundo a leer
const configuracionCamara = { fps: 10, qrbox: { width: 220, height: 220 } };

// Iniciamos la camara trasera del celular ("environment"). Si el dispositivo
// no tiene camara trasera (ej: un computador de escritorio), usara la que
// tenga disponible.
lector.start(
    { facingMode: "environment" },
    configuracionCamara,
    async (textoDetectado) => {
        if (yaProcesando) return;
        yaProcesando = true;

        mensajeEstado.textContent = "Código detectado, iniciando sesión...";
        mensajeEstado.className = "mensaje info";

        try {
            await lector.stop(); // Apagamos la camara, ya no la necesitamos

            const usuario = await apiFetch("/api/auth/login-qr", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ qr: textoDetectado })
            });

            guardarSesion(usuario);
            window.location.href = "cliente-app.html";
        } catch (error) {
            mensajeEstado.textContent = "Código QR inválido: " + error.message;
            mensajeEstado.className = "mensaje error";
            yaProcesando = false;
            // Reintentamos la camara para que el cliente pueda intentar de nuevo
            lector.start({ facingMode: "environment" }, configuracionCamara, arguments.callee);
        }
    },
    () => { /* Este callback se llama muchas veces por segundo mientras NO
                detecta nada; lo dejamos vacio a proposito. */ }
).then(() => {
    mensajeEstado.textContent = "Apunta la cámara al código QR de tu mesa.";
    mensajeEstado.className = "mensaje info";
}).catch((error) => {
    mensajeEstado.textContent = "No se pudo acceder a la cámara: " + error;
    mensajeEstado.className = "mensaje error";
});
