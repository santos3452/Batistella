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
  tipoGranja?: string; // Campo para variedades de animales de granja
  activo: boolean;
  fullName: string;
  // Campo local para manejar productos sin ID
  localId?: string;
  // Fechas de creación y actualización
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = 'http://localhost:8083/api/products';
  private productsCache: Product[] = [];
  private lastFetchTime: number = 0;
  private cacheDuration: number = 60000; // 1 minuto en milisegundos
  private marcasCache: string[] = [];
  private lastMarcasFetchTime: number = 0;

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
      // Reemplazar guiones bajos por espacios en tipoAlimento
      const tipoAlimentoFormateado = product.tipoAlimento.replace(/_/g, ' ');
      parts.push(tipoAlimentoFormateado);
    }
    
    // Añadir tipo de raza solo si no es null
    if (product.tipoRaza) {
      // Reemplazar guiones bajos por espacios en tipoRaza
      const tipoRazaFormateado = product.tipoRaza.replace(/_/g, ' ');
      parts.push(tipoRazaFormateado);
    }
    
    // Añadir tipo de animal de granja si el animalType es GRANJA y existe tipoGranja
    if (product.animalType === 'GRANJA' && product.tipoGranja) {
      // Reemplazar guiones bajos por espacios en tipoGranja
      const tipoGranjaFormateado = product.tipoGranja.replace(/_/g, ' ');
      parts.push(tipoGranjaFormateado);
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

  // Obtener productos para mostrar en el catálogo (solo activos)
  getActiveProducts(): Observable<Product[]> {
    return this.getProducts().pipe(
      map(products => products.filter(product => product.activo === true))
    );
  }

  getProductsByCategory(category: string): Observable<Product[]> {
    return this.getActiveProducts().pipe(
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
    
    return this.getActiveProducts().pipe(
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
    
    try {
      // Verificar si el formData contiene un objeto "productData"
      const productDataStr = formData.get('productData') as string;
      if (productDataStr) {
        // Parsear el JSON 
        const productData = JSON.parse(productDataStr);
        
        // Eliminar createdAt y establecer updatedAt en formato ISO
        delete productData.createdAt;
        productData.updatedAt = new Date().toISOString();
        
        // Reemplazar el objeto en el FormData
        formData.delete('productData');
        formData.append('productData', JSON.stringify(productData));
      }
    } catch (error) {
      console.error('Error al procesar formData en updateProductWithImage:', error);
    }
    
    return this.http.put<Product>(`${this.apiUrl}/UpdateProductWithImage`, formData);
  }

  updateProduct(id: string | number, product: Product): Observable<Product> {
    // Endpoint para actualizar un producto sin cambiar la imagen
    console.log('ProductService.updateProduct - ID:', id);
    
    // Preparar el objeto para enviar al backend
    // Formatear la fecha en ISO para el backend y excluir createdAt
    const now = new Date();
    const isoDate = now.toISOString(); // Formato ISO 8601: YYYY-MM-DDTHH:mm:ss.sssZ
    
    // Crear una copia del producto sin incluir createdAt
    const { createdAt, ...productToSend } = product;
    
    // Asignar la fecha de actualización en formato ISO
    productToSend.updatedAt = isoDate;
    
    console.log('ProductService.updateProduct - Datos:', JSON.stringify(productToSend, null, 2));
    console.log('ProductService.updateProduct - URL:', `${this.apiUrl}/updateProduct`);
    
    // Usar responseType 'text' ya que el backend responde con texto plano
    return this.http.put(`${this.apiUrl}/updateProduct`, productToSend, { responseType: 'text' })
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
    return this.http.delete(`${this.apiUrl}/deleteProduct/${id}?id=${id}`, { responseType: 'text' })
      .pipe(
        map(response => {
          console.log('Respuesta del servidor (cambio de estado):', response);
          return;
        })
      );
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

  /**
   * Actualiza los precios de productos por marca y porcentaje
   * @param porcentaje Porcentaje de ajuste (puede ser positivo o negativo)
   * @param marca Opcional: marca específica a actualizar, si no se especifica se actualizan todas
   * @returns Observable con la respuesta del servidor
   */
  updatePricesByBrandAndPercentage(porcentaje: number, marca?: string): Observable<any> {
    let url = `${this.apiUrl}/updatePrices?porcentaje=${porcentaje}`;
    
    if (marca && marca !== 'TODAS') {
      url += `&marca=${marca}`;
    }
    
    // Usar responseType 'text' ya que el backend responde con texto plano
    return this.http.put(url, {}, { responseType: 'text' }).pipe(
      map(response => {
        console.log('Respuesta de actualización de precios:', response);
        // Limpiar caché después de actualizar precios
        this.clearProductsCache();
        return response;
      }),
      // En caso de error, pero con status 200, tratar como éxito
      tap({
        error: error => {
          if (error.status === 200) {
            console.log('La actualización de precios fue exitosa a pesar del error técnico');
            this.clearProductsCache();
          }
        }
      })
    );
  }

  /**
   * Agrupa productos que son iguales excepto por su peso (kg) y precio
   * @returns Observable con productos agrupados donde cada grupo tiene variantes de peso
   */
  getProductsGroupedByWeight(): Observable<any[]> {
    return this.getActiveProducts().pipe(
      map(products => {
        // Crear un mapa para agrupar productos por marca, tipoAlimento y tipoRaza
        const productGroups = new Map();
        
        products.forEach(product => {
          // La clave es la combinación de marca + tipoAlimento + tipoRaza + animalType
          const key = `${product.marca}_${product.tipoAlimento}_${product.tipoRaza}_${product.animalType}`;
          
          if (!productGroups.has(key)) {
            // Crear un nuevo grupo con el primer producto y su variante de peso
            productGroups.set(key, {
              baseProduct: { ...product },
              variants: [{
                id: product.id,
                localId: product.localId,
                kg: product.kg,
                priceMinorista: product.priceMinorista,
                priceMayorista: product.priceMayorista,
                stock: product.stock
              }]
            });
          } else {
            // Añadir una nueva variante al grupo existente
            const group = productGroups.get(key);
            group.variants.push({
              id: product.id,
              localId: product.localId,
              kg: product.kg,
              priceMinorista: product.priceMinorista,
              priceMayorista: product.priceMayorista,
              stock: product.stock
            });
          }
        });
        
        // Convertir el mapa en un array de grupos de productos
        return Array.from(productGroups.values());
      })
    );
  }

  /**
   * Igual que getProductsGroupedByWeight pero filtrado por categoría
   */
  getProductsGroupedByWeightByCategory(category: string): Observable<any[]> {
    return this.getProductsGroupedByWeight().pipe(
      map(groupedProducts => 
        groupedProducts.filter(group => 
          group.baseProduct.animalType && 
          group.baseProduct.animalType.toUpperCase() === category.toUpperCase()
        )
      )
    );
  }

  /**
   * Obtiene todas las variantes de peso de un producto específico
   * @param product El producto base para buscar sus variantes
   * @returns Observable con el array de variantes encontradas
   */
  getProductVariants(product: Product): Observable<Product[]> {
    if (!product) {
      return throwError(() => new Error('Producto no válido'));
    }
    
    return this.getActiveProducts().pipe(
      map(products => {
        // Filtrar productos que sean variantes del producto indicado (mismo nombre, misma marca, mismo tipo)
        return products.filter(p => 
          p.marca === product.marca && 
          p.tipoAlimento === product.tipoAlimento && 
          p.tipoRaza === product.tipoRaza &&
          p.animalType === product.animalType
        );
      })
    );
  }

  // Método para obtener todas las marcas disponibles
  getAllMarcas(): Observable<string[]> {
    const now = Date.now();
    
    // Si tenemos marcas en caché y no han pasado más de 1 minuto, usamos la caché
    if (this.marcasCache.length > 0 && now - this.lastMarcasFetchTime < this.cacheDuration) {
      return of(this.marcasCache);
    }
    
    // Si no hay caché o está desactualizada, hacemos la petición
    return this.http.get<string[]>(`${this.apiUrl}/getAllMarcas`).pipe(
      tap(marcas => {
        this.marcasCache = marcas;
        this.lastMarcasFetchTime = Date.now();
      })
    );
  }
}
