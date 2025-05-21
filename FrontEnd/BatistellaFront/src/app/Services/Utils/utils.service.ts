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
   * @param asModal Si es true, muestra el mensaje como un modal superpuesto centrado
   */
  showToast(type: 'success' | 'error', message: string, asModal: boolean = false): void {
    // Implementación básica - podría ser reemplazada por una biblioteca de toasts
    const toast = document.createElement('div');
    
    if (asModal) {
      // Crear un fondo oscuro
      const backdrop = document.createElement('div');
      backdrop.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50';
      backdrop.id = 'toast-backdrop';
      
      // Crear el contenido del modal
      toast.className = `relative bg-white rounded-lg shadow-lg p-4 m-4 max-w-md transform transition-all ${
        type === 'success' ? 'border-l-4 border-green-500' : 'border-l-4 border-red-500'
      }`;
      
      // Título y mensaje
      const title = document.createElement('div');
      title.className = `text-lg font-bold mb-2 ${
        type === 'success' ? 'text-green-700' : 'text-red-700'
      }`;
      title.textContent = type === 'success' ? 'Operación Exitosa' : 'Error';
      
      const content = document.createElement('div');
      content.className = 'text-gray-700';
      content.textContent = message;
      
      // Botón de cerrar
      const closeButton = document.createElement('button');
      closeButton.className = 'absolute top-2 right-2 text-gray-500 hover:text-gray-700';
      closeButton.innerHTML = '&times;';
      closeButton.onclick = () => {
        document.body.removeChild(backdrop);
      };
      
      toast.appendChild(closeButton);
      toast.appendChild(title);
      toast.appendChild(content);
      backdrop.appendChild(toast);
      document.body.appendChild(backdrop);
      
      // Eliminar después de 3 segundos
      setTimeout(() => {
        if (document.body.contains(backdrop)) {
          document.body.removeChild(backdrop);
        }
      }, 3000);
    } else {
      // Toast normal en la esquina
      toast.className = `fixed bottom-4 right-4 p-4 rounded-md text-white ${
        type === 'success' ? 'bg-green-500' : 'bg-red-500'
      } z-50`;
      toast.textContent = message;
      document.body.appendChild(toast);
      
      // Eliminar después de 3 segundos
      setTimeout(() => {
        if (document.body.contains(toast)) {
          document.body.removeChild(toast);
        }
      }, 3000);
    }
  }
}
