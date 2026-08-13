package com.barmusic.app.repository;

import com.barmusic.app.model.CancionActiva;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CancionActivaRepository extends JpaRepository<CancionActiva, Integer> {

    // Todas las solicitudes activas de UN usuario en especifico
    // (Spring entiende "UsuarioSolicitaId" porque en la entidad
    //  CancionActiva el campo se llama "usuarioSolicita" y su "id").
    List<CancionActiva> findByUsuarioSolicita_Id(Integer idUsuario);
}
