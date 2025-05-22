import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CartService } from '../../../Services/Cart/cart.service';
import { UtilsService } from '../../../Services/Utils/utils.service';
import { PedidosService } from '../../../Services/Pedidos/pedidos.service';
import { AuthService } from '../../../Services/Auth/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

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

interface MercadoPagoItem {
  titulo: string;
  descripcion: string;
  cantidad: number;
  precioUnitario: number;
}

interface MercadoPagoRequest {
  pedidoId: number;
  codigoPedido: string;
  montoTotal: number;
  descripcion: string;
  items: MercadoPagoItem[];
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
    private authService: AuthService,
    private http: HttpClient
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
            
            // Obtener el ID y código de pedido de la respuesta
            const pedidoId = response.id || response.pedidoId;
            const codigoPedido = response.codigoPedido || response.codigo;
            
            if (!pedidoId || !codigoPedido) {
              console.error('Error: No se recibió el ID o código del pedido');
              this.errorMessage = 'Error al procesar el pedido. Datos incompletos.';
              this.isLoading = false;
              return;
            }
            
            // Procesar según el método de pago seleccionado
            if (this.selectedPaymentMethod === 'mercadopago') {
              this.procesarPagoMercadoPago(pedidoId, codigoPedido);
            } else {
              this.isLoading = false;
              this.prepareWhatsAppMessage();
              this.orderCreated = true;
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

  procesarPagoMercadoPago(pedidoId: number, codigoPedido: string): void {
    if (!this.orderSummary) {
      this.errorMessage = 'Error al procesar el pago: información del pedido no disponible';
      this.isLoading = false;
      return;
    }

    // Preparar los datos para la solicitud a Mercado Pago
    const mercadoPagoItems: MercadoPagoItem[] = this.orderSummary.items.map(item => ({
      titulo: item.product.fullName || item.product.name,
      descripcion: item.product.description || 'Producto Batistella',
      cantidad: item.quantity,
      precioUnitario: item.product.priceMinorista
    }));

    const mercadoPagoRequest: MercadoPagoRequest = {
      pedidoId: pedidoId,
      codigoPedido: codigoPedido,
      montoTotal: this.orderSummary.totalAmount,
      descripcion: `Compra en Batistella - ${codigoPedido}`,
      items: mercadoPagoItems
    };

    console.log('Enviando solicitud a MercadoPago:', mercadoPagoRequest);

    // Hacer la solicitud HTTP a la API de MercadoPago usando la URL del environment
    this.http.post(environment.mercadoPagoUrl, mercadoPagoRequest)
      .subscribe({
        next: (response: any) => {
          console.log('Respuesta de MercadoPago:', response);
          
          if (response && response.urlPago) {
            console.log('Redirigiendo a:', response.urlPago);
            
            // Guardar información del pedido en localStorage antes de redirigir
            this.saveOrderToHistory(pedidoId, codigoPedido, 'mercadopago');
            
            // Limpiar carrito
            this.cartService.clearCart();
            
            // Redirigir al usuario a la página de pago de MercadoPago en la misma pestaña
            window.location.href = response.urlPago;
          } else {
            console.error('Error: No se recibió el enlace de pago de MercadoPago');
            this.errorMessage = 'Error al procesar el pago con MercadoPago. Por favor intente nuevamente.';
            this.isLoading = false;
          }
        },
        error: (error) => {
          console.error('Error al obtener preferencia de MercadoPago:', error);
          this.errorMessage = 'Error al conectarse con MercadoPago. Por favor intente nuevamente.';
          this.isLoading = false;
        }
      });
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
  
  private saveOrderToHistory(pedidoId: number, codigoPedido: string, paymentMethod: 'mercadopago' | 'whatsapp'): void {
    // Guardar el pedido en el historial local
    const completedOrders = JSON.parse(localStorage.getItem('completedOrders') || '[]');
    completedOrders.push({
      ...this.orderSummary,
      orderId: pedidoId.toString(),
      orderCode: codigoPedido,
      orderDate: new Date().toISOString(),
      paymentMethod: paymentMethod,
      paymentStatus: 'PENDIENTE'
    });
    localStorage.setItem('completedOrders', JSON.stringify(completedOrders));
  }
  
  private completeOrder(): void {
    // Limpiar el carrito
    this.cartService.clearCart();
    
    // Redireccionar a la página de confirmación
    this.router.navigate(['/checkout/confirmation']);
  }
} 