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
  
  // Iconos de animales (emojis) para el fondo dinámico
  animalIcons: string[] = [
    // Perros
    '🐶', '🐕', '🦮', '🐕‍🦺', '🐩', '🐺', '🦊',
    // Gatos
    '🐱', '🐈', '🐈‍⬛', '😸', '😹', '😻', '😼', '😽', '🙀', '😿', '😾',
    // Animales de granja
    '🐄', '🐮', '🐷', '🐖', '🐴', '🐎', '🦄', '🐔', '🐓', '🐑', '🐏', '🦙', '🐐', '🐂', '🦬', '🦃'
  ];
  
  // Cuadrícula para posiciones de iconos
  gridSize = 10; // Divisiones de la pantalla en filas y columnas
  usedPositions: {x: number, y: number}[] = [];
  
  // Datos precalculados para cada icono (para evitar reinicio en navegación)
  animalData: {x: number, y: number, icon: string, delay: number}[] = [];

  constructor(private router: Router) {
    // Generamos los datos de todos los iconos una sola vez al crear el componente
    this.generateAnimalData();
  }

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
  
  // Genera datos para todos los iconos una sola vez
  generateAnimalData() {
    this.animalData = [];
    for (let i = 0; i < 100; i++) {
      const position = this.getGridPosition(i);
      this.animalData.push({
        x: position.x,
        y: position.y,
        icon: this.animalIcons[Math.floor(Math.random() * this.animalIcons.length)],
        delay: i * 0.2
      });
    }
  }

  toggleSidebar() {
    this.sidebarOpen = !this.sidebarOpen;
  }

  closeSidebar() {
    this.sidebarOpen = false;
  }
  
  // Función para obtener una posición por cuadrícula para cada icono de animal
  getGridPosition(index: number): {x: number, y: number} {
    // Calcula fila y columna basadas en el índice
    // Con 100 iconos y una cuadrícula de 10x10, cada uno tendrá una celda
    const row = Math.floor(index / this.gridSize);
    const col = index % this.gridSize;
    
    // Agrega un poco de aleatoriedad dentro de cada celda para que no queden perfectamente alineados
    const jitterX = Math.random() * 5;
    const jitterY = Math.random() * 5;
    
    return {
      x: (col * 10) + jitterX,
      y: (row * 10) + jitterY
    };
  }
  
  // Obtener datos del icono por índice
  getAnimalIcon(index: number): string {
    return this.animalData[index].icon;
  }
  
  // Obtener posición X del icono
  getPositionX(index: number): number {
    return this.animalData[index].x;
  }
  
  // Obtener posición Y del icono
  getPositionY(index: number): number {
    return this.animalData[index].y;
  }
  
  // Obtener retraso de animación del icono
  getAnimationDelay(index: number): string {
    return this.animalData[index].delay + 's';
  }
  
  // Función para obtener una posición aleatoria para cada icono de animal (mantenemos para compatibilidad)
  getRandomPosition(): number {
    return Math.floor(Math.random() * 90) + 5; // Valores entre 5 y 95% para mantener dentro de la pantalla
  }
  
  // Función para obtener un emoji aleatorio de animal
  getRandomAnimal(): string {
    return this.animalIcons[Math.floor(Math.random() * this.animalIcons.length)];
  }
}
