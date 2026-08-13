package com.barmusic.app.controller;

import com.barmusic.app.model.CatalogoCancion;
import com.barmusic.app.service.CancionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CONTROLADOR: CatalogoController
 * Usado en la Pagina 4 (catalogo de canciones) para que el cliente vea
 * todas las canciones disponibles para pedir.
 */
@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    @Autowired
    private CancionService cancionService;

    // GET /api/catalogo -> lista completa de canciones disponibles
    @GetMapping
    public List<CatalogoCancion> listar() {
        return cancionService.listarCatalogo();
    }
}
