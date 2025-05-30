import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PedidosVistosService {
  private readonly STORAGE_KEY = 'pedidosVistos';
  private pedidosVistos: Set<number> = new Set<number>();
  private pedidosVistosSubject = new BehaviorSubject<Set<number>>(new Set<number>());

  constructor() {
    this.cargarPedidosVistos();
  }

  /**
   * Carga los pedidos vistos desde localStorage al iniciar el servicio
   */
  private cargarPedidosVistos(): void {
    try {
      const pedidosVistosStr = localStorage.getItem(this.STORAGE_KEY);
      if (pedidosVistosStr) {
        const pedidosVistosArray = JSON.parse(pedidosVistosStr);
        this.pedidosVistos = new Set<number>(pedidosVistosArray);
        this.pedidosVistosSubject.next(this.pedidosVistos);
      }
    } catch (error) {
      console.error('Error al cargar pedidos vistos desde localStorage:', error);
    }
  }

  /**
   * Guarda los pedidos vistos en localStorage
   */
  private guardarPedidosVistos(): void {
    try {
      const pedidosVistosArray = Array.from(this.pedidosVistos);
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(pedidosVistosArray));
    } catch (error) {
      console.error('Error al guardar pedidos vistos en localStorage:', error);
    }
  }

  /**
   * Marca un pedido como visto
   * @param pedidoId ID del pedido a marcar como visto
   */
  marcarComoVisto(pedidoId: number): void {
    if (!this.pedidosVistos.has(pedidoId)) {
      this.pedidosVistos.add(pedidoId);
      this.pedidosVistosSubject.next(this.pedidosVistos);
      this.guardarPedidosVistos();
    }
  }

  /**
   * Comprueba si un pedido ha sido visto
   * @param pedidoId ID del pedido a comprobar
   * @returns true si el pedido ha sido visto, false en caso contrario
   */
  esPedidoVisto(pedidoId: number): boolean {
    return this.pedidosVistos.has(pedidoId);
  }

  /**
   * Obtiene un Observable con el conjunto de IDs de pedidos vistos
   * @returns Observable<Set<number>> con los IDs de pedidos vistos
   */
  getPedidosVistos(): Observable<Set<number>> {
    return this.pedidosVistosSubject.asObservable();
  }

  /**
   * Resetea todos los pedidos vistos
   */
  resetearPedidosVistos(): void {
    this.pedidosVistos.clear();
    this.pedidosVistosSubject.next(this.pedidosVistos);
    this.guardarPedidosVistos();
  }
} 