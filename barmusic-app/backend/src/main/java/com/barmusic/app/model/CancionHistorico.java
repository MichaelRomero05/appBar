package com.barmusic.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ENTIDAD: CancionHistorico -> tabla "cancioneshistorico_ch"
 * Aqui "aterrizan" las solicitudes ya finalizadas (completadas, eliminadas
 * por el admin, o canceladas por el propio cliente). Sirve como historial /
 * auditoria de todo lo que ha pasado en el bar.
 */
@Entity
@Table(name = "cancioneshistorico_ch")
public class CancionHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ch_id")
    private Integer id;

    // Guardamos el ID que tenia el registro cuando estaba en cancionesactivas_ca
    @Column(name = "ch_idcancionesactivas", nullable = false)
    private Integer idCancionesActivasOriginal;

    @ManyToOne
    @JoinColumn(name = "ch_idcatalogo", nullable = false)
    private CatalogoCancion catalogo;

    @ManyToOne
    @JoinColumn(name = "ch_idusuariosolicita", nullable = false)
    private Usuario usuarioSolicita;

    // Valores esperados: "COMPLETADA" | "ELIMINADA" | "CANCELADA"
    @Column(name = "ch_estado", length = 20, nullable = false)
    private String estado;

    @Column(name = "ch_fechasolicita", nullable = false)
    private LocalDateTime fechaSolicita;

    public CancionHistorico() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getIdCancionesActivasOriginal() { return idCancionesActivasOriginal; }
    public void setIdCancionesActivasOriginal(Integer idCancionesActivasOriginal) { this.idCancionesActivasOriginal = idCancionesActivasOriginal; }

    public CatalogoCancion getCatalogo() { return catalogo; }
    public void setCatalogo(CatalogoCancion catalogo) { this.catalogo = catalogo; }

    public Usuario getUsuarioSolicita() { return usuarioSolicita; }
    public void setUsuarioSolicita(Usuario usuarioSolicita) { this.usuarioSolicita = usuarioSolicita; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaSolicita() { return fechaSolicita; }
    public void setFechaSolicita(LocalDateTime fechaSolicita) { this.fechaSolicita = fechaSolicita; }
}
