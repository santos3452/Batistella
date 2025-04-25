import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProductService, Product } from '../../Services/Product/product.service';
import { ProductCardComponent } from '../product-card/product-card.component';
import { UtilsService } from '../../Services/Utils/utils.service';

@Component({
  selector: 'app-search-results',
  templateUrl: './search-results.component.html',
  styleUrl: './search-results.component.css',
  standalone: true,
  imports: [CommonModule, ProductCardComponent, RouterLink]
})
export class SearchResultsComponent implements OnInit {
  searchQuery: string = '';
  products: Product[] = [];
  isLoading = true;
  noResults = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    public utils: UtilsService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.searchQuery = params['q'] || '';
      if (this.searchQuery) {
        this.searchProducts();
      } else {
        // Si no hay consulta, redirigir a la página principal
        this.router.navigate(['/']);
      }
    });
  }

  searchProducts(): void {
    this.isLoading = true;
    this.noResults = false;
    
    this.productService.searchProducts(this.searchQuery).subscribe({
      next: (results) => {
        this.products = results;
        this.isLoading = false;
        this.noResults = results.length === 0;
      },
      error: (error) => {
        console.error('Error al buscar productos:', error);
        this.isLoading = false;
        this.noResults = true;
      }
    });
  }
} 