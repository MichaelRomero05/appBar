package com.barmusic.app.model;

import jakarta.persistence.*;

/**
 * ENTIDAD: CatalogoCancion -> tabla "catalogocanciones_cc"
 * Representa cada cancion disponible para pedir en el bar.
 * Esta tabla se llena SOLA cada 5 minutos gracias a SpotifySyncTask.
 */
@Entity
@Table(name = "catalogocanciones_cc")
public class CatalogoCancion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cc_id")
    private Integer id;

    @Column(name = "cc_nombre", length = 45, nullable = false)
    private String nombre;

    @Column(name = "cc_artistas", length = 45, nullable = false)
    private String artistas;

    public CatalogoCancion() {}

    public CatalogoCancion(String nombre, String artistas) {
        this.nombre = nombre;
        this.artistas = artistas;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getArtistas() { return artistas; }
    public void setArtistas(String artistas) { this.artistas = artistas; }
}
