package com.example.UsersAndLogin.Dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@Setter
@Getter
@NoArgsConstructor
public class UpdateUserDto {
    private String mail;
    private String password;
    private String nombre;
    private String apellido;
    private List<UpdateAdressDto> adresses;
}
