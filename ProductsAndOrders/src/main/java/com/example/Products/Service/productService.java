package com.example.Products.Service;

import com.example.Products.Dtos.ProductDTO;
import com.example.Products.Dtos.ProductListDTO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface productService {
    ProductDTO saveProduct(ProductDTO producto);

    ProductDTO updateProduct(ProductListDTO productListDTO);

    void deleteProduct(long id);

    List<ProductListDTO> getAllProducts();

    ProductDTO getProductById(long id);

    void aumentarPrecio(double porcentaje, String marca);


}
