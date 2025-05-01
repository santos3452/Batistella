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
  isLoading = false;
  errorMessage: string | null = null;
  
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

  // Modal de confirmación
  showConfirmModal = false;
  productToToggle: Product | null = null;

  // Modal actualización de precios
  showUpdatePricesModal = false;
  selectedBrand: string = 'TODAS';
  updatePercentage: number = 0;
  isUpdatingPrices = false;

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
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error fetching products', error);
          this.errorMessage = 'Ocurrió un error al cargar los productos. Por favor, intenta de nuevo más tarde.';
          this.isLoading = false;
        }
      });
  }

  extractFilterOptions(): void {
    // Extraer marcas únicas
    this.brands = [...new Set(this.products.map(p => p.marca))].filter(Boolean).sort();
    
    // Extraer tipos únicos
    this.types = [...new Set(this.products.map(p => p.tipoAlimento))].filter(Boolean).sort();
    
    // Extraer pesos únicos
    this.weights = [...new Set(this.products.map(p => p.kg?.toString()))].filter(Boolean).sort((a, b) => parseFloat(a) - parseFloat(b));
  }

  applyFilters(): void {
    this.filteredProducts = this.products.filter(product => {
      // Filtro por nombre
      const nameMatch = !this.filterName || 
        product.fullName?.toLowerCase().includes(this.filterName.toLowerCase());
      
      // Filtro por marca
      const brandMatch = !this.filterBrand || 
        product.marca === this.filterBrand;
      
      // Filtro por tipo
      const typeMatch = !this.filterType || 
        product.tipoAlimento === this.filterType;
      
      // Filtro por peso
      const weightMatch = !this.filterWeight || 
        product.kg?.toString() === this.filterWeight;
      
      // Filtro por estado
      const statusMatch = !this.filterStatus || 
        (this.filterStatus === 'Activo' && product.activo === true) ||
        (this.filterStatus === 'Inactivo' && product.activo === false);
      
      return nameMatch && brandMatch && typeMatch && weightMatch && statusMatch;
    });
  }

  resetFilters(): void {
    this.filterName = '';
    this.filterBrand = '';
    this.filterType = '';
    this.filterWeight = '';
    this.filterStatus = '';
    this.filteredProducts = [...this.products];
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

    this.productService.updatePricesByBrandAndPercentage(this.updatePercentage, this.selectedBrand)
      .subscribe({
        next: () => {
          this.utils.showToast('success', `Precios actualizados con éxito (${this.updatePercentage > 0 ? '+' : ''}${this.updatePercentage}%)`);
          
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
            this.utils.showToast('success', `Precios actualizados con éxito (${this.updatePercentage > 0 ? '+' : ''}${this.updatePercentage}%)`);
            
            // Limpiar la caché y recargar
            this.productService.clearProductsCache();
            setTimeout(() => {
              this.loadProducts();
              this.showUpdatePricesModal = false;
              this.isUpdatingPrices = false;
            }, 300);
          } else {
            this.utils.showToast('error', 'Error al actualizar los precios');
            this.showUpdatePricesModal = false;
            this.isUpdatingPrices = false;
          }
        }
      });
  }
} 