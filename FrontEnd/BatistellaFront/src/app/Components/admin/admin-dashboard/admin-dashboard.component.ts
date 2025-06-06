import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions, ChartType } from 'chart.js';
import { DashboardService } from '../../../Services/Dashboard/dashboard.service';
import { SalesSummary, DashboardFilters } from '../../../Models/dashboard';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, BaseChartDirective]
})
export class AdminDashboardComponent implements OnInit {
  isLoading = false;
  errorMessage: string | null = null;
  
  // Datos del dashboard
  salesSummary: SalesSummary | null = null;
  
  // Filtros de fecha
  filters: DashboardFilters = {
    from: this.getDefaultFromDate(),
    to: this.getDefaultToDate()
  };
  
  // Configuración del gráfico de barras
  public barChartOptions: ChartOptions = {
    responsive: true,
    plugins: {
      title: {
        display: true,
        text: 'Ventas por Período'
      },
      legend: {
        display: false
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
  
  constructor(
    private dashboardService: DashboardService
  ) {}
  
  ngOnInit(): void {
    this.loadSalesSummary();
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
   * Actualiza los datos del gráfico
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
   * Aplica los filtros y recarga los datos
   */
  applyFilters(): void {
    if (this.validateDateRange()) {
      this.loadSalesSummary();
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
    return true;
  }
  
  /**
   * Maneja errores de la API
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
} 