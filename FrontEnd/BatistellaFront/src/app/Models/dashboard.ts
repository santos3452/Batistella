export interface ChartDataPoint {
  label: string;
  value: number;
  count: number;
}

export interface SalesSummary {
  totalOrders: number;
  totalRevenue: number;
  averagePerOrder: number;
  bestSellingDay: string;
  chartData: ChartDataPoint[];
}

export interface DashboardFilters {
  from: string; // Formato: YYYY-MM-DD
  to: string;   // Formato: YYYY-MM-DD
}

// Nuevas interfaces para medios de pago
export interface ResumenPorMetodo {
  metodo: string;
  cantidad: number;
  porcentaje: number;
  montoTotal: number;
}

export interface ResumenPorEstado {
  estado: 'COMPLETADO' | 'CANCELADO' | 'PENDIENTE' | 'EN_PROCESO';
  cantidad: number;
  porcentaje: number;
}

export interface MedioPagoMasUsado {
  metodo: string;
  cantidad: number;
  porcentaje: number;
}

export interface FiltrosAplicados {
  fechaDesde: string;
  fechaHasta: string;
}

export interface PaymentSummary {
  filtrosAplicados: FiltrosAplicados;
  totalPagos: number;
  totalMontoPagado: number;
  medioPagoMasUsado: MedioPagoMasUsado;
  resumenPorMetodo: ResumenPorMetodo[];
  resumenPorEstado: ResumenPorEstado[];
}

// Nuevas interfaces para productos más vendidos
export interface TopProduct {
  productoId: number;
  nombreProducto: string;
  cantidadTotalVendida: number;
  numeroPedidos: number;
  ingresosTotales: number;
  precioUnitarioPromedio: number;
}

export interface ProductsSummary {
  filtrosAplicados: FiltrosAplicados;
  totalProductosUnicos: number;
  totalUnidadesVendidas: number;
  valorTotalVentas: number;
  topProductos: TopProduct[];
}

// Nuevas interfaces para clientes más frecuentes
export interface TopCustomer {
  usuarioId: number;
  nombreCompleto: string;
  email: string;
  cantidadOrdenes: number;
  cantidadProductosComprados: number;
  dineroTotalGastado: number;
  promedioGastadoPorOrden: number;
  primeraCompra: string; // Formato: YYYY-MM-DD
  ultimaCompra: string;  // Formato: YYYY-MM-DD
}

export interface CustomersSummary {
  filtrosAplicados: FiltrosAplicados;
  totalClientesUnicos: number;
  totalOrdenesProcesadas: number;
  ingresosTotales: number;
  promedioOrdenesPorCliente: number;
  promedioGastadoPorCliente: number;
  topClientes: TopCustomer[];
} 