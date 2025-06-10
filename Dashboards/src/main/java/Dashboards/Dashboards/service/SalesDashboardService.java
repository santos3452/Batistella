package Dashboards.Dashboards.service;

import java.time.LocalDate;

import Dashboards.Dashboards.model.dto.SalesSummaryDto;
import Dashboards.Dashboards.model.dto.TopCustomersSummaryDto;
import Dashboards.Dashboards.model.dto.TopProductsSummaryDto;

public interface SalesDashboardService {
    
    /**
     * Obtiene el resumen de ventas para un rango de fechas específico
     * @param from fecha de inicio (formato: YYYY-MM-DD)
     * @param to fecha de fin (formato: YYYY-MM-DD)
     * @param authToken token JWT para autenticación con el microservicio
     * @return resumen de ventas con indicadores y datos para gráficos
     */
    SalesSummaryDto getSalesSummary(LocalDate from, LocalDate to, String authToken);
    
    /**
     * Obtiene el ranking de productos más vendidos para un rango de fechas específico
     * @param from fecha de inicio (formato: YYYY-MM-DD)
     * @param to fecha de fin (formato: YYYY-MM-DD)
     * @param authToken token JWT para autenticación con el microservicio
     * @return ranking de productos ordenados por cantidad vendida
     */
    TopProductsSummaryDto getTopProducts(LocalDate from, LocalDate to, String authToken);
    
    /**
     * Obtiene el ranking de clientes más frecuentes para un rango de fechas específico
     * @param from fecha de inicio (formato: YYYY-MM-DD)
     * @param to fecha de fin (formato: YYYY-MM-DD)
     * @param authToken token JWT para autenticación con el microservicio
     * @return ranking de clientes ordenados por dinero gastado
     */
    TopCustomersSummaryDto getTopCustomers(LocalDate from, LocalDate to, String authToken);
} 