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
  isBaseProductSelected: boolean = true; // Nueva propiedad para controlar si el producto base está seleccionado
  currentPrice: number = 0;
  currentMinoristaPrice: number = 0;
  currentMayoristaPrice: number = 0;
  currentWeight: string = '';
  selectedVariant?: ProductVariant;
  
  // Variable para controlar si se muestra el precio mayorista
  showMayoristaPrice: boolean = false;

  // Variables para promociones de kilos gratis
  hasPromotion: boolean = false;
  freeKilos: string = '';
  displayWeight: string = '';

  // Calcular el delay de la animación basado en el índice
  get animationDelay(): string {
    return `${this.index * 100}ms`;
  }

  // Obtener el nombre a mostrar en la tarjeta del producto
  get displayName(): string {
    if (!this.product) return '';
    
    // Para productos de granja, usar el campo nombre
    if (this.product.animalType === 'GRANJA') {
      // Si hay nombre, usarlo junto con la categoría
      if (this.product.nombre) {
        return `${this.product.nombre}${this.product.categoriaGranja ? ' - ' + this.product.categoriaGranja : ''}`;
      }
    }
    
    // Para productos de mascotas, usar el fullName
    return this.product.fullName || '';
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
      // Inicializar con el producto base seleccionado
      this.isBaseProductSelected = true;
      this.selectBaseProduct();
    } else if (this.product) {
      // Modo estándar - uso directo del producto
      this.currentMinoristaPrice = this.product.priceMinorista;
      this.currentMayoristaPrice = this.product.priceMayorista || this.product.priceMinorista;
      this.currentWeight = this.product.kg;
      this.updateWeightDisplay(this.product.kg);
      this.setPriceBasedOnRole();
    }
    
    // Suscribirse a cambios en el usuario para actualizar precios
    this.authService.currentUser$.subscribe(() => {
      this.setPriceBasedOnRole();
    });
  }

  // Método para detectar y extraer los kilos gratis de una cadena de peso
  extractFreeKilos(weightString: string): { displayWeight: string, freeKilos: string, hasPromotion: boolean } {
    if (!weightString) {
      return { displayWeight: '', freeKilos: '', hasPromotion: false };
    }
    
    // Verificar si el formato es del tipo "XX+YYkg"
    const promotionRegex = /^(\d+)\+(\d+)kg$/i;
    const match = weightString.match(promotionRegex);
    
    if (match) {
      // Extraer los kilos base y los kilos de promoción
      const baseKilos = match[1];
      const extraKilos = match[2];
      
      return {
        displayWeight: `${baseKilos}kg`,
        freeKilos: `${extraKilos}kg`,
        hasPromotion: true
      };
    }
    
    // Si no es una promoción, devolver el peso original
    return {
      displayWeight: weightString,
      freeKilos: '',
      hasPromotion: false
    };
  }

  // Método para actualizar el peso a mostrar
  updateWeightDisplay(weightString: string): void {
    const { displayWeight, freeKilos, hasPromotion } = this.extractFreeKilos(weightString);
    this.displayWeight = displayWeight;
    this.freeKilos = freeKilos;
    this.hasPromotion = hasPromotion;
  }

  // Nuevo método para seleccionar el producto base
  selectBaseProduct(): void {
    if (!this.productGroup) return;
    
    this.isBaseProductSelected = true;
    this.selectedVariantIndex = -1; // Reset del índice de variante
    
    const baseProduct = this.productGroup.baseProduct;
    this.currentMinoristaPrice = baseProduct.priceMinorista;
    this.currentMayoristaPrice = baseProduct.priceMayorista || baseProduct.priceMinorista;
    this.currentWeight = baseProduct.kg;
    this.updateWeightDisplay(baseProduct.kg);
    this.selectedVariant = {
      id: baseProduct.id as string | number,
      localId: baseProduct.localId,
      kg: baseProduct.kg,
      priceMinorista: baseProduct.priceMinorista,
      priceMayorista: baseProduct.priceMayorista || baseProduct.priceMinorista,
      stock: baseProduct.stock
    };
    this.setPriceBasedOnRole();
  }

  // Selecciona una variante de producto por su índice
  selectVariant(index: number): void {
    if (!this.productGroup || index >= this.productGroup.variants.length) return;
    
    this.isBaseProductSelected = false; // Indicar que el producto base ya no está seleccionado
    this.selectedVariantIndex = index;
    this.selectedVariant = this.productGroup.variants[index];
    this.currentMinoristaPrice = this.selectedVariant.priceMinorista;
    this.currentMayoristaPrice = this.selectedVariant.priceMayorista || this.selectedVariant.priceMinorista;
    this.currentWeight = this.selectedVariant.kg;
    this.updateWeightDisplay(this.selectedVariant.kg);
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
      // Verificar si es el producto base o una variante
      if (this.isBaseProductSelected) {
        // Usar directamente el producto base
        this.cartService.addToCart(this.productGroup!.baseProduct);
      } else {
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
      }
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
