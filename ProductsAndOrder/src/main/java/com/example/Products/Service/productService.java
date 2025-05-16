package com.example.Products.Service;

import com.example.Products.Dtos.ProductosDto.ProductDTO;
import com.example.Products.Dtos.ProductosDto.ProductListDTO;
import com.example.Products.Dtos.ProductosDto.UpdateProductDto;
import com.example.Products.Entity.enums.CategoriaGranja;
import com.example.Products.Entity.enums.Marca;
import com.example.Products.Entity.enums.type;

import java.util.List;


public interface productService {
    ProductDTO saveProduct(ProductDTO producto);

    ProductDTO updateProduct(UpdateProductDto productListDTO);

    void deleteProduct(long id);

    List<ProductListDTO> getAllProducts();
    
    // Métodos para filtrar productos
    List<ProductListDTO> getProductosMascotas();
    
    List<ProductListDTO> getProductosGranja();
    
    List<ProductListDTO> getProductosPorTipoAnimal(type tipoAnimal);
    
    List<ProductListDTO> getProductosPorCategoriaGranja(CategoriaGranja categoriaGranja);

    ProductDTO getProductById(long id);

    void aumentarPrecio(double porcentaje, String marca);

    List<Marca> getAllMarcas();
}
