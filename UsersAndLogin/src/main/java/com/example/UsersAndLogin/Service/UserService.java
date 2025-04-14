package com.example.UsersAndLogin.Service;

import com.example.UsersAndLogin.Dto.UpdateUserDto;
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
     * @return Optional con el usuario si se encuentra, vacío si no
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Actualiza los datos de un usuario existente
     * @param email Email del usuario a actualizar
     * @param password Nueva contraseña del usuario
     * @param nombre Nuevo nombre del usuario
     * @param apellido Nuevo apellido del usuario
     * @return DTO con los datos actualizados del usuario
     * @throws IllegalArgumentException si no existe un usuario con el email proporcionado
     */
    UpdateUserDto UpdateUser(String email, String password, String nombre, String apellido);

} 