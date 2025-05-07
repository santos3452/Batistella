import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CartService } from '../../../Services/Cart/cart.service';
import { UtilsService } from '../../../Services/Utils/utils.service';

interface OrderSummary {
  items: any[];
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  address: any;
  deliveryNotes: string;
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

  constructor(
    private router: Router, 
    private cartService: CartService,
    public utils: UtilsService
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
    
    this.isLoading = true;
    
    try {
      // Implementación según el método de pago seleccionado
      if (this.selectedPaymentMethod === 'mercadopago') {
        // Implementar lógica para pago con MercadoPago
        // Por ahora simulamos una redirección
        setTimeout(() => {
          // Aquí iría la integración real con MercadoPago
          alert('Redirigiendo a MercadoPago...');
          this.completeOrder();
        }, 1500);
      } else if (this.selectedPaymentMethod === 'whatsapp') {
        // Crear el mensaje para WhatsApp
        let message = `*Nuevo Pedido de Batistella*\n\n`;
        message += `*Productos:*\n`;
        
        this.orderSummary?.items.forEach(item => {
          message += `- ${item.product.fullName} x${item.quantity}: ${this.utils.formatCurrency(item.product.priceMinorista * item.quantity)}\n`;
        });
        
        message += `\n*Total:* ${this.utils.formatCurrency(this.orderSummary?.totalAmount || 0)}\n\n`;
        message += `*Dirección de entrega:*\n`;
        message += `${this.orderSummary?.address.calle} ${this.orderSummary?.address.numero}, ${this.orderSummary?.address.ciudad}, CP: ${this.orderSummary?.address.codigoPostal}\n\n`;
        
        if (this.orderSummary?.deliveryNotes) {
          message += `*Notas:* ${this.orderSummary.deliveryNotes}\n\n`;
        }
        
        message += `Hola, quisiera realizar este pedido para pagar en efectivo/transferencia.`;
        
        // Codificar el mensaje para la URL
        const encodedMessage = encodeURIComponent(message);
        
        // Número de WhatsApp formateado (reemplazar con el número real)
        const phoneNumber = '5493516750801'; // Ejemplo: +54 9 3541 123456
        
        // URL para abrir WhatsApp con el mensaje
        const whatsappUrl = `https://wa.me/${phoneNumber}?text=${encodedMessage}`;
        
        // Completar el pedido y luego abrir WhatsApp
        this.completeOrder();
        
        // Abrir WhatsApp en una nueva pestaña
        window.open(whatsappUrl, '_blank');
      }
    } catch (error) {
      console.error('Error al procesar el pago:', error);
      this.errorMessage = 'Error al procesar el pago. Por favor intente nuevamente.';
      this.isLoading = false;
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