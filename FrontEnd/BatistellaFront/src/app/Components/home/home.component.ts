import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, ParamMap, RouterLink, Router } from '@angular/router';
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
  pageSize = 12;
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
    private route: ActivatedRoute,
    private router: Router
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
        this.sortProductsByCategory();
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

  private sortProductsByCategory(): void {
    // Definir el orden de prioridad por marca: Top Nutrition -> Kenl -> Otras marcas
    const brandOrder: Record<string, number> = {
      'TopNutrition': 1,
      'TOPNUTRITION': 1,
      'Top Nutrition': 1,
      'Kenl': 2,
      'KENL': 2
    };

    this.productGroups.sort((a, b) => {
      const brandA = a.baseProduct.marca || 'UNKNOWN';
      const brandB = b.baseProduct.marca || 'UNKNOWN';
      
      const orderA = brandOrder[brandA] || 999;
      const orderB = brandOrder[brandB] || 999;
      
      // Si tienen la misma prioridad de marca, ordenar alfabéticamente por marca y luego por nombre
      if (orderA === orderB) {
        // Primero ordenar por marca
        if (brandA !== brandB) {
          return brandA.localeCompare(brandB);
        }
        // Si es la misma marca, ordenar por nombre del producto
        const nameA = a.baseProduct.fullName || a.baseProduct.nombre || '';
        const nameB = b.baseProduct.fullName || b.baseProduct.nombre || '';
        return nameA.localeCompare(nameB);
      }
      
      return orderA - orderB;
    });
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
  
  // Obtiene las páginas visibles para mostrar en la paginación
  getVisiblePages(): number[] {
    const pages: number[] = [];
    
    // Si hay 5 o menos páginas, mostrar todas
    if (this.totalPages <= 5) {
      for (let i = 1; i <= this.totalPages; i++) {
        pages.push(i);
      }
      return pages;
    }
    
    // Lógica para más de 5 páginas
    if (this.page <= 3) {
      // Al inicio: mostrar 1, 2, 3, 4, 5
      for (let i = 1; i <= Math.min(5, this.totalPages); i++) {
        pages.push(i);
      }
    } else if (this.page >= this.totalPages - 2) {
      // Al final: mostrar las últimas 5 páginas
      for (let i = Math.max(1, this.totalPages - 4); i <= this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      // En el medio: mostrar página actual y 2 a cada lado
      for (let i = this.page - 2; i <= this.page + 2; i++) {
        if (i >= 1 && i <= this.totalPages) {
          pages.push(i);
        }
      }
    }
    
    return pages;
  }


}