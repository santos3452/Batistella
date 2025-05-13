package com.example.Products.Repository;

import com.example.Products.Entity.Products;
import com.example.Products.Entity.enums.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface productRepository extends JpaRepository<Products, Long> {

    List<Products> findByMarca(Marca marca);
}
