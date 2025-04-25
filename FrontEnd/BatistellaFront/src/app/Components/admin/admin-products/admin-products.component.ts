import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ProductService, Product } from '../../../Services/Product/product.service';
import { UtilsService } from '../../../Services/Utils/utils.service';

@Component({
  selector: 'app-admin-products',
  templateUrl: './admin-products.component.html',
  styleUrl: './admin-products.component.css',
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class AdminProductsComponent implements OnInit {
  products: Product[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(
    private productService: ProductService,
    public utils: UtilsService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.isLoading = true;
    this.productService.getProducts().subscribe({
      next: (data) => {
        this.products = data;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Error al cargar los productos: ' + error.message;
        this.isLoading = false;
      }
    });
  }

  deleteProduct(productId: string | undefined): void {
    if (!productId) return; // Si no hay ID, no hacer nada
    
    if (confirm('¿Estás seguro de que deseas eliminar este producto?')) {
      this.productService.deleteProduct(productId).subscribe({
        next: () => {
          this.products = this.products.filter(p => p.id !== productId);
        },
        error: (error) => {
          this.errorMessage = 'Error al eliminar el producto: ' + error.message;
        }
      });
    }
  }
} 