package com.barmusic.app.repository;

import com.barmusic.app.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    // Trae las notificaciones ordenadas de la mas nueva a la mas vieja
    List<Notificacion> findAllByOrderByFechaCreacionDesc();
}
