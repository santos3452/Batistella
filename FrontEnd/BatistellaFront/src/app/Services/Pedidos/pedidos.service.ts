import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, throwError, of, map } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Pedido as PedidoModel } from '../../Models/pedido';
import { environment } from '../../../environments/environment';

interface DetallePedido {
  id: number;
  productoId: number;
  nombreProducto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

interface Pedido {
  id: number;
  codigoPedido: string;
  usuarioId: number;
  nombreCompletoUsuario: string;
  email?: string;
  fechaPedido: string;
  estado: string;
  total: number;
  productos: DetallePedido[];
  createdAt: string;
  updatedAt: string;
  domicilio?: string;
}

export interface PedidoResponse {
  content: Pedido[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class PedidosService {
  private apiUrl = environment.pedidosUrl;
  private pedidosCache: Pedido[] = [];
  private lastFetchTime: number = 0;
  private cacheDuration: number = 60000; // 1 minuto en milisegundos

  constructor(private http: HttpClient) { }

  // Obtener todos los pedidos con paginación
  getPedidosPaginados(page?: number, size?: number, codigoPedido?: string, estado?: string, fecha?: string): Observable<PedidoResponse> {
    // Obtener token desde localStorage directamente
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('Token no disponible para la petición de pedidos');
      return throwError(() => new Error('No se pudo autenticar la solicitud. Por favor, inicie sesión nuevamente.'));
    }

    // Logging detallado del token (solo las primeras y últimas 10 caracteres)
    const tokenLength = token.length;
    const tokenFirstPart = token.substring(0, 10);
    const tokenLastPart = token.substring(tokenLength - 10, tokenLength);
    console.log(`Token disponible (longitud ${tokenLength}): ${tokenFirstPart}...${tokenLastPart}`);

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });

    // Iniciar con parámetros vacíos
    let params = new HttpParams();
    
    // Valores por defecto para la paginación cuando se usa
    const pagina = page !== undefined ? page : 1;
    const cantDatos = size !== undefined ? size : 12;
    
    // Determinar si vamos a usar paginación o no
    const usarPaginacion = page !== undefined && size !== undefined;
    
    // Solo añadir parámetros de paginación si vamos a usarlos
    if (usarPaginacion) {
      console.log(`Preparando petición con parámetros de paginación: pagina=${pagina}, cantDatos=${cantDatos}`);
      
      params = params
        .set('cantDatos', cantDatos.toString())
        .set('pagina', pagina.toString());
    } else {
      console.log('Preparando petición SIN parámetros de paginación para obtener todos los registros');
    }

    // Añadir parámetros opcionales solo si tienen valor
    if (codigoPedido && codigoPedido.trim() !== '') {
      params = params.set('codigoPedido', codigoPedido.trim());
      console.log(`Añadido filtro: codigoPedido=${codigoPedido.trim()}`);
    }
    
    if (estado && estado.trim() !== '') {
      params = params.set('estado', estado.trim());
      console.log(`Añadido filtro: estado=${estado.trim()}`);
    }
    
    if (fecha && fecha.trim() !== '') {
      params = params.set('fecha', fecha.trim());
      console.log(`Añadido filtro: fecha=${fecha.trim()}`);
    }

    const url = `${environment.pedidosUrl}/todosLosPedidos`;
    console.log(`Realizando petición a: ${url} con parámetros:`, params.toString());
    console.log('Headers de la petición:', {
      'Authorization': 'Bearer xxxx...xxxx',
      'Content-Type': headers.get('Content-Type'),
      'Accept': headers.get('Accept')
    });

    // Primero intentamos detectar si la respuesta es un objeto paginado o un array
    return this.http.get<any>(url, { 
      headers, 
      params,
      observe: 'response' // Para poder acceder a los headers de la respuesta
    })
    .pipe(
      map(response => {
        // Verificar si hay headers con información de paginación
        const totalCountHeader = response.headers.get('X-Total-Count') || 
                                response.headers.get('total-count') || 
                                response.headers.get('totalCount') || null;
        
        const totalPagesHeader = response.headers.get('X-Total-Pages') || 
                                response.headers.get('total-pages') || 
                                response.headers.get('totalPages') || null;
        
        const body = response.body;

        // Verificar si el body es un array o un objeto con propiedad content
        let pedidos: Pedido[];
        let totalItems: number;
        let totalPages: number;

        if (Array.isArray(body)) {
          // Si es un array directo
          console.log('Respuesta del backend es un array de pedidos');
          pedidos = body;
          totalItems = totalCountHeader ? parseInt(totalCountHeader) : pedidos.length;
          totalPages = totalPagesHeader ? parseInt(totalPagesHeader) : Math.ceil(totalItems / cantDatos);
        } 
        else if (body && Array.isArray(body.content)) {
          // Si ya es un objeto paginado con propiedad content
          console.log('Respuesta del backend es un objeto paginado');
          pedidos = body.content;
          totalItems = body.totalElements || (totalCountHeader ? parseInt(totalCountHeader) : pedidos.length);
          totalPages = body.totalPages || (totalPagesHeader ? parseInt(totalPagesHeader) : Math.ceil(totalItems / cantDatos));
        }
        else {
          // Formato inesperado, asumimos array vacío
          console.warn('Formato de respuesta no reconocido:', body);
          pedidos = [];
          totalItems = 0;
          totalPages = 0;
        }

        // Creamos un objeto que cumple con nuestra interfaz PedidoResponse
        const result: PedidoResponse = {
          content: pedidos,
          totalElements: totalItems,
          totalPages: totalPages,
          size: cantDatos,
          number: pagina
        };

        return result;
      }),
      tap(response => {
        console.log('Respuesta procesada:');
        console.log('Datos en la respuesta:', {
          totalElements: response.totalElements,
          totalPages: response.totalPages,
          size: response.size,
          number: response.number,
          contentLength: response.content?.length || 0
        });
      }),
      catchError(error => {
        console.error('Error al obtener pedidos paginados:', error);
        console.error('Status:', error.status);
        console.error('Message:', error.message);
        console.error('Error body:', error.error);
        return throwError(() => new Error('No se pudieron obtener los pedidos. Intente nuevamente más tarde.'));
      })
    );
  }

  // Obtener todos los pedidos de un usuario
  getPedidosUsuario(usuarioId: number): Observable<Pedido[]> {
    const now = Date.now();
    
    // Obtener el token y crear los headers
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'accept': 'application/hal+json'
    });
    
    // Si tenemos pedidos en caché y no han pasado más de 1 minuto, usamos la caché
    if (this.pedidosCache.length > 0 && now - this.lastFetchTime < this.cacheDuration) {
      return of(this.pedidosCache);
    }
    
    // Si no hay caché o está desactualizada, hacemos la petición
    return this.http.get<Pedido[]>(`${this.apiUrl}/usuario/${usuarioId}`, { headers }).pipe(
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

  // Actualizar el estado de un pedido
  actualizarEstadoPedido(pedidoId: number, nuevoEstado: string): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    
    return this.http.put(
      `${environment.pedidosUrl}/actualizarEstado/?pedidoId=${pedidoId}&nuevoEstado=${nuevoEstado}`, 
      {}, 
      { 
        headers,
        responseType: 'text' // Especificamos que esperamos texto plano, no JSON
      }
    ).pipe(
      tap(() => {
        // Actualizar el estado en la caché si existe
        const pedidoIndex = this.pedidosCache.findIndex(p => p.id === pedidoId);
        if (pedidoIndex !== -1) {
          this.pedidosCache[pedidoIndex].estado = nuevoEstado;
        }
      }),
      catchError(error => {
        console.error('Error al actualizar estado del pedido:', error);
        return throwError(() => new Error('No se pudo actualizar el estado del pedido. Intente nuevamente más tarde.'));
      })
    );
  }

  // Actualizar el estado de pago de un pedido
  actualizarEstadoPago(codigoPedido: string, nuevoEstado: string): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    
    return this.http.put(
      `${environment.pagosUrl}/cambiarEstado?codigoPedido=${codigoPedido}&estado=${nuevoEstado}`, 
      {}, 
      { 
        headers,
        responseType: 'text' // Especificamos que esperamos texto plano, no JSON
      }
    ).pipe(
      tap(() => {
        // Limpiar la caché para que se actualice en la próxima solicitud
        this.clearPedidosCache();
      }),
      catchError(error => {
        console.error('Error al actualizar estado de pago:', error);
        return throwError(() => new Error('No se pudo actualizar el estado de pago. Intente nuevamente más tarde.'));
      })
    );
  }

  // Crear un nuevo pedido
  crearPedido(usuarioId: number, productos: {productoId: number, cantidad: number}[], domicilioDeEntrega: string, costoEnvio?: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    const payload: any = {
      usuarioId,
      productos,
      domicilioDeEtrega: domicilioDeEntrega,  // Nombre exacto según el backend
      costoEnvio: costoEnvio !== undefined ? costoEnvio : 0  // Siempre enviar costoEnvio, 0 si es gratis
    };

    console.log('=== CREANDO PEDIDO CON COSTO DE ENVÍO ===');
    console.log('Costo de envío enviado al backend:', payload.costoEnvio);
    console.log('Payload completo para crear pedido:', payload);
    
    return this.http.post(
      `${environment.pedidosUrl}`, 
      payload,
      { headers }
    ).pipe(
      tap(() => {
        // Limpiar la caché para que se actualice en la próxima solicitud
        this.clearPedidosCache();
      }),
      catchError(error => {
        console.error('Error al crear pedido:', error);
        return throwError(() => new Error('No se pudo crear el pedido. Intente nuevamente más tarde.'));
      })
    );
  }
} 