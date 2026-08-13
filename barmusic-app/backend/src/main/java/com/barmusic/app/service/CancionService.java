package com.barmusic.app.service;

import com.barmusic.app.model.*;
import com.barmusic.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * ============================================================================
 *  SERVICIO: CancionService
 * ============================================================================
 *  Contiene TODA la logica relacionada con el ciclo de vida de una solicitud
 *  de cancion:
 *    1) El cliente la solicita       -> se crea en cancionesactivas_ca
 *    2) El admin la marca Completada -> se mueve a cancioneshistorico_ch
 *    3) El admin la Elimina          -> se mueve a cancioneshistorico_ch
 *    4) El cliente la Cancela (X)    -> se mueve a cancioneshistorico_ch
 * ============================================================================
 */
@Service
public class CancionService {

    @Autowired private CatalogoCancionRepository catalogoRepository;
    @Autowired private CancionActivaRepository activaRepository;
    @Autowired private CancionHistoricoRepository historicoRepository;
    @Autowired private UsuarioService usuarioService;

    // ------------------------------------------------------------------
    // PAGINA 4 (Catalogo): listar todas las canciones disponibles
    // ------------------------------------------------------------------
    public List<CatalogoCancion> listarCatalogo() {
        return catalogoRepository.findAll();
    }

    // ------------------------------------------------------------------
    // PAGINA 4: el cliente selecciona una cancion y la solicita
    // ------------------------------------------------------------------
    public CancionActiva solicitarCancion(Integer idCatalogo, Integer idUsuario) {
        CatalogoCancion cancion = catalogoRepository.findById(idCatalogo)
                .orElseThrow(() -> new NoSuchElementException("La cancion no existe en el catalogo"));
        Usuario usuario = usuarioService.buscarPorId(idUsuario)
                .orElseThrow(() -> new NoSuchElementException("El usuario no existe"));

        CancionActiva solicitud = new CancionActiva();
        solicitud.setCatalogo(cancion);
        solicitud.setUsuarioSolicita(usuario);
        solicitud.setFechaSolicita(LocalDateTime.now());

        return activaRepository.save(solicitud);
    }

    // ------------------------------------------------------------------
    // PAGINA 5: "Mis solicitudes" del cliente logueado
    // ------------------------------------------------------------------
    public List<CancionActiva> listarSolicitudesDeUsuario(Integer idUsuario) {
        return activaRepository.findByUsuarioSolicita_Id(idUsuario);
    }

    // ------------------------------------------------------------------
    // PAGINA 9 (Admin): todas las solicitudes activas de TODOS los clientes
    // ------------------------------------------------------------------
    public List<CancionActiva> listarTodasLasSolicitudesActivas() {
        return activaRepository.findAll();
    }

    /**
     * Metodo interno reutilizado por completar(), eliminar() y cancelar().
     * @Transactional asegura que el "borrar de activas" + "insertar en
     * historico" ocurran como UNA sola operacion: si algo falla a mitad de
     * camino, se revierte todo (no queda la BD en un estado incoherente).
     */
    @Transactional
    protected CancionHistorico moverAHistorico(Integer idSolicitudActiva, String estado) {
        CancionActiva activa = activaRepository.findById(idSolicitudActiva)
                .orElseThrow(() -> new NoSuchElementException("La solicitud activa no existe"));

        CancionHistorico historico = new CancionHistorico();
        historico.setIdCancionesActivasOriginal(activa.getId());
        historico.setCatalogo(activa.getCatalogo());
        historico.setUsuarioSolicita(activa.getUsuarioSolicita());
        historico.setEstado(estado);
        historico.setFechaSolicita(activa.getFechaSolicita());

        CancionHistorico guardado = historicoRepository.save(historico);
        activaRepository.delete(activa); // La borramos de "activas" porque ya se resolvio

        return guardado;
    }

    // PAGINA 9 (Admin) - boton "Completar"
    public CancionHistorico completarSolicitud(Integer idSolicitudActiva) {
        return moverAHistorico(idSolicitudActiva, "COMPLETADA");
    }

    // PAGINA 9 (Admin) - boton "Eliminar"
    public CancionHistorico eliminarSolicitud(Integer idSolicitudActiva) {
        return moverAHistorico(idSolicitudActiva, "ELIMINADA");
    }

    // PAGINA 5 (Cliente) - boton "X" para cancelar su propia solicitud
    public CancionHistorico cancelarSolicitud(Integer idSolicitudActiva) {
        return moverAHistorico(idSolicitudActiva, "CANCELADA");
    }
}
