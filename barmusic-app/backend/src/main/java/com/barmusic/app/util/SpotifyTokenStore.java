package com.barmusic.app.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ============================================================================
 *  UTILIDAD: SpotifyTokenStore
 * ============================================================================
 *  ¿PARA QUE SIRVE ESTO?
 *  Desde el cambio de Spotify de febrero de 2026, para leer las canciones de
 *  una playlist el backend YA NO puede autenticarse solo como "aplicacion"
 *  (flujo Client Credentials); tiene que demostrar que representa a la
 *  cuenta de Spotify DUEÑA de la playlist (flujo Authorization Code, ver
 *  SpotifyAuthController).
 *
 *  Ese flujo funciona asi: el dueño de la cuenta de Spotify inicia sesion
 *  UNA VEZ en su navegador y autoriza la app. A cambio, Spotify nos entrega
 *  un "refresh token" - una especie de llave que NO expira (mientras no se
 *  revoque) y que el backend puede usar una y otra vez, para siempre, para
 *  pedir nuevos "access tokens" sin que el dueño tenga que volver a
 *  iniciar sesion manualmente.
 *
 *  Esta clase simplemente GUARDA ese refresh token en un archivo de texto
 *  plano en el disco, y lo vuelve a leer cada vez que el servidor arranca.
 *
 *  NOTA DE SEGURIDAD: en un proyecto real de produccion, este token
 *  deberia guardarse encriptado o en un gestor de secretos, no en texto
 *  plano. Para un proyecto academico/bar pequeño, un archivo local es
 *  suficiente y mucho mas facil de entender.
 * ============================================================================
 */
public class SpotifyTokenStore {

    // Carpeta y archivo donde se guarda el refresh token. Se crea sola la
    // primera vez que alguien completa el login con Spotify.
    private static final Path CARPETA = Paths.get("./spotify-data");
    private static final Path ARCHIVO = CARPETA.resolve("refresh_token.txt");

    // Guarda (o reemplaza) el refresh token en el archivo.
    public static void guardar(String refreshToken) throws IOException {
        Files.createDirectories(CARPETA);
        Files.writeString(ARCHIVO, refreshToken);
    }

    // Lee el refresh token guardado. Devuelve null si todavia nadie ha
    // hecho el login con Spotify (archivo no existe).
    public static String leer() {
        try {
            if (!Files.exists(ARCHIVO)) return null;
            String contenido = Files.readString(ARCHIVO).trim();
            return contenido.isEmpty() ? null : contenido;
        } catch (IOException e) {
            System.out.println("[SpotifyTokenStore] No se pudo leer el refresh token: " + e.getMessage());
            return null;
        }
    }

    public static boolean hayTokenGuardado() {
        return leer() != null;
    }
}
