import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-payment-error',
  templateUrl: './payment-error.component.html',
  styleUrls: ['./payment-error.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class PaymentErrorComponent implements OnInit {
  paymentId: string | null = null;
  errorCode: string | null = null;
  errorMessage: string = 'Ha ocurrido un error al procesar tu pago.';
  pedidoInfo: any = null;
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    // Obtener parámetros de la URL
    this.route.queryParams.subscribe(params => {
      this.paymentId = params['payment_id'] || null;
      this.errorCode = params['error'] || null;
      const externalReference = params['external_reference'] || null;
      
      console.log('Parámetros de error recibidos:', { 
        paymentId: this.paymentId, 
        errorCode: this.errorCode,
        externalReference 
      });

      // Establecer mensaje de error según el código
      if (this.errorCode) {
        this.setErrorMessage(this.errorCode);
      }

      // Buscar información del pedido
      if (externalReference) {
        this.obtenerInfoPedido(externalReference);
      } else {
        // Buscar en localStorage si no tenemos external_reference
        this.buscarPedidoLocal();
      }
    });
  }

  setErrorMessage(errorCode: string): void {
    switch(errorCode) {
      case 'rejected':
        this.errorMessage = 'El pago fue rechazado. Por favor, intenta con otro método de pago.';
        break;
      case 'cc_rejected_insufficient_amount':
        this.errorMessage = 'La tarjeta no tiene fondos suficientes.';
        break;
      case 'cc_rejected_bad_filled_security_code':
        this.errorMessage = 'El código de seguridad de la tarjeta es incorrecto.';
        break;
      case 'cc_rejected_bad_filled_date':
        this.errorMessage = 'La fecha de vencimiento de la tarjeta es incorrecta.';
        break;
      case 'cc_rejected_call_for_authorize':
        this.errorMessage = 'El banco requiere autorización. Por favor, contacta a tu banco.';
        break;
      default:
        this.errorMessage = 'Ha ocurrido un error al procesar tu pago. Por favor, intenta nuevamente.';
    }
  }

  obtenerInfoPedido(codigoPedido: string): void {
    // Consultar el API para obtener la información del pedido
    this.http.get(`${environment.pedidosUrl}/buscar-por-codigo/${codigoPedido}`)
      .subscribe({
        next: (pedido: any) => {
          console.log('Pedido obtenido:', pedido);
          this.pedidoInfo = pedido;
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error al obtener información del pedido:', error);
          this.buscarPedidoLocal();
          this.isLoading = false;
        }
      });
  }

  buscarPedidoLocal(): void {
    // Buscar en localStorage por si tenemos el pedido guardado
    const completedOrders = JSON.parse(localStorage.getItem('completedOrders') || '[]');
    const lastOrder = completedOrders.length > 0 ? completedOrders[completedOrders.length - 1] : null;
    
    if (lastOrder) {
      this.pedidoInfo = {
        codigoPedido: lastOrder.orderCode,
        monto: lastOrder.totalAmount,
        items: lastOrder.items
      };
    }
    
    this.isLoading = false;
  }

  volverAIntentarlo(): void {
    // Volver a la página de pago
    this.router.navigate(['/checkout/payment']);
  }

  volverAInicio(): void {
    this.router.navigate(['/']);
  }
} 