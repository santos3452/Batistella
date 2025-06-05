import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { CartService, CartItem } from '../../../Services/Cart/cart.service';
import { UtilsService } from '../../../Services/Utils/utils.service';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../Services/Auth/auth.service';
import { User, Address } from '../../../Models/user';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-order-summary',
  templateUrl: './order-summary.component.html',
  styleUrls: ['./order-summary.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule]
})
export class OrderSummaryComponent implements OnInit, OnDestroy {
  cartItems: CartItem[] = [];
  totalItems = 0;
  subtotal = 0;
  taxAmount = 0;
  totalAmount = 0;
  
  currentUser: User | null = null;
  selectedAddress: any = null;
  deliveryNotes = '';
  isPickupSelected = false;
  
  isLoading = false;
  errorMessage = '';
  
  // Exponemos Math para poder utilizarlo en la plantilla
  Math = Math;

  private subscriptions = new Subscription();

  constructor(
    private router: Router,
    private cartService: CartService,
    public utils: UtilsService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    // Cargar datos del carrito
    this.subscriptions.add(
      this.cartService.items$.subscribe(items => {
        this.cartItems = items;
        
        if (items.length === 0) {
          // Redirigir si el carrito está vacío
          this.router.navigate(['/']);
        }
      })
    );
    
    this.subscriptions.add(
      this.cartService.totalItems$.subscribe(total => {
        this.totalItems = total;
      })
    );
    
    // Suscripción combinada para usuario actual y precio del carrito
    this.subscriptions.add(
      this.authService.currentUser$.subscribe(user => {
        this.currentUser = user;
        
        // Seleccionar primera dirección si hay direcciones disponibles
        if (user && user.domicilio && user.domicilio.length > 0) {
          this.selectedAddress = user.domicilio[0];
        }
        
        // Suscribirse al precio total considerando el tipo de usuario
        this.subscriptions.add(
          this.cartService.getTotalPriceByUserType$(user?.tipoUsuario || null).subscribe(price => {
            this.subtotal = price;
            // Puedes calcular impuestos si es necesario
            this.taxAmount = 0; // Por ahora sin impuestos adicionales
            this.totalAmount = this.subtotal + this.taxAmount;
          })
        );
      })
    );
  }
  
  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
  
  // Seleccionar la opción de retiro en local
  selectPickup(): void {
    this.isPickupSelected = true;
    this.selectedAddress = null;
  }
  
  // Seleccionar una dirección específica
  selectAddress(address: Address): void {
    this.selectedAddress = address;
    this.isPickupSelected = false;
  }
  
  // Eliminar un producto del carrito
  removeItem(item: CartItem): void {
    const productId = this.getProductId(item.product);
    this.cartService.removeFromCart(productId);
  }
  
  // Obtener el identificador único de un producto
  getProductId(product: any): string {
    const id = product.id || product.localId || '';
    const kg = product.kg || '';
    return `${id}-${kg}`;
  }
  
  /**
   * Obtiene el precio correcto del producto según el tipo de usuario
   */
  getProductPrice(product: any): number {
    return this.cartService.getProductPrice(product, this.authService.currentUser?.tipoUsuario);
  }
  
  proceedToPayment(): void {
    // Validar que las empresas tengan al menos 10 unidades
    const userRole = this.authService.userRole;
    if (userRole === 'ROLE_EMPRESA' && this.totalItems < 10) {
      const remaining = 10 - this.totalItems;
      this.errorMessage = `Las empresas deben comprar un mínimo de 10 unidades. Te faltan ${remaining} unidades más para continuar.`;
      return;
    }

    if (!this.isPickupSelected && !this.selectedAddress) {
      this.errorMessage = 'Por favor seleccione una dirección de entrega o la opción de retiro en local';
      return;
    }
    
    // Construir la dirección como string según la selección
    let direccionCompleta = 'Retiro en el local';
    
    if (!this.isPickupSelected && this.selectedAddress) {
      direccionCompleta = `${this.selectedAddress.calle} ${this.selectedAddress.numero}, ${this.selectedAddress.ciudad}, CP: ${this.selectedAddress.codigoPostal}`;
    }
    
    // Guardar información del pedido en un servicio o localStorage para acceder en la página de pago
    localStorage.setItem('orderSummary', JSON.stringify({
      items: this.cartItems,
      subtotal: this.subtotal,
      taxAmount: this.taxAmount,
      totalAmount: this.totalAmount,
      address: this.selectedAddress,
      deliveryNotes: this.deliveryNotes,
      isPickupSelected: this.isPickupSelected,
      direccionCompleta: direccionCompleta
    }));
    
    // Navegar a la página de selección de método de pago
    this.router.navigate(['/checkout/payment']);
  }
} 