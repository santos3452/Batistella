package com.example.UsersAndLogin.Dto;

import com.example.UsersAndLogin.Entity.enums.Role;
import com.example.UsersAndLogin.Entity.enums.UserType;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@NoArgsConstructor
public class UpdateUserDto {
    private String nombre;
    private String apellido;
    private String password;
}
