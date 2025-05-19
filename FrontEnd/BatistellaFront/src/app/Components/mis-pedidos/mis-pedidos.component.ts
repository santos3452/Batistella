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
    private router: Router
  ) {}

  ngOnInit(): void {
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

  // Método para procesar los pedidos y obtener las imágenes de los productos
  procesarPedidosConImagenes(pedidos: Pedido[]): void {
    // Si no hay pedidos, simplemente terminamos la carga con un array vacío
    if (!pedidos || pedidos.length === 0) {
      this.pedidos = [];
      this.cargando = false;
      return;
    }

    // Crear un Map para almacenar las URLs de imágenes de los productos
    const imagenesProductos = new Map<number, string>();
    
    // Crear observables para obtener las imágenes de los productos
    const solicitudesProductos = pedidos.flatMap(pedido => 
      pedido.productos.map(producto => {
        return this.productService.getProductById(producto.productoId).pipe(
          map(product => {
            // Almacenar la URL de la imagen en el mapa
            imagenesProductos.set(producto.productoId, product.imageUrl);
            
            // Si el producto tiene kg, lo añadimos al producto del pedido
            if (product.kg) {
              producto.kg = parseFloat(product.kg);
            }
            
            return product;
          }),
          catchError(err => {
            console.error(`Error al obtener producto ${producto.productoId}:`, err);
            return of(null);
          })
        );
      })
    );

    // Si no hay solicitudes de productos, procesamos los pedidos sin imágenes
    if (solicitudesProductos.length === 0) {
      this.pedidos = pedidos as PedidoConImagenes[];
      this.cargando = false;
      return;
    }

    // Procesar todas las solicitudes en paralelo
    forkJoin(solicitudesProductos).subscribe({
      next: () => {
        // Transformar los pedidos para incluir URLs de imágenes
        this.pedidos = pedidos.map(pedido => {
          const productosConImagen = pedido.productos.map(producto => {
            return {
              ...producto,
              imagenUrl: imagenesProductos.get(producto.productoId) || this.imagenPorDefecto
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
      },
      error: (err) => {
        console.error('Error al procesar productos:', err);
        // En caso de error, mostramos los pedidos sin imágenes
        this.pedidos = pedidos as PedidoConImagenes[];
        this.cargando = false;
      }
    });
  }

  toggleDetallePedido(pedidoId: number): void {
    if (this.pedidoExpandido === pedidoId) {
      this.pedidoExpandido = null;
    } else {
      this.pedidoExpandido = pedidoId;
    }
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
    if (!fecha) return '';
    const partes = fecha.split(' ');
    if (partes.length >= 1) {
      return partes[0];
    }
    return fecha;
  }

  calcularGastoTotal(): number {
    return this.pedidos.reduce((total, pedido) => total + pedido.total, 0);
  }

  contarPedidosPendientes(): number {
    return this.pedidos.filter(pedido => pedido.estado.toUpperCase() === 'PENDIENTE').length;
  }

  // Método para obtener los pedidos paginados
  get pagedPedidos(): PedidoConImagenes[] {
    const start = (this.page - 1) * this.pageSize;
    return this.pedidos.slice(start, start + this.pageSize);
  }

  // Métodos de paginación
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

  // Determina si un número de página debe mostrarse basado en la página actual
  showPageNumber(pageNum: number): boolean {
    // Si hay 5 o menos páginas, mostrar todas
    if (this.totalPages <= 5) return true;
    
    // Siempre mostrar la primera página
    if (pageNum === 1) return true;
    
    // Para páginas cercanas a la actual
    if (pageNum >= this.page - 1 && pageNum <= this.page + 1) return true;
    
    // No mostrar otras páginas
    return false;
  }

  // Obtiene el número de página que se debe mostrar en cada posición
  getPageNumberToShow(index: number): number {
    // Si hay 5 o menos páginas, mostrar secuencialmente
    if (this.totalPages <= 5) return index + 1;
    
    // Para la primera posición siempre mostrar página 1
    if (index === 0) return 1;
    
    // Si estamos en las primeras páginas
    if (this.page <= 3) {
      return index + 1;
    }
    
    // Si estamos cerca del final
    if (this.page >= this.totalPages - 2) {
      return this.totalPages - 4 + index;
    }
    
    // En medio del rango
    return this.page - 2 + index;
  }

  /**
   * Convierte un ProductoPedido a un objeto Product para añadirlo al carrito
   */
  private transformProductoPedidoToProduct(producto: ProductoPedidoConImagen): Product {
    return {
      id: producto.productoId,
      fullName: producto.nombreProducto,
      priceMinorista: producto.precioUnitario,
      kg: producto.kg ? producto.kg.toString() : '',
      // Valores por defecto requeridos por la interfaz Product
      marca: '',
      tipoAlimento: '',
      tipoRaza: '',
      description: '',
      priceMayorista: 0,
      stock: 0,
      imageUrl: producto.imagenUrl || this.imagenPorDefecto,
      animalType: '',
      activo: true
    };
  }

  /**
   * Repite un pedido anterior añadiendo todos los productos al carrito
   * con las mismas cantidades y precios originales
   */
  repetirPedido(pedido: PedidoConImagenes): void {
    // Primero limpiamos el carrito actual
    this.cartService.clearCart();
    
    // Añadimos cada producto del pedido original al carrito
    pedido.productos.forEach(producto => {
      const productToAdd = this.transformProductoPedidoToProduct(producto);
      
      // Añadimos el producto tantas veces como la cantidad original
      for (let i = 0; i < producto.cantidad; i++) {
        this.cartService.addToCart(productToAdd);
      }
    });
    
    // Mostramos el carrito y navegamos a la página de checkout
    this.cartService.openCart();
    this.router.navigate(['/checkout']);
  }
} 