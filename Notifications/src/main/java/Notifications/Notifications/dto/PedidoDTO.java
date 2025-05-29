package Notifications.Notifications.dto;

import java.io.Serializable;
import java.util.List;

/**
 * DTO para representar un pedido completo
 */
public class PedidoDTO implements Serializable {
    
    private Long id;
    private String codigoPedido;
    private Long usuarioId;
    private String nombreCompletoUsuario;
    private String fechaPedido;
    private String estado;
    private Double total;
    private String domicilio;
    private List<ProductoPedidoDTO> productos;
    private String estadoPago;
    private String metodoPago;
    private String fechaPago;
    private String createdAt;
    private String updatedAt;
    
    public PedidoDTO() {
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCodigoPedido() {
        return codigoPedido;
    }
    
    public void setCodigoPedido(String codigoPedido) {
        this.codigoPedido = codigoPedido;
    }
    
    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public String getNombreCompletoUsuario() {
        return nombreCompletoUsuario;
    }
    
    public void setNombreCompletoUsuario(String nombreCompletoUsuario) {
        this.nombreCompletoUsuario = nombreCompletoUsuario;
    }
    
    public String getFechaPedido() {
        return fechaPedido;
    }
    
    public void setFechaPedido(String fechaPedido) {
        this.fechaPedido = fechaPedido;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public Double getTotal() {
        return total;
    }
    
    public void setTotal(Double total) {
        this.total = total;
    }
    
    public String getDomicilio() {
        return domicilio;
    }
    
    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }
    
    public List<ProductoPedidoDTO> getProductos() {
        return productos;
    }
    
    public void setProductos(List<ProductoPedidoDTO> productos) {
        this.productos = productos;
    }
    
    public String getEstadoPago() {
        return estadoPago;
    }
    
    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }
    
    public String getMetodoPago() {
        return metodoPago;
    }
    
    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
    
    public String getFechaPago() {
        return fechaPago;
    }
    
    public void setFechaPago(String fechaPago) {
        this.fechaPago = fechaPago;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
} 