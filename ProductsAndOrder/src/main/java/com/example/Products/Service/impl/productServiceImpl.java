package com.example.Products.Service.impl;

import com.example.Products.Config.ModelMapperConfig;
import com.example.Products.Dtos.ProductosDto.ProductDTO;
import com.example.Products.Dtos.ProductosDto.ProductListDTO;
import com.example.Products.Dtos.ProductosDto.UpdateProductDto;
import com.example.Products.Entity.Products;
import com.example.Products.Entity.enums.CategoriaGranja;
import com.example.Products.Entity.enums.Marca;
import com.example.Products.Entity.enums.type;
import com.example.Products.Repository.productRepository;
import com.example.Products.Service.productService;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class productServiceImpl implements productService {

    @Autowired
    private productRepository productRepository;

    @Autowired
    private ModelMapperConfig modelMapper;

    @Override
    public ProductDTO saveProduct(ProductDTO producto) {
        // Validamos los campos según el tipo de animal
        validateProductFields(producto);
        
        // Buscar si ya existe un producto similar
        Products existingProduct = findExistingProduct(producto);
        
        if (existingProduct != null) {
            throw new IllegalArgumentException("Ya existe un producto con los mismos datos.");
        }
        
        // Si no existe, creamos un nuevo producto
        Products product = modelMapper.modelMapper().map(producto, Products.class);
        product.setActivo(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        Products savedProduct = productRepository.save(product);
        return modelMapper.modelMapper().map(savedProduct, ProductDTO.class);
    }
    
    private void validateProductFields(ProductDTO producto) {
        if (producto.getAnimalType() == type.GRANJA) {
            // Validaciones para productos de granja
            if (producto.getNombre() == null || producto.getNombre().isEmpty()) {
                throw new IllegalArgumentException("El nombre es obligatorio para productos de granja");
            }
            if (producto.getCategoriaGranja() == null) {
                throw new IllegalArgumentException("La categoría de granja es obligatoria para productos de granja");
            }
        } else {
            // Validaciones para productos de mascota (perros/gatos)
            if (producto.getMarca() == null) {
                throw new IllegalArgumentException("La marca es obligatoria para productos de mascota");
            }
            if (producto.getTipoAlimento() == null) {
                throw new IllegalArgumentException("El tipo de alimento es obligatorio para productos de mascota");
            }
        }
        
        // Validaciones comunes para todos los productos
        if (producto.getKg() == null) {
            throw new IllegalArgumentException("El peso (kg) es obligatorio");
        }
        if (producto.getPriceMinorista() == null) {
            throw new IllegalArgumentException("El precio minorista es obligatorio");
        }
        if (producto.getPriceMayorista() == null) {
            throw new IllegalArgumentException("El precio mayorista es obligatorio");
        }
        if (producto.getStock() == null) {
            throw new IllegalArgumentException("El stock es obligatorio");
        }
    }

    private Products findExistingProduct(ProductDTO producto) {
        // Para productos de granja buscamos por nombre y categoría
        if (producto.getAnimalType() == type.GRANJA) {
            return productRepository.findAll().stream()
                .filter(p -> 
                    p.getAnimalType() == type.GRANJA &&
                    Objects.equals(p.getNombre(), producto.getNombre()) &&
                    Objects.equals(p.getCategoriaGranja(), producto.getCategoriaGranja()) &&
                    p.getKg().equals(producto.getKg())
                )
                .findFirst()
                .orElse(null);
        } else {
            // Para productos de mascota buscamos por marca, tipo alimento, etc.
            return productRepository.findAll().stream()
                .filter(p -> 
                    p.getAnimalType() != type.GRANJA &&
                    Objects.equals(p.getMarca(), producto.getMarca()) &&
                    Objects.equals(p.getTipoAlimento(), producto.getTipoAlimento()) &&
                    p.getKg().equals(producto.getKg()) &&
                    Objects.equals(p.getTipoRaza(), producto.getTipoRaza()) &&
                    p.getAnimalType().equals(producto.getAnimalType())
                )
                .findFirst()
                .orElse(null);
        }
    }

    @Override
    public ProductDTO updateProduct(UpdateProductDto product) {
        Products existingProduct = productRepository.findById(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("No existe el producto con id: " + product.getId()));
        
        // Guardar temporalmente el valor createdAt original
        LocalDateTime createdAtOriginal = existingProduct.getCreatedAt();
        
        // Hacer el mapeo del DTO al producto existente
        modelMapper.modelMapper().map(product, existingProduct);
        
        // Asegurar que se mantenga el createdAt original
        existingProduct.setCreatedAt(createdAtOriginal);
        existingProduct.setUpdatedAt(LocalDateTime.now());

        Products savedProduct = productRepository.save(existingProduct);
        return modelMapper.modelMapper().map(savedProduct, ProductDTO.class);
    }

    @Override
    public void deleteProduct(long id) {
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
        List<Products> products = productRepository.findAll();
        return products.stream()
                .map(product -> modelMapper.modelMapper().map(product, ProductListDTO.class))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductListDTO> getProductosMascotas() {
        List<Products> products = productRepository.findAll();
        return products.stream()
                .filter(p -> p.getAnimalType() == type.PERROS || p.getAnimalType() == type.GATOS)
                .map(product -> modelMapper.modelMapper().map(product, ProductListDTO.class))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductListDTO> getProductosGranja() {
        List<Products> products = productRepository.findAll();
        return products.stream()
                .filter(p -> p.getAnimalType() == type.GRANJA)
                .map(product -> modelMapper.modelMapper().map(product, ProductListDTO.class))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductListDTO> getProductosPorTipoAnimal(type tipoAnimal) {
        List<Products> products = productRepository.findAll();
        return products.stream()
                .filter(p -> p.getAnimalType() == tipoAnimal)
                .map(product -> modelMapper.modelMapper().map(product, ProductListDTO.class))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductListDTO> getProductosPorCategoriaGranja(CategoriaGranja categoriaGranja) {
        List<Products> products = productRepository.findAll();
        return products.stream()
                .filter(p -> p.getAnimalType() == type.GRANJA && p.getCategoriaGranja() == categoriaGranja)
                .map(product -> modelMapper.modelMapper().map(product, ProductListDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO getProductById(long id) {
        Products producto = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el producto con id: " + id));
        return modelMapper.modelMapper().map(producto, ProductDTO.class);
    }

    @Override
    public void aumentarPrecio(double porcentaje, String marca) {
        if(porcentaje < -50) {
            throw new IllegalArgumentException("No se puede hacer un descuento de mas del 50%");
        }

        if(marca != null && !marca.isEmpty()) {
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
        } else {
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
        return Arrays.asList(Marca.values());
    }
}
