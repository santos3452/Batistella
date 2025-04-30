// app.component.ts
import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { NavbarComponent } from './Components/navbar/navbar.component';
import { SidebarComponent } from './Components/sidebar/sidebar.component';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavbarComponent, SidebarComponent, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
  standalone: true
})
export class AppComponent implements OnInit {
  sidebarOpen = false;
  isLoginRoute = false;
  isAuthRoute = false;
  currentYear = new Date().getFullYear();

  constructor(private router: Router) {}

  ngOnInit() {
    // Detectar cuando estamos en rutas de autenticación
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.isLoginRoute = event.url === '/login';
      this.isAuthRoute = event.url === '/login' || 
                        event.url === '/register' || 
                        event.url.startsWith('/reset-password');
    });

    // Verificar la ruta inicial
    const currentUrl = this.router.url;
    this.isLoginRoute = currentUrl === '/login';
    this.isAuthRoute = currentUrl === '/login' || 
                      currentUrl === '/register' || 
                      currentUrl.startsWith('/reset-password');
  }

  toggleSidebar() {
    this.sidebarOpen = !this.sidebarOpen;
  }

  closeSidebar() {
    this.sidebarOpen = false;
  }
}
