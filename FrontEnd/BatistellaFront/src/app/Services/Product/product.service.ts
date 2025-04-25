import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { map, tap } from 'rxjs/operators';

export interface Product {
  id?: number | string;
  marca: string;
  tipoAlimento: string;
  tipoRaza: string;
  description: string;
  kg: string;
  priceMinorista: number;
  priceMayorista: number;
  stock: number;
  imageUrl: string;
  animalType: string;
  activo: boolean;
  fullName: string;
  // Campo local para manejar productos sin ID
  localId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = 'http://localhost:8083/api/products';
  private productsCache: Product[] = [];
  private lastFetchTime: number = 0;
  private cacheDuration: number = 60000; // 1 minuto en milisegundos

  constructor(private http: HttpClient) { }

  // Genera un ID local basado en características del producto
  private generateLocalId(product: Product): string {
    // Combinar varios campos para crear un identificador único
    const baseString = [
      product.marca,
      product.tipoAlimento,
      product.animalType,
      product.kg,
      product.fullName
    ].filter(Boolean).join('-');
    
    // Crear un hash simple
    let hash = 0;
    for (let i = 0; i < baseString.length; i++) {
      const char = baseString.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash |= 0; // Convertir a entero de 32 bits
    }
    return 'local-' + Math.abs(hash).toString(16);
  }

  // Genera un nombre completo para el producto a partir de sus propiedades
  public generateProductFullName(product: Product): string {
    if (!product) return '';
    
    const parts: string[] = [];
    
    // Añadir marca si existe
    if (product.marca) {
      parts.push(product.marca);
    }
    
    // Añadir tipo de alimento si existe
    if (product.tipoAlimento) {
      parts.push(product.tipoAlimento);
    }
    
    // Añadir tipo de raza solo si no es null
    if (product.tipoRaza) {
      parts.push(product.tipoRaza);
    }
    
    // Unir las partes con espacios
    return parts.join(' ');
  }

  // Procesar productos para añadir IDs locales a los que no tienen ID
  private processProducts(products: Product[]): Product[] {
    return products.map(product => {
      if (!product.id) {
        // Si no tiene ID, generamos uno local y lo asignamos
        product.localId = this.generateLocalId(product);
      }
      
      // Generamos el fullName pero no lo asignamos directamente al objeto
      // para evitar enviarlo al backend
      const fullName = this.generateProductFullName(product);
      
      // Creamos una copia del producto con el fullName para uso local
      const processedProduct = { ...product, fullName };
      
      return processedProduct;
    });
  }

  getProducts(): Observable<Product[]> {
    const now = Date.now();
    
    // Si tenemos productos en caché y no han pasado más de 1 minuto, usamos la caché
    if (this.productsCache.length > 0 && now - this.lastFetchTime < this.cacheDuration) {
      return of(this.productsCache);
    }
    
    // Si no hay caché o está desactualizada, hacemos la petición
    return this.http.get<Product[]>(`${this.apiUrl}/getAllProducts`).pipe(
      map(products => this.processProducts(products)),
      tap(products => {
        this.productsCache = products;
        this.lastFetchTime = Date.now();
      })
    );
  }

  getProductsByCategory(category: string): Observable<Product[]> {
    return this.getProducts().pipe(
      map(products => products.filter(product => 
        product.animalType && product.animalType.toUpperCase() === category.toUpperCase()
      ))
    );
  }

  searchProducts(query: string): Observable<Product[]> {
    if (!query || query.trim() === '') {
      return of([]);
    }

    query = query.toLowerCase().trim();
    
    return this.getProducts().pipe(
      map(products => {
        // Primero, filtramos los productos nulos o undefined
        const productosValidos = products.filter(product => product !== null && product !== undefined);
        
        // Luego aplicamos los criterios de búsqueda de forma más flexible
        return productosValidos.filter(product => {
          // Buscar en diferentes campos del producto con una coincidencia más flexible
          const fullNameMatch = product.fullName && product.fullName.toLowerCase().includes(query);
          const marcaMatch = product.marca && product.marca.toLowerCase().includes(query);
          const descriptionMatch = product.description && product.description.toLowerCase().includes(query);
          const tipoRazaMatch = product.tipoRaza && product.tipoRaza.toLowerCase().includes(query);
          const tipoAlimentoMatch = product.tipoAlimento && product.tipoAlimento.toLowerCase().includes(query);
          const animalTypeMatch = product.animalType && product.animalType.toLowerCase().includes(query);
          
          // Permitir coincidencias parciales con palabras clave como "adulto"
          const esAdulto = query.includes('adulto') && 
                         product.tipoAlimento && 
                         !product.tipoAlimento.toLowerCase().includes('puppy') && 
                         !product.tipoAlimento.toLowerCase().includes('cachorro');
                           
          // Devolver true si coincide con cualquier criterio
          return fullNameMatch || 
                 marcaMatch || 
                 descriptionMatch || 
                 tipoRazaMatch || 
                 tipoAlimentoMatch || 
                 animalTypeMatch || 
                 esAdulto;
        });
      })
    );
  }

  // Obtener un producto basado en un identificador (puede ser ID o localId)
  getProductById(id: string | number): Observable<Product> {
    if (id === undefined || id === null) {
      return throwError(() => new Error('Identificador de producto inválido'));
    }
    
    // Buscar en la caché primero
    return this.getProducts().pipe(
      map(products => {
        // Buscar por ID o localId
        const product = products.find(p => 
          (p.id !== undefined && p.id.toString() === id.toString()) || 
          (p.localId && p.localId === id.toString())
        );
        
        if (!product) {
          throw new Error('Producto no encontrado');
        }
        
        return product;
      })
    );
  }

  saveProductWithImage(formData: FormData): Observable<Product> {
    return this.http.post<Product>(`${this.apiUrl}/saveProductWithImage`, formData);
  }

  updateProductWithImage(id: string | number, formData: FormData): Observable<Product> {
    // El endpoint espera un objeto JSON en "product" y un archivo en "image" (opcional para actualizaciones)
    return this.http.put<Product>(`${this.apiUrl}/UpdateProductWithImage`, formData);
  }

  updateProduct(id: string | number, product: Product): Observable<Product> {
    // Endpoint para actualizar un producto sin cambiar la imagen
    console.log('ProductService.updateProduct - ID:', id);
    console.log('ProductService.updateProduct - Datos:', JSON.stringify(product, null, 2));
    console.log('ProductService.updateProduct - URL:', `${this.apiUrl}/updateProduct`);
    
    // Usar responseType 'text' ya que el backend responde con texto plano
    return this.http.put(`${this.apiUrl}/updateProduct`, product, { responseType: 'text' })
      .pipe(
        map(response => {
          console.log('Respuesta del servidor:', response);
          // Devolver el producto original como respuesta simulada
          // ya que el backend no devuelve un objeto producto
          return product;
        })
      );
  }

  deleteProduct(id: string | number | undefined): Observable<void> {
    if (id === undefined || id === null) {
      return throwError(() => new Error('ID de producto inválido'));
    }
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
  
  /**
   * Limpia la caché de productos para forzar una nueva petición al servidor
   * Útil después de crear, actualizar o eliminar productos
   */
  clearProductsCache(): void {
    this.productsCache = [];
    this.lastFetchTime = 0;
    console.log('Caché de productos limpiada');
  }
}
