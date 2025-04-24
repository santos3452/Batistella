package com.example.Products.Service;

import com.example.Products.Dtos.ProductDTO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface productService {
    ProductDTO saveProduct(ProductDTO producto);

    void updateProduct(long id, String name, String description, String type,int kg, double price, int stock);

    void deleteProduct(long id);

    List<ProductDTO> getAllProducts();

    ProductDTO getProductById(long id);
}
