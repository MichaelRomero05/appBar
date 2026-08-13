package com.barmusic.app.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * ============================================================================
 *  UTILIDAD: PasswordUtil
 * ============================================================================
 *  REGLA DE ORO DE SEGURIDAD: nunca, JAMAS, guardes contraseñas en texto
 *  plano en la base de datos. Si alguien roba tu base de datos, tendria
 *  acceso inmediato a las contraseñas de todos tus usuarios.
 *
 *  En vez de eso usamos "BCrypt", un algoritmo que convierte la contraseña
 *  en un texto irreconocible (encriptado) y que ademas es imposible de
 *  "revertir" (no hay forma de sacar la contraseña original a partir del
 *  texto encriptado). Para verificar el login, no "desencriptamos", sino
 *  que volvemos a encriptar lo que el usuario escribio y comparamos si
 *  el resultado coincide.
 * ============================================================================
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    // Convierte una contraseña en texto plano a su version encriptada.
    // USALA cada vez que crees un usuario nuevo, antes de guardar en la BD.
    public static String encriptar(String contrasenaPlana) {
        return ENCODER.encode(contrasenaPlana);
    }

    // Compara una contraseña escrita por el usuario contra el hash guardado
    // en la base de datos. Devuelve true si coinciden.
    public static boolean coincide(String contrasenaPlana, String contrasenaEncriptada) {
        return ENCODER.matches(contrasenaPlana, contrasenaEncriptada);
    }
}
