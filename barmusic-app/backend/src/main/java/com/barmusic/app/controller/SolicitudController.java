package com.barmusic.app.controller;

import com.barmusic.app.model.CancionActiva;
import com.barmusic.app.model.CancionHistorico;
import com.barmusic.app.service.CancionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 *  CONTROLADOR: SolicitudController
 * ============================================================================
 *  Maneja el ciclo completo de una solicitud de cancion:
 *   - Paginas 4 y 5 (CLIENTE): solicitar y ver/cancelar sus propias solicitudes.
 *   - Pagina 9 (ADMIN): ver TODAS las solicitudes y marcarlas como
 *     Completada o Eliminada.
 * ============================================================================
 */
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    @Autowired
    private CancionService cancionService;

    // --------------------------------------------------------------------
    // ZONA CLIENTE
    // --------------------------------------------------------------------

    // POST /api/solicitudes  Body: { "idCatalogo": 3, "idUsuario": 7 }
    // Pagina 4: boton de check para solicitar una cancion del catalogo.
    @PostMapping
    public ResponseEntity<?> solicitar(@RequestBody Map<String, Integer> datos) {
        CancionActiva creada = cancionService.solicitarCancion(datos.get("idCatalogo"), datos.get("idUsuario"));
        return ResponseEntity.ok(creada);
    }

    // GET /api/solicitudes/usuario/7
    // Pagina 5: "Mis solicitudes" del cliente logueado.
    @GetMapping("/usuario/{idUsuario}")
    public List<CancionActiva> misSolicitudes(@PathVariable Integer idUsuario) {
        return cancionService.listarSolicitudesDeUsuario(idUsuario);
    }

    // DELETE /api/solicitudes/12
    // Pagina 5: boton "X" para que el cliente cancele su propia solicitud.
    @DeleteMapping("/{id}")
    public ResponseEntity<CancionHistorico> cancelar(@PathVariable Integer id) {
        return ResponseEntity.ok(cancionService.cancelarSolicitud(id));
    }

    // --------------------------------------------------------------------
    // ZONA ADMINISTRADOR
    // --------------------------------------------------------------------

    // GET /api/solicitudes  -> TODAS las solicitudes activas (de todos los clientes)
    // Pagina 9: panel de gestion del administrador.
    @GetMapping
    public List<CancionActiva> todasLasSolicitudesActivas() {
        return cancionService.listarTodasLasSolicitudesActivas();
    }

    // POST /api/solicitudes/12/completar
    // Pagina 9: boton "Completar" -> mueve el registro a cancioneshistorico_ch
    @PostMapping("/{id}/completar")
    public ResponseEntity<CancionHistorico> completar(@PathVariable Integer id) {
        return ResponseEntity.ok(cancionService.completarSolicitud(id));
    }

    // POST /api/solicitudes/12/eliminar
    // Pagina 9: boton "Eliminar" -> mueve el registro a cancioneshistorico_ch
    @PostMapping("/{id}/eliminar")
    public ResponseEntity<CancionHistorico> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(cancionService.eliminarSolicitud(id));
    }
}
