import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProductService, Product } from '../../Services/Product/product.service';
import { UtilsService } from '../../Services/Utils/utils.service';
import { CartService } from '../../Services/Cart/cart.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-product-detail',
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.css',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule]
})
export class ProductDetailComponent implements OnInit {
  product: Product | null = null;
  isLoading = true;
  errorMessage = '';
  quantity = 1;
  successMessage = '';
  showSuccessMessage = false;
  
  // Acordeón para secciones colapsables
  accordionState = {
    devoluciones: false,
    envios: false,
    retiro: false
  };
  
  // Exponemos Math para poder utilizarlo en la plantilla
  Math = Math;
  parseFloat = parseFloat;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService,
    public utils: UtilsService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const productId = params['id'];
      if (productId) {
        this.loadProduct(productId);
      } else {
        this.router.navigate(['/']);
      }
    });
  }

  loadProduct(id: string): void {
    // Verificar que el id sea válido antes de hacer la llamada a la API
    if (!id || id === 'undefined' || id === 'null') {
      this.errorMessage = 'Error: ID de producto no válido';
      this.isLoading = false;
      console.error('Intento de cargar producto con ID inválido:', id);
      return;
    }

    console.log('Intentando cargar producto con ID:', id);
    this.isLoading = true;
    
    this.productService.getProductById(id).subscribe({
      next: (product) => {
        console.log('Producto cargado exitosamente:', product);
        this.product = product;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al cargar el producto:', error);
        this.errorMessage = 'Error al cargar el producto: ' + (error.message || 'Producto no encontrado');
        this.isLoading = false;
      }
    });
  }

  incrementQuantity(): void {
    if (this.product && this.quantity < this.product.stock) {
      this.quantity++;
    }
  }

  decrementQuantity(): void {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }

  addToCart(): void {
    if (this.product) {
      // Agregar el producto al carrito la cantidad de veces seleccionada
      for (let i = 0; i < this.quantity; i++) {
        this.cartService.addToCart(this.product);
      }
      
      // Mostrar mensaje de éxito
      this.successMessage = `Se ${this.quantity > 1 ? 'agregaron' : 'agregó'} ${this.quantity} ${this.quantity > 1 ? 'unidades' : 'unidad'} de ${this.product.fullName} al carrito`;
      this.showSuccessMessage = true;
      
      // Ocultar mensaje después de 3 segundos
      setTimeout(() => {
        this.showSuccessMessage = false;
      }, 3000);
    }
  }
  
  // Alternar el estado de un acordeón
  toggleAccordion(section: 'devoluciones' | 'envios' | 'retiro'): void {
    this.accordionState[section] = !this.accordionState[section];
  }
  
  // Obtener fecha de entrega estimada
  getDeliveryDate(): string {
    const hoy = new Date();
    // Agregar entre 3 y 5 días para la entrega
    const diasEntrega = 3 + Math.floor(Math.random() * 3);
    const fechaEntrega = new Date(hoy);
    fechaEntrega.setDate(hoy.getDate() + diasEntrega);
    
    // Formatear la fecha al estilo español
    const diasSemana = ['domingo', 'lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado'];
    const meses = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];
    
    const diaSemana = diasSemana[fechaEntrega.getDay()];
    const diaMes = fechaEntrega.getDate();
    const mes = meses[fechaEntrega.getMonth()];
    
    return `${diaSemana} ${diaMes} de ${mes}`;
  }
} 