package com.barmusic.app.controller;

import com.barmusic.app.config.SpotifyConfig;
import com.barmusic.app.util.SpotifyTokenStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * ============================================================================
 *  CONTROLADOR: SpotifyAuthController
 * ============================================================================
 *  Este controlador maneja el LOGIN MANUAL, UNA SOLA VEZ, que el dueño de
 *  la cuenta de Spotify (con Premium) tiene que hacer para autorizar esta
 *  aplicacion a leer el contenido de su playlist. Despues de este login,
 *  el backend queda con un "refresh token" guardado (ver SpotifyTokenStore)
 *  y la sincronizacion automatica cada 5 minutos funciona sola, sin que
 *  nadie tenga que volver a iniciar sesion (a menos que la playlist cambie
 *  de dueño, o el token sea revocado manualmente desde Spotify).
 *
 *  COMO SE USA (pasos para el administrador del bar):
 *  1. Con el backend corriendo, abre en tu navegador:
 *         http://localhost:8080/api/spotify/conectar
 *  2. Te va a redirigir a la pagina de login de Spotify. Inicia sesion con
 *     la cuenta DUEÑA (o colaboradora) de la playlist que quieres
 *     sincronizar (debe tener Spotify Premium).
 *  3. Spotify te pedira aceptar permisos ("Ver tus playlists privadas y
 *     colaborativas"). Dale "Aceptar".
 *  4. Spotify te redirige de vuelta a esta app, que guarda el token y te
 *     muestra una pagina simple de "Conectado con éxito".
 *  5. Listo, no necesitas repetir esto de nuevo (a menos que el archivo
 *     spotify-data/refresh_token.txt se borre, o Spotify revoque el acceso).
 * ============================================================================
 */
@RestController
@RequestMapping("/api/spotify")
public class SpotifyAuthController {

    @Autowired
    private SpotifyConfig spotifyConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Endpoint: GET /api/spotify/conectar
     * Redirige el navegador hacia la pantalla de login/autorizacion de
     * Spotify. Esto NO es una llamada de API normal (no devuelve JSON):
     * es una redireccion de navegador, por eso el tipo de retorno es
     * ResponseEntity<Void> con status 302 (FOUND / redirect).
     */
    @GetMapping("/conectar")
    public ResponseEntity<Void> conectar() {
        String urlAutorizacion = "https://accounts.spotify.com/authorize"
                + "?response_type=code"
                + "&client_id=" + spotifyConfig.getClientId()
                + "&scope=" + URLEncoder.encode(spotifyConfig.getScopes(), StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(spotifyConfig.getRedirectUri(), StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(java.net.URI.create(urlAutorizacion));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * Endpoint: GET /api/spotify/callback
     * Spotify llama automaticamente a ESTA direccion despues de que el
     * usuario acepta los permisos, mandando un "code" temporal en la URL.
     * Aqui lo intercambiamos por el access_token + refresh_token reales.
     */
    @GetMapping("/callback")
    public ResponseEntity<String> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error) {

        if (error != null) {
            return ResponseEntity.ok(paginaHtml("❌ Autorización cancelada", "Spotify reportó: " + error));
        }

        try {
            String credenciales = spotifyConfig.getClientId() + ":" + spotifyConfig.getClientSecret();
            String credencialesBase64 = Base64.getEncoder().encodeToString(credenciales.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + credencialesBase64);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("redirect_uri", spotifyConfig.getRedirectUri());

            HttpEntity<MultiValueMap<String, String>> peticion = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> respuesta = restTemplate.postForObject(
                    "https://accounts.spotify.com/api/token", peticion, Map.class);

            if (respuesta == null || !respuesta.containsKey("refresh_token")) {
                return ResponseEntity.ok(paginaHtml("❌ Error",
                        "Spotify no devolvió un refresh_token. Intenta de nuevo desde /api/spotify/conectar"));
            }

            String refreshToken = (String) respuesta.get("refresh_token");
            SpotifyTokenStore.guardar(refreshToken);

            return ResponseEntity.ok(paginaHtml("✅ Conectado con éxito",
                    "Ya puedes cerrar esta pestaña. La sincronización automática con tu playlist empezará en el próximo ciclo (cada 5 minutos)."));

        } catch (Exception e) {
            return ResponseEntity.ok(paginaHtml("❌ Error al conectar", e.getMessage()));
        }
    }

    // Pequeña pagina HTML de confirmacion (no usamos plantillas/Thymeleaf
    // para mantener el proyecto simple, solo texto HTML directo en Java).
    private String paginaHtml(String titulo, String mensaje) {
        return "<html><body style='font-family:sans-serif;text-align:center;padding:60px;'>"
                + "<h1>" + titulo + "</h1><p>" + mensaje + "</p></body></html>";
    }
}
