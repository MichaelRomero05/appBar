package com.barmusic.app.controller;

import com.barmusic.app.model.Usuario;
import com.barmusic.app.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * ============================================================================
 *  CONTROLADOR: AuthController
 * ============================================================================
 *  ¿QUE ES UN "CONTROLLER" EN SPRING?
 *  Es la puerta de entrada de las peticiones HTTP que llegan desde el
 *  navegador (o desde JavaScript usando fetch()). Su UNICO trabajo es:
 *    1. Recibir los datos de la peticion (JSON, parametros, etc).
 *    2. Llamar al Service correspondiente para que haga el trabajo pesado.
 *    3. Devolver una respuesta (normalmente en formato JSON).
 *
 *  Este controlador maneja el INICIO DE SESION, tanto de clientes como de
 *  administradores (ambos usan la misma tabla usuarios_u, se diferencian
 *  por el campo "rol").
 *
 *  ¿COMO SE PRUEBA ESTO SIN EL FRONTEND? (util para aprender)
 *  Usando Postman o la extension "Thunder Client" de VSCode, puedes hacer:
 *    POST http://localhost:8080/api/auth/login
 *    Body (JSON): { "login": "admin@correo.com", "contrasena": "admin123" }
 * ============================================================================
 */
@RestController                  // Le dice a Spring: "esta clase responde JSON, no paginas HTML"
@RequestMapping("/api/auth")     // Todas las rutas de esta clase empiezan con /api/auth
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Endpoint: POST /api/auth/login
     * Recibe usuario y contraseña, valida, y devuelve los datos del usuario
     * (SIN la contraseña) si todo esta correcto.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> datos) {
        String login = datos.get("login");
        String contrasena = datos.get("contrasena");

        Optional<Usuario> usuarioValido = usuarioService.validarLogin(login, contrasena);

        if (usuarioValido.isEmpty()) {
            // 401 = "no autorizado". Es el codigo HTTP estandar para login fallido.
            return ResponseEntity.status(401).body(Map.of("mensaje", "Usuario o contraseña incorrectos"));
        }

        return ResponseEntity.ok(construirRespuestaSegura(usuarioValido.get()));
    }

    /**
     * Endpoint: POST /api/auth/login-qr
     * Recibe el texto leido por el escaner QR (pagina 3 del mockup) y busca
     * el usuario dueño de ese codigo.
     */
    @PostMapping("/login-qr")
    public ResponseEntity<?> loginPorQr(@RequestBody Map<String, String> datos) {
        String textoQr = datos.get("qr");
        Optional<Usuario> usuario = usuarioService.loginPorQr(textoQr);

        if (usuario.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Codigo QR invalido"));
        }

        return ResponseEntity.ok(construirRespuestaSegura(usuario.get()));
    }

    /**
     * Endpoint: POST /api/auth/registro
     * Crea una cuenta de cliente nueva (usado si tu bar deja auto-registro).
     */
    @PostMapping("/registro")
    public ResponseEntity<?> registrarCliente(@RequestBody Usuario usuarioNuevo) {
        usuarioNuevo.setRol("CLIENTE"); // Forzamos el rol: nadie se auto-registra como admin
        usuarioNuevo.setFechaCreacion(java.time.LocalDate.now());
        Usuario creado = usuarioService.crearUsuario(usuarioNuevo);
        return ResponseEntity.ok(construirRespuestaSegura(creado));
    }

    /**
     * IMPORTANTE POR SEGURIDAD:
     * Nunca devolvemos el objeto Usuario completo (traeria la contraseña
     * encriptada). Construimos manualmente un Map con SOLO los datos que el
     * frontend necesita.
     */
    private Map<String, Object> construirRespuestaSegura(Usuario usuario) {
        return Map.of(
                "id", usuario.getId(),
                "nombre", usuario.getNombre(),
                "correo", usuario.getCorreo(),
                "rol", usuario.getRol()
        );
    }
}
