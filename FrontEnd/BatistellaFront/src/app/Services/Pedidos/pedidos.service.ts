import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Pedido } from '../../Models/pedido';

@Injectable({
  providedIn: 'root'
})
export class PedidosService {
  private apiUrl = 'http://localhost:8083/api/pedidos';
  private pedidosCache: Pedido[] = [];
  private lastFetchTime: number = 0;
  private cacheDuration: number = 60000; // 1 minuto en milisegundos

  constructor(private http: HttpClient) { }

  // Obtener todos los pedidos de un usuario
  getPedidosUsuario(usuarioId: number): Observable<Pedido[]> {
    const now = Date.now();
    
    // Si tenemos pedidos en caché y no han pasado más de 1 minuto, usamos la caché
    if (this.pedidosCache.length > 0 && now - this.lastFetchTime < this.cacheDuration) {
      return of(this.pedidosCache);
    }
    
    // Si no hay caché o está desactualizada, hacemos la petición
    return this.http.get<Pedido[]>(`${this.apiUrl}/usuario/${usuarioId}`).pipe(
      tap(pedidos => {
        this.pedidosCache = pedidos;
        this.lastFetchTime = Date.now();
      }),
      catchError(error => {
        console.error('Error al obtener pedidos:', error);
        return throwError(() => new Error('No se pudieron obtener los pedidos. Intente nuevamente más tarde.'));
      })
    );
  }

  // Obtener los detalles de un pedido específico
  getPedidoDetalle(pedidoId: number): Observable<Pedido> {
    // Buscar primero en la caché
    const pedidoCached = this.pedidosCache.find(p => p.id === pedidoId);
    if (pedidoCached) {
      return of(pedidoCached);
    }
    
    // Si no está en caché, hacer la petición al servidor
    return this.http.get<Pedido>(`${this.apiUrl}/${pedidoId}`).pipe(
      catchError(error => {
        console.error(`Error al obtener detalles del pedido ${pedidoId}:`, error);
        return throwError(() => new Error('No se pudo obtener el detalle del pedido. Intente nuevamente más tarde.'));
      })
    );
  }

  // Limpiar la caché de pedidos
  clearPedidosCache(): void {
    this.pedidosCache = [];
    this.lastFetchTime = 0;
  }
} 