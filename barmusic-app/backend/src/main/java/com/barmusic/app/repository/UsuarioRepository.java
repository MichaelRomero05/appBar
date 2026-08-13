package com.barmusic.app.repository;

import com.barmusic.app.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * ============================================================================
 *  REPOSITORIO: UsuarioRepository
 * ============================================================================
 *  ¿QUE ES UN "REPOSITORY" EN SPRING?
 *  Es una interfaz (no una clase) donde NO escribimos codigo de logica.
 *  Solo con EXTENDER "JpaRepository<Usuario, Integer>", Spring nos regala
 *  GRATIS un monton de metodos ya hechos: save(), findById(), findAll(),
 *  deleteById(), etc. Es magia: Spring genera el codigo real por detras.
 *
 *  Ademas, si escribimos un metodo con un nombre "especial" como
 *  "findByLogin(String login)", Spring entiende solo, por el NOMBRE del
 *  metodo, que debe generar la consulta SQL equivalente a:
 *      SELECT * FROM usuarios_u WHERE u_login = ?
 *  ¡Sin que nosotros escribamos ni una linea de SQL!
 * ============================================================================
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Busca un usuario por su login (usado en el formulario de inicio de sesion)
    Optional<Usuario> findByLogin(String login);

    // Busca un usuario por el codigo guardado en su QR (usado al escanear el QR)
    Optional<Usuario> findByQr(String qr);
}
