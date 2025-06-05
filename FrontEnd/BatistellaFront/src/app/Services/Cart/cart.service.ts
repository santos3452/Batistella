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
  
  // Nuevo BehaviorSubject para controlar la visibilidad del carrito
  private cartVisibleSubject = new BehaviorSubject<boolean>(false);
  cartVisible$ = this.cartVisibleSubject.asObservable();

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
   * Obtiene el precio total del carrito considerando el tipo de usuario
   * @param userType Tipo de usuario ('EMPRESA' o null/undefined para cliente final)
   */
  getTotalPriceByUserType$(userType: string | null | undefined): Observable<number> {
    return this.items$.pipe(
      map(items => items.reduce((total, item) => {
        const price = this.getProductPrice(item.product, userType);
        return total + (price * item.quantity);
      }, 0))
    );
  }

  /**
   * Obtiene el precio correcto de un producto según el tipo de usuario
   * @param product Producto del cual obtener el precio
   * @param userType Tipo de usuario ('EMPRESA' o null/undefined para cliente final)
   * @returns Precio correcto según el tipo de usuario
   */
  getProductPrice(product: Product, userType: string | null | undefined): number {
    if (userType === 'EMPRESA') {
      return product.priceMayorista || product.priceMinorista;
    }
    return product.priceMinorista;
  }

  /**
   * Valida si una empresa puede proceder al checkout (mínimo 10 unidades)
   * @param userRole Rol del usuario actual
   * @returns Observable<boolean> true si puede proceder, false si no
   */
  canCompanyProceedToCheckout$(userRole: string | null): Observable<boolean> {
    return this.totalItems$.pipe(
      map(totalItems => {
        // Si es empresa, debe tener al menos 10 unidades
        if (userRole === 'ROLE_EMPRESA') {
          return totalItems >= 10;
        }
        // Para otros usuarios, no hay restricción
        return true;
      })
    );
  }

  /**
   * Obtiene el número total de unidades actual en el carrito
   */
  getCurrentTotalItems(): number {
    const currentItems = this.itemsSubject.getValue();
    return currentItems.reduce((total, item) => total + item.quantity, 0);
  }

  /**
   * Verifica si una empresa puede proceder al checkout (versión síncrona)
   * @param userRole Rol del usuario actual
   * @returns boolean true si puede proceder, false si no
   */
  canCompanyProceedToCheckout(userRole: string | null): boolean {
    if (userRole === 'ROLE_EMPRESA') {
      return this.getCurrentTotalItems() >= 10;
    }
    return true;
  }

  /**
   * Vacía el carrito completamente
   */
  clearCart(): void {
    this.itemsSubject.next([]);
  }

  /**
   * Abre el carrito desplegable
   */
  openCart(): void {
    this.cartVisibleSubject.next(true);
  }

  /**
   * Cierra el carrito desplegable
   */
  closeCart(): void {
    this.cartVisibleSubject.next(false);
  }

  /**
   * Alterna el estado de visibilidad del carrito
   */
  toggleCart(): void {
    this.cartVisibleSubject.next(!this.cartVisibleSubject.getValue());
  }
}
