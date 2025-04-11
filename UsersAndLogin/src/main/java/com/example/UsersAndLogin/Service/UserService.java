package com.example.UsersAndLogin.Service;

import com.example.UsersAndLogin.Dto.UserDto;
import com.example.UsersAndLogin.Entity.UserEntity;

import java.util.Optional;

public interface UserService {
    
    /**
     * Crea un nuevo usuario en el sistema
     * @param userDto Datos del usuario a crear
     * @return El usuario creado
     * @throws IllegalArgumentException si ya existe un usuario con el mismo email
     */
    UserEntity createUser(UserDto userDto);
    
    /**
     * Busca un usuario por su email
     * @param email Email del usuario a buscar
     * @return Usuario encontrado o vacío si no existe
     */
    Optional<UserEntity> findByEmail(String email);
} 