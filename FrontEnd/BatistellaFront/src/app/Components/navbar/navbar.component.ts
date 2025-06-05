import { Component, EventEmitter, Output, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { CartService } from '../../Services/Cart/cart.service';
import { CartDropdownComponent } from '../cart-dropdown/cart-dropdown.component';
import { Subscription, Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { AuthService } from '../../Services/Auth/auth.service';
import { UtilsService } from '../../Services/Utils/utils.service';
import { ProductService, Product } from '../../Services/Product/product.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
  standalone: true,
  imports: [CommonModule, RouterLink, CartDropdownComponent, CurrencyPipe, FormsModule]
})
export class NavbarComponent implements OnInit, OnDestroy {
  @Output() toggleSidebar = new EventEmitter<void>();
  
  totalItems = 0;
  cartTotal = 0;
  showCart = false;
  isLoggedIn = false;
  isAdmin = false;
  showUserMenu = false;
  userName = '';
  
  // Variables para la búsqueda
  searchQuery = '';
  searchResults: Product[] = [];
  showSearchResults = false;
  private searchSubject = new Subject<string>();
  
  // Mensaje de error
  errorMessage = '';
  showErrorMessage = false;
  
  private subscription: Subscription = new Subscription();

  constructor(
    private cartService: CartService,
    private authService: AuthService,
    public utils: UtilsService,
    private productService: ProductService,
    private router: Router
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
    
    // Suscripción al estado de visibilidad del carrito
    this.subscription.add(
      this.cartService.cartVisible$.subscribe(visible => {
        this.showCart = visible;
      })
    );
    
    // Suscripción al estado de autenticación
    this.subscription.add(
      this.authService.isAuthenticated$.subscribe(isAuthenticated => {
        this.isLoggedIn = isAuthenticated;
        this.updateUserNameFromCurrentUser();
      })
    );
    
    // Suscripción combinada para usuario actual y precio del carrito
    this.subscription.add(
      this.authService.currentUser$.subscribe(user => {
        if (user) {
          this.isLoggedIn = true;
          this.updateUserNameFromUser(user);
          this.isAdmin = user.rol === 'ROLE_ADMIN';
        }
        
        // Actualizar precio total del carrito según el tipo de usuario (o null si no hay usuario)
        this.subscription.add(
          this.cartService.getTotalPriceByUserType$(user?.tipoUsuario || null).subscribe(total => {
            this.cartTotal = total;
          })
        );
      })
    );
    
    // Configurar el debounce para la búsqueda
    this.subscription.add(
      this.searchSubject.pipe(
        debounceTime(300), // Esperar 300ms después de la última tecla
        distinctUntilChanged() // Solo procesar si el valor ha cambiado
      ).subscribe(query => {
        this.performSearch(query);
      })
    );
  }
  
  // Método para manejar la entrada de búsqueda
  onSearchInput(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    this.searchSubject.next(query);
  }
  
  // Método para realizar la búsqueda
  performSearch(query: string): void {
    if (!query || query.trim() === '') {
      this.searchResults = [];
      this.showSearchResults = false;
      return;
    }
    
    this.productService.searchProducts(query).subscribe({
      next: (results) => {
        console.log('Resultados de búsqueda completos:', results);
        
        // Verificar los IDs de los productos antes de mostrarlos
        const productsWithIds = results.filter(p => p && p.id);
        const productsWithoutIds = results.filter(p => !p || !p.id);
        
        console.log('Productos con IDs:', productsWithIds.length);
        console.log('Productos sin IDs:', productsWithoutIds.length);
        
        if (productsWithoutIds.length > 0) {
          console.warn('Productos sin ID encontrados:', productsWithoutIds);
        }
        
        // Mostrar todos los resultados sin filtrar por ID
        this.searchResults = results.slice(0, 5); // Limitar a 5 resultados
        
        if (this.searchResults.length === 0) {
          console.log('No se encontraron resultados para la búsqueda:', query);
        } else {
          console.log(`Se encontraron ${results.length} resultados, mostrando ${this.searchResults.length}`);
          
          // Imprimir cada producto que se mostrará
          this.searchResults.forEach((product, index) => {
            console.log(`Producto #${index + 1}:`, {
              id: product.id,
              fullName: product.fullName,
              marca: product.marca,
              tipo: product.tipoAlimento
            });
          });
        }
        
        this.showSearchResults = true;
      },
      error: (error) => {
        console.error('Error al buscar productos:', error);
        this.mostrarError('No se pudieron cargar los resultados de búsqueda.');
      }
    });
  }
  
  // Navegar a un producto
  navigateToProduct(product: any, event: MouseEvent): void {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    
    // Usar id o localId (lo que esté disponible)
    const productId = product.id || product.localId;
    
    if (productId) {
      console.log('Navegando al producto:', product.fullName, 'ID:', productId);
      this.showSearchResults = false;
      this.router.navigate(['/product', productId]);
    } else {
      console.error('Producto sin ID ni localId:', product);
      this.errorMessage = 'No se puede mostrar este producto. Intenta con otro.';
      setTimeout(() => {
        this.errorMessage = '';
      }, 3000);
    }
  }
  
  // Método auxiliar para mostrar errores
  private mostrarError(mensaje: string): void {
    this.errorMessage = mensaje;
    this.showErrorMessage = true;
    
    // Ocultar el mensaje después de 4 segundos
    setTimeout(() => {
      this.showErrorMessage = false;
      this.errorMessage = '';
    }, 4000);
  }
  
  // Navegar a la página de resultados de búsqueda
  viewAllResults(): void {
    if (this.searchQuery && this.searchQuery.trim() !== '') {
      this.router.navigate(['/search'], { queryParams: { q: this.searchQuery } });
      this.showSearchResults = false;
    }
  }
  
  // Cerrar resultados de búsqueda al hacer clic fuera
  closeSearchResults(): void {
    setTimeout(() => {
      this.showSearchResults = false;
    }, 200);
  }

  private updateUserNameFromCurrentUser(): void {
    if (this.isLoggedIn) {
      const user = this.authService.currentUser;
      if (user) {
        this.updateUserNameFromUser(user);
        this.isAdmin = user.rol === 'ROLE_ADMIN';
      } else {
        this.userName = 'Usuario';
        this.isAdmin = false;
        console.log('No se pudo obtener el usuario actual');
      }
    } else {
      this.userName = '';
      this.isAdmin = false;
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
        this.isAdmin = user.rol === 'ROLE_ADMIN';
      } catch (error) {
        console.error('Error al parsear datos de usuario:', error);
        this.userName = 'Usuario';
        this.isAdmin = false;
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
    this.cartService.toggleCart();
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
    this.showUserMenu = false;
    this.showCart = false;
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  // Método para manejar el clic en elementos del menú de usuario
  navigateToProtectedRoute(route: string): void {
    // Verificar si el usuario sigue autenticado (token válido)
    if (!this.authService.isAuthenticated()) {
      console.log('Token expirado o inválido al navegar');
      this.logout();
      return;
    }
    
    // Si está autenticado, permitir la navegación
    this.showUserMenu = false;
    this.router.navigate([route]);
  }
}
