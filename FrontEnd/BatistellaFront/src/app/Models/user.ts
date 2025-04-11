export interface User {
  id?: number;
  email: string;
  nombre?: string;
  apellido?: string;
  rol?: string;
  tipoUsuario?: string;
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