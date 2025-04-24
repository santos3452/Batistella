import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { ProductCardComponent } from '../product-card/product-card.component';
import { ProductService, Product } from '../../Services/Product/product.service';
import { Subscription } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
  standalone: true,
  imports: [CommonModule, ProductCardComponent]
})
export class HomeComponent implements OnInit, OnDestroy {
  products: Product[] = [];
  categoryFilter: string | null = null;
  categoryTitle = 'Todos los Productos';
  private subscription: Subscription = new Subscription();

  private categoryTitles: Record<string, string> = {
    'PERROS': 'Productos para Perros',
    'GATOS': 'Productos para Gatos',
    'GRANJA': 'Productos para Animales de Granja'
  };

  constructor(
    private productService: ProductService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.subscription.add(
      this.route.queryParamMap.subscribe((params: ParamMap) => {
        this.categoryFilter = params.get('category');
        this.updateCategoryTitle();
        this.loadProducts();
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private loadProducts(): void {
    if (this.categoryFilter) {
      const filterCategory = this.categoryFilter.toUpperCase();
      this.subscription.add(
        this.productService.getProducts()
          .pipe(
            map(products => products.filter(product => 
              product.animalType && product.animalType.toUpperCase() === filterCategory
            ))
          )
          .subscribe(products => {
            this.products = products;
          })
      );
    } else {
      this.subscription.add(
        this.productService.getProducts().subscribe(products => {
          this.products = products;
        })
      );
    }
  }

  private updateCategoryTitle(): void {
    if (this.categoryFilter) {
      const filterCategory = this.categoryFilter.toUpperCase();
      if (this.categoryTitles[filterCategory]) {
        this.categoryTitle = this.categoryTitles[filterCategory];
      } else {
        this.categoryTitle = 'Todos los Productos';
      }
    } else {
      this.categoryTitle = 'Todos los Productos';
    }
  }
}