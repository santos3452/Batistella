package Notifications.Notifications.dto;

import java.io.Serializable;

/**
 * DTO para representar un producto dentro de un pedido
 */
public class ProductoPedidoDTO implements Serializable {
    
    private Long id;
    private Long productoId;
    private String nombreProducto;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
    
    public ProductoPedidoDTO() {
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProductoId() {
        return productoId;
    }
    
    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }
    
    public String getNombreProducto() {
        return nombreProducto;
    }
    
    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }
    
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
    
    public Double getPrecioUnitario() {
        return precioUnitario;
    }
    
    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
    
    public Double getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
} 