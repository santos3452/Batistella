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
}
