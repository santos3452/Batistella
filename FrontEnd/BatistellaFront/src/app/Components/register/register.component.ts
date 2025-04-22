import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { UserType, Role, UserDto, UserService } from '../../Services/Auth/user.service';
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
  styleUrls: ['./register.component.css'],
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
  showReactivateModal: boolean = false;
  deactivatedEmail: string = '';
  showPassword: boolean = false;
  showConfirmPassword: boolean = false;

  constructor(
    private router: Router,
    private authService: AuthService,
    private userService: UserService
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
    
    // Llamada a la API usando AuthService
    this.authService.register(userDto).subscribe({
      next: (response) => {
        console.log('✅ Registro exitoso:', response);
        
        const elapsedTime = Date.now() - startTime;
        const remainingTime = Math.max(0, 2000 - elapsedTime);
        
        setTimeout(() => {
          this.isLoading = false;
          this.successMessage = '¡Registro exitoso! Iniciando sesión automáticamente...';
          
          this.loginAfterRegistration(userDto.email, userDto.password);
        }, remainingTime);
      },
      error: (error) => {
        console.error('Error en el registro:', error);
        
        const elapsedTime = Date.now() - startTime;
        const remainingTime = Math.max(0, 2000 - elapsedTime);
        
        setTimeout(() => {
          this.isLoading = false;
          
          if (error && error.message && error.message.includes('está dada de baja')) {
            this.deactivatedEmail = this.user.email;
            this.showReactivateModal = true;
          } else if (error && error.message) {
            this.errorMessage = error.message;
          } else {
            this.errorMessage = 'Ocurrió un error al registrar el usuario. Por favor, intenta nuevamente.';
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

  private resetForm(): void {
    this.user.email = '';
    this.user.password = '';
    this.user.confirmPassword = '';
    this.user.nombre = '';
    this.user.apellido = '';
    this.user.tipoUsuario = 'FINAL';
  }

  reactivateAccount() {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.authService.reactivateAccount(this.deactivatedEmail).subscribe({
      next: () => {
        this.isLoading = false;
        this.showReactivateModal = false;
        // Redirigir al login con un mensaje de éxito
        this.router.navigate(['/login'], { 
          state: { 
            successMessage: 'Cuenta reactivada exitosamente. Por favor, inicia sesión.' 
          }
        });
      },
      error: (error) => {
        this.isLoading = false;
        this.showReactivateModal = false;
        this.errorMessage = error.message || 'Error al reactivar la cuenta. Por favor, intenta nuevamente.';
      }
    });
  }

  cancelReactivation() {
    this.showReactivateModal = false;
  }
}

