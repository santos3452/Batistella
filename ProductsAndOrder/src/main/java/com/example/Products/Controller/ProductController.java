package com.example.Products.Controller;

import com.example.Products.Dtos.Error.ErrorDto;
import com.example.Products.Dtos.ProductosDto.ProductDTO;
import com.example.Products.Dtos.ProductosDto.UpdateProductDto;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.Products.Service.productService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import com.example.Products.Service.impl.ImageService;
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

    @PutMapping("/updatePrices")
    public ResponseEntity<?> UpdatePrices (
            @RequestParam(value = "porcentaje") double porcentaje,
            @Parameter(description = "Marca del producto (opcional). Si no se proporciona, se actualizarán todas las marcas.")
            @Valid @RequestParam(value = "marca", required = false) String marca) {
        try {
            productService.aumentarPrecio(porcentaje, marca);
            return ResponseEntity.ok("Precios actualizados exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorDto.of(
                            HttpStatus.BAD_REQUEST.value(),
                            "Error de Validación",
                            e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al actualizar precios: " + e.getMessage()
                    ));
        }
    }

    @DeleteMapping("/deleteProduct/{id}")
    public ResponseEntity<?> deleteProduct(@RequestParam("id") Long id){
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok("Producto Actualizado exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorDto.of(
                            HttpStatus.BAD_REQUEST.value(),
                            "Error de Validación",
                            e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al eliminar producto: " + e.getMessage()
                    ));
        }
    }
    @PutMapping("/updateProduct")
    public ResponseEntity<?> updateProduct(@Valid @RequestBody UpdateProductDto product){
        try {
                productService.updateProduct(product);
                return ResponseEntity.ok("Producto actualizado exitosamente");

        }
        catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorDto.of(
                            HttpStatus.BAD_REQUEST.value(),
                            "Error de Validación",
                            e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al actualizar producto: " + e.getMessage()
                    ));
        }


    }

    @GetMapping("/upload-form")
    public String uploadForm() {
        return "redirect:/static/example-upload.html";
    }
    @PutMapping(value = "/UpdateProductWithImage", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> updateProductWithImage(
            @RequestPart(value = "product", required = true) String productJson,
            @RequestPart(value = "image", required = true) MultipartFile image) {
        try {
            // Convertir la cadena JSON a objeto ProductDTO
            ObjectMapper objectMapper = new ObjectMapper();
            // Registrar módulos para manejo de fechas y otras configuraciones
            objectMapper.findAndRegisterModules();
            UpdateProductDto product = objectMapper.readValue(productJson, UpdateProductDto.class);

            // Guardar la imagen y obtener su URL
            String imageUrl = imageService.updateImage(image, product);

            // Imprimir información para depuración
            System.out.println("Imagen guardada en: " + imageUrl);
            System.out.println("Datos del producto: " + product);

            // Asignar la URL al producto
            product.setImageUrl(imageUrl);

            // Guardar el producto
            ProductDTO savedProduct = productService.updateProduct(product);
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

    @GetMapping("/getAllMarcas")
    public ResponseEntity<?> getAllMacrcas(){
        try {
            return ResponseEntity.ok(productService.getAllMarcas());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al obtener marcas: " + e.getMessage()
                    ));
        }





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
