package com.barmusic.app.service;

import com.barmusic.app.config.SpotifyConfig;
import com.barmusic.app.model.CatalogoCancion;
import com.barmusic.app.repository.CatalogoCancionRepository;
import com.barmusic.app.util.SpotifyTokenStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 *  SERVICIO: SpotifyService
 * ============================================================================
 *  ACTUALIZADO tras el cambio de Spotify de febrero de 2026: ya NO se usa
 *  el flujo "Client Credentials" (autenticacion solo de la app) para leer
 *  canciones de una playlist, porque Spotify ahora exige que el pedido
 *  venga "a nombre de" el usuario dueño/colaborador de esa playlist.
 *
 *  FLUJO ACTUAL:
 *  1) El administrador del bar hizo, UNA VEZ, el login manual descrito en
 *     SpotifyAuthController (visitando /api/spotify/conectar). Eso dejo
 *     guardado un "refresh token" en disco (ver SpotifyTokenStore).
 *  2) Cada vez que esta clase necesita hablar con Spotify, usa ese
 *     refresh token para pedir un "access token" nuevo (los access token
 *     dependen poco tiempo, minutos; el refresh token en cambio no expira).
 *  3) Con ese access token, se consulta el endpoint RENOMBRADO
 *     GET /v1/playlists/{id}/items (antes se llamaba ".../tracks").
 *
 *  RECORDATORIO IMPORTANTE: la playlist configurada en
 *  spotify.playlist.id DEBE pertenecer (o tener como colaboradora) a la
 *  MISMA cuenta de Spotify que inicio sesion en el paso 1. Si intentas
 *  sincronizar la playlist de otra persona (aunque sea publica), Spotify
 *  devolvera la respuesta sin el campo "items" - no es un error de este
 *  codigo, es una restriccion de la plataforma.
 * ============================================================================
 */
@Service
public class SpotifyService {

    @Autowired private SpotifyConfig spotifyConfig;
    @Autowired private CatalogoCancionRepository catalogoRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String URL_TOKEN = "https://accounts.spotify.com/api/token";
    // Endpoint RENOMBRADO por Spotify en Feb-2026: /tracks -> /items
    private static final String URL_PLAYLIST_ITEMS = "https://api.spotify.com/v1/playlists/%s/items?limit=100";

    /**
     * Usa el refresh token guardado (del login manual) para conseguir un
     * access token fresco. Si nadie ha hecho el login todavia, devuelve
     * null (y quien llame a este metodo debe manejar ese caso).
     */
    private String obtenerAccessTokenConRefreshToken() {
        String refreshToken = SpotifyTokenStore.leer();
        if (refreshToken == null) {
            return null; // Todavia nadie conecto la cuenta de Spotify
        }

        String credenciales = spotifyConfig.getClientId() + ":" + spotifyConfig.getClientSecret();
        String credencialesBase64 = Base64.getEncoder().encodeToString(credenciales.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + credencialesBase64);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> peticion = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> respuesta = restTemplate.postForObject(URL_TOKEN, peticion, Map.class);

        if (respuesta == null || !respuesta.containsKey("access_token")) {
            throw new IllegalStateException("Spotify no devolvio un access_token al refrescar la sesion.");
        }

        // A veces Spotify manda un refresh_token NUEVO junto con el access
        // token (rotacion de tokens). Si viene, lo actualizamos en disco.
        if (respuesta.containsKey("refresh_token")) {
            try {
                SpotifyTokenStore.guardar((String) respuesta.get("refresh_token"));
            } catch (Exception e) {
                System.out.println("[SpotifySync] No se pudo actualizar el refresh_token: " + e.getMessage());
            }
        }

        return (String) respuesta.get("access_token");
    }

    /**
     * Metodo principal, llamado cada 5 minutos por SpotifySyncTask.
     */
    @SuppressWarnings("unchecked")
    public void sincronizarCatalogoDesdeSpotify() {
        try {
            String token = obtenerAccessTokenConRefreshToken();

            if (token == null) {
                System.out.println("[SpotifySync] Todavia no se ha conectado ninguna cuenta de Spotify. "
                        + "Visita http://localhost:8080/api/spotify/conectar para autorizar la app.");
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> peticion = new HttpEntity<>(headers);

            String url = String.format(URL_PLAYLIST_ITEMS, spotifyConfig.getPlaylistId());
            ResponseEntity<Map> respuesta = restTemplate.exchange(url, HttpMethod.GET, peticion, Map.class);

            Map<String, Object> cuerpo = respuesta.getBody();
            if (cuerpo == null || !cuerpo.containsKey("items")) {
                System.out.println("[SpotifySync] La respuesta no trajo canciones. Verifica que la playlist "
                        + "pertenezca (o tenga como colaboradora) a la cuenta de Spotify que conectaste.");
                return;
            }

            // NOTA IMPORTANTE: la respuesta real de Spotify puede venir en
            // 2 formas distintas segun el endpoint/version (esto se
            // confirmo probando en vivo, la documentacion no lo deja del
            // todo claro):
            //   Forma A (plana):    { "items": [ {...}, {...} ] }
            //   Forma B (anidada):  { "items": { "items": [ {...}, {...} ], "total": N, ... } }
            // El siguiente bloque detecta cual de las 2 formas llego y
            // extrae la lista real de canciones sin importar cual sea.
            Object valorItems = cuerpo.get("items");
            List<Map<String, Object>> items;

            if (valorItems instanceof List) {
                // Forma A: "items" ya es directamente la lista de canciones
                items = (List<Map<String, Object>>) valorItems;
            } else if (valorItems instanceof Map) {
                // Forma B: "items" es un objeto de paginacion que tiene
                // adentro OTRO campo "items" con la lista real
                Object listaAnidada = ((Map<String, Object>) valorItems).get("items");
                items = (listaAnidada instanceof List) ? (List<Map<String, Object>>) listaAnidada : List.of();
            } else {
                items = List.of();
            }

            int nuevasGuardadas = 0;

            for (Map<String, Object> entrada : items) {
                // OJO: el campo se llama "item" (antes era "track") desde
                // el cambio de nomenclatura de Spotify de Feb-2026.
                Map<String, Object> item = (Map<String, Object>) entrada.get("item");
                if (item == null) continue; // Puede venir nulo si la cancion fue borrada de Spotify

                String nombreCancion = (String) item.get("name");
                List<Map<String, Object>> artistas = (List<Map<String, Object>>) item.get("artists");
                String nombreArtista = (artistas == null || artistas.isEmpty())
                        ? "Desconocido" : (String) artistas.get(0).get("name");

                nombreCancion = recortar(nombreCancion, 45);
                nombreArtista = recortar(nombreArtista, 45);

                boolean yaExiste = catalogoRepository.findByNombreAndArtistas(nombreCancion, nombreArtista).isPresent();
                if (!yaExiste) {
                    catalogoRepository.save(new CatalogoCancion(nombreCancion, nombreArtista));
                    nuevasGuardadas++;
                }
            }

            System.out.println("[SpotifySync] Sincronizacion completada. Canciones nuevas guardadas: " + nuevasGuardadas);

        } catch (Exception error) {
            System.out.println("[SpotifySync] Error al sincronizar con Spotify: " + error.getMessage());
        }
    }

    private String recortar(String texto, int maximo) {
        if (texto == null) return "";
        return texto.length() > maximo ? texto.substring(0, maximo) : texto;
    }
}
