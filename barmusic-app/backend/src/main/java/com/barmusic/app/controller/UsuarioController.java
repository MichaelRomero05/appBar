package com.barmusic.app.controller;

import com.barmusic.app.model.Usuario;
import com.barmusic.app.service.UsuarioService;
import com.barmusic.app.util.QrCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * ============================================================================
 *  CONTROLADOR: UsuarioController
 * ============================================================================
 *  Endpoint auxiliar para generar la IMAGEN del codigo QR de un usuario
 *  (se usa, por ejemplo, para imprimir el QR y pegarlo en la mesa del bar).
 * ============================================================================
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Endpoint: GET /api/usuarios/{id}/qr
     * Devuelve DIRECTAMENTE una imagen PNG (no JSON). Por eso el navegador
     * puede usarla asi: <img src="http://localhost:8080/api/usuarios/5/qr">
     */
    @GetMapping("/{id}/qr")
    public ResponseEntity<byte[]> obtenerImagenQr(@PathVariable Integer id) {
        Optional<Usuario> usuario = usuarioService.buscarPorId(id);
        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] imagenPng = QrCodeGenerator.generarPng(usuario.get().getQr(), 300, 300);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);

            return new ResponseEntity<>(imagenPng, headers, 200);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
