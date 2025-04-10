import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
  standalone: true
})
export class LoginComponent {
  email: string = '';
  password: string = '';

  constructor(private router: Router) {}

  onSubmit() {
    // En un entorno real, aquí verificarías las credenciales con un servicio de autenticación
    console.log('Email:', this.email);
    console.log('Password:', this.password);

    // Simulamos un inicio de sesión exitoso
    if (this.email && this.password) {
      // Aquí implementarías la lógica de autenticación real
      // Y establecerías tokens, cookies o el estado de autenticación

      // Redireccionar a la página principal
      this.router.navigate(['/']);
    }
  }

  loginWithGoogle() {
    // En una implementación real, aquí llamarías a la API de autenticación de Google
    console.log('Iniciando sesión con Google...');
    
    // Simulamos un inicio de sesión exitoso con Google
    setTimeout(() => {
      console.log('Inicio de sesión con Google exitoso');
      this.router.navigate(['/']);
    }, 1000);
  }
}
