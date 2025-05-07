package com.example.UsersAndLogin.Repository;

import com.example.UsersAndLogin.Entity.DomicilioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DomicilioRepository extends JpaRepository<DomicilioEntity, Long> {
    
    // Método para encontrar el domicilio con el ID más alto
    Optional<DomicilioEntity> findTopByOrderByIdDesc();
    
    // Consulta nativa para obtener el máximo ID
    @Query(value = "SELECT COALESCE(MAX(id), 0) FROM domicilio", nativeQuery = true)
    Long findMaxId();
    
    // Buscar domicilios por ID de usuario
    List<DomicilioEntity> findByUsuarioId(Long usuarioId);
} 