package com.barmusic.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ============================================================================
 *  CONFIGURACION PARA SERVIR LAS IMAGENES SUBIDAS (uploads)
 * ============================================================================
 *  Las imagenes que sube el administrador se guardan en una carpeta FISICA
 *  del disco (fuera del "jar" de la aplicacion). Esta clase le dice a
 *  Spring: "cuando alguien pida una URL que empiece con /uploads/, ve a
 *  buscar el archivo en esa carpeta fisica del disco duro".
 *
 *  NOTA TECNICA IMPORTANTE (bug corregido):
 *  En Windows, las rutas de carpetas usan "\" (backslash), pero una URL de
 *  tipo "file:" DEBE usar "/". Si armamos el texto "file:" + ruta a mano
 *  (concatenando Strings), en Windows queda una URL invalida y Spring no
 *  encuentra ningun archivo (siempre responde 404), aunque el archivo SI
 *  exista fisicamente en el disco.
 *  La forma correcta es usar Path.toUri(), que hace esa conversion de
 *  forma automatica y correcta sin importar el sistema operativo.
 * ============================================================================
 */
@Configuration
public class UploadsWebConfig implements WebMvcConfigurer {

    @Value("${app.uploads.dir:./uploads/notificaciones}")
    private String carpetaUploads;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1) Convertimos la ruta configurada en una ruta ABSOLUTA y normalizada
        Path carpeta = Paths.get(carpetaUploads).toAbsolutePath().normalize();

        // 2) Nos aseguramos de que la carpeta exista (si no, la creamos),
        //    para que Spring no falle al registrar el handler.
        try {
            Files.createDirectories(carpeta);
        } catch (Exception e) {
            System.out.println("[UploadsWebConfig] No se pudo crear la carpeta de uploads: " + e.getMessage());
        }

        // 3) "carpeta.toUri()" arma la URL file:/// correctamente en
        //    CUALQUIER sistema operativo (Windows, Mac, Linux), convirtiendo
        //    los backslashes de Windows en forward slashes automaticamente.
        String ubicacionRecurso = carpeta.toUri().toString(); // ej: file:/C:/Users/.../uploads/notificaciones/

        System.out.println("[UploadsWebConfig] Sirviendo imagenes desde: " + ubicacionRecurso);

        // OJO: el patron de la URL debe coincidir EXACTAMENTE con lo que
        // contiene la carpeta configurada. Como "app.uploads.dir" YA apunta
        // a la carpeta final "uploads/notificaciones", el patron tiene que
        // ser "/uploads/notificaciones/**" (no solo "/uploads/**"), o Spring
        // le agrega "notificaciones/" una segunda vez al buscar el archivo
        // (quedaria buscando uploads/notificaciones/notificaciones/archivo.png,
        // que no existe -> error "No static resource").
        registry.addResourceHandler("/uploads/notificaciones/**")
                .addResourceLocations(ubicacionRecurso);
    }
}
