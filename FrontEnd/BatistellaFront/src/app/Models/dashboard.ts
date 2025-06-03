export interface ChartDataPoint {
  label: string;
  value: number;
}

export interface SalesSummary {
  totalOrders: number;
  totalRevenue: number;
  averagePerOrder: number;
  chartData: ChartDataPoint[];
}

export interface DashboardFilters {
  from: string; // Formato: YYYY-MM-DD
  to: string;   // Formato: YYYY-MM-DD
} 