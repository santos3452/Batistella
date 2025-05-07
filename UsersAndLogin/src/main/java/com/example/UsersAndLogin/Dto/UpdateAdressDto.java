package com.example.UsersAndLogin.Dto;


import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@NoArgsConstructor
public class UpdateAdressDto {
    private Long id;
    private String calle;
    private String numero;
    private String ciudad;
    private int codigoPostal;
}
