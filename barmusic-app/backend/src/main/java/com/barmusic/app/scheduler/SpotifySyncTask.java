package com.barmusic.app.scheduler;

import com.barmusic.app.service.SpotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 *  TAREA PROGRAMADA (CRON): SpotifySyncTask
 * ============================================================================
 *  ¿QUE ES ESTO?
 *  Es una clase que Spring ejecuta AUTOMATICAMENTE, sin que nadie tenga que
 *  llamarla, cada cierto tiempo. Es el equivalente en Java a un "CRON job".
 *
 *  @Scheduled(fixedRate = 300000)
 *  "fixedRate" en MILISEGUNDOS. 300000 ms = 5 minutos EXACTOS.
 *  Formula si quieres cambiarlo: minutos x 60 x 1000
 *      1 minuto  ->  60000
 *      5 minutos -> 300000  (valor pedido en los requisitos)
 *     10 minutos -> 600000
 *
 *  REQUISITO OBLIGATORIO: la clase principal (BarMusicApplication) debe
 *  tener la anotacion @EnableScheduling, si no, este metodo NUNCA se
 *  ejecutara aunque el codigo este perfecto.
 * ============================================================================
 */
@Component
public class SpotifySyncTask {

    @Autowired
    private SpotifyService spotifyService;

    @Scheduled(fixedRate = 30000) // Se ejecuta cada 5 minutos desde que arranca el servidor
    public void ejecutarSincronizacionAutomatica() {
        System.out.println("[SpotifySync] Iniciando sincronizacion automatica con Spotify...");
        spotifyService.sincronizarCatalogoDesdeSpotify();
    }
}
