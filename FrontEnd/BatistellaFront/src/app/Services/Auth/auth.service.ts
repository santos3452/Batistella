import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap, catchError, of, throwError } from 'rxjs';
import { UserService, UserDto } from './user.service';
import { Router } from '@angular/router';

export interface UserInfo {
  id?: number;
  nombre: string;
  apellido: string;
  email: string;
  rol: string;
  tipoUsuario: string;
}

export interface AuthResponse {
  token: string;
  user: UserInfo;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<UserInfo | null>(null);
  public currentUser$: Observable<UserInfo | null> = this.currentUserSubject.asObservable();
  
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated$: Observable<boolean> = this.isAuthenticatedSubject.asObservable();

  constructor(
    private userService: UserService,
    private router: Router
  ) {
    // Verificar si hay un usuario en localStorage al iniciar
    this.loadUserFromStorage();
  }

  private loadUserFromStorage() {
    const userData = localStorage.getItem('currentUser');
    const token = localStorage.getItem('token');
    
    if (userData && token) {
      try {
        const user = JSON.parse(userData);
        this.currentUserSubject.next(user);
        this.isAuthenticatedSubject.next(true);
        console.log('Usuario cargado desde localStorage:', user.nombre);
      } catch (error) {
        console.error('Error al cargar los datos del usuario:', error);
        this.logout();
      }
    }
  }

  login(email: string, password: string): Observable<any> {
    console.log('Intentando iniciar sesión con:', { email, password });
    return this.userService.login(email, password).pipe(
      tap((response: any) => {
        console.log('Respuesta de login recibida:', response);
        
        let token: string;
        let user: UserInfo;
        
        // Extraer el token de la respuesta
        if (typeof response === 'string') {
          // Si la respuesta es un string, asumimos que es el token
          token = response;
        } else if (response.token) {
          // Si hay un campo token explícito
          token = response.token;
        } else if (response.data && response.data.token) {
          // Si el token está dentro de data
          token = response.data.token;
        } else {
          console.error('No se pudo extraer el token de la respuesta:', response);
          throw new Error('Token no encontrado en la respuesta');
        }
        
        // Extraer y preparar los datos del usuario
        if (response.user) {
          // Si hay un objeto user explícito
          user = response.user;
        } else if (response.nombre || response.apellido || response.email) {
          // Si los datos del usuario están en el nivel raíz
          user = {
            nombre: response.nombre || '',
            apellido: response.apellido || '',
            email: response.email || email,
            rol: response.rol || 'ROLE_CLIENTE',
            tipoUsuario: response.tipoUsuario || 'FINAL'
          };
        } else if (response.data && (response.data.nombre || response.data.email)) {
          // Si los datos vienen dentro de un objeto 'data'
          user = {
            nombre: response.data.nombre || '',
            apellido: response.data.apellido || '',
            email: response.data.email || email,
            rol: response.data.rol || 'ROLE_CLIENTE',
            tipoUsuario: response.data.tipoUsuario || 'FINAL'
          };
        } else {
          // Fallback: Si no podemos encontrar datos de usuario, usamos un objeto genérico
          console.warn('No se pudieron extraer datos del usuario de la respuesta:', response);
          user = {
            nombre: email.split('@')[0], // Usar parte del email como nombre
            apellido: '',
            email: email,
            rol: 'ROLE_CLIENTE',
            tipoUsuario: 'FINAL'
          };
        }
        
        console.log('Datos de usuario extraídos:', user);
        
        // Guardar datos en localStorage
        localStorage.setItem('token', token);
        localStorage.setItem('currentUser', JSON.stringify(user));
        
        // Actualizar los subjects
        this.currentUserSubject.next(user);
        this.isAuthenticatedSubject.next(true);
      }),
      catchError(error => {
        console.error('Error en login:', error);
        return throwError(() => error);
      })
    );
  }
  
  /**
   * Registra un nuevo usuario y maneja respuestas text/plain
   */
  register(user: UserDto): Observable<any> {
    return this.userService.registerUser(user).pipe(
      // Transformar la respuesta para manejar el caso de respuesta de texto
      tap(response => {
        console.log('Respuesta de registro recibida:', response);
        return response;
      }),
      catchError(error => {
        // Si el error es 201 (Created), entonces el registro fue exitoso
        if (error.status === 201) {
          console.log('Registro exitoso con respuesta de texto:', error);
          return of({ 
            status: 201, 
            message: error.error?.text || 'Usuario registrado exitosamente' 
          });
        }
        console.error('Error en registro:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Actualiza la información del usuario y notifica a los observadores
   */
  updateUserInfo(userInfo: UserInfo): void {
    console.log('Actualizando información de usuario:', userInfo);
    this.currentUserSubject.next(userInfo);
  }

  logout(): void {
    console.log('Cerrando sesión...');
    // Limpiar localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
    
    // Actualizar los subjects
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    
    // Redirigir al login
    this.router.navigate(['/login']);
  }

  get currentUser(): UserInfo | null {
    return this.currentUserSubject.value;
  }

  get userFullName(): string {
    const user = this.currentUserSubject.value;
    return user ? `${user.nombre} ${user.apellido}` : '';
  }

  get userToken(): string | null {
    return localStorage.getItem('token');
  }

  get userRole(): string | null {
    const user = this.currentUserSubject.value;
    return user ? user.rol : null;
  }

  /**
   * Intenta iniciar sesión con Google
   * @returns Observable con la respuesta del servidor
   */
  loginWithGoogle(): Observable<any> {
    return this.userService.loginWithGoogle().pipe(
      tap((response: any) => {
        console.log('Respuesta de login con Google:', response);
        // Si la respuesta contiene un token y datos de usuario
        if (response && response.token && response.user) {
          // Guardar datos en localStorage
          localStorage.setItem('token', response.token);
          localStorage.setItem('currentUser', JSON.stringify(response.user));
          
          // Actualizar los subjects
          this.currentUserSubject.next(response.user);
          this.isAuthenticatedSubject.next(true);
          
          console.log('Usuario autenticado con Google:', response.user.nombre);
        }
      }),
      catchError(error => {
        console.error('Error en login con Google:', error);
        return throwError(() => error);
      })
    );
  }
}
