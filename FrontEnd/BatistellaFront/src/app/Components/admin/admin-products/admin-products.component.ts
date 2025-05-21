import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ProductService } from '../../../Services/Product/product.service';
import { UtilsService } from '../../../Services/Utils/utils.service';
import { FormsModule } from '@angular/forms';
import { Product } from '../../../Services/Product/product.service';

@Component({
  selector: 'app-admin-products',
  templateUrl: './admin-products.component.html',
  styleUrls: ['./admin-products.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule]
})
export class AdminProductsComponent implements OnInit {
  products: Product[] = [];
  filteredProducts: Product[] = [];
  pagedProducts: Product[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  
  // Para usar Math en la plantilla
  Math = Math;
  
  // Paginación
  page = 1;
  pageSize = 10;
  totalPages = 0;
  
  // Filtros
  filterName: string = '';
  filterBrand: string = '';
  filterType: string = '';
  filterWeight: string = '';
  filterStatus: string = '';
  
  // Opciones de filtros
  brands: string[] = [];
  types: string[] = [];
  weights: string[] = [];
  statuses: string[] = ['Activo', 'Inactivo'];

  // Filtros para productos de granja
  filterCategoriaGranja: string = '';
  categoriasGranja: string[] = [];

  // Modal de confirmación
  showConfirmModal = false;
  productToToggle: Product | null = null;

  // Modal actualización de precios
  showUpdatePricesModal = false;
  selectedBrand: string = 'TODAS';
  updatePercentage: number = 0;
  isUpdatingPrices = false;

  // Control de vista de productos
  activeTab: 'MASCOTAS' | 'GRANJA' = 'MASCOTAS';

  constructor(
    private productService: ProductService,
    public utils: UtilsService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.productService.getProducts()
      .subscribe({
        next: (products) => {
          this.products = products;
          this.filteredProducts = [...products];
          this.extractFilterOptions();
          this.updatePagination();
          this.isLoading = false;
          this.applyFilters();
        },
        error: (error) => {
          console.error('Error fetching products', error);
          this.errorMessage = 'Ocurrió un error al cargar los productos. Por favor, intenta de nuevo más tarde.';
          this.isLoading = false;
        }
      });
  }

  extractFilterOptions(): void {
    // Extraer marcas únicas (para productos de mascotas)
    this.brands = [...new Set(this.products
      .filter(p => p.animalType === 'PERROS' || p.animalType === 'GATOS')
      .map(p => p.marca))]
      .filter(Boolean)
      .sort();
    
    // Extraer tipos únicos (para productos de mascotas)
    this.types = [...new Set(this.products
      .filter(p => p.animalType === 'PERROS' || p.animalType === 'GATOS')
      .map(p => p.tipoAlimento))]
      .filter(Boolean)
      .sort();
    
    // Extraer categorías de granja y cereales únicas
    this.categoriasGranja = [...new Set(this.products
      .filter(p => p.animalType === 'GRANJA' || p.animalType === 'CEREAL')
      .map(p => {
        // Para cereales, usamos "CEREAL" como categoría
        if (p.animalType === 'CEREAL') {
          return 'CEREAL';
        }
        return p.categoriaGranja;
      })
      .filter((categoria): categoria is string => categoria !== undefined))]
      .filter(Boolean)
      .sort();
    
    // Extraer pesos únicos
    this.weights = [...new Set(this.products.map(p => p.kg?.toString()))]
      .filter(Boolean)
      .sort((a, b) => parseFloat(a) - parseFloat(b));
  }

  setActiveTab(tab: 'MASCOTAS' | 'GRANJA'): void {
    this.activeTab = tab;
    this.resetFilters();
    this.page = 1;
    this.applyFilters();
  }

  applyFilters(): void {
    // Primero filtrar por el tipo de animal (mascota o granja/cereal)
    let filteredByType: Product[];
    
    if (this.activeTab === 'MASCOTAS') {
      filteredByType = this.products.filter(product => 
        product.animalType === 'PERROS' || product.animalType === 'GATOS'
      );
    } else {
      filteredByType = this.products.filter(product => 
        product.animalType === 'GRANJA' || product.animalType === 'CEREAL'
      );
    }
    
    // Luego aplicar el resto de filtros según el tipo
    this.filteredProducts = filteredByType.filter(product => {
      // Filtro por nombre (común para ambos tipos)
      const nameMatch = !this.filterName || 
        product.fullName?.toLowerCase().includes(this.filterName.toLowerCase()) ||
        (product.nombre && product.nombre.toLowerCase().includes(this.filterName.toLowerCase()));
      
      // Filtro por estado (común para ambos tipos)
      const statusMatch = !this.filterStatus || 
        (this.filterStatus === 'Activo' && product.activo === true) ||
        (this.filterStatus === 'Inactivo' && product.activo === false);
      
      // Filtro por peso (común para ambos tipos)
      const weightMatch = !this.filterWeight || 
        product.kg?.toString() === this.filterWeight;
      
      if (this.activeTab === 'MASCOTAS') {
        // Filtros específicos para mascotas
        const brandMatch = !this.filterBrand || 
          product.marca === this.filterBrand;
        
        const typeMatch = !this.filterType || 
          product.tipoAlimento === this.filterType;
        
        return nameMatch && brandMatch && typeMatch && weightMatch && statusMatch;
      } else {
        // Filtros específicos para granja y cereales
        let categoriaGranjaMatch = true;
        
        if (this.filterCategoriaGranja) {
          if (this.filterCategoriaGranja === 'CEREAL') {
            // Si se filtró por CEREAL, solo mostrar productos de tipo CEREAL
            categoriaGranjaMatch = product.animalType === 'CEREAL';
          } else {
            // Si se filtró por otra categoría, solo mostrar productos de GRANJA con esa categoría
            categoriaGranjaMatch = product.categoriaGranja === this.filterCategoriaGranja && product.animalType === 'GRANJA';
          }
        }
        
        return nameMatch && categoriaGranjaMatch && weightMatch && statusMatch;
      }
    });
    
    // Actualizar la paginación después de aplicar los filtros
    this.updatePagination();
  }

  resetFilters(): void {
    this.filterName = '';
    this.filterBrand = '';
    this.filterType = '';
    this.filterWeight = '';
    this.filterStatus = '';
    this.filterCategoriaGranja = '';
    
    this.applyFilters();
  }

  // Métodos de paginación
  updatePagination(): void {
    this.totalPages = Math.ceil(this.filteredProducts.length / this.pageSize);
    this.loadPagedProducts();
  }

  loadPagedProducts(): void {
    const start = (this.page - 1) * this.pageSize;
    const end = start + this.pageSize;
    this.pagedProducts = this.filteredProducts.slice(start, end);
  }

  prevPage(): void {
    if (this.page > 1) {
      this.page--;
      this.loadPagedProducts();
      window.scrollTo(0, 0);
    }
  }

  nextPage(): void {
    if (this.page < this.totalPages) {
      this.page++;
      this.loadPagedProducts();
      window.scrollTo(0, 0);
    }
  }

  goToPage(n: number): void {
    if (n >= 1 && n <= this.totalPages) {
      this.page = n;
      this.loadPagedProducts();
      window.scrollTo(0, 0);
    }
  }

  confirmToggleProductStatus(product: Product): void {
    if (!product || !product.id) return;
    
    this.productToToggle = product;
    this.showConfirmModal = true;
  }

  cancelToggleStatus(): void {
    this.showConfirmModal = false;
    this.productToToggle = null;
  }

  toggleProductStatus(): void {
    if (!this.productToToggle || !this.productToToggle.id) return;
    
    this.isLoading = true;
    
    // Guardamos el estado actual para el mensaje
    const wasActive = this.productToToggle.activo;
    
    this.productService.deleteProduct(this.productToToggle.id)
      .subscribe({
        next: () => {
          // Mensaje basado en el estado anterior
          const action = wasActive ? 'desactivado' : 'activado';
          this.utils.showToast('success', `Producto ${action} con éxito`);
          
          // Limpiar la caché de productos para forzar la recarga desde el servidor
          this.productService.clearProductsCache();
          
          // Pequeño delay para asegurar que el backend procesó el cambio
          setTimeout(() => {
            this.loadProducts();
            this.showConfirmModal = false;
            this.productToToggle = null;
            this.isLoading = false;
          }, 300);
        },
        error: (error) => {
          console.error('Error changing product status', error);
          
          // Verificar si realmente es un error o solo un problema de tipo de respuesta
          if (error.status === 200) {
            // Es un "error" pero con código 200, lo tratamos como éxito
            const action = wasActive ? 'desactivado' : 'activado';
            this.utils.showToast('success', `Producto ${action} con éxito`);
            
            // Limpiar la caché y recargar
            this.productService.clearProductsCache();
            setTimeout(() => {
              this.loadProducts();
            }, 300);
          } else {
            // Es un error real
            this.utils.showToast('error', 'Error al cambiar el estado del producto');
          }
          
          this.showConfirmModal = false;
          this.productToToggle = null;
          this.isLoading = false;
        }
      });
  }

  // Formatea la fecha de última actualización de forma amigable
  formatUpdatedAt(dateString: string): string {
    if (!dateString) return 'No disponible';
    
    try {
      // Verificar si el formato es DD/MM/YYYY HH:MM:SS
      if (dateString.includes('/')) {
        // Ya está en formato deseado, solo necesitamos formatear la hora
        const parts = dateString.split(' ');
        // Devolvemos la fecha y la hora en formato más legible
        return parts[0] + ' ' + parts[1].substring(0, 5); // Solo mostrar HH:MM
      }
      
      // Si es formato ISO, convertir a formato DD/MM/YYYY HH:MM
      const date = new Date(dateString);
      
      if (isNaN(date.getTime())) {
        return 'Fecha inválida';
      }
      
      const day = date.getDate().toString().padStart(2, '0');
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const year = date.getFullYear();
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      
      return `${day}/${month}/${year} ${hours}:${minutes}`;
    } catch (error) {
      console.error('Error parsing date', error);
      return 'Fecha inválida';
    }
  }

  // Determina si un número de página debe mostrarse basado en la página actual
  showPageNumber(pageNum: number): boolean {
    // Lógica para mostrar números de página apropiados
    if (this.totalPages <= 5) {
      // Si hay 5 o menos páginas, mostrar todos los números
      return true;
    }
    
    // Siempre mostrar la primera y última página
    if (pageNum === 1 || pageNum === this.totalPages) {
      return true;
    }
    
    // Para páginas intermedias, mostrar la página actual y ±1 página
    return Math.abs(pageNum - this.page) <= 1;
  }

  getPageNumberToShow(index: number): number {
    if (this.totalPages <= 5) {
      // Si hay 5 o menos páginas, mostrar la página con el índice
      return index + 1;
    }
    
    // Si estamos cerca del inicio
    if (this.page <= 3) {
      return index + 1;
    }
    
    // Si estamos cerca del final
    if (this.page >= this.totalPages - 2) {
      return this.totalPages - (4 - index);
    }
    
    // En medio
    return this.page - 2 + index;
  }

  private getTimeAgo(date: Date): string {
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffSecs = Math.floor(diffMs / 1000);
    const diffMins = Math.floor(diffSecs / 60);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);
    
    if (diffDays > 0) {
      return `hace ${diffDays} día${diffDays > 1 ? 's' : ''}`;
    }
    if (diffHours > 0) {
      return `hace ${diffHours} hora${diffHours > 1 ? 's' : ''}`;
    }
    if (diffMins > 0) {
      return `hace ${diffMins} minuto${diffMins > 1 ? 's' : ''}`;
    }
    return 'justo ahora';
  }

  // Métodos para la actualización de precios
  openUpdatePricesModal(): void {
    this.selectedBrand = 'TODAS'; // Valor por defecto: todas las marcas
    this.updatePercentage = 0;
    this.showUpdatePricesModal = true;
  }

  closeUpdatePricesModal(): void {
    this.showUpdatePricesModal = false;
  }

  updatePrices(): void {
    // Validar que el porcentaje esté dentro de un rango razonable
    if (this.updatePercentage < -90 || this.updatePercentage > 100) {
      this.utils.showToast('error', 'El porcentaje debe estar entre -90% y 100%');
      return;
    }

    this.isUpdatingPrices = true;
    
    let mensaje: string;
    if (this.selectedBrand === 'TODAS') {
      mensaje = `Precios actualizados con éxito (${this.updatePercentage > 0 ? '+' : ''}${this.updatePercentage}%)`;
    } else {
      mensaje = `Precios de ${this.selectedBrand} actualizados con éxito (${this.updatePercentage > 0 ? '+' : ''}${this.updatePercentage}%)`;
    }
    
    this.productService.updatePricesByBrandAndPercentage(this.updatePercentage, this.selectedBrand)
      .subscribe({
        next: () => {
          this.utils.showToast('success', mensaje, true); // Usar true para mostrar como modal
          
          // Limpiar la caché de productos para forzar la recarga desde el servidor
          this.productService.clearProductsCache();
          
          // Pequeño delay para asegurar que el backend procesó los cambios
          setTimeout(() => {
            this.loadProducts();
            this.showUpdatePricesModal = false;
            this.isUpdatingPrices = false;
          }, 300);
        },
        error: (error) => {
          console.error('Error al actualizar precios', error);
          
          // Verificar si realmente es un error o un "éxito" con código 200
          if (error.status === 200) {
            this.utils.showToast('success', mensaje, true); // Usar true para mostrar como modal
            
            // Limpiar la caché y recargar
            this.productService.clearProductsCache();
            setTimeout(() => {
              this.loadProducts();
              this.showUpdatePricesModal = false;
              this.isUpdatingPrices = false;
            }, 300);
          } else {
            this.utils.showToast('error', 'Error al actualizar los precios', true); // Usar true para mostrar como modal
            this.showUpdatePricesModal = false;
            this.isUpdatingPrices = false;
          }
        }
      });
  }

  /**
   * Formatea un tipo de alimento para mostrarlo sin guiones bajos
   * @param type Tipo de alimento (con guiones bajos)
   * @returns Tipo formateado (con espacios)
   */
  formatTypeDisplay(type: string): string {
    if (!type) return '';
    return type.replace(/_/g, ' ');
  }
} 