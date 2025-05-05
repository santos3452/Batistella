package com.example.UsersAndLogin.Service.Impl;

import com.example.UsersAndLogin.Dto.UpdateUserDto;
import com.example.UsersAndLogin.Dto.UserDto;
import com.example.UsersAndLogin.Entity.UserEntity;
import com.example.UsersAndLogin.Entity.enums.Role;
import com.example.UsersAndLogin.Repository.UserRepository;
import com.example.UsersAndLogin.Security.JwtUtils;
import com.example.UsersAndLogin.Service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Optional<UserEntity> existingUser = userRepository.findByEmail(userDto.getEmail());
        
        if (existingUser.isPresent()) {
            if (!existingUser.get().getActivo()) {
                throw new IllegalArgumentException("Esta cuenta está dada de baja. Por favor, reactive su cuenta para continuar.");
            }
            throw new IllegalArgumentException("Ya existe un usuario con ese email: " + userDto.getEmail());
        }

        // Verificar si el usuario está intentando crear un administrador
        if (userDto.getRol() == Role.ROLE_ADMIN) {
            // Obtener la autenticación actual (será null si no hay usuario autenticado)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            // Verificar si hay un usuario autenticado y si tiene rol admin
            if (auth == null || auth.getAuthorities().stream()
                    .noneMatch(grantedAuthority -> "ROLE_ADMIN".equals(grantedAuthority.getAuthority()))) {
                throw new IllegalArgumentException("Solo un administrador puede crear usuarios con permisos de administrador");
            }
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

    @Override
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        // Validar que la nueva contraseña no esté vacía
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía");
        }

        // Buscar el usuario
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        // Actualizar la contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!user.getActivo()) {
            throw new IllegalArgumentException("El usuario ya está dado de baja");
        }

        user.setActivo(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void reactivateUser(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (user.getActivo()) {
            throw new IllegalArgumentException("La cuenta ya está activa");
        }

        user.setActivo(true);
        userRepository.save(user);
    }

    @Override
    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }
} 