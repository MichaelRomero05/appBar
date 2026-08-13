package com.barmusic.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ============================================================================
 *  CLASE PRINCIPAL - PUNTO DE ARRANQUE DE TODA LA APLICACION
 * ============================================================================
 *  ¿QUE ES ESTO?
 *  Todo programa Java necesita un metodo "main" por donde empieza a
 *  ejecutarse. En una app Spring Boot, esta clase es la que "enciende" el
 *  servidor web interno (Tomcat embebido) y deja todo listo para recibir
 *  peticiones desde el navegador.
 *
 *  ¿COMO LA USO?
 *  - Para levantar el servidor en tu computador: clic derecho -> "Run" sobre
 *    este archivo (en IntelliJ/Eclipse/VSCode), o en la terminal:
 *        mvn spring-boot:run
 *  - Una vez levantado, veras un mensaje en la consola parecido a:
 *        Tomcat started on port(s): 8080
 *    Eso significa que ya puedes abrir tu navegador en:
 *        http://localhost:8080
 *
 *  @EnableScheduling: esta anotacion es OBLIGATORIA para que la tarea
 *  programada que sincroniza canciones de Spotify cada 5 minutos
 *  (ver clase SpotifySyncTask) funcione. Sin esta anotacion, el "reloj"
 *  interno de Spring nunca se activa.
 * ============================================================================
 */
@SpringBootApplication
@EnableScheduling
public class BarMusicApplication {

    public static void main(String[] args) {
        // Esta linea es la que realmente arranca todo el motor de Spring Boot.
        SpringApplication.run(BarMusicApplication.class, args);
        System.out.println("=========================================================");
        System.out.println(" BarMusicApp iniciada correctamente.");
        System.out.println(" Abre tu navegador en: http://localhost:8080");
        System.out.println("=========================================================");
    }
}
