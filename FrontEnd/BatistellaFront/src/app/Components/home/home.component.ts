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
  pageSize = 12;
  totalPages = 0;
  categoryFilter: string | null = null;
  marcaFilter: string | null = null;
  categoryTitle = 'Todos los Alimentos';
  isAdmin = false;
  private subscription: Subscription = new Subscription();

  private categoryTitles: Record<string, string> = {
    'PERROS': 'Alimentos para Perros',
    'GATOS': 'Alimentos para Gatos',
    'GRANJA': 'Alimentos para Animales de Granja'
  };

  get pagedProductGroups(): ProductGroup[] {
    const start = (this.page - 1) * this.pageSize;
    return this.productGroups.slice(start, start + this.pageSize);
  }

  constructor(
    private productService: ProductService,
    private route: ActivatedRoute,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.subscription.add(
      this.route.queryParamMap.subscribe(params => {
        this.categoryFilter = params.get('category');
        this.marcaFilter = params.get('marca');
        
        if (this.categoryFilter) {
          this.categoryTitle = this.categoryTitles[this.categoryFilter] || 'Productos';
        } else if (this.marcaFilter) {
          this.categoryTitle = `Productos de ${this.marcaFilter}`;
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
    if (this.categoryFilter) {
      this.productService.getProductsGroupedByWeightByCategory(this.categoryFilter).subscribe(groups => {
        this.productGroups = groups;
        
        if (this.marcaFilter) {
          this.filterGroupsByMarca();
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

  private setupPagination(): void {
    this.page = 1;
    this.totalPages = Math.ceil(this.productGroups.length / this.pageSize);
  }

  prevPage(): void {
    if (this.page > 1) {
      this.page--;
    }
  }

  nextPage(): void {
    if (this.page < this.totalPages) {
      this.page++;
    }
  }

  goToPage(n: number): void {
    if (n >= 1 && n <= this.totalPages) {
      this.page = n;
    }
  }
}