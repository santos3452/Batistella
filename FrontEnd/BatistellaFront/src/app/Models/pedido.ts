export interface ProductoPedido {
  id: number;
  productoId: number;
  nombreProducto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface Pedido {
  id: number;
  usuarioId: number;
  fechaPedido: string;
  estado: string;
  total: number;
  productos: ProductoPedido[];
  createdAt: string;
  updatedAt: string;
} 