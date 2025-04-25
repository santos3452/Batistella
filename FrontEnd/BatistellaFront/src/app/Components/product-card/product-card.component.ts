import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Product } from '../../Services/Product/product.service';
import { CartService } from '../../Services/Cart/cart.service';
import { UtilsService } from '../../Services/Utils/utils.service';

@Component({
  selector: 'app-product-card',
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.css',
  standalone: true,
  imports: [CommonModule]
})
export class ProductCardComponent {
  @Input() product!: Product;
  @Input() index: number = 0;

  // Calcular el delay de la animación basado en el índice
  get animationDelay(): string {
    return `${this.index * 100}ms`;
  }

  constructor(
    private cartService: CartService,
    public utils: UtilsService,
    private router: Router
  ) {}

  // Navega al detalle del producto
  navigateToDetail(): void {
    if (this.product && (this.product.id || this.product.localId)) {
      const productId = this.product.id || this.product.localId;
      this.router.navigate(['/product', productId]);
    }
  }

  // Modifica la función para prevenir que el clic en el botón navegue a la página de detalle
  addToCart(event?: MouseEvent): void {
    if (event) {
      // Detiene la propagación del evento para que no se active el evento clic de la tarjeta
      event.stopPropagation();
    }
    this.cartService.addToCart(this.product);
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
