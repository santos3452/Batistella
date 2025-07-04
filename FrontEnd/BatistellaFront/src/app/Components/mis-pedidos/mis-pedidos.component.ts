import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { PedidosService } from '../../Services/Pedidos/pedidos.service';
import { AuthService } from '../../Services/Auth/auth.service';
import { ProductService, Product } from '../../Services/Product/product.service';
import { Pedido, ProductoPedido } from '../../Models/pedido';
import { of, forkJoin } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { CartService } from '../../Services/Cart/cart.service';
import { PedidosVistosService } from '../../Services/Pedidos/pedidos-vistos.service';
import { PrintService } from '../../Services/Print/print.service';

interface ProductoPedidoConImagen extends ProductoPedido {
  imagenUrl?: string;
}

interface PedidoConImagenes extends Omit<Pedido, 'productos'> {
  productos: ProductoPedidoConImagen[];
}

@Component({
  selector: 'app-mis-pedidos',
  templateUrl: './mis-pedidos.component.html',
  styleUrl: './mis-pedidos.component.css',
  standalone: true,
  imports: [CommonModule, RouterLink]
})
export class MisPedidosComponent implements OnInit {
  pedidos: PedidoConImagenes[] = [];
  cargando: boolean = true;
  error: string | null = null;
  pedidoExpandido: number | null = null;
  imagenPorDefecto: string = 'assets/Images/BatistellaLogo.jpg';

  // Variables de paginación
  page = 1;
  pageSize = 8;
  totalPages = 0;
  Math = Math; // Para usar en la plantilla

  // Datos de muestra para desarrollo
  private pedidosMuestra: Pedido[] = [
    {
      "id": 1,
      "usuarioId": 1,
      "fechaPedido": "05/02/2025 17:56:57",
      "estado": "PENDIENTE",
      "total": 25000,
      "productos": [
        {
          "id": 1,
          "productoId": 1,
          "nombreProducto": "Alimento balanceado TopNutrition para perros adultos de raza grande",
          "cantidad": 1,
          "precioUnitario": 25000,
          "subtotal": 25000
        }
      ],
      "createdAt": "05/02/2025 17:56:57",
      "updatedAt": "05/02/2025 17:56:57"
    },
    {
      "id": 2,
      "usuarioId": 1,
      "fechaPedido": "05/02/2025 17:57:34",
      "estado": "PENDIENTE",
      "total": 50000,
      "productos": [
        {
          "id": 2,
          "productoId": 1,
          "nombreProducto": "Alimento balanceado TopNutrition para perros adultos de raza grande",
          "cantidad": 1,
          "precioUnitario": 25000,
          "subtotal": 25000
        },
        {
          "id": 3,
          "productoId": 2,
          "nombreProducto": "Alimento balanceado KenL para perros adultos de raza grande",
          "cantidad": 1,
          "precioUnitario": 25000,
          "subtotal": 25000
        }
      ],
      "createdAt": "05/02/2025 17:57:34",
      "updatedAt": "05/02/2025 17:57:34"
    }
  ];

  constructor(
    private pedidosService: PedidosService,
    private authService: AuthService,
    private productService: ProductService,
    private cartService: CartService,
    private pedidosVistosService: PedidosVistosService,
    private printService: PrintService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Verificar si el token sigue siendo válido
    if (!this.authService.isAuthenticated()) {
      console.log('Token expirado al acceder a mis pedidos');
      this.router.navigate(['/login']);
      return;
    }
    
    this.cargarPedidos();
  }

  cargarPedidos(): void {
    this.cargando = true;
    this.error = null;

    // Obtener el ID del usuario actual
    this.authService.currentUser$.subscribe(user => {
      if (user && user.id) {
        // Decodificar el token para obtener el ID del usuario
        const token = localStorage.getItem('token');
        if (token) {
          try {
            const tokenPayload = JSON.parse(atob(token.split('.')[1]));
            const userIdFromToken = tokenPayload.userId;
            
            // Usar el ID del token en lugar del ID del usuario almacenado
            this.pedidosService.getPedidosUsuario(userIdFromToken).subscribe({
              next: (pedidos) => {
                this.procesarPedidosConImagenes(pedidos);
              },
              error: (err) => {
                console.error('Error al cargar pedidos:', err);
                this.error = 'No se pudieron cargar tus pedidos. Por favor, intenta nuevamente más tarde.';
                this.cargando = false;
              }
            });
          } catch (error) {
            console.error('Error al decodificar el token:', error);
            this.error = 'Error al verificar tu sesión. Por favor, intenta nuevamente.';
            this.cargando = false;
          }
        } else {
          this.error = 'No se encontró el token de autenticación. Por favor, inicia sesión nuevamente.';
          this.cargando = false;
        }
      } else {
        this.error = 'Debes iniciar sesión para ver tus pedidos.';
        this.cargando = false;
      }
    });

    // Código de simulación para desarrollo (comentar en producción)
    /*
    setTimeout(() => {
      this.procesarPedidosConImagenes(this.pedidosMuestra);
    }, 1000); // Simulamos un segundo de carga
    */
  }

  // Método OPTIMIZADO para procesar los pedidos y obtener las imágenes de los productos
  procesarPedidosConImagenes(pedidos: Pedido[]): void {
    console.log('🔄 Procesando pedidos con imágenes - MÉTODO OPTIMIZADO');
    
    // Si no hay pedidos, simplemente terminamos la carga con un array vacío
    if (!pedidos || pedidos.length === 0) {
      this.pedidos = [];
      this.cargando = false;
      return;
    }

    // OPTIMIZACIÓN: Una sola llamada a getAllProducts en lugar de múltiples getProductById
    this.productService.getProducts().subscribe({
      next: (todosLosProductos) => {
        console.log('✅ Productos cargados:', todosLosProductos.length);
        
        // Crear un Map para búsqueda rápida por ID
        const productosMap = new Map<number, any>();
        todosLosProductos.forEach(producto => {
          if (producto.id) {
            productosMap.set(Number(producto.id), producto);
          }
        });

        // Transformar los pedidos para incluir URLs de imágenes
        this.pedidos = pedidos.map(pedido => {
          const productosConImagen = pedido.productos.map(producto => {
            const productoCompleto = productosMap.get(producto.productoId);
            
            return {
              ...producto,
              imagenUrl: productoCompleto?.imageUrl || this.imagenPorDefecto,
              // Si el producto tiene kg, lo añadimos al producto del pedido
              kg: productoCompleto?.kg ? parseFloat(productoCompleto.kg) : producto.kg
            };
          });

          return {
            ...pedido,
            productos: productosConImagen
          };
        });

        // Configurar la paginación después de cargar los pedidos
        this.totalPages = Math.ceil(this.pedidos.length / this.pageSize);
        this.page = 1; // Resetear a la primera página
        this.cargando = false;
        
        console.log('✅ Pedidos procesados exitosamente:', this.pedidos.length);
      },
      error: (err) => {
        console.error('❌ Error al obtener productos:', err);
        // En caso de error, mostramos los pedidos sin imágenes
        this.pedidos = pedidos.map(pedido => ({
          ...pedido,
          productos: pedido.productos.map(producto => ({
            ...producto,
            imagenUrl: this.imagenPorDefecto
          }))
        })) as PedidoConImagenes[];
        
        this.totalPages = Math.ceil(this.pedidos.length / this.pageSize);
        this.page = 1;
        this.cargando = false;
      }
    });
  }

  toggleDetallePedido(pedidoId: number): void {
    if (this.pedidoExpandido === pedidoId) {
      this.pedidoExpandido = null;
    } else {
      this.pedidoExpandido = pedidoId;
      // Marcar el pedido como visto cuando se expande
      this.pedidosVistosService.marcarComoVisto(pedidoId);
    }
  }

  /**
   * Verifica si un pedido ha sido visto
   * @param pedidoId ID del pedido a verificar
   * @returns true si el pedido ha sido visto, false en caso contrario
   */
  esPedidoVisto(pedidoId: number): boolean {
    return this.pedidosVistosService.esPedidoVisto(pedidoId);
  }

  obtenerEstadoClase(estado: string): string {
    switch (estado.toUpperCase()) {
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

  formatearFecha(fecha: string): string {
    try {
      // Convertir la fecha al formato local
      return new Date(fecha).toLocaleDateString('es-AR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      return fecha; // Si hay un error, devolver la fecha original
    }
  }

  calcularGastoTotal(): number {
    return this.pedidos.reduce((total, pedido) => total + pedido.total, 0);
  }

  // Calcular el subtotal de productos de un pedido
  calcularSubtotalProductos(pedido: PedidoConImagenes): number {
    return pedido.productos.reduce((subtotal, producto) => subtotal + producto.subtotal, 0);
  }

  // Obtener el costo de envío de un pedido
  obtenerCostoEnvio(pedido: PedidoConImagenes): number {
    return pedido.costoEnvio || 0;
  }

  contarPedidosPendientes(): number {
    return this.pedidos.filter(pedido => pedido.estado.toUpperCase() === 'PENDIENTE').length;
  }

  get pagedPedidos(): PedidoConImagenes[] {
    const start = (this.page - 1) * this.pageSize;
    const end = start + this.pageSize;
    return this.pedidos.slice(start, end);
  }

  prevPage(): void {
    if (this.page > 1) {
      this.page--;
    }
  }

  nextPage(): void {
    if (this.page < this.totalPages) {
      this.page++;
    }
  }

  goToPage(n: number): void {
    if (n >= 1 && n <= this.totalPages) {
      this.page = n;
    }
  }



  // Obtiene las páginas visibles para mostrar en la paginación
  getVisiblePages(): number[] {
    const pages: number[] = [];
    
    // Si hay 5 o menos páginas, mostrar todas
    if (this.totalPages <= 5) {
      for (let i = 1; i <= this.totalPages; i++) {
        pages.push(i);
      }
      return pages;
    }
    
    // Lógica para más de 5 páginas
    if (this.page <= 3) {
      // Al inicio: mostrar 1, 2, 3, 4, 5
      for (let i = 1; i <= Math.min(5, this.totalPages); i++) {
        pages.push(i);
      }
    } else if (this.page >= this.totalPages - 2) {
      // Al final: mostrar las últimas 5 páginas
      for (let i = Math.max(1, this.totalPages - 4); i <= this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      // En el medio: mostrar página actual y 2 a cada lado
      for (let i = this.page - 2; i <= this.page + 2; i++) {
        if (i >= 1 && i <= this.totalPages) {
          pages.push(i);
        }
      }
    }
    
    return pages;
  }

  // Transformar un ProductoPedido a Product para poder agregarlo al carrito
  private transformProductoPedidoToProduct(producto: ProductoPedidoConImagen): Product {
    return {
      id: producto.productoId,
      fullName: producto.nombreProducto,
      description: '',
      priceMinorista: producto.precioUnitario,
      priceMayorista: 0,
      stock: 999, // Suponemos que hay stock suficiente
      imageUrl: producto.imagenUrl || this.imagenPorDefecto,
      animalType: '',
      marca: '',
      tipoAlimento: '',
      tipoRaza: '',
      kg: producto.kg?.toString() || '',
      activo: true
    };
  }

  // Método para repetir un pedido (agregar todos los productos al carrito)
  repetirPedido(pedido: PedidoConImagenes): void {
    // Limpiar el carrito actual
    this.cartService.clearCart();
    
    // Agregar cada producto del pedido al carrito
    pedido.productos.forEach(producto => {
      const product = this.transformProductoPedidoToProduct(producto);
      for (let i = 0; i < producto.cantidad; i++) {
        this.cartService.addToCart(product);
      }
    });
    
    // Redirigir al carrito
    this.router.navigate(['/cart']);
  }

  /**
   * Imprime el detalle de un pedido específico en un PDF
   * @param pedido Pedido a imprimir
   */
  imprimirDetallePedido(pedido: PedidoConImagenes): void {
    this.printService.generarPDFDetallePedido(pedido);
  }
} 