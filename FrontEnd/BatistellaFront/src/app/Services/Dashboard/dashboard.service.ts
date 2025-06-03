import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SalesSummary, DashboardFilters } from '../../Models/dashboard';
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