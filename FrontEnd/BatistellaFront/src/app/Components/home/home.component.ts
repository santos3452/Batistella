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

  constructor(
    private productService: ProductService,
    private route: ActivatedRoute,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Suscribirse a los cambios de ruta para filtrar por categoría o marca
    this.subscription.add(
      this.route.queryParamMap.subscribe(params => {
        this.categoryFilter = params.get('category');
        this.marcaFilter = params.get('marca');
        
        // Actualizar el título según el filtro aplicado
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

    // Verificar si el usuario es administrador
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
      // Filtrar productos por categoría
      this.productService.getProductsGroupedByWeightByCategory(this.categoryFilter).subscribe(groups => {
        this.productGroups = groups;
        
        // Si también hay filtro por marca, aplicarlo después
        if (this.marcaFilter) {
          this.filterGroupsByMarca();
        }
      });
    } else if (this.marcaFilter) {
      // Cargar todos los productos y filtrar por marca
      this.productService.getProductsGroupedByWeight().subscribe(groups => {
        this.productGroups = groups;
        this.filterGroupsByMarca();
      });
    } else {
      // Cargar todos los productos activos agrupados
      this.productService.getProductsGroupedByWeight().subscribe(groups => {
        this.productGroups = groups;
      });
    }
  }

  // Método para filtrar grupos de productos por marca
  private filterGroupsByMarca(): void {
    if (this.marcaFilter) {
      this.productGroups = this.productGroups.filter(group => 
        group.baseProduct.marca === this.marcaFilter
      );
    }
  }
}