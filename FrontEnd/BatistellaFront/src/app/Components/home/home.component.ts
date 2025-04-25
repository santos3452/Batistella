import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, ParamMap, RouterLink } from '@angular/router';
import { ProductCardComponent } from '../product-card/product-card.component';
import { ProductService, Product } from '../../Services/Product/product.service';
import { AuthService } from '../../Services/Auth/auth.service';
import { Subscription } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
  standalone: true,
  imports: [CommonModule, ProductCardComponent, RouterLink]
})
export class HomeComponent implements OnInit, OnDestroy {
  products: Product[] = [];
  categoryFilter: string | null = null;
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
    // Suscribirse a los cambios de ruta para filtrar por categoría
    this.subscription.add(
      this.route.queryParamMap.subscribe(params => {
        this.categoryFilter = params.get('category');
        
        if (this.categoryFilter) {
          this.categoryTitle = this.categoryTitles[this.categoryFilter] || 'Productos';
        } else {
          this.categoryTitle = 'Todos los Alimentos';
        }
        
        this.loadProducts();
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

  loadProducts(): void {
    if (this.categoryFilter) {
      // Filtrar productos por categoría
      this.productService.getProductsByCategory(this.categoryFilter).subscribe(products => {
        this.products = products;
      });
    } else {
      // Cargar todos los productos
      this.productService.getProducts().subscribe(products => {
        this.products = products;
      });
    }
  }
}