package com.example.UsersAndLogin.Service.Impl;

import com.example.UsersAndLogin.Config.ModelMapperConfig;
import com.example.UsersAndLogin.Dto.UpdateAdressDto;
import com.example.UsersAndLogin.Dto.UpdateUserDto;
import com.example.UsersAndLogin.Dto.UserDto;
import com.example.UsersAndLogin.Entity.DomicilioEntity;
import com.example.UsersAndLogin.Entity.UserEntity;
import com.example.UsersAndLogin.Entity.enums.Role;
import com.example.UsersAndLogin.Repository.DomicilioRepository;
import com.example.UsersAndLogin.Repository.UserRepository;
import com.example.UsersAndLogin.Security.JwtUtils;
import com.example.UsersAndLogin.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final DomicilioRepository domicilioRepository;
    
    @Autowired
    private ModelMapperConfig modelMapperConfig;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, ModelMapperConfig modelmapper, DomicilioRepository domicilioRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.modelMapperConfig = modelmapper;
        this.domicilioRepository = domicilioRepository;
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

    public UpdateUserDto UpdateUser(UpdateUserDto userUpdateDto) {
        try {
            UserEntity user = userRepository.findByEmail(userUpdateDto.getMail())
                    .orElseThrow(() -> new IllegalArgumentException("No existe un usuario con el email: " + userUpdateDto.getMail()));


            user.setNombre(userUpdateDto.getNombre());
            user.setApellido(userUpdateDto.getApellido());
            Long userId = user.getId();

            // Gestionar domicilios
            if (userUpdateDto.getAdresses() != null && !userUpdateDto.getAdresses().isEmpty()) {
                updateAdress(userUpdateDto.getAdresses(), userId);

            }

            // Guardar el usuario actualizado
            userRepository.save(user);

            // Preparar la respuesta
            UpdateUserDto updatedUserDto = modelMapperConfig.modelMapper().map(user, UpdateUserDto.class);
            return updatedUserDto;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al actualizar el usuario: " + e.getMessage());
        }
    }

    public void updateAdress(List<UpdateAdressDto> addressDto, Long UserId) {

        for (UpdateAdressDto address : addressDto) {
            if (address.getId() == null) {
                DomicilioEntity newAddress = DomicilioEntity.builder()
                        .calle(address.getCalle())
                        .numero(address.getNumero())
                        .ciudad(address.getCiudad())
                        .codigoPostal(address.getCodigoPostal())
                        .usuario(userRepository.getReferenceById(UserId))
                        .build();

                domicilioRepository.save(newAddress);
            }
            else {
                DomicilioEntity updateAdress = domicilioRepository.getReferenceById(address.getId());
                updateAdress.setCalle(address.getCalle());
                updateAdress.setNumero(address.getNumero());
                updateAdress.setCiudad(address.getCiudad());
                updateAdress.setCodigoPostal(address.getCodigoPostal());
                domicilioRepository.save(updateAdress);
            }
        }

    }

    public void deleteAdress(Long id) {
        try {
            // Simplemente busca y elimina el domicilio sin verificaciones de seguridad (temporal)
            DomicilioEntity address = domicilioRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("No existe un domicilio con el ID: " + id));
            
            // Eliminar directamente
            domicilioRepository.delete(address);
            System.out.println("Domicilio eliminado con ID: " + id);
        } catch (Exception e) {
            System.err.println("Error al eliminar domicilio: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-lanzar para manejo en controlador
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

    @Override
    public UserDto getUserById(Long id) {
       UserEntity user = userRepository.findById(id)
               .orElseThrow(()-> new IllegalArgumentException("No existe un usuario con el ID: " + id));
         UserDto userDto = new UserDto();
         userDto.setNombre(user.getNombre());
         userDto.setApellido(user.getApellido());
         userDto.setEmail(user.getEmail());
         return userDto;
    }
} 