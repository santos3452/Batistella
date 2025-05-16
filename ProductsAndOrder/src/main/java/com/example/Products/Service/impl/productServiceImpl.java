package com.example.Products.Service.impl;

import com.example.Products.Config.ModelMapperConfig;
import com.example.Products.Dtos.ProductosDto.ProductDTO;
import com.example.Products.Dtos.ProductosDto.ProductListDTO;
import com.example.Products.Dtos.ProductosDto.UpdateProductDto;
import com.example.Products.Entity.Products;
import com.example.Products.Entity.enums.CategoriaGranja;
import com.example.Products.Entity.enums.Marca;
import com.example.Products.Entity.enums.type;
import com.example.Products.Service.productService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.Products.Repository.productRepository;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class productServiceImpl implements productService {

    @Autowired
    private productRepository productRepository;

    @Autowired
    private ModelMapperConfig modelMapper;

    @Override
    public ProductDTO saveProduct(ProductDTO producto) {
        // Buscar si ya existe un producto similar
        Products existingProduct = findExistingProduct(producto);
        
        if (existingProduct != null) {
            // Mensaje de error adaptado según el tipo de producto
            String mensajeError;
            if (producto.getAnimalType() == type.GRANJA) {
                mensajeError = "Ya existe un producto con los mismos datos: " + 
                    producto.getNombre() + ", " + 
                    producto.getCategoriaGranja() + ", " + 
                    producto.getKg();
            } else {
                mensajeError = "Ya existe un producto con los mismos datos: " + 
                    producto.getMarca() + ", " + 
                    producto.getTipoAlimento() + ", " + 
                    producto.getKg() + 
                    (producto.getTipoRaza() != null ? ", " + producto.getTipoRaza() : "");
            }
            throw new IllegalArgumentException(mensajeError);
        }
        
        // Si no existe, creamos un nuevo producto
        Products product = modelMapper.modelMapper().map(producto, Products.class);
        product.setActivo(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        Products savedProduct = productRepository.save(product);
        return modelMapper.modelMapper().map(savedProduct, ProductDTO.class);
    }

    private Products findExistingProduct(ProductDTO producto) {
        // Si es un producto de tipo GRANJA, buscar por nombre y categoría
        if (producto.getAnimalType() == type.GRANJA) {
            return productRepository.findAll().stream()
                .filter(p -> 
                    p.getAnimalType() == type.GRANJA &&
                    p.getNombre().equals(producto.getNombre()) &&
                    p.getCategoriaGranja().equals(producto.getCategoriaGranja()) &&
                    p.getKg().equals(producto.getKg())
                )
                .findFirst()
                .orElse(null);
        }
        
        // Para productos de mascotas, buscar por marca, tipo de alimento y tipo de raza
        return productRepository.findAll().stream()
            .filter(p -> 
                p.getAnimalType() != type.GRANJA &&
                // Agregamos una verificación para evitar NullPointerException
                (p.getMarca() != null && producto.getMarca() != null && p.getMarca().equals(producto.getMarca())) &&
                (p.getTipoAlimento() != null && producto.getTipoAlimento() != null && p.getTipoAlimento().equals(producto.getTipoAlimento())) &&
                p.getKg().equals(producto.getKg()) &&
                Objects.equals(p.getTipoRaza(), producto.getTipoRaza())
            )
            .findFirst()
            .orElse(null);
    }

    @Override
    public ProductDTO updateProduct(UpdateProductDto product) {
        Products existingProduct = productRepository.findById(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("No existe el producto con id: " + product.getId()));
        
        // Guardar temporalmente el valor createdAt original
        LocalDateTime createdAtOriginal = existingProduct.getCreatedAt();

        // Si es un producto de tipo GRANJA, establecer la marca como null
        if (product.getAnimalType() == type.GRANJA) {
            product.setMarca(null);
        }
        
        // Hacer el mapeo del DTO al producto existente
        modelMapper.modelMapper().map(product, existingProduct);
        
        // Asegurar que se mantenga el createdAt original
        existingProduct.setCreatedAt(createdAtOriginal);
        existingProduct.setUpdatedAt(LocalDateTime.now());
        existingProduct.setTipoRaza(product.getTipoRaza());

        Products savedProduct = productRepository.save(existingProduct);
        return modelMapper.modelMapper().map(savedProduct, ProductDTO.class);
    }

    @Override
    public void deleteProduct(long id) {
        // TODO: Implementar eliminación
        Products producto = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el producto con id: " + id));

        if(producto.getActivo()) {
            producto.setActivo(false);
        }
        else {
           producto.setActivo(true);
        }

        productRepository.save(producto);
    }

    @Override
    public List<ProductListDTO> getAllProducts() {
        // TODO: Implementar obtención de todos los productos
        List<Products> products = productRepository.findAll();
        List<ProductListDTO> productDTOs = products.stream()
                .map(product -> modelMapper.modelMapper().map(product, ProductListDTO.class))
                .toList();
        return productDTOs;
    }

    @Override
    public List<ProductListDTO> getProductosMascotas() {
        return List.of();
    }

    @Override
    public List<ProductListDTO> getProductosGranja() {
        return List.of();
    }

    @Override
    public List<ProductListDTO> getProductosPorTipoAnimal(type tipoAnimal) {
        return List.of();
    }

    @Override
    public List<ProductListDTO> getProductosPorCategoriaGranja(CategoriaGranja categoriaGranja) {
        return List.of();
    }

    @Override
    public ProductDTO getProductById(long id) {
        // TODO: Implementar obtención por ID
        return null;
    }

    @Override
    public void aumentarPrecio(double porcentaje, String marca) {


        if(porcentaje < -50) {
            throw new IllegalArgumentException("No se puede hacer un descuento de mas del 50%");
        }

        if( marca != null && !marca.isEmpty()) {
            String marca1 = marca.toUpperCase();
            Marca enumMarca = Marca.valueOf(marca1);
            List<Products> productos = productRepository.findByMarca(enumMarca);

            for (Products producto : productos) {
                BigDecimal porcentajeDecimal = BigDecimal.valueOf(porcentaje).divide(BigDecimal.valueOf(100));
                BigDecimal nuevoPrecioMinorista = producto.getPriceMinorista().multiply(BigDecimal.ONE.add(porcentajeDecimal));
                BigDecimal nuevoPrecioMayorista = producto.getPriceMayorista().multiply(BigDecimal.ONE.add(porcentajeDecimal));
                producto.setPriceMinorista(nuevoPrecioMinorista);
                producto.setPriceMayorista(nuevoPrecioMayorista);
                producto.setUpdatedAt(LocalDateTime.now());
                productRepository.save(producto);
            }

        }
        else {
            List<Products> productos = productRepository.findAll();

            for (Products producto : productos) {
                BigDecimal porcentajeDecimal = BigDecimal.valueOf(porcentaje).divide(BigDecimal.valueOf(100));
                BigDecimal nuevoPrecioMinorista = producto.getPriceMinorista().multiply(BigDecimal.ONE.add(porcentajeDecimal));
                BigDecimal nuevoPrecioMayorista = producto.getPriceMayorista().multiply(BigDecimal.ONE.add(porcentajeDecimal));
                producto.setPriceMinorista(nuevoPrecioMinorista);
                producto.setPriceMayorista(nuevoPrecioMayorista);
                producto.setUpdatedAt(LocalDateTime.now());
                productRepository.save(producto);
            }
        }


    }

    @Override
    public List<Marca> getAllMarcas() {
        // Devolvemos directamente todos los valores del enum Marca
        List<Marca> marcas = Arrays.asList(Marca.values());
        
        // Eliminar cualquier posible referencia a SIN_MARCA que pueda causar problemas
        return marcas.stream()
            .filter(Objects::nonNull)
            .toList();
    }
}
