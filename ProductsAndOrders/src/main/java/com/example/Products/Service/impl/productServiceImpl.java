package com.example.Products.Service.impl;

import com.example.Products.Config.ModelMapperConfig;
import com.example.Products.Dtos.ProductDTO;
import com.example.Products.Dtos.ProductListDTO;
import com.example.Products.Entity.Products;
import com.example.Products.Entity.enums.Marca;
import com.example.Products.Service.productService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.Products.Repository.productRepository;

import java.math.BigDecimal;
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
    public ProductDTO updateProduct(ProductListDTO product) {
        Products existingProduct = productRepository.findById(product.getId())
                .orElse(null);

        existingProduct = modelMapper.modelMapper().map(product, Products.class);

        productRepository.save(existingProduct);

        return modelMapper.modelMapper().map(existingProduct, ProductDTO.class);


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
                productRepository.save(producto);
            }
        }


    }
}
