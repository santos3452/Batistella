package com.example.Products.Service.impl;

import com.example.Products.Config.ModelMapperConfig;
import com.example.Products.Dtos.ProductDTO;
import com.example.Products.Entity.Products;
import com.example.Products.Service.productService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.Products.Repository.productRepository;
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
            throw new IllegalArgumentException("Ya existe un producto con los mismos datos: " + 
                producto.getMarca() + ", " + 
                producto.getTipoAlimento() + ", " + 
                producto.getKg() + 
                (producto.getTipoRaza() != null ? ", " + producto.getTipoRaza() : ""));
        }
        
        // Si no existe, creamos un nuevo producto
        Products product = modelMapper.modelMapper().map(producto, Products.class);
        product.setActivo(true);
        Products savedProduct = productRepository.save(product);
        return modelMapper.modelMapper().map(savedProduct, ProductDTO.class);
    }

    private Products findExistingProduct(ProductDTO producto) {
        // Buscar un producto con los mismos datos específicos
        return productRepository.findAll().stream()
            .filter(p -> 
                p.getMarca().equals(producto.getMarca()) &&
                p.getTipoAlimento().equals(producto.getTipoAlimento()) &&
                p.getKg().equals(producto.getKg()) &&
                Objects.equals(p.getTipoRaza(), producto.getTipoRaza()) &&
                p.getAnimalType().equals(producto.getAnimalType())
            )
            .findFirst()
            .orElse(null);
    }

    @Override
    public void updateProduct(long id, String name, String description, String type, int kg, double price, int stock) {
        // TODO: Implementar actualización
    }

    @Override
    public void deleteProduct(long id) {
        // TODO: Implementar eliminación
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        // TODO: Implementar obtención de todos los productos
        List<Products> products = productRepository.findAll();
        List<ProductDTO> productDTOs = products.stream()
                .map(product -> modelMapper.modelMapper().map(product, ProductDTO.class))
                .toList();
        return productDTOs;
    }

    @Override
    public ProductDTO getProductById(long id) {
        // TODO: Implementar obtención por ID
        return null;
    }
}
