package com.barmusic.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ENTIDAD: CancionActiva -> tabla "cancionesactivas_ca"
 * Cada fila es UNA solicitud de cancion que todavia esta "pendiente"
 * (el DJ / administrador aun no la ha marcado como Completada o Eliminada).
 *
 * NOTA: usamos @ManyToOne hacia Usuario y CatalogoCancion para que Hibernate
 * arme automaticamente las llaves foraneas (ca_idcatalogo / ca_idusuariosolicita)
 * y podamos, por ejemplo, escribir "cancionActiva.getCatalogo().getNombre()"
 * sin tener que hacer una segunda consulta SQL a mano.
 */
@Entity
@Table(name = "cancionesactivas_ca")
public class CancionActiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ca_id")
    private Integer id;

    // "@JoinColumn" indica cual es la columna FK en la tabla cancionesactivas_ca
    @ManyToOne
    @JoinColumn(name = "ca_idcatalogo", nullable = false)
    private CatalogoCancion catalogo;

    @ManyToOne
    @JoinColumn(name = "ca_idusuariosolicita", nullable = false)
    private Usuario usuarioSolicita;

    @Column(name = "ca_fechasolicita", nullable = false)
    private LocalDateTime fechaSolicita;

    public CancionActiva() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public CatalogoCancion getCatalogo() { return catalogo; }
    public void setCatalogo(CatalogoCancion catalogo) { this.catalogo = catalogo; }

    public Usuario getUsuarioSolicita() { return usuarioSolicita; }
    public void setUsuarioSolicita(Usuario usuarioSolicita) { this.usuarioSolicita = usuarioSolicita; }

    public LocalDateTime getFechaSolicita() { return fechaSolicita; }
    public void setFechaSolicita(LocalDateTime fechaSolicita) { this.fechaSolicita = fechaSolicita; }
}
