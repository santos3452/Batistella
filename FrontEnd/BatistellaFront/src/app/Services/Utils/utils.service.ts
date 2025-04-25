import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class UtilsService {

  constructor() { }

  /**
   * Formatea un número como moneda (ARS)
   * @param value Valor a formatear
   * @returns String formateado como moneda
   */
  formatCurrency(value: number): string {
    return '$ ' + new Intl.NumberFormat('es-AR', {
      style: 'decimal',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(value);
  }

  /**
   * Muestra un mensaje toast de éxito o error
   * @param type Tipo de toast ('success' o 'error')
   * @param message Mensaje a mostrar
   */
  showToast(type: 'success' | 'error', message: string): void {
    // Implementación básica - podría ser reemplazada por una biblioteca de toasts
    const toast = document.createElement('div');
    toast.className = `fixed bottom-4 right-4 p-4 rounded-md text-white ${
      type === 'success' ? 'bg-green-500' : 'bg-red-500'
    }`;
    toast.textContent = message;
    document.body.appendChild(toast);
    
    // Eliminar después de 3 segundos
    setTimeout(() => {
      document.body.removeChild(toast);
    }, 3000);
  }
}
