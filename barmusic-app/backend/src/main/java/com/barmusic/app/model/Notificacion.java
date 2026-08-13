package com.barmusic.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ENTIDAD: Notificacion -> tabla "notificaciones_n"
 * Avisos que el administrador envia a todos los clientes (texto y/o imagen).
 */
@Entity
@Table(name = "notificaciones_n")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idnotificacion")
    private Integer id;

    @Column(name = "n_texto", length = 255)
    private String texto;

    @Column(name = "n_rutaimagen", length = 255)
    private String rutaImagen;

    @Column(name = "n_fechacreacion", nullable = false)
    private LocalDateTime fechaCreacion;

    public Notificacion() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getRutaImagen() { return rutaImagen; }
    public void setRutaImagen(String rutaImagen) { this.rutaImagen = rutaImagen; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
