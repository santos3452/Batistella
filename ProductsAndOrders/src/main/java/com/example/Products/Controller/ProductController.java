package com.example.Products.Controller;

import com.example.Products.Dtos.Error.ErrorDto;
import com.example.Products.Dtos.ProductDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.Products.Service.productService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import com.example.Products.Service.ImageService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductController {

    @Autowired
    private productService productService;
    
    @Autowired
    private ImageService imageService;

    public ProductController(productService productService, ImageService imageService) {
        this.productService = productService;
        this.imageService = imageService;
    }

    @PostMapping("/saveProduct")
    public ResponseEntity<?> saveProduct(@Valid @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO savedProduct = productService.saveProduct(productDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
        } catch (IllegalArgumentException e) {
            // Manejar específicamente la excepción de producto duplicado
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ErrorDto.of(
                            HttpStatus.CONFLICT.value(),
                            "Producto Duplicado",
                            e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al registrar producto: " + e.getMessage()
                    ));
        }
    }

    @GetMapping("/getAllProducts")
    public ResponseEntity<?> getAllProducts() {
        try {
            return ResponseEntity.ok(productService.getAllProducts());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al obtener productos: " + e.getMessage()
                    ));
        }
    }

    @GetMapping("/upload-form")
    public String uploadForm() {
        return "redirect:/static/example-upload.html";
    }

    @PostMapping(value = "/saveProductWithImage", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> saveProductWithImage(
            @RequestPart(value = "product", required = true) String productJson,
            @RequestPart(value = "image", required = true) MultipartFile image) {
        try {
            // Convertir la cadena JSON a objeto ProductDTO
            ObjectMapper objectMapper = new ObjectMapper();
            // Registrar módulos para manejo de fechas y otras configuraciones
            objectMapper.findAndRegisterModules();
            ProductDTO productDTO = objectMapper.readValue(productJson, ProductDTO.class);
            
            // Guardar la imagen y obtener su URL
            String imageUrl = imageService.saveImage(image, productDTO);
            
            // Imprimir información para depuración
            System.out.println("Imagen guardada en: " + imageUrl);
            System.out.println("Datos del producto: " + productDTO);
            
            // Asignar la URL al producto
            // Ignoramos el error del linter porque sabemos que Lombok genera este método
            productDTO.setImageUrl(imageUrl);
            
            // Guardar el producto
            ProductDTO savedProduct = productService.saveProduct(productDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
        } catch (IllegalArgumentException e) {
            e.printStackTrace(); // Para depuración
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ErrorDto.of(
                            HttpStatus.CONFLICT.value(),
                            "Producto Duplicado",
                            e.getMessage()
                    ));
        } catch (IOException e) {
            e.printStackTrace(); // Para depuración
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorDto.of(
                            HttpStatus.BAD_REQUEST.value(),
                            "Error de imagen",
                            "No se pudo procesar la imagen: " + e.getMessage()
                    ));
        } catch (Exception e) {
            e.printStackTrace(); // Para depuración
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al registrar producto: " + e.getMessage()
                    ));
        }
    }
}
