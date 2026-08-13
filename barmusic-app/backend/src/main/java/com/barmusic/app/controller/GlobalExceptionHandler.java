package com.barmusic.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * ============================================================================
 *  MANEJADOR GLOBAL DE ERRORES: GlobalExceptionHandler
 * ============================================================================
 *  ¿PARA QUE SIRVE ESTO?
 *  Sin esta clase, cuando algo falla dentro de un Service/Controller (por
 *  ejemplo, pedir una cancion con un ID que no existe), Spring devuelve una
 *  pagina de error HTML generica ("Whitelabel Error Page") en vez de un
 *  JSON. El frontend (config.js) espera SIEMPRE recibir JSON con un campo
 *  "mensaje", asi que al recibir HTML no lo puede leer y muestra el
 *  mensaje generico "Ocurrio un error al comunicarse con el servidor".
 *
 *  Con @RestControllerAdvice, esta clase "escucha" los errores de TODOS los
 *  controladores de la aplicacion y los convierte en una respuesta JSON
 *  clara, con el mensaje real del problema. Asi, en el navegador (o en la
 *  pestaña Network de las herramientas de desarrollador) podras ver
 *  exactamente que fallo.
 * ============================================================================
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Se dispara cuando el codigo hace ".orElseThrow(...)" y el elemento no
    // existe (ej: pedir una cancion con un ID de catalogo que no existe,
    // o marcar como "Completada" una solicitud que ya fue eliminada antes).
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(NoSuchElementException error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", error.getMessage()));
    }

    // Red de seguridad: cualquier otro error inesperado tambien se
    // devuelve como JSON legible, en vez de una pagina HTML.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarErrorGeneral(Exception error) {
        // Imprimimos el error COMPLETO en la consola del servidor (con su
        // stack trace) para que, como desarrollador, puedas diagnosticarlo.
        error.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error interno: " + error.getMessage()));
    }
}
