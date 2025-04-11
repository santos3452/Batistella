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
        
        // Verificar que la respuesta contenga un token
        if (!response.token) {
          console.warn('La respuesta no contiene un token:', response);
          this.errorMessage = 'Error en la respuesta del servidor. No se recibió un token válido.';
          this.isLoading = false;
          return;
        }
        
        // Ya no necesitamos verificar manualmente el nombre ya que el servicio
        // se encarga de obtener los datos completos del usuario
        
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
        console.error('❌ Error al iniciar sesión (detallado):', JSON.stringify(error, null, 2));
        
        // Calcular cuánto tiempo ha pasado desde el inicio
        const elapsedTime = Date.now() - startTime;
        const remainingTime = Math.max(0, 2000 - elapsedTime);
        
        // Esperar al menos 2 segundos en total antes de mostrar el error
        setTimeout(() => {
          this.isLoading = false;
          
          // Información más detallada sobre el error
          if (error.status === 404) {
            this.errorMessage = 'No se pudo conectar con el servidor. Por favor verifica que la API esté en ejecución.';
          } else if (error.status === 401) {
            this.errorMessage = 'Credenciales incorrectas. Por favor, verifica tu email y contraseña.';
          } else if (error.status === 400) {
            this.errorMessage = 'Datos inválidos. Por favor, verifica la información ingresada.';
          } else {
            this.errorMessage = error.error?.message || 'Ocurrió un error al iniciar sesión. Por favor, intenta nuevamente.';
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
