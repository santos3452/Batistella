import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { UtilsService } from '../../../Services/Utils/utils.service';
import { AuthService } from '../../../Services/Auth/auth.service';
import { PedidosService } from '../../../Services/Pedidos/pedidos.service';
import { Observable } from 'rxjs';

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
  domicilio?: string;
}

interface PedidoResponse {
  content: Pedido[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
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
  filterFecha: string = '';
  
  // Opciones de filtros
  estados: string[] = ['PENDIENTE', 'CONFIRMADO', 'ENTREGADO', 'CANCELADO'];

  // Paginación
  currentPage: number = 1;
  itemsPerPage: number = 12;
  totalItems: number = 0;
  totalPages: number = 0;
  availablePages: number[] = []; // Páginas que tienen datos

  // Modal de detalles
  showDetailsModal = false;
  selectedPedido: Pedido | null = null;

  // Modal de actualización de estado
  showUpdateStatusModal = false;
  nuevoEstado: string = '';

  constructor(
    private http: HttpClient,
    public utils: UtilsService,
    private authService: AuthService,
    private pedidosService: PedidosService
  ) {}

  ngOnInit(): void {
    // Asegurarse de que los valores iniciales están configurados correctamente
    this.currentPage = 1;
    this.itemsPerPage = 12;
    this.loadPedidos();
    this.checkAvailablePages(1, 5); // Verificar las primeras 5 páginas al inicio
  }

  loadPedidos(): void {
    this.isLoading = true;
    this.errorMessage = null;

    // Obtener token directamente del localStorage para verificar
    const tokenFromStorage = localStorage.getItem('token');
    console.log('Token disponible en localStorage:', !!tokenFromStorage);

    // Usar el token del servicio de autenticación
    const token = this.authService.userToken;
    if (!token) {
      console.error('No hay token de autenticación disponible');
      this.errorMessage = 'No hay token de autenticación disponible. Por favor, inicie sesión nuevamente.';
      this.isLoading = false;
      return;
    }

    console.log('Token de autenticación obtenido del servicio:', !!token);

    // Asegurar que los parámetros estén correctamente formateados
    const page = this.currentPage || 1;
    const size = this.itemsPerPage || 12;
    
    // Limpiar espacios en blanco en los filtros
    const codigoPedido = this.filterCodigo?.trim() || '';
    const estado = this.filterEstado?.trim() || '';
    const fecha = this.filterFecha?.trim() || '';

    console.log('Enviando petición con parámetros:', {
      page,
      size,
      codigoPedido,
      estado,
      fecha
    });

    this.pedidosService.getPedidosPaginados(
      page,
      size,
      codigoPedido,
      estado,
      fecha
    ).subscribe({
      next: (response) => {
        console.log('Respuesta recibida en componente:', response);
        
        // Verificar si la respuesta es un array o un objeto con formato esperado
        if (response) {
          // Si es un array directo, convertirlo a objeto paginado (como respaldo)
          if (Array.isArray(response)) {
            console.log('Respuesta recibida como array, convirtiendo a formato paginado');
            this.pedidos = response;
            this.filteredPedidos = this.pedidos;
            this.totalItems = response.length;
            this.totalPages = Math.max(1, Math.ceil(response.length / this.itemsPerPage));
            
            // Si hay datos, añadir la página actual a las disponibles
            if (response.length > 0 && !this.availablePages.includes(this.currentPage)) {
              this.availablePages.push(this.currentPage);
              this.availablePages.sort((a, b) => a - b);
            }
            
            console.log(`Calculado totalPages=${this.totalPages} basado en totalItems=${this.totalItems} e itemsPerPage=${this.itemsPerPage}`);
          } 
          // Si es el objeto PedidoResponse esperado
          else if (response.content) {
            this.pedidos = response.content || [];
            this.filteredPedidos = this.pedidos;
            this.totalItems = response.totalElements || 0;
            this.totalPages = response.totalPages || Math.max(1, Math.ceil(this.totalItems / this.itemsPerPage));
            
            // Si hay datos, añadir la página actual a las disponibles
            if (this.pedidos.length > 0 && !this.availablePages.includes(this.currentPage)) {
              this.availablePages.push(this.currentPage);
              this.availablePages.sort((a, b) => a - b);
            }
            
            console.log(`Asignado totalPages=${this.totalPages} desde respuesta o calculado desde totalItems=${this.totalItems}`);
          } 
          else {
            console.error('Formato de respuesta no reconocido:', response);
            this.pedidos = [];
            this.filteredPedidos = [];
            this.totalItems = 0;
            this.totalPages = 0;
          }
          
          // Aplicar filtro por nombre de usuario si existe
          if (this.filterNombreUsuario && this.pedidos.length > 0) {
            this.filteredPedidos = this.pedidos.filter(pedido => 
              pedido.nombreCompletoUsuario?.toLowerCase().includes(this.filterNombreUsuario.toLowerCase())
            );
          }
        } else {
          this.pedidos = [];
          this.filteredPedidos = [];
          this.totalItems = 0;
          this.totalPages = 0;
        }
        
        console.log(`Estado final después de procesar respuesta: totalItems=${this.totalItems}, totalPages=${this.totalPages}, currentPage=${this.currentPage}`);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al cargar pedidos', error);
        
        // Mensaje de error más específico según el tipo de error
        if (error.status === 401 || error.status === 403) {
          this.errorMessage = 'No tiene permisos para acceder a esta información. Por favor, inicie sesión nuevamente.';
        } else if (error.status === 0) {
          this.errorMessage = 'No se pudo conectar al servidor. Verifique su conexión a internet.';
        } else {
          this.errorMessage = 'Ocurrió un error al cargar los pedidos. Por favor, intenta de nuevo más tarde.';
        }
        
        this.pedidos = [];
        this.filteredPedidos = [];
        this.totalItems = 0;
        this.totalPages = 0;
        this.isLoading = false;
      }
    });
  }

  applyFilters(): void {
    // Limpiar páginas disponibles ya que los filtros pueden cambiar los resultados
    this.availablePages = [];
    
    // Para filtro por nombre de usuario y para el resto de filtros
    // que requieren nueva consulta al servidor
    this.currentPage = 1; // Volver a la primera página
    this.loadPedidos();
    
    // Verificar las primeras 5 páginas con los nuevos filtros
    this.checkAvailablePages(1, 5);
  }

  resetFilters(): void {
    this.filterCodigo = '';
    this.filterNombreUsuario = '';
    this.filterEstado = '';
    this.filterFecha = '';
    
    // Limpiar páginas disponibles ya que estamos reiniciando los filtros
    this.availablePages = [];
    
    this.currentPage = 1;
    this.loadPedidos();
    
    // Verificar las primeras 5 páginas sin filtros
    this.checkAvailablePages(1, 5);
  }

  changePage(page: number): void {
    console.log(`Intentando cambiar a la página ${page}. Página actual: ${this.currentPage}`);
    
    // Convertir a números para evitar comparaciones de tipos diferentes
    const pageNum = Number(page);
    const currentPageNum = Number(this.currentPage);
    
    if (isNaN(pageNum) || pageNum < 1 || pageNum === currentPageNum) {
      console.log(`Cambio de página cancelado. Razón: ${
        isNaN(pageNum) ? 'número de página no válido' : 
        pageNum < 1 ? 'página menor a 1' : 
        'es la misma página actual'}`);
      return;
    }
    
    this.currentPage = pageNum;
    console.log(`Página cambiada a ${this.currentPage}. Cargando datos...`);
    this.loadPedidos();
    
    // Si estamos en la última página verificada, cargar las siguientes 5
    const maxCheckedPage = Math.max(...this.availablePages, 0);
    if (this.currentPage >= maxCheckedPage - 1) {
      this.checkAvailablePages(maxCheckedPage + 1, 5);
    }
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
    
    this.pedidosService.actualizarEstadoPedido(this.selectedPedido.id, this.nuevoEstado)
      .subscribe({
        next: (response) => {
          this.utils.showToast('success', 'Estado del pedido actualizado con éxito');
          
          // Actualizar el estado en la lista local
          const index = this.pedidos.findIndex(p => p.id === this.selectedPedido?.id);
          if (index !== -1) {
            this.pedidos[index].estado = this.nuevoEstado;
            // Actualizar también la fecha de modificación
            this.pedidos[index].updatedAt = new Date().toLocaleString('es-AR');
            this.applyFilters(); // Aplicar filtros para actualizar la vista
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
      case 'CONFIRMADO':
        return 'bg-blue-100 text-blue-800';
      case 'ENTREGADO':
        return 'bg-green-100 text-green-800';
      case 'CANCELADO':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  /**
   * Genera un array con las páginas alrededor de la página actual para mostrar en la paginación
   * @returns Array de números de página
   */
  getPagesAroundCurrent(): number[] {
    if (!this.totalPages) return [];
    
    if (this.totalPages <= 7) {
      // Si hay pocas páginas, devolver un array con todas las páginas (excepto la primera y última que se muestran siempre)
      const pages: number[] = [];
      for (let i = 2; i < this.totalPages; i++) {
        pages.push(i);
      }
      return pages;
    }
    
    const pages: number[] = [];
    const pagesToShow = 3; // Número de páginas para mostrar alrededor de la actual
    
    // Añadir páginas alrededor de la actual
    for (let i = Math.max(2, this.currentPage - Math.floor(pagesToShow / 2)); 
         i <= Math.min(this.totalPages - 1, this.currentPage + Math.floor(pagesToShow / 2)); 
         i++) {
      pages.push(i);
    }
    
    return pages;
  }

  /**
   * Verifica qué páginas tienen datos haciendo múltiples consultas en paralelo
   * @param startPage Página inicial para verificar
   * @param count Número de páginas a verificar
   */
  checkAvailablePages(startPage: number, count: number): void {
    console.log(`Verificando disponibilidad de páginas de ${startPage} a ${startPage + count - 1}`);
    
    // Limpiar espacios en blanco en los filtros
    const codigoPedido = this.filterCodigo?.trim() || '';
    const estado = this.filterEstado?.trim() || '';
    const fecha = this.filterFecha?.trim() || '';
    
    // Crear un array de consultas para las páginas a verificar
    const pageQueries: Array<Observable<any>> = [];
    
    for (let p = startPage; p < startPage + count; p++) {
      // Solo consultar páginas que no estén ya en availablePages
      if (!this.availablePages.includes(p)) {
        pageQueries.push(
          this.pedidosService.getPedidosPaginados(
            p,
            this.itemsPerPage,
            codigoPedido,
            estado,
            fecha
          )
        );
      }
    }
    
    // Si no hay páginas nuevas para consultar, no hacer nada
    if (pageQueries.length === 0) {
      console.log('No hay nuevas páginas para verificar');
      return;
    }
    
    // Hacer todas las consultas en paralelo
    import('rxjs').then(({ forkJoin, Observable }) => {
      forkJoin(pageQueries)
        .subscribe({
          next: (responses) => {
            console.log(`Recibidas ${responses.length} respuestas para verificación de páginas`);
            
            // Procesar las respuestas
            let pagesFound = 0;
            for (let i = 0; i < responses.length; i++) {
              const page = startPage + i;
              const response = responses[i];
              
              // Si la respuesta tiene datos, añadir la página a las disponibles
              if (response && 
                 ((Array.isArray(response) && response.length > 0) || 
                  (response.content && response.content.length > 0))) {
                
                if (!this.availablePages.includes(page)) {
                  this.availablePages.push(page);
                  pagesFound++;
                }
              }
            }
            
            // Ordenar el array de páginas disponibles
            this.availablePages.sort((a, b) => a - b);
            
            console.log(`Se encontraron ${pagesFound} nuevas páginas con datos. Total: ${this.availablePages.length}`);
            console.log('Páginas disponibles:', this.availablePages);
          },
          error: (error) => {
            console.error('Error al verificar páginas disponibles:', error);
          }
        });
    });
  }
} 