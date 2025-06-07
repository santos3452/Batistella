package com.example.Products.Service.impl;

import com.example.Products.Dtos.ProductosDto.ProductDTO;
import com.example.Products.Dtos.ProductosDto.UpdateProductDto;
import com.example.Products.Entity.enums.type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.Normalizer;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${app.upload.dir:disabled}")
    private String uploadDir;
    
    @Value("${app.base.url:http://localhost:8083}")
    private String baseUrl;
    
    private final GoogleCloudStorageService cloudStorageService;
    
    @Autowired
    public ImageService(GoogleCloudStorageService cloudStorageService) {
        this.cloudStorageService = cloudStorageService;
    }

    public String saveImage(MultipartFile image, ProductDTO productDTO) throws IOException {
        // Verificar que la imagen no sea nula y tenga contenido
        if (image == null || image.isEmpty()) {
            System.err.println("Error: La imagen está vacía o es nula");
            throw new IOException("La imagen está vacía o es nula");
        }
        
        System.out.println("Iniciando guardado de imagen para producto: " + productDTO);
        System.out.println("Tamaño de la imagen: " + image.getSize() + " bytes");
        System.out.println("Nombre original de la imagen: " + image.getOriginalFilename());
        
        // Determinar la carpeta según el tipo de producto
        String carpeta;
        if (productDTO.getAnimalType() == type.GRANJA) {
            carpeta = "GRANJA";
        } else {
            // Determinar la marca para la carpeta
            try {
                carpeta = productDTO.getMarca() != null ? productDTO.getMarca().toString() : "OTROS";
            } catch (Exception e) {
                System.err.println("Error al obtener la marca: " + e.getMessage());
                carpeta = "OTROS";
            }
        }
        
        System.out.println("Carpeta determinada: " + carpeta);
        
        // Generar nombre de archivo
        String nombreProducto = generarNombreArchivo(productDTO);
        String fileExtension = getFileExtension(image.getOriginalFilename());
        String newFilename = nombreProducto + "." + fileExtension;
        
        // Usar Google Cloud Storage para almacenar la imagen
        try {
            String imageUrl = cloudStorageService.uploadImage(image, carpeta, newFilename);
            System.out.println("Imagen subida a Google Cloud Storage: " + imageUrl);
            return imageUrl;
        } catch (Exception e) {
            System.err.println("Error al guardar la imagen en Google Cloud Storage: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error al guardar la imagen en Google Cloud Storage: " + e.getMessage());
        }
    }
    
    // Versión sin ProductDTO para compatibilidad con código existente
    public String saveImage(MultipartFile image) throws IOException {
        // Crear un nombre de archivo único
        String originalFilename = image.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "." + fileExtension;
        
        // Usar Google Cloud Storage para almacenar la imagen
        try {
            String imageUrl = cloudStorageService.uploadImage(image, "OTROS", newFilename);
            System.out.println("Imagen subida a Google Cloud Storage: " + imageUrl);
            return imageUrl;
        } catch (Exception e) {
            System.err.println("Error al guardar la imagen en Google Cloud Storage: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error al guardar la imagen en Google Cloud Storage: " + e.getMessage());
        }
    }
    
    // Método para actualizar imágenes con ProductListDTO
    public String updateImage(MultipartFile image, UpdateProductDto productListDTO) throws IOException {
        // Verificar que la imagen no sea nula y tenga contenido
        if (image == null || image.isEmpty()) {
            System.err.println("Error: La imagen está vacía o es nula");
            throw new IOException("La imagen está vacía o es nula");
        }
        
        System.out.println("Iniciando actualización de imagen para producto: " + productListDTO);
        System.out.println("Tamaño de la imagen: " + image.getSize() + " bytes");
        System.out.println("Nombre original de la imagen: " + image.getOriginalFilename());
        
        // Determinar la carpeta según el tipo de producto
        String carpeta;
        if (productListDTO.getAnimalType() == type.GRANJA) {
            carpeta = "GRANJA";
        } else {
            // Determinar la marca para la carpeta
            try {
                carpeta = productListDTO.getMarca() != null ? productListDTO.getMarca().toString() : "OTROS";
            } catch (Exception e) {
                System.err.println("Error al obtener la marca: " + e.getMessage());
                carpeta = "OTROS";
            }
        }
        
        System.out.println("Carpeta determinada: " + carpeta);
        
        // Generar nombre de archivo
        String nombreProducto = generarNombreArchivo(productListDTO);
        String fileExtension = getFileExtension(image.getOriginalFilename());
        String newFilename = nombreProducto + "." + fileExtension;
        
        // Si el producto ya tiene una URL de imagen, intentar eliminarla
        if (productListDTO.getImageUrl() != null && !productListDTO.getImageUrl().isEmpty()) {
            try {
                cloudStorageService.deleteImage(productListDTO.getImageUrl());
                System.out.println("Imagen anterior eliminada: " + productListDTO.getImageUrl());
            } catch (Exception e) {
                System.err.println("Error al eliminar la imagen anterior: " + e.getMessage());
                // Continuar con la actualización aunque no se haya podido eliminar la imagen anterior
            }
        }
        
        // Usar Google Cloud Storage para almacenar la imagen
        try {
            String imageUrl = cloudStorageService.uploadImage(image, carpeta, newFilename);
            System.out.println("Imagen subida a Google Cloud Storage: " + imageUrl);
            return imageUrl;
        } catch (Exception e) {
            System.err.println("Error al actualizar la imagen en Google Cloud Storage: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error al actualizar la imagen en Google Cloud Storage: " + e.getMessage());
        }
    }
    
    private String generarNombreArchivo(ProductDTO productDTO) {
        // Construir un nombre descriptivo para el archivo
        StringBuilder nombre = new StringBuilder();
        
        // Si es un producto de granja, usar nombre y categoría
        if (productDTO.getAnimalType() == type.GRANJA) {
            if (productDTO.getNombre() != null) {
                nombre.append(productDTO.getNombre());
            }
            if (productDTO.getCategoriaGranja() != null) {
                nombre.append(productDTO.getCategoriaGranja().toString());
            }
        } else {
            // Para productos de mascotas, usar marca, tipo de alimento y tipo de raza
            if (productDTO.getMarca() != null) {
                String marcaStr = productDTO.getMarca().toString();
                nombre.append(marcaStr.contains("Top") ? "Top" : marcaStr);
            } else {
                // Si no hay marca, usamos el nombre o un valor genérico
                if (productDTO.getNombre() != null && !productDTO.getNombre().isEmpty()) {
                    nombre.append(productDTO.getNombre());
                } else {
                    nombre.append("SinMarca");
                }
            }
            
            // Añadir tipo de alimento (por ejemplo, Adulto, Cachorro, etc.)
            if (productDTO.getTipoAlimento() != null) {
                nombre.append(productDTO.getTipoAlimento().toString());
            }
            
            // Añadir tipo de raza si existe
            if (productDTO.getTipoRaza() != null) {
                nombre.append(productDTO.getTipoRaza().toString().replace("RAZA_", "Raza"));
            }
        }
        
        // Limpiar el nombre (quitar espacios, caracteres especiales, etc.)
        String nombreLimpio = limpiarNombre(nombre.toString());
        
        return nombreLimpio;
    }
    
    // Método sobrecargado para ProductListDTO
    private String generarNombreArchivo(UpdateProductDto productListDTO) {
        // Construir un nombre descriptivo para el archivo
        StringBuilder nombre = new StringBuilder();
        
        // Si es un producto de granja, usar nombre y categoría
        if (productListDTO.getAnimalType() == type.GRANJA) {
            if (productListDTO.getNombre() != null) {
                nombre.append(productListDTO.getNombre());
            }
            if (productListDTO.getCategoriaGranja() != null) {
                nombre.append(productListDTO.getCategoriaGranja().toString());
            }
        } else {
            // Para productos de mascotas, usar marca, tipo de alimento y tipo de raza
            if (productListDTO.getMarca() != null) {
                String marcaStr = productListDTO.getMarca().toString();
                nombre.append(marcaStr.contains("Top") ? "Top" : marcaStr);
            } else {
                // Si no hay marca, usamos el nombre o un valor genérico
                if (productListDTO.getNombre() != null && !productListDTO.getNombre().isEmpty()) {
                    nombre.append(productListDTO.getNombre());
                } else {
                    nombre.append("SinMarca");
                }
            }
            
            // Añadir tipo de alimento (por ejemplo, Adulto, Cachorro, etc.)
            if (productListDTO.getTipoAlimento() != null) {
                nombre.append(productListDTO.getTipoAlimento().toString());
            }
            
            // Añadir tipo de raza si existe
            if (productListDTO.getTipoRaza() != null) {
                nombre.append(productListDTO.getTipoRaza().toString().replace("RAZA_", "Raza"));
            }
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