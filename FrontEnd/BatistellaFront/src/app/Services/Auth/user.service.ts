import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, of, switchMap } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { User } from '../../Models/user';

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
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) { }

  getUserByEmail(email: string): Observable<User> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders()
      .set('Content-Type', 'application/json')
      .set('Authorization', `Bearer ${token}`);

    return this.http.get<User>(`http://localhost:8080/api/users/email/${email}`, { headers });
  }

  register(user: UserDto): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`${this.apiUrl}/register`, user, { 
      headers,
      observe: 'response',
      responseType: 'text'
    }).pipe(
      map(response => {
        if (response.status === 201) {
          return { success: true, message: 'Usuario registrado exitosamente' };
        }
        try {
          return JSON.parse(response.body || '{}');
        } catch {
          return { success: true, message: response.body || 'Usuario registrado exitosamente' };
        }
      }),
      catchError(this.handleError)
    );
  }

  login(email: string, password: string): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`${this.apiUrl}/login`, { email, password }, { headers })
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

          return this.http.get(`http://localhost:8080/api/users/email/${email}`, { headers: authHeaders }).pipe(
            map(userData => {
              return {
                ...loginResponse,
                userData: userData
              };
            })
          );
        }),
        catchError(this.handleError)
      );
  }

  loginWithGoogle(token: string): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`${this.apiUrl}/google-login`, { token }, { headers })
      .pipe(
        catchError(this.handleError)
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
