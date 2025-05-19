import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CartService } from '../../../Services/Cart/cart.service';
import { UtilsService } from '../../../Services/Utils/utils.service';
import { PedidosService } from '../../../Services/Pedidos/pedidos.service';
import { AuthService } from '../../../Services/Auth/auth.service';

interface OrderSummary {
  items: any[];
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  address: any;
  deliveryNotes: string;
  isPickupSelected?: boolean;
  direccionCompleta?: string;
}

@Component({
  selector: 'app-payment-method',
  templateUrl: './payment-method.component.html',
  styleUrls: ['./payment-method.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule]
})
export class PaymentMethodComponent implements OnInit {
  orderSummary: OrderSummary | null = null;
  selectedPaymentMethod: 'mercadopago' | 'whatsapp' | null = null;
  isLoading = false;
  errorMessage = '';
  orderCreated = false;
  whatsappUrl = '';
  
  constructor(
    private router: Router, 
    private cartService: CartService,
    public utils: UtilsService,
    private pedidosService: PedidosService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Recuperar el resumen del pedido del localStorage
    const savedOrderSummary = localStorage.getItem('orderSummary');
    
    if (!savedOrderSummary) {
      // Si no hay resumen, volver a la página de resumen
      this.router.navigate(['/checkout/summary']);
      return;
    }
    
    try {
      this.orderSummary = JSON.parse(savedOrderSummary);
    } catch (error) {
      console.error('Error al parsear el resumen del pedido:', error);
      this.errorMessage = 'Error al cargar la información del pedido';
      
      // Navegar al resumen en caso de error
      setTimeout(() => {
        this.router.navigate(['/checkout/summary']);
      }, 2000);
    }
  }
  
  selectPaymentMethod(method: 'mercadopago' | 'whatsapp'): void {
    this.selectedPaymentMethod = method;
  }
  
  confirmOrder(): void {
    if (!this.selectedPaymentMethod) {
      this.errorMessage = 'Por favor seleccione un método de pago';
      return;
    }
    
    if (!this.orderSummary) {
      this.errorMessage = 'Error al cargar la información del pedido';
      return;
    }
    
    this.isLoading = true;
    
    try {
      // Obtener el ID de usuario del token o del servicio de autenticación
      const currentUser = this.authService.currentUser;
      
      if (!currentUser || !currentUser.id) {
        this.errorMessage = 'Error de autenticación, por favor inicie sesión nuevamente.';
        this.isLoading = false;
        return;
      }
      
      // Preparar los datos del pedido para la API
      const productos = this.orderSummary.items.map(item => ({
        productoId: item.product.id,
        cantidad: item.quantity
      }));
      
      // Obtener la dirección completa del pedido
      const domicilioDeEntrega = this.orderSummary.direccionCompleta || 'Retiro en el local';
      
      // Crear el pedido en la base de datos
      this.pedidosService.crearPedido(currentUser.id, productos, domicilioDeEntrega)
        .subscribe({
          next: (response) => {
            console.log('Pedido creado correctamente:', response);
            this.isLoading = false;
            
            // Preparar mensaje para WhatsApp si es necesario
            if (this.selectedPaymentMethod === 'whatsapp') {
              this.prepareWhatsAppMessage();
            }
            
            // Mostrar el modal de confirmación
            this.orderCreated = true;
            
            // Si es MercadoPago, seguimos el flujo después de un tiempo
            if (this.selectedPaymentMethod === 'mercadopago') {
              setTimeout(() => {
                alert('Redirigiendo a MercadoPago...');
                this.completeOrder();
              }, 1500);
            }
          },
          error: (error) => {
            console.error('Error al crear el pedido:', error);
            this.errorMessage = 'Error al crear el pedido. Por favor intente nuevamente.';
            this.isLoading = false;
          }
        });
      
    } catch (error) {
      console.error('Error al procesar el pago:', error);
      this.errorMessage = 'Error al procesar el pago. Por favor intente nuevamente.';
      this.isLoading = false;
    }
  }
  
  // Preparar el mensaje para WhatsApp y la URL
  private prepareWhatsAppMessage(): void {
    if (!this.orderSummary) return;
    
    // Crear el mensaje para WhatsApp
    let message = `*Nuevo Pedido de Batistella*\n\n`;
    message += `*Productos:*\n`;
    
    this.orderSummary.items.forEach(item => {
      message += `- ${item.product.fullName} x${item.quantity}: ${this.utils.formatCurrency(item.product.priceMinorista * item.quantity)}\n`;
    });
    
    message += `\n*Total:* ${this.utils.formatCurrency(this.orderSummary.totalAmount || 0)}\n\n`;
    message += `*Dirección de entrega:*\n`;
    
    // Usar la dirección completa que viene en el formato adecuado
    message += `${this.orderSummary.direccionCompleta}\n\n`;
    
    if (this.orderSummary.deliveryNotes) {
      message += `*Notas:* ${this.orderSummary.deliveryNotes}\n\n`;
    }
    
    message += `Hola, quisiera confirmar este pedido para pagar en efectivo. El pedido ya fue registrado en el sistema con estado PENDIENTE. ¡Gracias!`;
    
    // Codificar el mensaje para la URL
    const encodedMessage = encodeURIComponent(message);
    
    // Número de WhatsApp formateado
    const phoneNumber = '5493516750801'; // Reemplazar con el número real
    
    // URL para abrir WhatsApp con el mensaje
    this.whatsappUrl = `https://wa.me/${phoneNumber}?text=${encodedMessage}`;
  }
  
  // Método para continuar a WhatsApp después de ver el resumen
  continuarAWhatsApp(): void {
    // Completar el flujo de compra
    this.completeOrder();
    
    // Abrir WhatsApp en una nueva pestaña
    if (this.whatsappUrl) {
      window.open(this.whatsappUrl, '_blank');
    }
  }
  
  private completeOrder(): void {
    // Guardar el pedido en el historial (esto podría hacerlo un servicio)
    const completedOrders = JSON.parse(localStorage.getItem('completedOrders') || '[]');
    completedOrders.push({
      ...this.orderSummary,
      orderId: Date.now().toString(),
      orderDate: new Date().toISOString(),
      paymentMethod: this.selectedPaymentMethod
    });
    localStorage.setItem('completedOrders', JSON.stringify(completedOrders));
    
    // Limpiar el carrito
    this.cartService.clearCart();
    
    // Redireccionar a la página de confirmación
    this.router.navigate(['/checkout/confirmation']);
  }
} 