package com.barmusic.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ============================================================================
 *  CONFIGURACION DE CORS
 * ============================================================================
 *  ¿QUE ES CORS Y POR QUE LO NECESITO?
 *  Por seguridad, los navegadores bloquean que una pagina web hecha en, por
 *  ejemplo, "http://localhost:5500" pueda pedir datos a un servidor en
 *  "http://localhost:8080". Como en este proyecto el HTML/JS del cliente a
 *  veces se prueba por separado del backend, habilitamos CORS para permitir
 *  esas peticiones durante el desarrollo.
 *
 *  IMPORTANTE PARA PRODUCCION:
 *  Cuando publiques la app en internet de verdad, cambia el
 *  ".allowedOrigins("*")" de abajo por el dominio real de tu pagina
 *  (por ejemplo: "https://mibar.com"), para no dejar la puerta abierta a
 *  cualquier sitio del mundo.
 * ============================================================================
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")           // Aplica a todas las rutas que empiecen por /api/
                .allowedOrigins("*")              // <-- CAMBIA ESTO en produccion por tu dominio real
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
