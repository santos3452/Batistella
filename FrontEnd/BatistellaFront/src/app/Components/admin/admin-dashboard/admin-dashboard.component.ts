import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions, ChartType } from 'chart.js';
import { DashboardService } from '../../../Services/Dashboard/dashboard.service';
import { PrintService } from '../../../Services/Print/print.service';
import { SalesSummary, DashboardFilters, PaymentSummary, ProductsSummary, CustomersSummary } from '../../../Models/dashboard';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';

// Tipos para la agrupación temporal
export type TimeGrouping = 'daily' | 'weekly' | 'monthly';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, BaseChartDirective]
})
export class AdminDashboardComponent implements OnInit {
  isLoading = false;
  isLoadingPayments = false;
  isLoadingProducts = false;
  isLoadingCustomers = false;
  errorMessage: string | null = null;
  errorMessagePayments: string | null = null;
  errorMessageProducts: string | null = null;
  errorMessageCustomers: string | null = null;
  
  // Control de pestañas
  activeTab: 'ventas' | 'pagos' | 'productos' | 'clientes' = 'ventas';
  
  // Propiedades para agrupación temporal simplificada
  currentTimeGrouping: TimeGrouping = 'daily';
  originalChartData: any[] = [];
  
  // Control de impresión
    // Datos del dashboard
  salesSummary: SalesSummary | null = null;
  paymentSummary: PaymentSummary | null = null;
  productsSummary: ProductsSummary | null = null;
  customersSummary: CustomersSummary | null = null;
  
  // Filtros de fecha
  filters: DashboardFilters = {
    from: this.getDefaultFromDate(),
    to: this.getDefaultToDate()
  };

  // Handler para el click del gráfico (solo para navegación a pedidos en vista diaria)
  public chartClickHandler = (event: any, elements: any[], chart: any) => {
    // SOLO permitir click en vista diaria para ir a pedidos
    if (this.currentTimeGrouping !== 'daily') {
      return;
    }
    
    if (elements && elements.length > 0) {
      const element = elements[0];
      const dataIndex = element.index;
      
      let clickedLabel = '';
      if (chart && chart.data && chart.data.labels) {
        clickedLabel = chart.data.labels[dataIndex];
      } else if (this.barChartLabels && this.barChartLabels[dataIndex]) {
        clickedLabel = this.barChartLabels[dataIndex];
      }
      
      if (clickedLabel) {
        this.navigateToOrdersWithDate(clickedLabel);
      }
    }
  };

  // Configuración del gráfico de barras (ventas) - CON CLICK SOLO PARA VISTA DIARIA
  public barChartOptions: ChartOptions = {
    responsive: true,
    plugins: {
      title: {
        display: true,
        text: 'Ventas por Período'
      },
      legend: {
        display: false
      },
      tooltip: {
        callbacks: {
          label: (context: any) => {
            const value = context.parsed.y;
            const label = context.label;
            
            let tooltips = [`Ingresos: $${value.toLocaleString()}`];
            
            // Información de tooltip con hint de navegación
            switch (this.currentTimeGrouping) {
              case 'daily':
                const dailyData = this.originalChartData.find(item => 
                  this.convertToDisplayLabel(item.label) === label
                );
                if (dailyData) {
                  tooltips.push(`Pedidos realizados: ${dailyData.count}`);
                  tooltips.push(`💡 Haz clic para ver pedidos del día`);
                }
                break;
              case 'weekly':
                tooltips.push(`Vista semanal`);
                break;
              case 'monthly':
                tooltips.push(`Vista mensual`);
                break;
            }
            
            return tooltips;
          }
        }
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: function(value) {
            return '$' + Number(value).toLocaleString();
          }
        }
      }
    },
    onClick: this.chartClickHandler
  };

  // Configuración del gráfico de torta (medios de pago)
  public pieChartOptionsPaymentMethods: ChartOptions = {
    responsive: true,
    plugins: {
      title: {
        display: true,
        text: 'Distribución por Medios de Pago'
      },
      legend: {
        display: true,
        position: 'right'
      },
      tooltip: {
        callbacks: {
          label: (context: any) => {
            const dataIndex = context.dataIndex;
            const methodData = this.paymentSummary?.resumenPorMetodo[dataIndex];
            const percentage = methodData?.porcentaje || 0;
            const count = methodData?.cantidad || 0;
            const amount = methodData?.montoTotal || 0;
            
            return [
              `${context.label}: ${percentage.toFixed(1)}%`,
              `Cantidad: ${count} pagos`,
              `Monto: ${this.formatCurrency(amount)}`
            ];
          }
        }
      }
    }
  };

  // Configuración del gráfico de torta (estados de pago)
  public pieChartOptionsPaymentStates: ChartOptions = {
    responsive: true,
    plugins: {
      title: {
        display: true,
        text: 'Distribución por Estados de Pago'
      },
      legend: {
        display: true,
        position: 'right'
      },
      tooltip: {
        callbacks: {
          label: (context: any) => {
            const dataIndex = context.dataIndex;
            const stateData = this.paymentSummary?.resumenPorEstado[dataIndex];
            const percentage = stateData?.porcentaje || 0;
            const count = stateData?.cantidad || 0;
            
            return [
              `${context.label}: ${percentage.toFixed(1)}%`,
              `Cantidad: ${count} pagos`
            ];
          }
        }
      }
    }
  };
  
  public barChartLabels: string[] = [];
  public barChartType: ChartType = 'bar';
  public barChartData: ChartConfiguration['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Ventas',
        backgroundColor: 'rgba(34, 197, 94, 0.8)',
        borderColor: 'rgba(34, 197, 94, 1)',
        borderWidth: 1
      }
    ]
  };

  // Datos del gráfico de medios de pago
  public pieChartTypePaymentMethods: ChartType = 'pie';
  public pieChartDataPaymentMethods: ChartConfiguration['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        backgroundColor: [
          'rgba(59, 130, 246, 0.8)',   // Azul
          'rgba(34, 197, 94, 0.8)',    // Verde  
          'rgba(251, 191, 36, 0.8)',   // Amarillo
          'rgba(239, 68, 68, 0.8)',    // Rojo
          'rgba(147, 51, 234, 0.8)',   // Violeta
          'rgba(245, 101, 101, 0.8)',  // Rosa
          'rgba(14, 165, 233, 0.8)',   // Cyan
          'rgba(139, 69, 19, 0.8)',    // Marrón
        ],
        borderColor: [
          'rgba(59, 130, 246, 1)',
          'rgba(34, 197, 94, 1)',
          'rgba(251, 191, 36, 1)',
          'rgba(239, 68, 68, 1)',
          'rgba(147, 51, 234, 1)',
          'rgba(245, 101, 101, 1)',
          'rgba(14, 165, 233, 1)',
          'rgba(139, 69, 19, 1)',
        ],
        borderWidth: 1
      }
    ]
  };

  // Datos del gráfico de estados de pago
  public pieChartTypePaymentStates: ChartType = 'pie';
  public pieChartDataPaymentStates: ChartConfiguration['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        backgroundColor: [
          'rgba(34, 197, 94, 0.8)',    // Verde para COMPLETADO
          'rgba(239, 68, 68, 0.8)',    // Rojo para CANCELADO
          'rgba(251, 191, 36, 0.8)',   // Amarillo para PENDIENTE
          'rgba(59, 130, 246, 0.8)',   // Azul para EN_PROCESO
        ],
        borderColor: [
          'rgba(34, 197, 94, 1)',
          'rgba(239, 68, 68, 1)',
          'rgba(251, 191, 36, 1)',
          'rgba(59, 130, 246, 1)',
        ],
        borderWidth: 1
      }
    ]
  };
  
  constructor(
    private dashboardService: DashboardService,
    private router: Router,
    private printService: PrintService
  ) {}
  
  ngOnInit(): void {
    this.loadDashboardData();
  }

  /**
   * Carga todos los datos del dashboard
   */
  loadDashboardData(): void {
    this.loadSalesSummary();
    this.loadPaymentSummary();
    this.loadProductsSummary();
    this.loadCustomersSummary();
  }
  
  /**
   * Carga el resumen de ventas desde el backend
   */
  loadSalesSummary(): void {
    this.isLoading = true;
    this.errorMessage = null;
    
    this.dashboardService.getSalesSummary(this.filters).subscribe({
      next: (data: SalesSummary) => {
        this.salesSummary = data;
        this.updateChart(data);
        this.isLoading = false;
      },
      error: (error) => {
        this.handleError(error);
        this.isLoading = false;
      }
    });
  }
  
  /**
   * Carga el resumen de medios de pago desde el backend
   */
  loadPaymentSummary(): void {
    this.isLoadingPayments = true;
    this.errorMessagePayments = null;
    
    this.dashboardService.getPaymentSummary(this.filters).subscribe({
      next: (data: PaymentSummary) => {
        this.paymentSummary = data;
        this.updatePaymentCharts(data);
        this.isLoadingPayments = false;
      },
      error: (error) => {
        this.handlePaymentError(error);
        this.isLoadingPayments = false;
      }
    });
  }
  
  /**
   * Carga el resumen de productos más vendidos desde el backend
   */
  loadProductsSummary(): void {
    this.isLoadingProducts = true;
    this.errorMessageProducts = null;
    
    this.dashboardService.getProductsSummary(this.filters).subscribe({
      next: (data: ProductsSummary) => {
        this.productsSummary = data;
        this.isLoadingProducts = false;
      },
      error: (error) => {
        this.handleProductsError(error);
        this.isLoadingProducts = false;
      }
    });
  }
  
  /**
   * Carga el resumen de clientes más frecuentes desde el backend
   */
  loadCustomersSummary(): void {
    this.isLoadingCustomers = true;
    this.errorMessageCustomers = null;
    
    this.dashboardService.getCustomersSummary(this.filters).subscribe({
      next: (data: CustomersSummary) => {
        this.customersSummary = data;
        this.isLoadingCustomers = false;
      },
      error: (error) => {
        this.handleCustomersError(error);
        this.isLoadingCustomers = false;
      }
    });
  }
  
  /**
   * Actualiza los datos del gráfico de ventas
   */
  private updateChart(data: SalesSummary): void {
    // Guardar datos originales para agrupación
    this.originalChartData = [...data.chartData];
    
    // Aplicar agrupación según el tipo actual
    this.regroupChartData();
  }

  /**
   * Actualiza los datos de los gráficos de medios de pago
   */
  private updatePaymentCharts(data: PaymentSummary): void {
    // Actualizar gráfico de medios de pago
    this.pieChartDataPaymentMethods = {
      labels: data.resumenPorMetodo.map(item => this.formatPaymentMethodName(item.metodo)),
      datasets: [
        {
          data: data.resumenPorMetodo.map(item => item.porcentaje),
          backgroundColor: [
            'rgba(59, 130, 246, 0.8)',   // Azul
            'rgba(34, 197, 94, 0.8)',    // Verde  
            'rgba(251, 191, 36, 0.8)',   // Amarillo
            'rgba(239, 68, 68, 0.8)',    // Rojo
            'rgba(147, 51, 234, 0.8)',   // Violeta
            'rgba(245, 101, 101, 0.8)',  // Rosa
            'rgba(14, 165, 233, 0.8)',   // Cyan
            'rgba(139, 69, 19, 0.8)',    // Marrón
          ],
          borderColor: [
            'rgba(59, 130, 246, 1)',
            'rgba(34, 197, 94, 1)',
            'rgba(251, 191, 36, 1)',
            'rgba(239, 68, 68, 1)',
            'rgba(147, 51, 234, 1)',
            'rgba(245, 101, 101, 1)',
            'rgba(14, 165, 233, 1)',
            'rgba(139, 69, 19, 1)',
          ],
          borderWidth: 1
        }
      ]
    };

    // Actualizar gráfico de estados de pago
    this.pieChartDataPaymentStates = {
      labels: data.resumenPorEstado.map(item => this.formatPaymentStateName(item.estado)),
      datasets: [
        {
          data: data.resumenPorEstado.map(item => item.porcentaje),
          backgroundColor: data.resumenPorEstado.map(item => this.getStateColor(item.estado, 0.8)),
          borderColor: data.resumenPorEstado.map(item => this.getStateColor(item.estado, 1)),
          borderWidth: 1
        }
      ]
    };
  }

  /**
   * Obtiene el color según el estado del pago
   */
  private getStateColor(estado: string, opacity: number): string {
    const colors: {[key: string]: string} = {
      'COMPLETADO': `rgba(34, 197, 94, ${opacity})`,    // Verde
      'CANCELADO': `rgba(239, 68, 68, ${opacity})`,     // Rojo
      'PENDIENTE': `rgba(251, 191, 36, ${opacity})`,    // Amarillo
      'EN_PROCESO': `rgba(59, 130, 246, ${opacity})`    // Azul
    };
    return colors[estado] || `rgba(156, 163, 175, ${opacity})`; // Gris por defecto
  }

  /**
   * Formatea el nombre del método de pago para mostrar
   */
  formatPaymentMethodName(metodo: string): string {
    const methodNames: {[key: string]: string} = {
      'mercadopago': 'MercadoPago',
      'efectivo': 'Efectivo',
      'transferencia': 'Transferencia',
      'tarjeta_credito': 'Tarjeta de Crédito',
      'tarjeta_debito': 'Tarjeta de Débito'
    };
    return methodNames[metodo] || metodo;
  }

  /**
   * Formatea el nombre del estado de pago para mostrar
   */
  formatPaymentStateName(estado: string): string {
    const stateNames: {[key: string]: string} = {
      'COMPLETADO': 'Completado',
      'CANCELADO': 'Cancelado',
      'PENDIENTE': 'Pendiente',
      'EN_PROCESO': 'En Proceso'
    };
    return stateNames[estado] || estado;
  }
  
  /**
   * Aplica los filtros y recarga los datos
   */
  applyFilters(): void {
    if (this.validateDateRange()) {
      this.loadDashboardData();
    }
  }
  
  /**
   * Valida que el rango de fechas sea válido
   */
  private validateDateRange(): boolean {
    if (!this.filters.from || !this.filters.to) {
      this.errorMessage = 'Por favor, selecciona ambas fechas.';
      return false;
    }
    
    const fromDate = new Date(this.filters.from);
    const toDate = new Date(this.filters.to);
    
    if (fromDate > toDate) {
      this.errorMessage = 'La fecha "desde" no puede ser posterior a la fecha "hasta".';
      return false;
    }
    
    this.errorMessage = null;
    this.errorMessagePayments = null;
    this.errorMessageProducts = null;
    this.errorMessageCustomers = null;
    return true;
  }
  
  /**
   * Maneja errores de la API (ventas)
   */
  private handleError(error: any): void {
    if (error.status === 401 || error.status === 403) {
      this.errorMessage = 'No tienes permisos para acceder a esta información.';
    } else if (error.status === 0) {
      this.errorMessage = 'No se pudo conectar al servidor. Verifica tu conexión a internet.';
    } else {
      this.errorMessage = 'Ocurrió un error al cargar los datos. Por favor, intenta de nuevo más tarde.';
    }
  }

  /**
   * Maneja errores de la API (medios de pago)
   */
  private handlePaymentError(error: any): void {
    if (error.status === 401 || error.status === 403) {
      this.errorMessagePayments = 'No tienes permisos para acceder a la información de medios de pago.';
    } else if (error.status === 0) {
      this.errorMessagePayments = 'No se pudo conectar al servidor para obtener datos de medios de pago.';
    } else {
      this.errorMessagePayments = 'Ocurrió un error al cargar los datos de medios de pago.';
    }
  }

  /**
   * Maneja errores de la API (productos más vendidos)
   */
  private handleProductsError(error: any): void {
    if (error.status === 401 || error.status === 403) {
      this.errorMessageProducts = 'No tienes permisos para acceder a la información de productos más vendidos.';
    } else if (error.status === 0) {
      this.errorMessageProducts = 'No se pudo conectar al servidor para obtener datos de productos más vendidos.';
    } else {
      this.errorMessageProducts = 'Ocurrió un error al cargar los datos de productos más vendidos.';
    }
  }

  /**
   * Maneja errores de la API (clientes más frecuentes)
   */
  private handleCustomersError(error: any): void {
    if (error.status === 401 || error.status === 403) {
      this.errorMessageCustomers = 'No tienes permisos para acceder a la información de clientes más frecuentes.';
    } else if (error.status === 0) {
      this.errorMessageCustomers = 'No se pudo conectar al servidor para obtener datos de clientes más frecuentes.';
    } else {
      this.errorMessageCustomers = 'Ocurrió un error al cargar los datos de clientes más frecuentes.';
    }
  }
  
  /**
   * Formatea números como moneda
   */
  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS'
    }).format(value);
  }
  
  /**
   * Formatea números
   */
  formatNumber(value: number): string {
    return new Intl.NumberFormat('es-AR').format(value);
  }

  /**
   * Formatea porcentajes
   */
  formatPercentage(value: number): string {
    return `${value.toFixed(1)}%`;
  }

  /**
   * Formatea fechas en formato legible
   */
  formatDate(dateString: string): string {
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('es-AR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      });
    } catch (error) {
      return dateString;
    }
  }

  /**
   * Genera las iniciales del nombre completo
   */
  getInitials(fullName: string): string {
    return fullName
      .split(' ')
      .map(name => name.charAt(0))
      .join('')
      .toUpperCase()
      .substring(0, 2);
  }

  /**
   * Obtiene el día con más ventas cuando el backend no lo proporciona
   */
  getBestSellingDay(): string {
    if (!this.salesSummary?.chartData?.length) {
      return 'N/A';
    }
    
    const bestDay = this.salesSummary.chartData.reduce((prev, current) => {
      return (prev.value > current.value) ? prev : current;
    });
    
    return bestDay.label;
  }

  /**
   * Navega a la página de administración de pedidos con la fecha seleccionada
   */
  navigateToOrdersWithDate(dateLabel: string): void {
    // Buscar el item original correspondiente para obtener la fecha completa
    const originalItem = this.originalChartData.find(item => 
      this.convertToDisplayLabel(item.label) === dateLabel
    );
    
    let dateForUrl: string;
    if (originalItem) {
      // Usar la fecha original del backend para mayor precisión
      dateForUrl = this.convertDateLabelToDateFormat(originalItem.label);
    } else {
      // Fallback: convertir la etiqueta display
      dateForUrl = this.convertDateLabelToDateFormat(dateLabel);
    }
    
    // Navegar a admin/pedidos con el parámetro de fecha
    this.router.navigate(['/admin/pedidos'], { 
      queryParams: { 
        fecha: dateForUrl,
        fromDashboard: 'true'
      } 
    });
  }

  /**
   * Convierte el formato de fecha del dashboard a formato YYYY-MM-DD para la URL
   */
    private convertDateLabelToDateFormat(dateLabel: string): string {
    try {
      // Caso 1: Formato backend "DD-MMM.-YY" (ej: "07-may.-25")
      const backendMatch = dateLabel.match(/^(\d{1,2})-(.*?)\.?-(\d{2})$/);
      if (backendMatch) {
        const day = backendMatch[1].padStart(2, '0');
        const monthAbbr = backendMatch[2].toLowerCase().replace('.', '');
        const yearShort = parseInt(backendMatch[3]);
        
        // Conversión de año
        const year = yearShort >= 0 && yearShort <= 30 ? 2000 + yearShort : 1900 + yearShort;
        
        // Mapeo simplificado de meses
        const monthNum = monthAbbr === 'ene' ? '01' : monthAbbr === 'feb' ? '02' : monthAbbr === 'mar' ? '03' : 
                        monthAbbr === 'abr' ? '04' : monthAbbr === 'may' ? '05' : monthAbbr === 'jun' ? '06' :
                        monthAbbr === 'jul' ? '07' : monthAbbr === 'ago' ? '08' : monthAbbr === 'sep' ? '09' :
                        monthAbbr === 'oct' ? '10' : monthAbbr === 'nov' ? '11' : monthAbbr === 'dic' ? '12' : '01';
        
        const result = `${year}-${monthNum}-${day}`;
        return result;
      }
      
      // Caso 2: Formato display "DD-MMM" (ej: "07-may")
      const parts = dateLabel.split('-');
      if (parts.length === 2) {
        const day = parts[0].padStart(2, '0');
        const monthAbbr = parts[1].toLowerCase();
        
        // Mapeo simplificado de meses
        const monthNum = monthAbbr === 'ene' ? '01' : monthAbbr === 'feb' ? '02' : monthAbbr === 'mar' ? '03' : 
                        monthAbbr === 'abr' ? '04' : monthAbbr === 'may' ? '05' : monthAbbr === 'jun' ? '06' :
                        monthAbbr === 'jul' ? '07' : monthAbbr === 'ago' ? '08' : monthAbbr === 'sep' ? '09' :
                        monthAbbr === 'oct' ? '10' : monthAbbr === 'nov' ? '11' : monthAbbr === 'dic' ? '12' : '01';
        
        // Usar año de filtros o año actual
        let year = new Date().getFullYear();
        if (this.filters.from) {
          const fromDate = new Date(this.filters.from);
          if (!isNaN(fromDate.getTime())) {
            year = fromDate.getFullYear();
          }
        }
        
        const result = `${year}-${monthNum}-${day}`;
        return result;
      }
      
      // Caso 3: Si ya viene en formato ISO
      if (/^\d{4}-\d{2}-\d{2}$/.test(dateLabel)) {
        return dateLabel;
      }
      
      // Fallback
      return new Date().toISOString().split('T')[0];
      
    } catch (error) {
      return new Date().toISOString().split('T')[0];
    }
  }

  // ============ MÉTODOS DE AGRUPACIÓN TEMPORAL SIMPLIFICADA ============

  /**
   * Cambia el tipo de agrupación temporal
   */
  changeTimeGrouping(newGrouping: TimeGrouping): void {
    this.currentTimeGrouping = newGrouping;
    
    // Aplicar filtros automáticamente para refrescar los datos
    this.applyFilters();
  }

  /**
   * Reagrupa los datos del gráfico según el tipo de agrupación actual
   */
  private regroupChartData(): void {
    if (!this.originalChartData || this.originalChartData.length === 0) {
      return;
    }

    let groupedData: any[] = [];

    switch (this.currentTimeGrouping) {
      case 'daily':
        // Para vista diaria, convertir etiquetas para remover año
        groupedData = this.originalChartData.map(item => ({
          ...item,
          label: this.convertToDisplayLabel(item.label)
        }));
        break;
      case 'weekly':
        groupedData = this.groupDataByWeek(this.originalChartData);
        break;
      case 'monthly':
        groupedData = this.groupDataByMonth(this.originalChartData);
        break;
    }

    // Actualizar el gráfico con los datos agrupados
    this.updateChartWithGroupedData(groupedData);
  }

  /**
   * Agrupa los datos por semana
   */
  private groupDataByWeek(data: any[]): any[] {
    const weekGroups: { [key: string]: { 
      value: number, 
      count: number, 
      dates: string[], 
      weekStart: Date, 
      weekEnd: Date 
    } } = {};

    data.forEach(item => {
      // Intentar parsear la fecha de diferentes formatos
      let date: Date;
      if (item.label.includes('-')) {
        // Formato como "2024-05-15" o "15-May"
        date = new Date(item.label);
        if (isNaN(date.getTime())) {
          // Si falla, intentar con el formato "DD-MMM"
          date = this.parseDateLabel(item.label);
        }
      } else {
        date = new Date(item.label);
      }

      if (isNaN(date.getTime())) {
        return; // Saltar este elemento si no se puede parsear
      }

      const weekStart = this.getWeekStart(date);
      const weekEnd = new Date(weekStart);
      weekEnd.setDate(weekEnd.getDate() + 6);
      const weekKey = this.formatWeekLabel(weekStart);

      if (!weekGroups[weekKey]) {
        weekGroups[weekKey] = { 
          value: 0, 
          count: 0, 
          dates: [], 
          weekStart: weekStart, 
          weekEnd: weekEnd 
        };
      }

      weekGroups[weekKey].value += item.value;
      weekGroups[weekKey].count += item.count;
      weekGroups[weekKey].dates.push(item.label);
    });

    const result = Object.keys(weekGroups).map(weekKey => ({
      label: weekKey,
      value: weekGroups[weekKey].value,
      count: weekGroups[weekKey].count,
      dates: weekGroups[weekKey].dates,
      weekStart: weekGroups[weekKey].weekStart,
      weekEnd: weekGroups[weekKey].weekEnd
    }));

    return result;
  }

  /**
   * Agrupa los datos por mes
   */
  private groupDataByMonth(data: any[]): any[] {
    const monthGroups: { [key: string]: { 
      value: number, 
      count: number, 
      dates: string[] 
    } } = {};

    data.forEach(item => {
      // Intentar parsear la fecha de diferentes formatos
      let date: Date;
      if (item.label.includes('-')) {
        date = new Date(item.label);
        if (isNaN(date.getTime())) {
          date = this.parseDateLabel(item.label);
        }
      } else {
        date = new Date(item.label);
      }

      if (isNaN(date.getTime())) {
        return;
      }

      const monthKey = this.formatMonthLabel(date);

      if (!monthGroups[monthKey]) {
        monthGroups[monthKey] = { value: 0, count: 0, dates: [] };
      }

      monthGroups[monthKey].value += item.value;
      monthGroups[monthKey].count += item.count;
      monthGroups[monthKey].dates.push(item.label);
    });

    const result = Object.keys(monthGroups).map(monthKey => ({
      label: monthKey,
      value: monthGroups[monthKey].value,
      count: monthGroups[monthKey].count,
      dates: monthGroups[monthKey].dates
    }));

    return result;
  }

  /**
   * Obtiene el inicio de la semana para una fecha dada
   */
  private getWeekStart(date: Date): Date {
    const d = new Date(date);
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1); // Lunes como primer día
    return new Date(d.setDate(diff));
  }

  /**
   * Formatea la etiqueta de la semana
   */
  private formatWeekLabel(weekStart: Date): string {
    const weekEnd = new Date(weekStart);
    weekEnd.setDate(weekEnd.getDate() + 6);
    
    // Formato: "Sem DD-MM" donde DD es el día de inicio y MM es el mes
    const startDay = weekStart.getDate().toString().padStart(2, '0');
    const month = (weekStart.getMonth() + 1).toString().padStart(2, '0');
    
    const label = `Sem ${startDay}-${month}`;
    return label;
  }

  /**
   * Formatea la etiqueta del mes CON AÑO (para vista mensual)
   */
  private formatMonthLabel(date: Date): string {
    const months = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun',
                   'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return `${months[date.getMonth()]} ${date.getFullYear()}`;
  }

  /**
   * Formatea la etiqueta diaria SIN AÑO (para vista diaria)
   */
  private formatDayLabel(date: Date): string {
    const months = ['ene', 'feb', 'mar', 'abr', 'may', 'jun',
                   'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];
    const day = date.getDate().toString().padStart(2, '0');
    const month = months[date.getMonth()];
    return `${day}-${month}`;
  }

  /**
   * Convierte etiqueta del backend a formato diario sin año
   */
  private convertToDisplayLabel(backendLabel: string): string {
    if (this.currentTimeGrouping === 'daily') {
      // Para vista diaria, remover el año
      const parsedDate = this.parseDateLabel(backendLabel);
      if (!isNaN(parsedDate.getTime())) {
        return this.formatDayLabel(parsedDate);
      }
      
      // Fallback: intentar remover año manualmente
      const match = backendLabel.match(/^(\d{1,2}-.*?)\.?-\d{2}$/);
      if (match) {
        return match[1].replace('.', '');
      }
    }
    
    // Para otras vistas, mantener el formato original
    return backendLabel;
  }

    /**
   * Parsea etiquetas de fecha en múltiples formatos - ACTUALIZADO PARA BACKEND FORMAT
   */
  private parseDateLabel(dateLabel: string): Date {
    // Caso 1: Formato ISO (YYYY-MM-DD)
    if (/^\d{4}-\d{2}-\d{2}$/.test(dateLabel)) {
      const parsedDate = new Date(dateLabel);
      return parsedDate;
    }
    
    // Caso 2: NUEVO - Formato del Backend "DD-MMM.-YY" (ej: "07-may.-25")
    const backendMatch = dateLabel.match(/^(\d{1,2})-(.*?)\.?-(\d{2})$/);
    if (backendMatch) {
      const day = parseInt(backendMatch[1]);
      const monthAbbr = backendMatch[2].toLowerCase().replace('.', ''); // Remover punto si existe
      const yearShort = parseInt(backendMatch[3]);
      
      // Mapeo de meses incluyendo español e inglés (sin puntos)
      const monthMap: { [key: string]: number } = {
        'jan': 0, 'ene': 0, 'enero': 0,
        'feb': 1, 'febrero': 1,
        'mar': 2, 'marzo': 2,
        'apr': 3, 'abr': 3, 'abril': 3,
        'may': 4, 'mayo': 4,
        'jun': 5, 'junio': 5,
        'jul': 6, 'julio': 6,
        'aug': 7, 'ago': 7, 'agosto': 7,
        'sep': 8, 'sept': 8, 'septiembre': 8,
        'oct': 9, 'octubre': 9,
        'nov': 10, 'noviembre': 10,
        'dec': 11, 'dic': 11, 'diciembre': 11
      };
      
      const month = monthMap[monthAbbr];
      
      // Conversión de año de 2 dígitos a 4 dígitos
      let year: number;
      if (yearShort >= 0 && yearShort <= 30) {
        // 00-30 = 2000-2030
        year = 2000 + yearShort;
      } else if (yearShort >= 31 && yearShort <= 99) {
        // 31-99 = 1931-1999
        year = 1900 + yearShort;
      } else {
        year = new Date().getFullYear(); // Fallback
      }
      
      if (month !== undefined && !isNaN(day) && !isNaN(year)) {
        const parsedDate = new Date(year, month, day);
        return parsedDate;
      }
    }
    
    // Caso 3: Formato DD-MMM clásico (ej: "15-May") - SIN AÑO
    const parts = dateLabel.split('-');
    if (parts.length === 2) {
      const day = parseInt(parts[0]);
      const monthAbbr = parts[1].toLowerCase().replace('.', '');
      
      const monthMap: { [key: string]: number } = {
        'jan': 0, 'ene': 0, 'enero': 0,
        'feb': 1, 'febrero': 1,
        'mar': 2, 'marzo': 2,
        'apr': 3, 'abr': 3, 'abril': 3,
        'may': 4, 'mayo': 4,
        'jun': 5, 'junio': 5,
        'jul': 6, 'julio': 6,
        'aug': 7, 'ago': 7, 'agosto': 7,
        'sep': 8, 'sept': 8, 'septiembre': 8,
        'oct': 9, 'octubre': 9,
        'nov': 10, 'noviembre': 10,
        'dec': 11, 'dic': 11, 'diciembre': 11
      };
      
      const month = monthMap[monthAbbr];
      
      // Usar año de filtros si no hay año en la fecha
      let year = new Date().getFullYear();
      if (this.filters.from) {
        const fromDate = new Date(this.filters.from);
        if (!isNaN(fromDate.getTime())) {
          year = fromDate.getFullYear();
        }
      }
      
      if (month !== undefined && !isNaN(day)) {
        const parsedDate = new Date(year, month, day);
        return parsedDate;
      }
    }
    
    // Caso 4: Intentar parsing directo
    const directParse = new Date(dateLabel);
    if (!isNaN(directParse.getTime())) {
      return directParse;
    }
    
    return new Date(); // Fallback a fecha actual
  }

  /**
   * Obtiene el mes de una fecha para asignar colores
   */
  private getMonthFromDate(dateLabel: string): number {
    try {
      let date: Date | undefined;
      
      // Caso 1: Formato ISO YYYY-MM-DD
      if (/^\d{4}-\d{2}-\d{2}$/.test(dateLabel)) {
        date = new Date(dateLabel);
      } 
      // Caso 2: Formato diario sin año "07-may"
      else if (/^\d{1,2}-[a-z]{3}$/i.test(dateLabel)) {
        const [day, monthAbbr] = dateLabel.split('-');
        const monthMap: { [key: string]: number } = {
          'jan': 0, 'ene': 0, 'enero': 0,
          'feb': 1, 'febrero': 1,
          'mar': 2, 'marzo': 2,
          'apr': 3, 'abr': 3, 'abril': 3,
          'may': 4, 'mayo': 4,
          'jun': 5, 'junio': 5,
          'jul': 6, 'julio': 6,
          'aug': 7, 'ago': 7, 'agosto': 7,
          'sep': 8, 'sept': 8, 'septiembre': 8,
          'oct': 9, 'octubre': 9,
          'nov': 10, 'noviembre': 10,
          'dec': 11, 'dic': 11, 'diciembre': 11
        };
        
        const month = monthMap[monthAbbr.toLowerCase()];
        if (month !== undefined) {
          return month;
        }
      }
      // Caso 3: Usar parser personalizado para otros formatos
      else {
        date = this.parseDateLabel(dateLabel);
      }
      
      if (date && !isNaN(date.getTime())) {
        return date.getMonth(); // 0-11
      }
    } catch (error) {
      // Error obteniendo mes de la fecha
    }
    
    return new Date().getMonth(); // Fallback al mes actual
  }

  /**
   * Obtiene los colores para el gráfico según el tipo de agrupación - AHORA CON COLORES POR MES EN TODAS LAS VISTAS
   */
  private getChartColors(groupedData: any[]): { background: string[], border: string[] } {
    // Paleta de colores por mes (0=Enero, 11=Diciembre)
    const monthColors = [
      'rgba(255, 87, 87, 0.8)',   // Enero - Rojo
      'rgba(255, 154, 0, 0.8)',   // Febrero - Naranja
      'rgba(255, 235, 59, 0.8)',  // Marzo - Amarillo
      'rgba(129, 199, 132, 0.8)', // Abril - Verde claro
      'rgba(34, 197, 94, 0.8)',   // Mayo - Verde
      'rgba(38, 166, 154, 0.8)',  // Junio - Teal
      'rgba(33, 150, 243, 0.8)',  // Julio - Azul
      'rgba(63, 81, 181, 0.8)',   // Agosto - Índigo
      'rgba(156, 39, 176, 0.8)',  // Septiembre - Púrpura
      'rgba(233, 30, 99, 0.8)',   // Octubre - Rosa
      'rgba(121, 85, 72, 0.8)',   // Noviembre - Marrón
      'rgba(96, 125, 139, 0.8)'   // Diciembre - Gris azul
    ];
    
    const monthBorderColors = [
      'rgba(255, 87, 87, 1)',   // Enero
      'rgba(255, 154, 0, 1)',   // Febrero
      'rgba(255, 235, 59, 1)',  // Marzo
      'rgba(129, 199, 132, 1)', // Abril
      'rgba(34, 197, 94, 1)',   // Mayo
      'rgba(38, 166, 154, 1)',  // Junio
      'rgba(33, 150, 243, 1)',  // Julio
      'rgba(63, 81, 181, 1)',   // Agosto
      'rgba(156, 39, 176, 1)',  // Septiembre
      'rgba(233, 30, 99, 1)',   // Octubre
      'rgba(121, 85, 72, 1)',   // Noviembre
      'rgba(96, 125, 139, 1)'   // Diciembre
    ];
    
    const backgrounds: string[] = [];
    const borders: string[] = [];
    
    groupedData.forEach(item => {
      let monthIndex = -1;
      
      if (this.currentTimeGrouping === 'monthly') {
        // Para vista mensual: extraer mes de la etiqueta "Ene 2024"
        const monthName = item.label.split(' ')[0];
        monthIndex = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun',
                     'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'].indexOf(monthName);
      } else if (this.currentTimeGrouping === 'weekly') {
        // Para vista semanal: obtener mes de la primera fecha de la semana
        if (item.weekStart) {
          monthIndex = item.weekStart.getMonth();
        } else {
          // Fallback: intentar extraer del label "Sem 15-05" -> mes 5 (Mayo)
          const weekMatch = item.label.match(/Sem \d+-(\d+)/);
          if (weekMatch) {
            monthIndex = parseInt(weekMatch[1]) - 1; // -1 porque getMonth() retorna 0-11
          }
        }
      } else if (this.currentTimeGrouping === 'daily') {
        // Para vista diaria: obtener mes de la etiqueta "07-may" (sin año)
        // Primero intentar parsear la etiqueta actual
        monthIndex = this.getMonthFromDate(item.label);
        
        // Si no funciona, buscar en los datos originales
        if (monthIndex === new Date().getMonth()) {
          // Buscar el item original correspondiente
          const originalItem = this.originalChartData.find(orig => 
            this.convertToDisplayLabel(orig.label) === item.label
          );
          if (originalItem) {
            monthIndex = this.getMonthFromDate(originalItem.label);
          }
        }
      }
      
      // Aplicar color según el mes
      if (monthIndex >= 0 && monthIndex < 12) {
        backgrounds.push(monthColors[monthIndex]);
        borders.push(monthBorderColors[monthIndex]);
      } else {
        // Fallback al verde si no se puede determinar el mes
        backgrounds.push('rgba(34, 197, 94, 0.8)');
        borders.push('rgba(34, 197, 94, 1)');
      }
    });
    
    return { background: backgrounds, border: borders };
  }

  /**
   * Actualiza el gráfico con datos agrupados y colores diferenciados
   */
  private updateChartWithGroupedData(groupedData: any[]): void {
    // Actualizar las etiquetas
    this.barChartLabels = groupedData.map(item => item.label);
    
    // Obtener colores según el tipo de agrupación
    const colors = this.getChartColors(groupedData);
    
    // Crear nueva estructura de datos para forzar la actualización
    this.barChartData = {
      labels: [...this.barChartLabels],
      datasets: [
        {
          data: groupedData.map(item => item.value),
          label: 'Ventas ($)',
          backgroundColor: colors.background,
          borderColor: colors.border,
          borderWidth: 1
        }
      ]
    };
  }

  /**
   * Obtiene la etiqueta descriptiva del tipo de agrupación actual
   */
  getGroupingLabel(): string {
    switch (this.currentTimeGrouping) {
      case 'daily': return 'Vista Diaria';
      case 'weekly': return 'Vista Semanal';
      case 'monthly': return 'Vista Mensual';
      default: return '';
    }
  }

  /**
   * Obtiene la fecha por defecto "desde" (primer día del mes actual)
   */
  private getDefaultFromDate(): string {
    const now = new Date();
    const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
    return firstDay.toISOString().split('T')[0];
  }
  
  /**
   * Obtiene la fecha por defecto "hasta" (día actual)
   */
  private getDefaultToDate(): string {
    const now = new Date();
    return now.toISOString().split('T')[0];
  }
  
  /**
   * Establece filtros rápidos predefinidos
   */
  setQuickFilter(period: 'today' | 'week' | 'month' | 'quarter' | 'year'): void {
    const now = new Date();
    
    switch (period) {
      case 'today':
        this.filters.from = now.toISOString().split('T')[0];
        this.filters.to = now.toISOString().split('T')[0];
        break;
      case 'week':
        const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
        this.filters.from = weekAgo.toISOString().split('T')[0];
        this.filters.to = now.toISOString().split('T')[0];
        break;
      case 'month':
        const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);
        this.filters.from = monthStart.toISOString().split('T')[0];
        this.filters.to = now.toISOString().split('T')[0];
        break;
      case 'quarter':
        const quarterStart = new Date(now.getFullYear(), Math.floor(now.getMonth() / 3) * 3, 1);
        this.filters.from = quarterStart.toISOString().split('T')[0];
        this.filters.to = now.toISOString().split('T')[0];
        break;
      case 'year':
        const yearStart = new Date(now.getFullYear(), 0, 1);
        this.filters.from = yearStart.toISOString().split('T')[0];
        this.filters.to = now.toISOString().split('T')[0];
        break;
    }
    
    this.applyFilters();
  }
  
  /**
   * Cambia la pestaña activa
   */
  setActiveTab(tab: 'ventas' | 'pagos' | 'productos' | 'clientes'): void {
    this.activeTab = tab;
  }

  // ============ MÉTODOS DE EXPORTACIÓN A PDF ============
  




  async exportToPDF(): Promise<void> {
    try {
      
      // Crear el PDF
      const pdf = new jsPDF('p', 'mm', 'a4');
      const pageWidth = pdf.internal.pageSize.getWidth();
      const pageHeight = pdf.internal.pageSize.getHeight();
      const margin = 15;
      let yPosition = margin;

      // Agregar encabezado del PDF
      pdf.setFontSize(20);
      pdf.setTextColor(79, 70, 229); // Color azul
      pdf.text('Dashboard de Administracion', margin, yPosition);
      yPosition += 10;

      pdf.setFontSize(10);
      pdf.setTextColor(100, 100, 100);
      const currentDate = new Date().toLocaleDateString('es-ES', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
      pdf.text(`Reporte generado el ${currentDate}`, margin, yPosition);
      yPosition += 5;
      pdf.text(`Período: ${this.formatDate(this.filters.from)} - ${this.formatDate(this.filters.to)}`, margin, yPosition);
      yPosition += 15;

      // Línea separadora
      pdf.setDrawColor(229, 231, 235);
      pdf.line(margin, yPosition, pageWidth - margin, yPosition);
      yPosition += 10;

      // Exportar solo la pestaña activa actual
      yPosition = await this.addSectionToPDF(pdf, this.activeTab, yPosition, pageWidth, pageHeight, margin);

      // Abrir el PDF en una nueva pestaña para previsualización
      const pdfBlob = pdf.output('bloburl');
      const newWindow = window.open(pdfBlob, '_blank');
      
      if (!newWindow) {
        // Fallback si se bloquean las ventanas emergentes
        alert('Tu navegador bloqueó la ventana emergente. Por favor, permite ventanas emergentes para este sitio.');
        // Como alternativa, descargar el archivo
        const fileName = `Dashboard_${this.getSectionTitle(this.activeTab)}_${new Date().toISOString().split('T')[0]}.pdf`;
        pdf.save(fileName);
      }

    } catch (error) {
      console.error('Error al generar el PDF:', error);
      alert('Error al generar el PDF. Por favor, inténtalo de nuevo.');
    }
  }



  private async addSectionToPDF(pdf: jsPDF, section: string, yPosition: number, pageWidth: number, pageHeight: number, margin: number): Promise<number> {
    // Si no hay espacio suficiente, crear nueva página
    if (yPosition > pageHeight - 60) {
      pdf.addPage();
      yPosition = margin;
    }

    // Agregar título de la sección
    pdf.setFontSize(16);
    pdf.setTextColor(55, 65, 81);
    pdf.text(this.getSectionTitle(section), margin, yPosition);
    yPosition += 15;

    // Agregar KPIs como texto plano
    yPosition = await this.addKPIsTextToPDF(pdf, section, yPosition, pageWidth, margin);
    
    // Agregar contenido visual (tablas/gráficos) como imagen
    yPosition = await this.addVisualContentToPDF(pdf, section, yPosition, pageWidth, pageHeight, margin);

    return yPosition + 15; // Espacio extra entre secciones
  }

  private async addKPIsTextToPDF(pdf: jsPDF, section: string, yPosition: number, pageWidth: number, margin: number): Promise<number> {
    pdf.setFontSize(12);
    pdf.setTextColor(79, 70, 229);

    switch (section) {
      case 'ventas':
        if (this.salesSummary) {
          pdf.text(`Total de Pedidos: ${this.formatNumber(this.salesSummary.totalOrders)}`, margin, yPosition);
          yPosition += 8;
          pdf.text(`Ingresos Totales: ${this.formatCurrency(this.salesSummary.totalRevenue)}`, margin, yPosition);
          yPosition += 8;
          pdf.text(`Promedio por Pedido: ${this.formatCurrency(this.salesSummary.averagePerOrder)}`, margin, yPosition);
          yPosition += 8;
          pdf.text(`Día con Más Ventas: ${this.salesSummary.bestSellingDay || this.getBestSellingDay() || 'N/A'}`, margin, yPosition);
          yPosition += 8;
        }
        break;
        
      case 'pagos':
        if (this.paymentSummary) {
          pdf.text(`Total de Pagos: ${this.formatNumber(this.paymentSummary.totalPagos)}`, margin, yPosition);
          yPosition += 8;
          pdf.text(`Monto Total Pagado: ${this.formatCurrency(this.paymentSummary.totalMontoPagado)}`, margin, yPosition);
          yPosition += 8;
          pdf.text(`Medio Más Usado: ${this.formatPaymentMethodName(this.paymentSummary.medioPagoMasUsado.metodo)} (${this.formatNumber(this.paymentSummary.medioPagoMasUsado.cantidad)} pagos)`, margin, yPosition);
          yPosition += 8;
        }
        break;
        
      case 'productos':
        if (this.productsSummary) {
          pdf.text(`Productos Únicos: ${this.formatNumber(this.productsSummary.totalProductosUnicos)}`, margin, yPosition);
          yPosition += 8;
          pdf.text(`Unidades Vendidas: ${this.formatNumber(this.productsSummary.totalUnidadesVendidas)}`, margin, yPosition);
          yPosition += 8;
          pdf.text(`Valor Total Ventas: ${this.formatCurrency(this.productsSummary.valorTotalVentas)}`, margin, yPosition);
          yPosition += 8;
        }
        break;
        
      case 'clientes':
        if (this.customersSummary) {
          pdf.text(`Total de Clientes: ${this.formatNumber(this.customersSummary.totalClientesUnicos)}`, margin, yPosition);
          yPosition += 8;
          pdf.text(`Total Ordenes: ${this.formatNumber(this.customersSummary.totalOrdenesProcesadas)}`, margin, yPosition);
          yPosition += 8;
          pdf.text(`Promedio Gastado por Cliente: ${this.formatCurrency(this.customersSummary.promedioGastadoPorCliente)}`, margin, yPosition);
          yPosition += 8;
        }
        break;
    }

    return yPosition + 10; // Espacio después de los KPIs
  }

  private async addVisualContentToPDF(pdf: jsPDF, section: string, yPosition: number, pageWidth: number, pageHeight: number, margin: number): Promise<number> {
    // Obtener el elemento DOM de la sección
    const sectionElement = document.querySelector(`[data-section="${section}"]`) as HTMLElement;
    if (!sectionElement) {
      console.warn(`No se encontró la sección ${section}`);
      return yPosition;
    }

    try {
      // Capturar solo las tablas/gráficos, no los KPIs
      let targetElement: HTMLElement;
      
      switch (section) {
        case 'ventas':
          // Capturar solo el gráfico
          targetElement = sectionElement.querySelector('canvas') as HTMLElement ||
                         sectionElement.querySelector('.bg-white') as HTMLElement ||
                         sectionElement;
          break;
        case 'pagos':
          // Capturar gráficos y tabla - usando un contenedor más amplio
          const pagosVisualContent = sectionElement.querySelector('.grid.grid-cols-1.lg\\:grid-cols-2') as HTMLElement;
          targetElement = pagosVisualContent || sectionElement;
          break;
        case 'productos':
          // Capturar solo la tabla
          targetElement = sectionElement.querySelector('.overflow-x-auto') as HTMLElement || 
                         sectionElement.querySelector('.bg-white:last-child') as HTMLElement ||
                         sectionElement;
          break;
        case 'clientes':
          // Capturar la tabla completa, asegurándose de obtener el ancho total
          const clientesTableContainer = sectionElement.querySelector('.bg-white:last-child') as HTMLElement;
          targetElement = clientesTableContainer || sectionElement;
          break;
        default:
          targetElement = sectionElement;
      }

      if (targetElement) {
        // Configuración especial para tablas anchas
        const isWideTable = section === 'clientes' || section === 'productos';
        
        const canvas = await html2canvas(targetElement, {
          backgroundColor: '#ffffff',
          scale: isWideTable ? 1.5 : 2, // Escala menor para tablas anchas
          useCORS: true,
          allowTaint: true,
          scrollX: 0,
          scrollY: 0,
          width: isWideTable ? targetElement.scrollWidth : undefined, // Usar ancho completo para tablas
          height: isWideTable ? targetElement.scrollHeight : undefined,
          x: 0,
          y: 0
        });

        const imgData = canvas.toDataURL('image/png');
        const imgWidth = pageWidth - (margin * 2);
        const imgHeight = (canvas.height * imgWidth) / canvas.width;

        // Si la imagen es muy alta para la página actual, crear nueva página
        if (yPosition + imgHeight > pageHeight - margin) {
          pdf.addPage();
          yPosition = margin;
        }

        // Agregar la imagen al PDF
        pdf.addImage(imgData, 'PNG', margin, yPosition, imgWidth, imgHeight);
        yPosition += imgHeight + 10;
        
        // Agregar datos tabulares debajo del gráfico
        yPosition = await this.addChartDataTable(pdf, section, yPosition, pageWidth, pageHeight, margin);
      }

    } catch (error) {
      console.error(`Error al capturar la sección ${section}:`, error);
      
      // Fallback: mensaje de error
      pdf.setFontSize(10);
      pdf.setTextColor(100, 100, 100);
      pdf.text('Error al capturar el contenido visual de esta sección.', margin, yPosition);
      yPosition += 15;
    }

    return yPosition;
  }

  private async addChartDataTable(pdf: jsPDF, section: string, yPosition: number, pageWidth: number, pageHeight: number, margin: number): Promise<number> {
    const lineHeight = 6;
    
    try {
      switch (section) {
        case 'ventas':
          if (this.salesSummary && this.barChartData.datasets[0].data.length > 0) {
            // Crear nueva página si no hay espacio
            if (yPosition > pageHeight - 80) {
              pdf.addPage();
              yPosition = margin;
            }
            
            pdf.setFontSize(12);
            pdf.setTextColor(79, 70, 229);
            pdf.text('Datos Detallados del Gráfico:', margin, yPosition);
            yPosition += 10;
            
            pdf.setFontSize(9);
            pdf.setTextColor(55, 65, 81);
            
            this.barChartLabels.forEach((label, index) => {
              const value = this.barChartData.datasets[0].data[index];
              // Buscar datos adicionales para mostrar pedidos realizados
              const originalData = this.originalChartData.find(item => 
                this.convertToDisplayLabel(item.label) === label || item.label === label
              );
              const pedidosCount = originalData ? originalData.count : 'N/A';
              
              const text = `• ${label}: ${this.formatCurrency(value as number)} (${pedidosCount} pedidos)`;
              pdf.text(text, margin + 5, yPosition);
              yPosition += lineHeight;
              
              // Nueva página si es necesario
              if (yPosition > pageHeight - margin) {
                pdf.addPage();
                yPosition = margin;
              }
            });
          }
          break;
          
        case 'pagos':
          if (this.paymentSummary) {
            // Crear nueva página si no hay espacio
            if (yPosition > pageHeight - 100) {
              pdf.addPage();
              yPosition = margin;
            }
            
            // Datos del gráfico de métodos de pago
            if (this.paymentSummary.resumenPorMetodo && this.paymentSummary.resumenPorMetodo.length > 0) {
              pdf.setFontSize(12);
              pdf.setTextColor(79, 70, 229);
              pdf.text('Métodos de Pago (Detalle):', margin, yPosition);
              yPosition += 10;
              
              pdf.setFontSize(9);
              pdf.setTextColor(55, 65, 81);
              
              this.paymentSummary.resumenPorMetodo.forEach(metodo => {
                const text = `• ${this.formatPaymentMethodName(metodo.metodo)}: ${this.formatNumber(metodo.cantidad)} pagos (${this.formatPercentage(metodo.porcentaje)}) - ${this.formatCurrency(metodo.montoTotal)}`;
                pdf.text(text, margin + 5, yPosition);
                yPosition += lineHeight;
                
                if (yPosition > pageHeight - margin) {
                  pdf.addPage();
                  yPosition = margin;
                }
              });
              yPosition += 5;
            }
            
            // Datos del gráfico de estados de pago
            if (this.paymentSummary.resumenPorEstado && this.paymentSummary.resumenPorEstado.length > 0) {
              pdf.setFontSize(12);
              pdf.setTextColor(79, 70, 229);
              pdf.text('Estados de Pago (Detalle):', margin, yPosition);
              yPosition += 10;
              
              pdf.setFontSize(9);
              pdf.setTextColor(55, 65, 81);
              
                             this.paymentSummary.resumenPorEstado.forEach(estado => {
                 const text = `• ${this.formatPaymentStateName(estado.estado)}: ${this.formatNumber(estado.cantidad)} pagos (${this.formatPercentage(estado.porcentaje)})`;
                 pdf.text(text, margin + 5, yPosition);
                 yPosition += lineHeight;
                
                if (yPosition > pageHeight - margin) {
                  pdf.addPage();
                  yPosition = margin;
                }
              });
            }
          }
          break;
          
                case 'productos':
          // No agregar resumen adicional para productos, la tabla ya está en la imagen
          break;
          
        case 'clientes':
          if (this.customersSummary && this.customersSummary.topClientes && this.customersSummary.topClientes.length > 0) {
            // Crear nueva página si no hay espacio
            if (yPosition > pageHeight - 60) {
              pdf.addPage();
              yPosition = margin;
            }
            
            pdf.setFontSize(12);
            pdf.setTextColor(79, 70, 229);
            pdf.text('Resumen de Clientes:', margin, yPosition);
            yPosition += 10;
            
            pdf.setFontSize(9);
            pdf.setTextColor(55, 65, 81);
            
            this.customersSummary.topClientes.slice(0, 10).forEach((cliente, index) => {
              const text = `${index + 1}. ${cliente.nombreCompleto} - Órdenes: ${this.formatNumber(cliente.cantidadOrdenes)} - Total: ${this.formatCurrency(cliente.dineroTotalGastado)}`;
              pdf.text(text, margin + 5, yPosition);
              yPosition += lineHeight;
              
              if (yPosition > pageHeight - margin) {
                pdf.addPage();
                yPosition = margin;
              }
            });
          }
          break;
      }
      
    } catch (error) {
      console.error(`Error agregando datos tabulares para ${section}:`, error);
    }
    
    return yPosition + 10; // Espacio extra después de los datos
  }

  getSectionTitle(section: string): string {
    switch (section) {
      case 'ventas': return 'Resumen de Ventas';
      case 'pagos': return 'Medios de Pago';
      case 'productos': return 'Productos Más Vendidos';
      case 'clientes': return 'Clientes Más Frecuentes';
      default: return 'Sección';
    }
  }

} 