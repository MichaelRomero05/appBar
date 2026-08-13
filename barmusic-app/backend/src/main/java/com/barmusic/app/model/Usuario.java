package com.barmusic.app.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * ============================================================================
 *  ENTIDAD: Usuario  ->  tabla "usuarios_u"
 * ============================================================================
 *  ¿QUE ES UNA "ENTIDAD" EN JAVA?
 *  Es una clase normal de Java que representa UNA FILA de una tabla de la
 *  base de datos. Gracias a las anotaciones (las palabras que empiezan con
 *  @), Spring/Hibernate sabe automaticamente como convertir esta clase en
 *  filas de SQL y viceversa. Asi, en el resto del codigo, trabajamos con
 *  "objetos Java" (usuario.getNombre()) en vez de escribir SQL a mano.
 *
 *  CADA ATRIBUTO DE ABAJO CORRESPONDE A UNA COLUMNA DE LA TABLA usuarios_u.
 * ============================================================================
 */
@Entity                       // Le dice a Spring: "esto es una tabla de base de datos"
@Table(name = "usuarios_u")   // Nombre EXACTO de la tabla en MySQL
public class Usuario {

    @Id                                                  // Esta columna es la llave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Se autogenera (AUTO_INCREMENT)
    @Column(name = "u_id")
    private Integer id;

    @Column(name = "u_nombre", length = 45, nullable = false)
    private String nombre;

    @Column(name = "u_correo", length = 45, nullable = false, unique = true)
    private String correo;

    @Column(name = "u_login", length = 45, nullable = false, unique = true)
    private String login;

    @Column(name = "u_contrasena", nullable = false)
    private String contrasena; // Se guarda SIEMPRE encriptada (ver PasswordUtil)

    @Column(name = "u_qr")
    private String qr; // Codigo unico que se convierte en imagen QR

    @Column(name = "u_fechacreacion", nullable = false)
    private LocalDate fechaCreacion;

    @Column(name = "u_rol", length = 20, nullable = false)
    private String rol; // "CLIENTE" o "ADMINISTRADOR" (ver nota en el script SQL)

    // --------------------------------------------------------------------
    // CONSTRUCTORES
    // --------------------------------------------------------------------
    public Usuario() {
        // Constructor vacio: Hibernate lo necesita OBLIGATORIAMENTE para
        // poder crear objetos automaticamente al leer la base de datos.
    }

    // --------------------------------------------------------------------
    // GETTERS Y SETTERS
    // Son metodos para leer (get) y modificar (set) cada atributo desde
    // fuera de la clase. Es una convencion estandar de Java.
    // --------------------------------------------------------------------
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getQr() { return qr; }
    public void setQr(String qr) { this.qr = qr; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
