import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Product } from '../../Services/Product/product.service';
import { CartService } from '../../Services/Cart/cart.service';
import { UtilsService } from '../../Services/Utils/utils.service';
import { AuthService } from '../../Services/Auth/auth.service';

interface ProductVariant {
  id: string | number;
  localId?: string;
  kg: string;
  priceMinorista: number;
  priceMayorista: number;
  stock: number;
}

interface ProductGroup {
  baseProduct: Product;
  variants: ProductVariant[];
}

@Component({
  selector: 'app-product-card',
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.css',
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class ProductCardComponent implements OnInit {
  @Input() product!: Product;
  @Input() productGroup?: ProductGroup;
  @Input() index: number = 0;

  // Variables para manejar variantes
  hasVariants: boolean = false;
  selectedVariantIndex: number = 0;
  currentPrice: number = 0;
  currentMinoristaPrice: number = 0;
  currentMayoristaPrice: number = 0;
  currentWeight: string = '';
  selectedVariant?: ProductVariant;
  
  // Variable para controlar si se muestra el precio mayorista
  showMayoristaPrice: boolean = false;

  // Calcular el delay de la animación basado en el índice
  get animationDelay(): string {
    return `${this.index * 100}ms`;
  }

  constructor(
    private cartService: CartService,
    public utils: UtilsService,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Verificar si estamos usando el modo de variantes o el modo estándar
    if (this.productGroup && this.productGroup.variants.length > 0) {
      this.hasVariants = true;
      this.product = this.productGroup.baseProduct;
      this.selectVariant(0); // Selecciona la primera variante por defecto
    } else if (this.product) {
      // Modo estándar - uso directo del producto
      this.currentMinoristaPrice = this.product.priceMinorista;
      this.currentMayoristaPrice = this.product.priceMayorista || this.product.priceMinorista;
      this.currentWeight = this.product.kg;
      this.setPriceBasedOnRole();
    }
    
    // Suscribirse a cambios en el usuario para actualizar precios
    this.authService.currentUser$.subscribe(() => {
      this.setPriceBasedOnRole();
    });
  }

  // Selecciona una variante de producto por su índice
  selectVariant(index: number): void {
    if (!this.productGroup || index >= this.productGroup.variants.length) return;
    
    this.selectedVariantIndex = index;
    this.selectedVariant = this.productGroup.variants[index];
    this.currentMinoristaPrice = this.selectedVariant.priceMinorista;
    this.currentMayoristaPrice = this.selectedVariant.priceMayorista || this.selectedVariant.priceMinorista;
    this.currentWeight = this.selectedVariant.kg;
    this.setPriceBasedOnRole();
  }
  
  // Establece el precio a mostrar basado en el rol del usuario
  setPriceBasedOnRole(): void {
    const userRole = this.authService.userRole;
    const userType = this.authService.currentUser?.tipoUsuario;
    
    // Si es una empresa, mostrar precio mayorista
    if (userType === 'EMPRESA') {
      this.showMayoristaPrice = true;
      this.currentPrice = this.currentMayoristaPrice;
    } else {
      // Cliente final muestra precio minorista
      this.showMayoristaPrice = false;
      this.currentPrice = this.currentMinoristaPrice;
    }
  }

  // Navega al detalle del producto, usando el ID de la variante seleccionada si existe
  navigateToDetail(): void {
    if (this.hasVariants && this.selectedVariant) {
      // Si tenemos variantes, usamos el ID de la variante seleccionada
      const variantId = this.selectedVariant.id || this.selectedVariant.localId;
      this.router.navigate(['/product', variantId]);
    } else if (this.product && (this.product.id || this.product.localId)) {
      // Modo estándar
      const productId = this.product.id || this.product.localId;
      this.router.navigate(['/product', productId]);
    }
  }

  // Añade al carrito el producto con la variante seleccionada
  addToCart(event?: MouseEvent): void {
    if (event) {
      // Detiene la propagación del evento para que no se active el evento clic de la tarjeta
      event.stopPropagation();
    }

    if (this.hasVariants && this.selectedVariant && this.product) {
      // Creamos un producto completo con los datos de la variante seleccionada
      const productToAdd: Product = {
        ...this.product,
        id: this.selectedVariant.id,
        localId: this.selectedVariant.localId,
        kg: this.selectedVariant.kg,
        priceMinorista: this.selectedVariant.priceMinorista,
        priceMayorista: this.selectedVariant.priceMayorista,
        stock: this.selectedVariant.stock
      };
      this.cartService.addToCart(productToAdd);
    } else {
      // Modo estándar
      this.cartService.addToCart(this.product);
    }
  }

  getAnimalTypeLabel(animalType: string): string {
    switch(animalType.toUpperCase()) {
      case 'PERROS': return 'Perros';
      case 'GATOS': return 'Gatos';
      case 'GRANJA': return 'Granja';
      default: return animalType;
    }
  }
}
