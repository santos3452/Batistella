import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CartService, CartItem } from '../../Services/Cart/cart.service';
import { UtilsService } from '../../Services/Utils/utils.service';
import { AuthService } from '../../Services/Auth/auth.service';
import { Subscription } from 'rxjs';
import { RouterLink, Router } from '@angular/router';

@Component({
  selector: 'app-cart-dropdown',
  templateUrl: './cart-dropdown.component.html',
  styleUrl: './cart-dropdown.component.css',
  standalone: true,
  imports: [CommonModule, RouterLink]
})
export class CartDropdownComponent implements OnInit, OnDestroy {
  items: CartItem[] = [];
  totalItems = 0;
  totalPrice = 0;
  canProceedToCheckout = true;
  isCompany = false;
  minimumOrderMessage = '';
  private subscription: Subscription = new Subscription();

  constructor(
    private cartService: CartService,
    public utils: UtilsService,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Verificar si es empresa inicialmente
    this.isCompany = this.authService.userRole === 'ROLE_EMPRESA';

    // Suscribirse a los items del carrito
    this.subscription.add(
      this.cartService.items$.subscribe(items => {
        this.items = items;
        this.updateCheckoutValidation();
      })
    );

    // Suscribirse al total de items
    this.subscription.add(
      this.cartService.totalItems$.subscribe(total => {
        this.totalItems = total;
        this.updateCheckoutValidation();
      })
    );

    // Suscripción combinada para usuario actual y precio del carrito
    this.subscription.add(
      this.authService.currentUser$.subscribe(user => {
        // Actualizar si es empresa
        this.isCompany = user?.tipoUsuario === 'EMPRESA' || this.authService.userRole === 'ROLE_EMPRESA';
        this.updateCheckoutValidation();
        
        // Suscribirse al precio total considerando el tipo de usuario
        this.subscription.add(
          this.cartService.getTotalPriceByUserType$(user?.tipoUsuario || null).subscribe(price => {
            this.totalPrice = price;
          })
        );
      })
    );
  }

  ngOnDestroy(): void {
    // Limpiar suscripciones al destruir el componente
    this.subscription.unsubscribe();
  }

  /**
   * Actualiza la validación para proceder al checkout
   */
  private updateCheckoutValidation(): void {
    if (this.isCompany) {
      this.canProceedToCheckout = this.totalItems >= 10;
      
      if (!this.canProceedToCheckout) {
        const remaining = 10 - this.totalItems;
        this.minimumOrderMessage = `Las empresas deben comprar un mínimo de 10 unidades. Te faltan ${remaining} unidades más.`;
      } else {
        this.minimumOrderMessage = '';
      }
    } else {
      this.canProceedToCheckout = true;
      this.minimumOrderMessage = '';
    }
  }

  // Obtiene un identificador único para el producto
  getProductId(product: CartItem['product']): string {
    // Mismo método que en CartService
    const id = product.id || product.localId || '';
    const kg = product.kg || '';
    return `${id}-${kg}`;
  }

  removeItem(item: CartItem): void {
    const productId = this.getProductId(item.product);
    this.cartService.removeFromCart(productId);
  }

  updateQuantity(item: CartItem, quantity: number): void {
    const productId = this.getProductId(item.product);
    this.cartService.updateQuantity(productId, quantity);
  }

  incrementQuantity(item: CartItem): void {
    this.updateQuantity(item, item.quantity + 1);
  }

  decrementQuantity(item: CartItem): void {
    if (item.quantity > 1) {
      this.updateQuantity(item, item.quantity - 1);
    }
  }

  goToCheckout(): void {
    // Verificar si puede proceder al checkout
    if (!this.canProceedToCheckout) {
      return; // No hacer nada si no puede proceder
    }

    // Navegar a la página de resumen del pedido
    this.router.navigate(['/checkout/summary']);
    
    // Cerrar el carrito
    this.cartService.closeCart();
  }

  /**
   * Obtiene el precio correcto del producto según el tipo de usuario
   */
  getProductPrice(product: CartItem['product']): number {
    return this.cartService.getProductPrice(product, this.authService.currentUser?.tipoUsuario);
  }
}
