package com.barmusic.app.service;

import com.barmusic.app.config.SpotifyConfig;
import com.barmusic.app.model.CatalogoCancion;
import com.barmusic.app.repository.CatalogoCancionRepository;
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
 *  Este es el modulo que habla con la API oficial de Spotify para leer las
 *  canciones de una playlist y guardarlas en nuestra tabla catalogocanciones_cc.
 *
 *  FLUJO EXPLICADO PASO A PASO:
 *  1) PEDIR UN "TOKEN" DE ACCESO: Spotify no deja consultar nada sin antes
 *     autenticarse. Usamos el flujo "Client Credentials" (el mas simple),
 *     que sirve para leer datos PUBLICOS como una playlist publica. Le
 *     enviamos nuestro Client ID + Client Secret y Spotify nos regresa un
 *     "access_token" que vale por 1 hora.
 *  2) CON ESE TOKEN, pedimos la lista de canciones de la playlist indicada
 *     (endpoint GET /v1/playlists/{id}/tracks).
 *  3) Recorremos cada cancion recibida y, si NO existe ya en nuestra tabla
 *     catalogocanciones_cc (comparando nombre + artista), la insertamos.
 *
 *  IMPORTANTE: Si tu playlist es PRIVADA (no publica), el flujo "Client
 *  Credentials" NO tendra permiso para leerla. En ese caso necesitarias el
 *  flujo "Authorization Code" (login manual del dueño de la playlist una
 *  sola vez). Para simplificar y que un nivel 0 pueda usarlo, este proyecto
 *  asume una playlist PUBLICA.
 * ============================================================================
 */
@Service
public class SpotifyService {

    @Autowired private SpotifyConfig spotifyConfig;
    @Autowired private CatalogoCancionRepository catalogoRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String URL_TOKEN = "https://accounts.spotify.com/api/token";
    private static final String URL_PLAYLIST_TRACKS = "https://api.spotify.com/v1/playlists/%s/tracks?limit=100";

    /**
     * PASO 1: Pide a Spotify un token de acceso temporal.
     */
    private String obtenerAccessToken() {
        // Spotify exige mandar "Client ID:Client Secret" codificado en Base64
        // dentro del header "Authorization: Basic ..."
        String credenciales = spotifyConfig.getClientId() + ":" + spotifyConfig.getClientSecret();
        String credencialesBase64 = Base64.getEncoder().encodeToString(credenciales.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + credencialesBase64);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> peticion = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> respuesta = restTemplate.postForObject(URL_TOKEN, peticion, Map.class);

        if (respuesta == null || !respuesta.containsKey("access_token")) {
            throw new IllegalStateException("No se pudo obtener el token de Spotify. Revisa tu Client ID / Secret.");
        }
        return (String) respuesta.get("access_token");
    }

    /**
     * PASO 2 y 3: Consulta la playlist y guarda las canciones nuevas.
     * Este es el metodo que llama la tarea programada cada 5 minutos.
     */
    @SuppressWarnings("unchecked")
    public void sincronizarCatalogoDesdeSpotify() {
        try {
            String token = obtenerAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> peticion = new HttpEntity<>(headers);

            String url = String.format(URL_PLAYLIST_TRACKS, spotifyConfig.getPlaylistId());
            ResponseEntity<Map> respuesta = restTemplate.exchange(url, HttpMethod.GET, peticion, Map.class);

            Map<String, Object> cuerpo = respuesta.getBody();
            if (cuerpo == null || !cuerpo.containsKey("items")) {
                System.out.println("[SpotifySync] La respuesta de Spotify no trajo canciones.");
                return;
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) cuerpo.get("items");
            int nuevasGuardadas = 0;

            for (Map<String, Object> item : items) {
                Map<String, Object> track = (Map<String, Object>) item.get("track");
                if (track == null) continue; // Puede venir nulo si la cancion fue borrada de Spotify

                String nombreCancion = (String) track.get("name");
                List<Map<String, Object>> artistas = (List<Map<String, Object>>) track.get("artists");
                String nombreArtista = artistas.isEmpty() ? "Desconocido" : (String) artistas.get(0).get("name");

                // Limitamos a 45 caracteres porque asi se definio la columna en la BD
                nombreCancion = recortar(nombreCancion, 45);
                nombreArtista = recortar(nombreArtista, 45);

                // Evitamos duplicados: solo insertamos si NO existe ya
                boolean yaExiste = catalogoRepository.findByNombreAndArtistas(nombreCancion, nombreArtista).isPresent();
                if (!yaExiste) {
                    catalogoRepository.save(new CatalogoCancion(nombreCancion, nombreArtista));
                    nuevasGuardadas++;
                }
            }

            System.out.println("[SpotifySync] Sincronizacion completada. Canciones nuevas guardadas: " + nuevasGuardadas);

        } catch (Exception error) {
            // IMPORTANTE: atrapamos cualquier error (sin internet, credenciales
            // invalidas, etc) para que la tarea programada NO se caiga y lo
            // vuelva a intentar en el siguiente ciclo de 5 minutos.
            System.out.println("[SpotifySync] Error al sincronizar con Spotify: " + error.getMessage());
        }
    }

    private String recortar(String texto, int maximo) {
        if (texto == null) return "";
        return texto.length() > maximo ? texto.substring(0, maximo) : texto;
    }
}
