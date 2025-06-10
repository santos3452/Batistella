import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SalesSummary, DashboardFilters, PaymentSummary, ProductsSummary, CustomersSummary } from '../../Models/dashboard';
import { AuthService } from '../Auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private baseUrl = environment.dashboardUrl;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  /**
   * Obtiene el resumen de ventas para un período específico
   */
  getSalesSummary(filters: DashboardFilters): Observable<SalesSummary> {
    const headers = this.getAuthHeaders();
    
    let params = new HttpParams()
      .set('from', filters.from)
      .set('to', filters.to);

    return this.http.get<SalesSummary>(`${this.baseUrl}/sales-summary`, { 
      headers, 
      params 
    });
  }

  /**
   * Obtiene el resumen de medios de pago para un período específico
   */
  getPaymentSummary(filters: DashboardFilters): Observable<PaymentSummary> {
    const headers = this.getAuthHeaders();
    
    let params = new HttpParams()
      .set('from', filters.from)
      .set('to', filters.to);

    return this.http.get<PaymentSummary>(`${this.baseUrl}/payments-summary`, { 
      headers, 
      params 
    });
  }

  /**
   * Obtiene el resumen de productos más vendidos para un período específico
   */
  getProductsSummary(filters: DashboardFilters): Observable<ProductsSummary> {
    const headers = this.getAuthHeaders();
    
    let params = new HttpParams()
      .set('from', filters.from)
      .set('to', filters.to);

    return this.http.get<ProductsSummary>(`${this.baseUrl}/top-products`, { 
      headers, 
      params 
    });
  }

  /**
   * Obtiene el resumen de clientes más frecuentes para un período específico
   */
  getCustomersSummary(filters: DashboardFilters): Observable<CustomersSummary> {
    const headers = this.getAuthHeaders();
    
    let params = new HttpParams()
      .set('from', filters.from)
      .set('to', filters.to);

    return this.http.get<CustomersSummary>(`${this.baseUrl}/top-customers`, { 
      headers, 
      params 
    });
  }

  /**
   * Obtiene los headers de autenticación
   */
  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.userToken;
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }
} 