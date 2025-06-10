package Dashboards.Dashboards.model.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumen completo de clientes más frecuentes")
public class TopCustomersSummaryDto {
    
    @Schema(description = "Filtros aplicados en la consulta")
    private PaymentFilterDto filtrosAplicados;
    
    @Schema(description = "Número total de clientes únicos", example = "45")
    private Integer totalClientesUnicos;
    
    @Schema(description = "Número total de órdenes procesadas", example = "230")
    private Integer totalOrdenesProcesadas;
    
    @Schema(description = "Ingresos totales de todos los clientes", example = "1250000.00")
    private Double ingresosTotales;
    
    @Schema(description = "Promedio de órdenes por cliente", example = "5.11")
    private Double promedioOrdenesPorCliente;
    
    @Schema(description = "Promedio gastado por cliente", example = "27777.78")
    private Double promedioGastadoPorCliente;
    
    @Schema(description = "Lista de clientes ordenados por dinero gastado (mayor a menor)")
    private List<TopCustomerDto> topClientes;
} 