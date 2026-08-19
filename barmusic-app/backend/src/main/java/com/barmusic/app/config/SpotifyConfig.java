package com.barmusic.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * ============================================================================
 *  CONFIGURACION DE LAS CREDENCIALES DE SPOTIFY
 * ============================================================================
 *  IMPORTANTE (actualizado tras el cambio de Spotify de febrero 2026):
 *  Ya no basta con Client ID + Client Secret para leer canciones de una
 *  playlist. Ahora se necesita ademas que el DUEÑO de la cuenta de Spotify
 *  (que debe tener Spotify Premium) inicie sesion UNA VEZ en su navegador
 *  para autorizar la app (ver SpotifyAuthController). Por eso agregamos
 *  aqui el "redirect URI" y los "scopes" (permisos) necesarios para ese
 *  login.
 *
 *  ¿DONDE CONSIGO ESTOS DATOS?
 *  1. Entra a https://developer.spotify.com/dashboard con la cuenta de
 *     Spotify que sea DUEÑA (o colaboradora) de la playlist que quieres
 *     sincronizar. Esa cuenta debe tener Spotify Premium activo.
 *  2. Crea una App (o usa la que ya tenias). Copia el "Client ID" y el
 *     "Client Secret" en application.properties.
 *  3. En "Settings" de tu app, en el campo "Redirect URIs", agrega
 *     EXACTAMENTE esta direccion (debe coincidir letra por letra con la
 *     que tengas en spotify.redirect.uri de application.properties):
 *        http://localhost:8080/api/spotify/callback
 *  4. El ID de la playlist se obtiene igual que antes (desde el enlace de
 *     "Compartir" de la playlist en Spotify).
 * ============================================================================
 */
@Configuration
public class SpotifyConfig {

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    @Value("${spotify.playlist.id}")
    private String playlistId;

    // Debe coincidir EXACTO con lo que registraste en el Dashboard de Spotify
    @Value("${spotify.redirect.uri:http://127.0.0.1:8080/api/spotify/callback}")
    private String redirectUri;

    // Permisos que le pedimos al usuario al iniciar sesion: leer playlists
    // privadas propias y playlists colaborativas.
    @Value("${spotify.scopes:playlist-read-private playlist-read-collaborative}")
    private String scopes;

    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }
    public String getPlaylistId() { return playlistId; }
    public String getRedirectUri() { return redirectUri; }
    public String getScopes() { return scopes; }
}
