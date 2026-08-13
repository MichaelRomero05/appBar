package com.barmusic.app.repository;

import com.barmusic.app.model.CatalogoCancion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CatalogoCancionRepository extends JpaRepository<CatalogoCancion, Integer> {

    // Usado por SpotifySyncTask para saber si una cancion ya existe antes
    // de insertarla otra vez (evita duplicados en cada sincronizacion).
    Optional<CatalogoCancion> findByNombreAndArtistas(String nombre, String artistas);
}
