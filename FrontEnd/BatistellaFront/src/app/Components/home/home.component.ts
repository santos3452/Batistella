import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, ParamMap, RouterLink } from '@angular/router';
import { ProductCardComponent } from '../product-card/product-card.component';
import { ProductService, Product } from '../../Services/Product/product.service';
import { AuthService } from '../../Services/Auth/auth.service';
import { Subscription } from 'rxjs';
import { map } from 'rxjs/operators';

interface ProductGroup {
  baseProduct: Product;
  variants: Array<{
    id: string | number;
    localId?: string;
    kg: string;
    priceMinorista: number;
    priceMayorista: number;
    stock: number;
  }>;
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
  standalone: true,
  imports: [CommonModule, ProductCardComponent, RouterLink]
})
export class HomeComponent implements OnInit, OnDestroy {
  products: Product[] = [];
  productGroups: ProductGroup[] = [];
  page = 1;
  pageSize = 15;
  totalPages = 0;
  categoryFilter: string | null = null;
  marcaFilter: string | null = null;
  tipoGranjaFilter: string | null = null;
  categoryTitle = 'Todos los Alimentos';
  isAdmin = false;
  private subscription: Subscription = new Subscription();
  
  // Para usar Math en la plantilla
  Math = Math;

  private categoryTitles: Record<string, string> = {
    'PERROS': 'Alimentos para Perros',
    'GATOS': 'Alimentos para Gatos',
    'GRANJA': 'Alimentos para Animales de Granja',
    'CEREAL': 'Cereales'
  };

  // Nombres de las categorías de granja (sin cereales)
  private tipoGranjaTitles: Record<string, string> = {
    'AVES': 'Alimentos para Aves',
    'PONEDORAS': 'Alimentos para Ponedoras',
    'CONEJOS': 'Alimentos para Conejos',
    'PORCINOS': 'Alimentos para Porcinos',
    'EQUINOS': 'Alimentos para Equinos',
    'VACUNOS': 'Alimentos para Vacunos',
    'VARIOS': 'Varios Alimentos para Granja'
  };

  get pagedProductGroups(): ProductGroup[] {
    const start = (this.page - 1) * this.pageSize;
    return this.productGroups.slice(start, start + this.pageSize);
  }

  constructor(
    private productService: ProductService,
    private authService: AuthService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.subscription.add(
      this.route.queryParamMap.subscribe(params => {
        this.categoryFilter = params.get('category');
        this.marcaFilter = params.get('marca');
        this.tipoGranjaFilter = params.get('tipoGranja');
        
        if (this.categoryFilter) {
          this.categoryTitle = this.categoryTitles[this.categoryFilter] || 'Productos';
        } else if (this.marcaFilter) {
          this.categoryTitle = `Productos de ${this.marcaFilter}`;
        } else if (this.tipoGranjaFilter) {
          this.categoryTitle = this.tipoGranjaTitles[this.tipoGranjaFilter] || 'Productos para Granja';
        } else {
          this.categoryTitle = 'Todos los Alimentos';
        }
        
        this.loadProductGroups();
      })
    );

    this.subscription.add(
      this.authService.currentUser$.subscribe(user => {
        this.isAdmin = user?.rol === 'ROLE_ADMIN';
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  loadProductGroups(): void {
    // Si hay filtro de categoría de granja, establecemos categoryFilter a GRANJA
    if (this.tipoGranjaFilter) {
      this.categoryFilter = 'GRANJA';
    }

    if (this.categoryFilter) {
      this.productService.getProductsGroupedByWeightByCategory(this.categoryFilter).subscribe(groups => {
        this.productGroups = groups;
        
        if (this.marcaFilter) {
          this.filterGroupsByMarca();
        }
        
        if (this.tipoGranjaFilter) {
          this.filterGroupsByTipoGranja();
        }
        
        this.setupPagination();
      });
    } else if (this.marcaFilter) {
      this.productService.getProductsGroupedByWeight().subscribe(groups => {
        this.productGroups = groups;
        this.filterGroupsByMarca();
        this.setupPagination();
      });
    } else {
      this.productService.getProductsGroupedByWeight().subscribe(groups => {
        this.productGroups = groups;
        this.setupPagination();
      });
    }
  }

  private filterGroupsByMarca(): void {
    if (this.marcaFilter) {
      this.productGroups = this.productGroups.filter(group => 
        group.baseProduct.marca === this.marcaFilter
      );
    }
  }

  private filterGroupsByTipoGranja(): void {
    if (this.tipoGranjaFilter) {
      this.productGroups = this.productGroups.filter(group => 
        group.baseProduct.categoriaGranja === this.tipoGranjaFilter
      );
    }
  }

  private setupPagination(): void {
    this.page = 1;
    this.totalPages = Math.ceil(this.productGroups.length / this.pageSize);
  }

  prevPage(): void {
    if (this.page > 1) {
      this.page--;
      window.scrollTo(0, 0);
    }
  }

  nextPage(): void {
    if (this.page < this.totalPages) {
      this.page++;
      window.scrollTo(0, 0);
    }
  }

  goToPage(n: number): void {
    if (n >= 1 && n <= this.totalPages) {
      this.page = n;
      window.scrollTo(0, 0);
    }
  }
  
  // Determina si un número de página debe mostrarse basado en la página actual
  showPageNumber(pageNum: number): boolean {
    // Si hay 5 o menos páginas, mostrar todas
    if (this.totalPages <= 5) return true;
    
    // Siempre mostrar la primera página
    if (pageNum === 1) return true;
    
    // Para páginas cercanas a la actual
    if (pageNum >= this.page - 1 && pageNum <= this.page + 1) return true;
    
    // No mostrar otras páginas
    return false;
  }

  // Obtiene el número de página que se debe mostrar en cada posición
  getPageNumberToShow(index: number): number {
    // Si hay 5 o menos páginas, mostrar secuencialmente
    if (this.totalPages <= 5) return index + 1;
    
    // Para la primera posición siempre mostrar página 1
    if (index === 0) return 1;
    
    // Si estamos en las primeras páginas
    if (this.page <= 3) {
      return index + 1;
    }
    
    // Si estamos cerca del final
    if (this.page >= this.totalPages - 2) {
      return this.totalPages - 4 + index;
    }
    
    // En medio del rango
    return this.page - 2 + index;
  }
}