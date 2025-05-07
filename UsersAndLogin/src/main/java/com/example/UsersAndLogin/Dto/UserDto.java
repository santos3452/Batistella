package com.example.UsersAndLogin.Dto;

import com.example.UsersAndLogin.Entity.enums.Role;
import com.example.UsersAndLogin.Entity.enums.UserType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private Role rol;
    private UserType tipoUsuario;

} 