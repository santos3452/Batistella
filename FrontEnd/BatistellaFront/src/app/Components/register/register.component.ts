import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { UserType, Role, UserDto } from '../../Services/Auth/user.service';
import { AuthService } from '../../Services/Auth/auth.service';

export interface RegisterUser {
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  confirmPassword: string;
  tipoUsuario: UserType;
  rol: Role;
}

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink]
})
export class RegisterComponent {
  user: RegisterUser = {
    nombre: '',
    apellido: '',
    email: '',
    password: '',
    confirmPassword: '',
    tipoUsuario: 'FINAL',
    rol: 'ROLE_CLIENTE'
  };

  passwordError: string = '';
  formSubmitted: boolean = false;
  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  onSubmit() {
    this.formSubmitted = true;
    this.errorMessage = '';
    this.successMessage = '';
    
    // Validar que las contraseñas coincidan
    if (this.user.password !== this.user.confirmPassword) {
      this.passwordError = 'Las contraseñas no coinciden';
      return;
    }
    
    this.passwordError = '';
    
    // Asignar el rol según el tipo de usuario
    this.user.rol = this.user.tipoUsuario === 'EMPRESA' ? 'ROLE_EMPRESA' : 'ROLE_CLIENTE';
    
    // Crear objeto para enviar a la API
    const userDto: UserDto = {
      nombre: this.user.nombre,
      apellido: this.user.apellido,
      email: this.user.email,
      password: this.user.password,
      rol: this.user.rol,
      tipoUsuario: this.user.tipoUsuario
    };
    
    // Log detallado antes de enviar
    console.log('🔍 Datos que se enviarán al backend:', JSON.stringify(userDto, null, 2));
    
    // Iniciar carga
    this.isLoading = true;
    const startTime = Date.now();
    
    // Llamada a la API
    this.authService.register(userDto).subscribe({
      next: (response) => {
        console.log('✅ Registro exitoso:', response);
        
        // Calcular cuánto tiempo ha pasado desde el inicio
        const elapsedTime = Date.now() - startTime;
        const remainingTime = Math.max(0, 2000 - elapsedTime);
        
        // Esperar al menos 2 segundos en total antes de continuar
        setTimeout(() => {
          this.isLoading = false;
          this.successMessage = '¡Registro exitoso! Iniciando sesión automáticamente...';
          
          // Iniciar sesión automáticamente
          this.loginAfterRegistration(userDto.email, userDto.password);
        }, remainingTime);
      },
      error: (error) => {
        // Si el error es 201 (Created), entonces el registro fue exitoso pero hubo un error al procesar la respuesta
        if (error.status === 201) {
          console.log('✅ Registro exitoso pero con error en respuesta:', error);
          
          // Calcular cuánto tiempo ha pasado desde el inicio
          const elapsedTime = Date.now() - startTime;
          const remainingTime = Math.max(0, 2000 - elapsedTime);
          
          // Esperar al menos 2 segundos en total antes de continuar
          setTimeout(() => {
            this.isLoading = false;
            this.successMessage = '¡Registro exitoso! Iniciando sesión automáticamente...';
            
            // Iniciar sesión automáticamente
            this.loginAfterRegistration(userDto.email, userDto.password);
          }, remainingTime);
          return;
        }
        
        console.error('❌ Error al registrar:', error);
        
        // Calcular cuánto tiempo ha pasado desde el inicio
        const elapsedTime = Date.now() - startTime;
        const remainingTime = Math.max(0, 2000 - elapsedTime);
        
        // Esperar al menos 2 segundos en total antes de mostrar el error
        setTimeout(() => {
          this.isLoading = false;
          
          // Información más detallada sobre el error
          if (error.status === 404) {
            this.errorMessage = 'No se pudo conectar con el servidor de registro. Por favor verifica que la API esté en ejecución.';
          } else if (error.status === 400) {
            this.errorMessage = 'Datos inválidos. Por favor, verifica la información ingresada.';
          } else if (error.status === 409) {
            this.errorMessage = 'El email ya está en uso. Por favor, utiliza otro email.';
          } else {
            this.errorMessage = error.error?.message || 'Ocurrió un error al registrar. Por favor, intenta nuevamente.';
          }
        }, remainingTime);
      }
    });
  }
  
  /**
   * Inicia sesión automáticamente después del registro
   */
  private loginAfterRegistration(email: string, password: string): void {
    const startTime = Date.now();
    
    this.authService.login(email, password).subscribe({
      next: (response) => {
        console.log('✅ Inicio de sesión automático exitoso:', response);
        
        // Calcular cuánto tiempo ha pasado desde el inicio
        const elapsedTime = Date.now() - startTime;
        const remainingTime = Math.max(0, 2000 - elapsedTime);
        
        // Esperar al menos 2 segundos en total antes de redirigir
        setTimeout(() => {
          // Redirigir a la página principal después de iniciar sesión
          this.router.navigate(['/']);
        }, remainingTime);
      },
      error: (error) => {
        console.error('❌ Error en el inicio de sesión automático:', error);
        
        // Calcular cuánto tiempo ha pasado desde el inicio
        const elapsedTime = Date.now() - startTime;
        const remainingTime = Math.max(0, 2000 - elapsedTime);
        
        // Esperar al menos 2 segundos en total antes de redirigir
        setTimeout(() => {
          // Si falla el inicio de sesión automático, redirigir al login manual
          this.router.navigate(['/login']);
        }, Math.max(1500, remainingTime));
      }
    });
  }
}
