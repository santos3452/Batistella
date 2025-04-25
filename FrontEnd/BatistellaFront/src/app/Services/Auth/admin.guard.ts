import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from './auth.service';

export const AdminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  if (authService.isAuthenticated() && authService.isAdmin()) {
    return true;
  }
  
  // Si el usuario no es administrador, redirigir a la página de inicio
  router.navigate(['/']);
  return false;
}; 