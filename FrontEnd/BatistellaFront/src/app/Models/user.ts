export interface User {
  id?: number;
  email: string;
  nombre?: string;
  apellido?: string;
  rol?: string;
  tipoUsuario?: string;
  domicilio?: Address[];
  // Campos adicionales que podrían venir en la respuesta
  data?: any;
  token?: string;
}

export interface UserResponse {
  token?: string;
  user?: User;
  nombre?: string;
  apellido?: string;
  email?: string;
  data?: any;
}

export interface Address {
  id: number | null;
  calle: string;
  numero: string;
  ciudad: string;
  codigoPostal: number;
}

export interface UpdateAddressDto {
  id: number | null;
  calle: string;
  numero: string;
  ciudad: string;
  codigoPostal: number;
}

export interface UpdateUserDto {
  mail: string;
  password: string;
  nombre: string;
  apellido: string;
  adresses: UpdateAddressDto[];
} 