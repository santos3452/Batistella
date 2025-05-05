package com.example.UsersAndLogin.Dto;

import com.example.UsersAndLogin.Entity.UserEntity;
import com.example.UsersAndLogin.Entity.enums.Role;
import com.example.UsersAndLogin.Entity.enums.UserType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private Role rol;
    private UserType tipoUsuario;
    
    // Constructor para convertir desde la entidad
    public static UserResponseDto fromEntity(UserEntity entity) {
        return UserResponseDto.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .apellido(entity.getApellido())
                .email(entity.getEmail())
                .rol(entity.getRol())
                .tipoUsuario(entity.getTipoUsuario())
                .build();
    }
} 