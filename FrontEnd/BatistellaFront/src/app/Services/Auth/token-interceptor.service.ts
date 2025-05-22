import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class TokenInterceptorService implements HttpInterceptor {
  constructor(private authService: AuthService, private router: Router) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Obtener el token desde el servicio de autenticación
    const token = localStorage.getItem('token');
    
    if (token) {
      console.log('Interceptor - Token original:', token);
      
      // Verificar si la petición ya tiene headers de autorización
      if (request.headers.has('Authorization')) {
        console.log('La petición ya tiene headers de autorización, no se modificará');
        return next.handle(request);
      }
      
      // Clonar la petición y añadir el token en el header
      const clonedRequest = request.clone({
        setHeaders: {
          'Authorization': `Bearer ${token}`,
          'accept': 'application/hal+json'
        }
      });
      
      console.log('Interceptor - URL:', request.url);
      console.log('Interceptor - Headers:', clonedRequest.headers.get('Authorization'));
      
      return next.handle(clonedRequest).pipe(
        catchError((error: HttpErrorResponse) => {
          // Si el error es 401 (Unauthorized) o 403 (Forbidden), el token ha expirado o es inválido
          if (error.status === 401 || error.status === 403) {
            console.log('Token expirado o inválido. Cerrando sesión...');
            this.authService.logout();
          }
          return throwError(() => error);
        })
      );
    }
    
    return next.handle(request);
  }
}
