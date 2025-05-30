import { Injectable } from '@angular/core';
import { Pedido } from '../../Models/pedido';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

interface PedidoAgrupadoPorBarrio {
  barrio: string;
  pedidos: any[];
  total: number;
  pedidosPagados: number;
  porcentajePagados: number;
}

@Injectable({
  providedIn: 'root'
})
export class PrintService {

  constructor() { }

  /**
   * Genera un PDF con la lista de pedidos agrupados por barrio
   * @param pedidos Lista de pedidos a imprimir
   * @param titulo Título del documento
   */
  generarPDFPedidos(pedidos: any[], titulo: string = 'Listado de Pedidos'): void {
    // Crear un nuevo documento PDF
    const doc = new jsPDF();
    const fechaActual = new Date().toLocaleDateString('es-AR');
    
    // Título y encabezado
    doc.setFontSize(18);
    doc.setTextColor(4, 120, 87); // Color emerald-800
    doc.text(titulo, doc.internal.pageSize.getWidth() / 2, 20, { align: 'center' });
    
    // Información adicional
    doc.setFontSize(10);
    doc.setTextColor(0);
    doc.text(`Fecha de impresión: ${fechaActual}`, doc.internal.pageSize.getWidth() - 15, 30, { align: 'right' });
    doc.setFontSize(11);
    doc.text(`Total de pedidos: ${pedidos.length}`, 15, 40);
    
    // Agrupar pedidos por barrio
    const pedidosAgrupados = this.agruparPedidosPorBarrio(pedidos);
    
    // Para cada barrio, generar una sección con sus pedidos en una nueva página (excepto el primero)
    pedidosAgrupados.forEach((grupo, index) => {
      // Para todos los barrios excepto el primero, crear una nueva página
      if (index > 0) {
        doc.addPage();
      }
      
      // Posición vertical para el encabezado del barrio
      let posY = 50;
      
      // Si no es el primer barrio, dibujar encabezado de página
      if (index > 0) {
        // Título y encabezado en la nueva página
        doc.setFontSize(14);
        doc.setTextColor(16, 185, 129); // Color emerald-600
        doc.text('Batistella', doc.internal.pageSize.getWidth() - 15, 15, { align: 'right' });
        
        // Información de continuación
        doc.setFontSize(10);
        doc.setTextColor(0);
        doc.text(`Fecha de impresión: ${fechaActual}`, doc.internal.pageSize.getWidth() - 15, 25, { align: 'right' });
        
        // Posición Y ajustada para el encabezado del barrio
        posY = 35;
      }
      
      // Encabezado del barrio
      doc.setFontSize(14);
      doc.setTextColor(4, 120, 87); // Color emerald-800
      doc.text(`BARRIO: ${grupo.barrio}`, 15, posY);
      doc.setFontSize(12);
      doc.text(`Cantidad de pedidos: ${grupo.pedidos.length}`, 15, posY + 8);
      
      // Preparar los datos para la tabla de este barrio
      const tableColumn = ['Código', 'Cliente', 'Dirección', 'Productos', 'Pagado', 'Total'];
      const tableRows = grupo.pedidos.map(pedido => {
        // Crear una lista de productos para mostrar
        const productosTexto = pedido.productos
          .map((p: any) => `${p.cantidad} x ${p.nombreProducto}`)
          .join('\n');
        
        // Determinar si está pagado (usando emojis o símbolos)
        const pagado = pedido.estadoPago === 'COMPLETADO' ? '✓ SI' : '✗ NO';
        
        return [
          pedido.codigoPedido || pedido.id || '-',
          pedido.nombreCompletoUsuario || '-',
          pedido.domicilio || '-',
          productosTexto,
          pagado,
          `$${this.formatearNumero(pedido.total)}`
        ];
      });
      
      // Crear la tabla para este barrio
      autoTable(doc, {
        head: [tableColumn],
        body: tableRows,
        startY: posY + 12,
        styles: {
          fontSize: 8,
          cellPadding: 3,
          lineColor: [221, 221, 221],
          lineWidth: 0.1,
        },
        headStyles: {
          fillColor: [240, 253, 244], // Color emerald-50
          textColor: [4, 120, 87], // Color emerald-800
          fontStyle: 'bold',
          halign: 'center'
        },
        columnStyles: {
          0: { cellWidth: 25 },
          1: { cellWidth: 35 },
          2: { cellWidth: 40 },
          3: { cellWidth: 'auto' },
          4: { cellWidth: 20, halign: 'center' },
          5: { cellWidth: 25, halign: 'right' }
        },
        alternateRowStyles: {
          fillColor: [249, 250, 251], // Color gray-50
        },
        // Configuración del pie de página
        didDrawPage: (data: any) => {
          // Añadir número de página al pie
          doc.setFontSize(10);
          const pageNumber = (doc as any).internal.getNumberOfPages();
          doc.text(`Página ${pageNumber}`, 
            doc.internal.pageSize.getWidth() / 2, 
            doc.internal.pageSize.getHeight() - 10, 
            { align: 'center' }
          );
        },
        margin: { top: 45 }
      });
    });
    
    // Añadir resumen de pedidos por barrio
    doc.addPage();
    doc.setFontSize(16);
    doc.setTextColor(4, 120, 87); // Color emerald-800
    doc.text('RESUMEN POR BARRIO', doc.internal.pageSize.getWidth() / 2, 20, { align: 'center' });
    
    // Información de continuación
    doc.setFontSize(10);
    doc.setTextColor(0);
    doc.text(`Fecha de impresión: ${fechaActual}`, doc.internal.pageSize.getWidth() - 15, 30, { align: 'right' });
    
    // Preparar datos para la tabla de resumen
    const resumenColumns = ['Barrio', 'Cantidad Pedidos', 'Pagados', '% Pagados', 'Total $'];
    const resumenRows = pedidosAgrupados.map(grupo => {
      // Formatear el porcentaje con un indicador visual de texto
      const porcentajeTexto = this.formatearPorcentaje(grupo.porcentajePagados);
      
      return [
        grupo.barrio,
        grupo.pedidos.length.toString(),
        grupo.pedidosPagados.toString(),
        porcentajeTexto,
        `$${this.formatearNumero(grupo.total)}`
      ];
    });
    
    // Añadir fila de totales
    const totalPedidos = pedidosAgrupados.reduce((sum, grupo) => sum + grupo.pedidos.length, 0);
    const totalPedidosPagados = pedidosAgrupados.reduce((sum, grupo) => sum + grupo.pedidosPagados, 0);
    const porcentajeTotal = totalPedidos > 0 ? Math.round((totalPedidosPagados / totalPedidos) * 100) : 0;
    const montoTotal = pedidosAgrupados.reduce((sum, grupo) => sum + grupo.total, 0);
    
    // Crear tabla de resumen con tipos correctos
    autoTable(doc, {
      head: [resumenColumns],
      body: [
        ...resumenRows,
        [
          'TOTAL',
          totalPedidos.toString(),
          totalPedidosPagados.toString(),
          this.formatearPorcentaje(porcentajeTotal),
          `$${this.formatearNumero(montoTotal)}`
        ]
      ],
      startY: 40,
      styles: {
        fontSize: 10,
        cellPadding: 4,
      },
      headStyles: {
        fillColor: [240, 253, 244], // Color emerald-50
        textColor: [4, 120, 87], // Color emerald-800
        fontStyle: 'bold',
        halign: 'center'
      },
      columnStyles: {
        0: { cellWidth: 'auto' },
        1: { cellWidth: 50, halign: 'center' },
        2: { cellWidth: 35, halign: 'center' },
        3: { cellWidth: 35, halign: 'center' },
        4: { cellWidth: 50, halign: 'right' }
      },
      alternateRowStyles: {
        fillColor: [249, 250, 251], // Color gray-50
      },
      theme: 'grid',
      // Configuración del pie de página
      didDrawPage: (data: any) => {
        // Añadir número de página al pie
        doc.setFontSize(10);
        const pageNumber = (doc as any).internal.getNumberOfPages();
        doc.text(`Página ${pageNumber}`, 
          doc.internal.pageSize.getWidth() / 2, 
          doc.internal.pageSize.getHeight() - 10, 
          { align: 'center' }
        );
        
        // Añadir encabezado
        doc.setFontSize(14);
        doc.setTextColor(16, 185, 129); // Color emerald-600
        doc.text('Batistella', doc.internal.pageSize.getWidth() - 15, 15, { align: 'right' });
      }
    });
    
    // Abrir el PDF en una nueva ventana/pestaña
    doc.output('dataurlnewwindow');
  }
  
  /**
   * Agrupa los pedidos por barrio
   * @param pedidos Lista de pedidos a agrupar
   * @returns Lista de grupos de pedidos por barrio
   */
  private agruparPedidosPorBarrio(pedidos: any[]): PedidoAgrupadoPorBarrio[] {
    // Objeto para agrupar pedidos por barrio
    const grupos: { [barrio: string]: any[] } = {};
    
    // Grupo especial para "Retiro en el local"
    let pedidosRetiroLocal: any[] = [];
    
    // Recorrer todos los pedidos
    pedidos.forEach(pedido => {
      // Detectar si es retiro en local
      if (this.esRetiroEnLocal(pedido.domicilio)) {
        pedidosRetiroLocal.push(pedido);
        return; // Continuar con el siguiente pedido
      }
      
      // Extraer el barrio del domicilio
      const barrio = this.extraerBarrio(pedido.domicilio);
      
      // Si el barrio no existe en el objeto, crearlo
      if (!grupos[barrio]) {
        grupos[barrio] = [];
      }
      
      // Añadir el pedido al grupo correspondiente
      grupos[barrio].push(pedido);
    });
    
    // Convertir el objeto a un array de grupos
    const resultado: PedidoAgrupadoPorBarrio[] = Object.entries(grupos).map(([barrio, pedidosBarrio]) => {
      const totalBarrio = pedidosBarrio.reduce((sum, pedido) => sum + pedido.total, 0);
      const pedidosPagados = pedidosBarrio.filter(pedido => pedido.estadoPago === 'COMPLETADO').length;
      
      return {
        barrio,
        pedidos: pedidosBarrio,
        total: totalBarrio,
        pedidosPagados,
        porcentajePagados: pedidosBarrio.length > 0 ? Math.round((pedidosPagados / pedidosBarrio.length) * 100) : 0
      };
    });
    
    // Ordenar por nombre de barrio
    resultado.sort((a, b) => a.barrio.localeCompare(b.barrio));
    
    // Añadir grupo de "Retiro en el local" al final si tiene pedidos
    if (pedidosRetiroLocal.length > 0) {
      const totalRetiroLocal = pedidosRetiroLocal.reduce((sum, pedido) => sum + pedido.total, 0);
      const pedidosPagadosRetiroLocal = pedidosRetiroLocal.filter(pedido => pedido.estadoPago === 'COMPLETADO').length;
      
      resultado.push({
        barrio: 'RETIRO EN EL LOCAL',
        pedidos: pedidosRetiroLocal,
        total: totalRetiroLocal,
        pedidosPagados: pedidosPagadosRetiroLocal,
        porcentajePagados: pedidosRetiroLocal.length > 0 ? Math.round((pedidosPagadosRetiroLocal / pedidosRetiroLocal.length) * 100) : 0
      });
    }
    
    return resultado;
  }
  
  /**
   * Determina si un domicilio corresponde a "Retiro en el local"
   * @param domicilio Domicilio a verificar
   * @returns true si es retiro en local, false en caso contrario
   */
  private esRetiroEnLocal(domicilio: string | undefined): boolean {
    if (!domicilio) return false;
    
    const textoNormalizado = this.normalizarTexto(domicilio);
    
    // Patrones que indican retiro en local
    const patronesRetiroLocal = [
      /RETIRO EN( EL)? LOCAL/i,
      /RETIRA EN( EL)? LOCAL/i,
      /RETIRO EN( LA)? TIENDA/i,
      /PICK ?UP/i,
      /SIN ENVIO/i
    ];
    
    // Si cualquiera de los patrones coincide, es retiro en local
    return patronesRetiroLocal.some(patron => patron.test(textoNormalizado));
  }
  
  /**
   * Extrae el barrio de una dirección completa
   * @param domicilio Dirección completa
   * @returns Nombre del barrio extraído
   */
  private extraerBarrio(domicilio: string | undefined): string {
    if (!domicilio) return 'SIN ESPECIFICAR';
    
    // Si es retiro en local, tratarlo como caso especial
    if (this.esRetiroEnLocal(domicilio)) {
      return 'RETIRO EN EL LOCAL';
    }
    
    // Normalizar texto: eliminar acentos, convertir a mayúsculas, etc.
    const textoNormalizado = this.normalizarTexto(domicilio);
    
    // Patrones comunes para barrios
    const patronesBarrio = [
      // Barrio explícito
      /\b(?:BARRIO|B°|BO)\s+([^,;.]+)/,
      
      // Barrios conocidos (algunos comunes en Córdoba)
      /\b(NUEVA CORDOBA|ALTA CORDOBA|GENERAL PAZ|CENTRO|SAN VICENTE|GUEMES|JARDIN|ALBERDI|CERRO DE LAS ROSAS|JUNIORS|URCA|PUEYRREDON|ARGUELLO|ALTO ALBERDI|PARQUE CAPITAL|SAN MARTIN|COFICO|ALTO VERDE|LAS PALMAS|QUINTAS DE SANTA ANA|CHACRAS DE LA VILLA|VALLE ESCONDIDO|LAS DELICIAS)\b/,
      
      // Después de la primera coma (formato típico: calle, barrio, cp)
      /^[^,]+,\s*([^,;.]+)/,
      
      // Antes de un código postal (formato típico: dirección barrio CP)
      /([^,;.]+)\s+(?:CP|C\.P\.|X\d{4})/
    ];
    
    // Probar cada patrón
    for (const patron of patronesBarrio) {
      const match = textoNormalizado.match(patron);
      if (match && match[1]) {
        return match[1].trim();
      }
    }
    
    // Si no se encuentra un barrio específico, intentar inferirlo de la dirección
    const partes = textoNormalizado.split(/[,;]/);
    
    // Si hay más de una parte, tomamos la segunda (que suele ser el barrio)
    if (partes.length >= 2) {
      return partes[1].trim();
    }
    
    // Si todo falla, usamos "OTROS" como categoría por defecto
    return 'OTROS';
  }
  
  /**
   * Normaliza un texto para búsqueda: quita acentos, convierte a mayúsculas, etc.
   * @param texto Texto a normalizar
   * @returns Texto normalizado
   */
  private normalizarTexto(texto: string): string {
    // Convertir a mayúsculas
    let resultado = texto.toUpperCase();
    
    // Eliminar acentos y caracteres especiales
    resultado = resultado.normalize("NFD").replace(/[\u0300-\u036f]/g, "");
    
    // Reemplazar caracteres especiales y múltiples espacios
    resultado = resultado.replace(/[^\w\s,;.]/g, " ").replace(/\s+/g, " ").trim();
    
    return resultado;
  }

  /**
   * Genera un PDF detallado de un solo pedido
   * @param pedido Pedido a imprimir
   */
  generarPDFDetallePedido(pedido: any): void {
    // Crear un nuevo documento PDF
    const doc = new jsPDF();
    const fechaActual = new Date().toLocaleDateString('es-AR');
    
    // Título y encabezado
    doc.setFontSize(18);
    doc.setTextColor(4, 120, 87); // Color emerald-800
    doc.text(`Detalle de Pedido: ${pedido.codigoPedido || pedido.id}`, doc.internal.pageSize.getWidth() / 2, 20, { align: 'center' });
    
    // Información adicional
    doc.setFontSize(10);
    doc.setTextColor(0);
    doc.text(`Fecha de impresión: ${fechaActual}`, doc.internal.pageSize.getWidth() - 15, 30, { align: 'right' });
    
    // Sección: Datos del Cliente
    doc.setFontSize(14);
    doc.setTextColor(4, 120, 87); // Color emerald-800
    doc.text('Datos del Cliente', 15, 40);
    
    // Tabla de datos del cliente
    const clienteColumns = ['Campo', 'Valor'];
    const clienteRows = [
      ['Cliente', pedido.nombreCompletoUsuario || '-'],
      ['Email', pedido.email || '-'],
      ['Domicilio', pedido.domicilio || '-']
    ];
    
    autoTable(doc, {
      head: [clienteColumns],
      body: clienteRows,
      startY: 45,
      styles: {
        fontSize: 9,
        cellPadding: 3,
      },
      headStyles: {
        fillColor: [240, 253, 244], // Color emerald-50
        textColor: [4, 120, 87], // Color emerald-800
        fontStyle: 'bold',
      },
      alternateRowStyles: {
        fillColor: [249, 250, 251], // Color gray-50
      },
      theme: 'grid'
    });
    
    let finalY = (doc as any).lastAutoTable.finalY;
    
    // Sección: Datos del Pedido
    doc.setFontSize(14);
    doc.setTextColor(4, 120, 87); // Color emerald-800
    doc.text('Datos del Pedido', 15, finalY + 15);
    
    // Tabla de datos del pedido
    const pedidoColumns = ['Campo', 'Valor'];
    const pedidoRows = [
      ['Código', pedido.codigoPedido || pedido.id || '-'],
      ['Fecha', this.formatearFecha(pedido.fechaPedido)],
      ['Estado', pedido.estado || '-'],
      ['Método de Pago', pedido.metodoPago || '-'],
      ['Estado de Pago', pedido.estadoPago || '-']
    ];
    
    autoTable(doc, {
      head: [pedidoColumns],
      body: pedidoRows,
      startY: finalY + 20,
      styles: {
        fontSize: 9,
        cellPadding: 3,
      },
      headStyles: {
        fillColor: [240, 253, 244], // Color emerald-50
        textColor: [4, 120, 87], // Color emerald-800
        fontStyle: 'bold',
      },
      alternateRowStyles: {
        fillColor: [249, 250, 251], // Color gray-50
      },
      theme: 'grid'
    });
    
    finalY = (doc as any).lastAutoTable.finalY;
    
    // Sección: Productos
    doc.setFontSize(14);
    doc.setTextColor(4, 120, 87); // Color emerald-800
    doc.text('Productos', 15, finalY + 15);
    
    // Tabla de productos
    const productosColumns = ['Producto', 'Cantidad', 'Precio Unit.', 'Subtotal'];
    const productosRows = pedido.productos.map((producto: any) => [
      producto.nombreProducto,
      producto.cantidad,
      `$${this.formatearNumero(producto.precioUnitario)}`,
      `$${this.formatearNumero(producto.subtotal)}`
    ]);
    
    // Añadir fila de total
    productosRows.push([
      { content: 'Total', colSpan: 3, styles: { fontStyle: 'bold', halign: 'right' } },
      { content: `$${this.formatearNumero(pedido.total)}`, styles: { fontStyle: 'bold' } }
    ]);
    
    autoTable(doc, {
      head: [productosColumns],
      body: productosRows,
      startY: finalY + 20,
      styles: {
        fontSize: 9,
        cellPadding: 3,
        lineColor: [221, 221, 221],
        lineWidth: 0.1,
      },
      headStyles: {
        fillColor: [240, 253, 244], // Color emerald-50
        textColor: [4, 120, 87], // Color emerald-800
        fontStyle: 'bold',
        halign: 'center'
      },
      columnStyles: {
        0: { cellWidth: 'auto' },
        1: { halign: 'center' },
        2: { halign: 'right' },
        3: { halign: 'right' }
      },
      // Configuración del pie de página
      didDrawPage: (data: any) => {
        // Añadir número de página al pie
        doc.setFontSize(10);
        const pageNumber = (doc as any).internal.getNumberOfPages();
        doc.text(`Página ${pageNumber}`, 
          doc.internal.pageSize.getWidth() / 2, 
          doc.internal.pageSize.getHeight() - 10, 
          { align: 'center' }
        );
        
        // Añadir encabezado
        doc.setFontSize(14);
        doc.setTextColor(16, 185, 129); // Color emerald-600
        doc.text('Batistella', doc.internal.pageSize.getWidth() - 15, 15, { align: 'right' });
      },
      margin: { top: 40 }
    });
    
    // Abrir el PDF en una nueva ventana/pestaña
    doc.output('dataurlnewwindow');
  }

  /**
   * Formatea un número para su visualización
   * @param numero Número a formatear
   * @returns Número formateado con separador de miles
   */
  private formatearNumero(numero: number): string {
    return numero.toLocaleString('es-AR');
  }

  /**
   * Formatea una fecha para su visualización
   * @param fecha Fecha a formatear
   * @returns Fecha formateada
   */
  private formatearFecha(fecha: string): string {
    try {
      // Primero intentamos convertir al formato local
      return new Date(fecha).toLocaleDateString('es-AR');
    } catch (error) {
      // Si hay error, devolvemos la fecha original
      return fecha;
    }
  }

  /**
   * Formatea un porcentaje añadiendo un indicador visual
   * @param porcentaje Porcentaje a formatear
   * @returns Porcentaje formateado con indicador visual
   */
  private formatearPorcentaje(porcentaje: number): string {
    let indicador = '';
    
    if (porcentaje < 30) {
      indicador = '↓ '; // Bajo
    } else if (porcentaje < 60) {
      indicador = '→ '; // Medio
    } else {
      indicador = '↑ '; // Alto
    }
    
    return `${indicador}${porcentaje}%`;
  }
} 