package com.example.Products.Service;

import com.example.Products.Dtos.ProductosDto.ProductDTO;
import com.example.Products.Dtos.ProductosDto.ProductListDTO;
import com.example.Products.Dtos.ProductosDto.UpdateProductDto;
import com.example.Products.Entity.enums.Marca;

import java.util.List;


public interface productService {
    ProductDTO saveProduct(ProductDTO producto);

    ProductDTO updateProduct(UpdateProductDto productListDTO);

    void deleteProduct(long id);

    List<ProductListDTO> getAllProducts();

    ProductDTO getProductById(long id);

    void aumentarPrecio(double porcentaje, String marca);

    List<Marca> getAllMarcas();


}
