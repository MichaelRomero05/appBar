package com.barmusic.app.service;

import com.barmusic.app.model.Usuario;
import com.barmusic.app.repository.UsuarioRepository;
import com.barmusic.app.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * ============================================================================
 *  SERVICIO: UsuarioService
 * ============================================================================
 *  ¿QUE HACE UN "SERVICE" EN SPRING?
 *  Aqui va la LOGICA DE NEGOCIO (las reglas del sistema), separada de:
 *   - Los controladores (que solo reciben/responden peticiones HTTP)
 *   - Los repositorios (que solo hablan con la base de datos)
 *  Esto se llama "arquitectura en capas" y hace que el codigo sea mas
 *  facil de mantener y de probar.
 * ============================================================================
 */
@Service
public class UsuarioService {

    @Autowired // Spring "inyecta" automaticamente una instancia de este repositorio
    private UsuarioRepository usuarioRepository;

    /**
     * Verifica que el login + contraseña sean correctos.
     * @return el Usuario si las credenciales son validas, o Optional.empty() si no.
     */
    public Optional<Usuario> validarLogin(String login, String contrasenaPlana) {
        Optional<Usuario> posibleUsuario = usuarioRepository.findByLogin(login);

        if (posibleUsuario.isEmpty()) {
            return Optional.empty(); // No existe ese usuario
        }

        Usuario usuario = posibleUsuario.get();
        boolean claveCorrecta = PasswordUtil.coincide(contrasenaPlana, usuario.getContrasena());

        return claveCorrecta ? Optional.of(usuario) : Optional.empty();
    }

    /**
     * Busca un usuario a partir del texto leido desde un codigo QR escaneado.
     */
    public Optional<Usuario> loginPorQr(String textoQr) {
        return usuarioRepository.findByQr(textoQr);
    }

    /**
     * Crea un usuario nuevo. Aqui se encripta la contraseña automaticamente
     * y se genera un codigo QR unico usando UUID (un generador de codigos
     * aleatorios prácticamente imposibles de repetir).
     */
    public Usuario crearUsuario(Usuario usuarioNuevo) {
        usuarioNuevo.setContrasena(PasswordUtil.encriptar(usuarioNuevo.getContrasena()));
        usuarioNuevo.setQr("QR-" + UUID.randomUUID());
        if (usuarioNuevo.getRol() == null || usuarioNuevo.getRol().isBlank()) {
            usuarioNuevo.setRol("CLIENTE"); // Por defecto, todo usuario nuevo es cliente
        }
        return usuarioRepository.save(usuarioNuevo);
    }

    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }
}
