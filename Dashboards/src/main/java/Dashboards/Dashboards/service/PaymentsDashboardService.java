package Dashboards.Dashboards.service;

import java.time.LocalDate;

import Dashboards.Dashboards.model.dto.PaymentsSummaryDto;

public interface PaymentsDashboardService {
    
    /**
     * Obtiene el resumen de pagos para un rango de fechas específico
     * @param from fecha de inicio (formato: YYYY-MM-DD)
     * @param to fecha de fin (formato: YYYY-MM-DD)
     * @param authToken token JWT para autenticación con el microservicio
     * @return resumen de pagos con estadísticas y análisis por método y estado
     */
    PaymentsSummaryDto getPaymentsSummary(LocalDate from, LocalDate to, String authToken);
} 