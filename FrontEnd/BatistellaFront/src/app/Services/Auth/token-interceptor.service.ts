import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class TokenInterceptorService implements HttpInterceptor {
  constructor(private authService: AuthService) { }

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
      
      return next.handle(clonedRequest);
    }
    
    return next.handle(request);
  }
}
