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

  deleteProduct(id: string | number | undefined): void {
    if (id === undefined || id === null) return;
    
    if (confirm('¿Estás seguro de que deseas eliminar este producto?')) {
      this.productService.deleteProduct(id)
        .subscribe({
          next: () => {
            this.utils.showToast('success', 'Producto eliminado con éxito');
            this.loadProducts();
          },
          error: (error) => {
            console.error('Error deleting product', error);
            this.utils.showToast('error', 'Error al eliminar el producto');
          }
        });
    }
  }
} 