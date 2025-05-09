import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { UtilsService } from '../../../Services/Utils/utils.service';
import { AuthService } from '../../../Services/Auth/auth.service';

interface DetallePedido {
  id: number;
  productoId: number;
  nombreProducto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

interface Pedido {
  id: number;
  codigoPedido: string;
  usuarioId: number;
  nombreCompletoUsuario: string;
  fechaPedido: string;
  estado: string;
  total: number;
  productos: DetallePedido[];
  createdAt: string;
  updatedAt: string;
}

@Component({
  selector: 'app-admin-pedidos',
  templateUrl: './admin-pedidos.component.html',
  styleUrls: ['./admin-pedidos.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule]
})
export class AdminPedidosComponent implements OnInit {
  pedidos: Pedido[] = [];
  filteredPedidos: Pedido[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  
  // Filtros
  filterCodigo: string = '';
  filterNombreUsuario: string = '';
  filterEstado: string = '';
  
  // Opciones de filtros
  estados: string[] = ['PENDIENTE', 'EN_PROCESO', 'ENVIADO', 'ENTREGADO', 'CANCELADO'];

  // Modal de detalles
  showDetailsModal = false;
  selectedPedido: Pedido | null = null;

  // Modal de actualización de estado
  showUpdateStatusModal = false;
  nuevoEstado: string = '';

  constructor(
    private http: HttpClient,
    public utils: UtilsService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadPedidos();
  }

  loadPedidos(): void {
    this.isLoading = true;
    this.errorMessage = null;

    const token = this.authService.userToken;
    if (!token) {
      this.errorMessage = 'No hay token de autenticación disponible';
      this.isLoading = false;
      return;
    }

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    this.http.get<Pedido[]>(`${environment.pedidosUrl}/todosLosPedidos`, { headers })
      .subscribe({
        next: (pedidos) => {
          this.pedidos = pedidos;
          this.filteredPedidos = [...pedidos];
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error al cargar pedidos', error);
          this.errorMessage = 'Ocurrió un error al cargar los pedidos. Por favor, intenta de nuevo más tarde.';
          this.isLoading = false;
        }
      });
  }

  applyFilters(): void {
    this.filteredPedidos = this.pedidos.filter(pedido => {
      // Filtro por código de pedido
      const codigoMatch = !this.filterCodigo || 
        pedido.codigoPedido?.toLowerCase().includes(this.filterCodigo.toLowerCase());
      
      // Filtro por nombre de usuario
      const nombreUsuarioMatch = !this.filterNombreUsuario || 
        pedido.nombreCompletoUsuario?.toLowerCase().includes(this.filterNombreUsuario.toLowerCase());
      
      // Filtro por estado
      const estadoMatch = !this.filterEstado || 
        pedido.estado === this.filterEstado;
      
      return codigoMatch && nombreUsuarioMatch && estadoMatch;
    });
  }

  resetFilters(): void {
    this.filterCodigo = '';
    this.filterNombreUsuario = '';
    this.filterEstado = '';
    this.filteredPedidos = [...this.pedidos];
  }

  openDetailsModal(pedido: Pedido): void {
    this.selectedPedido = pedido;
    this.showDetailsModal = true;
  }

  closeDetailsModal(): void {
    this.showDetailsModal = false;
    this.selectedPedido = null;
  }

  openUpdateStatusModal(pedido: Pedido): void {
    this.selectedPedido = pedido;
    this.nuevoEstado = pedido.estado;
    this.showUpdateStatusModal = true;
  }

  closeUpdateStatusModal(): void {
    this.showUpdateStatusModal = false;
    this.selectedPedido = null;
  }

  updatePedidoStatus(): void {
    if (!this.selectedPedido || !this.selectedPedido.id) return;
    
    this.isLoading = true;
    const token = this.authService.userToken;
    
    if (!token) {
      this.errorMessage = 'No hay token de autenticación disponible';
      this.isLoading = false;
      return;
    }

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    this.http.put(
      `${environment.pedidosUrl}/${this.selectedPedido.id}/estado`, 
      { estado: this.nuevoEstado },
      { headers }
    ).subscribe({
      next: () => {
        this.utils.showToast('success', 'Estado del pedido actualizado con éxito');
        
        // Actualizar el estado en la lista local
        const index = this.pedidos.findIndex(p => p.id === this.selectedPedido?.id);
        if (index !== -1) {
          this.pedidos[index].estado = this.nuevoEstado;
          this.filteredPedidos = [...this.pedidos];
        }
        
        this.closeUpdateStatusModal();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al actualizar estado del pedido', error);
        this.utils.showToast('error', 'Ocurrió un error al actualizar el estado del pedido');
        this.isLoading = false;
      }
    });
  }

  getEstadoClass(estado: string): string {
    switch (estado) {
      case 'PENDIENTE':
        return 'bg-yellow-100 text-yellow-800';
      case 'EN_PROCESO':
        return 'bg-blue-100 text-blue-800';
      case 'ENVIADO':
        return 'bg-purple-100 text-purple-800';
      case 'ENTREGADO':
        return 'bg-green-100 text-green-800';
      case 'CANCELADO':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
} 