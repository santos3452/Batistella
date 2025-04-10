import { Component, EventEmitter, Output, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CartService } from '../../Services/cart.service';
import { CartDropdownComponent } from '../cart-dropdown/cart-dropdown.component';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
  standalone: true,
  imports: [CommonModule, RouterLink, CartDropdownComponent]
})
export class NavbarComponent implements OnInit, OnDestroy {
  @Output() toggleSidebar = new EventEmitter<void>();
  
  totalItems = 0;
  showCart = false;
  isLoggedIn = false;
  private subscription: Subscription = new Subscription();

  constructor(private cartService: CartService) {}

  ngOnInit(): void {
    this.subscription.add(
      this.cartService.totalItems$.subscribe(total => {
        this.totalItems = total;
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  onToggleSidebar(): void {
    this.toggleSidebar.emit();
  }

  toggleCart(): void {
    this.showCart = !this.showCart;
  }

  logout(): void {
    this.isLoggedIn = false;
  }
}
