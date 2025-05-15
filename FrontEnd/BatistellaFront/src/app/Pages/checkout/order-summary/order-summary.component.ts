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
  selectedAddress: Address | null = null;
  deliveryNotes: string = '';
  isPickupSelected: boolean = false;
  
  isLoading = false;
  errorMessage = '';
  
  private subscriptions = new Subscription();

  constructor(
    private cartService: CartService,
    public utils: UtilsService,
    private authService: AuthService,
    private router: Router
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
    
    this.subscriptions.add(
      this.cartService.totalPrice$.subscribe(price => {
        this.subtotal = price;
        // Puedes calcular impuestos si es necesario
        this.taxAmount = 0; // Por ahora sin impuestos adicionales
        this.totalAmount = this.subtotal + this.taxAmount;
      })
    );
    
    // Cargar usuario y direcciones
    this.currentUser = this.authService.currentUser;
    
    if (this.currentUser && this.currentUser.domicilio && this.currentUser.domicilio.length > 0) {
      this.selectedAddress = this.currentUser.domicilio[0]; // Seleccionar la primera dirección por defecto
    }
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
  
  proceedToPayment(): void {
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