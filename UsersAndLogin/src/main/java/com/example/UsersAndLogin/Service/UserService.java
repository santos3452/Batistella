package com.example.UsersAndLogin.Service;

import com.example.UsersAndLogin.Dto.UpdateAdressDto;
import com.example.UsersAndLogin.Dto.UpdateUserDto;
import com.example.UsersAndLogin.Dto.UserDto;
import com.example.UsersAndLogin.Entity.UserEntity;

import java.util.List;
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

     * @return DTO con los datos actualizados del usuario
     * @throws IllegalArgumentException si no existe un usuario con el email proporcionado
     */
    UpdateUserDto UpdateUser(UpdateUserDto userUpdateDto);


    /**
     * Cambia la contraseña de un usuario existente
     * @param email Email del usuario
     * @param oldPassword Contraseña actual
     * @param newPassword Nueva contraseña
     * @throws IllegalArgumentException si la contraseña actual es incorrecta o si hay otros errores de validación
     */
    void changePassword(String email, String oldPassword, String newPassword);

    void deleteAdress(Long id);

    /**
     * Realiza la baja lógica de un usuario
     * @param email Email del usuario a dar de baja
     * @throws IllegalArgumentException si el usuario no existe o ya está dado de baja
     */
    void deleteUser(String email);

    /**
     * Reactiva una cuenta de usuario previamente dada de baja
     * @param email Email del usuario a reactivar
     * @throws IllegalArgumentException si el usuario no existe o ya está activo
     */
    void reactivateUser(String email);

    /**
     * Guarda o actualiza un usuario en la base de datos
     * @param user El usuario a guardar o actualizar
     * @return El usuario guardado
     */
    UserEntity save(UserEntity user);

    UserDto getUserById(Long id);

} 