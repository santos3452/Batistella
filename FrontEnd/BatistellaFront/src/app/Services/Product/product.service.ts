import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';

export interface Product {
  id?: string;
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
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = 'http://localhost:8083/api/products';

  constructor(private http: HttpClient) { }

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/getAllProducts`);
  }

  getProductsByCategory(category: string): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/getAllProducts`);
    // Nota: Este endpoint no filtra por categoría, deberíamos implementar una función 
    // para filtrar por animalType si queremos filtrar en el cliente
  }

  getProductById(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${id}`);
  }

  saveProductWithImage(formData: FormData): Observable<Product> {
    return this.http.post<Product>(`${this.apiUrl}/saveProductWithImage`, formData);
  }

  updateProductWithImage(id: string, formData: FormData): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/updateProductWithImage/${id}`, formData);
  }

  deleteProduct(id: string | undefined): Observable<void> {
    if (!id) {
      return throwError(() => new Error('ID de producto inválido'));
    }
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
