package com.barmusic.app.service;

import com.barmusic.app.model.Notificacion;
import com.barmusic.app.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ============================================================================
 *  SERVICIO: NotificacionService
 * ============================================================================
 *  Logica para que el administrador cree avisos (texto y/o imagen) y para
 *  que los clientes los consulten.
 *
 *  ¿DONDE SE GUARDAN LAS IMAGENES SUBIDAS?
 *  En la carpeta configurada en "application.properties" bajo la propiedad
 *  "app.uploads.dir" (por defecto: ./uploads/notificaciones). Esa carpeta
 *  se sirve publicamente a traves de la ruta /uploads/** (ver
 *  UploadWebConfig si decides agregarla, o el metodo obtenerRutaPublica).
 * ============================================================================
 */
@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    // Carpeta fisica donde se guardan las imagenes subidas por el admin.
    // Puedes cambiar esta ruta en application.properties.
    @org.springframework.beans.factory.annotation.Value("${app.uploads.dir:./uploads/notificaciones}")
    private String carpetaUploads;

    public List<Notificacion> listarTodas() {
        return notificacionRepository.findAllByOrderByFechaCreacionDesc();
    }

    /**
     * PAGINA 10/11 (Admin): borra una notificacion.
     * Ademas de borrar el registro de la base de datos, si la notificacion
     * tenia una imagen asociada, tambien la borramos del disco - si no,
     * quedarian archivos "huerfanos" ocupando espacio para siempre.
     */
    public void eliminar(Integer id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("La notificacion no existe"));

        if (notificacion.getRutaImagen() != null && !notificacion.getRutaImagen().isBlank()) {
            borrarImagenDelDisco(notificacion.getRutaImagen());
        }

        notificacionRepository.delete(notificacion);
    }

    /**
     * Convierte la ruta PUBLICA guardada en la base de datos (ej:
     * "/uploads/notificaciones/abc123.png") de vuelta a la ruta FISICA en
     * el disco, y borra ese archivo. Si el archivo ya no existe por
     * cualquier motivo, simplemente lo ignoramos (no es un error grave).
     */
    private void borrarImagenDelDisco(String rutaPublica) {
        try {
            String nombreArchivo = rutaPublica.substring(rutaPublica.lastIndexOf("/") + 1);
            Path archivo = Paths.get(carpetaUploads).resolve(nombreArchivo);
            Files.deleteIfExists(archivo);
        } catch (Exception e) {
            System.out.println("[NotificacionService] No se pudo borrar la imagen del disco: " + e.getMessage());
        }
    }

    /**
     * Crea una notificacion nueva. El parametro "imagen" puede ser null si
     * el administrador solo quiso mandar texto (ver mockup pagina 10 y 11:
     * "solo texto o cargando una imagen").
     */
    public Notificacion crear(String texto, MultipartFile imagen) throws IOException {
        Notificacion notificacion = new Notificacion();
        notificacion.setTexto(texto);
        notificacion.setFechaCreacion(LocalDateTime.now());

        if (imagen != null && !imagen.isEmpty()) {
            String rutaGuardada = guardarImagenEnDisco(imagen);
            notificacion.setRutaImagen(rutaGuardada);
        }

        return notificacionRepository.save(notificacion);
    }

    /**
     * Guarda el archivo de imagen fisicamente en el disco del servidor y
     * devuelve la ruta PUBLICA (la que el navegador usara en el <img src="...">).
     */
    private String guardarImagenEnDisco(MultipartFile imagen) throws IOException {
        Path carpeta = Paths.get(carpetaUploads);
        Files.createDirectories(carpeta); // Crea la carpeta si no existe

        // Generamos un nombre unico para evitar que dos imagenes se
        // sobreescriban si tienen el mismo nombre original.
        String extension = obtenerExtension(imagen.getOriginalFilename());
        String nombreArchivo = UUID.randomUUID() + extension;

        Path destino = carpeta.resolve(nombreArchivo);
        try (InputStream in = imagen.getInputStream()) {
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
        }

        // Esta es la URL con la que el frontend podra mostrar la imagen:
        // http://localhost:8080/uploads/notificaciones/<nombreArchivo>
        return "/uploads/notificaciones/" + nombreArchivo;
    }

    private String obtenerExtension(String nombreOriginal) {
        if (nombreOriginal == null || !nombreOriginal.contains(".")) return "";
        return nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
    }
}
