import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CartService } from '../../../Services/Cart/cart.service';
import { UtilsService } from '../../../Services/Utils/utils.service';
import { PedidosService } from '../../../Services/Pedidos/pedidos.service';
import { AuthService } from '../../../Services/Auth/auth.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

interface OrderSummary {
  items: any[];
  subtotal: number;
  shippingCost: number;
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
      
      console.log('=== DATOS PARA CREAR PEDIDO ===');
      console.log('Productos:', productos);
      console.log('Domicilio:', domicilioDeEntrega);
      console.log('Subtotal productos:', this.orderSummary.subtotal);
      console.log('Costo envío que se enviará:', this.orderSummary.shippingCost);
      console.log('Total calculado:', this.orderSummary.totalAmount);
      console.log('================================');

      // Crear el pedido en la base de datos incluyendo el costo de envío
      this.pedidosService.crearPedido(currentUser.id, productos, domicilioDeEntrega, this.orderSummary.shippingCost)
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
              if (!this.orderSummary) {
                this.errorMessage = 'Error: información del pedido no disponible';
                this.isLoading = false;
                return;
              }
              // Registrar el pago manual antes de mostrar el modal de WhatsApp
              this.registrarPagoManual(codigoPedido, this.orderSummary.totalAmount)
                .subscribe({
                  next: (pagoResponse) => {
                    console.log('Pago manual registrado:', pagoResponse);
                    this.isLoading = false;
                    this.prepareWhatsAppMessage(codigoPedido);
                    this.orderCreated = true;
                  },
                  error: (error) => {
                    console.error('Error al registrar pago manual:', error);
                    // Continuamos con WhatsApp aunque falle el registro del pago
                    this.isLoading = false;
                    this.prepareWhatsAppMessage(codigoPedido);
                    this.orderCreated = true;
                  }
                });
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

  // Método para registrar el pago manual
  private registrarPagoManual(codigoPedido: string, monto: number) {
    const params = {
      codigoPedido: codigoPedido,
      monto: monto.toString(),
      metodo: 'Transferencia'
    };

    return this.http.post(environment.pagoManualUrl, null, { params });
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

    // Si hay costo de envío y no es retiro en local, agregarlo como ítem adicional
    if (this.orderSummary.shippingCost > 0 && !this.orderSummary.isPickupSelected) {
      mercadoPagoItems.push({
        titulo: 'Envío a domicilio',
        descripcion: 'Costo de envío del pedido',
        cantidad: 1,
        precioUnitario: this.orderSummary.shippingCost
      });
    }

    const mercadoPagoRequest: MercadoPagoRequest = {
      pedidoId: pedidoId,
      codigoPedido: codigoPedido,
      montoTotal: this.orderSummary.totalAmount,
      descripcion: `Compra en Batistella - ${codigoPedido}`,
      items: mercadoPagoItems
    };

    console.log('=== RESUMEN DETALLADO MERCADOPAGO ===');
    console.log('Subtotal productos:', this.orderSummary.subtotal);
    console.log('Costo de envío:', this.orderSummary.shippingCost);
    console.log('Es retiro en local:', this.orderSummary.isPickupSelected);
    console.log('Total calculado:', this.orderSummary.totalAmount);
    console.log('Ítems enviados a MercadoPago:');
    mercadoPagoItems.forEach((item, index) => {
      console.log(`  ${index + 1}. ${item.titulo} - Cantidad: ${item.cantidad} - Precio: $${item.precioUnitario}`);
    });
    console.log('Solicitud completa a MercadoPago:', mercadoPagoRequest);
    console.log('==========================================');

    // Obtener el token de autenticación
    const token = localStorage.getItem('token');
    console.log('Token para MercadoPago:', token);

    // Crear headers con el token explícitamente
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });

    // Hacer la solicitud HTTP a la API de MercadoPago usando la URL del environment y los headers
    this.http.post(environment.mercadoPagoUrl, mercadoPagoRequest, { headers })
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
  private prepareWhatsAppMessage(codigoPedido?: string): void {
    if (!this.orderSummary) return;
    
    // Crear el mensaje para WhatsApp
    let message = `*Nuevo Pedido de Batistella*\n\n`;
    
    if (codigoPedido) {
      message += `*Código de Pedido:* ${codigoPedido}\n\n`;
    }
    
    message += `*Productos:*\n`;
    
    this.orderSummary.items.forEach(item => {
      message += `- ${item.product.fullName} x${item.quantity}: ${this.utils.formatCurrency(item.product.priceMinorista * item.quantity)}\n`;
    });
    
    // Agregar línea del subtotal
    message += `\n*Subtotal:* ${this.utils.formatCurrency(this.orderSummary.subtotal)}\n`;
    
    // Agregar costo de envío si corresponde
    if (this.orderSummary.shippingCost > 0 && !this.orderSummary.isPickupSelected) {
      message += `*Envío:* ${this.utils.formatCurrency(this.orderSummary.shippingCost)}\n`;
    } else if (this.orderSummary.isPickupSelected) {
      message += `*Envío:* Retiro en local (sin costo)\n`;
    } else {
      message += `*Envío:* Gratis\n`;
    }
    
    message += `*Total:* ${this.utils.formatCurrency(this.orderSummary.totalAmount || 0)}\n\n`;
    message += `*Dirección de entrega:*\n`;
    
    // Usar la dirección completa que viene en el formato adecuado
    message += `${this.orderSummary.direccionCompleta}\n\n`;
    
    if (this.orderSummary.deliveryNotes) {
      message += `*Notas:* ${this.orderSummary.deliveryNotes}\n\n`;
    }
    
    // Obtener el usuario actual y personalizar el mensaje final
    const currentUser = this.authService.currentUser;
    const nombreUsuario = currentUser ? (currentUser.nombre || currentUser.email || 'Usuario') : 'Usuario';
    
    message += `Hola, mi nombre es ${nombreUsuario}, me gustaria poder hacer el pago correspondiente. Gracias!`;
    
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