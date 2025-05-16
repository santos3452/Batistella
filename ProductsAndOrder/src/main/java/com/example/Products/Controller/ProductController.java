package com.example.Products.Controller;

import com.example.Products.Dtos.Error.ErrorDto;
import com.example.Products.Dtos.ProductosDto.ProductDTO;
import com.example.Products.Dtos.ProductosDto.ProductListDTO;
import com.example.Products.Dtos.ProductosDto.UpdateProductDto;
import com.example.Products.Entity.enums.CategoriaGranja;
import com.example.Products.Entity.enums.Marca;
import com.example.Products.Entity.enums.type;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;

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
            // Validación específica para productos de granja
            if (productDTO.getAnimalType() == type.GRANJA) {
                // Asegurarnos de que la marca sea null para productos de granja
                productDTO.setMarca(null);
                
                if (productDTO.getNombre() == null || productDTO.getNombre().trim().isEmpty()) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ErrorDto.of(
                                    HttpStatus.BAD_REQUEST.value(),
                                    "Datos inválidos",
                                    "El nombre es obligatorio para productos de granja"
                            ));
                }
                if (productDTO.getCategoriaGranja() == null) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ErrorDto.of(
                                    HttpStatus.BAD_REQUEST.value(),
                                    "Datos inválidos",
                                    "La categoría de granja es obligatoria para productos de granja"
                            ));
                }
            } else {
                // Validación para productos de mascotas
                if (productDTO.getMarca() == null) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ErrorDto.of(
                                    HttpStatus.BAD_REQUEST.value(),
                                    "Datos inválidos",
                                    "La marca es obligatoria para productos de mascotas"
                            ));
                }
                if (productDTO.getTipoAlimento() == null) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ErrorDto.of(
                                    HttpStatus.BAD_REQUEST.value(),
                                    "Datos inválidos",
                                    "El tipo de alimento es obligatorio para productos de mascotas"
                            ));
                }
            }
            
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
                // Si es un producto de tipo GRANJA, establecer la marca como null
                if (product.getAnimalType() == type.GRANJA) {
                    product.setMarca(null);
                }
                
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
            
            // Procesar el JSON para manejar marca null en productos GRANJA
            String processedJson = productJson.replace("\"marca\":\"SIN_MARCA\"", "\"marca\":null");
            UpdateProductDto product = objectMapper.readValue(processedJson, UpdateProductDto.class);
            
            // Si es un producto de tipo GRANJA, establecer la marca como null
            if (product.getAnimalType() == type.GRANJA) {
                product.setMarca(null);
            }

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
            
            // Procesamiento previo del JSON para manejar marca null en productos GRANJA
            String processedJson = productJson.replace("\"marca\":\"SIN_MARCA\"", "\"marca\":null");
            ProductDTO productDTO = objectMapper.readValue(processedJson, ProductDTO.class);
            
            // Validación específica para productos de granja
            if (productDTO.getAnimalType() == type.GRANJA) {
                // Asegurarnos de que la marca sea null para productos de granja
                productDTO.setMarca(null);
                
                if (productDTO.getNombre() == null || productDTO.getNombre().trim().isEmpty()) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ErrorDto.of(
                                    HttpStatus.BAD_REQUEST.value(),
                                    "Datos inválidos",
                                    "El nombre es obligatorio para productos de granja"
                            ));
                }
                if (productDTO.getCategoriaGranja() == null) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ErrorDto.of(
                                    HttpStatus.BAD_REQUEST.value(),
                                    "Datos inválidos",
                                    "La categoría de granja es obligatoria para productos de granja"
                            ));
                }
            } else {
                // Validación para productos de mascotas
                if (productDTO.getMarca() == null) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ErrorDto.of(
                                    HttpStatus.BAD_REQUEST.value(),
                                    "Datos inválidos",
                                    "La marca es obligatoria para productos de mascotas"
                            ));
                }
                if (productDTO.getTipoAlimento() == null) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ErrorDto.of(
                                    HttpStatus.BAD_REQUEST.value(),
                                    "Datos inválidos",
                                    "El tipo de alimento es obligatorio para productos de mascotas"
                            ));
                }
            }
            
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

    @GetMapping("/por-tipo")
    @Operation(
            summary = "Filtrar productos por tipo",
            description = "Permite filtrar productos por tipo de animal (mascotas/granja) y por categoría específica de granja",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    public ResponseEntity<?> obtenerProductosPorTipo(
            @Parameter(description = "Tipo de animal (PERROS, GATOS, GRANJA)")
            @RequestParam(required = false) type tipoAnimal,
            
            @Parameter(description = "Categoría específica de granja (solo aplica cuando tipoAnimal=GRANJA)")
            @RequestParam(required = false) CategoriaGranja categoriaGranja
    ) {
        try {
            List<ProductListDTO> productos;
            
            if (tipoAnimal != null) {
                if (tipoAnimal == type.GRANJA && categoriaGranja != null) {
                    // Filtrar por categoría específica de granja
                    productos = productService.getProductosPorCategoriaGranja(categoriaGranja);
                } else {
                    // Filtrar por tipo de animal
                    productos = productService.getProductosPorTipoAnimal(tipoAnimal);
                }
            } else {
                // Si no se especifica tipo, devolver todos
                productos = productService.getAllProducts();
            }
            
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}
