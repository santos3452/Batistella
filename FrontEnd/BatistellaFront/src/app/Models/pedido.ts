export interface ProductoPedido {
  id: number;
  productoId: number;
  nombreProducto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  kg?: number; // Kilos del producto
}

export interface Pedido {
  id: number;
  codigoPedido?: string;
  usuarioId: number;
  email?: string;
  fechaPedido: string;
  estado: string;
  total: number;
  costoEnvio?: number;
  productos: ProductoPedido[];
  createdAt: string;
  updatedAt: string;
  domicilio?: string;
  metodoPago?: string;
} 