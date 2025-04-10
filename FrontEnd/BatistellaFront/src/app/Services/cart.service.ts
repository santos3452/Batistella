import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Product } from '../Models/product';

export interface CartItem extends Product {
  quantity: number;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private itemsSubject = new BehaviorSubject<CartItem[]>([]);
  items$: Observable<CartItem[]> = this.itemsSubject.asObservable();

  constructor() { }

  /**
   * Añade un producto al carrito o incrementa su cantidad si ya existe
   * @param product Producto a añadir
   */
  addToCart(product: Product): void {
    const currentItems = this.itemsSubject.getValue();
    const existingItemIndex = currentItems.findIndex(item => item.id === product.id);
    
    if (existingItemIndex > -1) {
      // El producto ya existe, incrementa la cantidad
      const updatedItems = [...currentItems];
      updatedItems[existingItemIndex].quantity += 1;
      this.itemsSubject.next(updatedItems);
    } else {
      // El producto no existe, añádelo con cantidad 1
      this.itemsSubject.next([...currentItems, { ...product, quantity: 1 }]);
    }
  }

  /**
   * Elimina un producto del carrito
   * @param productId ID del producto a eliminar
   */
  removeFromCart(productId: number): void {
    const currentItems = this.itemsSubject.getValue();
    this.itemsSubject.next(currentItems.filter(item => item.id !== productId));
  }

  /**
   * Actualiza la cantidad de un producto en el carrito
   * @param productId ID del producto
   * @param quantity Nueva cantidad
   */
  updateQuantity(productId: number, quantity: number): void {
    const currentItems = this.itemsSubject.getValue();
    this.itemsSubject.next(
      currentItems.map(item => 
        item.id === productId ? { ...item, quantity } : item
      )
    );
  }

  /**
   * Obtiene el número total de items en el carrito
   */
  get totalItems$(): Observable<number> {
    return this.items$.pipe(
      map(items => items.reduce((total, item) => total + item.quantity, 0))
    );
  }

  /**
   * Obtiene el precio total del carrito
   */
  get totalPrice$(): Observable<number> {
    return this.items$.pipe(
      map(items => items.reduce((total, item) => total + (item.price * item.quantity), 0))
    );
  }
}
