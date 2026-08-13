package com.barmusic.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * ============================================================================
 *  CONFIGURACION DE LAS CREDENCIALES DE SPOTIFY
 * ============================================================================
 *  Esta clase simplemente "lee" los valores que pusiste en el archivo
 *  application.properties (o en variables de entorno) y los deja
 *  disponibles como propiedades Java normales para que el resto del
 *  programa (SpotifyService) los pueda usar.
 *
 *  ¿DONDE CONSIGO ESTOS DATOS?
 *  1. Entra a https://developer.spotify.com/dashboard
 *  2. Inicia sesion con tu cuenta de Spotify.
 *  3. Crea una "App" nueva (dale cualquier nombre, ej: "BarMusicApp").
 *  4. Ahi veras tu "Client ID" y tu "Client Secret".
 *  5. Copia esos dos valores dentro de application.properties (ver ese
 *     archivo, tiene instrucciones exactas de donde pegarlos).
 *  6. Tambien necesitas el ID de la PLAYLIST que quieres sincronizar. Lo
 *     obtienes abriendo la playlist en Spotify -> "Compartir" ->
 *     "Copiar enlace". El enlace se ve asi:
 *        https://open.spotify.com/playlist/37i9dQZF1DXcBWIGoYBM5M
 *     El texto despues de "/playlist/" (37i9dQZF1DXcBWIGoYBM5M) es el ID.
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

    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }
    public String getPlaylistId() { return playlistId; }
}
