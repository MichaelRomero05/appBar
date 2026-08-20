package com.barmusic.app.controller;

import com.barmusic.app.model.Notificacion;
import com.barmusic.app.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ============================================================================
 *  CONTROLADOR: NotificacionController
 * ============================================================================
 *  Paginas 10 y 11 (ADMIN): crear notificaciones con texto y/o imagen.
 *  Pagina 6 (CLIENTE): ver las notificaciones enviadas.
 *
 *  ¿POR QUE "consumes = MULTIPART_FORM_DATA" EN VEZ DE JSON?
 *  Cuando se sube un ARCHIVO (la imagen) desde un formulario HTML, no se
 *  puede mandar como JSON normal. Se usa un formato especial llamado
 *  "multipart/form-data" que permite mezclar texto y archivos binarios en
 *  una sola peticion. El navegador arma esto automaticamente cuando usamos
 *  la clase JavaScript "FormData" (ver admin-notificaciones.js).
 * ============================================================================
 */
@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    // GET /api/notificaciones -> Pagina 6: el cliente ve todos los avisos
    @GetMapping
    public List<Notificacion> listar() {
        return notificacionService.listarTodas();
    }

    // DELETE /api/notificaciones/12 -> Pagina 9/10: el admin borra un aviso
    // (boton de basurita). Devuelve 204 (No Content): la operacion salio
    // bien pero no hay ningun dato que devolver de vuelta.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        notificacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/notificaciones (multipart/form-data con campos "texto" e "imagen")
    // Pagina 10/11: el administrador crea un aviso nuevo.
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> crear(
            @RequestParam(value = "texto", required = false) String texto,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen) {
        try {
            Notificacion creada = notificacionService.crear(texto, imagen);
            return ResponseEntity.ok(creada);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al guardar la notificacion: " + e.getMessage());
        }
    }
}
