import { Component, EventEmitter, Output, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CartService } from '../../Services/Cart/cart.service';
import { CartDropdownComponent } from '../cart-dropdown/cart-dropdown.component';
import { Subscription } from 'rxjs';
import { AuthService } from '../../Services/Auth/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
  standalone: true,
  imports: [CommonModule, RouterLink, CartDropdownComponent]
})
export class NavbarComponent implements OnInit, OnDestroy {
  @Output() toggleSidebar = new EventEmitter<void>();
  
  totalItems = 0;
  showCart = false;
  isLoggedIn = false;
  showUserMenu = false;
  userName = '';
  private subscription: Subscription = new Subscription();

  constructor(
    private cartService: CartService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Comprobar si ya hay usuario autenticado al cargar
    this.checkAuthenticationStatus();
    
    // Suscripción al servicio de carrito
    this.subscription.add(
      this.cartService.totalItems$.subscribe(total => {
        this.totalItems = total;
      })
    );
    
    // Suscripción al estado de autenticación
    this.subscription.add(
      this.authService.isAuthenticated$.subscribe(isAuthenticated => {
        this.isLoggedIn = isAuthenticated;
        this.updateUserNameFromCurrentUser();
      })
    );
    
    // Suscripción al usuario actual
    this.subscription.add(
      this.authService.currentUser$.subscribe(user => {
        if (user) {
          this.isLoggedIn = true;
          this.updateUserNameFromUser(user);
        }
      })
    );
  }
  
  private updateUserNameFromCurrentUser(): void {
    if (this.isLoggedIn) {
      const user = this.authService.currentUser;
      if (user) {
        this.updateUserNameFromUser(user);
      } else {
        this.userName = 'Usuario';
        console.log('No se pudo obtener el usuario actual');
      }
    } else {
      this.userName = '';
    }
  }
  
  private updateUserNameFromUser(user: any): void {
    // Verificar que tengamos un nombre válido
    if (user.nombre && user.nombre.trim() !== '' && user.nombre !== 'Usuario') {
      // Si tenemos nombre, lo usamos con el apellido (si existe)
      this.userName = user.apellido ? 
        `${user.nombre} ${user.apellido}` : 
        user.nombre;
      console.log('Nombre de usuario actualizado:', this.userName);
    } else {
      // Usar email como fallback
      this.userName = user.email ? user.email.split('@')[0] : 'Usuario';
      console.log('Usando email como nombre de usuario:', this.userName);
    }
  }
  
  private checkAuthenticationStatus(): void {
    // Verificar si hay un token en localStorage
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('currentUser');
    
    if (token && userData) {
      try {
        const user = JSON.parse(userData);
        this.isLoggedIn = true;
        this.updateUserNameFromUser(user);
      } catch (error) {
        console.error('Error al parsear datos de usuario:', error);
        this.userName = 'Usuario';
      }
    }
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  onToggleSidebar(): void {
    this.toggleSidebar.emit();
  }

  toggleCart(): void {
    this.showCart = !this.showCart;
    if (this.showCart) {
      this.showUserMenu = false;
    }
  }
  
  toggleUserMenu(): void {
    this.showUserMenu = !this.showUserMenu;
    if (this.showUserMenu) {
      this.showCart = false;
    }
  }

  logout(): void {
    this.authService.logout();
    this.showUserMenu = false;
    this.isLoggedIn = false;
    this.userName = '';
  }
}
