package com.example.Products.Controller;

import com.example.Products.Dtos.Error.ErrorDto;
import com.example.Products.Dtos.ProductDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.Products.Service.productService;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductController {

    @Autowired
    private productService productService;

    public ProductController(productService productService) {
        this.productService = productService;
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

  
}
