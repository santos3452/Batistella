import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../Services/Auth/auth.service';
import { UserService } from '../../Services/Auth/user.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule]
})
export class LoginComponent {
  email: string = '';
  password: string = '';
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;
  showReactivateModal: boolean = false;
  deactivatedEmail: string = '';
  showForgotPasswordModal: boolean = false;
  forgotPasswordEmail: string = '';
  forgotPasswordMessage: string = '';
  showPassword: boolean = false;

  constructor(
    private userService: UserService,
    private router: Router,
    private authService: AuthService
  ) {
    // Verificar si hay un mensaje de éxito en el estado de navegación
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras.state) {
      const state = navigation.extras.state as { successMessage?: string };
      if (state.successMessage) {
        this.successMessage = state.successMessage;
      }
    }
  }

  goToHome() {
    this.router.navigate(['/']);
  }

  goToRegister() {
    this.router.navigate(['/register']);
  }

  onSubmit() {
    this.errorMessage = '';
    this.successMessage = '';
    this.showReactivateModal = false;

    if (!this.email || !this.password) {
      this.errorMessage = 'Por favor, completa todos los campos';
      return;
    }

    this.isLoading = true;
    const startTime = Date.now();
    
    this.authService.login(this.email, this.password).subscribe({
      next: (response) => {
        console.log('✅ Inicio de sesión exitoso (detallado):', JSON.stringify(response, null, 2));
        
        if (!response) {
          console.warn('La respuesta es vacía o nula:', response);
          this.errorMessage = 'Error en la respuesta del servidor. Respuesta vacía.';
          this.isLoading = false;
          return;
        }
        
        const elapsedTime = Date.now() - startTime;
        const remainingTime = Math.max(0, 2000 - elapsedTime);
        
        setTimeout(() => {
          this.isLoading = false;
          this.router.navigate(['/']);
        }, remainingTime);
      },
      error: (error) => {
        console.error('Error en el login:', error);
        
        const elapsedTime = Date.now() - startTime;
        const remainingTime = Math.max(0, 2000 - elapsedTime);
        
        setTimeout(() => {
          this.isLoading = false;
          
          if (error && error.message && error.message.includes('está dada de baja')) {
            this.deactivatedEmail = this.email;
            this.showReactivateModal = true;
          } else if (error && error.message) {
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

  reactivateAccount() {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';
    
    this.authService.reactivateAccount(this.deactivatedEmail).subscribe({
      next: () => {
        this.isLoading = false;
        this.showReactivateModal = false;
        this.successMessage = 'Cuenta reactivada exitosamente. Por favor, inicia sesión nuevamente.';
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

  toggleForgotPasswordModal() {
    this.showForgotPasswordModal = !this.showForgotPasswordModal;
    if (!this.showForgotPasswordModal) {
      this.forgotPasswordEmail = '';
      this.forgotPasswordMessage = '';
    }
  }

  onForgotPasswordSubmit() {
    if (!this.forgotPasswordEmail) {
      this.forgotPasswordMessage = 'Por favor, ingresa tu correo electrónico';
      return;
    }

    this.isLoading = true;
    this.forgotPasswordMessage = '';

    this.authService.forgotPassword(this.forgotPasswordEmail).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.forgotPasswordMessage = response.message || 'Se ha enviado un enlace de recuperación a tu correo electrónico';
        if (response.success) {
          setTimeout(() => {
            this.toggleForgotPasswordModal();
          }, 3000);
        }
      },
      error: (error) => {
        this.isLoading = false;
        if (error.error && typeof error.error === 'string') {
          this.forgotPasswordMessage = error.error;
        } else {
          this.forgotPasswordMessage = error.message || 'Ocurrió un error al procesar tu solicitud';
        }
      }
    });
  }
}
