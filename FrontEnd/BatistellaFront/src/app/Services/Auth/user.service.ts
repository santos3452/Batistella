import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, of, switchMap } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { User, UpdateUserDto, UpdateAddressDto } from '../../Models/user';
import { environment } from '../../../environments/environment';

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
  private authUrl = environment.authUrl;
  private usersUrl = environment.usersUrl;

  constructor(private http: HttpClient) { }

  getUserByEmail(email: string): Observable<User> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders()
      .set('Content-Type', 'application/json')
      .set('Authorization', `Bearer ${token}`);

    return this.http.get<User>(`${this.usersUrl}/email/${email}`, { headers });
  }

  register(user: UserDto): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`${this.authUrl}/register`, user, { headers }).pipe(
      map(response => {
        return { success: true, message: 'Usuario registrado exitosamente' };
      }),
      catchError((error: any) => {
        console.log('Error en el registro:', error);
        // Si el error contiene el texto de éxito, significa que el registro fue exitoso
        if (error.error?.text === 'Usuario registrado exitosamente') {
          return of({ success: true, message: 'Usuario registrado exitosamente' });
        }
        // Si hay otro tipo de error
        if (error.error) {
          return throwError(() => error.error);
        }
        return throwError(() => error);
      })
    );
  }

  login(email: string, password: string): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`${this.authUrl}/login`, { email, password }, { headers })
      .pipe(
        map(response => {
          if (typeof response === 'string') {
            try {
              return JSON.parse(response);
            } catch (e) {
              return response;
            }
          }
          return response;
        }),
        switchMap(loginResponse => {
          // Obtener el token de la respuesta
          const token = loginResponse.token || loginResponse;
          
          // Crear headers con el token
          const authHeaders = new HttpHeaders()
            .set('Content-Type', 'application/json')
            .set('Authorization', `Bearer ${token}`);

          return this.http.get(`${this.usersUrl}/email/${email}`, { headers: authHeaders }).pipe(
            map(userData => {
              console.log('Datos completos del usuario obtenidos:', userData);
              return {
                ...loginResponse,
                userData: userData
              };
            })
          );
        }),
        catchError((error: any) => {
          console.log('Error en el servicio:', error);
          // Si el error tiene un cuerpo de respuesta, lo devolvemos
          if (error.error) {
            return throwError(() => error.error);
          }
          return throwError(() => error);
        })
      );
  }

  updateUser(userData: UpdateUserDto): Observable<any> {
    const token = localStorage.getItem('token');
    console.log('Token para actualización:', token);

    if (!token) {
      console.error('No se encontró el token de autorización');
      return throwError(() => new Error('No hay token de autorización'));
    }

    const headers = new HttpHeaders()
      .set('Content-Type', 'application/json')
      .set('Authorization', `Bearer ${token}`);

    console.log('Datos para actualización:', userData);

    return this.http.put(`${this.usersUrl}/updateUser`, userData, { 
      headers,
      responseType: 'text'
    }).pipe(
      map(response => {
        console.log('Respuesta de actualización:', response);
        return { success: true, message: response };
      }),
      catchError(error => {
        console.error('Error en la actualización:', error);
        
        // Si el error es de autorización
        if (error.status === 403) {
          return throwError(() => ({ 
            success: false, 
            message: error.error?.mensaje || 'No tienes permiso para actualizar los datos de otro usuario'
          }));
        }
        
        // Si el error es de validación
        if (error.status === 400) {
          return throwError(() => ({ 
            success: false, 
            message: error.error?.mensaje || 'Error en la validación de los datos'
          }));
        }

        // Si el error contiene el texto de éxito (por si acaso)
        if (error.error?.text === 'Usuario actualizado exitosamente') {
          return of({ success: true, message: 'Usuario actualizado exitosamente' });
        }

        return throwError(() => ({ 
          success: false, 
          message: error.error?.mensaje || 'Error al actualizar el perfil'
        }));
      })
    );
  }

  verifyCurrentPassword(email: string, currentPassword: string): Observable<boolean> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders()
      .set('Content-Type', 'application/json')
      .set('Authorization', `Bearer ${token}`);

    return this.http.post<boolean | { success: boolean; message: string }>(
      `${this.usersUrl}/verify-password`, 
      { email, password: currentPassword }, 
      { headers }
    ).pipe(
      map(response => {
        if (typeof response === 'boolean') {
          return response;
        }
        return response.success;
      }),
      catchError(error => {
        console.error('Error al verificar la contraseña:', error);
        return of(false);
      })
    );
  }

  changePassword(oldPassword: string, newPassword: string): Observable<any> {
    const token = localStorage.getItem('token');
    if (!token) {
      return throwError(() => new Error('No hay token de autorización'));
    }

    const headers = new HttpHeaders()
      .set('Content-Type', 'application/json')
      .set('Authorization', `Bearer ${token}`);

    return this.http.post(
      `${this.usersUrl}/change-password`,
      null,
      { 
        headers,
        params: {
          oldPassword,
          newPassword
        },
        responseType: 'text'
      }
    ).pipe(
      map(response => {
        console.log('Respuesta cambio de contraseña:', response);
        return { success: true, message: response };
      }),
      catchError(error => {
        console.error('Error al cambiar la contraseña:', error);
        // Si el error es un string, intentamos parsearlo como JSON
        if (error.error && typeof error.error === 'string') {
          try {
            const errorObj = JSON.parse(error.error);
            return throwError(() => ({ message: errorObj.message }));
          } catch (e) {
            // Si no se puede parsear, devolvemos el error como está
            return throwError(() => ({ message: error.error }));
          }
        }
        // Si ya es un objeto, solo pasamos el mensaje
        if (error.error && error.error.message) {
          return throwError(() => ({ message: error.error.message }));
        }
        return throwError(() => ({ message: 'Error al cambiar la contraseña' }));
      })
    );
  }

  deleteUser(): Observable<any> {
    const token = localStorage.getItem('token');
    
    if (!token) {
      return throwError(() => new Error('No hay token de autorización'));
    }

    const headers = new HttpHeaders()
      .set('Content-Type', 'application/json')
      .set('Authorization', `Bearer ${token}`);

    return this.http.delete(`${this.usersUrl}/deleteUser`, { 
      headers,
      responseType: 'text'
    }).pipe(
      map(response => {
        return { success: true, message: response || 'Usuario eliminado exitosamente' };
      }),
      catchError(error => {
        console.error('Error al eliminar usuario:', error);
        return throwError(() => ({
          success: false,
          message: error.error?.mensaje || 'Error al eliminar el usuario'
        }));
      })
    );
  }

  reactivateAccount(email: string): Observable<any> {
    return this.http.post(
      `${this.authUrl}/reactivate`,
      null,
      {
        params: { email },
        responseType: 'text'
      }
    ).pipe(
      map(response => {
        return { success: true, message: response || 'Cuenta reactivada exitosamente' };
      }),
      catchError(error => {
        console.error('Error al reactivar la cuenta:', error);
        return throwError(() => ({
          success: false,
          message: error.error?.message || 'Error al reactivar la cuenta'
        }));
      })
    );
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.authUrl}/forgot-password`, null, {
      params: { email },
      headers: new HttpHeaders().set('Content-Type', 'application/json'),
      responseType: 'text'
    }).pipe(
      map(response => {
        return { success: true, message: response };
      }),
      catchError((error: HttpErrorResponse) => {
        // Si recibimos una respuesta exitosa pero como error (debido al tipo de respuesta)
        if (error.error && typeof error.error === 'string' && error.error.includes('enlace')) {
          return of({ success: true, message: error.error });
        }
        console.error('Error en recuperación de contraseña:', error);
        return throwError(() => error);
      })
    );
  }

  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.authUrl}/reset-password`, null, {
      params: { token, newPassword },
      headers: new HttpHeaders().set('Content-Type', 'application/json'),
      responseType: 'text'
    }).pipe(
      map(response => {
        return { success: true, message: response };
      }),
      catchError((error: HttpErrorResponse) => {
        console.error('Error al restablecer la contraseña:', error);
        return throwError(() => error);
      })
    );
  }

  private handleError(error: HttpErrorResponse) {
    // Si es un código 201, no lo tratamos como error
    if (error.status === 201) {
      return of({ success: true, message: 'Operación exitosa' });
    }

    let errorMessage = 'An error occurred';
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = error.error.message;
    } else {
      // Server-side error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
