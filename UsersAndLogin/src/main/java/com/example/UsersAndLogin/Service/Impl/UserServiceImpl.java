package com.example.UsersAndLogin.Service.Impl;

import com.example.UsersAndLogin.Dto.UpdateUserDto;
import com.example.UsersAndLogin.Dto.UserDto;
import com.example.UsersAndLogin.Entity.UserEntity;
import com.example.UsersAndLogin.Repository.UserRepository;
import com.example.UsersAndLogin.Security.JwtUtils;
import com.example.UsersAndLogin.Service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Override
    @Transactional
    public UserEntity createUser(UserDto userDto) {
        // Verificar si ya existe un usuario con ese email
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email: " + userDto.getEmail());
        }

        // Crear nueva entidad y asignar valores del DTO
        UserEntity newUser = new UserEntity();
        newUser.setNombre(userDto.getNombre());
        newUser.setApellido(userDto.getApellido());
        newUser.setEmail(userDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(userDto.getPassword())); // Encriptar la contraseña
        newUser.setRol(userDto.getRol());
        newUser.setTipoUsuario(userDto.getTipoUsuario());

        // Guardar en base de datos
        return userRepository.save(newUser);
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public UpdateUserDto UpdateUser(String email, String password, String nombre, String apellido) {
        try {
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("No existe un usuario con el email: " + email));
            
            // Actualizar los campos del usuario
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }
            user.setNombre(nombre);
            user.setApellido(apellido);
            
            // Guardar los cambios en la base de datos
            userRepository.save(user);
            
            // Devolver el usuario actualizado como DTO
            UpdateUserDto updatedUserDto = new UpdateUserDto();
            updatedUserDto.setNombre(user.getNombre());
            updatedUserDto.setApellido(user.getApellido());
            updatedUserDto.setPassword(user.getPassword());

            return updatedUserDto;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al actualizar el usuario: " + e.getMessage());
        }
    }
} 