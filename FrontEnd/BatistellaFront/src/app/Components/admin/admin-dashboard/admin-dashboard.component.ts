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
  
  // Control de impresión
  showPrintModal = false;
  printOptions = {
    ventas: true,
    pagos: true,
    productos: true,
    clientes: true
  };
  
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
  
  // Configuración del gráfico de barras (ventas)
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
            const dataIndex = context.dataIndex;
            const chartData = this.salesSummary?.chartData[dataIndex];
            const value = context.parsed.y;
            const count = chartData?.count || 0;
            
            return [
              `Ingresos: $${value.toLocaleString()}`,
              `Cantidad vendida: ${count} productos`
            ];
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
    onClick: (event: any, elements: any[]) => {
      if (elements && elements.length > 0) {
        const dataIndex = elements[0].index;
        const clickedDate = this.salesSummary?.chartData[dataIndex]?.label;
        if (clickedDate) {
          this.navigateToOrdersWithDate(clickedDate);
        }
      }
    }
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
        console.error('Error al cargar el resumen de ventas:', error);
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
    
    console.log('🔍 Cargando resumen de pagos con filtros:', this.filters);
    
    this.dashboardService.getPaymentSummary(this.filters).subscribe({
      next: (data: PaymentSummary) => {
        console.log('✅ Datos de pagos recibidos:', data);
        console.log('💰 Total monto pagado:', data.totalMontoPagado);
        console.log('📊 Resumen por método:', data.resumenPorMetodo);
        
        this.paymentSummary = data;
        this.updatePaymentCharts(data);
        this.isLoadingPayments = false;
      },
      error: (error) => {
        console.error('❌ Error al cargar el resumen de medios de pago:', error);
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
    
    console.log('🔍 Cargando resumen de productos más vendidos con filtros:', this.filters);
    
    this.dashboardService.getProductsSummary(this.filters).subscribe({
             next: (data: ProductsSummary) => {
         console.log('✅ Datos de productos más vendidos recibidos:', data);
         console.log('📊 Top productos:', data.topProductos);
         
         this.productsSummary = data;
         this.isLoadingProducts = false;
       },
      error: (error) => {
        console.error('❌ Error al cargar el resumen de productos más vendidos:', error);
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
    
    console.log('🔍 Cargando resumen de clientes más frecuentes con filtros:', this.filters);
    
    this.dashboardService.getCustomersSummary(this.filters).subscribe({
      next: (data: CustomersSummary) => {
        console.log('✅ Datos de clientes más frecuentes recibidos:', data);
        console.log('📊 Top clientes:', data.topClientes);
        
        this.customersSummary = data;
        this.isLoadingCustomers = false;
      },
      error: (error) => {
        console.error('❌ Error al cargar el resumen de clientes más frecuentes:', error);
        this.handleCustomersError(error);
        this.isLoadingCustomers = false;
      }
    });
  }
  
  /**
   * Actualiza los datos del gráfico de ventas
   */
  private updateChart(data: SalesSummary): void {
    this.barChartLabels = data.chartData.map(item => item.label);
    this.barChartData = {
      labels: this.barChartLabels,
      datasets: [
        {
          data: data.chartData.map(item => item.value),
          label: 'Ventas ($)',
          backgroundColor: 'rgba(34, 197, 94, 0.8)',
          borderColor: 'rgba(34, 197, 94, 1)',
          borderWidth: 1
        }
      ]
    };
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
    // Convertir el formato de fecha del dashboard (ej: "09-May") a formato DD/MM/YYYY
    const convertedDate = this.convertDateLabelToDateFormat(dateLabel);
    
    // Navegar a admin/pedidos con el parámetro de fecha
    this.router.navigate(['/admin/pedidos'], { 
      queryParams: { 
        fecha: convertedDate,
        fromDashboard: 'true'
      } 
    });
  }

  /**
   * Convierte el formato de fecha del dashboard a formato DD/MM/YYYY
   */
  private convertDateLabelToDateFormat(dateLabel: string): string {
    try {
      // Si el formato es "DD-MMM" (ej: "09-May")
      const parts = dateLabel.split('-');
      if (parts.length === 2) {
        const day = parts[0].padStart(2, '0');
        const monthAbbr = parts[1];
        
        // Mapeo de abreviaciones de meses en inglés a números
        const monthMap: { [key: string]: string } = {
          'Jan': '01', 'Feb': '02', 'Mar': '03', 'Apr': '04',
          'May': '05', 'Jun': '06', 'Jul': '07', 'Aug': '08',
          'Sep': '09', 'Oct': '10', 'Nov': '11', 'Dec': '12'
        };
        
        const month = monthMap[monthAbbr] || '01';
        const currentYear = new Date().getFullYear();
        
        // Retornar en formato YYYY-MM-DD para el input date
        return `${currentYear}-${month}-${day}`;
      }
      
      // Si ya viene en formato correcto, devolverlo tal como está
      return dateLabel;
    } catch (error) {
      console.error('Error convirtiendo fecha:', error);
      return new Date().toISOString().split('T')[0]; // Fecha actual como fallback
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
  
  openPrintModal(): void {
    this.showPrintModal = true;
  }

  closePrintModal(): void {
    this.showPrintModal = false;
  }

  selectAllSections(): void {
    this.printOptions = {
      ventas: true,
      pagos: true,
      productos: true,
      clientes: true
    };
  }

  deselectAllSections(): void {
    this.printOptions = {
      ventas: false,
      pagos: false,
      productos: false,
      clientes: false
    };
  }

  async exportToPDF(): Promise<void> {
    const sectionsSelected = Object.values(this.printOptions).some(selected => selected);
    
    if (!sectionsSelected) {
      alert('Por favor selecciona al menos una sección para exportar.');
      return;
    }

    try {
      // Mostrar indicador de carga
      this.closePrintModal();
      
      // Crear el PDF
      const pdf = new jsPDF('p', 'mm', 'a4');
      const pageWidth = pdf.internal.pageSize.getWidth();
      const pageHeight = pdf.internal.pageSize.getHeight();
      const margin = 15;
      let yPosition = margin;

      // Agregar encabezado del PDF
      pdf.setFontSize(20);
      pdf.setTextColor(79, 70, 229); // Color azul
      pdf.text('📊 Dashboard de Administración', margin, yPosition);
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

      // Exportar cada sección seleccionada
      if (this.printOptions.ventas && this.salesSummary) {
        yPosition = await this.addSectionToPDF(pdf, 'ventas', yPosition, pageWidth, pageHeight, margin);
      }

      if (this.printOptions.pagos && this.paymentSummary) {
        yPosition = await this.addSectionToPDF(pdf, 'pagos', yPosition, pageWidth, pageHeight, margin);
      }

      if (this.printOptions.productos && this.productsSummary) {
        yPosition = await this.addSectionToPDF(pdf, 'productos', yPosition, pageWidth, pageHeight, margin);
      }

      if (this.printOptions.clientes && this.customersSummary) {
        yPosition = await this.addSectionToPDF(pdf, 'clientes', yPosition, pageWidth, pageHeight, margin);
      }

      // Guardar el PDF
      const fileName = `Dashboard_${new Date().toISOString().split('T')[0]}.pdf`;
      pdf.save(fileName);

    } catch (error) {
      console.error('Error al generar el PDF:', error);
      alert('Error al generar el PDF. Por favor, inténtalo de nuevo.');
    }
  }

  private async addSectionToPDF(pdf: jsPDF, section: string, yPosition: number, pageWidth: number, pageHeight: number, margin: number): Promise<number> {
    // Si no hay espacio suficiente, crear nueva página
    if (yPosition > pageHeight - 50) {
      pdf.addPage();
      yPosition = margin;
    }

    // Obtener el elemento DOM de la sección
    const sectionElement = document.querySelector(`[data-section="${section}"]`) as HTMLElement;
    if (!sectionElement) {
      console.warn(`No se encontró la sección ${section}`);
      return yPosition;
    }

    try {
      // Capturar la sección como imagen
      const canvas = await html2canvas(sectionElement, {
        backgroundColor: '#ffffff',
        scale: 2,
        useCORS: true,
        allowTaint: true,
        scrollX: 0,
        scrollY: 0
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

    } catch (error) {
      console.error(`Error al capturar la sección ${section}:`, error);
      
      // Fallback: agregar texto simple
      pdf.setFontSize(14);
      pdf.setTextColor(55, 65, 81);
      pdf.text(`📊 ${this.getSectionTitle(section)}`, margin, yPosition);
      yPosition += 10;
      
      pdf.setFontSize(10);
      pdf.setTextColor(100, 100, 100);
      pdf.text('Error al capturar el contenido visual de esta sección.', margin, yPosition);
      yPosition += 15;
    }

    return yPosition;
  }

  private getSectionTitle(section: string): string {
    switch (section) {
      case 'ventas': return 'Resumen de Ventas';
      case 'pagos': return 'Medios de Pago';
      case 'productos': return 'Productos Más Vendidos';
      case 'clientes': return 'Clientes Más Frecuentes';
      default: return 'Sección';
    }
  }


} 