import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../Services/Auth/auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink, CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
  standalone: true
})
export class LoginComponent {
  email: string = '';
  password: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  onSubmit() {
    this.errorMessage = '';

    if (!this.email || !this.password) {
      this.errorMessage = 'Por favor, completa todos los campos';
      return;
    }

    this.isLoading = true;
    const startTime = Date.now();
    
    this.authService.login(this.email, this.password).subscribe({
      next: (response) => {
        console.log('✅ Inicio de sesión exitoso (detallado):', JSON.stringify(response, null, 2));
        
        // Verificar que la respuesta sea válida
        if (!response) {
          console.warn('La respuesta es vacía o nula:', response);
          this.errorMessage = 'Error en la respuesta del servidor. Respuesta vacía.';
          this.isLoading = false;
          return;
        }
        
        // Calcular cuánto tiempo ha pasado desde el inicio
        const elapsedTime = Date.now() - startTime;
        const remainingTime = Math.max(0, 2000 - elapsedTime);
        
        // Esperar al menos 2 segundos en total antes de redirigir
        setTimeout(() => {
          this.isLoading = false;
          this.router.navigate(['/']);
        }, remainingTime);
      },
      error: (error) => {
        // Log del error completo para debug
        console.error('Error recibido:', error);
        
        // Calcular cuánto tiempo ha pasado desde el inicio
        const elapsedTime = Date.now() - startTime;
        const remainingTime = Math.max(0, 2000 - elapsedTime);
        
        // Esperar al menos 2 segundos en total antes de mostrar el error
        setTimeout(() => {
          this.isLoading = false;
          
          // El error ahora debería ser directamente el ErrorDto
          if (error && error.message) {
            this.errorMessage = error.message;
          } else if (error && error.status === 404) {
            this.errorMessage = 'No se pudo conectar con el servidor. Por favor verifica que la API esté en ejecución.';
          } else {
            this.errorMessage = 'Ocurrió un error al iniciar sesión. Por favor, intenta nuevamente.';
          }
        }, remainingTime);
      }
    });
  }

  loginWithGoogle() {
    this.errorMessage = '';
    this.isLoading = true;
    const startTime = Date.now();
    
    // En una implementación real, aquí llamarías a la API de autenticación de Google
    // Por ahora simplemente redirigimos al endpoint de OAuth
    this.authService.loginWithGoogle().subscribe({
      next: (response) => {
        // Calcular cuánto tiempo ha pasado desde el inicio
        const elapsedTime = Date.now() - startTime;
        const remainingTime = Math.max(0, 2000 - elapsedTime);
        
        // Esperar al menos 2 segundos en total antes de redirigir
        setTimeout(() => {
          this.isLoading = false;
          if (response.redirectUrl) {
            window.location.href = response.redirectUrl;
          } else {
            this.router.navigate(['/']);
          }
        }, remainingTime);
      },
      error: (error) => {
        console.error('Error al iniciar sesión con Google (detallado):', JSON.stringify(error, null, 2));
        
        // Calcular cuánto tiempo ha pasado desde el inicio
        const elapsedTime = Date.now() - startTime;
        const remainingTime = Math.max(0, 2000 - elapsedTime);
        
        // Esperar al menos 2 segundos en total antes de mostrar el error
        setTimeout(() => {
          this.isLoading = false;
          this.errorMessage = 'Error al conectar con Google. Por favor, intenta nuevamente.';
        }, remainingTime);
      }
    });
  }
}
