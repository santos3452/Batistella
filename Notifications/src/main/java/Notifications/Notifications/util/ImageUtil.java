package Notifications.Notifications.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Utilidad para manejar imágenes y convertirlas a base64 para su uso en correos HTML
 */
@Component
public class ImageUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageUtil.class);
    
    /**
     * Convierte una imagen a base64 para su uso en una etiqueta img HTML
     * 
     * @param imagePath Ruta de la imagen en el classpath
     * @return String con la imagen codificada en base64 lista para usar en HTML
     */
    public String getImageAsBase64DataUrl(String imagePath) {
        try {
            logger.info("Intentando cargar imagen desde: {}", imagePath);
            
            // Cargar la imagen desde el classpath
            ClassPathResource resource = new ClassPathResource(imagePath);
            
            if (!resource.exists()) {
                logger.error("No se pudo encontrar el archivo de imagen en la ruta: {}", imagePath);
                return "";
            }
            
            logger.info("Imagen encontrada, leyendo bytes...");
            InputStream inputStream = resource.getInputStream();
            byte[] imageBytes = inputStream.readAllBytes();
            inputStream.close();
            
            logger.info("Bytes leídos: {} bytes", imageBytes.length);
            
            // Codificar la imagen en base64
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            // Determinar el tipo MIME basado en la extensión del archivo
            String mimeType = getMimeType(imagePath);
            
            logger.info("Imagen codificada exitosamente con tipo MIME: {}", mimeType);
            
            // Retornar la URL de datos completa
            return "data:" + mimeType + ";base64," + base64Image;
        } catch (IOException e) {
            logger.error("Error al cargar o codificar la imagen: {}", e.getMessage(), e);
            return "";
        }
    }
    
    /**
     * Método alternativo que no usa ClassPathResource
     */
    public String getImageAsBase64FromFilesystem(String imagePath) {
        try {
            logger.info("Intentando cargar imagen desde el filesystem: {}", imagePath);
            
            // Construir la ruta completa
            String fullPath = "src/main/resources/" + imagePath;
            logger.info("Ruta completa: {}", fullPath);
            
            java.nio.file.Path path = java.nio.file.Paths.get(fullPath);
            
            if (!java.nio.file.Files.exists(path)) {
                logger.error("No se pudo encontrar el archivo de imagen en la ruta: {}", fullPath);
                return "";
            }
            
            logger.info("Imagen encontrada, leyendo bytes...");
            byte[] imageBytes = java.nio.file.Files.readAllBytes(path);
            
            logger.info("Bytes leídos: {} bytes", imageBytes.length);
            
            // Codificar la imagen en base64
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            // Determinar el tipo MIME basado en la extensión del archivo
            String mimeType = getMimeType(imagePath);
            
            logger.info("Imagen codificada exitosamente con tipo MIME: {}", mimeType);
            
            // Retornar la URL de datos completa
            return "data:" + mimeType + ";base64," + base64Image;
        } catch (IOException e) {
            logger.error("Error al cargar o codificar la imagen desde filesystem: {}", e.getMessage(), e);
            return "";
        }
    }
    
    /**
     * Determina el tipo MIME basado en la extensión del archivo
     * 
     * @param filePath Ruta del archivo
     * @return Tipo MIME correspondiente
     */
    private String getMimeType(String filePath) {
        if (filePath.endsWith(".png")) {
            return "image/png";
        } else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filePath.endsWith(".gif")) {
            return "image/gif";
        } else if (filePath.endsWith(".svg")) {
            return "image/svg+xml";
        } else {
            return "application/octet-stream";
        }
    }
} 