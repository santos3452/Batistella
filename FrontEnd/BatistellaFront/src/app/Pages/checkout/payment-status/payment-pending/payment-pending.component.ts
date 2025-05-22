import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { UtilsService } from '../../../../Services/Utils/utils.service';

@Component({
  selector: 'app-payment-pending',
  templateUrl: './payment-pending.component.html',
  styleUrls: ['./payment-pending.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class PaymentPendingComponent implements OnInit {
  paymentId: string | null = null;
  pedidoInfo: any = null;
  isLoading = true;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    public utils: UtilsService
  ) {}

  ngOnInit(): void {
    // Obtener parámetros de la URL
    this.route.queryParams.subscribe(params => {
      this.paymentId = params['payment_id'] || null;
      const externalReference = params['external_reference'] || null;
      
      console.log('Parámetros recibidos:', { 
        paymentId: this.paymentId, 
        externalReference 
      });

      // Buscar información del pedido
      if (externalReference) {
        this.obtenerInfoPedido(externalReference);
      } else {
        // Buscar en localStorage si no tenemos external_reference
        this.buscarPedidoLocal();
      }
    });
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
          this.errorMessage = 'No se pudo obtener la información del pedido';
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
        items: lastOrder.items,
        estado: 'PENDIENTE'
      };
    }
    
    this.isLoading = false;
  }

  volverAInicio(): void {
    this.router.navigate(['/']);
  }

  verPedidos(): void {
    this.router.navigate(['/mis-pedidos']);
  }
} 