package com.example.Products.Service;

import com.example.Products.Dtos.ProductDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${app.upload.dir:src/main/resources/static/images}")
    private String uploadDir;
    
    @Value("${app.base.url:http://localhost:8083}")
    private String baseUrl;

    public String saveImage(MultipartFile image, ProductDTO productDTO) throws IOException {
        // Verificar que la imagen no sea nula y tenga contenido
        if (image == null || image.isEmpty()) {
            System.err.println("Error: La imagen está vacía o es nula");
            throw new IOException("La imagen está vacía o es nula");
        }
        
        System.out.println("Iniciando guardado de imagen para producto: " + productDTO);
        System.out.println("Tamaño de la imagen: " + image.getSize() + " bytes");
        System.out.println("Nombre original de la imagen: " + image.getOriginalFilename());
        
        // Determinar la marca para la carpeta
        String marca = "OTROS";
        try {
            marca = productDTO.getMarca() != null ? productDTO.getMarca().toString() : "OTROS";
        } catch (Exception e) {
            System.err.println("Error al obtener la marca: " + e.getMessage());
        }
        
        System.out.println("Marca determinada: " + marca);
        
        // Asegurarnos de que existe el directorio de marcas
        String marcaDir = uploadDir + File.separator + marca;
        File marcaDirFile = new File(marcaDir);
        if (!marcaDirFile.exists()) {
            boolean dirCreated = marcaDirFile.mkdirs();
            System.out.println("Directorio creado: " + dirCreated + " - Ruta: " + marcaDirFile.getAbsolutePath());
        }
        
        // Generar nombre de archivo
        String nombreProducto = generarNombreArchivo(productDTO);
        String fileExtension = getFileExtension(image.getOriginalFilename());
        String newFilename = nombreProducto + "." + fileExtension;
        
        // Ruta completa del archivo
        String filePath = marcaDir + File.separator + newFilename;
        File fileToSave = new File(filePath);
        
        System.out.println("Guardando imagen en: " + fileToSave.getAbsolutePath());
        
        try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
            fos.write(image.getBytes());
            System.out.println("Imagen guardada exitosamente. Tamaño: " + fileToSave.length() + " bytes");
        } catch (Exception e) {
            System.err.println("Error al guardar la imagen: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error al guardar la imagen: " + e.getMessage());
        }
        
        // Devolver la URL relativa para acceder a la imagen, incluyendo la URL base
        String relativePath = "/images/" + marca + "/" + newFilename;
        String imageUrl = baseUrl + relativePath;
        System.out.println("URL de la imagen generada: " + imageUrl);
        return imageUrl;
    }
    
    // Versión sin ProductDTO para compatibilidad con código existente
    public String saveImage(MultipartFile image) throws IOException {
        // Crear un nombre de archivo único
        String originalFilename = image.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "." + fileExtension;
        
        // Ruta completa del archivo
        String filePath = uploadDir + File.separator + newFilename;
        File fileToSave = new File(filePath);
        
        // Crear directorio si no existe
        File directory = fileToSave.getParentFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        System.out.println("Guardando imagen genérica en: " + fileToSave.getAbsolutePath());
        
        try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
            fos.write(image.getBytes());
        }
        
        // Devolver la URL completa
        return baseUrl + "/images/" + newFilename;
    }
    
    private String generarNombreArchivo(ProductDTO productDTO) {
        // Construir un nombre descriptivo para el archivo
        StringBuilder nombre = new StringBuilder();
        
        // Añadir marca
        if (productDTO.getMarca() != null) {
            String marcaStr = productDTO.getMarca().toString();
            nombre.append(marcaStr.contains("Top") ? "Top" : marcaStr);
        }
        
        // Añadir tipo de alimento (por ejemplo, Adulto, Cachorro, etc.)
        if (productDTO.getTipoAlimento() != null) {
            nombre.append(productDTO.getTipoAlimento().toString());
        }
        
        // Añadir tipo de raza si existe
        if (productDTO.getTipoRaza() != null) {
            nombre.append(productDTO.getTipoRaza().toString().replace("RAZA_", "Raza"));
        }
        
        // Limpiar el nombre (quitar espacios, caracteres especiales, etc.)
        String nombreLimpio = limpiarNombre(nombre.toString());
        
        return nombreLimpio;
    }
    
    private String limpiarNombre(String nombre) {
        // Quitar espacios, guiones y caracteres especiales
        String normalizado = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "") // Eliminar acentos y caracteres no ASCII
                .replaceAll("\\s+", "") // Eliminar espacios
                .replaceAll("_", ""); // Eliminar guiones bajos
        
        return normalizado;
    }
    
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "webp"; // Default to webp format
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex < 0) {
            return "webp"; // Default to webp format
        }
        return filename.substring(lastDotIndex + 1);
    }
} 