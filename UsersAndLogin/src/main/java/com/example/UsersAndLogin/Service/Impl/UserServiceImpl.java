package com.example.UsersAndLogin.Service.Impl;

import com.example.UsersAndLogin.Dto.UserDto;
import com.example.UsersAndLogin.Entity.UserEntity;
import com.example.UsersAndLogin.Repository.UserRepository;
import com.example.UsersAndLogin.Service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
} 