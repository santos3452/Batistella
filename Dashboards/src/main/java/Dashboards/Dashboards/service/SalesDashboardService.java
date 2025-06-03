package Dashboards.Dashboards.service;

import java.time.LocalDate;

import Dashboards.Dashboards.model.dto.SalesSummaryDto;

public interface SalesDashboardService {
    
    /**
     * Obtiene el resumen de ventas para un rango de fechas específico
     * @param from fecha de inicio (formato: YYYY-MM-DD)
     * @param to fecha de fin (formato: YYYY-MM-DD)
     * @param authToken token JWT para autenticación con el microservicio
     * @return resumen de ventas con indicadores y datos para gráficos
     */
    SalesSummaryDto getSalesSummary(LocalDate from, LocalDate to, String authToken);
} 