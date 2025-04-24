import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CartService, CartItem } from '../../Services/Cart/cart.service';
import { UtilsService } from '../../Services/Utils/utils.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-cart-dropdown',
  templateUrl: './cart-dropdown.component.html',
  styleUrl: './cart-dropdown.component.css',
  standalone: true,
  imports: [CommonModule]
})
export class CartDropdownComponent implements OnInit, OnDestroy {
  items: CartItem[] = [];
  totalItems = 0;
  totalPrice = 0;
  private subscription: Subscription = new Subscription();

  constructor(
    private cartService: CartService,
    public utils: UtilsService
  ) {}

  ngOnInit(): void {
    // Suscribirse a los items del carrito
    this.subscription.add(
      this.cartService.items$.subscribe(items => {
        this.items = items;
      })
    );

    // Suscribirse al total de items
    this.subscription.add(
      this.cartService.totalItems$.subscribe(total => {
        this.totalItems = total;
      })
    );

    // Suscribirse al precio total
    this.subscription.add(
      this.cartService.totalPrice$.subscribe(price => {
        this.totalPrice = price;
      })
    );
  }

  ngOnDestroy(): void {
    // Limpiar suscripciones al destruir el componente
    this.subscription.unsubscribe();
  }

  removeItem(productName: string): void {
    this.cartService.removeFromCart(productName);
  }

  updateQuantity(productName: string, quantity: number): void {
    this.cartService.updateQuantity(productName, quantity);
  }

  incrementQuantity(item: CartItem): void {
    this.updateQuantity(item.product.fullName, item.quantity + 1);
  }

  decrementQuantity(item: CartItem): void {
    if (item.quantity > 1) {
      this.updateQuantity(item.product.fullName, item.quantity - 1);
    }
  }
}
