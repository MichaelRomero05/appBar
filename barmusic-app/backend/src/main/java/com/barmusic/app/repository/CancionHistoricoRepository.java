package com.barmusic.app.repository;

import com.barmusic.app.model.CancionHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CancionHistoricoRepository extends JpaRepository<CancionHistorico, Integer> {
}
