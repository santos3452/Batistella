import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export type UserType = 'FINAL' | 'EMPRESA';
export type Role = 'ROLE_CLIENTE' | 'ROLE_EMPRESA' | 'ROLE_ADMIN';

export interface UserDto {
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  rol: Role;
  tipoUsuario: UserType;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  // URL base de la API de usuarios (ahora es relativa gracias al proxy)
  private apiUrl = '/api/auth';

  constructor(private http: HttpClient) {
    console.log('UserService: API URL =', this.apiUrl);
  }

  /**
   * Registra un nuevo usuario en el sistema
   * @param user Datos del usuario a registrar
   * @returns Observable con la respuesta del servidor
   */
  registerUser(user: UserDto): Observable<any> {
    const registerUrl = `${this.apiUrl}/register`;
    console.log('Enviando petición POST a:', registerUrl);
    console.log('Datos enviados:', user);
    
    // Configurar headers para aceptar texto plano
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json, text/plain, */*'
    });
    
    return this.http.post(registerUrl, user, { 
      headers, 
      responseType: 'text',  // Aceptar respuesta como texto
      observe: 'response'    // Observar la respuesta completa
    }).pipe(
      tap(
        response => {
          console.log('Respuesta exitosa:', response);
          // Devolver un objeto con la información relevante
          return {
            status: response.status,
            message: response.body
          };
        },
        error => console.log('Error detallado:', error)
      )
    );
  }

  /**
   * Verifica las credenciales de un usuario para iniciar sesión
   * @param email Email del usuario
   * @param password Contraseña del usuario
   * @returns Observable con la respuesta del servidor
   */
  login(email: string, password: string): Observable<any> {
    const loginUrl = `${this.apiUrl}/login`;
    console.log('Enviando petición POST a:', loginUrl, { email, password });
    
    // Usar responseType: 'json' para asegurar que se intenta parsear la respuesta como JSON
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      })
    };
    
    return this.http.post(loginUrl, { email, password }, options)
      .pipe(
        tap(
          response => {
            console.log('Login exitoso - Tipo de respuesta:', typeof response);
            console.log('Login exitoso - Detalle completo:', JSON.stringify(response, null, 2));
            
            try {
              // Intentar parsear una respuesta que podría venir como string
              let parsedResponse: any = response;
              if (typeof response === 'string') {
                try {
                  parsedResponse = JSON.parse(response);
                  console.log('Respuesta JSON parseada:', parsedResponse);
                } catch (e) {
                  console.warn('La respuesta no es un JSON válido:', response);
                }
              }
              
              // Verificar estructura de respuesta esperada
              if (typeof parsedResponse === 'object') {
                console.log('Propiedades en la respuesta:', Object.keys(parsedResponse));
                
                if (!parsedResponse.hasOwnProperty('token')) {
                  console.warn('Respuesta de login no contiene token:', parsedResponse);
                  
                  // Buscar un token en cualquier parte de la respuesta
                  const responseStr = JSON.stringify(parsedResponse);
                  const tokenMatch = responseStr.match(/"token"\s*:\s*"([^"]+)"/);
                  if (tokenMatch) {
                    console.log('Se encontró un token en la respuesta:', tokenMatch[1]);
                    (parsedResponse as any).token = tokenMatch[1];
                  }
                }
                
                if (!parsedResponse.hasOwnProperty('user')) {
                  console.warn('Respuesta de login no contiene datos de usuario:', parsedResponse);
                  
                  // Buscar campos de usuario en la respuesta
                  const userFields = ['nombre', 'apellido', 'email', 'rol', 'tipoUsuario'];
                  let hasUserData = false;
                  
                  userFields.forEach(field => {
                    if (parsedResponse.hasOwnProperty(field)) {
                      hasUserData = true;
                      console.log(`Campo de usuario encontrado en raíz: ${field} =`, parsedResponse[field]);
                    }
                  });
                  
                  if (hasUserData) {
                    console.log('Se encontraron datos de usuario en la raíz de la respuesta');
                  } else if (parsedResponse.hasOwnProperty('data')) {
                    console.log('Se encontraron datos en el campo "data":', (parsedResponse as any).data);
                    
                    // Verificar campos de usuario en data
                    const data = (parsedResponse as any).data;
                    if (typeof data === 'object') {
                      userFields.forEach(field => {
                        if (data.hasOwnProperty(field)) {
                          console.log(`Campo de usuario encontrado en data: ${field} =`, data[field]);
                        }
                      });
                    }
                  }
                } else {
                  console.log('Datos de user en la respuesta:', parsedResponse.user);
                }
              }
              
              return parsedResponse;
            } catch (error) {
              console.error('Error al procesar la respuesta:', error);
              return response;
            }
          },
          error => {
            console.log('Error de login:', error);
            return error;
          }
        )
      );
  }

  /**
   * Intenta iniciar sesión con Google
   * @returns Observable con la respuesta del servidor
   */
  loginWithGoogle(): Observable<any> {
    const googleUrl = `${this.apiUrl}/oauth/google`;
    console.log('Enviando petición GET a:', googleUrl);
    return this.http.get(googleUrl)
      .pipe(
        tap(
          response => console.log('Login con Google exitoso:', response),
          error => console.log('Error de login con Google:', error)
        )
      );
  }
}
