import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Product } from '../Product/product.service';

export interface CartItem {
  product: Product;
  quantity: number;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private itemsSubject = new BehaviorSubject<CartItem[]>([]);
  items$: Observable<CartItem[]> = this.itemsSubject.asObservable();

  constructor() { 
    // Cargar carrito desde localStorage si existe
    this.loadCartFromStorage();
    
    // Suscribirse a cambios para guardar en localStorage
    this.items$.subscribe(items => {
      localStorage.setItem('cart', JSON.stringify(items));
    });
  }

  /**
   * Carga el carrito desde localStorage si existe
   */
  private loadCartFromStorage(): void {
    const savedCart = localStorage.getItem('cart');
    if (savedCart) {
      try {
        const parsedCart = JSON.parse(savedCart);
        this.itemsSubject.next(parsedCart);
      } catch (error) {
        console.error('Error al cargar el carrito desde localStorage:', error);
      }
    }
  }

  /**
   * Genera un identificador único para cada producto basado en su ID y peso
   * @param product Producto a identificar
   * @returns String único que identifica al producto
   */
  private getProductUniqueId(product: Product): string {
    // Usamos una combinación de ID (o localId) y peso para identificar cada variante única
    const id = product.id || product.localId || '';
    const kg = product.kg || '';
    return `${id}-${kg}`;
  }

  /**
   * Añade un producto al carrito o incrementa su cantidad si ya existe
   * @param product Producto a añadir
   */
  addToCart(product: Product): void {
    const currentItems = this.itemsSubject.getValue();
    const productUniqueId = this.getProductUniqueId(product);
    
    // Buscar por ID y peso, no solo por nombre
    const existingItemIndex = currentItems.findIndex(item => 
      this.getProductUniqueId(item.product) === productUniqueId
    );
    
    if (existingItemIndex > -1) {
      // El producto ya existe, incrementa la cantidad
      const updatedItems = [...currentItems];
      updatedItems[existingItemIndex].quantity += 1;
      this.itemsSubject.next(updatedItems);
    } else {
      // El producto no existe, añádelo con cantidad 1
      this.itemsSubject.next([...currentItems, { product, quantity: 1 }]);
    }
  }

  /**
   * Elimina un producto del carrito
   * @param productId Identificador único del producto
   */
  removeFromCart(productId: string): void {
    const currentItems = this.itemsSubject.getValue();
    this.itemsSubject.next(
      currentItems.filter(item => this.getProductUniqueId(item.product) !== productId)
    );
  }

  /**
   * Actualiza la cantidad de un producto en el carrito
   * @param productId Identificador único del producto
   * @param quantity Nueva cantidad
   */
  updateQuantity(productId: string, quantity: number): void {
    const currentItems = this.itemsSubject.getValue();
    this.itemsSubject.next(
      currentItems.map(item => 
        this.getProductUniqueId(item.product) === productId 
          ? { ...item, quantity } 
          : item
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
      map(items => items.reduce((total, item) => total + (item.product.priceMinorista * item.quantity), 0))
    );
  }

  /**
   * Vacía el carrito completamente
   */
  clearCart(): void {
    this.itemsSubject.next([]);
  }
}
