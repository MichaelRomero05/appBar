package com.barmusic.app.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * ============================================================================
 *  UTILIDAD: QrCodeGenerator
 * ============================================================================
 *  Convierte un simple texto (por ejemplo "QR-CLIENTE-0001", el valor
 *  guardado en usuarios_u.u_qr) en una IMAGEN de codigo QR en formato PNG,
 *  usando la libreria gratuita ZXing (la misma que usa Google internamente).
 *
 *  ¿COMO SE USA ESTO EN LA APP?
 *  1. Cuando un cliente se registra, se genera un texto unico y se guarda
 *     en u_qr.
 *  2. El administrador (o un proceso de "impresion de mesas") llama al
 *     endpoint GET /api/usuarios/{id}/qr, que usa esta clase para devolver
 *     la imagen PNG del QR, lista para imprimir y pegar en la mesa.
 *  3. El cliente escanea ese QR fisico con la camara de su celular (pagina
 *     3 del mockup) y la app lo usa para iniciar sesion automaticamente.
 * ============================================================================
 */
public class QrCodeGenerator {

    /**
     * Genera un arreglo de bytes PNG que representa el codigo QR del texto dado.
     * @param texto  El contenido que ira codificado dentro del QR.
     * @param ancho  Ancho de la imagen en pixeles (ej: 300).
     * @param alto   Alto de la imagen en pixeles (ej: 300).
     */
    public static byte[] generarPng(String texto, int ancho, int alto) throws WriterException, IOException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(texto, BarcodeFormat.QR_CODE, ancho, alto);

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", salida);
        return salida.toByteArray();
    }
}
