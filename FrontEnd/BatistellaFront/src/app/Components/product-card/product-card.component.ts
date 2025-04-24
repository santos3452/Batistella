import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
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
    public utils: UtilsService
  ) {}

  addToCart(): void {
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
