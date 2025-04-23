package com.example.Products.Service.impl;

import com.example.Products.Config.ModelMapperConfig;
import com.example.Products.Dtos.ProductDTO;
import com.example.Products.Models.Products;
import com.example.Products.Service.productService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.Products.Repository.productRepository;

@Service
public class productServiceImpl implements productService {

    @Autowired
    private productRepository productRepository;

    @Autowired
    private ModelMapperConfig modelMapper;

    @Override
    public ProductDTO saveProduct(ProductDTO producto) {
        Products product = new Products();
        modelMapper.modelMapper().map(producto, product);
        product.setActivo(true);
        Products savedProduct = productRepository.save(product);
        return modelMapper.modelMapper().map(savedProduct, ProductDTO.class);
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
    public ProductDTO getAllProducts() {
        // TODO: Implementar obtención de todos los productos
        return null;
    }

    @Override
    public ProductDTO getProductById(long id) {
        // TODO: Implementar obtención por ID
        return null;
    }
}
